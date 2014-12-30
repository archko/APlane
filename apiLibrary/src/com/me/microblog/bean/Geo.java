package com.me.microblog.bean;

import java.io.Serializable;

/**
 * 位置服务中的微博内容geo
 * User: archko Date: 12-8-13 Time: 下午2:33
 */
public class Geo implements Serializable {

    public static final long serialVersionUID = 3894560643019408226L;

    public String type;//"point",
    public String coordinates;// [0.0, 0.0]
}
