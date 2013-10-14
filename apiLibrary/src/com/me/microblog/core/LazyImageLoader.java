package com.me.microblog.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.me.microblog.WeiboUtil;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
@Deprecated
public class LazyImageLoader {

    private static final String TAG="LazyImageLoader";
    public static final int HANDLER_MESSAGE_ID=1;
    public static final String EXTRA_BITMAP="extra_bitmap";
    public static final String EXTRA_IMAGE_URL="extra_image_url";
    private ImageManager mImageManager=null;

    public LazyImageLoader(Context context) {
        mImageManager=new ImageManager();
    }

    /*public Bitmap getBitmapNotFromCache(String url, String dir) {
        Bitmap bitmap=mImageManager.loadBitmapFromSysByUrl(url, dir, -1);
        if (null!=bitmap) {
            saveBitmapToCache(url, bitmap);
            return bitmap;
        }

        WeiboLog.d(TAG, "download image:"+url);
        String filePath=mImageManager.downloadImages(url, dir);
        if (!TextUtils.isEmpty(filePath)) {
            bitmap=mImageManager.loadBitmapFromSysByPath(filePath, -1);
            if (null!=bitmap) {
                saveBitmapToCache(url, bitmap);
                return bitmap;
            }
        }
        return bitmap;
    }

    void saveBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap.getWidth()*bitmap.getHeight()<300000) {
            mImageManager.putBitmapIntoCache(url, bitmap);
        }
    }

    public void putBitmapIntoCache(String url, Bitmap bitmap) {
        mImageManager.putBitmapIntoCache(url, bitmap);
    }*/

    /**
     * 获取gif图片
     *
     * @param url gif的url
     * @param dir 存储目录
     * @return
     */
    public InputStream getGifNotFromCache(String url, String dir) {
        InputStream fis=null;
        String path=dir+WeiboUtil.getWeiboUtil().getMd5(url)+WeiboUtil.getExt(url);
        File file=new File(path);
        if (file.exists()) {
            try {
                fis=new FileInputStream(file);
                return fis;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WeiboLog.d(TAG, "download gif:");
        fis=mImageManager.downloadGif(url, path);
        return fis;
    }

    /*public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap=null;
        bitmap=mImageManager.getBitmapFromCache(url);

        return bitmap;
    }

    public Bitmap getBitmap(String url, String dir) {
        Bitmap bitmap=null;
        bitmap=mImageManager.getBitmapFromCache(url);
        if (null!=bitmap) {
            return bitmap;
        }

        synchronized (this) {
            String path=dir+WeiboUtil.getWeiboUtil().getMd5(url)+WeiboUtil.getExt(url);
            File file=new File(path);
            if (file.exists()) {
                bitmap=BitmapFactory.decodeFile(path);
                return bitmap;
            }
        }

        bitmap=mImageManager.downloadImage(url, dir);
        return bitmap;
    }*/

    // Low-level interface to get ImageManager
    public ImageManager getImageManager() {
        return mImageManager;
    }

    public interface ImageLoaderCallback {

        void refresh(String url, Bitmap bitmap);

    }
}
