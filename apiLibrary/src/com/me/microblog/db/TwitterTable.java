package com.me.microblog.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-7-4
 */
public class TwitterTable {

    //数据库名
    public static final String DB_NAME="twitter.db";
    //数据库版本
    public static final int VERSION=17;
    public static final String AUTHORITY="cn.archko.microblog";

    /**
     * 新浪微博内容表,在离线时可以查看内容,所以只保存必要的信息,并不是把整个Status的内容保存,
     * 只保存主页的20条信息,刷新时需要更新这里的数据,加载更多时不修改.
     */
    public static class SStatusTbl implements BaseColumns {

        public static final String TBNAME="sina_home_status";

        public static final String STATUS_ID="status_id"; //对应Status的id
        public static final String USER_ID="user_id"; //微博的发布者id,从Status的user属性中取得
        public static final String USER_SCREEN_NAME="screen_name"; //来源同上
        public static final String PORTRAIT="portrait";//用户的头像,来源同上
        public static final String CREATED_AT="created_at";//微博的发布时间
        public static final String TEXT="text";//微博的内容
        public static final String SOURCE="source";//微博发布的来源

        public static final String FAVORITED="favorited";  //暂时无用

        public static final String TYPE="status_type"; //存储着微博的类型
        public static final String UID="uid"; //用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询

        public static final String PIC_THUMB="pic_thumbnail"; //微博的小图
        public static final String PIC_MID="pic_middle";
        public static final String PIC_ORIG="pic_original";

        public static final String R_STATUS_ID="r_status_id";//转发微博的id
        public static final String R_STATUS_USERID="r_status_userid";//转发微博的作者id
        public static final String R_STATUS_NAME="r_status_name";//转发微博的作者
        public static final String R_STATUS="r_status_text";//转发微博的内容
        //public static final String R_STATUS_THUMB="r_status_thumb";//转发微博的内容

        public static final String R_PIC_THUMB="r_pic_thumbnail"; //微博的小图
        public static final String R_PIC_MID="r_pic_middle";
        public static final String R_PIC_ORIG="r_pic_original";

        public static final String R_NUM="r_num";//转发数
        public static final String C_NUM="c_num";//评论数

        public static final String PIC_URLS="pic_urls"; //缩略图,可能有多个,用,号隔开.

        //类型
        public static final int ITEM=13;
        public static final int ITEM_ID=14;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/ss");

        //MIMETYPE
        public static final String STATUS_TYPE="vnd.android.cursor.dir/ss";
        public static final String STATUS_ITEM_TYPE="vnd.android.cursor.item/ss";

        public static final String SSTATUS_ID_INDEX="sstatus_id_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +STATUS_ID+" integer,"
            +USER_ID+" integer,"
            +USER_SCREEN_NAME+" text,"
            +PORTRAIT+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +TYPE+" integer,"
            +PIC_THUMB+" text,"
            +PIC_MID+" text,"
            +PIC_ORIG+" text,"
            +R_PIC_THUMB+" text,"
            +R_PIC_MID+" text,"
            +R_PIC_ORIG+" text,"
            +R_STATUS_NAME+" text,"
            +R_STATUS+" text,"
            +R_NUM+" integer,"
            +C_NUM+" integer);";

        public static final String IDX1="CREATE INDEX "+SSTATUS_ID_INDEX+" ON "+
            TBNAME+" ("+_ID+","+STATUS_ID+");";
    }

    /**
     * 微博用户表,现在开始再启用，存储着主页的用户，用于＠，也可以存储其它的用户
     * 目前只存储主页的用户。
     * 增加存储的类型，添加了最近查看的用户，主要用于@，可以按拼音搜索。
     */
    public static class UserTbl implements BaseColumns {

        public static final String TBNAME="twitter_user";
        public static final String USER_ID="user_id";
        public static final String USER_SCREEN_NAME="screen_name";

        public static final String UID="uid"; //用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询

        public static final String PINYIN="pinyin"; //用户名的拼音
        public static final String TYPE="type"; //用户名的类型，

        //类型
        public static final int ITEM=3;
        public static final int ITEM_ID=4;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/user");

        public static final String USER_TYPE="vnd.android.cursor.dir/user";
        public static final String USER_ITEM_TYPE="vnd.android.cursor.item/user";

        public static final int TYPE_FRIEND=0; //用户名的类型,关注对象
        public static final int TYPE_RECENT_AT=1; //用户名的类型,最近@r的对象

        public static final String USER_SCREEN_NAME_INDEX="user_id_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +USER_ID+" integer,"
            +USER_SCREEN_NAME+" text);";

        public static final String IDX1="CREATE INDEX "+USER_SCREEN_NAME_INDEX+" ON "+
            TBNAME+" ("+USER_SCREEN_NAME+","+PINYIN+");";
    }

    /**
     * 微博登录用户表
     */
    public static class AUTbl implements BaseColumns {

        public static final String ACCOUNT_TBNAME="twitter_au";
        public static final String ACCOUNT_NAME="au_name";  //用户名
        public static final String ACCOUNT_PASS="au_pass";  //密码
        public static final String ACCOUNT_TOKEN="au_token";    //授权的token
        public static final String ACCOUNT_SECRET="au_secret";  //secret
        public static final String ACCOUNT_TYPE="au_type";//api的实现类型
        /**
         * 默认帐户状态:0表示是默认帐户,-1或其它值表示非默认帐户.在version=10以前是没有值的，所以在更新数据库时，需要注意。
         */
        public static final String ACCOUNT_AS_DEFAULT="au_default";  //默认帐户状态:1表示是默认帐户,-1表示非默认帐户.
        public static final String ACCOUNT_USERID="a_userid";//用户id,登录后的
        public static final String ACCOUNT_TIME="au_time";  //过期时间
        public static final String ACCOUNT_OAUTH_TYPE="oauth_type";//网页认证还是密码认证.目前可实现两个认证.
        public static final String ACCOUNT_CUSTOM_KEY="custom_key";//自定义的key
        public static final String ACCOUNT_CUSTOM_SECRET="custom_secret";//密钥

        //常量
        public static final int WEIBO_SINA=0;   //新浪的号

        public static final int WEIBO_SINA_DESK=10; //新浪的号

        public static final int ACCOUNT_IS_DEFAULT=1;
        public static final int ACCOUNT_IS_NOT_DEFAULT=-1;
        public static final int ACCOUNT_OAUTH_WEB=0;//网页认证
        public static final int ACCOUNT_OAUTH_PASS=1;//密码认证

        //类型
        public static final int ITEM=7;
        public static final int ITEM_ID=8;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/au");

        public static final String USER_TYPE="vnd.android.cursor.dir/au";
        public static final String USER_ITEM_TYPE="vnd.android.cursor.item/au";

        public static final String CREATE_ACCOUNT="create table "+ACCOUNT_TBNAME+" ("
            +_ID+" integer primary key autoincrement,"
            +ACCOUNT_NAME+" text,"
            +ACCOUNT_PASS+" text,"
            +ACCOUNT_TOKEN+" text,"
            +ACCOUNT_SECRET+" text,"
            +ACCOUNT_TYPE+" integer,"
            +ACCOUNT_AS_DEFAULT+" integer,"
            +ACCOUNT_USERID+" text);";
    }

    /**
     * 草稿。
     */
    public static class DraftTbl implements BaseColumns {

        public static final String TBNAME="draft";

        public static final String USER_ID="user_id"; //微博草稿对应的的发布者id，多帐号时有用
        public static final String CONTENT="content"; //内容
        public static final String IMG_URL="img_url";//发布的图片，存储着本的的路径
        public static final String CREATED_AT="created_at";//创建时间
        public static final String TEXT="text";//微博的内容
        public static final String SOURCE="source";//微博发布的来源,如果是评论，需要存储原微博的id
        public static final String DATA="data";//备用
        public static final String UID="uid"; //当前登录用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询

        public static final String TYPE="status_type"; //存储着微博的类型，暂时只存储发布的微博，以后可能存储转发，评论。

        //常量
        public static final int STATUS_TYPE=0; //发布的微博
        public static final int REPOST_STATUS_TYPE=1; //转发的微博
        public static final int COMMENT_TYPE=2; //评论

        //类型
        public static final int ITEM=11;
        public static final int ITEM_ID=12;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/dt");

        //MIMETYPE
        public static final String DRAFT_TYPE="vnd.android.cursor.dir/dt";
        public static final String DRAFT_ITEM_TYPE="vnd.android.cursor.item/dt";

        public static final String DRAFT_ID_INDEX="draft_id_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +USER_ID+" integer,"
            +CONTENT+" text,"
            +IMG_URL+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +TYPE+" integer,"
            +DATA+" text);";

        public static final String IDX1="CREATE INDEX "+DRAFT_ID_INDEX+" ON "+
            TBNAME+" ("+_ID+","+USER_ID+");";
    }

    /**
     * 发送队列表,包含微博，评论，转发，评论回复（暂时不处理）
     */
    public static class SendQueueTbl implements BaseColumns {

        public static final String TBNAME="send_queue";

        public static final String USER_ID="user_id"; //微博草稿对应的的发布者id，多帐号时有用
        public static final String CONTENT="content"; //内容
        public static final String IMG_URL="img_url";//发布的图片，存储着本的的路径
        public static final String CREATED_AT="created_at";//创建时间
        public static final String TEXT="text";//微博的内容
        public static final String SOURCE="source";//微博发布的来源,如果是评论，需要存储原微博的id
        public static final String DATA="data";//现在作为发送的结果错误
        //public static final String UID="uid"; //当前登录用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询
        //TODO 下一次更新数据库需要注意这个值。
        public static final String SEND_RESULT_MSG="send_result_msg";//发送结果。
        public static final String SEND_RESULT_CODE="send_result_code";   //发送的结果代码。

        public static final String TYPE="send_type"; //存储着发布队列的类型，包含微博，评论，转发，评论回复（暂时不处理）

        //常量
        public static final int SEND_TYPE_STATUS=0; //发布的微博
        public static final int SEND_TYPE_REPOST_STATUS=1; //转发的微博
        public static final int SEND_TYPE_COMMENT=2; //发布的评论
        public static final int SEND_TYPE_ADD_FAV=3; //添加收藏

        //类型
        public static final int ITEM=15;
        public static final int ITEM_ID=16;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/sq");

        //MIMETYPE
        public static final String SEND_QUEUE_TYPE="vnd.android.cursor.dir/sq";
        public static final String SEND_QUEUE_ITEM_TYPE="vnd.android.cursor.item/sq";

        public static final String SEND_QUEUE_ID_INDEX="send_queue_id_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +USER_ID+" integer,"
            +CONTENT+" text,"
            +IMG_URL+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +TYPE+" integer,"
            +DATA+" text);";

        public static final String IDX1="CREATE INDEX "+SEND_QUEUE_ID_INDEX+" ON "+
            TBNAME+" ("+_ID+","+USER_ID+");";
    }

    /**
     * 私信表
     */
    public static class DirectMsgTbl implements BaseColumns {

        public static final String TBNAME="direct_msg";

        public static final String DM_ID="dm_id"; //私信ID
        public static final String IDSTR="idstr"; //idstr
        public static final String RECIPIENT_ID="recipient_id"; //接受人UID
        public static final String RECIPIENT_SCREENNAME="recipient_screenname";//接受人昵称
        public static final String RECIPIENT_PROFILE_URL="recipient_profile_url";//接受人头像
        public static final String CREATED_AT="created_at";//创建时间
        public static final String SENDER_ID="sender_id";//发送人UID
        public static final String SENDER_SCREENNAME="sender_screenname";//发发送人昵称
        public static final String SENDER_PROFILE_URL="sender_profile_url";//发发送人头像
        public static final String TEXT="text";//私信内容
        public static final String SOURCE="source";//微博发布的来源
        public static final String DATA="data";//备用
        public static final String UID="uid"; //当前登录用户id，因为私信有发送与接收者的id,所以这个值是在查询私信,不分接收与发送者时用

        //类型
        public static final int ITEM=17;
        public static final int ITEM_ID=18;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/dm");

        //MIMETYPE
        public static final String DIRECT_MSG_TYPE="vnd.android.cursor.dir/dm";
        public static final String DIRECT_MSG_ITEM_TYPE="vnd.android.cursor.item/dm";

        public static final String DIRECT_MSG_ID_INDEX="direct_msg_id_index";
        public static final String DIRECT_MSG_TEXT_INDEX="direct_msg_text_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +DM_ID+" integer,"
            +IDSTR+" text,"
            +RECIPIENT_ID+" integer,"
            +RECIPIENT_SCREENNAME+" text,"
            +RECIPIENT_PROFILE_URL+" text,"
            +SENDER_ID+" integer,"
            +SENDER_SCREENNAME+" text,"
            +SENDER_PROFILE_URL+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +UID+" integer,"
            +DATA+" text);";

        public static final String IDX1="CREATE INDEX "+DIRECT_MSG_ID_INDEX+" ON "+
            TBNAME+" ("+RECIPIENT_ID+","+SENDER_ID+");";

        public static final String IDX2="CREATE INDEX "+DIRECT_MSG_TEXT_INDEX+" ON "+
            TBNAME+" ("+TEXT+");";
    }

    /**
     * 存储@与评论数据,因为使用了文件存储,在获取数据时,顺序是个问题,干脆用数据库来存储.千条数据暂时也不是个问题.
     */
    public static class SStatusCommentTbl implements BaseColumns {

        public static final String TBNAME="status_or_comment";

        public static final String STATUS_ID="status_id"; //对应Status的id,如果是评论,就是评论的id
        public static final String USER_ID="user_id"; //微博的发布者id,从Status的user属性中取得,评论者id
        public static final String USER_SCREEN_NAME="screen_name"; //来源同上
        public static final String PORTRAIT="portrait";//用户的头像,来源同上
        public static final String CREATED_AT="created_at";//发布时间
        public static final String TEXT="text";//微博的内容或评论的内容
        public static final String SOURCE="source";//微博发布的来源或评论的来源

        public static final String TYPE="status_type"; //存储着微博的类型,这里的类型有微博,评论.
        public static final String UID="uid"; //用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询

        //如果评论有原微博,这些就存着原微博的图片地址.
        public static final String PIC_THUMB="pic_thumbnail"; //微博的小图
        public static final String PIC_MID="pic_middle";
        public static final String PIC_ORIG="pic_original";

        public static final String R_STATUS_ID="r_status_id";//转发微博的id,如果是评论,就是存储原微博的id
        public static final String R_STATUS_USERID="r_status_userid";//转发微博的作者id,原微博的id
        public static final String R_STATUS_NAME="r_status_name";//转发微博的作者,评论原微博的用户名,如果有
        public static final String R_STATUS="r_status_text";//转发微博的内容,评论的原微博的内容,

        //对于微博,有下面数据,对于评论则没有.但是评论有对于评论的.
        public static final String R_PIC_THUMB="r_pic_thumbnail"; //微博的小图,如果是评论,对应原来的评论
        public static final String R_PIC_MID="r_pic_middle";    //如果是评论,对应原来的评论的作者id
        public static final String R_PIC_ORIG="r_pic_original"; //如果是评论,对应原来的评论作者名字

        public static final String R_NUM="r_num";//转发数
        public static final String C_NUM="c_num";//评论数

        public static final String PIC_URLS="pic_urls"; //缩略图,可能有多个,用,号隔开.

        //类型
        public static final int ITEM=19;
        public static final int ITEM_ID=20;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/ssc");

        //MIMETYPE
        public static final String STATUS_COMMENT_TYPE="vnd.android.cursor.dir/ssc";
        public static final String STATUS_COMMENT_ITEM_TYPE="vnd.android.cursor.item/ssc";

        public static final int TYPE_STATUT=0;  //存储着微博
        public static final int TYPE_COMMENT=1; //存储着非@评论
        public static final int TYPE_AT_COMMENT=2; //存储着@的评论

        public static final String SSTATUS_COMMENT_ID_INDEX="sstatus_comment_id_index";
        public static final String SSTATUS_COMMENT_UID_TYPE_INDEX="sstatus_comment_uid_type_index";

        public static final String CREATE_TABLE="create table "+TBNAME+" ("
            +_ID+" integer primary key,"
            +STATUS_ID+" integer,"
            +USER_ID+" integer,"
            +USER_SCREEN_NAME+" text,"
            +PORTRAIT+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +TYPE+" integer,"
            +UID+" integer,"
            +PIC_THUMB+" text,"
            +PIC_MID+" text,"
            +PIC_ORIG+" text,"
            +R_STATUS_ID+" integer,"
            +R_STATUS_USERID+" integer,"
            +R_STATUS_NAME+" text,"
            +R_STATUS+" text,"
            +R_PIC_THUMB+" text,"
            +R_PIC_MID+" text,"
            +R_PIC_ORIG+" text,"
            +R_NUM+" integer,"
            +C_NUM+" integer);";

        public static final String IDX1="CREATE INDEX "+SSTATUS_COMMENT_ID_INDEX+" ON "+
            TBNAME+" ("+_ID+","+UID+");";

        public static final String IDX2="CREATE INDEX "+SSTATUS_COMMENT_UID_TYPE_INDEX+" ON "+
            TBNAME+" ("+UID+","+TYPE+");";
    }

    //--------------------- 其它微博 ---------------------

    /**
     * 帐户表,用于帐户管理
     */
    @Deprecated
    public static class AccountTbl implements BaseColumns {

        public static final String ACCOUNT_TBNAME="twitter_account";
        public static final String ACCOUNT_NAME="account_name";
        public static final String ACCOUNT_TOKEN="account_token";
        public static final String ACCOUNT_SECRET="account_secret";
        public static final String ACCOUNT_STATUS="account_status";//帐户状态:0表示正常,-1表示错误.
        public static final String ACCOUNT_S="account_s";//默认帐户状态:0表示是默认帐户,-1表示非默认帐户.
        public static final String ACCOUNT_USERID="account_userid";//用户id,登录后的

        //类型
        public static final int ITEM=5;
        public static final int ITEM_ID=6;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/ac");

        public static final String USER_TYPE="vnd.android.cursor.dir/account";
        public static final String USER_ITEM_TYPE="vnd.android.cursor.item/account";

        public static final String CREATE_ACCOUNT="create table "+ACCOUNT_TBNAME+" ("
            +_ID+" integer primary key autoincrement,"
            +ACCOUNT_NAME+" text,"
            +ACCOUNT_TOKEN+" text,"
            +ACCOUNT_SECRET+" text,"
            +ACCOUNT_STATUS+" integer,"
            +ACCOUNT_S+" integer,"
            +ACCOUNT_USERID+" text);";
    }

    /**
     * 腾讯微博内容表,在离线时可以查看内容,所以只保存必要的信息,并不是把整个Status的内容保存,
     * 只保存主页的30条信息,刷新时需要更新这里的数据,加载更多时不修改.
     * 暂时不保存，先保存到文件。
     */
    @Deprecated
    public static class QStatusTbl implements BaseColumns {

        public static final String QSTATUS_TBNAME="qhome_status";

        public static final String STATUS_ID="status_id"; //对应Status的id
        public static final String NICK_NAME="nick_name"; //昵称
        public static final String NAME="name"; //名字
        public static final String PORTRAIT="portrait";//用户的头像,来源同上
        public static final String CREATED_AT="created_at";//微博的发布时间
        public static final String TEXT="text";//微博的内容
        public static final String SOURCE="source";//微博发布的来源
        public static final String FROME_URL="from_url";//来源的url
        public static final String OPENID="openid";
        public static final String STATUS="status";//状态

        public static final String TYPE="status_type"; //存储着微博的类型
        public static final String IMAGE="image"; //微博图片，可能是多个

        public static final String R_NICK_NAME="r_nick_name";//转发微博的作者
        public static final String R_NAME="r_name";//转发微博的作者
        public static final String R_STATUS_TEXT="r_status_text";//转发微博的内容
        public static final String R_HEAD="r_head";//转发微博的用户头像
        public static final String R_ID="r_id";//转发微博的id
        public static final String R_OPENID="r_openid";//转发微博的id
        public static final String R_TYPE="r_type";//转发微博的类型，1-原创发表，2-转载，3-私信，4-回复，5-空回，6-提及，7-评论

        public static final String R_IMAGE="r_image"; //微博的图片

        public static final String R_NUM="r_num";//转发数
        public static final String C_NUM="c_num";//评论数

        //类型
        public static final int ITEM=9;
        public static final int ITEM_ID=10;
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/item");

        //MIMETYPE
        public static final String QSTATUS_TYPE="vnd.android.cursor.dir/qstatus";
        public static final String QSTATUS_ITEM_TYPE="vnd.android.cursor.item/qstatus";

        public static final String QSTATUS_ID_INDEX="qstatus_id_index";

        public static final String CREATE_STATUS="create table "+QSTATUS_TBNAME+" ("
            +STATUS_ID+" integer primary key,"
            +NICK_NAME+" text,"
            +NAME+" text,"
            +PORTRAIT+" text,"
            +CREATED_AT+" integer,"
            +TEXT+" text,"
            +SOURCE+" text,"
            +FROME_URL+" text,"
            +OPENID+" text,"
            +STATUS+" integer,"
            +TYPE+" integer,"
            +IMAGE+" text,"
            +R_NICK_NAME+" text,"
            +R_NAME+" text,"
            +R_STATUS_TEXT+" text,"
            +R_HEAD+" text,"
            +R_ID+" text,"
            +R_OPENID+" text,"
            +R_TYPE+" integer,"
            +R_IMAGE+" text,"
            +R_NUM+" integer,"
            +C_NUM+" integer);";

        public static final String IDX1="CREATE INDEX "+QSTATUS_ID_INDEX+" ON "+
            QSTATUS_TBNAME+" ("+STATUS_ID+");";
    }
}
