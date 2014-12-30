package com.me.microblog.core;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.ResultToken;
import com.me.microblog.http.PostParameter;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.List;

/**
 * 新浪微博的api实现方法,作为父类,包含所有的api,共同的部分.
 *
 * @author archko
 */
@Deprecated
public abstract class BaseApi {

    public static final String DE_OAUTH_CALLBACK_URL = "http://archko.deoauth.com";
    public static final String USERAGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
    protected static final String ACCEPTENCODING = "gzip,deflate";

    //设置http参数
    public static final int CONNECT_TIMEOUT = 6000;
    public static final int READ_TIMEOUT = 10000;
    public static HttpParams httpParameters;

    {
        httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECT_TIMEOUT);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT);
    }

    /**
     * 获取基本的host
     *
     * @return
     */
    public abstract String getBaseUrl();

    public abstract ResultToken oauth();

    public abstract String get(String urlString);

    public abstract String getNotZIP(String urlString);

    public abstract String post(String urlString, boolean gzip) throws WeiboException;

    /**
     * 还参数的请求
     *
     * @param urlString  请求URL
     * @param parameters 请求参数,一个数组.
     * @return
     */
    public abstract String post(String urlString, PostParameter[] parameters) throws WeiboException;

    public abstract String post(String urlString, List<BasicNameValuePair> nvps)
        throws WeiboException;

    public abstract String get(String urlString, List<BasicNameValuePair> nvps)
        throws WeiboException;

    public abstract String getNotGZIP(String urlString, List<BasicNameValuePair> nvps)
        throws WeiboException;

    //public abstract String delete(String urlString) throws WeiboException;

    //------------------------------ api --------------------------------

}
