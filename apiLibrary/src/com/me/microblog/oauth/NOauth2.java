package com.me.microblog.oauth;

import android.content.Context;
import com.me.microblog.http.SSLSocketFactoryEx;
import com.me.microblog.util.WeiboLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author archko
 */
public class NOauth2 extends BaseOauth2 {

    public static final String CONSUMER_KEY = "5mQDrc7XQaC2MLGC";
    public static final String AUTHENTICATIONURL = "https://api.t.163.com/oauth2/authorize";
    public static final String CALLBACK_URL = "http://archko.com";

    public String getCallbackUrl() {
        return CALLBACK_URL;
    }

    @Override
    public String buildOauthTokenUrl(String consumer_key, String callback_url) {
        String touchParam = "";

        touchParam = "&state=ABCDEFG";

        return AUTHENTICATIONURL + "?" + "client_id=" + consumer_key
            + "&redirect_uri=" + callback_url
            + "&response_type=token" + touchParam
            + "&display=mobile" + "&confirm=1&oauthCheckItem=1";
    }

    @Deprecated
    public OauthBean login(Object... params) {
        mOauthBean = null;
        mAccessToken = null;
        mExpireTime = 0L;

        String username = (String) params[ 0 ];
        String password = (String) params[ 1 ];

        try {
            HttpClient httpClient = SSLSocketFactoryEx.getNewHttpClient();

            // 读取用户名和密码
            HttpPost postMethod = new HttpPost("https://reg.163.com/logins.jsp");
            NameValuePair username2 = new BasicNameValuePair("username", username);
            NameValuePair password2 = new BasicNameValuePair("password", password);
            NameValuePair savelogin = new BasicNameValuePair("savelogin", "1");
            NameValuePair product = new BasicNameValuePair("product", "t");
            NameValuePair type = new BasicNameValuePair("type", "1");
            //NameValuePair url2 = new BasicNameValuePair("url","http://t.163.com/session/first");

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(username2);
            nvps.add(password2);
            nvps.add(savelogin);
            nvps.add(product);
            nvps.add(type);
            // nvps.add(url2);
            postMethod.setHeader("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; TheWorld)");
            postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = httpClient.execute(postMethod);

            return fetchAccessToken(httpClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    OauthBean fetchAccessToken(HttpClient client) {
        try {
            String urlString = buildOauthTokenUrl(CONSUMER_KEY, CALLBACK_URL);
            HttpPost post = new HttpPost(urlString);
            ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();

            BasicNameValuePair basicNameValuePair = new BasicNameValuePair("action",
                "submit");
            nvps.add(basicNameValuePair);
            basicNameValuePair = new BasicNameValuePair("client_id", CONSUMER_KEY);
            nvps.add(basicNameValuePair);
            basicNameValuePair = new BasicNameValuePair("redirect_uri", CALLBACK_URL);
            nvps.add(basicNameValuePair);
            basicNameValuePair = new BasicNameValuePair("confirm", "1");
            nvps.add(basicNameValuePair);
            basicNameValuePair = new BasicNameValuePair("oauthCheckItem", "1");
            nvps.add(basicNameValuePair);
            post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            // post.setHeader("User-Agent", USERAGENT);

            HttpContext context = new BasicHttpContext();
            HttpResponse httpResponse = client.execute(post, context);
            WeiboLog.d("httpResponse:" + httpResponse);

            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                String name = header.getName();
                WeiboLog.d("header:" + name + " val:" + header.getValue());
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
                        mOauthBean.refreshToken = map.get("refresh_token");
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
}
