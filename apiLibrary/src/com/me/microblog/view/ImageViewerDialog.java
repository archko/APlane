package com.me.microblog.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.andrew.apollo.cache.ImageCache;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.WeiboUtils;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.ImageManager;
import com.me.microblog.util.Constants;
import com.me.microblog.util.StreamUtils;
import com.me.microblog.util.WeiboLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @description:
 * @author: archko 11-9-22
 */
@Deprecated
public class ImageViewerDialog extends AlertDialog {

    public static final String TAG = "ImageViewerDialog";
    private static Drawable defaultBackground = null;
    private Context mContext;
    private boolean finishPicLoad;
    private LayoutInflater mInflater;
    private ImageView mImage;
    private LinearLayout mLayout;
    private AsyncTask<Void, Void, Uri> loadPicTask;
    public boolean loadPictureRunning = false;
    private String mSaveDir; //目前不是存在这个目录的。
    private TextProgressBar mProgressBar;
    private String uri;
    //private GifView2 gifView;//gif
    MyWebView mWebView;
    private TextView mTxt;//显示帮助文字,下载或解析
    private TextView mPathView; //保存的文本路径
    private int imageType = 0;    //图像类型,gif还是其它
    private ImageView mSave;//保存
    String mPath;   //文件路径,如果存在就直接保存,
    boolean mShouldDownloadImage = true; //是否需要下载
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Integer progress = (Integer) msg.obj;
            switch (what) {
                case 1:
                    mProgressBar.setProgress(progress);
                    break;

                case 2:
                    mProgressBar.setMax(progress);
                    break;

                case 3:
                    mProgressBar.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private IImageLoadCallback imageLoadCallback = new IImageLoadCallback() {

        @Override
        public void loadError() {
            ImageViewerDialog.this.mProgressBar.setVisibility(View.GONE);
            //Toast.makeText(mContext, "gif图片解析失败！", Toast.LENGTH_SHORT).show();
            mTxt.setText("gif图片解析失败！");
            WeiboLog.d(TAG, "gif图片解析失败！");
            //gifView.stopAnimate();
        }

        @Override
        public void loadFinish() {
            mTxt.setVisibility(View.GONE);
            ImageViewerDialog.this.mProgressBar.setVisibility(View.GONE);
            mSave.setVisibility(View.VISIBLE);
        }
    };

    public interface IImageLoadCallback {

        void loadError();

        void loadFinish();
    }

    /**
     * 设置默认图片
     *
     * @param url
     */
    void setDefaultBackground(String url) {
    }

    public ImageViewerDialog(Context ctx, String imageUrl, String saveDir, Drawable d, String thumb) {
        super(ctx);
        this.mContext = ctx;
        this.mSaveDir = saveDir;
        this.imageType = imageUrl.endsWith("gif") ? Constants.GIF_TYPE : Constants.PNG_TYPE;
        this.uri = imageUrl;

        this.mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mLayout = (LinearLayout) this.mInflater.inflate(R.layout.imageviewerdialog, null);
        this.mImage = (ImageView) this.mLayout.findViewById(R.id.ivImage);
        this.mImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View paramView) {
                ImageViewerDialog.this.dismiss();

                mShouldDownloadImage = false;
            }
        });
        this.mTxt = (TextView) mLayout.findViewById(R.id.txt);
        this.mPathView = (TextView) mLayout.findViewById(R.id.path_txt);
        this.mSave = (ImageView) mLayout.findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
        this.mProgressBar = (TextProgressBar) this.mLayout.findViewById(R.id.progress_bar);
        /*this.gifView=(GifView2) mLayout.findViewById(R.id.gif_view);
        gifView.setImageLoadCallback(imageLoadCallback);*/
        mWebView = (MyWebView) mLayout.findViewById(R.id.webview);
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE || motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (null != ImageViewerDialog.this.getWindow().getDecorView()
                        && ImageViewerDialog.this.getWindow().getDecorView().isShown()) {
                        ImageViewerDialog.this.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });

        loadPictureRunning = false;
        mShouldDownloadImage = true;

        //setDefaultBackground(thumb);
    }

    /**
     * 保存图片到文件系统。
     */
    private void saveImage() {
        if (! Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.image_sdcard_not_mount, Toast.LENGTH_SHORT).show();
            return;
        }

        String dir = Environment.getExternalStorageDirectory().getPath() + "/";//((App) mContext.getApplicationContext()).mCacheDir;
        /*if (imageType==WeiboUtil.GIF_TYPE) {
            dir+=WeiboUtil.GIF;
        } else {
            dir+=WeiboUtil.PICTURE_DIR;
        }*/
        String targetFilePath = dir + System.currentTimeMillis() + WeiboUtils.getExt(uri);

        /*String source=mSaveDir;
        if (imageType==Constants.GIF_TYPE) {
            source+=Constants.GIF;
        } else {
            source+=Constants.PICTURE_DIR;
        }source+WeiboUtil.getWeiboUtil().getMd5(uri)+WeiboUtil.getExt(uri)*/
        boolean flag = StreamUtils.copyFileToFile(targetFilePath, mPath);
        WeiboLog.d(TAG, "保存图片:" + targetFilePath);
        mSave.setVisibility(View.GONE);
        if (flag) {
            Toast.makeText(mContext, String.format(mContext.getString(R.string.image_save_suc), targetFilePath),
                Toast.LENGTH_LONG).show();
            mPathView.setText(mPath);
        } else {
            mTxt.setVisibility(View.VISIBLE);
            mTxt.setText(R.string.image_save_failed);
        }
    }

    private Drawable getLoadingBackground() {
        if (defaultBackground == null) {
            /*Bitmap localBitmap = BitmapLoader.getBitmap(this.mContext.getResources(), 2130838029);
            defaultBackground = new BitmapDrawable(localBitmap);*/
        }
        return defaultBackground;
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        WeiboLog.d(TAG, "onCreate.");
        setContentView(mLayout);

        downloadImage();
    }

    DownloadThread mDownloadThread;

    private void downloadImage() {
        mWebView.setVisibility(View.GONE);
        if (! loadPictureRunning) {
            mProgressBar.setVisibility(View.VISIBLE);
            mDownloadThread = new DownloadThread();
            mDownloadThread.start();
        } else {
            Toast.makeText(mContext, "正在下载图片，请稍后...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        WeiboLog.d(TAG, "onDetachedFromWindow");
        //gifView.stopAnimate();
        mWebView.setVisibility(View.GONE);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        WeiboLog.d(TAG, "dismiss.");
        mShouldDownloadImage = false;
        //gifView.stopAnimate();
        mWebView.setVisibility(View.GONE);
        mPath = null;
    }

    //////////////////------------------
    class DownloadThread extends Thread {

        @Override
        public void run() {
            loadPictureRunning = true;
            //get bitmap from sys.
            String dir = mSaveDir + Constants.PICTURE_DIR;
            if (imageType == Constants.GIF_TYPE) {
                dir = mSaveDir + Constants.GIF;
            }

            //identity folder and file.
            final String name = WeiboUtils.getWeiboUtil().getMd5(uri) + WeiboUtils.getExt(uri);
            String path = dir + name;
            mPath = path;
            WeiboLog.d(TAG, "DownloadThread.path:" + path);
            final File file = new File(path);
            if (file.exists()) {
                showImage(file);
                return;
            }

            //download image and save
            //return result true or false.
            boolean result = downloadFile(uri, file);
            if (result) {
                showImage(file);
            } else {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mTxt.setVisibility(View.VISIBLE);
                        mTxt.setText("下载图片失败...");
                    }
                });
            }
            loadPictureRunning = false;
        }
    }

    void showImage(final File file) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                ImageViewerDialog.this.mProgressBar.setVisibility(View.GONE);
                mTxt.setVisibility(View.GONE);
                if (imageType == Constants.PNG_TYPE) {
                    ImageViewerDialog.this.mProgressBar.setVisibility(View.GONE);
                    //gifView.setVisibility(View.GONE);
                    mImage.setVisibility(View.VISIBLE);
                    mSave.setVisibility(View.VISIBLE);
                    ImageViewerDialog.this.mLayout.setBackgroundResource(R.drawable.image_loading_light);
                    Bitmap bitmap = null;
                    try {
                        bitmap = new ImageManager().loadFullBitmapFromSys(file.getAbsolutePath(), - 1);
                    } catch (OutOfMemoryError e) {
                        System.gc();
                        e.printStackTrace();
                        Toast.makeText(ImageViewerDialog.this.mContext, R.string.out_memory, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (null != bitmap) {
                        WeiboLog.d(TAG, "width：" + bitmap.getWidth() + " height:" + bitmap.getHeight());
                        mImage.setImageBitmap(bitmap);
                    } else {
                        try {
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    ImageViewerDialog.this.finishPicLoad = true;
                } else {
                    /*InputStream is=null;
                    try {
                        is=new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    mTxt.setText("开始解析gif图片...");*/
                    mImage.setVisibility(View.GONE);
                    String url = "file://" + file.getAbsolutePath();
                    WeiboLog.d("url:" + url);
                    WebSettings websettings = mWebView.getSettings();
                    android.webkit.WebSettings.LayoutAlgorithm layoutalgorithm = android.webkit.WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
                    websettings.setLayoutAlgorithm(layoutalgorithm);
                    //mWebView.setMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mWebView.loadUrl(url);
                    //setMeasureSpec(80, 100);
                    mWebView.setVisibility(View.VISIBLE);

                    /*gifView.setVisibility(View.VISIBLE);
                    gifView.setKeepScreenOn(true);
                    mImage.setVisibility(View.GONE);
                    boolean flag=App.showMemory();*/
                    /*if(!flag){
                        Toast.makeText(mContext, "您的手机内存小于512m，不适合查看gif图片。",Toast.LENGTH_SHORT).show();
                    }*/
                    //gifView.setGifImage(is, flag);
                }
            }
        });
    }

    void setMeasureSpec(int i1, int j1) {
        int k1 = (int) getContext().getResources().getDimension(R.dimen.image_dialog_max_width);
        int l1 = (int) getContext().getResources().getDimension(R.dimen.image_dialog_max_height);
        if (i1 > k1) {
            int i2 = (j1 * k1) / i1;
            if (i2 > l1) {
                int j2 = (i1 * l1) / j1;
                mWebView.setMeasureSpec(j2, l1);
                return;
            } else {
                mWebView.setMeasureSpec(k1, i2);
                return;
            }
        }

        if (j1 > l1) {
            int k2 = (i1 * l1) / j1;
            mWebView.setMeasureSpec(k2, l1);
            return;
        } else {
            mWebView.setMeasureSpec(i1, j1);
            return;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.clearCache(true);
            mWebView.destroy();
        }
    }

    public boolean downloadFile(String downloadUrl, File saveFilePath) {
        int fileSize = - 1;
        int downFileSize = 0;
        boolean result = false;
        int progress = 0;

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (null == conn) {
                try {
                    saveFilePath.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            // 读取超时时间 毫秒级  
            try {
                conn.setReadTimeout(180000);
                conn.setConnectTimeout(6000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent", AbsApiImpl.USERAGENT);
                conn.setDoInput(true);
                conn.connect();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    fileSize = conn.getContentLength();
                    InputStream is = new BufferedInputStream(conn.getInputStream());//conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(saveFilePath);
                    byte[] buffer = new byte[ 2048 ];
                    int i = 0;
                    Message msg;

                    msg = Message.obtain();
                    msg.what = 2;
                    msg.obj = fileSize;

                    while ((i = is.read(buffer)) != - 1) {
                        if (! mShouldDownloadImage) {
                            try {
                                saveFilePath.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return false;
                        }

                        downFileSize = downFileSize + i;
                        // 下载进度
                        progress = (int) (downFileSize * 100.0 / fileSize);
                        fos.write(buffer, 0, i);

                        msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = progress;
                        mHandler.sendMessage(msg);
                    }
                    fos.flush();
                    fos.close();
                    is.close();

                    msg = Message.obtain();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                    result = true;
                } else {
                    WeiboLog.d(TAG, "code:" + conn.getResponseCode() + " message:" + conn.getResponseMessage());
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            result = false;
            WeiboLog.e(TAG, "downloadFile catch Exception:", e);
        }
        return result;
    }
}