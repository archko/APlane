package com.me.microblog.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtils;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.util.WeiboLog;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * 管理图片,缓存,
 */
public class ImageManager {

    private static final String TAG = "ImageManager";
    public static int IMAGE_MAX_WIDTH = 480;
    public static int IMAGE_MAX_HEIGHT = 800;
    public static final int BITMAP_SIZE = 1000 * 1000 * 6;

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(),
            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public ImageManager() {
    }

    /*private String getHashString(MessageDigest digest) {
        StringBuilder builder=new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b>>4)&0xf));
            builder.append(Integer.toHexString(b&0xf));
        }

        return builder.toString();
    }*/

    /**
     * 从网络下载图片,并放到缓存中.
     *
     * @param url 图片的url
     * @param dir 图片存储路径
     * @return
     */
    public synchronized Bitmap downloadImage(String url, String dir) {
        InputStream is = null;
        try {
            is = ImageManager.getImageStream(url);
            if (null == is || is.available() == 0) {
                return null;
            }
            int len = is.available();
            WeiboUtils weiboUtil = WeiboUtils.getWeiboUtil();
            String filepath = dir + weiboUtil.getMd5(url) + WeiboUtils.getExt(url);
            //writeStreamToFile(is, filepath);
            saveStreamAsFile(is, filepath);

            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            //writeBitmapToFile(bitmap, file);
            //WeiboLog.d(TAG,"download image:"+url);
            if (len < 300 * 1000) {
                ImageCache2.getInstance().addBitmapToMemCache(url, bitmap);
            }

            return bitmap;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                    is = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 下载，直接返回Bitmap，因为有可能没有卡
     *
     * @param url
     * @param dir
     * @param cache
     * @return
     */
    public Bitmap downloadImage2(String url, String dir, boolean cache) {
        InputStream is = null;
        try {
            is = ImageManager.getImageStream(url);
            if (null == is || is.available() == 0) {
                return null;
            }
            int len = is.available();
            WeiboUtils weiboUtil = WeiboUtils.getWeiboUtil();
            String filepath = dir + weiboUtil.getMd5(url) + WeiboUtils.getExt(url);

            //saveStreamAsFile(is, filepath);

            Bitmap bitmap = decodeBitmap(is, 1);
            if (cache) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(filepath));
            }

            return bitmap;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                    is = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取图片，主要用于个人资料中的头像获取，临时用的。下载完成后直接放入内存中。
     *
     * @param url 下载地址
     * @param dir 存储目录
     * @return
     */
    public Bitmap getBitmapFromDiskOrNet(String url, String dir, boolean cache) {
        Bitmap bitmap = loadBitmapFromSysByUrl(url, dir, - 1);
        if (null != bitmap) {
            ImageCache2.getInstance().addBitmapToMemCache(url, bitmap);
            return bitmap;
        }

        WeiboLog.d(TAG, "download image:" + url);
        /*String filePath=downloadImages(url, dir);
        if (!TextUtils.isEmpty(filePath)) {
            bitmap=loadBitmapFromSysByPath(filePath, -1);
            if (null!=bitmap) {
                ImageCache2.getInstance().addBitmapToMemCache(url, bitmap);
                return bitmap;
            }
        }*/
        bitmap = downloadImage2(url, dir, cache);
        if (null != bitmap) {
            ImageCache2.getInstance().addBitmapToMemCache(url, bitmap);
        }
        return bitmap;
    }

    /**
     * 下载图片，存储路径需要通过url与dir计算得到
     *
     * @param url 下载地址
     * @param dir 图片的目录
     * @return
     */
    public static String downloadImages(String url, String dir) {
        byte[] is = null;
        try {
            is = ImageManager.getImageByte(url);
            if (null == is || is.length == 0) {
                return null;
            }

            WeiboUtils weiboUtil = WeiboUtils.getWeiboUtil();
            String filePath = dir + weiboUtil.getMd5(url) + WeiboUtils.getExt(url);
            boolean res = saveBytesAsFile(is, filePath);

            if (res) {
                return filePath;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        }

        return null;
    }

    /**
     * 下载gif图片
     *
     * @param url
     * @param filePath 文件存储路径
     * @return
     */
    public static InputStream downloadGif(String url, String filePath) {
        byte[] is = null;
        try {
            is = ImageManager.getImageByte(url);
            if (null == is) {
                return null;
            }

            boolean res = saveBytesAsFile(is, filePath);

            if (res) {
                return new FileInputStream(new File(filePath));
            }

            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        }

        return null;
    }

    public static byte[] getImageByte(String urlString) throws IOException {
        try {
            OkHttpClient client=new OkHttpClient();
            Request.Builder builder=new Request.Builder();
            builder.url(urlString);
            client.setConnectTimeout(AbsApiImpl.CONNECT_TIMEOUT, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(AbsApiImpl.READ_TIMEOUT, TimeUnit.SECONDS);    // socket timeout

            Request request=builder.build();
            Response response=client.newCall(request).execute();
            if (response.isSuccessful()){
                return response.body().bytes();
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 从缓存中取出图片
     *
     * @param url
     * @return
     */
    /*public Bitmap getBitmapFromCache(String url) {
        //WeiboLog.i(TAG, "cache size:" + bitmapCache.getSize());
        Bitmap bitmap=null;
        LruCache<String, Bitmap> lruCache=App.getLruCache();
        bitmap=lruCache.get(url);
        return bitmap;
    }*/

    /**
     * 将图片放入缓存中.key是图片对应的url.
     *
     * @param url
     * @param bitmap
     */
    /*public void putBitmapIntoCache(String url, Bitmap bitmap) {
        LruCache<String, Bitmap> lruCache=App.getLruCache();
        lruCache.put(url, bitmap);
    }*/

    /**
     * 下载图片
     *
     * @param urlString 图片url
     * @return
     * @throws IOException
     */
    public static InputStream getImageStream(String urlString) throws IOException {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream inputStrem = null;

        url = new URL(urlString);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(AbsApiImpl.CONNECT_TIMEOUT);
        conn.setReadTimeout(AbsApiImpl.READ_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", AbsApiImpl.USERAGENT);
        conn.connect();
        inputStrem = conn.getInputStream();

        return inputStrem;
    }

    /**
     * 保持长宽比缩小Bitmap
     *
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @param quality   1~100
     * @return
     */
    public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {

        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();

        // no need to resize
        if (originWidth < maxWidth && originHeight < maxHeight) {
            return bitmap;
        }

        int newWidth = originWidth;
        int newHeight = originHeight;

        // 若图片过宽, 则保持长宽比缩放图片
        if (originWidth > maxWidth) {
            newWidth = maxWidth;

            double i = originWidth * 1.0 / maxWidth;
            newHeight = (int) Math.floor(originHeight / i);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        // 若图片过长, 则从中部截取
        if (newHeight > maxHeight) {
            newHeight = maxHeight;

            int half_diff = (int) ((originHeight - maxHeight) / 2.0);
            bitmap = Bitmap.createBitmap(bitmap, 0, half_diff, newWidth, newHeight);
        }

        WeiboLog.d(TAG, newWidth + " width");
        WeiboLog.d(TAG, newHeight + " height");

        return bitmap;
    }

    /////////////////---------------------

    /**
     * 通过url加载本地缓存图片
     *
     * @param url 图片的闷罐车地址
     * @param dir 存储目录
     * @return 图片
     */
    public Bitmap loadBitmapFromSysByUrl(String url, String dir) {
        String path = dir + WeiboUtils.getWeiboUtil().getMd5(url) + WeiboUtils.getExt(url);
        WeiboLog.d(TAG, "loadBitmapFromSysByUrl.path:" + path);
        return loadBitmapFromSysByPath(path);
    }

    /**
     * 通过url加载本地缓存图片
     *
     * @param url   图片的闷罐车地址
     * @param dir   存储目录
     * @param ratio 缩放率，如果是-1表示依照当前的屏幕高与宽缩放
     * @return 图片
     */
    public Bitmap loadBitmapFromSysByUrl(String url, String dir, int ratio) {
        String path = dir + WeiboUtils.getWeiboUtil().getMd5(url) + WeiboUtils.getExt(url);
        WeiboLog.d(TAG, "loadBitmapFromSysByUrl.path:" + path + " ratio:" + ratio);
        return loadBitmapFromSysByPath(path, ratio);
    }

    /**
     * 根据文件名加载图片缓存资源
     *
     * @param filename 文件名
     * @return 图片
     */
    public Bitmap loadBitmapFromSysByPath(String filename) {
        return loadBitmapFromSysByPath(filename, - 1);
    }

    /**
     * 根据文件名加载图片缓存资源
     * 手机里面的图片多数是窄而长的，所以按照宽来缩放，而且指定宽为400是为了兼顾有些手机的内存
     *
     * @param filename 文件名
     * @param ratio    加载质量,是缩放的倍数，如果是-1表示按照宽来缩放，
     * @return 图片
     */
    public Bitmap loadBitmapFromSysByPath(String filename, int ratio) {
        Bitmap bitmap = null;
        File file = new File(filename);
        if (file.exists()) {
            //TODO 
            int dw = IMAGE_MAX_WIDTH;
            int dh = IMAGE_MAX_HEIGHT;

            // Load up the image's dimensions not the image itself 
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (ratio == - 1) {
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeFile(filename, options);

                int heightRatio = (int) Math.ceil(options.outHeight / ((float) dh));
                int widthRatio = (int) Math.ceil(options.outWidth / (float) dw);

                /*WeiboLog.d(TAG, "widthRatio:"+widthRatio+" width:"+options.outWidth+
                    " height:"+options.outHeight+" dw:"+dw+" dh:"+dh+" heightRatio:"+heightRatio);*/

                // If both of the ratios are greater than 1,  
                // one of the sides of the image is greater than the screen 
                if (widthRatio > 1) {
                    options.inSampleSize = widthRatio;
                    if (heightRatio > 1) {
                        if (heightRatio > widthRatio) {
                            // Height ratio is larger, scale according to it 
                            options.inSampleSize = heightRatio;
                        }
                    }
                } else if (heightRatio > 1) {
                    options.inSampleSize = heightRatio;
                    if (widthRatio > 1) {
                        if (widthRatio > heightRatio) {
                            // Height ratio is larger, scale according to it
                            options.inSampleSize = widthRatio;
                        }
                    }
                }
            } else {
                options.inSampleSize = ratio;
            }

            //WeiboLog.d(TAG, "inSampleSize:"+options.inSampleSize+" w:"+dw+" h:"+dh);

            // Decode it for real
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filename, options);
        }

        return bitmap;
    }

    /**
     * 根据文件名加载图片缓存资源，只有高宽都大于默认值，才压缩
     * 为了更好地显示图片，这里对图片的压缩比在列表中的要小，显示的要大，高显示为原来的3/2。
     *
     * @param filename 文件名
     * @param ratio    加载质量,是缩放的倍数
     * @return 图片
     */
    public Bitmap loadBitmapFromSysByPathPortrait(String filename, int ratio) {
        Bitmap bitmap = null;
        File file = new File(filename);
        if (file.exists()) {
            int dw = IMAGE_MAX_WIDTH;
            int dh = IMAGE_MAX_HEIGHT * 2;

            // Load up the image's dimensions not the image itself 
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (ratio == - 1) {
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeFile(filename, options);

                int heightRatio = (int) Math.ceil(options.outHeight / (float) dh);
                int widthRatio = (int) Math.ceil(options.outWidth / (float) dw);

                /*WeiboLog.d(TAG, "Portrait widthRatio:"+widthRatio+" width:"+options.outWidth+
                    " height:"+options.outHeight+" dw:"+dw+" dh:"+dh+" heightRatio:"+heightRatio);*/

                if (widthRatio > 1 || heightRatio > 1) {
                    if (heightRatio > widthRatio) {
                        // Height ratio is larger, scale according to it 
                        options.inSampleSize = heightRatio;
                    }
                } else if (heightRatio > 1) {
                    options.inSampleSize = heightRatio;
                    if (widthRatio > 1) {
                        if (widthRatio > heightRatio) {
                            // Wdith ratio is larger, scale according to it
                            options.inSampleSize = widthRatio;
                        }
                    }
                }
            } else {
                options.inSampleSize = ratio;
            }

            /*WeiboLog.d(TAG, "Portrait inSampleSize:"+options.inSampleSize+" w:"+dw+" h:"+dh+
                " width:"+options.outWidth+" height:"+options.outHeight);*/

            // Decode it for real
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filename, options);
        }

        return bitmap;
    }

    /**
     * 加载横屏的图片，为了最大化的查看图片的内容，适应内存大的手机。
     *
     * @param filename
     * @param ratio
     * @return
     */
    public Bitmap loadBitmapFromSysByPathLandscape(String filename, int ratio) {
        Bitmap bitmap = null;
        File file = new File(filename);
        if (file.exists()) {
            int dw = IMAGE_MAX_WIDTH;
            int dh = IMAGE_MAX_HEIGHT;

            // Load up the image's dimensions not the image itself
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (ratio == - 1) {
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeFile(filename, options);

                int heightRatio = (int) Math.ceil(options.outHeight / (float) dh);
                int widthRatio = (int) Math.ceil(options.outWidth / (float) dw);

                WeiboLog.d(TAG, "widthRatio:" + widthRatio + " width:" + options.outWidth +
                    " height:" + options.outHeight + " dw:" + dw + " dh:" + dh + " heightRatio:" + heightRatio);

                if (widthRatio > 1 || heightRatio > 1) {
                    if (heightRatio > widthRatio) {
                        // Height ratio is larger, scale according to it
                        options.inSampleSize = heightRatio;
                    }
                } else if (heightRatio > 1) {
                    options.inSampleSize = heightRatio;
                    if (widthRatio > 1) {
                        if (widthRatio > heightRatio) {
                            // Wdith ratio is larger, scale according to it
                            options.inSampleSize = widthRatio;
                        }
                    }
                }
            } else {
                options.inSampleSize = ratio;
            }

            WeiboLog.d(TAG, "inSampleSize:" + options.inSampleSize + " w:" + dw + " h:" + dh);

            // Decode it for real
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filename, options);
        }

        return bitmap;
    }

    /**
     * 加载图片，为了最大化的查看图片的内容，适应内存大的手机,这里全部加载图片。
     *
     * @param filename
     * @return
     */
    public Bitmap loadFullBitmapFromSys(String filename, int ratio) {
        Bitmap bitmap = null;
        File file = new File(filename);
        if (file.exists()) {
            int dw = IMAGE_MAX_WIDTH;
            int dh = IMAGE_MAX_HEIGHT;

            // Load up the image's dimensions not the image itself
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (ratio == - 1) {
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeFile(filename, options);

                int heightRatio = (int) Math.ceil(options.outHeight / (float) dh);
                int widthRatio = (int) Math.ceil(options.outWidth / (float) dw);

                /*WeiboLog.d(TAG, "widthRatio:"+widthRatio+" width:"+options.outWidth+
                    " height:"+options.outHeight+" dw:"+dw+" dh:"+dh+" heightRatio:"+heightRatio);*/
                int fullSize = options.outHeight * options.outWidth;
                if (fullSize > BITMAP_SIZE) {
                    if (fullSize >= BITMAP_SIZE * 2) {
                        options.inSampleSize = 4;
                    } else {
                        options.inSampleSize = 2;
                    }
                }

                if (options.inSampleSize < 2 && widthRatio > 2) {
                    options.inSampleSize = widthRatio;
                }

                /*if (widthRatio>1||heightRatio>1) {
                    if (heightRatio>widthRatio) {
                        // Height ratio is larger, scale according to it
                        options.inSampleSize=heightRatio;
                    }
                } else if (heightRatio>1) {
                    options.inSampleSize=heightRatio;
                    if (widthRatio>1) {
                        if (widthRatio>heightRatio) {
                            // Wdith ratio is larger, scale according to it
                            options.inSampleSize=widthRatio;
                        }
                    }
                }*/
            } else {
                options.inSampleSize = ratio;
            }

            // Decode it for real
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filename, options);
        }

        return bitmap;
    }

    public Bitmap loadFullBitmapFromSys(String filename) {
        Bitmap bitmap = null;
        File file = new File(filename);
        if (file.exists()) {
            int dw = IMAGE_MAX_WIDTH;
            int dh = IMAGE_MAX_HEIGHT;

            // Load up the image's dimensions not the image itself
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFile(filename, options);

            int heightRatio = (int) Math.ceil(options.outHeight / (float) dh);
            int widthRatio = (int) Math.ceil(options.outWidth / (float) dw);

            WeiboLog.d(TAG, "widthRatio:" + widthRatio + " width:" + options.outWidth +
                " height:" + options.outHeight + " dw:" + dw + " dh:" + dh + " heightRatio:" + heightRatio);
            int fullSize = options.outHeight * options.outWidth;
            if (fullSize > BITMAP_SIZE) {
                if (fullSize >= BITMAP_SIZE * 4) {
                    options.inSampleSize = 2;
                } else {
                    options.inSampleSize = 1;
                }
            }

            if (options.inSampleSize < 2 && widthRatio > 2) {
                options.inSampleSize = widthRatio;
            }

            // Decode it for real
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filename, options);
            WeiboLog.d(TAG, "size:" + options.inSampleSize + " bmsize:" + bitmap.getWidth() + " h:" + bitmap.getHeight());
        }

        return bitmap;
    }

    /////////////////---------------------

    /**
     * 保存图片资源
     *
     * @param is       图片流
     * @param filePath 保存的路径,全路径
     */
    public static boolean saveBitmap(InputStream is, String filePath) {
        return saveBitmap(is, filePath, 100);
    }

    /**
     * 保存图片资源
     *
     * @param is       图片流
     * @param filePath 保存的路径,全路径
     * @param quality  保存的质量,使用Bitmap.CompressFormat压缩
     */
    public static boolean saveBitmap(InputStream is, String filePath, int quality) {
        return saveBitmap(is, filePath, quality, Bitmap.CompressFormat.PNG);
    }

    /**
     * 保存图片资源
     *
     * @param is       图片流
     * @param filePath 保存的路径,全路径
     * @param quality  保存的质量,使用Bitmap.CompressFormat压缩
     * @param format   保存的格式
     */
    public static boolean saveBitmap(InputStream is, String filePath, int quality, Bitmap.CompressFormat format) {
        FileOutputStream fos = null;
        Bitmap bitmap = null;
        try {
            fos = new FileOutputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            int heightRatio = (int) Math.ceil(options.outHeight / (float) IMAGE_MAX_HEIGHT);
            int widthRatio = (int) Math.ceil(options.outWidth / (float) IMAGE_MAX_WIDTH);
            int m = 1;
            if (heightRatio > 1 && widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    // Height ratio is larger, scale according to it
                    m = heightRatio;
                } else {
                    // Width ratio is larger, scale according to it
                    m = widthRatio;
                }
                WeiboLog.d(TAG, "高宽都超过了高分辨率..");
            }

            options.inDither = true;
            options.inSampleSize = m;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            bitmap = BitmapFactory.decodeStream(is, null, options);
            bitmap.compress(format, quality, fos);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bitmap) {
                bitmap.recycle();
            }
        }

        return false;
    }

    /**
     * 保存图片资源
     *
     * @param bytes    图片字节数组
     * @param filePath 保存路径
     * @return
     */
    public static boolean saveBitmap(byte[] bytes, String filePath) {
        return saveBitmap(bytes, filePath, 100);
    }

    /**
     * 保存图片资源
     *
     * @param bytes    图片字节数组
     * @param filePath 保存路径
     * @param quality  质量
     * @return
     */
    public static boolean saveBitmap(byte[] bytes, String filePath, int quality) {
        return saveBitmap(bytes, filePath, quality, Bitmap.CompressFormat.PNG);
    }

    /**
     * 保存图片资源
     *
     * @param bytes    图片字节数组
     * @param filePath 保存路径
     * @param quality  质量
     * @param format   格式
     * @return
     */
    public static boolean saveBitmap(byte[] bytes, String filePath, int quality, Bitmap.CompressFormat format) {
        FileOutputStream fos = null;
        Bitmap bitmap = null;
        try {
            fos = new FileOutputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            //没有这样处理的，一旦取消注释，就无法解码。
            /*options.inJustDecodeBounds=true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            int heightRatio=(int) Math.ceil(options.outHeight/(float) IMAGE_MAX_HEIGHT);
            int widthRatio=(int) Math.ceil(options.outWidth/(float) IMAGE_MAX_WIDTH);
            int m=1;
            if (heightRatio>1&&widthRatio>1) {
                if (heightRatio>widthRatio) {
                    // Height ratio is larger, scale according to it
                    m=heightRatio;
                } else {
                    // Width ratio is larger, scale according to it
                    m=widthRatio;
                }
                WeiboLog.d(TAG, "高宽都超过了高分辨率."+filePath);
            }

            options.inSampleSize=m;*/
            options.inDither = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            bitmap.compress(format, quality, fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bitmap) {
                bitmap.recycle();
            }
        }

        return false;
    }

    /**
     * 保存流资源,以文件保存,直接写入流
     * gif 图片的下载就可以这样,但是一般可能用不到.
     *
     * @param is       图片流
     * @param filePath 保存的路径,全路径
     * @return 是否保存成功.
     */
    public static boolean saveStreamAsFile(InputStream is, String filePath) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            byte[] buffer = new byte[ 1024 ];
            int i = 0;

            while ((i = is.read(buffer)) != - 1) {
                fos.write(buffer, 0, i);
            }
            fos.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (is != null) {
                is.close();
            }
        }

        return false;
    }

    /**
     * 保存字节数组资源,以文件保存,直接写入流
     * gif 图片的下载就可以这样,但是一般可能用不到.
     *
     * @param bytes    图片流
     * @param filePath 保存的路径,全路径
     * @return 是否保存成功.
     */
    public static boolean saveBytesAsFile(byte[] bytes, String filePath) throws IOException {
        BufferedOutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(filePath), 512);

            fos.write(bytes);
            fos.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        return false;
    }

    /**
     * 复制文件
     *
     * @param target 需要复制的目的地
     * @param source 源文件全路径
     * @return 是否复制成功
     */
    public boolean copyFileToFile(String target, String source) {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(target);
            byte[] b = new byte[ 1024 * 5 ];
            int len;
            while ((len = input.read(b)) != - 1) {
                output.write(b, 0, len);
            }
            output.flush();
            output.close();
            input.close();
            return true;
        } catch (Exception e) {
            System.out.println("files copy error." + e);
        }
        return false;
    }

    //////////---------
    //以下两个方法只适合小图的，没有缩放处理。
    public static Bitmap decodeBitmap(byte[] bytes, int ratio) {
        Bitmap bitmap = null;

        int dw = IMAGE_MAX_WIDTH;
        int dh = IMAGE_MAX_HEIGHT;

        // Load up the image's dimensions not the image itself 
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*if (ratio==-1) {
            options.inJustDecodeBounds=true;
            bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            //int heightRatio=(int) Math.ceil(options.outHeight/(float) dh);
            int widthRatio=(int) Math.ceil(options.outWidth/(float) dw);

            WeiboLog.d(TAG, "widthRatio:"+widthRatio+" width:"+options.outWidth+
                " height:"+options.outHeight+" dw:"+dw+" dh:"+dh);

            // If both of the ratios are greater than 1,  
            // one of the sides of the image is greater than the screen 
            if (widthRatio>1) {
                options.inSampleSize=widthRatio;
                *//*if (heightRatio>1) {
                    if (heightRatio>widthRatio) {
                        // Height ratio is larger, scale according to it 
                        bmpFactoryOptions.inSampleSize=heightRatio;
                    } else {
                        // Width ratio is larger, scale according to it 
                        bmpFactoryOptions.inSampleSize=widthRatio;
                    }
                }*//*
            }
        } else {
            options.inSampleSize=ratio;
        }*/

        options.inDither = true;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    public static Bitmap decodeBitmap(InputStream is, int ratio) {
        Bitmap bitmap = null;

        int dw = IMAGE_MAX_WIDTH;
        int dh = IMAGE_MAX_HEIGHT;

        BitmapFactory.Options options = new BitmapFactory.Options();
        /*options.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(is, null, options);
        if (ratio==-1) {
            options.inJustDecodeBounds=true;
            bitmap=BitmapFactory.decodeStream(is, null, options);

            //int heightRatio=(int) Math.ceil(options.outHeight/(float) dh);
            int widthRatio=(int) Math.ceil(options.outWidth/(float) dw);

            WeiboLog.d(TAG, "widthRatio:"+widthRatio+" width:"+options.outWidth+
                " height:"+options.outHeight+" dw:"+dw+" dh:"+dh);

            // If both of the ratios are greater than 1,  
            // one of the sides of the image is greater than the screen 
            if (widthRatio>1) {
                options.inSampleSize=widthRatio;
                *//*if (heightRatio>1) {
                    if (heightRatio>widthRatio) {
                        // Height ratio is larger, scale according to it 
                        bmpFactoryOptions.inSampleSize=heightRatio;
                    } else {
                        // Width ratio is larger, scale according to it 
                        bmpFactoryOptions.inSampleSize=widthRatio;
                    }
                }*//*
            }
        } else {
            options.inSampleSize=ratio;
        }*/

        options.inDither = true;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        bitmap = BitmapFactory.decodeStream(is, null, options);

        return bitmap;
    }
}
