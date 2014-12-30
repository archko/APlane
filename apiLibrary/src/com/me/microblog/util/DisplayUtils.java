package com.me.microblog.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import com.me.microblog.App;

/**
 * 屏幕工具类
 *
 * @author archko
 */
public class DisplayUtils {

    private static String TAG="DisplayUtil";

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics dm=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth=dm.widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth=wm.getDefaultDisplay().getWidth();
        return screenWidth;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        return getScreenWidth(App.getAppContext());
    }

    /**
     * 获取屏幕高度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics dm=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight=dm.heightPixels;
        return screenHeight;
    }

    /**
     * 获取屏幕高度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenHeight=wm.getDefaultDisplay().getHeight();
        return screenHeight;
    }

    /**
     * 获取屏幕高度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight() {
        return getScreenHeight(App.getAppContext());
    }

    /**
     * 获取屏幕显示像素密度比例
     *
     * @return 屏幕显示像素密度比例
     */
    public static float getDisplayDensity(Context context) {
        DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    /**
     * 获取屏幕显示像素密度比例
     *
     * @return 屏幕显示像素密度比例
     */
    public static float getDisplayDensity() {
        return getDisplayDensity(App.getAppContext());
    }

    public static int convertPxToDp(int px) {
        WindowManager wm=(WindowManager) App.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display=wm.getDefaultDisplay();
        DisplayMetrics metrics=new DisplayMetrics();
        display.getMetrics(metrics);
        float logicalDensity=metrics.density;
        int dp=Math.round(px/logicalDensity);
        return dp;
    }

    public static int convertDpToPx(int dp) {
        return Math.round(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                App.getAppContext().getResources().getDisplayMetrics())
        );
    }

    public static void setFullscreen(Window win, boolean on) {
        WindowManager.LayoutParams winParams=win.getAttributes();
        final int bits=WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (on) {
            winParams.flags|=bits;
        } else {
            winParams.flags&=~bits;
        }
        win.setAttributes(winParams);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setOverscan(Window win, boolean on) {
        WindowManager.LayoutParams winParams=win.getAttributes();
        final int bits=WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;
        if (on) {
            winParams.flags|=bits;
        } else {
            winParams.flags&=~bits;
        }
        win.setAttributes(winParams);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentStatus(Window win, boolean on) {
        WindowManager.LayoutParams winParams=win.getAttributes();
        final int bits=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags|=bits;
        } else {
            winParams.flags&=~bits;
        }
        win.setAttributes(winParams);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentNavigation(Window win, boolean on) {
        WindowManager.LayoutParams winParams=win.getAttributes();
        final int bits=WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (on) {
            winParams.flags|=bits;
        } else {
            winParams.flags&=~bits;
        }
        win.setAttributes(winParams);
    }
}
