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
import android.util.Log;
import android.widget.ImageView;
import com.andrew.apollo.utils.ApolloUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A subclass of {@link ImageWorker} that fetches images from a URL.
 */
public class ImageFetcher extends ImageWorker {

    public static final int IO_BUFFER_SIZE_BYTES = 1024;

    public static final int DEFAULT_MAX_IMAGE_HEIGHT = 1920;

    public static final int DEFAULT_MAX_IMAGE_WIDTH = 720;
    public static int DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT=4096;
    public static int DEFAULT_MAX_IMAGE_TEXTURE_WIDTH=4096;


    public static final String DEFAULT_HTTP_CACHE_DIR = "http"; //$NON-NLS-1$

    public static final int CONNECT_TIMEOUT=10000;
    public static final int READ_TIMEOUT=120000;

    private static ImageFetcher sInstance = null;

    ImageOption mImageOption;

    /**
     * Creates a new instance of {@link ImageFetcher}.
     *
     * @param context The {@link Context} to use.
     */
    public ImageFetcher(final Context context) {
        super(context);
        if (null==mImageOption) {
            mImageOption=new ImageOption();
        }
    }

    public void setImageOption(ImageOption mImageOption) {
        this.mImageOption=mImageOption;
    }

    public void initImageOption(int maxWidth, int maxHeight, Bitmap.Config config){
        if (null==mImageOption) {
            mImageOption=new ImageOption();
        }
        mImageOption.setMaxWidth(maxWidth);
        mImageOption.setMaxHeight(maxHeight);
        mImageOption.setConfig(config);
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Bitmap processBitmap(final String url, ImageOption imageOption) {
        if (url == null) {
            Log.w("", "url is null");
            return null;
        }

        ImageOption option=mImageOption;
        if (imageOption!=null) {
            option=imageOption;
        }
        Scheme scheme=Scheme.ofUri(url);
        //Log.d("", "scheme:"+scheme+" url:"+url);
        if (scheme==Scheme.HTTP||scheme==Scheme.HTTPS) {
            final File file = downloadBitmapToFileByOKHttp(mContext, url, DEFAULT_HTTP_CACHE_DIR);
            if (file != null) {
                // Return a sampled down version
                final Bitmap bitmap = decodeSampledBitmapFromFile(file.toString(), option.getMaxWidth(),
                    option.getMaxHeight(), option.getConfig());
                file.delete();
                if (bitmap != null) {
                    return bitmap;
                }
            }
        } else if (scheme==Scheme.FILE||url.startsWith("/")){
            final File file = new File(url);
            if (file != null) {
                // Return a sampled down version
                final Bitmap bitmap = decodeSampledBitmapFromFile(file.toString(), option.getMaxWidth(),
                    option.getMaxHeight(), option.getConfig());
                if (bitmap != null) {
                    Log.d("", "width:"+bitmap.getWidth()+" height:"+bitmap.getHeight()+" mh:"+DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT);
                    return bitmap;
                }
            }
        } else if (scheme==Scheme.DRAWABLE) {

        } else if (scheme==Scheme.ASSETS) {

        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String processImageUrl(final String url) {
        return url;
    }

    /**
     * Used to fetch album images.
     */
    public void startLoadImage(final String key, final ImageView imageView) {
        //loadImage(key, imageView);
        startLoadImage(key, imageView, null);
    }

    public void startLoadImage(final String key, final ImageView imageView, final ImageOption imageOption) {
        loadImage(key, imageView, imageOption);
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
        return null;
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
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
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

    public static final File downloadBitmapToFileByOKHttp(final Context context, final String urlString,
        final String uniqueName) {
        final File cacheDir=ImageCache.getDiskCacheDir(context, uniqueName);

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        //disableConnectionReuseIfNecessary();
        BufferedOutputStream out=null;

        try {
            final File tempFile=File.createTempFile("bitmap", null, cacheDir); //$NON-NLS-1$

            OkHttpClient client=new OkHttpClient();
            Request.Builder builder=new Request.Builder();
            builder.url(urlString);
            client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);    // socket timeout

            Request request=builder.build();
            Response response=client.newCall(request).execute();

            if (!response.isSuccessful()) {
                //System.out.println("downloadFailed:"+urlString);
                return null;
            }
            //System.out.println("downloadSuc:"+urlString);

            ResponseBody body=response.body();
            final InputStream in=new BufferedInputStream(body.byteStream(), IO_BUFFER_SIZE_BYTES);
            out=new BufferedOutputStream(new FileOutputStream(tempFile), IO_BUFFER_SIZE_BYTES);

            int oneByte;
            while ((oneByte=in.read())!=-1) {
                out.write(oneByte);
            }
            return tempFile;
        } catch (final IOException ignored) {
        } finally {
            if (out!=null) {
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
        return decodeSampledBitmapFromFile(filename, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT,
            Bitmap.Config.RGB_565);
        // First decode with inJustDecodeBounds=true to check dimensions
        /*final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, DEFAULT_MAX_IMAGE_WIDTH,
                DEFAULT_MAX_IMAGE_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(filename, options);*/
    }

    public static Bitmap decodeSampledBitmapFromFile(final String filename, int maxWidth, int maxheight, Bitmap.Config config) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize2(options, maxWidth, maxheight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig=config;
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
            while (height/(inSampleSize)>DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * textture max height 2048,4096
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static final int calculateInSampleSize2(final BitmapFactory.Options options,
        final int reqWidth, final int reqHeight) {
        final int height=options.outHeight;
        final int width=options.outWidth;
        int inSampleSize=1;

        while (height/(inSampleSize)>DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT||width/inSampleSize>DEFAULT_MAX_IMAGE_WIDTH) {
            inSampleSize*=2;
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
