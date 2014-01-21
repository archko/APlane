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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.andrew.apollo.utils.ApolloUtils;
import com.me.microblog.WeiboUtil;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.cache.Md5Digest;
import com.me.microblog.core.BaseApi;
import com.me.microblog.core.ImageManager;
import com.me.microblog.thread.DownloadPiece;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.WeiboLog;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * A subclass of {@link ImageWorker} that fetches images from a URL.
 */
public class ImageFetcher extends ImageWorker {

    public static final int IO_BUFFER_SIZE_BYTES = 1024;

    private static final int DEFAULT_MAX_IMAGE_HEIGHT = 1024;

    private static final int DEFAULT_MAX_IMAGE_WIDTH = 1024;

    private static final String DEFAULT_HTTP_CACHE_DIR = "http"; //$NON-NLS-1$

    private static ImageFetcher sInstance = null;

    /**
     * Creates a new instance of {@link ImageFetcher}.
     * 
     * @param context The {@link Context} to use.
     */
    public ImageFetcher(final Context context) {
        super(context);
    }

    /**
     * Used to create a singleton of the image fetcher
     * 
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    public static final ImageFetcher getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ImageFetcher(context.getApplicationContext());
        }
        return sInstance;
    }

    private HttpParams params;
    ClientConnectionManager connectionManager;
    public static final int READ_TIMEOUT=20000;

    {
        params=new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);

        params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, 100);

        //  params.setLongParameter(ClientPNames.conCONNECTION_MANAGER_TIMEOUT, TIMEOUT);
        //  HttpConnectionManagerParams.setMaxTotalConnections(params, 3000);

        //  HttpConnectionParams.setSoTimeout(params, 60*1000);
        //  HttpConnectionParams.setConnectionTimeout(params, 60*1000);
        //  ConnManagerParams.setTimeout(params, 60*1000);

        HttpConnectionParams.setConnectionTimeout(params, BaseApi.CONNECT_TIMEOUT);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Bitmap processBitmap(final String url, final DownloadPiece piece) {
        if (piece == null) {
            WeiboLog.w("piece == null");
            return null;
        }
        /*final File file = downloadBitmapToFile(mContext, url, DEFAULT_HTTP_CACHE_DIR);
        if (file != null) {
            // Return a sampled down version
            final Bitmap bitmap = decodeSampledBitmapFromFile(file.toString());
            file.delete();
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;*/
        return downloadBitmap(url, piece);
    }

    private Bitmap downloadBitmap(final String url, final DownloadPiece piece) {
        HttpGet httpGet=new HttpGet(piece.uri);
        httpGet.setHeader("User-Agent", BaseApi.USERAGENT);
        DefaultHttpClient httpClient=new DefaultHttpClient(connectionManager, params);
        HttpResponse response;
        Bitmap bitmap=null;
        try {
            String ext=WeiboUtil.getExt(piece.uri);
            String name=Md5Digest.getInstance().getMd5(piece.uri)+ext;
            String imagepath=piece.dir+name;

            bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(imagepath, -1);
            WeiboLog.d("download:"+imagepath+" bitmap:"+bitmap);
            if (null==bitmap) {
                response=httpClient.execute(httpGet);
                int code=response.getStatusLine().getStatusCode();
                if (code==200) {
                    byte[] bytes=EntityUtils.toByteArray(response.getEntity());
                    if (piece.cache) {
                        ImageManager.saveBytesAsFile(bytes, imagepath);
                        //WeiboLog.d(TAG, "需要缓存："+str2);
                        bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(imagepath, -1);
                    } else {
                        bitmap=ImageManager.decodeBitmap(bytes, -1);
                    }
                } else {
                    WeiboLog.w("下载图片失败:"+url);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 加载主页的图片，
     *
     * @param key
     * @param imageView
     */
    public void loadHomeImage(final String key, final ImageView imageView, DownloadPiece piece) {
        Log.v("", "loadHomeImage:"+key+" imv:"+imageView);
        loadImage(key, imageView, piece);
    }

    /**
     * @param pause True to temporarily pause the disk cache, false otherwise.
     */
    public void setPauseDiskCache(final boolean pause) {
        if (mImageCache != null) {
            mImageCache.setPauseDiskCache(pause);
        }
    }

    /**
     * Clears the disk and memory caches
     */
    public void clearCaches() {
        if (mImageCache != null) {
            mImageCache.clearCaches();
        }
    }

    /**
     * @param key The key used to find the image to remove
     */
    public void removeFromCache(final String key) {
        if (mImageCache != null) {
            mImageCache.removeFromCache(key);
        }
    }

    /**
     * @param key The key used to find the image to return
     */
    public Bitmap getCachedBitmap(final String key) {
        if (mImageCache != null) {
            return mImageCache.getCachedBitmap(key);
        }
        return getDefaultArtwork();
    }

    /**
     * Download a {@link Bitmap} from a URL, write it to a disk and return the
     * File pointer. This implementation uses a simple disk cache.
     * 
     * @param context The context to use
     * @param urlString The URL to fetch
     * @return A {@link File} pointing to the fetched bitmap
     */
    public static final File downloadBitmapToFile(final Context context, final String urlString,
            final String uniqueName) {
        final File cacheDir = ImageCache.getDiskCacheDir(context, uniqueName);

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            final File tempFile = File.createTempFile("bitmap", null, cacheDir); //$NON-NLS-1$

            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            final InputStream in = new BufferedInputStream(urlConnection.getInputStream(),
                    IO_BUFFER_SIZE_BYTES);
            out = new BufferedOutputStream(new FileOutputStream(tempFile), IO_BUFFER_SIZE_BYTES);

            int oneByte;
            while ((oneByte = in.read()) != -1) {
                out.write(oneByte);
            }
            return tempFile;
        } catch (final IOException ignored) {
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Decode and sample down a {@link Bitmap} from a file to the requested
     * width and height.
     * 
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A {@link Bitmap} sampled down from the original with the same
     *         aspect ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(final String filename) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, DEFAULT_MAX_IMAGE_WIDTH,
                DEFAULT_MAX_IMAGE_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Calculate an inSampleSize for use in a
     * {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This
     * implementation calculates the closest inSampleSize that will result in
     * the final decoded bitmap having a width and height equal to or larger
     * than the requested width and height. This implementation does not ensure
     * a power of 2 is returned for inSampleSize which can be faster when
     * decoding but results in a larger bitmap which isn't as useful for caching
     * purposes.
     * 
     * @param options An options object with out* params already populated (run
     *            through a decode* method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static final int calculateInSampleSize(final BitmapFactory.Options options,
            final int reqWidth, final int reqHeight) {
        /* Raw height and width of image */
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            /* More than 2x the requested pixels we'll sample down further */
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        /* HTTP connection reuse which was buggy pre-froyo */
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false"); //$NON-NLS-1$
        }
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more
     * information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     * 
     * @return true if this OS version is affected, false otherwise
     */
    public static final boolean hasHttpConnectionBug() {
        return !ApolloUtils.hasFroyo();
    }

}
