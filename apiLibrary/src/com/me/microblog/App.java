package com.me.microblog;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.me.microblog.bean.AKLocation;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.io.File;
/*import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;*/

/**
 * @author archko
 */
public class App extends Application {

    public static final String TAG = "App";
    private static App instance;
    private OauthBean mOauthBean;
    public static boolean isLogined = false;
    public DownloadPool mDownloadPool = null;
    public static String mCacheDir; //图片存储上级目录
    public static final String KEY = "abcdefgopqrstuvwxyzhijklmn";
    //public static String OAUTH_MODE=Constants.SOAUTH_TYPE_WEB;   //默认使用的是客户端认证。
    /**
     * OAuth2的过期时间,使用OauthBean中的值
     */
    //public long oauth2_timestampe=0;
    private int pageCount = Constants.WEIBO_COUNT;

    public void logout() {
        mOauthBean = null;
        //oauth2_timestampe=0;
        isLogined = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int threadCount = pref.getInt(Constants.PREF_THREAD_COUNT, Constants.THREAD_COUNT);

        //initDownloadPool(threadCount);

        initImageLoader(this);

        initCacheDir();

        initOauth2(false);

        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Constants.PREF_SERVICE_STATUS);
        editor.remove(Constants.PREF_SERVICE_COMMENT);
        editor.remove(Constants.PREF_SERVICE_FOLLOWER);
        editor.remove(Constants.PREF_SERVICE_AT);
        editor.remove(Constants.PREF_SERVICE_AT_COMMENT);
        editor.remove(Constants.PREF_SERVICE_DM);
        editor.apply();

        loadAccount(pref);

        if (WeiboLog.isDEBUG()) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
        }
    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext() {
        return instance;
    }

    /**
     * 获取认证的bean
     *
     * @return
     */
    public OauthBean getOauthBean() {
        if (null == mOauthBean) {
            mOauthBean = new OauthBean();
        }
        return mOauthBean;
    }

    /**
     * 设置认证bean
     *
     * @param oauthBean
     */
    public void setOauthBean(OauthBean oauthBean) {
        mOauthBean = oauthBean;
        //oauth2_timestampe=mOauthBean.expireTime;
    }

    /**
     * 初始化认证,修改后需要检查accessToken的值
     *
     * @param force 是否强制初始化，如果是在登录页面选择的，就需要强制初始化一次
     */
    public void initOauth2(boolean force) {
        if (mOauthBean != null && ! TextUtils.isEmpty(mOauthBean.accessToken)) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.i(TAG, "initOauth2已经初始化过了！" + mOauthBean);
            }
            return;
        }

        OauthBean bean = SqliteWrapper.queryAccount(this, TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, - 1);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "initOauth2:" + bean);
        }
        if (null != bean) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_ACCESS_TOKEN, bean.accessToken);
            editor.putLong(Constants.PREF_CURRENT_USER_ID, Long.valueOf(bean.openId));
            editor.commit();

            setOauthBean(bean);
        } else {
            //TODO 查询为空，有可能是没有帐户，有可能是默认的帐户注销了!当前不作处理，默认帐户如果注销，需要修改其它帐户为默认的帐户
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("查询为空，有可能是没有帐户，有可能是默认的帐户注销了!");
            }
        }
    }

    private void initCacheDir() {
        mCacheDir = Constants.CACHE_DIR;
        File file = new File(mCacheDir + Constants.ICON_DIR);
        if (! file.exists()) {
            file.mkdirs();
            if (WeiboLog.isDEBUG()) {
                WeiboLog.i(TAG, "创建头像存储目录." + file.getAbsolutePath());
            }
        }

        file = new File(mCacheDir + Constants.PICTURE_DIR);
        if (! file.exists()) {
            file.mkdirs();
            if (WeiboLog.isDEBUG()) {
                WeiboLog.i(TAG, "创建图片存储目录." + file.getAbsolutePath());
            }
        }

        file = new File(mCacheDir + Constants.GIF);
        if (! file.exists()) {
            file.mkdirs();
            if (WeiboLog.isDEBUG()) {
                WeiboLog.i(TAG, "创建gif图片存储目录." + file.getAbsolutePath());
            }
        }
    }

    /**
     * 判断网络是否连接
     *
     * @param activity
     * @return
     */
    public static boolean hasInternetConnection(Activity activity) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[ i ].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[ i ].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void loadAccount(SharedPreferences pref) {
        int weiboCount = pref.getInt(Constants.PREF_WEIBO_COUNT, Constants.WEIBO_COUNT);
        setPageCount(weiboCount);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public void onTerminate() {
        isLogined = false;
        mOauthBean = null;
        WeiboLog.i(TAG, "onTerminate");

        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WeiboLog.i(TAG, "onLowMemory");
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        /*ImageLoaderConfiguration config=new ImageLoaderConfiguration.Builder(context)
            .threadPriority(Thread.NORM_PRIORITY-2)
            .denyCacheImageMultipleSizesInMemory()
            .discCacheFileNameGenerator(new Md5FileNameGenerator())
            .tasksProcessingOrder(QueueProcessingType.LIFO)
            .writeDebugLogs() // Remove for release app
            .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);*/
    }

    public static boolean showMemory() {

        /*Method _readProclines=null;
        try {
            Class procClass;
            procClass=Class.forName("android.os.Process");
            Class parameterTypes[]=new Class[]{String.class, String[].class, long[].class};
            _readProclines=procClass.getMethod("readProcLines", parameterTypes);
            Object arglist[]=new Object[3];
            final String[] mMemInfoFields=new String[]{"MemTotal:",
                "MemFree:", "Buffers:", "Cached:"};
            long[] mMemInfoSizes=new long[mMemInfoFields.length];
            mMemInfoSizes[0]=30;
            mMemInfoSizes[1]=-30;
            arglist[0]=new String("/proc/meminfo");
            arglist[1]=mMemInfoFields;
            arglist[2]=mMemInfoSizes;
            if (_readProclines!=null) {
                _readProclines.invoke(null, arglist);
                for (int i=0; i<mMemInfoSizes.length; i++) {
                    WeiboLog.d(TAG, mMemInfoFields[i]+" : "+mMemInfoSizes[i]/1024);
                }
            }

            if (mMemInfoSizes[0]>=512*1000) {
                //WeiboLog.d(TAG, "内存大于512m");
                return false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }*/

        return false;
    }

    private AKLocation mLocation;

    public AKLocation getLocation() {
        return mLocation;
    }

    public AKLocation getLocation(long time) {
        if (null != mLocation) {
            if (System.currentTimeMillis() - mLocation.mLocationTimestamp < time) {
                return mLocation;
            }
        }
        return null;
    }

    public void setLocation(AKLocation mAkLocation) {
        this.mLocation = mAkLocation;
    }
}
