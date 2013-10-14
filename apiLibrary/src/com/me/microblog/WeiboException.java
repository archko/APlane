package com.me.microblog;

/**
 * An exception class that will be thrown when WeiboAPI calls are failed.<br>
 * In case the Weibo server returned HTTP error code, you can get the HTTP status code using getStatusCode() method.
 */
public class WeiboException extends Exception {

    private int statusCode=-1;

    public WeiboException(String msg) {
        super(msg);
    }

    public WeiboException(Exception cause) {
        super(cause);
    }

    public WeiboException(String msg, int statusCode) {
        super(msg);
        this.statusCode=statusCode;
    }

    public WeiboException(String msg, Exception cause) {
        super(msg, cause);
    }

    public WeiboException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode=statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String toString() {
        return "WeiboException{"+
            "statusCode="+statusCode+
            "msg="+super.toString()+
            '}';
    }

    //--------------------- 主要的错误代码 ---------------------
    public static final int API_ERROR=100001; //api获取失败。
    public static final int TOKEN_ERROR=100002; //token异常。api获取失败。

    /**
     * token过期代码
     */
    public static final int EX_CODE_TOKEN_EXPIRE=100003;

    public static final int SYSTEM_ERROR=10001; //系统错误
    public static final int SERVICE_UNAVALIBLE=10002; //服务端资源不可用
    public static final int REMOTE_SERVICE_ERROR=10003; //远程服务出错
    public static final int LACK_OF_KEY_PERMISSION=10005; //该资源需要appkey拥有更高级的授权

    public static final int APP_API_LIMIT=10014; //第三方应用访问api接口权限受限制

    public static final int IP_MAX_LIMIT=10022; //IP请求超过上限
    public static final int USER_MAX_LIMIT=10023; //用户请求超过上限
    public static final int API_MAX_LIMIT=10024; //用户请求接口%s超过上限

    public static final int REPOST_SELF=20103; //不能转发自己的微博

    public static final int LACK_OF_REPOST_PERMISSION=20120; //由于作者设置了可见性，你没有权限转发此微博

    public static final int SEND_TO_MORE=20016; //发微博太多啦，休息一会儿吧
    public static final int SEND_RECENT=20017; //你刚刚已经发送过相似内容了哦，先休息一会吧
    public static final int SEND_TO_DUPLICATE=20019; //不要太贪心哦，发一次就够啦
    public static final int ALLOW_FOLLOW_COMMENT=20206; //作者只允许关注用户评论
    public static final int ALLOW_TRUST_COMMENT=20207; //作者只允许可信用户评论
    public static final int LACK_OF_COMMENT_PERMISSION=20130; //由于作者隐私设置，你没有权限评论此微博

    public static final int FOLLOW_SELF=20504; //你不能关注自己
    public static final int FOLLOW_MAX_LIMIT=20505; //加关注请求超过上限

}
