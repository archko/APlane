package com.me.microblog.bean;

import java.io.Serializable;

public class Unread implements Serializable {

    /**
     * 新评论
     */
    public int comments;
    /**
     * 新私信，未授权
     */
    public int dm;
    /**
     * 新粉丝
     */
    public int followers;
    @Deprecated
    public int mentions;
    @Deprecated
    public int newStatus = 0;
    /**
     * 新微博数
     */
    public int status;
    /**
     * 新提及我的微博数
     */
    public int mention_status;
    /**
     * 新提及我的评论数
     */
    public int mention_cmt;
    /**
     * 微群消息未读数
     */
    public int group;
    /**
     * 私有微群消息未读数
     */
    public int private_group;
    /**
     * 新通知未读数
     */
    public int notice;
    /**
     * 新邀请未读数
     */
    public int invite;
    /**
     * 新勋章数
     */
    public int badge;
    /**
     * 相册消息未读数
     */
    public int photo;

    public Unread() {
    }

    @Override
    public String toString() {
        return "Unread{" +
            "comments=" + comments +
            ", dm=" + dm +
            ", followers=" + followers +
            ", status=" + status +
            ", mention_status=" + mention_status +
            ", mention_cmt=" + mention_cmt +
            ", group=" + group +
            ", private_group=" + private_group +
            ", notice=" + notice +
            ", invite=" + invite +
            ", badge=" + badge +
            ", photo=" + photo +
            '}';
    }
}
