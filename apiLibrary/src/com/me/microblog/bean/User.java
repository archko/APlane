package com.me.microblog.bean;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    public static final long serialVersionUID=3894560643019408206L;

    public boolean allowAllActMsg;
    /**
     * 城市编码（参考城市编码表）
     */
    public String city;
    /**
     * 创建时间
     */
    public Date createdAt;
    /**
     * 个人描述
     */
    public String description;
    /**
     * 用户个性化URL
     */
    public String domain;
    /**
     * 收藏数
     */
    public int favouritesCount;
    /**
     * 粉丝数
     */
    public int followersCount;
    /**
     * 是否已关注
     */
    public boolean following;
    /**
     * 关注数
     */
    public int friendsCount;
    /**
     * 性别,m--男，f--女,n--未知
     */
    public String gender;
    @Deprecated
    public boolean geoEnabled;
    /**
     * 用户UID
     */
    public long id;
    /**
     * 地址
     */
    public String location;
    /**
     * 友好显示名称，如Bill Gates(此特性暂不支持)
     */
    public String name;
    /**
     * 自定义图像
     */
    public String profileImageUrl;
    /**
     * 省份编码（参考省份编码表）
     */
    public String province;
    /**
     * 微博昵称
     */
    public String screenName;
    public Status status;
    /**
     * 微博数
     */
    public int statusesCount;
    public String token;
    public String tokenSecret;
    /**
     * 用户博客地址
     */
    public String url;
    /**
     * 加V标示，是否微博认证用户
     */
    public boolean verified;
    /**
     * 用户大头像地址
     */
    public String avatar_large;
    /**
     * 用户头像地址（高清），高清头像原图
     */
    public String avatar_hd;
    public String verified_reason;//认证原因
    public boolean follow_me;//该用户是否关注当前登录用户
    public int online_status;//用户的在线状态，0：不在线、1：在线
    public int bi_followers_count;//用户的互粉数    status 	object 	用户的最近一条微博信息字段

    public String weihao;
    public String remark;
    public boolean allowAllComment;
    public String lang;
    public int level;
    public int type;
    public int ulevel;
    public int mlevel;

    public User() {
    }

    public User(String screenName) {
        this.screenName=screenName;
    }

    @Override
    public String toString() {
        return "User{"+
            "city='"+city+'\''+
            ", favouritesCount="+favouritesCount+
            ", followersCount="+followersCount+
            ", following="+following+
            ", friendsCount="+friendsCount+
            ", gender='"+gender+'\''+
            ", geoEnabled="+geoEnabled+
            ", location='"+location+'\''+
            ", name='"+name+'\''+
            ", profileImageUrl='"+profileImageUrl+'\''+
            ", province='"+province+'\''+
            ", screenName='"+screenName+'\''+
            ", avatar_large='"+avatar_large+'\''+
            ", avatar_hd='"+avatar_hd+'\''+
            ", status="+status+
            ", statusesCount="+statusesCount+
            ", url='"+url+'\''+
            ", id="+id+
            '}';
    }
}
