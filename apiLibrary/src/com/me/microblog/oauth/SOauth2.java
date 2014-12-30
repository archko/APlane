package com.me.microblog.oauth;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.me.microblog.core.BaseApi;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.http.SSLSocketFactoryEx;
import com.me.microblog.util.WeiboLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//import com.me.microblog.util.Base64;

/**
 * @author archko
 */
public class SOauth2 extends BaseOauth2 {

    public static final String CONSUMER_KEY = "1199047716";
    public static final String AUTHENTICATIONURL = "https://api.weibo.com/oauth2/authorize";
    public static final String CALLBACK_URL = "http://archko.oauth.com";
    public static final String DESKTOP_KEY = "140226478";
    public static final String DESKTOP_SECRET = "42fcc96d3e64d9e248649369d61632a6";
    public static final String DESKTOP_CALLBACK = "https://api.weibo.com/oauth2/default.html";

    @Override
    public String getCallbackUrl() {
        return CALLBACK_URL;
    }

    @Override
    public String buildOauthTokenUrl(String consumer_key, String callback_url) {
        String touchParam;

        touchParam = "&display=mobile&state=ABCDEFG";

        return AUTHENTICATIONURL + "?" + "client_id=" + consumer_key
            + "&response_type=token" + touchParam
            + "&redirect_uri=" + callback_url;
    }

    @Deprecated
    public OauthBean login(Object... params) {
        mOauthBean = null;
        mAccessToken = null;
        mExpireTime = 0L;

        String username = (String) params[ 0 ];
        String password = (String) params[ 1 ];
        //WeiboLog.d("name:"+username+" pass:"+password);

        DefaultHttpClient client = (DefaultHttpClient) SSLSocketFactoryEx.getNewHttpClient();
        client.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
        try {
            HttpPost post = new HttpPost("http://login.sina.com.cn/sso/login.php?client=ssologin.js");

            String data = getServerTime();

            String nonce = makeNonce(6);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("entry", "weibo"));
            nvps.add(new BasicNameValuePair("gateway", "1"));
            nvps.add(new BasicNameValuePair("from", ""));
            nvps.add(new BasicNameValuePair("savestate", "7"));
            nvps.add(new BasicNameValuePair("useticket", "1"));
            nvps.add(new BasicNameValuePair("ssosimplelogin", "1"));
            nvps.add(new BasicNameValuePair("su", encodeAccount(username)));
            nvps.add(new BasicNameValuePair("service", "miniblog"));
            nvps.add(new BasicNameValuePair("servertime", data));
            nvps.add(new BasicNameValuePair("nonce", nonce));
            nvps.add(new BasicNameValuePair("pwencode", "wsse"));
            nvps.add(new BasicNameValuePair("sp", new SinaSSOEncoder().encode(password, data, nonce)));

            nvps.add(new BasicNameValuePair(
                "url",
                "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
            nvps.add(new BasicNameValuePair("returntype", "META"));
            nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
            nvps.add(new BasicNameValuePair("vsnval", ""));

            post.setHeader("User-Agent", BaseApi.USERAGENT);
            post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = client.execute(post);
            String entity = EntityUtils.toString(response.getEntity());
            //WeiboLog.d("entity:"+entity);
            String url = entity.substring(entity.indexOf("http://weibo.com/ajaxlogin.php?"), entity.indexOf("code=0") + 6);
            WeiboLog.d("url:" + url);

            // 获取到实际url进行连接  
            HttpGet getMethod = new HttpGet(url);

            response = client.execute(getMethod);
            //entity = EntityUtils.toString(response.getEntity());
            //entity = entity.substring(entity.indexOf("userdomain") + 13, entity.lastIndexOf("\""));
            //WeiboLog.d(entity);

            return fetchAccessToken(client);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    OauthBean fetchAccessToken(HttpClient client) {
        try {
            String urlString = buildOauthTokenUrl(CONSUMER_KEY, CALLBACK_URL);
            //WeiboLog.d("urlString:"+urlString);
            HttpPost post = new HttpPost(urlString);

            DefaultHttpClient defaultHttpClient = (DefaultHttpClient) client;
            defaultHttpClient.setRedirectHandler(new RedirectHandler() {

                @Override
                public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                    WeiboLog.d("isRedirectRequested.context:" + context.toString());
                    return false;
                }

                @Override
                public URI getLocationURI(HttpResponse response, HttpContext context)
                    throws ProtocolException {
                    return null;
                }
            });

            HttpContext context = new BasicHttpContext();
            HttpResponse httpResponse = client.execute(post);
            /*String res = EntityUtils.toString(httpResponse.getEntity());
            WeiboLog.d("rs:" + res);*/

            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                String name = header.getName();
                //WeiboLog.d("header:"+name+" val:"+header.getValue());
                if (name.equals("Location")) {
                    OauthBean oauthBean = null;
                    try {
                        Map<String, String> map = parseAccessToken(header.getValue());
                        oauthBean = new OauthBean();
                        oauthBean.accessToken = map.get("access_token");
                        oauthBean.expireTime = Long.valueOf(map.get("expires_in"));
                        mOauthBean = oauthBean;
                        mAccessToken = oauthBean.accessToken;
                        mExpireTime = oauthBean.expireTime;
                        mOauthBean.openId = map.get("uid");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return oauthBean;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    void saveAccessToken(Context ctx) {
    }

    public WebView oauthByWebView(Object... params) {
        String username = (String) params[ 0 ];
        String password = (String) params[ 1 ];
        Context context = (Context) params[ 2 ];
        Handler handler = (Handler) params[ 3 ];
        Object[] objects = (Object[]) params[ 4 ]; //这个是参数列表
        String key = CONSUMER_KEY;
        String secret = "";
        String url = CALLBACK_URL;
        if (params.length == 8) {
            key = (String) params[ 5 ];
            secret = (String) params[ 6 ];
            url = (String) params[ 7 ];
        }
        oauthFlag = false;
        reload = false;
        return bindViews(username, password, context, handler, key, url, secret, objects);
    }

    /**
     * 认证的url是否已经重载了.
     */
    boolean oauthFlag = false;
    boolean reload = false;

    /**
     * 通过webview认证
     *
     * @param username 用户名
     * @param password 密码
     * @param context
     * @param handler  认证中的ui处理器，用于回调的
     * @param s
     * @param key
     * @param objects  在认证前执行方法的参数。  @return webview，可以直接添加到ui上显示网页。
     */
    public WebView bindViews(final String username, final String password, Context context, final Handler handler,
        final String key, final String callback_url, final String secret, final Object[] objects) {
        WeiboLog.v("binds:" + username + " p:" + password);
        final OauthWebView webView = new OauthWebView(context, handler);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        //settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSavePassword(false);

        WebViewClient wvc = new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String reUrl = webView.getUrl();
                WeiboLog.v("reUrl:" + oauthFlag + " " + reUrl);
                if (! oauthFlag) {
                    if (! TextUtils.isEmpty(username) && ! TextUtils.isEmpty(password)) {
                        webView.loadUrl("javascript:(function() {" +
                            "   try{document.getElementById('userId').value='" + username + "';" +
                            "   document.getElementById('passwd').value='" + password + "';" +
                            "   document.getElementsByName('authZForm')[0].submit();" +
                            "   }catch(err){/*alert('oauth_failed');*/}" +
                            "        })();");
                    }
                    oauthFlag = true;
                }

                /**
                 * 不能一味地判断url相同就是认证失败，
                 * 前一步是登录，后一步是授权，需要再判断是否有uid，有了这个表示登录成功。
                 */
                if (AUTHENTICATIONURL.equals(reUrl)) {
                    WeiboLog.v("reload:" + reload);
                    if (! reload) {
                        webView.loadUrl("javascript:(function() {" +
                            "        var node=document.getElementsByName('uid')[0];" +
                            " if(undefined==node){alert('oauth_failed'); return;}" +
                            " var uid=document.getElementsByName('uid')[0].value;" +
                            "        if(uid==''){" +
                            "//alert('oauth_failed');" +
                            "        }else {" +
                            "        document.getElementsByName('authZForm')[0].submit();" +
                            "        }" +
                            "        })();");
                        reload = true;
                        Message message = Message.obtain();
                        message.what = 0;
                        message.obj = null;
                        handler.sendMessage(message);
                        webView.isOauthed = true;
                        return;
                    }

                    WeiboLog.e("认证失败。");
                    Message message = Message.obtain();
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    webView.isOauthed = true;
                    /*handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            webView.destroy();
                        }
                    }, 500l);*/
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WeiboLog.v("shouldOverrideUrlLoading.url:" + url);
                if (processOauthResult(url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                WeiboLog.v("onPageStarted.url:" + url);
                processOauthResult(url);
            }

            private boolean processOauthResult(String url) {
                if (url.contains("access_token=")) {
                    OauthBean oauthBean = null;
                    try {
                        Map<String, String> map = parseAccessToken(url);
                        oauthBean = new OauthBean();
                        oauthBean.accessToken = map.get("access_token");
                        oauthBean.expireTime = Long.valueOf(map.get("expires_in"));
                        oauthBean.openId = map.get("uid");
                        mOauthBean = oauthBean;
                        mAccessToken = oauthBean.accessToken;
                        mExpireTime = oauthBean.expireTime;
                        oauthBean.userId = map.get("uid");
                        oauthBean.name = username;
                        oauthBean.pass = password;
                        oauthBean.customKey = key;
                        oauthBean.customSecret = secret;
                        oauthBean.callbackUrl = callback_url;
                        oauthBean.authenticationUrl = AUTHENTICATIONURL;
                        WeiboLog.d("认证成功 oauthbean:" + oauthBean);
                        Message message = Message.obtain();
                        message.what = 0;
                        message.obj = new Object[]{oauthBean, objects};
                        message.arg1 = 0;
                        handler.sendMessage(message);
                        webView.isOauthed = true;
                        /*handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                webView.destroy();
                            }
                        }, 500l);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                } else {

                }
                return false;
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); 默认的处理方式，WebView变成空白页
                handler.proceed();//接受证书
                //handleMessage(Message msg); 其他处理
            }
        };
        webView.setWebViewClient(wvc);
        WebChromeClient webChromeClient = new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                WeiboLog.v("onJsAlert:" + message + " url:" + url);
                boolean rs = true;
                try {
                    //rs=super.onJsAlert(view, url, message, result);
                    if ("oauth_failed".equals(message)) {
                        WeiboLog.d("二次，认证失败");
                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = null;
                        webView.isOauthed = true;
                        handler.sendMessage(msg);
                        //webView.destroy();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return rs;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                WeiboLog.v("onJsConfirm:" + message + " url:" + url);
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                WeiboLog.v("onJsPrompt:" + message + " url:" + url);
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                WeiboLog.v("onJsBeforeUnload:" + message + " url:" + url);
                return super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public boolean onJsTimeout() {
                return super.onJsTimeout();
            }
        };
        webView.setWebChromeClient(webChromeClient);

        String url = buildOauthTokenUrl(key, callback_url);
        webView.loadUrl(url);

        return webView;
    }

    /**
     * 通过password类型获取授权，如果secret没有改变，就可以用这个方法。如果改变了，就没有办法了。
     *
     * @param username
     * @param password
     * @param key
     * @param secret
     * @return
     */
    public OauthBean fetchAccessTokenByPass(String username, String password, String key, String secret) {
        OauthBean oauthBean = null;
        DefaultHttpClient client = (DefaultHttpClient) SSLSocketFactoryEx.getNewHttpClient();
        client.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 9000);
        try {
            HttpPost post = new HttpPost("https://api.weibo.com/oauth2/access_token");

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("client_id", key));
            nvps.add(new BasicNameValuePair("client_secret", secret));
            nvps.add(new BasicNameValuePair("grant_type", "password"));
            nvps.add(new BasicNameValuePair("username", username));
            nvps.add(new BasicNameValuePair("password", password));

            post.setHeader("User-Agent", BaseApi.USERAGENT);
            post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = client.execute(post);
            String entity = EntityUtils.toString(response.getEntity());
            WeiboLog.d("entity:" + entity);

            JSONObject jsonObject = WeiboParser.contructJSONObject(entity);
            if (null != jsonObject) {
                oauthBean = new OauthBean();
                oauthBean.accessToken = jsonObject.getString("access_token");
                oauthBean.expireTime = jsonObject.getLong("expires_in");
                oauthBean.openId = jsonObject.getString("uid");
                oauthBean.customKey = key;
                oauthBean.customSecret = secret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return oauthBean;
    }

    //-----------------------------
    private static String encodeAccount(String account) {
        String userName = "";
        try {
            //userName = Base64.encodeBase64String(URLEncoder.encode(account, "UTF-8").getBytes());
            //userName = Base64.encodeToString(URLEncoder.encode(account, "UTF-8").getBytes(), Base64.DEFAULT);
            userName = com.me.microblog.Base64.encode(URLEncoder.encode(account, "UTF-8").getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return userName;
    }

    private static String makeNonce(int len) {
        String x = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String str = "";
        for (int i = 0; i < len; i++) {
            str += x.charAt((int) (Math.ceil(Math.random() * 1000000) % x.length()));
        }
        return str;
    }

    private static String getServerTime() {
        long servertime = new Date().getTime() / 1000;
        return String.valueOf(servertime);
    }

    class SinaSSOEncoder {

        private boolean i = false;
        private int g = 8;

        public SinaSSOEncoder() {
        }

        public String encode(String psw, String servertime, String nonce) {
            String password;
            password = hex_sha1("" + hex_sha1(hex_sha1(psw)) + servertime + nonce);
            return password;
        }

        private String hex_sha1(String j) {
            return h(b(f(j, j.length() * g), j.length() * g));
        }

        private String h(int[] l) {
            String k = i ? "0123456789ABCDEF" : "0123456789abcdef";
            String m = "";
            for (int j = 0; j < l.length * 4; j++) {
                m += k.charAt((l[ j >> 2 ] >> ((3 - j % 4) * 8 + 4)) & 15) + ""
                    + k.charAt((l[ j >> 2 ] >> ((3 - j % 4) * 8)) & 15);
            }
            return m;
        }

        private int[] b(int[] A, int r) {
            A[ r >> 5 ] |= 128 << (24 - r % 32);
            A[ ((r + 64 >> 9) << 4) + 15 ] = r;
            int[] B = new int[ 80 ];
            int z = 1732584193;
            int y = - 271733879;
            int v = - 1732584194;
            int u = 271733878;
            int s = - 1009589776;
            for (int o = 0; o < A.length; o += 16) {
                int q = z;
                int p = y;
                int n = v;
                int m = u;
                int k = s;
                for (int l = 0; l < 80; l++) {
                    if (l < 16) {
                        B[ l ] = A[ o + l ];
                    } else {
                        B[ l ] = d(B[ l - 3 ] ^ B[ l - 8 ] ^ B[ l - 14 ] ^ B[ l - 16 ], 1);
                    }
                    int C = e(e(d(z, 5), a(l, y, v, u)), e(e(s, B[ l ]), c(l)));
                    s = u;
                    u = v;
                    v = d(y, 30);
                    y = z;
                    z = C;
                }
                z = e(z, q);
                y = e(y, p);
                v = e(v, n);
                u = e(u, m);
                s = e(s, k);
            }
            return new int[]{z, y, v, u, s};
        }

        private int a(int k, int j, int m, int l) {
            if (k < 20) {
                return (j & m) | ((~ j) & l);
            }
            ;
            if (k < 40) {
                return j ^ m ^ l;
            }
            ;
            if (k < 60) {
                return (j & m) | (j & l) | (m & l);
            }
            ;
            return j ^ m ^ l;
        }

        private int c(int j) {
            return (j < 20) ? 1518500249 : (j < 40) ? 1859775393
                : (j < 60) ? - 1894007588 : - 899497514;
        }

        private int e(int j, int m) {
            int l = (j & 65535) + (m & 65535);
            int k = (j >> 16) + (m >> 16) + (l >> 16);
            return (k << 16) | (l & 65535);
        }

        private int d(int j, int k) {
            return (j << k) | (j >>> (32 - k));
        }

        private int[] f(String m, int r) {
            int[] l;
            int j = (1 << this.g) - 1;
            int len = ((r + 64 >> 9) << 4) + 15;
            int k;
            for (k = 0; k < m.length() * g; k += g) {
                len = k >> 5 > len ? k >> 5 : len;
            }
            l = new int[ len + 1 ];
            for (k = 0; k < l.length; k++) {
                l[ k ] = 0;
            }
            for (k = 0; k < m.length() * g; k += g) {
                l[ k >> 5 ] |= (m.charAt(k / g) & j) << (24 - k % 32);
            }
            return l;
        }
    }
}
