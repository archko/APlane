package cn.archko.microblog.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.archko.microblog.fragment.PickImageFragment;
import com.android.gallery3d.common.BitmapUtils;
import com.me.microblog.bean.UploadImage;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理线程
 *
 * @author: archko 14-1-14 :20:41
 */
public class BitmapThread {

    public static final String TAG="BitmapThread";
    Handler mHandler;
    private List<UploadImage> mQuery;

    public BitmapThread() {
        init();
    }

    private void init() {
        initDecodeThread();
        //this.mHandler.sendEmptyMessage(0);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    private void initDecodeThread() {
        quitLooper();

        Log.d(TAG, "initThread:");
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
                    Log.d(TAG, "quit.");
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
        try {
            //Log.d(TAG, "internalStart:");
            //mQuery.add((UploadImage) msg.obj);
            decodeBitmap(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decodeBitmap(Message msg) {
        try {
            Object[] objects=(Object[]) msg.obj;
            ArrayList<UploadImage> uploadImages=(ArrayList<UploadImage>) objects[0];
            Handler handler=(Handler) objects[1];
            UploadImage image=null;

            for (int i=0; i<uploadImages.size(); i++) {
                image=uploadImages.get(i);
                if (null!=image) {
                    Bitmap bitmap=PickImageFragment.bitmapLruCache.get(image.path);
                    if (null!=bitmap) {
                    } else {
                        BitmapFactory.Options options=new BitmapFactory.Options();
                        options.inJustDecodeBounds=true;
                        BitmapFactory.decodeFile(image.path, options);
                        options.inSampleSize=BitmapUtils.computeSampleSizeLarger(options.outWidth, options.outHeight, 120);
                        WeiboLog.d("inSampleSize:"+options.inSampleSize+" width:"+options.outWidth+" height:"+options.outHeight);
                        options.inPreferredConfig=Bitmap.Config.RGB_565;
                        options.inJustDecodeBounds=false;
                        bitmap=BitmapFactory.decodeFile(image.path, options);
                    }

                    if (null!=bitmap) {
                        PickImageFragment.bitmapLruCache.put(image.path, bitmap);
                        Message message=Message.obtain(handler);
                        //message.obj=bitmap;
                        message.what=1;
                        handler.sendMessage(message);
                    }
                }
            }

            Message message=Message.obtain(handler);
            message.what=1;
            handler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
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

    private void internalRelease() {
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

    public void addMessage(Message msg) {
        mHandler.sendMessage(msg);
    }
}
