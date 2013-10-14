package com.me.microblog.bean;

/**
 * User: archko Date: 12-9-27 Time: 下午4:21
 */
public class ContentItem {

    /**
     * 类型，@，#，http
     */
    public String type;
    /**
     * 类型对应的内容，如果是@,#就有包括这两个
     */
    public String content;

    @Override
    public String toString() {
        return "ContentItem{"+
            "type='"+type+'\''+
            ", content="+content+
            '}';
    }
}
