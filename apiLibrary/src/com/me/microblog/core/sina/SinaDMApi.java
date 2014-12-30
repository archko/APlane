package com.me.microblog.core.sina;

import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.DirectMessage;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.IDMApi;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:20
 * @description:
 */
public class SinaDMApi extends AbsApiImpl implements IDMApi {

    public static final String TAG = "SinaDMApi";
    //--------------------- 高级信息 ---------------------

    /**
     * get 获取当前用户发送的最新私信列表
     * GET 需要登录	访问级别：不开放接口  频次限制：是
     * since_id. 可选参数. 返回ID比数值since_id大（比since_id时间晚的）的私信。
     * max_id. 可选参数. 返回ID不大于max_id(时间不晚于max_id)的私信。
     * count. 可选参数. 每次返回的最大记录数（即页面大小），不大于200。
     * page. 可选参数. 返回结果的页序号。注意：有分页限制。
     */
    public SStatusData<DirectMessage> getSentDirectMessages(long sinnceId, long maxId, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages/sent.json";
        urlString += "?access_token=" + mAccessToken;

        ArrayList<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair basicNameValuePair;
        basicNameValuePair = new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(basicNameValuePair);
        basicNameValuePair = new BasicNameValuePair("page", String.valueOf(page));
        nvps.add(basicNameValuePair);

        if (sinnceId > 0) {
            basicNameValuePair = new BasicNameValuePair("since_id", String.valueOf(sinnceId));
            nvps.add(basicNameValuePair);
        }
        if (0 < maxId) {
            basicNameValuePair = new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(basicNameValuePair);
        }

        String rs = get(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "dm:" + rs);
        return WeiboParser.parseDirectMessages(rs);
    }

    /**
     * get 获取当前用户最新私信列表，只是收到的，没有发出的。
     * GET 需要登录	访问级别：不开放接口  频次限制：是
     * since_id. 可选参数. 返回ID比数值since_id大（比since_id时间晚的）的私信。
     * max_id. 可选参数. 返回ID不大于max_id(时间不晚于max_id)的私信。
     * count. 可选参数. 每次返回的最大记录数（即页面大小），不大于200
     * page. 可选参数. 返回结果的页序号。注意：有分页限制。
     *
     * @param l
     * @param l1
     * @param i
     * @param j
     * @return
     * @throws WeiboException
     */
    public SStatusData<DirectMessage> getDirectMessages(long sinceId, long maxId, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages.json";
        urlString += "?access_token=" + mAccessToken;
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();

        BasicNameValuePair basicNameValuePair;
        basicNameValuePair = new BasicNameValuePair("count", String.valueOf(count));
        nvps.add(basicNameValuePair);
        basicNameValuePair = new BasicNameValuePair("page", String.valueOf(page));
        nvps.add(basicNameValuePair);

        if (sinceId > - 1) {
            basicNameValuePair = new BasicNameValuePair("since_id", String.valueOf(sinceId));
            nvps.add(basicNameValuePair);
        }
        if (0 < maxId) {
            basicNameValuePair = new BasicNameValuePair("max_id", String.valueOf(maxId));
            nvps.add(basicNameValuePair);
        }

        String rs = get(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseDirectMessages(rs);
    }

    /**
     * get 获取当前用户私信用户列表，且与此用户的一条最新私信
     * GET 需要登录	访问级别：不开放接口  频次限制：是
     * since_id. 可选参数. 返回ID比数值since_id大（比since_id时间晚的）的私信。
     * max_id. 可选参数. 返回ID不大于max_id(时间不晚于max_id)的私信。
     * count. 可选参数. 每次返回的最大记录数（即页面大小），不大于200
     * page. 可选参数. 返回结果的页序号。注意：有分页限制。
     *
     * @return
     * @throws WeiboException
     */
    public SStatusData<DirectMessage> getDirectMessagesUsers() throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages/user_list.json";
        urlString += "?access_token=" + mAccessToken;

        String rs = get(urlString, false, null);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseDirectMessages(rs);
    }

    /**
     * 发送私信
     *
     * @param uid  接收者的uid，原来可能是id
     * @param text 私信内容
     * @return
     * @throws WeiboException
     */
    public DirectMessage sendDirectMessage(long uid, String text) throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages/new.json";
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));

        pair = new BasicNameValuePair("uid", String.valueOf(uid));
        nvps.add(pair);
        pair = new BasicNameValuePair("text", text);
        nvps.add(pair);

        String rs = post(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseDirectMessage(rs);
    }

    /**
     * 发送私信
     *
     * @param screen_name
     * @param text        私信内容
     * @return
     * @throws WeiboException
     */
    public DirectMessage sendDirectMessage(String screen_name, String text) throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages/new.json";
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));

        pair = new BasicNameValuePair("screen_name", screen_name);
        nvps.add(pair);
        pair = new BasicNameValuePair("text", text);
        nvps.add(pair);

        String rs = post(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseDirectMessage(rs);
    }

    /**
     * 删除一条私信
     *
     * @param id 私信的id
     * @return
     * @throws WeiboException
     */
    public final DirectMessage destroyDirectMessage(long id) throws WeiboException {
        String urlString = getBaseUrl() + "direct_messages/destroy.json";
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        nvps.add(new BasicNameValuePair("access_token", mAccessToken));
        pair = new BasicNameValuePair("id", String.valueOf(id));
        nvps.add(pair);

        String rs = post(urlString, false, nvps);
        WeiboUtils.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseDirectMessage(rs);
    }
}
