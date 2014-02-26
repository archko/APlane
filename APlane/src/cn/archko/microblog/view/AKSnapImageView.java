package cn.archko.microblog.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.archko.microblog.ui.PrefsActivity;
import com.andrew.apollo.cache.ImageCache;
import com.me.microblog.App;
import cn.archko.microblog.R;
import com.me.microblog.WeiboUtil;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.BaseApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;
import com.me.microblog.view.MyWebView;
import com.me.microblog.view.TextProgressBar;
//import com.nostra13.universalimageloader.core.ImageLoader;
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

/**
 * 查看图片的View没有缓存的View,抓拍的
 *
 * @author: archko 12-6-24
 */
public class AKSnapImageView extends LinearLayout implements View.OnClickListener {

    private static final String TAG="AKSnapImageView";
    private String bmiddlePic;
    private String mThumbPath;   //缩略存储路径.
    private String mBmidPath;   //缩略存储路径.
    protected Context mContext;
    protected String mCacheDir;    //图片缓存目录

    String imageBean;
    MyWebView myWebView;
    PhotoView imageView;
    TextProgressBar textProgressBar;
    public boolean loadPictureRunning=false;
    DownloadThread mDownloadThread;
    boolean mShouldDownloadImage=true; //是否需要下载
    /**
     * 是否下载完成了.用于保存数据用的.
     */
    boolean mImageDownloaded=false;
    int mBitmapType=0;

    public AKSnapImageView(Context context, String bean) {
        super(context);
        //((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.scroll_gif_view, this);

        mContext=context;
        //update(bean);

        setOnClickListener(this);
    }

    public boolean isImageDownloaded() {
        return mImageDownloaded;
    }

    public String getBmidPath() {
        return mBmidPath;
    }

    public void update(String bean) {
        WeiboLog.v(TAG, "update:"+bean);
        if (TextUtils.isEmpty(bean)) {
            WeiboLog.d(TAG, "TextUtils.isEmpty(bean)."+bean);
            return;
        }

        if (!bean.startsWith("http")) {
            File file=new File(bean);
            if (file.exists()) {
                mBmidPath=bean;
                loadView(bean);
                updateBitmap(null);
            }
        }

        if (null!=imageBean&&imageBean.equals(bean)) {
            return;
        }

        imageBean=bean;

        SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean showOriginal=mPrefs.getBoolean(PrefsActivity.PREF_IMAGEVIEWER, true);
        if (!showOriginal) {
            bmiddlePic=bean.replace("thumbnail", "bmiddle");
        } else {
            bmiddlePic=bean.replace("thumbnail", "large");
        }

        String dir=App.mCacheDir+Constants.PICTURE_DIR;
        if (bean.endsWith("gif")) {
            dir=App.mCacheDir+Constants.GIF;
        }

        String name=WeiboUtil.getWeiboUtil().getMd5(bean)+WeiboUtil.getExt(bean);
        mThumbPath=dir+name;

        name=WeiboUtil.getWeiboUtil().getMd5(bmiddlePic)+WeiboUtil.getExt(bmiddlePic);
        mBmidPath=dir+name;

        //loadView(bean);
    }

    /**
     * 启动加载缩略图模式
     */
    public void loadThumb() {
        mBitmapType=0;
        WeiboLog.v(TAG, "loadThumb:"+imageBean);

        stopDownload();

        removeAllViews();

        addImageView();
        textProgressBar.setVisibility(View.GONE);

        Bitmap bitmap;
        //ImageCache2.getInstance().getBitmapFromMemCache(imageBean);
        //bitmap=ImageLoader.getInstance().getMemoryCache().get(imageBean);
        bitmap=ImageCache.getInstance(mContext).getCachedBitmap(imageBean);
        if (null!=bitmap) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);
        }
    }

    private void stopDownload() {
        mShouldDownloadImage=false;
    }

    /**
     * 启动加载大图模式
     */
    public void loadLargeBitmap() {
        mBitmapType=1;
        mShouldDownloadImage=true;
        WeiboLog.v(TAG, "loadLargeBitmap:"+bmiddlePic);

        removeAllViews();

        if (imageBean.endsWith("gif")) {
            addWebView();
            textProgressBar.setVisibility(View.VISIBLE);
            loadWebview();
        } else {
            addImageView();
            textProgressBar.setVisibility(View.VISIBLE);

            loadImageView();
        }
    }

    /**
     * 加载图片布局.仅用于本地图片.
     *
     * @param bean
     */
    private void loadView(String bean) {
        if (getChildCount()>0) {
            removeAllViews();
        }

        if (bean.endsWith("gif")) {
            addWebView();
            loadWebview();
        } else {
            addImageView();
            loadImageView();
        }
    }

    /**
     * 添加webview布局,用于查看gif
     */
    private void addWebView() {
        ((LayoutInflater) mContext.getSystemService("layout_inflater")).inflate(R.layout.imageviewer_gif, this);
        myWebView=(MyWebView) findViewById(R.id.webview);
        textProgressBar=(TextProgressBar) findViewById(R.id.progress_bar);
    }

    /**
     * 添加普通的ImageView,可多点触摸.查看大图与缩略图共用.
     */
    private void addImageView() {
        ((LayoutInflater) mContext.getSystemService("layout_inflater")).inflate(R.layout.imageviewer_png, this);
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
    }

    @Override
    public void onClick(View v) {
        close();
    }

    private void close() {
        if (null!=myWebView) {
            removeView(myWebView);  //remove it first,dettach view, and close.
            myWebView.destroy();
        }

        if (null!=imageView) {
            imageView.setImageBitmap(null);
        }

        if (mContext instanceof Activity) {

            Activity activity=(Activity) mContext;
            activity.finish();
        }
    }

    /**
     * 加载大图
     */
    private void loadImageView() {
        mImageDownloaded=false;

        //Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(imageBean);
        Bitmap bitmap;
        //bitmap=ImageLoader.getInstance().getMemoryCache().get(ImageLoader.getInstance().getKey(imageBean, imageView));
        bitmap=ImageCache.getInstance(mContext).getCachedBitmap(imageBean);

        if (null==bitmap) {
            //bitmap=ImageLoader.getInstance().getMemoryCache().get(ImageLoader.getInstance().getKey(bmiddlePic, imageView));
            bitmap=ImageCache.getInstance(mContext).getCachedBitmap(bmiddlePic);
        }

        if (null!=bitmap&&!bitmap.isRecycled()) {
            int screenHeight=getHeight();
            WeiboLog.v(TAG, "loadImageView:"+screenHeight+" bheight:"+bitmap.getHeight());
            if (screenHeight<bitmap.getHeight()) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            imageView.setImageBitmap(bitmap);
        }

        WeiboLog.d(TAG, "loadImageView:"+loadPictureRunning+" bmid:"+bmiddlePic+" bitmap:"+bitmap);
        if (!TextUtils.isEmpty(bmiddlePic)) {
            downloadImage();
        }
    }

    /**
     * 加载gif大图
     */
    private void loadWebview() {
        mImageDownloaded=false;
        File file=new File(mThumbPath);
        if (file.exists()) {
            WeiboLog.d(TAG, "loadWebview:"+mThumbPath);
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            setMeasureSpec(myWebView, AKUtils.convertDpToPx(opts.outWidth), AKUtils.convertDpToPx(opts.outHeight));
            myWebView.loadUrl("file://"+file.getAbsolutePath());
        }

        WeiboLog.d(TAG, "loadWebview:"+loadPictureRunning+" bmid:"+bmiddlePic);
        if (!TextUtils.isEmpty(bmiddlePic)) {
            downloadImage();
        }
    }

    private void downloadImage() {
        if (!loadPictureRunning) {
            textProgressBar.setProgress(0);
            mDownloadThread=new DownloadThread(new WeakReference<AKSnapImageView>(this));
            mDownloadThread.start();
        } else {
            WeiboLog.d("is downloading...");
        }
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
        //if (w<width&&h<height) {
        webView.setMeasureSpec(w, h);
        //}
    }

    Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                int what=msg.what;
                Integer progress=(Integer) msg.obj;
                switch (what) {
                    case 1:
                        textProgressBar.setProgress(progress);
                        break;

                    case 2:
                        textProgressBar.setMax(progress);
                        break;

                    case 3:
                        textProgressBar.setText("下载失败");
                        break;

                    case 4:
                        updateBitmap(msg);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void updateBitmap(Message msg) {
        WeiboLog.w(TAG, "updateBitmap."+mBitmapType);
        if (mBitmapType==0) {
            return;
        }

        mImageDownloaded=true;
        textProgressBar.setVisibility(View.GONE);
        File file=new File(mBmidPath);
        if (file.exists()) {
            if (null!=mBmidPath&&mBmidPath.endsWith("gif")) {
                WeiboLog.d(TAG, "loadWebview:"+mBmidPath);
                if (null!=myWebView) {
                    try {
                        BitmapFactory.Options opts=new BitmapFactory.Options();
                        opts.inJustDecodeBounds=true;
                        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                        setMeasureSpec(myWebView, AKUtils.convertDpToPx(opts.outWidth), AKUtils.convertDpToPx(opts.outHeight));
                        myWebView.loadUrl("file://"+file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                WeiboLog.d(TAG, "loadImageview:"+mBmidPath);
                if (null!=imageView) {
                    Bitmap bitmap=null;
                    try {
                        bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(file.getAbsolutePath());
                    } catch (OutOfMemoryError e) {
                        System.gc();
                        e.printStackTrace();
                        return;
                    }

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
                        try {
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            WeiboLog.w(TAG, "file not exist.");
        }
    }

    class DownloadThread extends Thread {

        WeakReference<AKSnapImageView> akSnapImageView;

        DownloadThread(WeakReference<AKSnapImageView> akSnapImageView) {
            this.akSnapImageView=akSnapImageView;
        }

        @Override
        public void run() {
            loadPictureRunning=true;
            WeiboLog.v(TAG, "DownloadThread: path:"+mBmidPath+" bmid:"+bmiddlePic+" thumb:"+imageBean);
            final File file=new File(mBmidPath);
            if (file.exists()) {
                Message msg;
                msg=Message.obtain();
                msg.what=4;
                sendMessage(msg);
                loadPictureRunning=false;
                return;
            }

            //download image and save
            //return result true or false.
            boolean result=downloadFile(bmiddlePic, file);
            if (result) {
                Message msg;
                msg=Message.obtain();
                msg.what=4;
                sendMessage(msg);
            } else {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg;
                msg=Message.obtain();
                msg.what=3;
                sendMessage(msg);
            }
            loadPictureRunning=false;
        }

        void sendMessage(Message msg) {
            if (null!=akSnapImageView&&akSnapImageView.get()!=null&&null!=mHandler) {
                mHandler.sendMessage(msg);
            } else {
                mShouldDownloadImage=false;
            }
        }

        public boolean downloadFile(String downloadUrl, File saveFilePath) {
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
                    conn.setReadTimeout(180000);
                    conn.setConnectTimeout(6000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("User-Agent", BaseApi.USERAGENT);
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
                        msg.what=2;
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
                            msg.what=1;
                            msg.obj=progress*2;
                            //mHandler.sendMessage(msg);
                            sendMessage(msg);
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        /*msg=Message.obtain();
                        msg.what=3;
                        //mHandler.sendMessage(msg);
                        sendMessage(msg);*/
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
    }

}