package com.me.microblog.core;

import android.text.TextUtils;
import com.me.microblog.WeiboException;
import com.me.microblog.http.SSLSocketFactoryEx;
import com.me.microblog.util.StreamUtils;
import com.me.microblog.util.WeiboLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * 获得Oauth2认证签名.
 *
 * @author archko
 */
public class TwitterOAuth2 {

    ///////////------- oauth url -------///////////////
    String authenticationUrl = null;
    String callbackUrl = null;
    private String username, password;

    public TwitterOAuth2(String username, String password, String authenticationUrl, String callbackUrl) {
        this.username = username;
        this.password = password;
        this.authenticationUrl = authenticationUrl;
        this.callbackUrl = callbackUrl;
    }

    /**
     * 执行Request,并返回字符串.
     *
     * @param request 需要执行的Request,可以是Post,get,delete等.
     * @return
     * @throws java.io.IOException
     */
    public static String execute(HttpUriRequest request) throws WeiboException {
        HttpResponse response = null;
        String string = "";

        HttpClient client = SSLSocketFactoryEx.getNewHttpClient();
        int statusCode = - 1;
        try {
            response = client.execute(request);

            // response status should be 200 OK
            statusCode = response.getStatusLine().getStatusCode();
            //WeiboLog.d("", "statusCode:" + statusCode);
            final String reason = response.getStatusLine().getReasonPhrase();

            string = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                WeiboLog.e(reason);
                throw new WeiboException(reason, statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(string)) {
            throw new WeiboException("获取数据失败.请确定网络是否正常.", statusCode);
        }
        return string;
    }

    /**
     * 是否使用gzip压缩内容.目前已知:getUser方法不可以使用
     *
     * @param request
     * @param gzip
     * @return
     */
    public static String execute(HttpUriRequest request, boolean gzip) throws WeiboException {
        HttpResponse response = null;
        String string = "";

        HttpClient client = SSLSocketFactoryEx.getNewHttpClient();
        if (gzip) {
            request.setHeader("Accept-Encoding", BaseApi.ACCEPTENCODING);
        }

        int statusCode = - 1;
        try {
            response = client.execute(request);

            // response status should be 200 OK
            statusCode = response.getStatusLine().getStatusCode();
            //WeiboLog.d("", "statusCode:" + statusCode);
            final String reason = response.getStatusLine().getReasonPhrase();
            Header[] headers = response.getAllHeaders();
            boolean flag = false;
            Header header;
            for (int i = 0; i < headers.length; i++) {
                header = headers[ i ];
                //System.out.println("headers:"+header.getName()+"--"+header.getValue());
                if (header.toString().indexOf("gzip") != 0) {
                    flag = true;
                    break;
                }
            }

            if (statusCode != 200) {
                WeiboLog.e("TwitterConnector", reason);
                throw new WeiboException(reason, statusCode);
            }

            if (! gzip) {
                flag = false;
            }

            if (flag) {
                //WeiboLog.i("Twitter", "gzip.");
                try {
                    string = StreamUtils.parseInputStream(new GZIPInputStream(response.getEntity().getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                    string = EntityUtils.toString(response.getEntity());
                }
            } else {
                string = EntityUtils.toString(response.getEntity());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (TextUtils.isEmpty(string)) {
            throw new WeiboException("获取数据失败.请确定网络是否正常.", statusCode);
        }

        return string;
    }

    public static HttpResponse execute2(HttpUriRequest request) throws IOException {
        HttpResponse response = null;
        HttpClient client = SSLSocketFactoryEx.getNewHttpClient();
        response = client.execute(request);

        return response;
    }

    public static byte[] getImageByte(String urlString) throws IOException {
        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, BaseApi.CONNECT_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParameters, BaseApi.READ_TIMEOUT);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpGet httpGet = new HttpGet(urlString);
            HttpResponse localHttpResponse = httpClient.execute(httpGet);
            int code = localHttpResponse.getStatusLine().getStatusCode();
            if (code != 200) {
                throw new WeiboException("" + localHttpResponse.getStatusLine().getReasonPhrase());
            }
            byte[] arrayOfByte = EntityUtils.toByteArray(localHttpResponse.getEntity());
            return arrayOfByte;
        } catch (Exception e) {
        }
        return null;
    }

    public static InputStream getImageStream(String urlString) throws IOException {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream inputStrem = null;

        url = new URL(urlString);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(BaseApi.CONNECT_TIMEOUT);
        conn.setReadTimeout(BaseApi.READ_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", BaseApi.USERAGENT);
        conn.connect();
        inputStrem = conn.getInputStream();

        return inputStrem;
    }
}
