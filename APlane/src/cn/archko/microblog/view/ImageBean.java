package com.me.microblog.view;

/**
 * @description:
 * @author: archko 13-9-27 :上午9:22
 */
public class ImageBean {

    public String id;
    public String name;
    public String url;
    public String thumb;
    public String path;

    @Override
    public String toString() {
        return "ImageBean{"+
            ", id='"+id+'\''+
            "name='"+name+'\''+
            ", url='"+url+'\''+
            ", thumb='"+thumb+'\''+
            //", path='"+path+'\''+
            '}';
    }
}
