package com.me.microblog.oauth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import com.me.microblog.util.WeiboLog;

/**
 * User: archko Date: 12-8-31 Time: 上午7:53
 */
public class OauthWebView extends WebView {

    public static final String TAG = "OauthWebView";
    Handler mHandler;
    /**
     * 是否认证过了，成功时为true,失败为false，有可能是系统结束的。可惜这里永远不会执行。
     */
    public boolean isOauthed = false;

    public OauthWebView(Context context, Handler handler) {
        super(context);
        mHandler = handler;
    }

    @Override
    public void destroy() {
        WeiboLog.d(TAG, "destroy.");
        if (! isOauthed && null != mHandler) {
            Message message = Message.obtain();
            message.what = 0;
            message.obj = null;
            mHandler.sendMessage(message);
        }
        super.destroy();
    }
}
