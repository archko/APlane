package com.andrew.apollo.cache;

import android.graphics.Bitmap;

/**
 * @author: archko 14-2-18 :下午1:55
 */
public class ImageOption {

    private int maxWidth;
    private int maxHeight;
    private Bitmap.Config mConfig;
    /**
     * 加载成功或失败的id,如果是0,则用黑底的.如果是-1,则不设置
     * 这个用处不大,而且是统一的,全局变量.应该要在传入的参数作一些修改,然后可以加入不同的值.
     */
    public int mLoadingDrawableId=0;
    public int mLoadFailedDrawableId=0;

    public ImageOption() {
        maxWidth=ImageFetcher.DEFAULT_MAX_IMAGE_WIDTH;
        maxHeight=ImageFetcher.DEFAULT_MAX_IMAGE_HEIGHT;
        mConfig=Bitmap.Config.RGB_565;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth=maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight=maxHeight;
    }

    public void setConfig(Bitmap.Config mConfig) {
        this.mConfig=mConfig;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public Bitmap.Config getConfig() {
        return mConfig;
    }
}
