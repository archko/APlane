package com.me.microblog.bean;

/**
 * @author archko
 */
public class QVideo {

    public static final long serialVersionUID=3894560643019408217L;

    /**
     * 缩略图
     */
    public String picurl;
    /**
     * 播放器地址
     */
    public String player;
    /**
     * 视频原地址
     */
    public String realurl;
    /**
     * 视频的短url
     */
    public String shorturl;
    /**
     * 视频标题
     */
    public String title;

    @Override
    public String toString() {
        return "QVideo{"+
            "picurl='"+picurl+'\''+
            ", player='"+player+'\''+
            ", realurl='"+realurl+'\''+
            ", shorturl='"+shorturl+'\''+
            ", title='"+title+'\''+
            '}';
    }
}
