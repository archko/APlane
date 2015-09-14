package cn.archko.microblog.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.archko.microblog.R;
import cn.archko.microblog.bean.ImageBean;
import cn.archko.microblog.ui.PrefsActivity;
import com.andrew.apollo.cache.ImageCache;
import com.andrew.apollo.cache.Scheme;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.MyWebView;
import com.me.microblog.view.TextProgressBar;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * 查看图片的View没有缓存的View
 * 调用loadLargeBitmap(),实现加载大图.
 * 调用loadThumb(),加载小图.
 * 加载方式与顺序:
 * 1.设置Bean.
 * 2.根据当前的类型,加载大图或小图.mImageShowType.
 * 2.1 加载大图.计算路径,key.获取文件,如果存在,解码,显示大图.如果不存在,先加载小图.显示小图,下载.
 * 这里存在一种状况,如果正在下载,也是存在文件的.所以,需要检测是否正在下载.
 * 2.2 加载小图.计算key,获取缓存图片.显示小图.如果不存在,不显示.
 * 当移出视线范围时,应该加载小图,依然保持下载线程.而销毁时,取消下载线程,且删除未下载完成的文件.
 *
 * @author: archko 12-6-24
 */
public class AKSnapImageView extends LinearLayout implements View.OnClickListener {

    private static final String TAG="AKSnapImageView";

    private static final int MSG_PROGRESS=1;
    private static final int MSG_MAX_PROGESS=2;
    private static final int MSG_FAILED=3;
    private static final int MSG_SUCCESS=4;

    private static final int CONNECT_TIMEOUT=80000;
    private static final int READ_TIMEOUT=240000;

    private static final int IMG_SHOW_TYPE_THUMB=1;
    private static final int IMG_SHOW_TYPE_ORI=2;
    private static final int IMG_TYPE_PNG=1;
    private static final int IMG_TYPE_GIF=2;

    protected Context mContext;
    protected String mCacheDir;    //图片缓存目录

    private ImageBean mImageBean;
    private View mWebViewParent;
    //private MyWebView myWebView;
    GifImageView mGifImageView;
    private PhotoView imageView;
    private TextProgressBar textProgressBar;
    private UploadHandler mUploadHandler;
    /**
     * 是否下载完成了.用于保存数据用的.
     */
    private boolean mImageDownloaded=false;
    private int mImageShowType=IMG_SHOW_TYPE_THUMB;

    public AKSnapImageView(Context context, ImageBean bean) {
        super(context);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.imageviewer_all, this);
        mWebViewParent=findViewById(R.id.lay_webview_parent);
        //myWebView=(MyWebView) findViewById(R.id.webview);
        mGifImageView=(GifImageView) findViewById(R.id.gifview);
        imageView=(PhotoView) findViewById(R.id.imageview);
        textProgressBar=(TextProgressBar) findViewById(R.id.progress_bar);

        imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                close();
            }
        });
        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                close();
            }
        });
        mGifImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        mContext=context;

        setOnClickListener(this);
        mImageBean=bean;
    }

    public boolean isImageDownloaded() {
        return mImageDownloaded;
    }

    public ImageBean getImageBean() {
        return mImageBean;
    }

    public void update(ImageBean bean) {
        WeiboLog.v(TAG, "update:"+bean);
        if (null==bean||TextUtils.isEmpty(bean.thumb)) {
            WeiboLog.d(TAG, "TextUtils.isEmpty(bean)."+bean);
            return;
        }

        mImageBean=bean;
    }

    /**
     * 启动加载缩略图模式
     */
    public void loadThumb() {
        mImageShowType=IMG_SHOW_TYPE_THUMB;
        WeiboLog.v(TAG, "loadThumb:"+mImageBean);

        if (imageView.getVisibility()==GONE) {
            imageView.setVisibility(VISIBLE);
        }

        if (mWebViewParent.getVisibility()==VISIBLE) {
            mWebViewParent.setVisibility(GONE);
        }

        Bitmap bitmap;
        bitmap=ImageCache.getInstance(mContext).getCachedBitmap(mImageBean.thumb);
        if (null!=bitmap) {
            if (ImageView.ScaleType.FIT_CENTER!=imageView.getScaleType()) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);
        }
    }

    /**
     * 启动加载大图模式
     */
    public void loadLargeBitmap() {
        mImageShowType=IMG_SHOW_TYPE_ORI;
        //WeiboLog.v(TAG, "loadLargeBitmap:"+mImageBean);
        String thumb=mImageBean.thumb;
        Scheme scheme=Scheme.ofUri(thumb);
        if (scheme==Scheme.FILE) {
            File file=new File(thumb);
            if (file.exists()) {
                if (thumb.endsWith("gif")) {
                    loadWebview(thumb);
                } else {
                    loadImageView(thumb);
                }
            }
            return;
        }

        textProgressBar.setVisibility(View.VISIBLE);
        loadView();
    }

    /**
     * 释放所有的资源.但不关闭Activity
     */
    public void release() {
        /*if (null!=myWebView) {
            removeAllViews();
            //removeView(myWebView);  //remove it first,dettach view, and close.
            myWebView.destroy();
        }*/
        if (null!=mGifImageView) {
            mGifImageView.setImageDrawable(null);
        }

        if (null!=imageView) {
            imageView.setImageBitmap(null);
        }
    }

    /**
     * 关闭Activity
     */
    public void close() {
        release();

        if (mContext instanceof Activity) {
            Activity activity=(Activity) mContext;
            activity.finish();
        }
    }

    public void stopDownload() {
        if (null!=mUploadHandler) {
            mUploadHandler.removeCallbacksAndMessages(null);
            mUploadHandler.getLooper().quit();
            mUploadHandler=null;
        }
    }

    /**
     * 加载图片布局.仅用于本地图片.
     *
     * @param bean
     */
    private void loadView() {
        String bean=mImageBean.thumb;
        SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean showOriginal=mPrefs.getBoolean(PrefsActivity.PREF_IMAGEVIEWER, true);
        String url;
        if (!showOriginal) {
            url=mImageBean.thumb.replace("thumbnail", "bmiddle");
        } else {
            url=mImageBean.thumb.replace("thumbnail", "large");
        }
        if (bean.endsWith("gif")) {
            loadWebview(url);
        } else {
            loadImageView(url);
        }
    }

    @Override
    public void onClick(View v) {
        close();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    /**
     * 加载大图
     *
     * @param url
     */
    private void loadImageView(String url) {
        mImageDownloaded=false;

        Bitmap bitmap;
        bitmap=ImageCache.getInstance(mContext).getCachedBitmap(mImageBean.thumb);

        if (null==bitmap) {
            bitmap=ImageCache.getInstance(mContext).getCachedBitmap(url);
        }

        if (null!=bitmap&&!bitmap.isRecycled()) {
            int screenHeight=getHeight();
            WeiboLog.v(TAG, "loadImageView:"+screenHeight+" bheight:"+bitmap.getHeight());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(bitmap);
        }

        WeiboLog.d(TAG, "loadImageView:"+" bmid:"+url+" bitmap:"+bitmap);
        if (!TextUtils.isEmpty(url)) {
            downloadImage();
        }
    }

    /**
     * 加载gif大图
     *
     * @param url
     */
    private void loadWebview(String url) {
        mImageDownloaded=false;
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        WeiboLog.v(TAG, "loadWebview:"+" bmid:"+url);
        if (!TextUtils.isEmpty(url)) {
            downloadImage();
        }
    }

    private void downloadImage() {
        textProgressBar.setProgress(0);
        if (null!=mUploadHandler) {
            mUploadHandler.removeCallbacksAndMessages(null);
            mUploadHandler.sendEmptyMessage(111);
            mUploadHandler.getLooper().quit();
        } else {
            //mUploadHandler.setAkSnapImageView(new WeakReference<AKSnapImageView>(this));
        }
        final HandlerThread uploadHandler=new HandlerThread("UploadHandler",
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
        uploadHandler.start();
        mUploadHandler=new UploadHandler(uploadHandler.getLooper(), new WeakReference<AKSnapImageView>(this));
        mUploadHandler.sendEmptyMessage(0);

    }

    private void setMeasureSpec(MyWebView webView, int w, int h) {
        Display display=((Activity) mContext).getWindowManager().getDefaultDisplay();
        DisplayMetrics dm=new DisplayMetrics();
        display.getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        if (w>width) {
            h=(int) ((float) width/(float) w*h);
            w=width;
        }
        webView.setMeasureSpec(w, h);
    }

    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleViewMessage(msg);
        }
    };

    private void handleViewMessage(Message msg) {
        try {
            int what=msg.what;
            switch (what) {
                case MSG_PROGRESS: {
                    Integer progress=(Integer) msg.obj;
                    textProgressBar.setProgress(progress);
                    break;
                }

                case MSG_MAX_PROGESS: {
                    Integer progress=(Integer) msg.obj;
                    textProgressBar.setMax(progress);
                    break;
                }

                case MSG_FAILED:
                    textProgressBar.setText("下载失败");
                    break;

                case MSG_SUCCESS:
                    updateBitmap(msg);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBitmap(Message msg) {
        WeiboLog.w(TAG, "updateBitmap."+mImageShowType);
        mImageDownloaded=true;
        textProgressBar.setVisibility(View.GONE);
        if (mImageShowType==IMG_SHOW_TYPE_THUMB) {
            WeiboLog.d(TAG, "only show thumbs.");
            return;
        }
        int imgType=msg.arg1;
        if (imgType==IMG_TYPE_PNG) {
            showPNG((Bitmap) msg.obj);
        } else {
            showGif((String) msg.obj);
        }
    }

    void showPNG(Bitmap bitmap) {
        WeiboLog.d(TAG, "loadImageview:"+mImageBean);
        mWebViewParent.setVisibility(GONE);
        if (null!=imageView) {
            if (null!=bitmap) {
                int screenHeight=getHeight();
                WeiboLog.v(TAG, "width："+bitmap.getWidth()+" height:"+bitmap.getHeight()+" screenHeight:"+screenHeight);
                try {
                    if (screenHeight<bitmap.getHeight()) {
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
                    } else {
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        }
    }

    void showGif(String path) {
        WeiboLog.d(TAG, "showGif:"+mImageBean);
        /*File file=new File(path);
        if (null!=myWebView&&null!=file&&file.exists()) {
            try {
                mWebViewParent.setVisibility(VISIBLE);
                BitmapFactory.Options opts=new BitmapFactory.Options();
                opts.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                setMeasureSpec(myWebView, DisplayUtils.convertDpToPx(opts.outWidth), DisplayUtils.convertDpToPx(opts.outHeight));
                myWebView.loadUrl("file://"+file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        File file=new File(path);
        if (null!=mGifImageView&&null!=file&&file.exists()) {
            try {
                mWebViewParent.setVisibility(VISIBLE);
                GifDrawable drawable=new GifDrawable(file.getAbsolutePath());
                mGifImageView.setImageDrawable(drawable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imageView.setVisibility(GONE);
    }

    class UploadHandler extends Handler {

        WeakReference<AKSnapImageView> akSnapImageView;
        String url;
        String path;
        private boolean mShouldDownloadImage=true;
        boolean isRunning=false;

        public UploadHandler(final Looper looper, WeakReference<AKSnapImageView> akSnapImageView) {
            super(looper);
            this.akSnapImageView=akSnapImageView;
            initImageParams();
        }

        private void initImageParams() {
            ImageBean bean=mImageBean;
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean showOriginal=mPrefs.getBoolean(PrefsActivity.PREF_IMAGEVIEWER, true);
            String bmiddlePic;
            if (!showOriginal) {
                bmiddlePic=bean.thumb.replace("thumbnail", "bmiddle");
            } else {
                bmiddlePic=bean.thumb.replace("thumbnail", "large");
            }

            String dir=App.mCacheDir+Constants.PICTURE_DIR;
            if (bean.thumb.endsWith("gif")) {
                dir=App.mCacheDir+Constants.GIF;
            }

            String name=WeiboUtils.getWeiboUtil().getMd5(bean.thumb)+WeiboUtils.getExt(bean.thumb);
            name=WeiboUtils.getWeiboUtil().getMd5(bmiddlePic)+WeiboUtils.getExt(bmiddlePic);
            path=dir+name;
            url=bmiddlePic;
        }

        public UploadHandler(final Looper looper, WeakReference<AKSnapImageView> akSnapImageView, String url, String path) {
            super(looper);
            this.akSnapImageView=akSnapImageView;
            this.url=url;
            this.path=path;
        }

        public void setAkSnapImageView(WeakReference<AKSnapImageView> akSnapImageView) {
            this.akSnapImageView=akSnapImageView;
        }

        public void run() {
            //WeiboLog.v(TAG, "DownloadThread: path:"+path+" bmid:"+url+" thumb:"+mImageBean);
            final File file=new File(path);
            if (file.exists()) {
                //WeiboLog.v(TAG, "already exists.");
                downloadSuccess(file);
                isRunning=false;
                return;
            }

            //download image and save
            //return result true or false.
            boolean result=downloadFileByOKHttp(url, file);
            if (result) {
                downloadSuccess(file);
            } else {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg;
                msg=Message.obtain();
                msg.what=MSG_FAILED;
                sendProgressMessage(msg);
            }
            isRunning=false;
        }

        private void downloadSuccess(File file) {
            //WeiboLog.v(TAG, "downloadSuccess");
            Message msg;
            msg=Message.obtain();
            int imgType=url.endsWith("gif") ? IMG_TYPE_GIF : IMG_TYPE_PNG;
            msg.arg1=imgType;
            Bitmap bitmap=null;
            if (imgType==IMG_TYPE_PNG) {
                bitmap=null;
                try {
                    bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(file.getAbsolutePath());
                } catch (OutOfMemoryError e) {
                    System.gc();
                    e.printStackTrace();
                }
                msg.obj=bitmap;
            } else {
                msg.obj=path;
            }
            msg.what=MSG_SUCCESS;
            sendProgressMessage(msg);
        }

        @Override
        public void handleMessage(Message msg) {
            int what=msg.what;
            if (what==111) {
                if (null!=path) {
                    File file=new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } else if (what==0) {
                if (!isRunning) {
                    isRunning=true;
                    run();
                }
            }
        }

        public boolean downloadFile(String downloadUrl, File saveFilePath) {
            //WeiboLog.v(TAG, "downloadFile");
            int fileSize=-1;
            int downFileSize=0;
            boolean result=false;
            int progress=0;

            try {
                URL url=new URL(downloadUrl);
                HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                if (null==conn) {
                    try {
                        saveFilePath.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    WeiboLog.v(TAG, "no connection.");
                    return false;
                }

                // 读取超时时间 毫秒级
                try {
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setConnectTimeout(CONNECT_TIMEOUT);
                    conn.setRequestMethod("GET");
                    //conn.setRequestProperty("Connection", "Keep-Alive");
                    //conn.setRequestProperty("User-Agent", BaseApi.USERAGENT);
                    conn.setDoInput(true);
                    conn.connect();
                    if (conn.getResponseCode()==HttpURLConnection.HTTP_OK) {
                        fileSize=conn.getContentLength();
                        InputStream is=new BufferedInputStream(conn.getInputStream());//conn.getInputStream();
                        FileOutputStream fos=new FileOutputStream(saveFilePath);
                        byte[] buffer=new byte[2048];
                        int i=0;
                        Message msg;

                        msg=Message.obtain();
                        msg.what=MSG_MAX_PROGESS;
                        msg.obj=fileSize;

                        while ((i=is.read(buffer))!=-1) {
                            if (!mShouldDownloadImage) {
                                try {
                                    saveFilePath.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                WeiboLog.v(TAG, "stop download.");
                                return false;
                            }

                            downFileSize=downFileSize+i;
                            // 下载进度
                            progress=(int) (downFileSize*50.0/fileSize);
                            fos.write(buffer, 0, i);

                            msg=Message.obtain();
                            msg.what=MSG_PROGRESS;
                            msg.obj=progress*2;
                            sendProgressMessage(msg);
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        WeiboLog.d(TAG, "下载完成."+fileSize);
                        result=true;
                    } else {
                        WeiboLog.d(TAG, "code:"+conn.getResponseCode()+" message:"+conn.getResponseMessage());
                        result=false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    result=false;
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                result=false;
                WeiboLog.e(TAG, "downloadFile catch Exception:", e);
            }
            return result;
        }

        public boolean downloadFileByOKHttp(String downloadUrl, File saveFilePath) {
            //WeiboLog.v(TAG, "downloadFileByOKHttp");
            long fileSize=-1;
            int downFileSize=0;
            boolean result=false;
            int progress=0;

            try {
                //URL url=new URL(downloadUrl);
                //HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                OkHttpClient client=new OkHttpClient();
                Request.Builder builder=new Request.Builder();
                builder.url(url);
                client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); // connect timeout
                client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);    // socket timeout

                Request request=builder.build();
                Response response=client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    //System.out.println("downloadFailed:"+urlString);
                    try {
                        saveFilePath.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    WeiboLog.v(TAG, "no connection.");
                    return false;
                }

                // 读取超时时间 毫秒级
                try {
                    ResponseBody body=response.body();
                    fileSize=body.contentLength();
                    InputStream is=new BufferedInputStream(body.byteStream());//conn.getInputStream();
                    FileOutputStream fos=new FileOutputStream(saveFilePath);
                    byte[] buffer=new byte[2048];
                    int i=0;
                    Message msg;

                    msg=Message.obtain();
                    msg.what=MSG_MAX_PROGESS;
                    msg.obj=fileSize;

                    while ((i=is.read(buffer))!=-1) {
                        if (!mShouldDownloadImage) {
                            try {
                                saveFilePath.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            WeiboLog.v(TAG, "stop download.");
                            return false;
                        }

                        downFileSize=downFileSize+i;
                        // 下载进度
                        progress=(int) (downFileSize*50.0/fileSize);
                        fos.write(buffer, 0, i);

                        msg=Message.obtain();
                        msg.what=MSG_PROGRESS;
                        msg.obj=progress*2;
                        sendProgressMessage(msg);
                    }
                    fos.flush();
                    fos.close();
                    is.close();

                    WeiboLog.d(TAG, "下载完成."+fileSize);
                    result=true;
                } catch (IOException e) {
                    e.printStackTrace();
                    result=false;
                } finally {
                    //conn.disconnect();
                }
            } catch (Exception e) {
                result=false;
                WeiboLog.e(TAG, "downloadFileByOKHttp catch Exception:", e);
            }
            return result;
        }

        void sendProgressMessage(Message msg) {
            if (null!=akSnapImageView&&akSnapImageView.get()!=null&&null!=mHandler) {
                mHandler.sendMessage(msg);
            } else {
                mShouldDownloadImage=false;
            }
        }
    }

}