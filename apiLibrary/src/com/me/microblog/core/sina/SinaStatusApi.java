package com.me.microblog.core.sina;

import android.text.TextUtils;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.Count;
import com.me.microblog.bean.Favorite;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.TwitterOAuth2;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.IStatusApi;
import com.me.microblog.http.PostParameter;
import com.me.microblog.util.WeiboLog;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:15
 * @description:
 */
public class SinaStatusApi extends AbsApiImpl implements IStatusApi {

    public static final String TAG="SinaStatusApi";

    //--------------------- timeline ---------------------
    protected ArrayList<Status> getStatuses(String jsonArrayString) throws WeiboException {
        return WeiboParser.parseStatuses(jsonArrayString);
    }

    protected SStatusData<Status> getStatuses2(String jsonArrayString) throws WeiboException {
        return WeiboParser.parseStatuses2(jsonArrayString);
    }

    /**
     * 与1一样，多了page参数。返回数据稍有不同。内容多了,以{"statuses":开头,而不是[]。
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * source 	false 	string 	采用OAuth授权方式不需要此参数，其他授权方式为必填参数，数值为应用的AppKey。
     * access_token 	false 	string 	采用OAuth授权方式为必填参数，其他授权方式不需要此参数，OAuth授权后获得。
     * count 	false 	int 	单页返回的记录条数，默认为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     */
    public SStatusData<Status> getPublicTimeline(int count) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/public_timeline.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        //WeiboUtil.savePublicTimeLine(rs);
        WeiboUtil.printResult(TAG, "getPublicTimeline:"+rs);
        return getStatuses2(rs);
    }

    /**
     * 获取当前登录用户及其所关注用户的最新微博
     * GET 需要登录	访问级别：普通接口  频次限制：是 获取当前用户所关注用户的最新微博信息
     * 可选参数:
     * since_id: 微博信息ID. 只返回ID比since_id大（比since_id时间晚的）的微博信息内容，默认为0
     * max_id: 微博信息ID. 返回ID不大于max_id的微博信息内容，默认为0。
     * count: 单页返回的记录条数，默认为50。
     * page: 返回结果的页序号。默认1
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getHomeTimeline(long sinceId, long maxId, int count, int page, int feature)
        throws WeiboException {
        String urlString=getBaseUrl()+"statuses/home_timeline.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        if (sinceId>0) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId>0) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (feature>0) {
            pair=new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 多了trim_user参数。与home_timeline同。
     * GET 需要登录	访问级别：普通接口  频次限制：是 获取当前用户所关注用户的最新微博信息
     * 可选参数:
     * since_id: 微博信息ID. 只返回ID比since_id大（比since_id时间晚的）的微博信息内容。
     * max_id: 微博信息ID. 返回ID不大于max_id的微博信息内容。
     * count: 每次返回的最大记录数，不能超过200，默认20.
     * page: 返回结果的页序号。注意：有分页限制。根据用户关注对象发表的数量，通常最多返回1,000条最新微博分页内容, 默认1
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     *
     * @param l
     * @param l1
     * @param i
     * @param j
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getFriendsTimeline(long sinceId, long maxId, int count, int page, int feature)
        throws WeiboException {
        String urlString=getBaseUrl()+"statuses/friends_timeline.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        if (sinceId>0) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId>0) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (feature>0) {
            pair=new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 获取当前登录用户及其所关注用户的最新微博的ID
     * GET 需要登录	访问级别：普通接口  频次限制：是 获取当前用户所关注用户的最新微博信息
     * 可选参数:
     * since_id: 微博信息ID. 只返回ID比since_id大（比since_id时间晚的）的微博信息内容，默认为0
     * max_id: 微博信息ID. 返回ID不大于max_id的微博信息内容，默认为0。
     * count: 单页返回的记录条数，默认为50。
     * page: 返回结果的页序号。默认1
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public void getFriendsTimelineIDS(long sinceId, long maxId, int count, int page, int feature)
        throws WeiboException {
        String urlString=getBaseUrl()+"statuses/friends_timeline/ids.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        if (sinceId>0) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId>0) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (feature>0) {
            pair=new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
    }

    /**
     * 获取某个用户最新发表的微博列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * uid 	false 	int64 	需要查询的用户ID。
     * screen_name 	false 	string 	需要查询的用户昵称。
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，默认为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     * 参数uid与screen_name二者必选其一，且只能选其一
     * 参数uid与screen_name都没有或错误，则默认返回当前登录用户的数据
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getUserTimeLine(long uid, long sinceId, long maxId, int count,
        int page, int feature) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/user_timeline.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        if (uid>0) {
            pair=new BasicNameValuePair("uid", String.valueOf(uid));
            nvps.add(pair);
        }

        if (0<sinceId) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0<maxId) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (0<feature) {
            pair=new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 获取某个用户最新发表的微博列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * uid 	false 	int64 	需要查询的用户ID。
     * screen_name 	false 	string 	需要查询的用户昵称。
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，默认为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     * 参数uid与screen_name二者必选其一，且只能选其一
     * 参数uid与screen_name都没有或错误，则默认返回当前登录用户的数据
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getUserTimeLine(String screenName, long sinceId, long maxId, int count,
        int page, int feature) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/user_timeline.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        if (!TextUtils.isEmpty(screenName)) {
            pair=new BasicNameValuePair("screenName", screenName);
            nvps.add(pair);
        }

        if (0<sinceId) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0<maxId) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (0<feature) {
            pair=new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 获取当前用户最新转发的微博列表,返回数据与时间线有些不同。
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * @param since_id： 可选参数（微博信息ID）. 只返回ID比since_id大（比since_id时间晚的）的微博信息内容
     * @param max_id:   可选参数（微博信息ID）. 返回ID不大于max_id的微博信息内容。
     * @param count:    可选参数. 每次返回的最大记录数，最多返回200条，默认50。
     * @param page：     可选参数. 分页返回。注意：最多返回200条分页内容。
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getRepostByMe(long sinceId, long maxId, int count, int page)
        throws WeiboException {
        String urlString=getBaseUrl()+"statuses/repost_by_me.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        if (0<sinceId) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0<maxId) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 获取最新的提到登录用户的微博列表，即@我的微博
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * since_id. 可选参数. 返回ID比数值since_id大（比since_id时间晚的）的提到。
     * max_id. 可选参数. 返回ID不大于max_id(时间不晚于max_id)的提到。
     * count. 可选参数. 每次返回的最大记录数（即页面大小），默认为50。
     * page. 可选参数. 返回结果的页序号。注意：有分页限制。
     * filter_by_author 	false 	int 	作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0。
     * filter_by_source 	false 	int 	来源筛选类型，0：全部、1：来自微博、2：来自微群，默认为0。
     * filter_by_type 	false 	int 	原创筛选类型，0：全部微博、1：原创的微博，默认为0。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     *
     * @param l
     * @param l1
     * @param i
     * @param j
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getMentions(long sinceId, long maxId, int count, int page,
        int filter_by_author, int filter_by_source, int filter_by_type) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/mentions.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);
        if (sinceId>0) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0<maxId) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (filter_by_author>0) {
            pair=new BasicNameValuePair("filter_by_author", String.valueOf(filter_by_author));
            nvps.add(pair);
        }

        if (filter_by_source>=0) {
            pair=new BasicNameValuePair("filter_by_source", String.valueOf(filter_by_source));
            nvps.add(pair);
        }

        if (filter_by_type>=0) {
            pair=new BasicNameValuePair("filter_by_type", String.valueOf(filter_by_type));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        //WeiboUtil.saveStatus(rs,"/sdcard/","mentions.json");
        return getStatuses2(rs);
    }

    /**
     * 按天返回热门微博转发榜的微博列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * count 	false 	int 	单页返回的记录条数，不超过50，默认为20。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * repost_type 类型repost_weekly为周热门，repost_daily为当天热门
     */
    @Deprecated
    public ArrayList<Status> getHotRepost(int count, String repost_type) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/hot/"+repost_type+".json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;

        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        String rs=get(urlString, false, nvps);
        //WeiboUtil.savePublicTimeLine(rs);
        return getStatuses(rs);
    }

    /**
     * 按天返回热门微博评论榜的微博列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * count 	false 	int 	单页返回的记录条数，不超过50，默认为20。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * repost_type 类型comments_weekly为周热门，comments_daily为当天热门
     *
     * @param count
     * @param page
     * @param repost_type
     * @return
     * @throws com.me.microblog.WeiboException
     */
    @Deprecated
    public ArrayList<Status> getHotComment(int count, int page, String repost_type)
        throws WeiboException {
        String urlString=getBaseUrl()+"statuses/hot/"+repost_type+".json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;

        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        /*if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }*/

        String rs=get(urlString, false, nvps);
        return getStatuses(rs);
    }

    /**
     * 批量获取指定微博的转发数评论数
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * ids 	true 	string 	需要获取数据的微博ID，多个之间用逗号分隔，最多不超过100个。
     *
     * @param al
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public ArrayList<Count> getCounts(long al[]) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/count.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;

        int i=0;
        int len=al.length;
        StringBuilder ids=new StringBuilder();
        for (; i<len; i++) {
            ids.append(al[i]).append(",");
        }

        pair=new BasicNameValuePair("ids", ids.toString());
        nvps.add(pair);

        String rs=get(urlString, true, nvps);
        return WeiboParser.parseCounts(rs);
    }

    /**
     * 转发一条微博
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * id 			true int64 要转发的微博ID
     * status 		false string  添加的转发文本。必须做URLEncode,信息内容不超过140个汉字。如不填则默认为“转发微博”。
     * is_comment 	false int  是否在转发的同时发表评论。0表示不发表评论，1表示发表评论给当前微博，2表示发表评论给原微博，3是1、2都发表。默认为0
     *
     * @param id
     * @param status
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Status repostStatus(long id, String status, String is_comment) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/repost.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;

        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        if (!TextUtils.isEmpty(status)) {
            pair=new BasicNameValuePair("status", status);
            nvps.add(pair);
        }

        pair=new BasicNameValuePair("is_comment", is_comment);
        nvps.add(pair);

        String rs=post(urlString, true, nvps);

        return WeiboParser.parseStatus(rs);
    }

    /**
     * 发布一条微博信息。连续两次发布的微博不可以重复；非会员发表定向微博，分组成员数最多200。
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * status 	true 	string 	要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
     * visible 	false 	int 	微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
     * list_id 	false 	string 	微博的保护投递指定分组ID，只有当visible参数为3时生效且必选。
     * lat 	false 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
     * long 	false 	float 	经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
     * annotations 	false 	string 	元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，
     * 必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
     *
     * 连续两次发布的微博不可以重复
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Status updateStatus(String status, double lat, double llong, int visible) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/update.json";

        PostParameter[] parameter;
        parameter=new PostParameter[]{
            new PostParameter("status", status),
            new PostParameter("lat", String.valueOf(lat)),
            new PostParameter("long", String.valueOf(llong)),
            new PostParameter("visible", String.valueOf(visible))};

        String rs=post(urlString, parameter);

        return WeiboParser.parseStatus(rs);
    }

    /**
     * 上传图片并发布一条新微博。请求必须用POST方式提交(注意采用multipart/form-data编码方式)。
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * status 	true 	string 	要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
     * pic 	true 	binary 	要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M。
     * lat 	false 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
     * long 	false 	float 	经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
     * annotations 	false 	string 	元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，
     * 必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
     *
     * return
     * throws WeiboException
     */
    public Status upload(String filePath, String contentString, double lat, double llong, int visible) throws WeiboException {
        String url=getBaseUrl()+"statuses/upload.json";
        HttpPost httppost=new HttpPost(url);
        MultipartEntity multipartentity=new MultipartEntity();
        File file=new File(filePath);

        if (file.exists()) {
            WeiboLog.i("file:"+file.getAbsolutePath());
            multipartentity.addPart("pic", new FileBody(file));
        }

        try {
            multipartentity.addPart("access_token", new StringBody(mAccessToken));
            multipartentity.addPart("status",
                new StringBody(contentString, Charset.forName("UTF-8")));
            if (0.0d!=lat) {
                multipartentity.addPart("lat", new StringBody(String.valueOf(lat)));
            }
            if (0.0d!=llong) {
                multipartentity.addPart("long", new StringBody(String.valueOf(llong)));
            }
            multipartentity.addPart("visible", new StringBody(String.valueOf(visible)));
        } catch (IllegalCharsetNameException e) {
            e.printStackTrace();
        } catch (UnsupportedCharsetException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httppost.setEntity(multipartentity);

        String rs=TwitterOAuth2.execute(httppost);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 指定一个图片URL地址抓取后上传并同时发布一条新微博
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     * status 	false 	string 	要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
     * visible 	false 	int 	微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
     * list_id 	false 	string 	微博的保护投递指定分组ID，只有当visible参数为3时生效且必选。
     * url 	false 	string 	图片的URL地址，必须以http开头。
     * pic_id 	false 	string 	已经上传的图片pid，多个时使用英文半角逗号符分隔，最多不超过9个。
     * lat 	false 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
     * long 	false 	float 	经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
     * annotations 	false 	string 	元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
     *
     * return
     * throws WeiboException
     */
    public Status uploadUrlText(String status, double lat, double llong, int visible, String pic_id, String url) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/upload_url_text.json";

        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;

        pair=new BasicNameValuePair("status", status);
        nvps.add(pair);

        pair=new BasicNameValuePair("lat", String.valueOf(lat));
        nvps.add(pair);

        pair=new BasicNameValuePair("long", String.valueOf(llong));
        nvps.add(pair);

        pair=new BasicNameValuePair("visible", String.valueOf(visible));
        nvps.add(pair);

        pair=new BasicNameValuePair("pic_id", pic_id);
        nvps.add(pair);

        String rs=post(urlString, false, nvps);

        WeiboUtil.printResult(TAG, "uploadPic:"+rs);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 上传图片，返回图片picid,urls(3个url)
     * POST 需要登录	访问级别：高级接口（需要授权） 频次限制：是
     * pic 	true 	string 	需要上传的图片路径。
     *
     * return
     * throws WeiboException
     */
    public void uploadPic(String filePath) throws WeiboException {
        String url=getBaseUrl()+"statuses/upload_pic.json";
        HttpPost httppost=new HttpPost(url);
        MultipartEntity multipartentity=new MultipartEntity();
        File file=new File(filePath);

        if (file.exists()) {
            WeiboLog.i("file:"+file.getAbsolutePath());
            multipartentity.addPart("pic", new FileBody(file));
        }

        try {
            multipartentity.addPart("access_token", new StringBody(mAccessToken));
        } catch (IllegalCharsetNameException e) {
            e.printStackTrace();
        } catch (UnsupportedCharsetException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httppost.setEntity(multipartentity);

        String rs=TwitterOAuth2.execute(httppost);
        WeiboUtil.printResult(TAG, "uploadPic:"+rs);
        //return WeiboParser.parseStatus(rs);
    }

    /**
     * 是否需要登录 true
     * POST, DELETE 删除一条微博信息
     *
     * @param id. 必须参数. 要删除的微博ID.
     * @return
     */
    public Status deleteStatus(long id) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/destroy.json";

        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;

        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=post(urlString, true, nvps);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 根据微博ID获取单条微博内容
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * id 	true 	int64 	需要获取的动态ID。
     *
     * 与timeline.json相关，动态id从上面取得，其实微博的内容一样，不必再获取一次，但有时需要刷新。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Status getStatusShow(long id) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/show.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;

        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=get(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 获取当前登录用户关注的人发给其的定向微博
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * page 	false 	int 	返回结果的页码，默认为1。
     * count 	false 	int 	返回结果的条数数量，最大不超过200，默认为20。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回uid，默认为0。
     *
     * A与B互相关注，B在A的分组G中，A发送定向微博S到G，则，在B的【发给我的微博中】会包括S这条微博；
     */
    public SStatusData<Status> getToMeStatus(long sinceId, long maxId, int count, int page) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/to_me.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        if (0<sinceId) {
            pair=new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0<maxId) {
            pair=new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (0<page) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        return getStatuses2(rs);
    }

    /**
     * 屏蔽某条微博
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     * id 	true 	int64 	微博id。
     *
     * @return 屏蔽的微博
     */
    public Status filterCreate(long id) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/filter/create.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=post(urlString, true, nvps);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 屏蔽某个@到我的微博以及后续由对其转发引起的@提及
     *
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     * id 	true 	int64 	需要屏蔽的@提到我的微博ID。此ID必须在statuses/mentions列表中。
     * follow_up 	false 	int 	是否仅屏蔽当前微博。0：仅屏蔽当前@提到我的微博；1：屏蔽当前@提到我的微博，以及后续对其转发而引起的@提到我的微博。默认1。
     *
     * @return 屏蔽是否成功{result":true}
     */
    public boolean mentionsShield(long id, int follow_up) throws WeiboException {
        String urlString=getBaseUrl()+"statuses/mentions/shield.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        if (0<follow_up) {
            pair=new BasicNameValuePair("follow_up", String.valueOf(follow_up));
            nvps.add(pair);
        }

        String rs=post(urlString, true, nvps);
        return WeiboParser.parseResult(rs);
    }

    //--------------------- 收藏 ---------------------

    /**
     * 添加一条微博到收藏里
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * id 	true 	int64 	要收藏的微博ID。
     *
     * @return
     */
    public Favorite createFavorite(long id) throws WeiboException {
        String urlString=getBaseUrl()+"favorites/create.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=post(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
        return WeiboParser.parseFavorite(rs);
    }

    /**
     * 取消收藏一条微博
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * id 	true 	int64 	要取消收藏的微博ID。
     *
     * @return
     */
    public Favorite destroyFavorite(long id) throws WeiboException {
        String urlString=getBaseUrl()+"favorites/destroy.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=post(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
        return WeiboParser.parseFavorite(rs);
    }

    /**
     * 根据收藏ID批量取消收藏
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * ids 	true 	string 	要取消收藏的收藏ID，用半角逗号分隔，最多不超过10个。
     *
     * @return { "result": true }
     */
    public boolean destroyFavorites(String ids) throws WeiboException {
        String urlString=getBaseUrl()+"favorites/destroy_batch.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("ids", ids);
        nvps.add(pair);

        String rs=post(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
        return WeiboParser.parseResult(rs);
    }

    /**
     * 获取当前登录用户的收藏列表
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * count 	false 	int 	单页返回的记录条数，默认为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     *
     * @return
     */
    public SStatusData<Favorite> myFavorites(int count, int page) throws WeiboException {
        String urlString=getBaseUrl()+"favorites.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        if (count>0) {
            pair=new BasicNameValuePair("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page>0) {
            pair=new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs=get(urlString, true, nvps);
        //WeiboUtil.printResult("rs:"+rs);
        return WeiboParser.parseFavorites(rs);
    }

    /**
     * 根据收藏ID获取指定的收藏信息
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * id 	true 	int64 	需要查询的收藏ID。
     *
     * @return
     */
    public Favorite showFavorite(long id) throws WeiboException {
        String urlString=getBaseUrl()+"favorites/show.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair=new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs=get(urlString, true, nvps);
        WeiboUtil.printResult(TAG, "rs:"+rs);
        return WeiboParser.parseFavorite(rs);
    }
}
