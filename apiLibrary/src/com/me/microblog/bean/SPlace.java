package com.me.microblog.bean;

import java.io.Serializable;

/**
 * User: archko Date: 12-8-13 Time: 下午2:36
 */
public class SPlace implements Serializable {

    public static final long serialVersionUID=3894560643019408212L;

    public String poiid;//"123456", //位置微博，可能没有这个。但它有url，geo信息是url中的
    public String title;//"Wall Street",
    public String type;// "tip",如果是place表示是位置微博，如果是checkin，表示是签到
    public String source;// "微领地"
    public int ppublic;//签到才有，更新
    public double lat;//签到才有
    public double lon;//签到才有
    public String placeUrl; //位置微博才有，包含lng,lat两个坐标信息

    @Override
    public String toString() {
        return "SPlace{"+
            "poiid='"+poiid+'\''+
            ", title='"+title+'\''+
            ", type='"+type+'\''+
            ", source='"+source+'\''+
            ", ppublic="+ppublic+
            ", lat="+lat+
            ", lon="+lon+
            ", placeUrl='"+placeUrl+'\''+
            '}';
    }
}
