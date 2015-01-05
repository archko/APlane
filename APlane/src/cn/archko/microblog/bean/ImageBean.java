package cn.archko.microblog.bean;

/**
 * @description:
 * @author: archko 13-9-27 :上午9:22
 */
public class ImageBean {

    public String id;
    /**
     * 标题
     */
    public String name;
    /**
     * 大图url
     */
    public String url;
    /**
     * 小图url
     */
    public String thumb;
    /**
     * 本地全路径
     */
    public String path;
    /**
     * 文件大小.
     */
    public long filesize;

    @Override
    public String toString() {
        return "ImageBean{"+
            "id='"+id+'\''+
            ", name='"+name+'\''+
            ", url='"+url+'\''+
            ", thumb='"+thumb+'\''+
            ", path='"+path+'\''+
            ", filesize="+filesize+
            '}';
    }
}
