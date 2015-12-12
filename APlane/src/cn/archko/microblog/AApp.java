package cn.archko.microblog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import cn.archko.microblog.settings.AppSettings;
import com.andrew.apollo.cache.ImageFetcher;
import com.me.microblog.App;
import com.me.microblog.util.DisplayUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko 2015/2/9 :17:31
 */
public class AApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings.init();
        ImageFetcher.getInstance(this).initImageOption(DisplayUtils.getScreenWidth()*2, DisplayUtils.getScreenHeight()*2, Bitmap.Config.RGB_565);
        int height=DisplayUtils.getScreenHeight();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "screenHeight:"+height);
        }
        if (height>=1920) {
            height=4096;
        } else if (height>1280) {
            height=4096;
        } else {
            height=2048;
        }
        Canvas canvas=new Canvas();
        /*if (canvas.getMaximumBitmapHeight()<=4096) {
            ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT=canvas.getMaximumBitmapHeight();
            ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_WIDTH=canvas.getMaximumBitmapWidth();
        }*/
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, " ch:"+canvas.getMaximumBitmapHeight()+" cw:"+canvas.getMaximumBitmapWidth()+" height:"+height);
        }
        ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT=height;
        ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_WIDTH=height;
    }
}
