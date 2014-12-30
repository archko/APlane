package com.me.microblog.thread;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.WeiboUtils;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.cache.Md5Digest;
import com.me.microblog.core.ImageManager;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 下载图片线程。
 *
 * @author archko
 */
@Deprecated
public class ImageTask extends Thread {

    public static final String TAG="FetchImage";
    private DefaultHttpClient httpClient;
    private HttpGet mHttpGet;
    private Context mContext;
    private Handler mHandler;
    /**
     * 图片的类型，是微博图片，还是转发内容图片或者用户头像等
     */
    private int mType;
    //private String mFilepath;
    private String uri;
    private String dir;
    /**
     * 是否缓存在sdcard中
     */
    private boolean cache;
    public DownloadPiece mPiece;

    /**
     * Default transition drawable fade time
     */
    public static final int FADE_IN_TIME=200;

    /**
     * Default album art
     */
    public Bitmap mDefault;

    /**
     * Default artwork
     */
    private BitmapDrawable mDefaultArtwork;

    /**
     * First layer of the transition drawable
     */
    private ColorDrawable mCurrentDrawable;

    /**
     * Layer drawable used to cross fade the result from the worker
     */
    private Drawable[] mArrayDrawable;

    public boolean isCancled=false;

    public ImageTask(Context context, DefaultHttpClient client, HttpGet httpGet, DownloadPiece piece) {
        this.httpClient=client;
        this.mHttpGet=httpGet;
        this.mType=piece.type;
        this.mHandler=piece.handler;
        this.mContext=context;
        this.uri=piece.uri;
        this.dir=piece.dir;
        this.cache=piece.cache;
        mPiece=piece;
        init(context);
    }

    public final String getDefaultTheme(Context context) {
        SharedPreferences mPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        String themeId=mPreferences.getString("theme", "2");
        return themeId;
    }

    private void init(Context context) {
        int mResId=R.drawable.image_loading_dark;
        if (mPiece.type==Constants.TYPE_PORTRAIT) {
            mResId=R.drawable.user_default_photo;
        } else {
            String themeId=getDefaultTheme(context);
            mResId=R.drawable.image_loading_dark;
            if ("2".equals(themeId)) {
                mResId=R.drawable.image_loading_light;
            }
        }
        mDefault=((BitmapDrawable) context.getResources().getDrawable(mResId)).getBitmap();
        mDefaultArtwork=new BitmapDrawable(context.getResources(), mDefault);
        // No filter and no dither makes things much quicker
        mDefaultArtwork.setFilterBitmap(false);
        mDefaultArtwork.setDither(false);
        mCurrentDrawable=new ColorDrawable(App.getAppContext().getResources().getColor(R.color.transparent));
        // A transparent image (layer 0) and the new result (layer 1)
        mArrayDrawable=new Drawable[2];
        mArrayDrawable[0]=mCurrentDrawable;
    }

    public void startThread() {
        setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        start();
    }

    @Override
    public void run() {
        App app=(App) this.mContext.getApplicationContext();
        HttpResponse response;

        if (ImageCache2.getInstance().isScrolling()||DownloadPoolThread.cancelWork(mPiece)||isCancled) {
            //app.mDownloadPool.ActiveThread_Pop();
            DownloadPoolThread.getDownloadPoolThread().popDownloadQuery(mPiece.uri);
            return;
        }

        try {
            Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(mPiece.uri);
            if (null!=bitmap) {
                SendMessage(mPiece.handler, mPiece, bitmap);
                return;
            }

            String ext= WeiboUtils.getExt(uri);
            String name=Md5Digest.getInstance().getMd5(uri)+ext;
            if (null==name) {
                //app.mDownloadPool.ActiveThread_Pop();
                DownloadPoolThread.getDownloadPoolThread().popDownloadQuery(mPiece.uri);
                return;
            }
            String imagepath=dir+name;

            bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(imagepath, -1);
            if (null!=bitmap) {
                if (!DownloadPoolThread.cancelWork(mPiece)) {
                    SendMessage(mPiece.handler, mPiece, bitmap);
                }
                return;
            }

            if (DownloadPoolThread.cancelWork(mPiece)||isCancled) {
                //app.mDownloadPool.ActiveThread_Pop();
                DownloadPoolThread.getDownloadPoolThread().popDownloadQuery(mPiece.uri);
                return;
            }
            response=httpClient.execute(mHttpGet);
            int code=response.getStatusLine().getStatusCode();
            if (code==200) {
                byte[] bytes=EntityUtils.toByteArray(response.getEntity());
                if (cache) {
                    ImageManager.saveBytesAsFile(bytes, imagepath);
                    //WeiboLog.d(TAG, "需要缓存："+str2);
                    bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(imagepath, -1);
                } else {
                    bitmap=ImageManager.decodeBitmap(bytes, -1);
                }
                //WeiboLog.d(TAG, "cache:"+cache+" uri:"+uri+" length:"+bytes.length+" mFilepath:"+mFilepath+" dir:"+dir+" bitmap:"+bitmap);

                if (DownloadPoolThread.cancelWork(mPiece)) {   //下载过程,如果View已经销毁,不需要返回.
                    return;
                }

                SendMessage(mPiece.handler, mPiece, bitmap);
            } else {
                WeiboLog.w(TAG, "下载图片失败:"+uri);
            }
        } catch (IOException e) {
            WeiboLog.d(TAG, "uri:"+uri+" exception:"+e.toString());
        } finally {
            // 默认把它移出，不再下载。
            //app.mDownloadPool.ActiveThread_Pop();
            DownloadPoolThread.getDownloadPoolThread().popDownloadQuery(mPiece.uri);
            if (null!=mHttpGet) {
                mHttpGet.abort();
            }
        }
    }

    public void SendMessage(Handler handler, final DownloadPiece piece, final Bitmap bitmap) {
        DownloadPoolThread.getDownloadPoolThread().popDownloadQuery(mPiece.uri);
        if (null==piece||null==bitmap||handler==null||isCancled) {
            WeiboLog.d(TAG, "SendMessage,bitmap is null.");
            return;
        }

        //if (!mPiece.isShowLargeBitmap) {   //大图暂时不缓存内存，但是缓存小图
        ImageCache2.getInstance().addBitmapToMemCache(piece.uri, bitmap);
        /*} else {
            LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
            lruCache.put(piece.uri, bitmap);
        }*/

        /*if (handler==null||ImageCache2.getInstance().isScrolling()) {
            WeiboLog.v(TAG, "SendMessage:"+piece+" handler is null:or is scrolling.");
            return;
        }*/
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!DownloadPoolThread.cancelWork(mPiece)) {
                    WeakReference<ImageView> viewWeakReference=mPiece.mImageReference;//DownloadPoolThread.downloading.get(uri);
                    ImageView view=(ImageView) viewWeakReference.get();
                    if (null!=view) {
                        //WeiboLog.v(TAG, "SendMessage "+uri);
                        //view.setImageBitmap(bitmap);
                        setBitmap(view, bitmap, piece.type);
                    } else {
                        WeiboLog.v(TAG, "SendMessage view is null:"+piece);
                    }
                } else {
                    WeiboLog.d(TAG, "SendMessage,cancel work:"+piece);
                }
            }

            private void setBitmap(ImageView view, Bitmap bitmap, int type) {
                if (type==Constants.TYPE_PORTRAIT) {
                    view.setImageBitmap(bitmap);
                } else {
                    final BitmapDrawable layerTwo=new BitmapDrawable(App.getAppContext().getResources(), bitmap);
                    /*layerTwo.setFilterBitmap(false);
                    layerTwo.setDither(false);
                    mArrayDrawable[1]=layerTwo;

                    // Finally, return the image
                    final TransitionDrawable result=new TransitionDrawable(mArrayDrawable);
                    result.setCrossFadeEnabled(true);
                    result.startTransition(FADE_IN_TIME);*/
                    view.setImageDrawable(layerTwo);
                }
            }
        });
    }

    @Deprecated
    public static void SendMessage(Handler handler, int what, Bundle bundle, String uri) {
        if (handler==null) {
            return;
        }
        Message message=Message.obtain();//new Message();
        message.what=what;
        message.obj=uri;
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public void cancel(boolean b) {
        isCancled=true;
    }
}
