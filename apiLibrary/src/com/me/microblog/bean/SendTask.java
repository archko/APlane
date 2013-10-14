package com.me.microblog.bean;

import java.io.Serializable;

public class SendTask implements Serializable {

    public static final long serialVersionUID=3894560643019408213L;

    public long id;
    /**
     * 微博草稿对应的的发布者id，多帐号时有用
     */
    public long userId;
    /**
     * 内容
     */
    public String content;
    /**
     * 发布的图片，存储着本的的路径
     */
    public String imgUrl;
    /**
     * 创建时间
     */
    public long createAt;
    /**
     * 微博的内容,通常没有什么用处，因为内容是放在content中的，但对于评论与转发，就是原微博的部分内容了。
     */
    public String text;
    /**
     * 微博发布的来源,如果是评论，需要存储原微博的id
     */
    public String source;
    /**
     * 备用,在发微博时存储经纬度，latitude+"-"+longitude,
     * 评论时存储是否评论原微博。
     */
    public String data;
    /**
     * 当前登录用户id，不存储AUTbl主键，因为根据用户id更直接。主页需要根据这个值来查询
     */
    public long uid;
    /**
     * 存储着微博的类型，暂时只存储发布的微博，以后可能存储转发，评论。
     */
    public int type;
    /**
     * 返回的错误信息
     */
    public String resultMsg;
    /**
     * 返回的错误代码
     */
    public int resultCode;

    public SendTask() {
    }

    @Override
    public String toString() {
        return "SendTask{"+
            "id="+id+
            ", userId="+userId+
            ", content='"+content+'\''+
            ", imgUrl='"+imgUrl+'\''+
            ", createAt="+createAt+
            ", text='"+text+'\''+
            ", source='"+source+'\''+
            ", data='"+data+'\''+
            ", uid="+uid+
            ", type="+type+
            ", resultMsg='"+resultMsg+'\''+
            ", resultCode="+resultCode+
            '}';
    }
}
