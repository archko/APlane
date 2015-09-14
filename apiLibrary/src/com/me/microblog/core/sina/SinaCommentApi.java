package com.me.microblog.core.sina;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.ICommentApi;
import com.me.microblog.http.PostParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:18
 * @description:
 */
public class SinaCommentApi extends AbsApiImpl implements ICommentApi {

    public static final String TAG = "SinaCommentApi";

    //--------------------- 评论 ---------------------
    protected SStatusData<Comment> parseComments2(String rs) throws WeiboException {
        SStatusData<Comment> arraylist = WeiboParser.parseComments2(rs);
        return arraylist;
    }

    /**
     * 根据微博ID返回某条微博的评论列表
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * id. 必选参数. 返回指定的微博ID
     * count. 可选参数. 每次返回的最大记录数（即页面大小），不大于200，默认为20。
     * page. 可选参数. 返回结果的页序号。注意：有分页限制。
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的评论（即比since_id时间晚的评论），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的评论，默认为0。
     * filter_by_author 	false 	int 	作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0。
     *
     * @param id
     * @param count
     * @param page
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Comment> getComments(long id, int count, int page, long sinceId, long maxId,
        int filter_by_author) throws WeiboException {
        String urlString = getBaseUrl() + "comments/show.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("id", String.valueOf(id));
        nvps.add(pair);

        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0 < maxId) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (filter_by_author > 0) {
            pair = new PostParameter("filter_by_author", String.valueOf(filter_by_author));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        return parseComments2(rs);
    }

    /**
     * 获取当前登录用户所发出的评论列表
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * since_id：可选参数（评论ID）. 只返回比since_id大（比since_id时间晚的）的评论
     * max_id:false         可选参数（评论ID）. 返回ID不大于max_id的评论。
     * count:               false	可选参数. 每次返回的最大记录数，默认为50。
     * page                 false	返回结果的页码，默认为1。
     * filter_by_source 	false 	int 	来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Comment> getCommentsByMe(long sinceId, long maxId, int count, int page,
        int filter_by_source) throws WeiboException {
        String urlString = getBaseUrl() + "comments/by_me.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0 < maxId) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (filter_by_source > 0) {
            pair = new PostParameter("filter_by_source", String.valueOf(filter_by_source));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        return parseComments2(rs);
    }

    /**
     * 获取当前登录用户所接收到的评论列表
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * since_id：可选参数（评论ID）. 只返回比since_id大（比since_id时间晚的）的评论
     * max_id:              false	可选参数（评论ID）. 返回ID不大于max_id的评论。
     * count:               false	可选参数. 每次返回的最大记录数，默认为50。
     * page                 false	返回结果的页码，默认为1。
     * filter_by_author 	false 	int 	作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0。
     * filter_by_source 	false 	int 	来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Comment> getCommentsToMe(long sinceId, long maxId, int count, int page,
        int filter_by_author, int filter_by_source) throws WeiboException {
        String urlString = getBaseUrl() + "comments/to_me.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0 < maxId) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (filter_by_author > 0) {
            pair = new PostParameter("filter_by_author", String.valueOf(filter_by_author));
            nvps.add(pair);
        }

        if (filter_by_source > 0) {
            pair = new PostParameter("filter_by_source", String.valueOf(filter_by_source));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        return parseComments2(rs);
    }

    /**
     * 获取当前登录用户的最新评论包括接收到的与发出的
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * since_id：可选参数（评论ID）. 只返回比since_id大（比since_id时间晚的）的评论
     * max_id:              false	可选参数（评论ID）. 返回ID不大于max_id的评论。
     * count:               false	可选参数. 每次返回的最大记录数，默认为50。
     * page                 false	返回结果的页码，默认为1。
     * trim_user 	false 	int 	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Comment> getCommentsTimeline(long sinceId, long maxId, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "comments/timeline.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }
        if (0 < maxId) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        return parseComments2(rs);
    }

    /**
     * 获取最新的提到当前登录用户的评论，即@我的评论
     * 多了"previous_cursor": 0,"next_cursor": 0,"total_number": 3
     * GET 需要登录	访问级别：普通接口  频次限制：是
     *
     * since_id：可选参数（评论ID）. 只返回比since_id大（比since_id时间晚的）的评论
     * max_id:              false	可选参数（评论ID）. 返回ID不大于max_id的评论。
     * count:               false	可选参数. 每次返回的最大记录数，默认为50。
     * page                 false	返回结果的页码，默认为1。
     * filter_by_author 	false 	int 	作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0。
     * filter_by_source 	false 	int 	来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Comment> getAtMeComments(long sinceId, long maxId, int count, int page,
        int filter_by_author, int filter_by_source) throws WeiboException {
        String urlString = getBaseUrl() + "comments/mentions.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("count", String.valueOf(count));
        nvps.add(pair);

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0 < maxId) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (filter_by_author > 0) {
            pair = new PostParameter("filter_by_author", String.valueOf(filter_by_author));
            nvps.add(pair);
        }

        if (filter_by_source > 0) {
            pair = new PostParameter("filter_by_source", String.valueOf(filter_by_source));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        return parseComments2(rs);
    }

    /**
     * 对一条微博进行评论
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * comment 	true 	string 	评论内容，必须做URLencode，内容不超过140个汉字。
     * id 	true 	int64 	需要评论的微博ID。
     * comment_ori 	false 	int 	当评论转发微博时，是否评论给原微博，0：否、1：是，默认为0。
     *
     * @return
     */
    public Comment commentStatus(long id, String comment, String comment_ori)
        throws WeiboException {
        String urlString = getBaseUrl() + "comments/create.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("id", String.valueOf(id));
        nvps.add(pair);
        pair = new PostParameter("comment", comment);
        nvps.add(pair);

        pair = new PostParameter("comment_ori", comment_ori);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseComment(rs);
    }

    /**
     * 回复一条评论
     * POST 需要登录	访问级别：普通接口  频次限制：是
     *
     * cid 	true 	int64 	需要回复的评论ID。
     * id 	true 	int64 	需要评论的微博ID。
     * comment 	true 	string 	回复评论内容，必须做URLencode，内容不超过140个汉字。
     * without_mention 	false 	int 	回复中是否自动加入“回复@用户名”，0：是、1：否，默认为0。
     * comment_ori 	false 	int 	当评论转发微博时，是否评论给原微博，0：否、1：是，默认为0。
     *
     * @return
     */
    public Comment commentReply(long cid, long id, String comment, String comment_ori) throws WeiboException {
        String urlString = getBaseUrl() + "comments/reply.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("cid", String.valueOf(cid));
        nvps.add(pair);
        pair = new PostParameter("id", String.valueOf(id));
        nvps.add(pair);
        pair = new PostParameter("comment", comment);
        nvps.add(pair);

        pair = new PostParameter("without_mention", "1");
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return WeiboParser.parseComment(rs);
    }
}
