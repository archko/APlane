package com.me.microblog.oauth;

/**
 * @author archko
 */
public interface Oauth2 extends Oauth1 {

    /**
     * get accesstoken by webview
     */
    public static final int OAUTH_TYPE_WEB=0;
    /**
     * fetch accesstoken by pass
     */
    public static final int OAUTH_TYPE_PWD=1;

    public long getExpireTime();
}
