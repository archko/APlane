package com.me.microblog.bean;

/**
 * User: archko Date: 12-12-6 Time: 下午7:24
 */
public class AtUser {

    public static final long serialVersionUID = 3894560643019408232L;

    /**
     * 数据库主键id
     */
    public long id;
    public long userId;
    public String name;
    public String pinyin;
    /**
     * 用户的类型，可能是主页关注的，可能是最近查看过的。
     */
    public int type;
    /**
     * 此用户关联当前登录的用户id
     */
    public long uid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtUser atUser = (AtUser) o;

        if (userId != atUser.userId) return false;
        if (name != null ? ! name.equals(atUser.name) : atUser.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AtUser{" +
            "id=" + id +
            "userId=" + userId +
            ", name='" + name + '\'' +
            ", pinyin='" + pinyin + '\'' +
            '}';
    }
}
