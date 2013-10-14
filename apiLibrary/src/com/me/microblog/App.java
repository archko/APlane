package com.me.microblog;

import java.io.File;

import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.support.v4.util.LruCache;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;*/

import com.me.microblog.bean.User;
import com.me.microblog.core.*;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.BaseOauth2;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.oauth.SOauth2;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.Constants;
import com.me.microblog.util.RC4;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
public class App extends Application {

    public static final String TAG="App";
    private static App instance;
    private OauthBean mOauthBean;
    /**
     * 当前登录的用户，之前这个没有用到。
     * 它不具备所有的用户属性，提供id，token，
     */
    public static User currentUser;
    public static boolean isLogined=false;
    public DownloadPool mDownloadPool=null;
    public static String mCacheDir; //图片存储上级目录
    public static final int OAUTH1=0;   //新浪oauth1认证
    public static final int OAUTH2=1;   //新浪oauth2认证
    public static final int QOAUTH1=2;  //腾讯oauth1认证
    public static final int QOAUTH2=3;  //腾讯oauth2认证
    public static final int OAUTH_TYPE=OAUTH2;
    public static final String KEY="abcdefgopqrstuvwxyzhijklmn";
    public static String OAUTH_MODE=Constants.SOAUTH_TYPE_WEB;   //默认使用的是客户端认证。
    /**
     * OAuth2的过期时间
     */
    public long oauth2_timestampe=0;
    /**
     * 高级key的OAuth2的过期时间
     */
    public long mAdvancedOauth2Timestampe=0;
    /**
     * 高级key对应的bean，其实没多大用处
     */
    private OauthBean mAdvancedOauthBean;
    /**
     * 大图片缓存
     */
    private LruCache<String, Bitmap> mLargeLruCache;
    private int pageCount=Constants.WEIBO_COUNT;

    /**
     * 获取api实例，需要在ui线程外的线程中执行，因为登录过程较慢。
     *
     * @param ctx
     * @return
     */
    /*public static BaseApi getMicroBlog(Context ctx) throws WeiboException {
        if (weiboApi==null) {
            if (OAUTH_TYPE==OAUTH2) {
                if (OAUTH_MODE.equalsIgnoreCase(Constants.SOAUTH_TYPE_WEB)) {
                    WeiboLog.e(TAG, "认证方式为web，程序出错。");
                    return null;
                }
                return getOauth2WeiboApi(ctx);
            } else if (OAUTH_TYPE==OAUTH1) {
                return getOauth1WeiboApi(ctx);
            } else if (OAUTH_TYPE==QOAUTH2) {
                return getQOauth2WeiboApi(ctx);
            }
        }

        //TODO 这里需要重新考虑，如果直接把它设为空，会引发一些问题。
        if (System.currentTimeMillis()>oauth2_timestampe&&oauth2_timestampe!=0&&OAUTH_TYPE==OAUTH2) {
            WeiboLog.i("token过期了,现在需要重新获取."+oauth2_timestampe);
            //Toast.makeText(App.getAppContext(), "token过期了,需要重新获取.请稍候！", Toast.LENGTH_SHORT).show();
            weiboApi=null;
            throw new WeiboException("token过期了,需要重新获取.请稍候！", WeiboException.EX_CODE_TOKEN_EXPIRE);
        }

        return weiboApi;
    }

    @Deprecated
    static BaseApi getOauth1WeiboApi(Context ctx) {
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ctx);
        String token=preferences.getString(Constants.PREF_TOKEN, "");
        String secret=preferences.getString(Constants.PREF_SECRET, "");

        if (!TextUtils.isEmpty(token)&&!TextUtils.isEmpty(secret)) {
            *//*App.weiboApi=new WeiboApi();
            ((WeiboApi) App.weiboApi).twitter.setConsumer(token, secret);*//*
        }

        return App.weiboApi;
    }*/

    /**
     * 设置api实例，在程序运行过程中，需要一直存在的，需要注意的是expireTime是认证后的相对过期时间，
     * time是绝对过期时间，这里统一取绝对时间点，如果手机的时间不正确，只有再认证了。
     *
     * @param bean
     */
    /*public static void setWeiboApi(OauthBean bean) {
        if (OAUTH_TYPE==OAUTH2) {
            WeiboLog.d(TAG, "设置api数据："+bean);
            weiboApi=new SWeiboApi2(bean.accessToken);
            mOauthBean=bean;
            oauth2_timestampe=bean.time;
            isLogined=true;
            if (null!=mAdvancedOauthBean) {
                ((SWeiboApi2) weiboApi).setDAccessToken(mAdvancedOauthBean.accessToken);
            }
        }
    }*/

    /**
     * 设置高级api实例，在程序运行过程中，需要一直存在的，需要注意的是expireTime是认证后的相对过期时间，
     * time是绝对过期时间，这里统一取绝对时间点，如果手机的时间不正确，只有再认证了。
     *
     * @param bean
     */
    /*public static void setAdvancedWeiboApi(OauthBean bean) {
        WeiboLog.d(TAG, "设置高级api数据："+bean);
        mAdvancedOauthBean=bean;
        mAdvancedOauth2Timestampe=bean.time;
        if (null!=weiboApi) {
            ((SWeiboApi2) weiboApi).setDAccessToken(bean.accessToken);
        }
    }*/

    /**
     * 初始化api，通常只会调用一次，如果程序重启，则有可能多次。所以这里是查询默认的帐户。如果没有，表示当前没有默认的帐户。
     * 只是查询，并把相应的api放入sp中，设置运行时的调用api的accessToken.
     *
     * @param ctx
     * @return
     */
    /*public static BaseApi initWeiboApi(Context ctx) {
        if (null!=weiboApi) {
            WeiboLog.d(TAG, "已经存在了api，不需要查询。");
            return weiboApi;
        }

        OauthBean bean=SqliteWrapper.queryAccount(ctx, TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, -1);
        if (null!=bean) {
            SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString(Constants.PREF_ACCESS_TOKEN, bean.accessToken);
            editor.putLong(Constants.PREF_CURRENT_USER_ID, Long.valueOf(bean.openId));
            editor.commit();

            setWeiboApi(bean);
        }
        return weiboApi;
    }*/

    /**
     * 只是查询，查询高级的key，并把相应的api放入sp中，设置运行时的调用api的accessToken.
     *
     * @param ctx
     * @return
     */
    /*public static BaseApi getAdvancedWeiboApi(Context ctx) {
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ctx);
        long userId=preferences.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        OauthBean bean=SqliteWrapper.queryAccount(ctx, TwitterTable.AUTbl.WEIBO_SINA_DESK, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT, userId);
        WeiboLog.d(TAG, "getAdvancedWeiboApi:"+bean);
        if (null!=bean) {
            setAdvancedWeiboApi(bean);
        }
        return weiboApi;
    }*/

    /**
     * 将整型数值转为ip地址
     *
     * @param ctx
     * @return
     */
    static String intToIp(Context ctx) {
        WifiManager wifiManager=(WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo=wifiManager.getConnectionInfo();
        int i=wifiInfo.getIpAddress();

        return (i&0xFF)+"."+
            ((i>>8)&0xFF)+"."+
            ((i>>16)&0xFF)+"."+
            (i>>24&0xFF);
    }

    public void logout() {
        mOauthBean=null;
        oauth2_timestampe=0;
        mAdvancedOauth2Timestampe=0;
        mAdvancedOauthBean=null;
        isLogined=false;
        currentUser=null;
        cleanupImages();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance=this;

        initDownloadPool();

        initCacheDir();

        /*if (Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            WeiboCrashHandler.getInstance().init(this);
        }*/

        //initLargeCache();

        initOauth2(false);

        /*mBMapMan=new BMapManager(this);
        mBMapMan.init(mStrKey, new MyGeneralListener());
        mBMapMan.getLocationManager().setNotifyInternal(140, 5);*/
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor=pref.edit();
        editor.remove(Constants.PREF_SERVICE_STATUS);
        editor.remove(Constants.PREF_SERVICE_COMMENT);
        editor.remove(Constants.PREF_SERVICE_FOLLOWER);
        editor.remove(Constants.PREF_SERVICE_AT);
        editor.remove(Constants.PREF_SERVICE_AT_COMMENT);
        editor.remove(Constants.PREF_SERVICE_DM);
        editor.commit();

        loadAccount(pref);
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
        return mOauthBean;
    }

    /**
     * 设置认证bean
     *
     * @param oauthBean
     */
    public void setOauthBean(OauthBean oauthBean) {
        mOauthBean=oauthBean;
        oauth2_timestampe=mOauthBean.expireTime;
    }

    /**
     * 获取高级权限认证的bean
     *
     * @return
     */
    public OauthBean getDOauthBean() {
        return mAdvancedOauthBean;
    }

    /**
     * 设置高级权限认证bean
     *
     * @param oauthBean
     */
    public void setDOauthBean(OauthBean oauthBean) {
        mAdvancedOauthBean=oauthBean;
        mAdvancedOauth2Timestampe=mAdvancedOauthBean.expireTime;
    }

    /**
     * 初始化认证
     *
     * @param force 是否强制初始化，如果是在登录页面选择的，就需要强制初始化一次
     */
    public void initOauth2(boolean force) {
        if (mOauthBean!=null) {
            WeiboLog.i(TAG, "initOauth2已经初始化过了！");
            return;
        }

        OauthBean bean=SqliteWrapper.queryAccount(this, TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, -1);
        WeiboLog.d(TAG, "initOauth2:"+bean);
        if (null!=bean) {
            SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString(Constants.PREF_ACCESS_TOKEN, bean.accessToken);
            editor.putLong(Constants.PREF_CURRENT_USER_ID, Long.valueOf(bean.openId));
            editor.commit();

            setOauthBean(bean);

            initDOauth2(bean.openId);
        } else {
            //TODO 查询为空，有可能是没有帐户，有可能是默认的帐户注销了!当前不作处理，默认帐户如果注销，需要修改其它帐户为默认的帐户
            WeiboLog.d("查询为空，有可能是没有帐户，有可能是默认的帐户注销了!");
        }
    }

    public void initDOauth2(String userId) {
        OauthBean bean=SqliteWrapper.queryAccount(this, TwitterTable.AUTbl.WEIBO_SINA_DESK, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT, Long.valueOf(userId));
        WeiboLog.d(TAG, "initDOauth2:"+bean);
        if (null!=bean) {
            setDOauthBean(bean);
        } else {
            WeiboLog.d("查询为空，有可能是没有高级帐户!");
        }
    }

    private void initLargeCache() {
        mLargeLruCache=new LruCache<String, Bitmap>(3);
    }

    public LruCache<String, Bitmap> getLargeLruCache() {
        if (null==mLargeLruCache) {
            initLargeCache();
        }
        return mLargeLruCache;
    }

    public void clearLargeLruCache() {
        if (null!=mLargeLruCache) {
            mLargeLruCache.evictAll();
            mLargeLruCache=null;
        }
    }

    private void initCacheDir() {
        mCacheDir=Constants.CACHE_DIR;
        File file=new File(mCacheDir+Constants.ICON_DIR);
        if (!file.exists()) {
            file.mkdirs();
            WeiboLog.i(TAG, "创建头像存储目录."+file.getAbsolutePath());
        }

        file=new File(mCacheDir+Constants.PICTURE_DIR);
        if (!file.exists()) {
            file.mkdirs();
            WeiboLog.i(TAG, "创建图片存储目录."+file.getAbsolutePath());
        }

        file=new File(mCacheDir+Constants.GIF);
        if (!file.exists()) {
            file.mkdirs();
            WeiboLog.i(TAG, "创建gif图片存储目录."+file.getAbsolutePath());
        }
    }

    public String getNetworkType() {
        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo=connectivityManager.getActiveNetworkInfo();
        //NetworkInfo mobNetInfo = connectivityManager
        //		.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (activeNetInfo!=null) {
            return activeNetInfo.getExtraInfo(); // 接入点名称: 此名称可被用户任意更改 如: cmwap, cmnet,
            // internet ...
        } else {
            return null;
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
            ConnectivityManager connectivity=(ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity==null) {
                return false;
            } else {
                NetworkInfo[] info=connectivity.getAllNetworkInfo();
                if (info!=null) {
                    for (int i=0; i<info.length; i++) {
                        if (info[i].getState()==NetworkInfo.State.CONNECTED) {
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
        ConnectivityManager connectivity=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity==null) {
            return false;
        } else {
            NetworkInfo[] info=connectivity.getAllNetworkInfo();
            if (info!=null) {
                for (int i=0; i<info.length; i++) {
                    if (info[i].getState()==NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void loadAccount(SharedPreferences pref) {
        int weiboCount=pref.getInt(Constants.PREF_WEIBO_COUNT, Constants.WEIBO_COUNT);
        setPageCount(weiboCount);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount=pageCount;
    }

    @Override
    public void onTerminate() {
        //FIXME: 根据android文档，onTerminate不会在真实机器上被执行到
        //因此这些清理动作需要再找合适的地方放置，以确保执行。
        cleanupImages();
        isLogined=false;
        currentUser=null;
        //Toast.makeText(this, "exit app", Toast.LENGTH_LONG);
        WeiboLog.i(TAG, "onTerminate");
        /*if (mBMapMan!=null) {
            mBMapMan.destroy();
            mBMapMan=null;
        }*/

        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WeiboLog.i(TAG, "onLowMemory");
        cleanupImages();
    }

    private void cleanupImages() {
        if (null!=mLargeLruCache) {
            mLargeLruCache.evictAll();
        }
    }

    private void initDownloadPool() {
        if (this.mDownloadPool!=null) {
            return;
        }

        WeiboLog.d(TAG, "initDownloadPool.");
        DownloadPool downloadPool=new DownloadPool(this);
        this.mDownloadPool=downloadPool;
        this.mDownloadPool.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        this.mDownloadPool.setName("DownloadPool");
        this.mDownloadPool.start();
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

    //----------------------------
    /*public static final String mStrKey="59CD6317C9FB8E36814FEC225ACB38B1C51A8A5D";
    boolean m_bKeyRight=true;    // 授权Key正确，验证通过
    //百度MapAPI的管理类
    public BMapManager mBMapMan=null;

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetNetworkState(int iError) {
            WeiboLog.d(TAG, "onGetNetworkState error is "+iError);
        }

        @Override
        public void onGetPermissionState(int iError) {
            WeiboLog.d(TAG, "onGetPermissionState error is "+iError);
            if (iError==MKEvent.ERROR_PERMISSION_DENIED) {
                // 授权Key错误：
                App.instance.m_bKeyRight=false;
            }
        }
    }*/
    /**
     * 经纬度,供附近的Fragment使用的,
     */
    public double longitude=0.0;
    public double latitude=0.0;
    public int range=10000;
    /**
     * 定位的时间.如果地图定位没有自动更新,就需要手动更新.
     */
    public long mLocationTimestamp=0;
}
