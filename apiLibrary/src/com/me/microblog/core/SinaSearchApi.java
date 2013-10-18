package com.me.microblog.core;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.abs.ISearchApi;
import com.me.microblog.util.WeiboLog;
import org.apache.http.message.BasicNameValuePair;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:20
 * @description:
 */
public class SinaSearchApi extends AbsApiImpl implements ISearchApi{

    public static final String TAG="SinaSearchApi";
    //--------------------- 搜索 ---------------------

    /**
     * 搜索用户时的联想搜索建议
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * q 	true 	string 	搜索的关键字，必须做URLencoding。
     * count 	false 	int 	返回的记录条数，默认为10。
     * type 	false 	int 	学校类型，0：全部、1：大学、2：高中、3：中专技校、4：初中、5：小学，默认为0。
     */
    public ArrayList<User> getSuggestionsSchools(String q, int count) throws WeiboException {
        String urlString=getBaseUrl()+"search/suggestions/schools.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("q", q);
        nvps.add(pair);

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return WeiboParser.getUsers(rs);
    }

    /**
     * 搜索微博时的联想搜索建议
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * q 	true 	string 	搜索的关键字，必须做URLencoding。
     * count 	false 	int 	返回的记录条数，默认为10。
     * 返回值比较少，需要注意不是整个微博对象{"suggestion": "最关注","count": 72702},
     * 将这些搜索整合在一起，除了学校，其它的参数是一样的。
     *
     * @param type 类型，0为微博，1为用户，2为学校，3为公司，4为应用
     */
    public ArrayList<Map<String, String>> getSearchSuggestions(String q, int count, int type) throws WeiboException {
        String searchUrl="";
        if (type==0) {
            searchUrl="search/suggestions/statuses.json";
        } else if (type==1) {
            searchUrl="search/suggestions/users.json";
        } else if (type==2) {
            searchUrl="search/suggestions/schools.json";
        } else if (type==3) {
            searchUrl="search/suggestions/companies.json";
        } else if (type==4) {
            searchUrl="search/suggestions/apps.json";
        }

        String urlString=getBaseUrl()+searchUrl;
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("q", q);
        nvps.add(pair);

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        WeiboLog.v(TAG, "search:q:"+q+" count:"+count+" type:"+type);
        String rs=get(urlString, false, nvps);
        WeiboLog.v(TAG, "rs:"+rs);
        return WeiboParser.getSuggestions(rs, type);
    }

    /**
     * 搜索某一话题下的微博
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * q 	true 	string 	搜索的话题关键字，必须进行URLencode，utf-8编码。
     * count 	false 	int 	单页返回的记录条数，默认为10，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     *
     * 关键词只能为两#间的话题，即只能搜索某话题下的微博,只返回最新200条结果
     *
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public SStatusData<Status> searchTopics(String q, int count, int page) throws WeiboException {
        String urlString=getBaseUrl()+"search/topics.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("q", q);
        nvps.add(pair);

        if (0<count) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs=get(urlString, false, nvps);
        WeiboLog.v(TAG, "searchTopics:"+rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 返回与指定的一个或多个条件相匹配的微博。
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * q 	false 	string 	搜索的关键字。必须进行[http://en.wikipedia.org/wiki/URL_encoding
     * filter_ori 	false 	int 	过滤器，是否为原创，0为全部，5为原创，4为转发。默认为0。
     * filter_pic 	false 	int 	过滤器。是否包含图片。0为全部，1为包含，2为不包含。
     * fuid 	false 	int64 	微博作者的用户ID。
     * province 	false 	int 	省份ID，参考省份城市编码表
     * city 	false 	int 	城市ID，参考省份城市编码表
     * starttime 	false 	number 	开始时间，Unix时间戳
     * endtime 	false 	number 	结束时间，Unix时间戳
     * page 	false 	int 	页码
     * count 	false 	int 	每页返回的微博数。（默认返回10条，最大200条。）
     * needcount 	false 	boolean 	返回结果中是否包含返回记录数。true则返回搜索结果记录数。
     * base_app 	false 	int 	是否按照当前应用信息对搜索结果进行过滤。当值为1时，仅返回通过该应用发送的微博消息。
     * callback 	false 	string 	仅JSON方式支持，用于JSONP跨域数据访问。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> searchStatuses(String q, int count, int page) throws WeiboException {
        String urlString=getBaseUrl()+"search/statuses.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("q", URLEncoder.encode(q));
        nvps.add(pair);

        if (0<count) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs=get(urlString, false, nvps);
        WeiboLog.v("searchStatuses:"+rs);
        return WeiboParser.parseStatuses2(rs);
    }
}
