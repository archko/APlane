package com.me.microblog.cache;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import com.me.microblog.App;
import com.me.microblog.core.ImageManager;

/**
 * @param : archko Date: 12-12-25 Time: 下午2:26
 */
public class ImageCache2 {

    /**
     * Default memory cache size as a percent of device memory class
     */
    private static final float MEM_CACHE_DIVIDER=0.25f/2;

    private static ImageCache2 sInstance;
    LruCache<String, Bitmap> mLruCache;
    ImageManager mImageManager;
    //private static LruCache<String, Bitmap> mLargeLruCache;

    /**
     * Used to temporarily pause the disk cache while scrolling
     */
    public boolean mPauseDiskAccess=false;

    public ImageCache2() {
        init(App.getAppContext());
        mImageManager=new ImageManager();
    }

    public final static ImageCache2 getInstance() {
        if (null==sInstance) {
            sInstance=new ImageCache2();
        }

        return sInstance;
    }

    /**
     * Initialize the cache, providing all parameters.
     *
     * @param context     The {@link Context} to use
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(final Context context) {
        /*ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                // Initialize the disk cahe in a background thread
                initDiskCache(context);
                return null;
            }
        }, (Void[]) null);*/
        // Set up the memory cache
        initLruCache(context);
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
     * Sets up the Lru cache
     *
     * @param context The {@link Context} to use
     */
    //@SuppressLint("NewApi")
    public void initLruCache(final Context context) {
        final ActivityManager activityManager=(ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);
        final int lruCacheSize=Math.round(MEM_CACHE_DIVIDER*activityManager.getMemoryClass()
            *1024*1024);
        mLruCache=new MemoryCache(lruCacheSize);

        // Release some memory as needed
        if(true){//if (ApolloUtils.hasICS()) {
            context.registerComponentCallbacks(new ComponentCallbacks2() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onTrimMemory(final int level) {
                    if (level>=TRIM_MEMORY_MODERATE) {
                        evictAll();
                    } else if (level>=TRIM_MEMORY_BACKGROUND) {
                        mLruCache.trimToSize(mLruCache.size()/2);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onLowMemory() {
                    // Nothing to do
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onConfigurationChanged(final Configuration newConfig) {
                    // Nothing to do
                }
            });
        }
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

    //======================================

    /**
     * Used to temporarily pause the disk cache while the user is scrolling to
     * improve scrolling.
     *
     * @param pause True to temporarily pause the disk cache, false otherwise.
     */
    public void setPauseDiskCache(final boolean pause) {
        mPauseDiskAccess=pause;
    }

    /**
     * @return True if the user is scrolling, false otherwise.
     */
    public boolean isScrolling() {
        return mPauseDiskAccess;
    }

    /**
     * Evicts all of the items from the memory cache and lets the system know
     * now would be a good time to garbage collect
     */
    public void evictAll() {
        if (mLruCache!=null) {
            mLruCache.evictAll();
        }
        System.gc();
    }

    /**
     * Used to cache images via {@link LruCache}.
     */
    public static final class MemoryCache extends LruCache<String, Bitmap> {

        /**
         * Constructor of <code>MemoryCache</code>
         *
         * @param maxSize The allowed size of the {@link LruCache}
         */
        public MemoryCache(final int maxSize) {
            super(maxSize);
        }

        /**
         * Get the size in bytes of a bitmap.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        public static final int getBitmapSize(final Bitmap bitmap) {
            //if (ApolloUtils.hasHoneycombMR1()) {
                return bitmap.getByteCount();
            //}
            /* Pre HC-MR1 */
            //return bitmap.getRowBytes()*bitmap.getHeight();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected int sizeOf(final String paramString, final Bitmap paramBitmap) {
            return getBitmapSize(paramBitmap);
        }

    }

}
