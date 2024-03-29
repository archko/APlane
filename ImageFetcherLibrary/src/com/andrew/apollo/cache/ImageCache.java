/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.cache;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import cn.archko.thread.AsyncTask;
import com.andrew.apollo.utils.ApolloUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class holds the memory and disk bitmap caches.
 */
public final class ImageCache {

    private static final String TAG = ImageCache.class.getSimpleName();

    private static final String CACHE_DIR="auil";

    /**
     * Default memory cache size as a percent of device memory class
     */
    private static final float MEM_CACHE_DIVIDER = 0.25f;

    /**
     * Default disk cache size 10MB
     */
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20;

    /**
     * Compression settings when writing images to disk cache
     */
    private static final CompressFormat COMPRESS_FORMAT = CompressFormat.PNG;

    /**
     * Disk cache index to read from
     */
    private static final int DISK_CACHE_INDEX = 0;

    /**
     * Image compression quality
     */
    private static final int COMPRESS_QUALITY = 98;

    /**
     * LRU cache
     */
    private MemoryCache mLruCache;

    /**
     * Disk LRU cache
     */
    private DiskLruCache mDiskCache;

    private static ImageCache sInstance;

    /**
     * Used to temporarily pause the disk cache while scrolling
     */
    public boolean mPauseDiskAccess = false;

    private int mDiskCacheSize=DISK_CACHE_SIZE;
    private int mCompressQuality=COMPRESS_QUALITY;

    /**
     * Constructor of <code>ImageCache</code>
     *
     * @param context The {@link Context} to use
     */
    public ImageCache(final Context context) {
        init(context);
    }

    /**
     * Constructor of <code>ImageCache</code>
     *
     * @param context The {@link android.content.Context} to use
     * @param quality
     */
    public ImageCache(final Context context, int diskCacheSize, int quality) {
        mDiskCacheSize=diskCacheSize;
        mCompressQuality=quality;
        init(context);
    }

    /**
     * Used to create a singleton of {@link ImageCache}
     * 
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    public final static ImageCache getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ImageCache(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Used to create a singleton of {@link ImageCache}
     *
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    public final static ImageCache getInstance(final Context context, int diskCacheSize, int quality) {
        if (sInstance == null) {
            sInstance = new ImageCache(context.getApplicationContext(), diskCacheSize, quality);
        }
        return sInstance;
    }

    /**
     * Initialize the cache, providing all parameters.
     * 
     * @param context The {@link Context} to use
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(final Context context) {
        ApolloUtils.executeCustomTask(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                // Initialize the disk cahe in a background thread
                initDiskCache(context);
                return null;
            }
        }, (Void[])null);
        // Set up the memory cache
        initLruCache(context);
    }

    /**
     * Initializes the disk cache. Note that this includes disk access so this
     * should not be executed on the main/UI thread. By default an ImageCache
     * does not initialize the disk cache when it is created, instead you should
     * call initDiskCache() to initialize it on a background thread.
     * 
     * @param context The {@link Context} to use
     */
    public void initDiskCache(final Context context) {
        // Set up disk cache
        if (mDiskCache == null || mDiskCache.isClosed()) {
            File diskCacheDir = getDiskCacheDir(context, CACHE_DIR);
            if (diskCacheDir != null) {
                if (!diskCacheDir.exists()) {
                    diskCacheDir.mkdirs();
                }
                if (getUsableSpace(diskCacheDir) > mDiskCacheSize) {
                    try {
                        mDiskCache = DiskLruCache.open(diskCacheDir, 1, 1, mDiskCacheSize);
                    } catch (final IOException e) {
                        diskCacheDir = null;
                    }
                }
            }
        }
    }

    /**
     * Sets up the Lru cache
     * 
     * @param context The {@link Context} to use
     */
    @SuppressLint("NewApi")
    public void initLruCache(final Context context) {
        final ActivityManager activityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final int lruCacheSize = Math.round(MEM_CACHE_DIVIDER * activityManager.getMemoryClass()
                * 1024 * 1024);
        mLruCache = new MemoryCache(lruCacheSize);

        // Release some memory as needed
        if (ApolloUtils.hasICS()) {
            context.registerComponentCallbacks(new ComponentCallbacks2() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onTrimMemory(final int level) {
                    if (level >= TRIM_MEMORY_MODERATE) {
                        evictAll();
                    } else if (level >= TRIM_MEMORY_BACKGROUND) {
                        mLruCache.trimToSize(mLruCache.size() / 2);
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
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created using the supplied params and saved
     * to a {@link RetainFragment}
     * 
     * @param activity The calling {@link FragmentActivity}
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist
     */
    public static final ImageCache findOrCreateCache(final Activity activity) {
        return findOrCreateCache(activity, DISK_CACHE_SIZE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static final ImageCache findOrCreateCache(final Activity activity, int diskCacheSize) {
        // Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment retainFragment = findOrCreateRetainFragment(activity.getFragmentManager());

        // See if we already have an ImageCache stored in RetainFragment
        ImageCache cache = (ImageCache)retainFragment.getObject();

        // No existing ImageCache, create one and store it in RetainFragment
        if (cache == null) {
            cache = getInstance(activity, diskCacheSize, COMPRESS_QUALITY);
            retainFragment.setObject(cache);
        }
        return cache;
    }

    public static final ImageCache findOrCreateCache(final Context activity) {
        return findOrCreateCache(activity, DISK_CACHE_SIZE, COMPRESS_QUALITY);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static final ImageCache findOrCreateCache(final Context activity, int diskCacheSize, int quality) {
        // See if we already have an ImageCache stored in RetainFragment
        ImageCache cache = null;

        // Search for, or create an instance of the non-UI RetainFragment
        RetainFragment retainFragment = null;

        if (activity instanceof Activity&&ApolloUtils.hasHoneycomb()) {
            Activity act=(Activity) activity;
            retainFragment = findOrCreateRetainFragment(act.getFragmentManager());
        }

        // See if we already have an ImageCache stored in RetainFragment
        cache = (ImageCache)retainFragment.getObject();

        // No existing ImageCache, create one and store it in RetainFragment
        if (cache == null) {
            cache = getInstance(activity, diskCacheSize, quality);
            if (null!=retainFragment) {
                retainFragment.setObject(cache);
            }
        }
        return cache;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static final RetainFragment findOrCreateRetainFragment(final FragmentManager fm) {
        // Check to see if we have retained the worker fragment
        RetainFragment retainFragment = (RetainFragment)fm.findFragmentByTag(TAG);

        // If not retained, we need to create and add it
        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, TAG).commit();
        }
        return retainFragment;
    }

    /**
     * Adds a new image to the memory and disk caches
     *
     * @param data The key used to store the image
     * @param url
     * @param bitmap The {@link android.graphics.Bitmap} to cache
     */
    public void addBitmapToCache(final String data, String url, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        addBitmapToMemCache(data, bitmap);

        Scheme scheme=Scheme.ofUri(url);
        if (scheme==Scheme.HTTP||scheme==Scheme.HTTPS) {
        // Add to disk cache
        if (mDiskCache != null) {
            final String key = hashKeyForDisk(data);
            OutputStream out = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                if (snapshot == null) {
                    final DiskLruCache.Editor editor = mDiskCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        bitmap.compress(COMPRESS_FORMAT, mCompressQuality, out);
                        editor.commit();
                        out.close();
                        flush();
                    }
                } else {
                    snapshot.getInputStream(DISK_CACHE_INDEX).close();
                }
            } catch (final IOException e) {
                Log.e(TAG, "addBitmapToCache - " + e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (final IllegalStateException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                }
            }
        }
        }
    }

    /**
     * Called to add a new image to the memory cache
     * 
     * @param data The key identifier
     * @param bitmap The {@link Bitmap} to cache
     */
    public void addBitmapToMemCache(final String data, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }
        // Add to memory cache
        if (getBitmapFromMemCache(data) == null) {
            mLruCache.put(data, bitmap);
        }
    }

    /**
     * Fetches a cached image from the memory cache
     * 
     * @param data Unique identifier for which item to get
     * @return The {@link Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getBitmapFromMemCache(final String data) {
        if (data == null) {
            return null;
        }
        if (mLruCache != null) {
            final Bitmap lruBitmap = mLruCache.get(data);
            if (lruBitmap != null) {
                return lruBitmap;
            }
        }
        return null;
    }

    /**
     * Fetches a cached image from the disk cache
     * 
     * @param data Unique identifier for which item to get
     * @return The {@link Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getBitmapFromDiskCache(final String data) {
        if (data == null) {
            return null;
        }

        // Check in the memory cache here to avoid going to the disk cache less
        // often
        if (getBitmapFromMemCache(data) != null) {
            return getBitmapFromMemCache(data);
        }

        while (mPauseDiskAccess) {
            // Pause for moment
        }
        final String key = hashKeyForDisk(data);
        if (mDiskCache != null) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    if (inputStream != null) {
                        BitmapFactory.Options options=new BitmapFactory.Options();
                        options.inPreferredConfig=Bitmap.Config.RGB_565;
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            } catch (final IOException e) {
                Log.e(TAG, "getBitmapFromDiskCache - " + e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * Tries to return a cached image from memory cache before fetching from the
     * disk cache
     * 
     * @param data Unique identifier for which item to get
     * @return The {@link Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getCachedBitmap(final String data) {
        if (data == null) {
            return null;
        }
        Bitmap cachedImage = getBitmapFromMemCache(data);
        if (cachedImage == null) {
            cachedImage = getBitmapFromDiskCache(data);
        }
        if (cachedImage != null) {
            addBitmapToMemCache(data, cachedImage);
            return cachedImage;
        }
        return null;
    }

    /**
     * flush() is called to synchronize up other methods that are accessing the
     * cache first
     */
    public void flush() {
        ApolloUtils.executeCustomTask(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                if (mDiskCache != null) {
                    try {
                        if (!mDiskCache.isClosed()) {
                            mDiskCache.flush();
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "flush - " + e);
                    }
                }
                return null;
            }
        }, (Void[])null);
    }

    /**
     * Clears the disk and memory caches
     */
    public void clearCaches() {
        ApolloUtils.executeCustomTask(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                // Clear the disk cache
                try {
                    if (mDiskCache != null) {
                        mDiskCache.delete();
                        mDiskCache = null;
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "clearCaches - " + e);
                }
                // Clear the memory cache
                evictAll();
                return null;
            }
        }, (Void[])null);
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI
     * thread.
     */
    public void close() {
        ApolloUtils.executeCustomTask(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                if (mDiskCache != null) {
                    try {
                        if (!mDiskCache.isClosed()) {
                            mDiskCache.close();
                            mDiskCache = null;
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "close - " + e);
                    }
                }
                return null;
            }
        }, (Void[])null);
    }

    /**
     * Evicts all of the items from the memory cache and lets the system know
     * now would be a good time to garbage collect
     */
    public void evictAll() {
        if (mLruCache != null) {
            mLruCache.evictAll();
        }
        System.gc();
    }

    /**
     * @param key The key used to identify which cache entries to delete.
     */
    public void removeFromCache(final String key) {
        if (key == null) {
            return;
        }
        // Remove the Lru entry
        if (mLruCache != null) {
            mLruCache.remove(key);
        }

        try {
            // Remove the disk entry
            if (mDiskCache != null) {
                mDiskCache.remove(hashKeyForDisk(key));
            }
        } catch (final IOException e) {
            Log.e(TAG, "remove - " + e);
        }
        flush();
    }

    /**
     * Used to temporarily pause the disk cache while the user is scrolling to
     * improve scrolling.
     * 
     * @param pause True to temporarily pause the disk cache, false otherwise.
     */
    public void setPauseDiskCache(final boolean pause) {
        mPauseDiskAccess = pause;
    }

    /**
     * @return True if the user is scrolling, false otherwise.
     */
    public boolean isScrolling() {
        return mPauseDiskAccess;
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise)
     * 
     * @param context The {@link Context} to use
     * @param uniqueName A unique directory name to append to the cache
     *            directory
     * @return The cache directory
     */
    public static final File getDiskCacheDir(final Context context, final String uniqueName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(
                context).getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable
     * 
     * @return True if external storage is removable (like an SD card), false
     *         otherwise
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static final boolean isExternalStorageRemovable() {
        if (ApolloUtils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory
     * 
     * @param context The {@link Context} to use
     * @return The external cache directory
     */
    public static final File getExternalCacheDir(final Context context) {
        if (ApolloUtils.hasFroyo()) {
            final File mCacheDir = context.getExternalCacheDir();
            if (mCacheDir != null) {
                return mCacheDir;
            }
        }

        /* Before Froyo we need to construct the external cache dir ourselves */
        final String mCacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + mCacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     * 
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static final long getUsableSpace(final File path) {
        if (ApolloUtils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long)stats.getBlockSize() * (long)stats.getAvailableBlocks();
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable
     * for using as a disk filename.
     * 
     * @param key The key used to store the file
     */
    public static final String hashKeyForDisk(final String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (final NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * http://stackoverflow.com/questions/332079
     * 
     * @param bytes The bytes to convert.
     * @return A {@link String} converted from the bytes of a hashable key used
     *         to store a filename on the disk, to hex digits.
     */
    private static final String bytesToHexString(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * A simple non-UI Fragment that stores a single Object and is retained over
     * configuration changes. In this sample it will be used to retain an
     * {@link ImageCache} object.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static final class RetainFragment extends Fragment {

        /**
         * The object to be stored
         */
        private Object mObject;

        /**
         * Empty constructor as per the {@link Fragment} documentation
         */
        public RetainFragment() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);
        }

        /**
         * Store a single object in this {@link Fragment}
         * 
         * @param object The object to store
         */
        public void setObject(final Object object) {
            mObject = object;
        }

        /**
         * Get the stored object
         * 
         * @return The stored object
         */
        public Object getObject() {
            return mObject;
        }
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
            if (ApolloUtils.hasHoneycombMR1()) {
                return bitmap.getByteCount();
            }
            /* Pre HC-MR1 */
            return bitmap.getRowBytes() * bitmap.getHeight();
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
