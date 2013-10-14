package com.me.microblog.oauth;

import android.os.Message;

/**
 * User: archko Date: 12-9-4 Time: 上午10:00
 */
public interface OauthCallback {
    void postOauthSuc(Object[] params);

    //void oauthResult(final Message msg);

    void postOauthFailed(int oauthCode);
}
