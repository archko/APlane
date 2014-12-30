package com.me.microblog.bean;

import java.io.Serializable;

/**
 * 收藏结果，对于 oauth1已经变化了，多了tag,favorited_time
 * User: archko Date: 12-7-26 Time: 上午11:23
 */
public class Favorite implements Serializable {

    public static final long serialVersionUID = 3894560643019408227L;

    public Status mStatus;
    public Tags tags;
    public String favorited_time;

    @Override
    public String toString() {
        return "Favorite{" +
            "mStatus=" + mStatus +
            ", tags=" + tags +
            ", favorited_time='" + favorited_time + '\'' +
            '}';
    }
}
