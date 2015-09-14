package com.me.microblog.core;

import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.http.PostParameter;
import com.me.microblog.oauth.OauthBean;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:13
 * @description: 抽象api，提供基本的get与post方法，由于子类的方法多样性，算不上是桥模式
 */
public abstract class AbsApiImpl {

    public static final String OAUTH2_BASEURL = "https://api.weibo.com/2/";
    public String mAccessToken = "";
    public static final String USERAGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
    protected static final String ACCEPTENCODING = "gzip,deflate";

    //设置http参数
    public static final int CONNECT_TIMEOUT = 6000;
    public static final int READ_TIMEOUT = 10000;
    /*public static HttpParams httpParameters;

    {
        httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECT_TIMEOUT);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT);
    }*/

    public AbsApiImpl() {
        updateToken();
    }

    public void setAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }

    public void updateToken() {
        App app = ((App) App.getAppContext());
        OauthBean oauthBean = app.getOauthBean();
        if (null == oauthBean) {
            return;
        }
        mAccessToken = oauthBean.accessToken;
    }

    public String getBaseUrl() {
        return this.OAUTH2_BASEURL;
    }

    public String get(String urlString, boolean gzip) {
        if (urlString.indexOf("?") == - 1) {
            urlString += "?access_token=" + mAccessToken;
        } else {
            urlString += "&access_token=" + mAccessToken;
        }
        //HttpGet httpGet = new HttpGet(urlString);
        String rs = null;
        try {
            //rs = TwitterOAuth2.get(httpGet, gzip);
            rs=TwitterOAuth2.get(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public String get(String urlString, boolean gzip, List<PostParameter> nvps) throws WeiboException {
        if (!urlString.contains("?")) {
            urlString += "?access_token=" + mAccessToken;
        } else {
            urlString += "&access_token=" + mAccessToken;
        }

        for (PostParameter nvp : nvps) {
            try {
                urlString += "&" + nvp.getName() + "=" + URLEncoder.encode(nvp.getValue().trim(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //HttpGet httpGet = new HttpGet(urlString);
        String rs = null;
        //rs = TwitterOAuth2.execute(httpGet, gzip);
        try {
            rs=TwitterOAuth2.get(urlString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public String post(String urlString, boolean gzip) throws WeiboException {
        /*HttpPost httpPost = new HttpPost(urlString);
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }*/

        String rs = null;
        //rs = TwitterOAuth2.execute(httpPost, gzip);
        RequestBody formBody=new FormEncodingBuilder()
            .add("access_token", mAccessToken)
            .build();
        try {
            rs=TwitterOAuth2.postForm(urlString, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rs;
    }

    public String post(String urlString, boolean gzip, List<PostParameter> nvps) throws WeiboException {
        String rs = null;
        FormEncodingBuilder formEncodingBuilder=new FormEncodingBuilder()
            .add("access_token", mAccessToken);
        if (null!=nvps) {
            for (PostParameter parameter : nvps) {
                formEncodingBuilder.add(parameter.getName(), parameter.getValue());
            }
        }
        try {
            RequestBody formBody=formEncodingBuilder.build();
            rs=TwitterOAuth2.postForm(urlString, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rs;
    }

    /**
     * 还参数的请求
     *
     * @param urlString  请求URL
     * @param parameters 请求参数,一个数组.
     * @return
     */
    public String post(String urlString, PostParameter[] parameters) throws WeiboException {
        /*HttpPost post = new HttpPost(urlString);
        if (null != parameters) {
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair("access_token", mAccessToken));
            for (PostParameter parameter : parameters) {
                nvps.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
            }

            try {
                post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }*/

        //String rs = TwitterOAuth2.execute(post, false);
        String rs = null;
        //rs = TwitterOAuth2.execute(httpPost, gzip);
        FormEncodingBuilder formEncodingBuilder=new FormEncodingBuilder()
            .add("access_token", mAccessToken);
        if (null!=parameters) {
            for (PostParameter parameter : parameters) {
                formEncodingBuilder.add(parameter.getName(), parameter.getValue());
            }
        }

        try {
            RequestBody formBody=formEncodingBuilder.build();
            rs=TwitterOAuth2.postForm(urlString, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rs;
    }
}
