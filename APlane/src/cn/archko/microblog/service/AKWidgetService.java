package cn.archko.microblog.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import cn.archko.microblog.appwidgets.AppWidgetLarge;
import com.me.microblog.App;
import com.me.microblog.bean.Status;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @description:
 * @author: archko 13-2-22 :下午5:59
 */
public class AKWidgetService extends Service {

    public static final String TAG = "AKWidgetService";

    /**
     * Called to indicate a general service commmand. Used in
     * {@link MediaButtonIntentReceiver}
     */
    public static final String SERVICECMD = "com.me.archko.servicecommand";

    /**
     * Called to go toggle between pausing and playing the music
     */
    public static final String TOGGLEPAUSE_ACTION = "com.me.archko.togglepause";

    /**
     * Called to go to the previous track
     */
    public static final String PREVIOUS_ACTION = "com.me.archko.previous";

    /**
     * Called to go to the next track
     */
    public static final String NEXT_ACTION = "com.me.archko.next";

    public static final String CMDNAME = "command";
    private MyBinder myBinder = new MyBinder();
    SharedPreferences mPrefs;
    ArrayList<Status> mDataList;
    private int mCurrPos = 0;

    /**
     * 4x2 widget
     */
    private AppWidgetLarge mAppWidgetLarge = AppWidgetLarge.getInstance();

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

        AKWidgetService getService() {
            return AKWidgetService.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        WeiboLog.d(TAG, "onCreate");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);
        initData(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the mount listener
        unregisterReceiver(mIntentReceiver);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            initData(false);
            final String action = intent.getAction();
            final String command = intent.getStringExtra("command");
            WeiboLog.d(TAG, "action:" + action + " command:" + command);
            if (NEXT_ACTION.equals(action)) {
                mCurrPos++;
                final int[] large = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLarge.performUpdate(AKWidgetService.this, large);
            } else if (PREVIOUS_ACTION.equals(action)) {
                mCurrPos--;
                final int[] large = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLarge.performUpdate(AKWidgetService.this, large);
            } else if (TOGGLEPAUSE_ACTION.equals(action)) {
            } else if (AppWidgetLarge.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] large = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLarge.performUpdate(AKWidgetService.this, large);
            } else if (Constants.SERVICE_NOTIFY_UNREAD.equals(action)) {
                //receiveUnread((Unread) intent.getSerializableExtra("unread"));
            }
        }
    };

    @Override
    public void onStart(Intent intent, int startId) {
        WeiboLog.d(TAG, "onStart:" + " startId:" + startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WeiboLog.d(TAG, "onStartCommand,flags:" + flags + " startId:" + startId);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        mAppWidgetLarge.performUpdate(this, null);
        return START_STICKY;
    }

    void initData(boolean force) {
        if (null == mDataList || force) {
            ContentResolver resolver = App.getAppContext().getContentResolver();
            long aUserId = PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).
                getLong(Constants.PREF_CURRENT_USER_ID, - 1);
            mDataList = SqliteWrapper.queryStatuses(resolver, aUserId);
        }
    }

    public Status getStatus() {
        if (null != mDataList && mDataList.size() > 0) {
            if (mCurrPos < 0) {
                mCurrPos = mDataList.size() - 1;
            }

            if (mCurrPos >= mDataList.size()) {
                mCurrPos = 0;
            }
            return mDataList.get(mCurrPos);
        }

        return null;
    }
}
