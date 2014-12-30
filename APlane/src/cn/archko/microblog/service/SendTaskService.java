package cn.archko.microblog.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import cn.archko.microblog.R;
import com.me.microblog.bean.SendTask;
import com.me.microblog.thread.SendTaskPool;
import com.me.microblog.util.WeiboLog;

/**
 * 队列服务，用于处理队列操作，发新微博，评论，回复，转发等。
 * 需要在解决了Oauth2认证的问题后再启用。
 *
 * @author archko
 */
public class SendTaskService extends Service {

    public static final String TAG = "SendTaskService";
    private MyBinder myBinder = new MyBinder();
    private NotificationManager mNM;
    SharedPreferences settings;

    public SendTaskPool mSendTaskPool = null;
    public static final int TYPE_ORI_TASK = 0;
    public static final int TYPE_RESTART_TASK = 1;

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

    @Override
    public void onCreate() {
        super.onCreate();
        WeiboLog.d(TAG, "onCreate");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        initSendTaskPool();
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

        destroySendTaskPool();
    }

    /**
     * 初始化任务线程
     */
    private void initSendTaskPool() {
        if (this.mSendTaskPool != null) {
            return;
        }

        WeiboLog.d(TAG, "initSendTaskPool.");
        SendTaskPool sendTaskPool = new SendTaskPool(this);
        this.mSendTaskPool = sendTaskPool;
        this.mSendTaskPool.setPriority(Thread.MIN_PRIORITY);
        this.mSendTaskPool.setName("SendTaskPool");
        this.mSendTaskPool.start();
    }

    /**
     * 销毁任务线程
     */
    private void destroySendTaskPool() {
        if (this.mSendTaskPool == null) {
            return;
        }

        WeiboLog.d(TAG, "destroySendTaskPool.");
        mSendTaskPool.setStop(true);
        this.mSendTaskPool = null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        WeiboLog.d(TAG, "onStart:" + " startId:" + startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WeiboLog.d(TAG, "onStartCommand,flags:" + flags + " startId:" + startId);

        addTaskToQueue(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * 添加任务到队列中。
     *
     * @param intent
     */
    void addTaskToQueue(Intent intent) {
        if (null == mSendTaskPool) {
            initSendTaskPool();
        }

        if (intent == null || null == intent.getSerializableExtra("send_task")) {
            return;
        }

        SendTask task = (SendTask) intent.getSerializableExtra("send_task");
        int type = intent.getIntExtra("type", TYPE_ORI_TASK);
        WeiboLog.d(TAG, "添加任务:" + task);
        if (null != task) {
            if (type == TYPE_ORI_TASK) {
                mSendTaskPool.Push(task);
            } else {
                mSendTaskPool.restart(task);
            }
        }
    }

    void doNotify() {
        // 创建一个通知
        Notification notification = new Notification(R.drawable.logo, "", System.currentTimeMillis());
        // 指定这个通知的布局文件
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_remoteview);

        // 设置通知显示的内容
        remoteViews.setTextViewText(R.id.title, "");

        // 将内容指定给通知
        notification.contentView = remoteViews;
        // 指定点击通知后跳到那个Activity
        notification.contentIntent = PendingIntent.getActivity(this, 0, null, PendingIntent.FLAG_UPDATE_CURRENT);

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
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
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
        //notification.flags|=Notification.FLAG_SHOW_LIGHTS;

        // 向NotificationManager注册一个notification，并用NOTIFICATION_ID作为管理的唯一标示
        mNM.notify(R.string.local_service_started, notification);
    }
}
