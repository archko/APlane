package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Date;

public class QStatus implements Serializable {

    public static final long serialVersionUID=3894560643019408219L;

    /**
     * 微博内容
     */
    public String text;
    /**
     * 原始内容
     */
    public String origtext;
    /**
     * 微博被转次数
     */
    public int count;
    /**
     * :点评次数
     */
    public int mcount;
    /**
     * 来源
     */
    public String from;
    /**
     * 来源url
     */
    public String fromurl;
    /**
     * 微博id，唯一
     */
    public String id;
    /**
     * 图片url列表
     */
    public String image;
    /**
     * 视频，可能为空
     */
    public QVideo qVideo;
    /**
     * 音频
     */
    public QMusic qMusic;
    /**
     * 发表人帐户名
     */
    public String name;
    /**
     * 用户唯一id，与name相对应
     */
    public String openId;
    /**
     * 发表人昵称
     */
    public String nick;
    //---------------------
    /**
     * 是否自己发表的，0不是，1是
     */
    public int self;
    /**
     * 发表时间
     */
    public long timestamp;
    /**
     * 微博类型，1-原创发表，2-转载，3-私信，4-回复，5-空回，6-提及，7-评论
     */
    public int type;
    /**
     * 发表者头像url
     */
    public String head;
    /**
     * 发表者所在地
     */
    public String location;
    /**
     * 国家码（其他时间线一样）
     */
    public String country_code;
    /**
     * 省份码（其他时间线一样）
     */
    public String province_code;
    /**
     * 城市码（其他时间线一样）
     */
    public String city_code;
    /**
     * 是否微博认证用户，0-不是，1-是
     */
    public int isvip;
    /**
     * 发表者地理信息
     */
    public String geo;
    /**
     * 微博状态，0-正常，1-系统删除，2-审核中，3-用户删除，4-根删除（根节点被系统审核删除）
     */
    public int status;
    /**
     * emotionurl:心情图片url
     */
    public String emotionurl;
    /**
     * emotiontype:心情类型
     */
    public int emotiontype;
    /**
     * 当type=2时，source即为源tweet
     *
     * 转发微博内容
     */
    public QStatus retweetedStatus;
    //-------- 文档里没有说明，但返回数据里有的 ------
    public long storetime;
    public long latitude;
    public long longitude;
    public int isrealname;

    @Override
    public String toString() {
        return "QStatus{"+
            "text='"+text+'\''+
            ", origtext='"+origtext+'\''+
            ", count="+count+
            ", mcount="+mcount+
            ", from='"+from+'\''+
            ", fromurl='"+fromurl+'\''+
            ", id='"+id+'\''+
            ", image='"+image+'\''+
            ", qVideo="+qVideo+
            ", qMusic="+qMusic+
            ", name='"+name+'\''+
            ", openId='"+openId+'\''+
            ", nick='"+nick+'\''+
            ", self="+self+
            ", timestamp="+timestamp+
            ", type="+type+
            ", head='"+head+'\''+
            ", location='"+location+'\''+
            ", country_code='"+country_code+'\''+
            ", province_code='"+province_code+'\''+
            ", city_code='"+city_code+'\''+
            ", isvip="+isvip+
            ", geo='"+geo+'\''+
            ", status="+status+
            ", emotionurl='"+emotionurl+'\''+
            ", emotiontype="+emotiontype+
            ", retweetedStatus="+retweetedStatus+
            ", storetime="+storetime+
            ", latitude="+latitude+
            ", longitude="+longitude+
            ", isrealname="+isrealname+
            '}';
    }
}
