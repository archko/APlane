package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-21
 */
public class SPoiSearchResult implements Serializable {

    public static final long serialVersionUID=3894560643019408210L;

    /**
     * 未知
     */
    public int result;
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
    /**
     * center_poi包含一个poi 中心点的信息（用id和city查询时返回，用xy作为中心点参数时，不返回center_poi信息）
     */
    public SPoi centerPoi;
    /**
     * 结果poi集
     */
    public SPoi[] sPois;
}
