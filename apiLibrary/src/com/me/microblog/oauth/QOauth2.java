package com.me.microblog.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.me.microblog.WeiboUtil;
import com.me.microblog.http.SSLSocketFactoryEx;
import com.me.microblog.util.WeiboLog;

/**
 * 
 * @author archko
 */
public class QOauth2 extends BaseOauth2 {

	@Deprecated
	public OauthBean login(Object... params) {
		try {
            String urlString="http://ptlogin2.id.qq.com/login?";
            WeiboLog.d("urlString:"+urlString);
            HttpPost post=new HttpPost(urlString);
            ArrayList nvps=new ArrayList();
            String userId="331337913@qq.com";
            String password="";

            BasicNameValuePair basicNameValuePair=new BasicNameValuePair("action", "submit");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("u", userId);
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("p", "ìQ%2ÍÇMrctu<");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("ptredirect", "1");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("from_ui", "1");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("u1", "https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=801184721&response_type=token&redirect_uri=http%3A%2F%2Farchko.com&checkStatus=yes&appfrom=&g_tk=&sessionKey=4028e2661a3b4ee9b2cbb3314439d6e6&checkType=showAuth");
            nvps.add(basicNameValuePair);

            basicNameValuePair=new BasicNameValuePair("fp", "loginerroralert");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("h", "1");
            nvps.add(basicNameValuePair);
            
            post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            //post.setHeader("User-Agent", USERAGENT);

            HttpClient client=SSLSocketFactoryEx.getNewHttpClient();//new DefaultHttpClient();
            HttpContext context = new BasicHttpContext();
			HttpResponse httpResponse = client.execute(post, context);
            
			HttpUriRequest currentReq = (HttpUriRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI()
					.toString() : (currentHost.toURI() + currentReq.getURI());
			WeiboLog.d("currentUrl:" + currentUrl);

            WeiboLog.d("httpResponse:"+WeiboUtil.parseInputStream(httpResponse));
            
            return fetchAccessToken(client);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
	}

	@Deprecated
	OauthBean fetchAccessToken(HttpClient client) {
		WeiboLog.d("parseAuthHtml.");
		try {
			String urlString="https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=801184721&response_type=token&redirect_uri=http%3A%2F%2Farchko.com";
			HttpPost post = new HttpPost(urlString);
			ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();

			String userId="331337913@qq.com";
            String password="wrf14174";
            
			BasicNameValuePair basicNameValuePair=new BasicNameValuePair("action", "submit");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("u", userId);
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("p", password);
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("ptredirect", "1");
            nvps.add(basicNameValuePair);
            basicNameValuePair=new BasicNameValuePair("from_ui", "1");
            nvps.add(basicNameValuePair);
            post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			post.getParams()
				.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			// post.setHeader("User-Agent", USERAGENT);

			// HttpClient client=SSLSocketFactoryEx.getNewHttpClient();
			// new DefaultHttpClient();
			HttpContext context = new BasicHttpContext();
			HttpResponse httpResponse = client.execute(post, context);

			HttpUriRequest currentReq = (HttpUriRequest) context
				.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) context
				.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI()
				.toString() : (currentHost.toURI() + currentReq.getURI());
			WeiboLog.d("currentUrl:" + currentUrl);

			Map<String, String> rs = parseAccessToken(currentUrl);
			String accessToken = rs.get("access_token");
			mAccessToken = accessToken;
			rs.get("");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	void saveAccessToken(Context ctx) {
	}

}
