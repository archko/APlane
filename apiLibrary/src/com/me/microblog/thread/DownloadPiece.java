package com.me.microblog.thread;

import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * @author: archko 14-1-21 :下午4:19
 */
public class DownloadPiece {

    public Handler handler;
    //public String name; //md5加密过的名字,废除
    public String dir;//存储目录
    public String uri;//图片的url
    public int type;// 图片的类型，是微博图片，还是转发内容图片或者用户头像等
    @Deprecated
    public String filepath; // 图片存储的路径，绝对的，废除
    public boolean cache;
    public boolean isShowLargeBitmap;
    public WeakReference<ImageView> mImageReference;

    /**
     * 构造一个下载实体
     *
     * @param h        回调
     * @param uri      图片的下载地址
     * @param type     类型，目前有头像与微博图片
     * @param filepath 图片的路径，废除了，现在在这里对图片进行解码，而不用绝对路径，用目录与url进行构造
     * @param cache    是否缓存
     * @param dir      存储目录，这是图片存储的绝对目录，可以由url+dir计算得到存储的绝对路径。
     */
    public DownloadPiece(Handler h, String uri, int type, boolean cache, String dir, boolean isShowLargeBitmap, ImageView imageView) {
        this.uri=uri;
        //this.name=name;
        this.handler=h;
        this.type=type;
        //this.filepath=filepath;
        this.cache=cache;
        this.dir=dir;
        this.isShowLargeBitmap=isShowLargeBitmap;
        this.mImageReference=new WeakReference<ImageView>(imageView);
    }
}
