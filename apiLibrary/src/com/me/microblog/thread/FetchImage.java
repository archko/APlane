package com.me.microblog.thread;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import com.me.microblog.WeiboUtil;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.cache.Md5Digest;
import com.me.microblog.core.ImageManager;
import com.me.microblog.util.WeiboLog;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.me.microblog.App;

/**
 * 下载图片线程。
 *
 * @author archko
 */
public class FetchImage extends Thread {

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
    DownloadPool.DownloadPiece mPiece;

    /**
     * @param context
     * @param handler
     * @param client
     * @param httpGet
     * @param type     类型，主要用于获取微博或转发微博的图片
     * @param filepath 图片名字，是md5加密后的
     * @param uri      图片url
     * @param dir      图片存储目录
     */
    public FetchImage(Context context, Handler handler, DefaultHttpClient client, HttpGet httpGet,
        int type, String filepath, String uri, boolean cache, String dir) {
        this.httpClient=client;
        this.mHttpGet=httpGet;
        this.mType=type;
        //this.mFilepath=filepath;
        this.mHandler=handler;
        this.mContext=context;
        this.uri=uri;
        this.dir=dir;
        this.cache=cache;
    }

    public FetchImage(Context context, DefaultHttpClient client, HttpGet httpGet, DownloadPool.DownloadPiece piece) {
        this.httpClient=client;
        this.mHttpGet=httpGet;
        this.mType=piece.type;
        this.mHandler=piece.handler;
        this.mContext=context;
        this.uri=piece.uri;
        this.dir=piece.dir;
        this.cache=piece.cache;
        mPiece=piece;
    }

    @Override
    public void run() {
        App app=(App) this.mContext.getApplicationContext();
        HttpResponse response;
        if (cancelWork()) {
            app.mDownloadPool.ActiveThread_Pop();
            if (null!=mHttpGet) {
                mHttpGet.abort();
            }
            return;
        }

        //造成滚动的延时,卡的感觉.
        /*synchronized (app.mDownloadPool) {
            if (app.mDownloadPool.GetThreadCount()==DownloadPool.MAX_THREAD_COUNT
                &&app.mDownloadPool.GetCount()>9) {
                WeiboLog.d(TAG, "当前的线程数为3,且等待下载的数量大于6,清除数据.");
                app.mDownloadPool.PopPiece();
            }
        }*/

        try {
            Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(mPiece.uri);
            if (null!=bitmap) {
                /*Bundle bundle=new Bundle();
                bundle.putParcelable("name", bitmap);
                FetchImage.SendMessage(mPiece.handler, mPiece.type, bundle, mPiece.uri);*/
                SendMessage(mPiece.handler, mPiece.uri, bitmap);
                return;
            }

            String ext=WeiboUtil.getExt(uri);
            String name=Md5Digest.getInstance().getMd5(uri)+ext;
            if (null==name) {
                app.mDownloadPool.ActiveThread_Pop();
                return;
            }
            String imagepath=dir+name;

            bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(imagepath, -1);
            if (null!=bitmap) {
                if (!cancelWork()) {
                    /*Bundle bundle=new Bundle();
                    bundle.putParcelable("name", bitmap);
                    FetchImage.SendMessage(mHandler, mType, bundle, uri);*/
                    SendMessage(mPiece.handler, mPiece.uri, bitmap);
                }
                return;
            }

            /*HttpParams httpParameters=new BasicHttpParams();
              HttpConnectionParams.setConnectionTimeout(httpParameters, Twitter.CONNECT_TIMEOUT);
              HttpConnectionParams.setSoTimeout(httpParameters, Twitter.READ_TIMEOUT);
              DefaultHttpClient httpClient=new DefaultHttpClient(httpParameters);*/
            if (cancelWork()) {
                app.mDownloadPool.ActiveThread_Pop();
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

                if (cancelWork()) {   //下载过程,如果View已经销毁,不需要返回.
                    return;
                }
                /*Bundle bundle=new Bundle();
                bundle.putParcelable("name", bitmap);
                FetchImage.SendMessage(mHandler, mType, bundle, uri);*/
                SendMessage(mPiece.handler, mPiece.uri, bitmap);
            } else {
                WeiboLog.w(TAG, "下载图片失败:"+uri);
            }
        } catch (IOException e) {
            WeiboLog.d(TAG, "uri:"+uri+" exception:"+e.toString());
        } finally {
            // 默认把它移出，不再下载。
            app.mDownloadPool.ActiveThread_Pop();
            if (null!=mHttpGet) {
                mHttpGet.abort();
            }
        }
    }

    private boolean cancelWork() {
        WeakReference<ImageView> viewWeakReference=mPiece.mImageReference;//DownloadPool.downloading.get(uri);
        if (null==viewWeakReference||viewWeakReference.get()==null) {
            //app.mDownloadPool.ActiveThread_Pop();
            return true;
        }
        return false;
    }

    public void SendMessage(Handler handler, final String uri, final Bitmap bitmap) {
        if (null==uri||null==bitmap) {
            WeiboLog.d(TAG, "SendMessage,bitmap is null.");
            return;
        }

        if (!mPiece.isShowLargeBitmap) {   //大图暂时不缓存内存，但是缓存小图
            ImageCache2.getInstance().addBitmapToMemCache(uri, bitmap);
        } else {
            LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
            lruCache.put(uri, bitmap);
        }

        if (handler==null) {
            WeiboLog.v(TAG, "SendMessage:"+uri+" handler is null:");
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!cancelWork()) {
                    WeakReference<ImageView> viewWeakReference=mPiece.mImageReference;//DownloadPool.downloading.get(uri);
                    ImageView view=(ImageView) viewWeakReference.get();
                    if (null!=view) {
                        //WeiboLog.v(TAG, "SendMessage "+uri);
                        view.setImageBitmap(bitmap);
                        //view.setTag(uri);
                    } else {
                        WeiboLog.v(TAG, "SendMessage view is null:"+uri);
                    }
                } else {
                    WeiboLog.d(TAG, "SendMessage,cancel work:"+uri);
                }
            }
        });
    }

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
}
