package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Date;

public class DirectMessage implements Serializable {

    public static final long serialVersionUID=3894560643019408229L;

    /**
     * 发送时间
     */
    public Date createdAt;
    /**
     * 私信ID
     */
    public long id;
    /**
     * idstr
     */
    public String idstr;
    /**
     * mid
     */
    public String mid;
    /**
     * 接受人信息，参考user说明
     */
    public User recipient;
    /**
     * 接受人UID
     */
    public long recipientId;
    /**
     * 接受人昵称
     */
    public String recipientScreenName;
    /**
     * 发送人信息，参考user说明
     */
    public User sender;
    /**
     * 发送人UID
     */
    public long senderId;
    /**
     * 发送人昵称
     */
    public String senderScreenName;
    /**
     * 私信内容
     */
    public String text;
    /**
     * source
     */
    public String source;

    // database column
    public long _id;
    public String data;
    public long uid;

    public DirectMessage() {
    }

    public DirectMessage(Date createdAt, long id, String idstr, User recipient, long recipientId,
        String recipientScreenName, User sender, long senderId, String senderScreenName,
        String text, String source, long _id, String data, long uid) {
        this.createdAt=createdAt;
        this.id=id;
        this.idstr=idstr;
        this.recipient=recipient;
        this.recipientId=recipientId;
        this.recipientScreenName=recipientScreenName;
        this.sender=sender;
        this.senderId=senderId;
        this.senderScreenName=senderScreenName;
        this.text=text;
        this.source=source;
        this._id=_id;
        this.data=data;
        this.uid=uid;
    }

    @Override
    public String toString() {
        return "DirectMessage{"+
            "createdAt="+createdAt+
            ", id="+id+
            ", idstr='"+idstr+'\''+
            ", mid='"+mid+'\''+
            ", recipientId="+recipientId+
            ", recipientScreenName='"+recipientScreenName+'\''+
            ", senderId="+senderId+
            ", senderScreenName='"+senderScreenName+'\''+
            ", text='"+text+'\''+
            ", source='"+source+'\''+
            '}';
    }
}
