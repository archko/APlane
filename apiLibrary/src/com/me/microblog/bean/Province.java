package com.me.microblog.bean;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-7
 */
public class Province {

    public static final long serialVersionUID=3894560643019408220L;

    public String id;
    public String name;
    public ArrayList<City> cities;

    @Override
    public String toString() {
        return "Province{"+
            "id='"+id+'\''+
            ", name='"+name+'\''+
            ", cities="+cityToList(cities)+
            '}';
    }

    private String cityToList(ArrayList<City> cities) {
        if (null==cities) {
            return "";
        }

        StringBuilder sb=new StringBuilder();
        for (City city : cities) {
            sb.append(city);
        }

        return sb.toString();
    }
}
