package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {

    public static final long serialVersionUID=3894560643019408230L;

    /**
     * 评论时间
     */
    public Date createdAt;
    /**
     * 评论ID
     */
    public long id;
    /**
     * 评论来源，数据结构跟comment一致
     */
    public Comment replyComment;
    /**
     * 评论来源
     */
    public String source;
    /**
     * 评论的微博,结构参考status
     */
    public Status status;
    /**
     * 评论内容
     */
    public String text;
    /**
     * 评论人信息,结构参考user
     */
    public User user;
    /**
     * 是否收藏
     */
    public boolean favorited;
    /**
     * 是否被截断
     */
    public boolean truncated;

    public Comment() {
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null||getClass()!=o.getClass()) return false;

        Comment comment=(Comment) o;

        if (id!=comment.id) return false;
        if (replyComment!=null ? !replyComment.equals(comment.replyComment) : comment.replyComment!=null) return false;
        if (text!=null ? !text.equals(comment.text) : comment.text!=null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result=(int) (id^(id>>>32));
        result=31*result+(replyComment!=null ? replyComment.hashCode() : 0);
        result=31*result+(text!=null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Comment{"+"createdAt="+createdAt+", id="+id+", replyComment="+replyComment+", source="+source+", status="+status+", text="+text+", user="+user+", favorited="+favorited+", truncated="+truncated+'}';
    }

}
