package com.me.microblog.core;

import android.text.TextUtils;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Unread;
import com.me.microblog.util.WeiboLog;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:20
 * @description:
 */
public class SinaUnreadApi extends AbsApiImpl {

    public static final String TAG="SinaUnreadApi";
    static boolean mAdvanceTokenAvalible=false;
    //--------------------- 未读 ---------------------

    /**
     * 获取某个用户的各种消息未读数
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要获取消息未读数的用户UID，必须是当前登录用户。
     * callback 	false 	string 	JSONP回调函数，用于前端调用返回JS格式的信息。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public Unread getUnread(long uid) throws WeiboException {
        String urlString=getBaseUrl()+"remind/unread_count.json";
        if (mAdvanceTokenAvalible&&!TextUtils.isEmpty(mDAccessToken)) {
            urlString+="?access_token="+mDAccessToken;
        }
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("uid", String.valueOf(uid));
        nvps.add(pair);

        String rs=null;
        try {
            rs=get(urlString, false, nvps);
            WeiboLog.v("rs:"+rs);
        } catch (WeiboException e) {
            int code=e.getStatusCode();
            if (code==400) {
                mAdvanceTokenAvalible=false;
            }
            //e.printStackTrace();
            urlString=getBaseUrl()+"remind/unread_count.json";
            rs=get(urlString, false, nvps);
        }
        return WeiboParser.parseUnread(rs);
    }

    /**
     * 对当前登录用户某一种消息未读数进行清零
     * GET 需要登录	访问级别：高级接口（需要授权）  频次限制：否
     * type 	true 	string 	需要清零未读数的消息项，status：新微博数、follower：新粉丝数、cmt：新评论数、dm：新私信数、
     * mention_status：新提及我的微博数、mention_cmt：新提及我的评论数，一次只能操作一项。
     * 出错时返回：follower,cmt,dm,mention_status,mention_cmt,group,invite,badge,photo,attitude,close_friends_feeds,close_friends_mention_status,close_friends_mention_status,tome,close_friends_mention_cmt,close_friends_cmt,close_friends_attitude,close_friends_common_cmt,close_friends_invite,sys_notice,app_message
     *
     * {"result": true}
     *
     * @return
     * @throws WeiboException
     */
    public String setUnread(String type) throws WeiboException {
        String urlString=getBaseUrl()+"remind/set_count.json";
        List<BasicNameValuePair> nvps=new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair;
        pair=new BasicNameValuePair("type", type);
        nvps.add(pair);

        if (!TextUtils.isEmpty(mDAccessToken)) {
            nvps.add(new BasicNameValuePair("access_token", mDAccessToken));
        }

        String rs=get(urlString, false, nvps);
        WeiboLog.v("rs:"+rs);
        //TODO return WeiboParser.parseResult(rs);
        return rs;//return WeiboParser.parseSetUnread(rs);
    }
}
