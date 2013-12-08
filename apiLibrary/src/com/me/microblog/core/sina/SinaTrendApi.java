package com.me.microblog.core.sina;

import android.text.TextUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.Trends;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:20
 * @description:
 */
public class SinaTrendApi extends AbsApiImpl {

    public static final String TAG="SinaTrendApi";

    //--------------------- 话题 ---------------------

    /**
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * 这个值比较重要，默认值即可。
     */
    public Trends getTrends(String type) throws WeiboException {
        String hourly="https://api.weibo.com/2/trends/hourly.json";
        String daily="https://api.weibo.com/2/trends/daily.json";
        String weekly="https://api.weibo.com/2/trends/weekly.json";
        String urlString=getBaseUrl()+"trends/"+type+".json";
        WeiboLog.i("urlString:"+urlString);

        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("base_app", String.valueOf(0));
        nvps.add(pair);

        String rs=get(urlString, false, nvps);
        WeiboLog.v("rs:"+rs);
        if (!TextUtils.isEmpty(rs)&&!"[]".equals(rs)&&"daily".equals(type)) {
            WeiboUtil.saveStatus(rs, App.getAppContext().getFilesDir().getAbsolutePath(), Constants.TREND_FILE);
        }
        return WeiboParser.parseTrends(rs);
    }

    /**
     * 关注某话题
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * trend_name 	true 	string 	要关注的话题关键词。
     *
     * @param trend_name return {"topicid": 1568197}
     */
    @Deprecated
    public Trends followTrend(String trend_name) throws WeiboException {
        String urlString=getBaseUrl()+"trends/follow.json";

        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("trend_name", trend_name);
        nvps.add(pair);

        String rs=get(urlString, true, nvps);
        return WeiboParser.parseTrends(rs);
    }

    /**
     * 取消对某话题的关注
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * trend_id 	true 	int64 	要取消关注的话题ID。
     *
     * @param trend_name return {"result": true}
     */
    @Deprecated
    public boolean unfollowTrend(String trend_name) throws WeiboException {
        String urlString=getBaseUrl()+"trends/follow.json";

        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("trend_name", trend_name);
        nvps.add(pair);

        String rs=get(urlString, true, nvps);
        WeiboLog.v(rs);
        return WeiboParser.parseResult(rs);
    }
}
