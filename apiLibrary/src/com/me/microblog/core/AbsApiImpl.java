package com.me.microblog.core;

import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.http.PostParameter;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.WeiboLog;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:13
 * @description: 抽象api，提供基本的get与post方法，由于子类的方法多样性，算不上是桥模式
 */
public abstract class AbsApiImpl {

    public final String OAUTH2_BASEURL="https://api.weibo.com/2/";
    String mAccessToken="";
    /**
     * 高级的key。高级的api都用这个来处理。
     */
    String mDAccessToken="";
    public static final String USERAGENT="Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
    protected static final String ACCEPTENCODING="gzip,deflate";

    //设置http参数
    public static final int CONNECT_TIMEOUT=6000;
    public static final int READ_TIMEOUT=10000;
    public static HttpParams httpParameters;

    {
        httpParameters=new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECT_TIMEOUT);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT);
    }

    protected AbsApiImpl() {
        updateToken();
    }

    public void setAccessToken(String mAccessToken) {
        this.mAccessToken=mAccessToken;
    }

    public void setDAccessToken(String mDAccessToken) {
        this.mDAccessToken=mDAccessToken;
    }

    public void updateToken() {
        App app=((App) App.getAppContext());
        OauthBean oauthBean=app.getOauthBean();
        if (null==oauthBean) {
            return;
        }
        mAccessToken=oauthBean.accessToken;
        oauthBean=app.getDOauthBean();
        if (null!=oauthBean) {
            mDAccessToken=oauthBean.accessToken;
        }
    }

    public String getBaseUrl() {
        return this.OAUTH2_BASEURL;
    }

    public String get(String urlString, boolean gzip) {
        if (urlString.indexOf("?")==-1) {
            urlString+="?access_token="+mAccessToken;
        } else {
            urlString+="&access_token="+mAccessToken;
        }
        HttpGet httpGet=new HttpGet(urlString);
        String rs=null;
        try {
            rs=TwitterOAuth2.execute(httpGet, gzip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public String get(String urlString, boolean gzip, List<BasicNameValuePair> nvps) throws WeiboException {
        if (urlString.indexOf("?")==-1) {
            urlString+="?access_token="+mAccessToken;
        } else {
            urlString+="&access_token="+mAccessToken;
        }

        for (NameValuePair nvp : nvps) {
            try {
                urlString+="&"+nvp.getName()+"="+URLEncoder.encode(nvp.getValue().trim(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        HttpGet httpGet=new HttpGet(urlString);
        String rs=null;
        rs=TwitterOAuth2.execute(httpGet, gzip);
        return rs;
    }

    public String post(String urlString, boolean gzip) throws WeiboException {
        HttpPost httpPost=new HttpPost(urlString);
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        String rs=null;
        rs=TwitterOAuth2.execute(httpPost, gzip);

        return rs;
    }

    public String post(String urlString, boolean gzip, List<BasicNameValuePair> nvps) throws WeiboException {
        HttpPost httpPost=new HttpPost(urlString);
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        String rs=null;
        rs=TwitterOAuth2.execute(httpPost, gzip);

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
        HttpPost post=new HttpPost(urlString);
        if (null!=parameters) {
            List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair("access_token", mAccessToken));
            for (PostParameter parameter : parameters) {
                nvps.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
            }

            try {
                post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }

        String rs=TwitterOAuth2.execute(post, false);

        return rs;
    }
}
