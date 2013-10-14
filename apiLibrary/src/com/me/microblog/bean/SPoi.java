package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-21
 */
public class SPoi implements Serializable {

    public static final long serialVersionUID=3894560643019408211L;

    /**
     * 本条poi记录的spid
     */
    public String spid;
    /**
     * poi点名称
     */
    public String name;
    /**
     * poi点的地址
     */
    public String address;
    /**
     * poi点的分类（详见分类代码表）
     */
    public String category;
    /**
     * poi点的分类名称
     */
    public String navigator;
    /**
     * poi点的电话
     */
    public String telephone;
    /**
     * poi点的图片地址
     */
    public String pic_url;
    /**
     * poi点的经度
     */
    public long longitude;
    /**
     * poi点的纬度
     */
    public long latitude;
    /**
     * 城市
     */
    public String city;
    public String province;
}
