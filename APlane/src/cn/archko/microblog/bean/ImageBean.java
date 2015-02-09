package cn.archko.microblog.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @description:
 * @author: archko 13-9-27 :上午9:22
 */
public class ImageBean implements Parcelable {

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

    public static final Parcelable.Creator<ImageBean> CREATOR=new Parcelable.Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel source) {
            return new ImageBean(source);
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };

    public ImageBean() {
    }

    public ImageBean(Parcel in) {
        readFromParcel(in);
    }

    protected void readFromParcel(Parcel in) {
        id=in.readString();
        name=in.readString();
        url=in.readString();
        thumb=in.readString();
        path=in.readString();
        filesize=in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(thumb);
        dest.writeString(path);
        dest.writeLong(filesize);
    }

    @Override
    public boolean equals(Object object) {
        if (this==object) return true;
        if (object==null||getClass()!=object.getClass()) return false;

        ImageBean bean=(ImageBean) object;

        if (id!=null ? !id.equals(bean.id) : bean.id!=null) return false;
        if (name!=null ? !name.equals(bean.name) : bean.name!=null) return false;
        if (thumb!=null ? !thumb.equals(bean.thumb) : bean.thumb!=null) return false;
        if (url!=null ? !url.equals(bean.url) : bean.url!=null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result=id!=null ? id.hashCode() : 0;
        result=31*result+(name!=null ? name.hashCode() : 0);
        result=31*result+(url!=null ? url.hashCode() : 0);
        result=31*result+(thumb!=null ? thumb.hashCode() : 0);
        return result;
    }

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
