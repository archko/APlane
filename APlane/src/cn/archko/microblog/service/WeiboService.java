package cn.archko.microblog.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.PrefsActivity;
import com.me.microblog.App;
import com.me.microblog.bean.Unread;
import com.me.microblog.core.sina.SinaUnreadApi;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

import java.lang.ref.WeakReference;

/**
 * 当前服务要做的事就是不断查询是否有新的消息.
 * 添加了认证操作.
 *
 * @author root
 */
public class WeiboService extends Service {

    public static final String TAG = "WeiboService";
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    private MyBinder myBinder = new MyBinder();
    private NotificationManager mNM;
    SharedPreferences settings;
    /**
     * 查询新微博时间
     */
    public static int DELAY_TIME = 10 * 1000 * 60;

    public static final String REFRESH = "cn.archko.microblog.refresh";

    private static final String OAUTH = "cn.archko.microblog.oauth";

    public boolean isOauthing = false;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public IBinder onBind(Intent intent) {
        WeiboLog.d(TAG, "WeiboService.onBind");
        return myBinder;
    }

    //  重新绑定时调用该方法
    @Override
    public void onRebind(Intent intent) {
        WeiboLog.d(TAG, "WeiboService.onRebind");
        super.onRebind(intent);
    }

    //  解除绑定时调用该方法
    @Override
    public boolean onUnbind(Intent intent) {
        WeiboLog.d(TAG, "WeiboService.onUnbind");
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder {

        WeiboService getService() {
            return WeiboService.this;
        }

    }

    private static final class ServiceHandler extends Handler {

        private final WeakReference<WeiboService> mService;

        /**
         * Constructor of <code>ServiceHandler</code>
         *
         * @param service The service to use.
         * @param looper  The thread to run on.
         */
        public ServiceHandler(final WeiboService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<WeiboService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            final WeiboService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                default:
                    service.doTask();
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WeiboLog.d(TAG, "WeiboService.onCreate");
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null && mWakeLock == null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE
                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        }

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        //Display a notification about us starting.We put an icon in the status bar.
        //showNotification(0);
        String chk_new_status_time = settings.getString(PrefsActivity.PREF_CHK_NEW_STATUS_TIME, "1");
        if (chk_new_status_time.equals("0")) {
            WeiboService.DELAY_TIME = 1 * 1000 * 60;
        } else if (chk_new_status_time.equals("1")) {
            WeiboService.DELAY_TIME = 2 * 1000 * 60;
        } else if (chk_new_status_time.equals("2")) {
            WeiboService.DELAY_TIME = 5 * 1000 * 60;
        } else if (chk_new_status_time.equals("3")) {
            WeiboService.DELAY_TIME = 20 * 1000 * 60;
        }

        final HandlerThread thread = new HandlerThread("ServiceHandler",
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        // Initialize the handler
        mServiceHandler = new ServiceHandler(this, thread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WeiboLog.d(TAG, "WeiboService.onDestroy");

        try {
            mNM.cancel(R.string.local_service_started);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mServiceHandler) {
            mServiceHandler.removeCallbacksAndMessages(null);
            mServiceHandler.getLooper().quit();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WeiboLog.d(TAG, "onStartCommand,flags:" + flags + " startId:" + startId);

        /*if (intent!=null) {
            final String action=intent.getAction();
            if (REFRESH.equals(action)) {
                fetchNewStatuses(intent, startId);
            } else if (OAUTH.equals(action)) {
                oauth2(intent);
            }
        }*/
        fetchNewStatuses(intent, startId);

        return START_STICKY;
    }

    /**
     * 处理任务
     */
    private void doTask() {
        if (! App.hasInternetConnection(this)) {
            WeiboLog.d(TAG, "no internet connection.");
            return;
        }

        App app = (App) App.getAppContext();
        if (app.getOauthBean().oauthType == Oauth2.OAUTH_TYPE_WEB &&
            System.currentTimeMillis() >= app.getOauthBean().expireTime && app.getOauthBean().expireTime != 0) {
            WeiboLog.w(TAG, "token expired.");
            return;
        }

        try {
            long currentUserId = settings.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
            WeiboLog.d(TAG, "currentUserId:" + currentUserId);
            if (currentUserId != - 1) {
                SinaUnreadApi unreadApi = new SinaUnreadApi();
                unreadApi.updateToken();
                Unread unread = unreadApi.getUnread(currentUserId);
                WeiboLog.i(TAG, "获取新微博数据为:" + unread);

                boolean shouldUpdate = unread.status != 0 || unread.comments != 0 || unread.followers != 0 ||
                    unread.mention_status != 0 || unread.mention_cmt != 0 || unread.dm != 0;
                if (null != unread && shouldUpdate) {
                    int statusCount = unread.status;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(Constants.PREF_SERVICE_STATUS, statusCount);
                    editor.putInt(Constants.PREF_SERVICE_COMMENT, unread.comments);
                    editor.putInt(Constants.PREF_SERVICE_FOLLOWER, unread.followers);
                    editor.putInt(Constants.PREF_SERVICE_AT, unread.mention_status);
                    editor.putInt(Constants.PREF_SERVICE_AT_COMMENT, unread.mention_cmt);
                    editor.putInt(Constants.PREF_SERVICE_DM, unread.dm);
                    editor.commit();

                    Intent intent = new Intent(Constants.SERVICE_NOTIFY_UNREAD);
                    intent.putExtra("unread", unread);
                    WeiboService.this.sendBroadcast(intent);

                    //showNotification(unread, null);
                } else {
                    WeiboLog.d(TAG, "没有新微博:");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Message msg = mServiceHandler.obtainMessage();
            mServiceHandler.sendMessageDelayed(msg, DELAY_TIME);
        }
    }

    /**
     * 获取新的微博内容，由于高级api没有开放，只能在有新微博时再获取最新的一条微博内容。
     *
     * @param intent
     * @param startId
     */
    private void fetchNewStatuses(Intent intent, int startId) {
        WeiboLog.d(TAG, "fetchNewStatuses.");
        boolean chk_new_status = settings.getBoolean(PrefsActivity.PREF_AUTO_CHK_NEW_STATUS, true);
        if (! chk_new_status) {
            WeiboLog.d(TAG, "no chk_new_status.");
            mServiceHandler.removeCallbacksAndMessages(null);
            return;
        }

        App app = (App) App.getAppContext();
        if (app.getOauthBean().oauthType == Oauth2.OAUTH_TYPE_WEB &&
            System.currentTimeMillis() >= app.getOauthBean().expireTime && app.getOauthBean().expireTime != 0) {
            WeiboLog.e(TAG, "web认证，token过期了.不能启动定时器:" + app.getOauthBean().expireTime);

            return;
        }

        try {
            WeiboLog.d(TAG, "WeiboService.onStartCommand.");
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- 帐户认证操作 ---------------------
    void oauth2(Intent intent) {
        if (isOauthing) {
            WeiboLog.d(TAG, "isOauthing.");
            //return;
        }

        if (intent == null || null == intent.getSerializableExtra("oauth_bean")) {
            WeiboLog.d(TAG, "oauth2,intent =null.");
            return;
        }

        OauthBean oauthBean = (OauthBean) intent.getSerializableExtra("oauth_bean");
        WeiboLog.d(TAG, "添加任务:" + oauthBean);
        if (null != oauthBean) {
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
