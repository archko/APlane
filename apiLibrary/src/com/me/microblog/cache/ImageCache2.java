package com.me.microblog.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.me.microblog.core.ImageManager;

/**
 * @param : archko Date: 12-12-25 Time: 下午2:26
 */
public class ImageCache2 {

    private static ImageCache2 sInstance;
    LruCache<String, Bitmap> mLruCache;
    ImageManager mImageManager;
    private static LruCache<String, Bitmap> mLargeLruCache;

    public ImageCache2() {
        mLruCache=new LruCache<String, Bitmap>(32);
        mImageManager=new ImageManager();
    }

    public final static ImageCache2 getInstance() {
        if (null==sInstance) {
            sInstance=new ImageCache2();
        }

        return sInstance;
    }

    public LruCache<String, Bitmap> getLruCache() {
        if (null==mLruCache) {
            mLruCache=new LruCache<String, Bitmap>(32);
        }
        return mLruCache;
    }

    public ImageManager getImageManager() {
        if (null==mImageManager) {
            mImageManager=new ImageManager();
        }
        return mImageManager;
    }

    /**
     * Called to add a new image to the memory cache
     *
     * @param data   The key identifier
     * @param bitmap The {@link android.graphics.Bitmap} to cache
     */
    public void addBitmapToMemCache(final String data, final Bitmap bitmap) {
        if (data==null||bitmap==null) {
            return;
        }
        // Add to memory cache
        if (getBitmapFromMemCache(data)==null) {
            mLruCache.put(data, bitmap);
        }
    }

    /**
     * Fetches a cached image from the memory cache
     *
     * @param data Unique identifier for which item to get
     * @return The {@link android.graphics.Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getBitmapFromMemCache(final String data) {
        if (data==null) {
            return null;
        }
        if (mLruCache!=null) {
            final Bitmap lruBitmap=mLruCache.get(data);
            if (lruBitmap!=null) {
                return lruBitmap;
            }
        }
        return null;
    }

    /**
     * Fetches a cached image from the disk cache
     *
     * @param data Unique identifier for which item to get
     * @return The {@link android.graphics.Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getBitmapFromDiskCache(final String data) {
        if (data==null) {
            return null;
        }

        // Check in the memory cache here to avoid going to the disk cache less
        // often
        if (getBitmapFromMemCache(data)!=null) {
            return getBitmapFromMemCache(data);
        }

        final String key=Md5Digest.getInstance().getMd5(data);
        Bitmap bitmap=mImageManager.loadBitmapFromSysByPathPortrait(key, -1);
        if (bitmap!=null) {
            return bitmap;
        }
        return null;
    }

    /**
     * Tries to return a cached image from memory cache before fetching from the
     * disk cache
     *
     * @param data Unique identifier for which item to get
     * @return The {@link android.graphics.Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getCachedBitmap(final String data) {
        if (data==null) {
            return null;
        }
        Bitmap cachedImage=getBitmapFromMemCache(data);
        if (cachedImage==null) {
            cachedImage=getBitmapFromDiskCache(data);
        }
        if (cachedImage!=null) {
            addBitmapToMemCache(data, cachedImage);
            return cachedImage;
        }
        return null;
    }

    /**
     * 获取扩展名,带点号的,这里有一个问题就是当图片的url不是以png,gif,jpg这样结束的,
     * 没有后缀名,就无法获取对应的值,反而会以http://xx.cn/adf 取cn为扩展名.
     *
     * @return
     */
    public static String getExt(String uri) {
        /*String ext=".png";
        int dot=uri.lastIndexOf('.');
        if ((dot>-1)&&(dot<(uri.length()-1))) {
            ext=uri.substring(dot);
        }
        return ext;*/
        String ext=".png";
        if (uri.endsWith("gif")) {
            ext=".gif";
        } else if (uri.endsWith(".jpg")) {
            ext=".jpg";
        }

        return ext;
    }
}
