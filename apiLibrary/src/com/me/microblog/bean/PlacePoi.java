package com.me.microblog.bean;

import java.io.Serializable;

/**
 * 位置服务的poi与地理定位的poi不同
 * User: archko Date: 12-8-13 Time: 上午11:24
 */
public class PlacePoi implements Serializable {

    public static final long serialVersionUID=3894560643019408221L;

    public String poiid;// "B2094654D069A6F4419C",
    public String title;// "三个贵州人(中关村店)",
    public String address;// "北四环西路58号理想国际大厦202-205",
    public double lon;// "116.30999",
    public double lat;// "39.98435",
    public String category;// "83",
    public String city;// "0010",
    public String province;// null,
    public String country;// null,
    public String url;// "",
    public String phone;//"010-82607678",
    public String postcode;// "100000",
    public String weibo_id;//"0",
    public String categorys;// "64 69 83",
    public String category_name;// "云贵菜",
    public String icon;// "http://u1.sinaimg.cn/upload/2012/03/23/1/xysh.png",
    public int checkin_num;// 0,
    public int checkin_user_num;//"0",
    public int tip_num;// 0,
    public int photo_num;// 0,
    public int todo_num;// 0,
    public int distance;// 70,
    public String checkin_time;// "2011-10-23 00:02:14"

    @Override
    public String toString() {
        return "PlacePoi{"+
            "poiid='"+poiid+'\''+
            ", title='"+title+'\''+
            ", address='"+address+'\''+
            ", lon="+lon+
            ", lat="+lat+
            ", category='"+category+'\''+
            ", city='"+city+'\''+
            ", province='"+province+'\''+
            ", country='"+country+'\''+
            ", url='"+url+'\''+
            ", phone='"+phone+'\''+
            ", postcode='"+postcode+'\''+
            ", weibo_id='"+weibo_id+'\''+
            ", categorys='"+categorys+'\''+
            ", category_name='"+category_name+'\''+
            ", icon='"+icon+'\''+
            ", checkin_num="+checkin_num+
            ", checkin_user_num="+checkin_user_num+
            ", tip_num="+tip_num+
            ", photo_num="+photo_num+
            ", todo_num="+todo_num+
            ", distance="+distance+
            ", checkin_time='"+checkin_time+'\''+
            '}';
    }
}
