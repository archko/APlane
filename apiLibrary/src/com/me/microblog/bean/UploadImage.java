package com.me.microblog.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: archko 14-1-12 :14:29
 */
public class UploadImage implements Parcelable {

    /**
     * 数据库主键,自身主键
     */
    public int id;
    /**
     * 草稿的id,在保存数据时,是一对多的关系.对应数据库的主键.
     * 队列的id也是这个,在队列中,就是队列的主键,在草稿中,就是草稿的.
     * 一个图片不会在草稿,又在队列中.
     */
    public int dbId;
    /**
     * 上传图片的id
     */
    public String pic_id;
    /**
     * 图片未上传的本地路径.
     */
    public String path;
    /**
     * 是否上传了,这个为关键标识,而不是判断是否有id或路径.
     */
    public boolean hasUploaded = false;

    public UploadImage() {
    }

    public UploadImage(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UploadImage createFromParcel(Parcel in) {
            return new UploadImage(in);
        }

        public UploadImage[] newArray(int size) {
            return new UploadImage[ size ];
        }
    };

    protected void readFromParcel(Parcel in) {
        id = in.readInt();
        dbId = in.readInt();
        pic_id = in.readString();
        path = in.readString();
        hasUploaded = in.readInt() != 0;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(dbId);
        dest.writeString(pic_id);
        dest.writeString(path);
        dest.writeInt(hasUploaded ? 1 : 0);
    }

    @Override
    public String toString() {
        return "UploadImage{" +
            "id=" + id +
            ", dbId=" + dbId +
            ", pic_id='" + pic_id + '\'' +
            ", path='" + path + '\'' +
            ", hasUploaded=" + hasUploaded +
            '}';
    }
}
