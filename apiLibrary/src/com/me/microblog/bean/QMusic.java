package com.me.microblog.bean;

/**
 *
 * @author archko
 */
public class QMusic {

    public static final long serialVersionUID=3894560643019408219L;

    /**
     * 演唱者
     */
    public String author;
    /**
     * 音频地址
     */
    public String url;
    /**
     * 音频名字，歌名
     */
    public String title;

    @Override
    public String toString() {
        return "QMusic{"+
            "author='"+author+'\''+
            ", url='"+url+'\''+
            ", title='"+title+'\''+
            '}';
    }
}
