package com.me.microblog.oauth;

import android.content.Context;
import com.me.microblog.util.WeiboLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证的基础类,
 *
 * @author archko
 */
public abstract class BaseOauth2 implements Oauth2 {

    String mAccessToken;
    long mExpireTime;
    OauthBean mOauthBean;

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public String getSecret() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 设置accessToken，需要注意，同时设置过期时间
     *
     * @param token
     */
    @Override
    public void setAccessToken(String token) {
        this.mAccessToken = token;
    }

    @Override
    public void setSecret(String secret) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 获取token的过期时间
     *
     * @return
     */
    @Override
    public long getExpireTime() {
        return mExpireTime;
    }

    public boolean oauth() {
        return false;
    }

    public void setOauthBean() {

    }

    public String getBaseurl() {
        return null;
    }

    public String getCallbackUrl() {
        return null;
    }

    public String getOauthUrl() {
        return null;
    }

    //oauth method
    public String buildOauthTokenUrl(String consumer_key, String callback_url) {
        return null;
    }

    /**
     * 登录
     */
    //public abstract OauthBean login(Object... params);

    /**
     * 获取AccessToken
     *
     * @param client 登录后获取的.
     */
    //abstract OauthBean fetchAccessToken(HttpClient client);
    abstract void saveAccessToken(Context ctx);

    /**
     * 解析url得到参数
     *
     * @param queryString 等解析的url
     * @return
     */
    public Map<String, String> parseAccessToken(String queryString) {
        String str1 = "#";
        int index = queryString.indexOf(str1);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("index:" + index);
        }
        if (index != - 1) {
            String newUrl = queryString.substring(index + 1);
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("parseAccessToken:" + newUrl);
            }

            String[] querys = newUrl.split("&");
            HashMap<String, String> map = new HashMap<String, String>();
            int length = querys.length;
            int idx = 0;
            String key;
            String val;

            while (true) {
                if (idx >= length) {
                    return map;
                }
                String[] str2 = querys[ idx ].split("=");
                key = str2[ 0 ];
                val = str2[ 1 ];
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d("key:" + key + " val:" + val);
                }
                map.put(key, val);
                ++ idx;
            }
        }
        return null;
    }
}
