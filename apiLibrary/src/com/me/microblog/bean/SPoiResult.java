package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-21
 */
public class SPoiResult implements Serializable {

    public static final long serialVersionUID=3894560643019408211L;

    /**
     * 结果总数
     */
    public int total;
    /**
     * 本页返回的结果数
     */
    public int count;
    /**
     * 返回的首条结果在结果集中的页码
     */
    public int page;

    public SPoi[] sPois;
}
