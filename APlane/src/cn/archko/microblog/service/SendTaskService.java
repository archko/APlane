package cn.archko.microblog.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.HomeActivity;
import com.me.microblog.App;
import com.me.microblog.bean.SendTask;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.lang.ref.WeakReference;

/**
 * 队列服务，用于处理队列操作，发新微博，评论，回复，转发等。
 * 需要在解决了Oauth2认证的问题后再启用。
 *
 * @author archko
 */
public class SendTaskService extends Service {

    public static final String TAG="SendTaskService";
    private MyBinder myBinder=new MyBinder();
    private NotificationManager mNM;
    SharedPreferences settings;

    //---------------------------

    public static final String ACTION_NEW_TASK="cn.archko.microblog.service.new_task";
    public static final String ACTION_STOP_TASK="cn.archko.microblog.service.stop_task";

    public static final int MSG_START_TASK=0;
    public static final int MSG_RESTART_TASK=1;
    public static final int MSG_STOP_TASK=-10;

    public static final int TYPE_ORI_TASK=0;
    public static final int TYPE_RESTART_TASK=1;

    private ServiceHandler mServiceHandler;
    private SendTaskHandler mTaskHandler;

    @Override
    public IBinder onBind(Intent intent) {
        WeiboLog.d(TAG, "onBind");
        return myBinder;
    }

    //  重新绑定时调用该方法
    @Override
    public void onRebind(Intent intent) {
        WeiboLog.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    //  解除绑定时调用该方法
    @Override
    public boolean onUnbind(Intent intent) {
        WeiboLog.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder {

        SendTaskService getService() {
            return SendTaskService.this;
        }

    }

    private static final class ServiceHandler extends Handler {

        private final WeakReference<SendTaskService> mService;

        /**
         * Constructor of <code>ServiceHandler</code>
         *
         * @param service The service to use.
         * @param looper  The thread to run on.
         */
        public ServiceHandler(final SendTaskService service, final Looper looper) {
            super(looper);
            mService=new WeakReference<SendTaskService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            final SendTaskService service=mService.get();
            if (service==null) {
                WeiboLog.d(TAG, "service == null");
                return;
            }

            switch (msg.what) {
                case MSG_STOP_TASK: {
                    WeiboLog.d(TAG, "handle stop");
                    service.stopTask();
                }

                case MSG_START_TASK:
                default: {
                    service.doTask((Intent) msg.obj);
                }

                break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WeiboLog.d(TAG, "onCreate");
        mNM=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        settings=PreferenceManager.getDefaultSharedPreferences(this);
        final HandlerThread thread=new HandlerThread("ServiceHandler",
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Initialize the handler
        mServiceHandler=new ServiceHandler(this, thread.getLooper());
        initTaskHandler();

        IntentFilter filter=new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(SendTaskService.ACTION_NEW_TASK);
        registerReceiver(mServiceReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WeiboLog.d(TAG, "onDestroy");

        try {
            mNM.cancel(R.string.local_service_started);
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(mServiceReceiver);

        if (null!=mServiceHandler) {
            mServiceHandler.removeCallbacksAndMessages(null);
            mServiceHandler.getLooper().quit();
            mServiceHandler=null;
        }
        stopTaskHandler();
    }

    /**
     * 初始化任务线程
     */
    private void initTaskHandler() {
        final HandlerThread uploadHandler=new HandlerThread("UploadHandler",
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
        uploadHandler.start();
        mTaskHandler=new SendTaskHandler(this, uploadHandler.getLooper());
    }

    /**
     * 销毁任务线程
     */
    private void stopTaskHandler() {
        if (null!=mTaskHandler) {
            mTaskHandler.removeCallbacksAndMessages(null);
            mTaskHandler.getLooper().quit();
            mTaskHandler=null;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        WeiboLog.d(TAG, "onStart:"+" startId:"+startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WeiboLog.d(TAG, "onStartCommand,flags:"+flags+" startId:"+startId);

        addTaskToQueue(intent, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * 添加任务到队列中。
     *
     * @param intent
     * @param startId
     */
    void addTaskToQueue(Intent intent, int startId) {
        Message msg=mServiceHandler.obtainMessage();
        msg.arg1=startId;
        msg.what=MSG_START_TASK;
        msg.obj=intent;
        mServiceHandler.sendMessage(msg);
    }

    /**
     * 处理任务
     *
     * @param obj
     */
    public void doTask(Intent obj) {
        saveTask(obj);
        //store new task from intent
        //WeiboLog.d(TAG, "doTask:" + obj);
        //start upload thread
        if (null==mTaskHandler) {
            initTaskHandler();
        }
        Message msg=mTaskHandler.obtainMessage();
        msg.what=MSG_START_TASK;
        msg.obj=obj;
        doTask(msg);
    }

    /**
     * 将任务存储到数据库中.
     *
     * @param intent
     */
    private void saveTask(Intent intent) {
        if (intent==null||null==intent.getSerializableExtra("send_task")) {
            return;
        }
        SendTask task=(SendTask) intent.getSerializableExtra("send_task");
        int type=intent.getIntExtra("type", TYPE_ORI_TASK);
        WeiboLog.d(TAG, "添加任务:"+task);
        if (type==TYPE_ORI_TASK) {
            SendTaskHandler.addTask(task);
        } else {
            SqliteWrapper.updateSendTask(App.getAppContext(), SendTask.CODE_INIT, "", task);
        }
    }

    public void doTask(Message msg) {
        mTaskHandler.sendMessage(msg);
    }

    public void stopTask() {
        WeiboLog.d(TAG, "stopTask");
        stopTaskHandler();
    }

    public void doNotify(int msgId) {
        // 创建一个通知
        Notification notification=new Notification(R.drawable.logo, "", System.currentTimeMillis());
        // 指定这个通知的布局文件
        RemoteViews remoteViews=new RemoteViews(getPackageName(), R.layout.custom_remoteview);

        // 设置通知显示的内容
        remoteViews.setTextViewText(R.id.title, getString(msgId));

        notification.tickerText=getString(msgId);
        // 将内容指定给通知
        notification.contentView=remoteViews;
        // 指定点击通知后跳到那个Activity
        notification.contentIntent=PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // //或者启动一个Service
        // notification.contentIntent=PendingIntent.getService(
        // NotificationDemoActivity.this, 0,new
        // Intent(NotificationDemoActivity.this,NotificationDemoActivity.class),
        // PendingIntent.FLAG_UPDATE_CURRENT);

        // //或者启动一个Broadcast广播
        // notification.contentIntent=PendingIntent.getBroadcast(
        // NotificationDemoActivity.this, 0,new
        // Intent(NotificationDemoActivity.this,NotificationDemoActivity.class),
        // PendingIntent.FLAG_UPDATE_CURRENT);

        // 指定通知可以清除
        notification.flags|=Notification.FLAG_AUTO_CANCEL;
        // 指定通知不能清除
        // notification.flags|=Notification.FLAG_NO_CLEAR;
        // 通知显示的时候播放默认声音
        //notification.defaults|=Notification.DEFAULT_SOUND;

        // 其实通知也能支持震动的
        // 需要加入震动权限
        //<uses-permission android:name="android.permission.VIBRATE"/>
        // 如何修改 Notification 的震动，以重复的方式 1 秒震动、 1 秒停止，共 5 秒。
        //long[] vibrate=new long[]{1000, 1000, 1000, 1000, 1000};
        //notification.vibrate=vibrate;

        // 手机闪光 (Notification.FLAG_SHOW_LIGHTS)
        // Notification 也包含属性来设置手机 LED 的颜色和闪烁频率
        // ledARGB 属性用于设置 LED 的颜色，而 ledOffMS 和 ledOnMS 属性用来设置 LED
        // 闪烁的频率和样式。
        // 你可以设置 ledOnMS 属性为 1 ， ledOffMS 属性为 0 来让 LED 始终亮着；
        // 或者将两者设置为 0 来将 LED 关闭。
        // 一旦你设置了 LED 的设定，你也必须为 Notification 的 flags 属性添加
        // FLAG_SHOW_LIGHTS 标志位。
        // 接下来的代码片段显示了如何将点亮红色的 LED ：

        //简单的直接设置某一个颜色
        //notification.ledARGB=Color.RED;
        //复杂的设置自己的颜色--前提是手机支持，不然不起作用
        //notification.ledARGB=0xaabbccdd;
        //notification.ledOffMS=0;
        //notification.ledOnMS=1;
        notification.flags|=Notification.FLAG_SHOW_LIGHTS;

        // 向NotificationManager注册一个notification，并用NOTIFICATION_ID作为管理的唯一标示
        mNM.notify(R.string.local_service_started, notification);
    }

    private final BroadcastReceiver mServiceReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();

            if (action.equalsIgnoreCase(SendTaskService.ACTION_NEW_TASK)) {
                addTaskToQueue(null, 0);
            } else if (action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                /*if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                    WeiboLog.i(TAG, "netWork has lost");
                }

                NetworkInfo tmpInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                WeiboLog.i(TAG, tmpInfo.toString() + " {isConnected = " + tmpInfo.isConnected() + "}");*/

                if (App.hasInternetConnection(App.getAppContext())) {
                    addTaskToQueue(null, 0);
                } else {
                    WeiboLog.d(TAG, "receiv network changed");
                    stopTask();
                }
            } else if (ACTION_STOP_TASK.equals(action)) {
                WeiboLog.d(TAG, "receiv stop action");
                stopTask();
            }
        }
    };
}
