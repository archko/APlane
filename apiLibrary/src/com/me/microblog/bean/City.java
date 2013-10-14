package com.me.microblog.bean;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-7
 */
public class City {

    public static final long serialVersionUID=3894560643019408231L;

    /**
     * 城市id
     */
    public String id;
    /**
     * 城市名字
     */
    public String name;
    /**
     * 城市名字的全拼，可能为空
     */
    public String pinyin;

    @Override
    public String toString() {
        return "City{"+
            "id='"+id+'\''+
            ", name='"+name+'\''+
            ", pinyin='"+pinyin+'\''+
            '}';
    }
}
