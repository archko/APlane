package cn.archko.microblog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import cn.archko.microblog.settings.AppSettings;
import com.andrew.apollo.cache.ImageFetcher;
import com.me.microblog.App;
import com.me.microblog.util.DisplayUtils;

/**
 * @author: archko 2015/2/9 :17:31
 */
public class AApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings.init();
        ImageFetcher.getInstance(this).initImageOption(DisplayUtils.getScreenWidth()*2, DisplayUtils.getScreenHeight()*2, Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas();
        if (canvas.isHardwareAccelerated()) {
            ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_HEIGHT=canvas.getMaximumBitmapHeight();
            ImageFetcher.DEFAULT_MAX_IMAGE_TEXTURE_WIDTH=canvas.getMaximumBitmapWidth();
        }
    }
}
