package com.andrew.apollo.utils;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import cn.archko.microblog.R;
import com.me.microblog.App;

/**
 * A collection of helpers designed to get and set various preferences across
 * Apollo.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class ThemeUtils {

    public static ThemeUtils sInstance;

    public ThemeUtils() {
    }

    public static ThemeUtils getsInstance() {
        if (null == sInstance) {
            sInstance = new ThemeUtils();
        }
        return sInstance;
    }

    /**
     * @param actionBar
     * @param context
     */
    public void themeActionBar(ActionBar actionBar, Context context) {
        String themeId = PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("1".equals(themeId)) {
            BitmapDrawable bg=(BitmapDrawable) context.getResources().getDrawable(R.drawable.bg_striped_split_img);
            bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setBackgroundDrawable(bg);

            BitmapDrawable bgSplit=(BitmapDrawable) context.getResources().getDrawable(R.drawable.bg_striped_split_img);
            bgSplit.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setSplitBackgroundDrawable(bgSplit);
        } else if ("2".equals(themeId)){
            Drawable bg=(Drawable) context.getResources().getDrawable(R.color.orange500);
            //bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setBackgroundDrawable(bg);

            //Drawable bgSplit=(Drawable) context.getResources().getDrawable(R.drawable.abs__ab_bottom_solid_light_holo);
            //bgSplit.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setSplitBackgroundDrawable(null);
        } else if ("0".equals(themeId)) {
            //Drawable bg=(Drawable) context.getResources().getDrawable(R.drawable.abs__ab_solid_dark_holo);
            //bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setBackgroundDrawable(null);

            //Drawable bgSplit=(Drawable) context.getResources().getDrawable(R.drawable.abs__ab_bottom_solid_dark_holo);
            //bgSplit.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            actionBar.setSplitBackgroundDrawable(null);
        }
    }

    /**
     * 设置主题背景色。
     *
     * @param view
     * @param context
     */
    public void themeBackground(View view, Context context) {
        /*String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
            BitmapDrawable bg=(BitmapDrawable) context.getResources().getDrawable(R.drawable.bg_stripes_dark);
            bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            view.setBackgroundDrawable(bg);
        } else if ("1".equals(themeId)) {
            BitmapDrawable bg=(BitmapDrawable) context.getResources().getDrawable(R.drawable.bg_striped);
            bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            view.setBackgroundDrawable(bg);
        } else {
            view.setBackgroundResource(R.color.holo_light_bg_view);
        }*/
    }
}