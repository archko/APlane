package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Status implements Serializable {

    public static final long serialVersionUID=3894560643019408205L;

    public long _id;
    /**
     * 中型图片
     */
    public String bmiddlePic;
    /**
     * 创建时间
     */
    public Date createdAt;
    /**
     * 是否已收藏(正在开发中，暂不支持)
     */
    public boolean favorited;
    /**
     * 微博ID
     */
    public long id;
    /**
     * 回复人昵称
     */
    public String inReplyToScreenName;
    /**
     * 回复ID
     */
    public String inReplyToStatusId;
    /**
     * 回复人UID
     */
    public String inReplyToUserId;
    /**
     * 原始图片
     */
    public String originalPic;
    /**
     * 微博来源
     */
    public String source;
    /**
     * 微博信息内容
     */
    public String text;
    /**
     * 缩略图
     */
    public String thumbnailPic;
    /**
     * 是否被截断
     */
    public boolean truncated;
    /**
     * 作者信息
     */
    public User user;
    /**
     * 转发的博文，内容为status，如果不是转发，则没有此字段
     */
    public Status retweetedStatus;
    /**
     * 多图的数组
     */
    public String[] thumbs;

    //------------
    public int r_num;//转发数
    public int c_num;//评论数
    //-----------------------
    /**
     * 微博附加注释信息
     */
    public SAnnotation annotations;
    /**
     * 微博MID
     */
    public String mid;
    //geo object 	地理信息字段
    public Geo geo;

    public int distance;    //位置服务中的距离.
    /**
     * 表态数
     */
    public int attitudes_count;

    public AKSpannableStringBuilder mStatusSpannable;
    public AKSpannableStringBuilder mRetweetedSpannable;

    public Status() {
    }

    public Status(long _id, long id, Date createdAt, String text, String source, String thumbnailPic, String bmiddlePic,
        String originalPic, Status retweetedStatus, User user, int r_num, int c_num) {
        this._id=_id;
        this.bmiddlePic=bmiddlePic;
        this.retweetedStatus=retweetedStatus;
        this.user=user;
        this.id=id;
        this.createdAt=createdAt;
        this.originalPic=originalPic;
        this.source=source;
        this.text=text;
        this.thumbnailPic=thumbnailPic;
        this.r_num=r_num;
        this.c_num=c_num;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null||getClass()!=o.getClass()) return false;

        Status status=(Status) o;

        if (id!=status.id) return false;
        if (text!=null ? !text.equals(status.text) : status.text!=null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result=(int) (id^(id>>>32));
        result=31*result+(text!=null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Status{"+
            "bmiddlePic='"+bmiddlePic+'\''+
            ", createdAt="+createdAt+
            ", favorited="+favorited+
            ", inReplyToScreenName='"+inReplyToScreenName+'\''+
            ", inReplyToStatusId='"+inReplyToStatusId+'\''+
            ", inReplyToUserId='"+inReplyToUserId+'\''+
            ", originalPic='"+originalPic+'\''+
            ", source='"+source+'\''+
            ", text='"+text+'\''+
            ", thumbnailPic='"+thumbnailPic+'\''+
            ", user="+user+
            ", retweetedStatus="+retweetedStatus+
            ", thumbs="+Arrays.toString(thumbs)+
            ", r_num="+r_num+
            ", c_num="+c_num+
            ", mid='"+mid+'\''+
            ", geo="+geo+
            ", distance="+distance+
            ", attitudes_count="+attitudes_count+
            ", id="+id+
            ", annotations="+annotations+
            '}';
    }
}
