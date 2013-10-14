package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {

    public static final long serialVersionUID=3894560643019408225L;

    /**
     * 分组ID
     */
    public String id;
    /**
     *
     */
    public String idstr;
    /**
     * 组名
     */
    public String name;
    /**
     * 分组的模式：private
     */
    public String mode;
    /**
     * 是否可见
     */
    public int visible;
    /**
     *
     */
    public int like_count;
    /**
     * 成员数
     */
    public int member_count;
    /**
     * 描述
     */
    public String description;
    /**
     * 标签，备用，这是一个[]
     */
    public String tags;
    /**
     *
     */
    public String profile_image_url;
    /**
     * 创建时间
     */
    public String createdAt;
    /**
     * 作者信息
     */
    public User user;

    public Group() {
    }

    @Override
    public String toString() {
        /*return "Group{"+
            "id='"+id+'\''+
            ", idstr='"+idstr+'\''+
            ", name='"+name+'\''+
            ", mode='"+mode+'\''+
            ", visible="+visible+
            ", like_count="+like_count+
            ", member_count="+member_count+
            ", createdAt="+createdAt+
            '}';*/
        return name+" ("+member_count+")";    //由于在主页使用ArrayAdapter，所以这里只能返回字符串。
    }
}
