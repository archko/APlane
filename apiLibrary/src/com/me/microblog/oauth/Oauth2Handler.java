package com.me.microblog.oauth;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.RC4;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

/**
 * 认证处理类
 * User: archko Date: 12-9-4 Time: 上午9:57
 */
public class Oauth2Handler {

    Context mContext;
    OauthCallback mOauthCallback;

    public Oauth2Handler(Context ctx, OauthCallback callback) {
        mContext=ctx;
        mOauthCallback=callback;
    }

    /**
     * 这个需要在ui线程中调用，否则无法创建Handler
     * TODO以后需要修改，这里不一定要ui线程的操作，也需要另一个接口来监听
     */
    Handler mHandler=new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (mContext instanceof Activity) {
                try {
                    Activity activity=(Activity) mContext;
                    if (activity.isFinishing()) {
                        WeiboLog.d("isFinishing()");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            super.handleMessage(msg);
            int what=msg.what;
            if (what==0) {
                oauthResult(msg);
            }
        }
    };

    /**
     * 实际的认证方法
     *
     * @param params 认证成功后的调用方法参数
     */
    public void oauth2(Object[] params) {
        String password=null;
        String username=null;

        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        long userId=preferences.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        OauthBean bean=SqliteWrapper.queryAccount(mContext, TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, userId);
        WeiboLog.d("oauth2:"+bean);

        if (null!=bean&&!TextUtils.isEmpty(bean.name)) {//if (!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)) {
            username=RC4.RunRC4(bean.name, App.KEY);
            password=RC4.RunRC4(bean.pass, App.KEY);
            if (!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)) {
                SOauth2 ouath2=new SOauth2();
                ouath2.oauthByWebView(new Object[]{username, password, mContext, mHandler, params});
            } else {
                int res=SqliteWrapper.deleteAccount(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, userId);
                WeiboLog.d("res:"+res);
                Message message=new Message();
                message.arg2=Constants.USER_PASS_IS_NULL;
                oauthResult(message);
            }
        }
    }

    /**
     * 认证结果回调
     *
     * @param oauthBean
     */
    public void oauthResult(final Message msg) {
        WeiboLog.d("bean:"+msg);
        Object[] objects=(Object[]) msg.obj;
        if (null==objects||objects[0]==null) {
            Toast.makeText(App.getAppContext(), App.getAppContext().getString(R.string.login2_run_error), Toast.LENGTH_LONG).show();
            WeiboLog.e("运行中认证失败。");
            oauthFailed(msg.arg2);
            return;
        }
        try {
            OauthBean oauthBean=(OauthBean) objects[0];
            Object[] params=(Object[]) objects[1];
            if (oauthBean!=null) {
                WeiboLog.d("认证成功。"+oauthBean);
                SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(Constants.PREF_SOAUTH_TYPE, String.valueOf(Oauth2.OAUTH_TYPE_WEB));
                editor.commit();

                App.isLogined=true;
                postLogin(new Object[]{"", oauthBean, ""}, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 认证失败后的操作，如果是列表，默认是刷新
     */
    public void oauthFailed(int oauthCode) {
        if (null!=mOauthCallback) {
            mOauthCallback.postOauthFailed(oauthCode);
        }
    }

    public void postLogin(Object[] resultObj, Object[] params) {
        OauthBean bean=((OauthBean) resultObj[1]);

        saveWeiboApi(resultObj, mContext, true);
        WeiboLog.d("bean+"+bean);
        //postOauth(params);
        if (null!=mOauthCallback) {
            mOauthCallback.postOauthSuc(params);
        }
    }

    /**
     * 高级认证
     *
     * @param params
     */
    @Deprecated
    public OauthBean advancedOauth2() {
        String password=null;
        String username=null;

        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        long userId=preferences.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        OauthBean bean=SqliteWrapper.queryAccount(mContext, TwitterTable.AUTbl.WEIBO_SINA_DESK, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT, userId);
        if (null==bean) {
            bean=SqliteWrapper.queryAccount(mContext, TwitterTable.AUTbl.WEIBO_SINA, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT, userId);
        }
        WeiboLog.d("advancedOauth2:"+bean);

        if (null!=bean&&!TextUtils.isEmpty(bean.name)) {
            username=RC4.RunRC4(bean.name, App.KEY);
            password=RC4.RunRC4(bean.pass, App.KEY);
            SOauth2 ouath2=new SOauth2();
            bean=ouath2.fetchAccessTokenByPass(username, password, SOauth2.DESKTOP_KEY, SOauth2.DESKTOP_SECRET);

            WeiboLog.i("运行时的高级认证！"+bean);
            if (null!=bean) {
                Object[] objects=new Object[]{bean, username, password};
                saveAdvancedWeiboApi(objects, mContext);
            }

            return bean;
        }

        return null;
    }

    //--------------------- oauth操作 ---------------------

    /**
     * 保存认证后的值。这是在oauth2handler用的
     *
     * @param resultObj 这是包含OauthBean的对象，第一个元素是用户名，第三个是密码，第2个才是OauthBean
     * @param ctx
     * @param isDefault 是否是默认的帐户，对于登录来说，需要将它修改为默认的帐户，也有情况不是的，比如就是添加一个新的用户，且认证，
     */
    public static final boolean saveWeiboApi(Object[] resultObj, Context ctx, boolean isDefault) {
        OauthBean bean=((OauthBean) resultObj[1]);
        if (null==bean) {
            Toast.makeText(App.getAppContext(), R.string.login_error, Toast.LENGTH_LONG).show();
            return false;
        }
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor=preferences.edit();
        try {
            editor.putLong(Constants.PREF_CURRENT_USER_ID, Long.valueOf(bean.openId));
            editor.putString(Constants.PREF_ACCESS_TOKEN, bean.accessToken);
            editor.commit();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        bean.time=bean.expireTime*1000+System.currentTimeMillis()-100l;
        bean.expireTime=bean.time;
        App app=(App) App.getAppContext();
        app.setOauthBean(bean);
        WeiboLog.d("new bean+"+bean);

        Cursor cursor=null;
        try {
            ContentResolver resolver=ctx.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null, TwitterTable.AUTbl.ACCOUNT_TYPE+
                "='"+TwitterTable.AUTbl.WEIBO_SINA+"' and "+TwitterTable.AUTbl.ACCOUNT_USERID+"='"+bean.openId+"'", null, null);

            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                Uri uri=Uri.withAppendedPath(TwitterTable.AUTbl.CONTENT_URI,
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID))));
                ContentValues cv=new ContentValues();
                cv.put(TwitterTable.AUTbl.ACCOUNT_TOKEN, bean.accessToken);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TIME, bean.time);
                if (isDefault) {
                    cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT);
                }
                int res=resolver.update(uri, cv, null, null);
                WeiboLog.d("更新token:"+res);
                return true;
            } else {
                String username=(String) resultObj[0];
                String passwd=(String) resultObj[2];

                ContentValues cv=new ContentValues();
                if (TextUtils.isEmpty(username)||TextUtils.isEmpty(passwd)) {
                    WeiboLog.e("数据库中没有用户名与密码。认证成功但需要手动处理。");
                    Toast.makeText(App.getAppContext(), R.string.oauth_suc_user_pass_is_null, Toast.LENGTH_LONG).show();
                    //return false;
                } else {
                    cv.put(TwitterTable.AUTbl.ACCOUNT_NAME, RC4.RunRC4(username, App.KEY));
                    cv.put(TwitterTable.AUTbl.ACCOUNT_PASS, RC4.RunRC4(passwd, App.KEY));
                }
                cv.put(TwitterTable.AUTbl.ACCOUNT_TOKEN, bean.accessToken);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TIME, bean.time);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TYPE, TwitterTable.AUTbl.WEIBO_SINA);
                cv.put(TwitterTable.AUTbl.ACCOUNT_USERID, bean.openId);
                if (isDefault) {
                    cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT);
                } else {
                    cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT);
                }
                Uri uri=resolver.insert(TwitterTable.AUTbl.CONTENT_URI, cv);
                WeiboLog.d("插入新的token:"+uri);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * 保存高级认证后的值,仅用于高级key,这些key具有password认证类型的.而不是webview.
     *
     * @param resultObj 这是包含OauthBean的对象，第一个元素是用户名，第三个是密码，第2个才是OauthBean
     * @param ctx
     * @param isDefault 是否是默认的帐户，对于登录来说，需要将它修改为默认的帐户，也有情况不是的，比如就是添加一个新的用户，且认证，
     */
    public static final boolean saveAdvancedWeiboApi(Object[] resultObj, Context ctx) {
        OauthBean bean=((OauthBean) resultObj[0]);

        bean.time=bean.expireTime*1000+System.currentTimeMillis()-100l;
        bean.expireTime=bean.time;
        App app=(App) App.getAppContext();
        WeiboLog.d("bean+"+bean);

        Cursor cursor=null;
        try {
            ContentResolver resolver=ctx.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null, TwitterTable.AUTbl.ACCOUNT_TYPE+
                "='"+TwitterTable.AUTbl.WEIBO_SINA_DESK+"' and "+TwitterTable.AUTbl.ACCOUNT_USERID+"='"+bean.openId+"'", null, null);

            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                Uri uri=Uri.withAppendedPath(TwitterTable.AUTbl.CONTENT_URI,
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID))));
                ContentValues cv=new ContentValues();
                cv.put(TwitterTable.AUTbl.ACCOUNT_TOKEN, bean.accessToken);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TIME, bean.time);

                int res=resolver.update(uri, cv, null, null);
                WeiboLog.d("更新高级token:"+res);
                return true;
            } else {
                String username=(String) resultObj[1];
                String passwd=(String) resultObj[2];
                if (TextUtils.isEmpty(username)||TextUtils.isEmpty(passwd)) {
                    WeiboLog.e("数据库中没有token,没有用户名与密码。认证成功但无效。");
                    //Toast.makeText(ctx, R.string.login2_suc_error, Toast.LENGTH_LONG).show();
                    return false;
                }

                ContentValues cv=new ContentValues();
                cv.put(TwitterTable.AUTbl.ACCOUNT_NAME, RC4.RunRC4(username, App.KEY));
                cv.put(TwitterTable.AUTbl.ACCOUNT_PASS, RC4.RunRC4(passwd, App.KEY));
                cv.put(TwitterTable.AUTbl.ACCOUNT_TOKEN, bean.accessToken);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TIME, bean.time);
                cv.put(TwitterTable.AUTbl.ACCOUNT_TYPE, TwitterTable.AUTbl.WEIBO_SINA_DESK);
                cv.put(TwitterTable.AUTbl.ACCOUNT_USERID, bean.openId);
                cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT);   //私信不能是默认的帐户

                Uri uri=resolver.insert(TwitterTable.AUTbl.CONTENT_URI, cv);
                WeiboLog.d("插入新的高级token:"+uri);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return false;
    }
}
