package com.me.microblog.thread;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

/**
 * 图片下载线程池,暂时可允许最多三个线程同时下载。
 *
 * @author archko
 */
public class DownloadPool extends Thread {

    public static final String TAG="DownloadPool";
    public static int MAX_THREAD_COUNT=2;
    //private DefaultHttpClient httpClient;
    private int mActiveThread=0;
    private App mApp;
    private List<DownloadPiece> mQuery;
    private HttpParams params;
    ClientConnectionManager connectionManager;
    private boolean isStop=false;
    public static final int READ_TIMEOUT=20000;
    public static final int CONNECT_TIMEOUT=20000;
    //public static Map<String, WeakReference<View>> downloading=new Hashtable<String, WeakReference<View>>();

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

    public DownloadPool(App app) {
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

    /**
     * 处理图片下载
     *
     * @param handler
     * @param name     图片名字废除
     * @param filepath 图片绝对路径
     * @param type     类型，只是作为参数传递，在这无其它用处
     * @param uri      图片url地址
     * @param dir      图片存储目录
     * @param cache    是否缓存本地文件
     */
    /*private void FrechImg_Impl(Handler handler, String filepath, int type, String uri, boolean cache, String dir) {
        //WeiboLog.d(TAG, "FrechImg_Impl.type:"+type);

        if (uri==null||dir==null) {
            WeiboLog.w(TAG, "名字不存在。");
            return;
        }

        WeakReference<View> viewWeakReference=downloading.get(uri);
        if (null==viewWeakReference) {
            WeiboLog.i(TAG, "viewWeakRef is null."+uri);
            downloading.remove(uri);
            return;
        }

        if (null==viewWeakReference.get()) {
            WeiboLog.i(TAG, "viewWeakRef get is null."+uri);
            downloading.remove(uri);
            return;
        }

        Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(uri);
        if (null!=bitmap) {
            Bundle bundle=new Bundle();
            bundle.putParcelable("name", bitmap);
            FetchImage.SendMessage(handler, type, bundle, uri);
            return;
        }

        synchronized (this) {
            mApp.mDownloadPool.ActiveThread_Push();
            String str3=Uri.encode(uri, ":/");
            HttpGet httpGet=new HttpGet(str3);
            httpGet.setHeader("User-Agent", BaseApi.USERAGENT);
            DefaultHttpClient httpClient=new DefaultHttpClient(connectionManager, params);
            //FetchImage fetchImage=new FetchImage(mApp, handler, httpClient, httpGet, type, imagepath, uri, cache);
            FetchImage fetchImage=new FetchImage(mApp, handler, httpClient, httpGet, type, null, uri, cache, dir);
            fetchImage.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            fetchImage.start();
        }
    }*/
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
        /*if (TextUtils.isEmpty(mpiece.uri)) {
            WeiboLog.d(TAG, "uri is null.");
            return;
        }
        synchronized (this) {   //这里的同步造成了ui缓慢。
            for (DownloadPiece piece : mQuery) {    //在这里先排除总比在FrechImg_Impl()中查找文件要快.
                if (piece.uri.equals(mpiece.uri)) {
                    WeiboLog.d(TAG, "已经存在url:"+mpiece.uri);
                    mQuery.remove(piece);
                    break;
                }
            }
            mQuery.add(mpiece);

            notifyAll();
        }*/
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
        //downloading.put(uri, new WeakReference<View>(imageView));
        /*synchronized (this) {   //这里的同步造成了ui缓慢。
            for (DownloadPiece piece : mQuery) {    //在这里先排除总比在FrechImg_Impl()中查找文件要快.
                if (piece.uri.equals(uri)) {
                    //WeiboLog.v(TAG, "已经存在url:"+uri);
                    mQuery.remove(piece);
                    break;
                }
            }
            DownloadPiece piece=new DownloadPiece(handler, uri, type, cache, dir, false, imageView);
            mQuery.add(piece);

            notifyAll();
        }*/
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

    public class DownloadPiece {

        Handler handler;
        //public String name; //md5加密过的名字,废除
        public String dir;//存储目录
        public String uri;//图片的url
        public int type;// 图片的类型，是微博图片，还是转发内容图片或者用户头像等
        @Deprecated
        public String filepath; // 图片存储的路径，绝对的，废除
        public boolean cache;
        public boolean isShowLargeBitmap;
        public WeakReference<ImageView> mImageReference;

        /**
         * 构造一个下载实体
         *
         * @param h        回调
         * @param uri      图片的下载地址
         * @param type     类型，目前有头像与微博图片
         * @param filepath 图片的路径，废除了，现在在这里对图片进行解码，而不用绝对路径，用目录与url进行构造
         * @param cache    是否缓存
         * @param dir      存储目录，这是图片存储的绝对目录，可以由url+dir计算得到存储的绝对路径。
         */
        public DownloadPiece(Handler h, String uri, int type, boolean cache, String dir, boolean isShowLargeBitmap, ImageView imageView) {
            this.uri=uri;
            //this.name=name;
            this.handler=h;
            this.type=type;
            //this.filepath=filepath;
            this.cache=cache;
            this.dir=dir;
            this.isShowLargeBitmap=isShowLargeBitmap;
            this.mImageReference=new WeakReference<ImageView>(imageView);
        }
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
        if (null!=bitmap) {
            if (!cancelWork(piece)&&null!=piece.handler) {
                final WeakReference<ImageView> viewWeakReference=piece.mImageReference;
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
        }

        //synchronized (this) {
            mApp.mDownloadPool.ActiveThread_Push();
            String str3=Uri.encode(uri, ":/");
            HttpGet httpGet=new HttpGet(str3);
            httpGet.setHeader("User-Agent", BaseApi.USERAGENT);
            DefaultHttpClient httpClient=new DefaultHttpClient(connectionManager, params);
            FetchImage fetchImage=new FetchImage(mApp, httpClient, httpGet, piece);
            fetchImage.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            fetchImage.start();
        //}
    }

    public static boolean cancelWork(DownloadPiece piece) {
        if (null==piece){
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

        synchronized (DownloadPool.this) {   //这里的同步造成了ui缓慢。
            for (DownloadPiece piece : mQuery) {    //在这里先排除总比在FrechImg_Impl()中查找文件要快.
                if (piece.uri.equals(mpiece.uri)) {
                    WeiboLog.d(TAG, "已经存在url:"+mpiece.uri);
                    mQuery.remove(piece);
                    break;
                }
            }
            mQuery.add(mpiece);

            DownloadPool.this.notifyAll();
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
