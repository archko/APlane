package com.me.microblog.core.sina;

import android.text.TextUtils;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.IGroupApi;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:15
 * @description:
 */
public class SinaGroupApi extends AbsApiImpl implements IGroupApi {

    public static final String TAG = "SinaGroupApi";

    //--------------------- 好友分组 ------------------------
    //需要单独向用户提出SCOPE授权请求的接口，用户单独授权后才可以调用

    /**
     * 获取指定用户的好友分组列表
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * 返回的数据按设置的分组顺序排序；
     * 好友分组上限为20，“未分组”不计入上限；
     * "未分组"的分组ID为0，此接口不返回“未分组”；
     *
     * 返回其它数据：],"hasvisible": false,"previous_cursor": 0,"next_cursor": 3504652345931549,"total_number": 2008
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Group> getGroups() throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups.json";

        String rs = get(urlString, true);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        /*if (!TextUtils.isEmpty(rs)){//&&!"[]".equals(rs)) { 不管有没有数据，都保存，
            WeiboUtil.saveStatus(rs, App.getAppContext().getFilesDir().getAbsolutePath(), Constants.GROUP_FILE);
        }*/
        return WeiboParser.parseGroups(rs);
    }

    /**
     * 获取当前登录用户某一好友分组的微博列表
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	需要查询的好友分组ID，建议使用返回值里的idstr，当查询的为私有分组时，则当前登录用户必须为其所有者。
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，最大不超过200，默认为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * feature 	false 	int 	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     *
     * 返回其它数据：],"hasvisible": false,"previous_cursor": 0,"next_cursor": 3504652345931549,"total_number": 2008
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getGroupTimeLine(String list_id, long sinceId, long maxId, int count,
        int page, int feature) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/timeline.json";
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair = new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        pair = new BasicNameValuePair("list_id", String.valueOf(list_id));
        nvps.add(pair);

        if (0 < sinceId) {
            pair = new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (0 < maxId) {
            pair = new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (0 < page) {
            pair = new BasicNameValuePair("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (0 < feature) {
            pair = new BasicNameValuePair("feature", String.valueOf(feature));
            nvps.add(pair);
        }

        pair = new BasicNameValuePair("base_app", "0");
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取某一好友分组下的成员列表
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	好友分组ID，建议使用返回值里的idstr。
     * count 	false 	int 	单页返回的记录条数，默认为50，最大不超过200。
     * cursor 	false 	int 	分页返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0。
     *
     * ],"next_cursor": 1,"previous_cursor": 0,"total_number": 41
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getGroupMembers(String list_id, long cur, int count) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/members.json";
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;

        pair = new BasicNameValuePair("list_id", String.valueOf(list_id));
        nvps.add(pair);

        pair = new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        if (cur > - 1) {
            pair = new BasicNameValuePair("cursor", String.valueOf(cur));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 获取当前登陆用户某个分组的详细信息
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	需要查询的好友分组ID，建议使用返回值里的idstr。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Group getGroup(String list_id) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/show.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_id", String.valueOf(list_id));
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseGroup(rs);
    }

    /**
     * 创建好友分组
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * name 	true 	string 	要创建的好友分组的名称，不超过10个汉字，20个半角字符。
     * description 	false 	string 	要创建的好友分组的描述，不超过70个汉字，140个半角字符。
     * tags 	false 	string 	要创建的好友分组的标签，多个之间用逗号分隔，最多不超过10个，每个不超过7个汉字，14个半角字符。
     *
     * 每个用户最多能够创建20个好友分组；
     * 重复创建同名分组将给出错误；用户自己创建的分组，相互之间名称不能重复；
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Group createGroup(String name, String description, String tags) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/create.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("name", String.valueOf(name));
        nvps.add(pair);

        if (! TextUtils.isEmpty(description)) {
            pair = new BasicNameValuePair("description", description);
            nvps.add(pair);
        }

        if (! TextUtils.isEmpty(tags)) {
            pair = new BasicNameValuePair("tags", tags);
            nvps.add(pair);
        }

        String rs = post(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseGroup(rs);
    }

    /**
     * 更新好友分组
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	需要更新的好友分组ID，建议使用返回值里的idstr，只能更新当前登录用户自己创建的分组。
     * name 	false 	string 	好友分组更新后的名称，不超过8个汉字，16个半角字符。
     * description 	false 	string 	好友分组更新后的描述，不超过70个汉字，140个半角字符。
     * tags 	false 	string 	好友分组更新后的标签，多个之间用逗号分隔，最多不超过10个，每个不超过7个汉字，14个半角字符。
     *
     * 当前登录用户必须为好友分组的创建者；
     * 参数name、description、tags，至少需要传其中一个，否则接口报错；
     * 重复的同名分组将给出错误；用户自己创建的分组，相互之间名称不能重复；
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Group updateGroup(String list_id, String name, String description, String tags) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/update.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_id", list_id);
        nvps.add(pair);

        pair = new BasicNameValuePair("name", name);
        nvps.add(pair);

        if (! TextUtils.isEmpty(description)) {
            pair = new BasicNameValuePair("description", description);
            nvps.add(pair);
        }

        if (! TextUtils.isEmpty(tags)) {
            pair = new BasicNameValuePair("tags", tags);
            nvps.add(pair);
        }

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseGroup(rs);
    }

    /**
     * 删除好友分组
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	要删除的好友分组ID，建议使用返回值里的idstr。
     *
     * 只能操作当前登录用户自己的好友分组；
     * {"id": 201101270045006180,"idstr": "201101270045006191","name": "同学"}
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    @Deprecated
    public String destroyGroup(String list_id) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/destroy.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_id", list_id);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        return rs;
    }

    /**
     * 添加关注用户到好友分组
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * uid 	true 	int64 	需要添加的用户的UID。
     * list_id 	true 	int64 	好友分组ID，建议使用返回值里的idstr。
     *
     * 好友分组成员上限为500；当前登录用户必须是该分组的创建者，且只能添加自己关注的人；
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Group addMemberToGroup(String uid, String list_id) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/members/add.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;

        pair = new BasicNameValuePair("uid", uid);
        nvps.add(pair);

        pair = new BasicNameValuePair("list_id", list_id);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseGroup(rs);
    }

    /**
     * 批量添加用户到好友分组
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	好友分组ID，建议使用返回值里的idstr。
     * uids 	true 	string 	需要添加的用户的UID，多个之间用逗号分隔，最多不超过30个。
     * group_descriptions 	false 	string 	添加成员的分组说明，每个说明最多8个汉字，16个半角字符，多个需先URLencode，
     * 然后再用半角逗号分隔，最多不超过30个，且需与uids参数一一对应。
     *
     * 好友分组成员上限为500；
     * 当前登录用户必须是该分组的创建者，且只能添加自己关注的人；
     * 返回值result，全部成功和部分成功返回true，全部失败返回false或错误结果；
     *
     * {"result": false}
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    @Deprecated
    public boolean addMembersToGroup(String list_id, String uids, String group_descriptions) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/members/add_batch.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_id", list_id);
        nvps.add(pair);

        pair = new BasicNameValuePair("uids", uids);
        nvps.add(pair);

        if (! TextUtils.isEmpty(group_descriptions)) {
            pair = new BasicNameValuePair("description", group_descriptions);
            nvps.add(pair);
        }

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseResult(rs);
    }

    /**
     * 更新好友分组中成员的分组说明
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_id 	true 	int64 	好友分组ID，建议使用返回值里的idstr。
     * uid 	true 	int64 	需要更新分组成员说明的用户的UID。
     * group_description 	false 	string 	需要更新的分组成员说明，每个说明最多8个汉字，16个半角字符，需要URLencode。
     *
     * 只能操作当前登录用户自己的好友分组；
     *
     * {"uid": 1404376560,"list_id": 201101270045006180,"group_description": "分组描述"
     * }
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    @Deprecated
    public String updateGroupDesc(String list_id, String uid, String group_description) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/members/update.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_id", list_id);
        nvps.add(pair);

        pair = new BasicNameValuePair("uid", uid);
        nvps.add(pair);

        if (! TextUtils.isEmpty(group_description)) {
            pair = new BasicNameValuePair("group_description", group_description);
            nvps.add(pair);
        }

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return rs;
    }

    /**
     * 删除好友分组内的关注用户
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * uid 	true 	int64 	需要删除的用户的UID。
     * list_id 	true 	int64 	好友分组ID，建议使用返回值里的idstr。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Group destroyGroupMember(String uid, String list_ids) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/members/destroy.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("uid", uid);
        nvps.add(pair);

        pair = new BasicNameValuePair("list_ids", list_ids);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseGroup(rs);
    }

    /**
     * 调整当前登录用户的好友分组顺序
     * POST 需要登录	访问级别：高级接口（需要授权）  频次限制：是
     *
     * list_ids 	true 	string 	调整好顺序后的分组ID列表，以逗号分隔，例：57,38，表示57排第一、38排第二，以此类推。
     * count 	true 	int 	好友分组数量，必须与用户所有的分组数一致、与分组ID的list_id个数一致。
     *
     * {"result": true}
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    @Deprecated
    public boolean orderGroup(String list_ids, int count) throws WeiboException {
        String urlString = getBaseUrl() + "friendships/groups/order.json";

        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair pair;
        pair = new BasicNameValuePair("list_ids", list_ids);
        nvps.add(pair);

        pair = new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseResult(rs);
    }
}
