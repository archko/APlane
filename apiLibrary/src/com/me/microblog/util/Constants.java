package com.me.microblog.util;

import android.os.Environment;

/**
 * User: archko Date: 12-6-25 Time: 上午10:09
 */
public class Constants {

    //登录一些相关变量
    public final static String TAG="Constants";
    /**
     * 发新微博的intent过滤器
     */
    public final static String INTENT_NEW_BLOG="com.me.microblog.intent.action.NEW_BLOG";
    public final static String PREF_USERNAME_KEY="username_key";
    public final static String PREF_PASSWORD_KEY="password_key";
    //public static final String KEY="abcdefgopqrstuvwxyzhijklmn";
    public final static String PREF_TOKEN="token";
    public final static String PREF_SECRET="secret";
    public final static String PREF_CURRENT_USER_ID="user_id";
    public static final String PREF_TIMESTAMP="timestamp";
    /**
     * 微博一次获取的数量的存储key,需要与prefsactivity里面的一样
     */
    public static final String PREF_WEIBO_COUNT="pref_weibo_count";

    public static final int INPUT_STRING_COUNT=140;

    //----------------------------

    public static final String PREF_SCREENNAME_KEY="screen_name";
    public static final String PREF_LOCATION="location";
    public static final String PREF_FOLLWWERCOUNT_KEY="followers_count";
    public static final String PREF_FRIENDCOUNT_KEY="friends_count";
    public static final String PREF_FAVOURITESCOUNT_KEY="favourites_count";
    public static final String PREF_STATUSCOUNT_KEY="statues_count";
    public static final String PREF_TOPICCOUNT_KEY="topic_count";
    public static final String PREF_PORTRAIT_URL="portrait_url";
    public static final String PREF_AVATAR_LARGE="avatar_large";
    public static final String PREF_NEES_TO_UPDATE="nees_to_update";

    /**
     * oauth2的token
     */
    public static final String PREF_ACCESS_TOKEN="access_token";
    /**
     * oauth2的token过期时间存储key
     */
    public static final String PREF_ACCESS_TOKEN_TIME="access_token_time";
    public static final String PREF_OAUTH_TYPE="oauth_type";
    public static final String PREF_SOAUTH_TYPE="soauth_type";   //新浪微博的登录方式
    //public static final String SOAUTH_TYPE_CLIENT="soauth_type_client";   //新浪微博的登录方式client，这是默认的
    //public static final String SOAUTH_TYPE_WEB="soauth_type_web";   //新浪微博的登录方式web

    //下载图片的一些常量 .
    public static final int PNG_TYPE=0;//非gif图片类型
    public static final int GIF_TYPE=1;//gif图片类型,可以解析为动画.

    /**
     * 头像
     */
    public static final int TYPE_PORTRAIT=1;
    /**
     * 微博图片
     */
    public static final int TYPE_PICTURE=2;
    /**
     * 转发微博图片
     */
    public static final int TYPE_RETWEET_PICTURE=3;
    /**
     * 缓存目录,如果有sd卡的话
     */
    public static final String CACHE_DIR=Environment.getExternalStorageDirectory().getAbsolutePath()+"/.microblog/";
    /**
     * 头像存储路径
     */
    public static final String ICON_DIR="/icon/";
    /**
     * 大图片存储路径
     */
    public static final String PICTURE_DIR="/picture/";
    /**
     * gif大图存储路径
     */
    public static final String GIF="/gif/";
    /**
     * 腾讯微博本地存储的文件
     */
    public static final String QSTATUS_HOME_TIMELINE="q_home_timeline.json";

    //-------------- 天气的一些属性，谷歌已经不提供服务了 ------------------
    public static final String GOOGLE_WEATHER="http://www.google.com/";
    public static final String WEA_URL_STRING="http://www.google.com/ig/api?hl=zh-cn&weather=";
    public static final String WEATHER_FILE="wea_file";
    public static final String WEA_TIMESTAMP="wea_timestamp";
    public static final String WEA_LOCATION="wea_location";
    public static final String WEA_LOCATION_ALIAS="wea_location_alias";
    public static final String WEA_DEFAULT_CITY="Fuzhou";

    //-------------- operation bar ------------------
    public static final int OP_ID_COMMENT=0;    //评论
    public static final int OP_ID_REPOST=1; //转发
    public static final int OP_ID_FAVORITE=2;   //收藏
    public static final int OP_ID_DELETE=3; //删除
    public static final int OP_ID_ORITEXT=4;    //原文
    public static final int OP_ID_FOLLOW=5; //关注用户
    public static final int OP_ID_VIEW_USER=6;   //查看用户
    public static final int OP_ID_REPOST_Q=7;   //快速转发
    public static final int OP_ID_DOWN_ORI_IMG=8;   //下载原图
    public static final int OP_ID_VIEW_IMG_GALLERY=9;   //图库显示
    public static final int OP_ID_GIF=10;   //显示gif动画
    public static final int OP_ID_STATUS=11;    //显示微博列表
    public static final int OP_ID_UNFOLLOW=12;    //取消关注
    public static final int OP_ID_FOLLOWS=13;    //粉丝列表
    public static final int OP_ID_FRIENDS=14;    //关注列表
    public static final int OP_ID_QUICK_REPOST=15;    //快速转发，默认的转发内容
    public static final int OP_ID_REPLY_COMMENT=16;    //回复评论
    public static final int OP_ID_OPB_DESTROY_COMMENT=17;    //删除评论
    public static final int OP_ID_DESTROY_BATCH_COMMENT=18;    //批量删除
    public static final int OP_ID_AT=19;    //at用户
    public static final int OP_ID_MORE_CONTENT=20;    //内容操作
    public static final int OP_ID_MORE_CONTENT_COPY_STATUS=21;    //原内容复制
    public static final int OP_ID_MORE_CONTENT_COPY_RET_STATUS=22;    //转发内容复制
    public static final int OP_ID_MORE_CONTENT_OTHER=23;    //其它的操作，@，#，url
    public static final int OP_ID_MORE_RETSTATUS=24;    //查看转发的微博原文
    public static final int OP_ID_REFRESH=25;    //刷新操作
    public static final int OP_ID_REPLY_DM=26;    //回复私信
    public static final int OP_ID_OPB_DESTROY_DM=27;    //删除私信
    public static final int OP_ID_SHIELD_ONE=28;    //屏蔽当前@的微博  TODO 没有必要屏蔽当前的
    public static final int OP_ID_SHIELD_ALL=29;    //屏蔽此微博@我
    public static final int OP_ID_COMMENT_STATUS=30;    //评论的微博原文

    //-------------- weibo service ------------------
    public static final String PREF_SERVICE_STATUS="service_status";  //新微博
    public static final String PREF_SERVICE_COMMENT="service_comment";  //新评论
    public static final String PREF_SERVICE_AT_COMMENT="service_at_comment";  //新at评论
    public static final String PREF_SERVICE_AT="service_at";  //新at
    public static final String PREF_SERVICE_FOLLOWER="service_follower";  //新粉丝
    public static final String PREF_SERVICE_DM="service_dm";  //新私信
    public static final String SERVICE_NOTIFY_RETASK="service_notify_retask";   //      服务需要重新调任务
    public static final String SERVICE_NOTIFY_UNREAD="service_notify_unread";   //服务的未计消息通知。

    public static final int WEIBO_COUNT=25;
    public static final int THREAD_COUNT=1;
    public static final int WEIBO_COUNT_MIN=15;
    /**
     * 图片下载的线程数量
     */
    public final static String PREF_THREAD_COUNT="pref_thread_count";

    public static final String TAB_ID_HOME="tab_id_home";  //主页id
    public static final String TAB_ID_COMMENT="tab_id_comment";  //
    public static final String TAB_ID_AT_STATUS="tab_id_at_status";  //at status
    public static final String TAB_ID_FOLLOWER="tab_id_follower";  //tab_id_follower
    public static final String TAB_ID_FRIEND="tab_id_friend";//
    public static final String TAB_ID_MY_POST="tab_id_my_post";
    public static final String TAB_ID_MY_FAV="tab_id_my_fav";
    public static final String TAB_ID_PROFILE="tab_id_profile";
    public static final String TAB_ID_PUBLIC="tab_id_public";
    public static final String TAB_ID_PLACE_FRIEND_TIMELINE="tab_id_place_friend_timeline"; //placefriendtimeline
    public static final String TAB_ID_HOT="tab_id_hot"; //热门用户与精微微博
    public static final String TAB_ID_HOT_COMMENT="tab_id_hot_comment"; //热门评论
    public static final String TAB_ID_HOT_REPOST="tab_id_hot_repost"; //热门转发
    public static final String TAB_ID_PLACE_NEARBY_USERS="tab_id_place_nearby_users"; //获取附近发位置微博的人
    public static final String TAB_ID_PLACE_NEARBY_PHOTOS="tab_id_place_nearby_photos"; //获取附近照片列表
    public static final String TAB_ID_DIRECT_MSG="tab_id_direct_msg";   //私信
    public static final String TAB_ID_SEND_TASK="tab_id_send_task";   //任务
    public static final String TAB_ID_ABOUT="tab_id_about";   //about
    public static final String TAB_ID_AT_COMMENT="tab_id_at_comment";  //at我的评论
    public static final String TAB_ID_SETTINGS="tab_label_settings"; //settings

    //未读消息类型常量
    public static final String REMIND_STATUS="status"; //新微博数
    public static final String REMIND_FOLLOWER="follower"; //新粉丝数
    public static final String REMIND_CMT="cmt"; //新评论数
    public static final String REMIND_DM="dm"; //新私信数
    public static final String REMIND_MENTION_STATUS="mention_status"; //新提及我的微博数
    public static final String REMIND_MENTION_CMT="cmt"; //新提及我的评论数，一次只能操作一项

    public static final String TREND_FILE="dailytrends";   //当前话题的文件json
    public static final String GROUP_FILE="group_file.json";   //分组的文件json，前缀是用户id
    public static final String USER_SELF_FILE="user_self_file.json";   //自己信息的文件json，前缀是用户id
    public static final String USER_FRIENDS_FILE="user_friends_file.json";   //用户关注
    @Deprecated //废除,用数据库存储
    public static final String COMMENT_FILE="comment_file.json";   //评论的的文件json，前缀是用户id
    @Deprecated //废除,用数据库存储
    public static final String MENTION_STATUS_FILE="mention_status_file.json";   //@我的微博
    public static final String MY_POST_FILE="my_post_file.json";   //我发布的，存储第一页
    public static final String MY_FAV_FILE="my_fav_file.json";   //我的收藏，存储第一页

    public static final String SHORTCUTS="shortcuts";

    //-------- auto update
    public static final long UPDATE_DELTA=24*3600000l;
    public static final String UPDATE_TIMESTAMP="update_timestamp";

    //---- 任务队列改变了。
    public static final String TASK_CHANGED="task_changed";

    public static final String EXIT_APP="exit_app";   //退出应用，主要是切换帐户时用到。

    //--------------------- 认证code ---------------------
    /**
     * 用户名与密码为空,无法认证
     */
    public static final int USER_PASS_IS_NULL=10000;
}
