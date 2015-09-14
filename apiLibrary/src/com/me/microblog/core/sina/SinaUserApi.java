package com.me.microblog.core.sina;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.Relationship;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.IUserApi;
import com.me.microblog.http.PostParameter;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:20
 * @description:
 */
public class SinaUserApi extends AbsApiImpl implements IUserApi {

    public static final String TAG = "SinaUserApi";
    //--------------------- 用户 ---------------------

    /**
     * 用户OAuth授权之后获取用户UID，作用相当于旧版接口的（account/verify_credentials）
     * 但只返回uid而没有其它信息。TODO在解析时需要单独解析uid，原来的用户是返回信息，id。
     * get 验证当前用户身份是否合法
     * 访问级别：普通接口
     * 频次限制：否,是否需要登录 是
     * source 	false 	string 	采用OAuth授权方式不需要此参数，其他授权方式为必填参数，数值为应用的AppKey。
     * access_token 	false 	string 	采用OAuth授权方式为必填参数，其他授权方式不需要此参数，OAuth授权后获得。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public User getMyself() throws WeiboException {
        String urlString = getBaseUrl() + "account/get_uid.json";
        String rs = get(urlString, true);

        try {
            long id = WeiboParser.parseID(rs);
            User user = new User();
            user.id = id;
            return user;
        } catch (WeiboException e) {
            throw new WeiboException(e);
        }
    }

    /**
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	false 	int64 	需要查询的用户ID。
     * screen_name 	false 	string 	需要查询的用户昵称。
     * 参数uid与screen_name二者必选其一，且只能选其一
     *
     * @param 用户ID user_id. 指定用户UID,主要是用来区分用户UID跟微博昵称一样
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public User getUser(long id) throws WeiboException {
        String urlString = getBaseUrl() + "users/show.json";

        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("uid", String.valueOf(id));
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        WeiboLog.v(TAG, "getUser:" + id + " rs:" + rs);
        return WeiboParser.parseUser(rs);
    }

    /**
     * 根据用户ID获取用户资料（授权用户），同上
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * @param screen_name. 指定微博昵称，主要是用来区分用户UID跟微博昵称一样，产生歧义的时候。
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public User getUser(String screenName) throws WeiboException {
        String urlString = getBaseUrl() + "users/show.json";

        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("screen_name", screenName);
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        WeiboLog.v(TAG, "getUser:" + screenName + " rs:" + rs);
        return WeiboParser.parseUser(rs);
    }

    //--------------------- 关系 ------------------------

    /**
     * 获取用户关注列表及每个关注用户的最新一条微博
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	false 	int64 	需要查询的用户UID。
     * screen_name 	false 	string 	需要查询的用户昵称。
     * count 	false 	int 	单页返回的记录条数，默认为50，最大不超过200。
     * cursor 	false 	int 	返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0。
     * trim_status 	false 	int 	返回值中user字段中的status字段开关，0：返回完整status字段、1：status字段仅返回status_id，默认为1。
     * 参数uid与screen_name二者必选其一，且只能选其一
     *
     * @param userId
     * @param cur
     * @param count
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getFriends(long userId, long cur, int count, int trim_status) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/friends.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        pair = new PostParameter("uid", String.valueOf(userId));
        nvps.add(pair);

        if (cur > - 1) {
            pair = new PostParameter("cursor", String.valueOf(cur));
            nvps.add(pair);
        }

        pair = new PostParameter("trim_status", String.valueOf(trim_status));
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        return WeiboParser.getUserObjs2(rs);
    }

    public SStatusData<User> getFriends(String screenName, long cur, int count, int trim_status) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/friends.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        pair = new PostParameter("screen_name", screenName);
        nvps.add(pair);

        if (cur > - 1) {
            pair = new PostParameter("cursor", String.valueOf(cur));
            nvps.add(pair);
        }
        pair = new PostParameter("trim_status", String.valueOf(trim_status));

        String rs = get(urlString, true, nvps);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 获取用户的粉丝列表及每个粉丝的最新一条微博
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	false 	int64 	需要查询的用户UID。
     * screen_name 	false 	string 	需要查询的用户昵称。
     * count 	false 	int 	单页返回的记录条数，默认为50，最大不超过200。
     * cursor 	false 	int 	返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0。
     * trim_status 	false 	int 	返回值中user字段中的status字段开关，0：返回完整status字段、1：status字段仅返回status_id，默认为1。
     * 参数uid与screen_name二者必选其一，且只能选其一；最多返回5000条数据
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getMyFollowers(long userId, long cur, int count) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/followers.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("uid", String.valueOf(userId));
        nvps.add(pair);

        if (cur > - 1) {
            pair = new PostParameter("cursor", String.valueOf(cur));
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        pair = new PostParameter("trim_status", "0");
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        return WeiboParser.getUserObjs2(rs);
    }

    public SStatusData<User> getMyFollowers(String screenName, long cur, int count) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/followers.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        pair = new PostParameter("screen_name", screenName);
        nvps.add(pair);

        if (cur > - 1) {
            pair = new PostParameter("cursor", String.valueOf(cur));
            nvps.add(pair);
        }
        String rs = get(urlString, true, nvps);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 获取用户的活跃粉丝列表及每个粉丝的最新一条微博,似乎没有多余的数据，返回可解析成链表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户UID。
     * count 	false 	int 	返回的记录条数，默认为20，最大不超过200。
     *
     * @return 是一个用户列表.
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getMyActiveFollowers(long userId, int count) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/followers.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        if (userId > 0) {
            pair = new PostParameter("uid", String.valueOf(userId));
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }
        String rs = get(urlString, true, nvps);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 关注一个用户
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * uid 	false 	int64 	需要关注的用户ID。
     * screen_name 	false 	string 	需要关注的用户昵称。
     *
     * @param uid
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public User createFriendships(long uid) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/create.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("uid", String.valueOf(uid));
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseUser(rs);
    }

    public User createFriendships(String s) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/create.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("screen_name", s);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseUser(rs);
    }

    /**
     * 取消关注一个用户
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * uid 	false 	int64 	需要取消关注的用户ID。
     * screen_name 	false 	string 	需要取消关注的用户昵称。
     *
     * @param uid
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public User deleteFriendships(long uid) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/destroy.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("uid", String.valueOf(uid));
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseUser(rs);
    }

    public User deleteFriendships(String s) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/destroy.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("screen_name", s);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseUser(rs);
    }

    /**
     * 获取两个用户之间的详细关注关系情况
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * source_id 	false 	int64 	源用户的UID。
     * source_screen_name 	false 	string 	源用户的微博昵称。
     * target_id 	false 	int64 	目标用户的UID。
     * target_screen_name 	false 	string 	目标用户的微博昵称。
     * 参数source_id与source_screen_name二者必选其一，且只能选其一
     * 参数target_id与target_screen_name二者必选其一，且只能选其一
     *
     * @param uid
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Relationship getFriendship(Long sourceId, String source_screen_name, Long targetId,
        String target_screen_name) throws WeiboException {

        String urlString = getBaseUrl() + "friendships/show.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        if (sourceId != null) {
            pair = new PostParameter("source_id", String.valueOf(sourceId));
            nvps.add(pair);
        }
        if (source_screen_name != null && ! "".equals(source_screen_name)) {
            pair = new PostParameter("source_screen_name", source_screen_name);
            nvps.add(pair);
        }
        if (targetId != null) {
            pair = new PostParameter("target_id", String.valueOf(targetId));
            nvps.add(pair);
        }
        if (target_screen_name != null && ! "".equals(target_screen_name)) {
            pair = new PostParameter("target_screen_name", target_screen_name);
            nvps.add(pair);
        }
        String rs = get(urlString, true, nvps);
        return WeiboParser.parseRelationship(rs);
    }

    /**
     * 获取两个用户之间的详细关注关系情况
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * source_id 	false 	int64 	源用户的UID。
     * source_screen_name 	false 	string 	源用户的微博昵称。
     * target_id 	false 	int64 	目标用户的UID。
     * target_screen_name 	false 	string 	目标用户的微博昵称。
     * 参数source_id与source_screen_name二者必选其一，且只能选其一
     * 参数target_id与target_screen_name二者必选其一，且只能选其一
     *
     * @param uid
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Relationship getFriendship(Long sourceId, Long targetId) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/show.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("source_id", String.valueOf(sourceId));
        nvps.add(pair);

        pair = new PostParameter("target_id", String.valueOf(targetId));
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        return WeiboParser.parseRelationship(rs);
    }
}
