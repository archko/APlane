package com.me.microblog.thread;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;
import com.me.microblog.App;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.BaseApi;
import com.me.microblog.util.WeiboLog;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片下载线程池,暂时可允许最多三个线程同时下载。
 *
 * @author archko
 */
public class DownloadPoolThread extends Thread {

    public static final String TAG="DownloadPoolThread";
    public static int MAX_THREAD_COUNT=2;
    //private DefaultHttpClient httpClient;
    private int mActiveThread=0;
    private App mApp;
    private List<DownloadPiece> mQuery;
    private HttpParams params;
    ClientConnectionManager connectionManager;
    private boolean isStop=false;
    public static final int READ_TIMEOUT=24000;
    public static final int CONNECT_TIMEOUT=12000;

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

        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
    }

    private final static SchemeRegistry createRegitry() {
        SchemeRegistry schemeRegistry=new SchemeRegistry();
        // Register the "http" and "https" protocol schemes, they are
        // required by the default operator to look up socket factories.
        SocketFactory sf=PlainSocketFactory.getSocketFactory();
        schemeRegistry.register(new Scheme("http", sf, 80));
        //    sf = SSLSocketFactory.getSocketFactory();

        SSLSocketFactory ssf=SSLSocketFactory.getSocketFactory();
        schemeRegistry.register(new Scheme("https", ssf, 443));

        return schemeRegistry;
    }

    public void setThreadCount(int threadCount) {
        MAX_THREAD_COUNT=threadCount;
    }

    public DownloadPoolThread(App app) {
        this.mQuery=new ArrayList<DownloadPiece>();
        //this.httpClient=new DefaultHttpClient(params);
        this.mApp=app;

        connectionManager=new ThreadSafeClientConnManager(params, createRegitry());
        init();
    }

    public void setStop(boolean stop) {
        synchronized (this) {
            notifyAll();
        }
        isStop=stop;
        if (stop) {
            release();
        }
    }

    public void ActiveThread_Pop() {
        synchronized (this) {
            int i=this.mActiveThread-1;
            this.mActiveThread=i;
            notifyAll();
        }
    }

    public void ActiveThread_Push() {
        synchronized (this) {
            mActiveThread++;
        }
    }

    public DownloadPiece Get(int paramInt) {
        synchronized (this) {
            int size=this.mQuery.size();
            if (paramInt<=size) {
                return mQuery.get(paramInt);
            }
        }
        return null;
    }

    public int GetCount() {
        synchronized (this) {
            return mQuery.size();
        }
    }

    public int GetThreadCount() {
        synchronized (this) {
            return mActiveThread;
        }
    }

    public DownloadPiece Pop() {
        synchronized (this) {
            DownloadPiece downloadPiece=(DownloadPiece) this.mQuery.get(0);
            this.mQuery.remove(0);
            return downloadPiece;
        }
    }

    /**
     * 清除未下载的列表,只保留5个等待队列.
     */
    public void PopPiece() {
        synchronized (this) {
            int size=mQuery.size();
            mQuery=mQuery.subList(size-8, size);
        }
    }

    /**
     * 清除队列所有的任务，用于主页的清除按钮，
     */
    public void cleanAllQuery() {
        synchronized (this) {
            mQuery.clear();
            //downloading.clear();
            notifyAll();
        }
    }

    /**
     * 添加下载的url
     *
     * @param handler   回调用的
     * @param uri       图片url
     * @param imageView 显示的View
     * @param dir       图片存储目录，废除，不需要再靠图片的url与目录计算出存储路径，减少运算
     * @param filepath  图片存储的路径，绝对的
     * @param cache     是否缓存
     */
    public void Push(DownloadPiece mpiece) {
        Message msg=Message.obtain();
        msg.obj=mpiece;
        msg.what=0;
        mHandler.sendMessage(msg);
    }

    /**
     * 添加下载的url
     *
     * @param handler   回调用的
     * @param uri       图片url
     * @param type      图片的类型，是微博图片，还是转发内容图片或者用户头像等
     * @param dir       图片存储目录，废除，不需要再靠图片的url与目录计算出存储路径，减少运算
     * @param cache     是否缓存
     * @param imageView 图片视图
     */
    public void Push(Handler handler, String uri, int type, boolean cache, String dir, ImageView imageView) {
        Message msg=Message.obtain();
        DownloadPiece piece=new DownloadPiece(handler, uri, type, cache, dir, false, imageView);
        msg.obj=piece;
        msg.what=0;
        mHandler.sendMessage(msg);
    }

    @Override
    public void run() {
        WeiboLog.d(TAG, "download.");
        while (true) {
            synchronized (this) {
                if (isStop) {
                    WeiboLog.d(TAG, "downloadpool stop.");
                    break;
                }

                notifyAll();
                if ((GetCount()!=0)&&(GetThreadCount()<=MAX_THREAD_COUNT)) {
                    DownloadPiece piece=Pop();
                    FrechImg_Impl(piece);
                } else {
                    //WeiboLog.d(TAG, "wait."+GetThreadCount());
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        release();
    }

    private void FrechImg_Impl(DownloadPiece piece) {
        final String uri=piece.uri;
        //WeiboLog.v(TAG, "FrechImg_Impl:"+uri);
        if (uri==null||piece.dir==null) {
            WeiboLog.w(TAG, "名字不存在。");
            return;
        }

        if (cancelWork(piece)) {
            WeiboLog.i(TAG, "viewWeakRef is null."+uri);
            //downloading.remove(uri);
            return;
        }

        final Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(uri);
        final WeakReference<ImageView> viewWeakReference=piece.mImageReference;
        ImageView imageView=(ImageView) viewWeakReference.get();
        if (null!=bitmap) {
            if (!cancelWork(piece)&&null!=piece.handler) {
                piece.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView view=(ImageView) viewWeakReference.get();
                        if (null!=view) {
                            view.setImageBitmap(bitmap);
                        } else {
                            WeiboLog.v(TAG, "view is null;"+uri);
                        }
                    }
                });
            } else {
                WeiboLog.v(TAG, "null==viewWeakReference:"+uri);
            }
            return;
        } else if (executePotentialWork(piece.uri, viewWeakReference.get())) {
            mApp.mDownloadPool.ActiveThread_Push();
            String str3=Uri.encode(uri, ":/");
            HttpGet httpGet=new HttpGet(str3);
            httpGet.setHeader("User-Agent", BaseApi.USERAGENT);
            DefaultHttpClient httpClient=new DefaultHttpClient(connectionManager, params);
            ImageTask imageTask=new ImageTask(mApp, httpClient, httpGet, piece);
            //final AsyncDrawable asyncDrawable=new AsyncDrawable(imageTask);
            AsyncDrawable asyncDrawable;
            Drawable drawable=imageView.getDrawable();
            if (null==drawable) {
                asyncDrawable=new AsyncDrawable(null, null, imageTask);
            } else {
                asyncDrawable=new AsyncDrawable(null, imageTask.mDefault, imageTask);
            }
            imageView.setImageDrawable(asyncDrawable);
            imageTask.startThread();
        }
    }

    public static final boolean executePotentialWork(final Object data, final ImageView imageView) {
        final ImageTask bitmapWorkerTask=getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask!=null) {
            final Object bitmapData=bitmapWorkerTask.mPiece;
            if (bitmapData==null||!((DownloadPiece) bitmapData).uri.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        return true;
    }

    public static final ImageTask getBitmapWorkerTask(final ImageView imageView) {
        if (imageView!=null) {
            final Drawable drawable=imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable=(AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static final class AsyncDrawable extends BitmapDrawable {

        private final WeakReference<ImageTask> mBitmapWorkerTaskReference;

        /*public AsyncDrawable(final ImageTask mBitmapWorkerTask) {
            super(Color.TRANSPARENT);
            mBitmapWorkerTaskReference=new WeakReference<ImageTask>(mBitmapWorkerTask);
        }*/

        /**
         * Constructor of <code>AsyncDrawable</code>
         */
        public AsyncDrawable(final Resources res, final Bitmap bitmap,
            final ImageTask mBitmapWorkerTask) {
            super(bitmap);
            mBitmapWorkerTaskReference=new WeakReference<ImageTask>(mBitmapWorkerTask);
        }

        /**
         * @return The {@link BitmapWorkerTask} associated with this drawable
         */
        public ImageTask getBitmapWorkerTask() {
            return mBitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelWork(DownloadPiece piece) {
        if (null==piece) {
            return false;
        }
        WeakReference<ImageView> viewWeakReference=piece.mImageReference;//DownloadPool.downloading.get(uri);
        if (null==viewWeakReference||viewWeakReference.get()==null) {
            return true;
        }
        return false;
    }

    //=======================================

    Handler mHandler;
    int count=10;

    private void init() {
        initDecodeThread();
        //this.mHandler.sendEmptyMessage(0);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    private void initDecodeThread() {
        quitLooper();

        WeiboLog.d(TAG, "initDecodeThread:");
        synchronized (this) {
            final Thread previewThread=new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    mHandler=new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            internalhandleMessage(msg);
                        }
                    };
                    looperPrepared();
                    Looper.loop();
                    WeiboLog.d(TAG, "quit.");
                }
            };
            previewThread.start();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void looperPrepared() {
        synchronized (this) {
            try {
                notify();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void internalhandleMessage(Message msg) {
        //Log.d(TAG, "internalhandleMessage:"+msg.what);
        switch (msg.what) {
            default:
                return;
            case 0:
                internalStart(msg);
                return;
            case 1:
                moniter();
                return;
            case 2:
                internalRelease();
                return;
            case 3:
                return;
        }
    }

    private void moniter() {
        return;
    }

    private void internalStart(Message msg) {
        //Log.d(TAG, "internalStart:");
        DownloadPiece mpiece=(DownloadPiece) msg.obj;
        if (TextUtils.isEmpty(mpiece.uri)) {
            WeiboLog.d(TAG, "internalStart uri is null.");
            return;
        }

        synchronized (DownloadPoolThread.this) {   //这里的同步造成了ui缓慢。
            for (DownloadPiece piece : mQuery) {    //在这里先排除总比在FrechImg_Impl()中查找文件要快.
                if (piece.uri.equals(mpiece.uri)) {
                    WeiboLog.d(TAG, "已经存在url:"+mpiece.uri);
                    mQuery.remove(piece);
                    break;
                }
            }
            /*int size=mQuery.size();
            if (mQuery.size()>=count) {
                mQuery=mQuery.subList(size-count, size);
            }*/
            mQuery.add(mpiece);

            DownloadPoolThread.this.notifyAll();
        }
    }

    /**
     * 资源释放
     */
    public void release() {
        if (null!=mHandler) {
            mHandler.sendEmptyMessage(2);
        }
    }

    public void internalRelease() {
        if (this.mHandler!=null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        quitLooper();
    }

    private void quitLooper() {
        try {
            synchronized (this) {
                Looper.myLooper().quit();
            }
        } catch (Exception e) {
        }
    }
}
