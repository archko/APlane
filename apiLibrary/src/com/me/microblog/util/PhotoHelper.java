package com.me.microblog.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 照片选择类.选择照片后,可以配置是否裁剪.不同的情况,有不同的回调.
 *
 * @author archko 2014/9/28 :11:09
 */
public class PhotoHelper {

    public static final String TAG="PhotoHelper";
    public static final int MAX_WIDTH=480;
    public static final int MAX_HEIGHT=960;
    private int mWidth=MAX_WIDTH;
    private int mHeight=MAX_HEIGHT;

    int mOutputX=200;
    int mOutputY=200;
    /**
     * 是否裁剪
     */
    private boolean shouldCrop=true;

    private static String FILE_DIR_NAME="photo";

    private static final int TAKE_CAMERA_PICTURE=1;
    private static final int CHOOSE_LOCAL_PICTURE=2;
    PhotoLisener mPhotoLisener;

    private Fragment mActivity;

    public PhotoHelper(Fragment fragment) {
        this.mActivity=fragment;
    }

    public void setFragment(Fragment fragment) {
        this.mActivity=fragment;
    }

    public void setHeight(int mHeight) {
        this.mHeight=mHeight;
    }

    public void setWidth(int mWidth) {
        this.mWidth=mWidth;
    }

    public void setOutputY(int mOutputY) {
        this.mOutputY=mOutputY;
    }

    public void setOutputX(int mOutputX) {
        this.mOutputX=mOutputX;
    }

    public void setShouldCrop(boolean shouldCrop) {
        this.shouldCrop=shouldCrop;
    }

    public void setPhotoLisener(PhotoLisener photoLisener) {
        this.mPhotoLisener=photoLisener;
    }

    //-------------------------------------------

    /**
     * 获取本地图片
     */
    private void getloaclPic() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        mActivity.startActivityForResult(intent, CHOOSE_LOCAL_PICTURE);
    }

    public void restoreData(Bundle savedInstanceState) {
        if (null!=savedInstanceState) {
            if (null==mCurrentPhotoFile&&null!=savedInstanceState) {
                String path=(String) savedInstanceState.get("key_photo_file");
                WeiboLog.d(TAG, "mCurrentPhotoFile:"+path);
                if (null!=path&&null==mCurrentPhotoFile) {
                    mCurrentPhotoFile=new File(path);
                    WeiboLog.d(TAG, "restoreImageList,file:"+path);
                }
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (null!=mCurrentPhotoFile) {
            outState.putString("key_photo_file", mCurrentPhotoFile.getAbsolutePath());
        }
        WeiboLog.d(TAG, "onSaveInstanceState");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        WeiboLog.d(TAG, "onActivityResult requestCode = "+requestCode);
        WeiboLog.d(TAG, "onActivityResult resultCode = "+resultCode);
        WeiboLog.d(TAG, "onActivityResult data = "+data);
        if (resultCode!=Activity.RESULT_OK) {// result is not correct
            return;
        } else {
            if (requestCode==CAMERA_WITH_DATA_TO_THUMB) {
                if (shouldCrop) {
                    doCropPhoto(data.getData());
                } else {
                    processGalleryData(data.getData());
                }
            } else if (requestCode==PHOTO_PICKED_WITH_DATA) {   //裁剪返回
                if (null!=data) {
                    if (null!=data.getData()) {
                        processGalleryData(data.getData());
                    } else {
                        if (null!=data.getExtras()) {
                            if (null!=mPhotoLisener) {
                                mPhotoLisener.onSelected((Bitmap) data.getExtras().get("data"));
                            }
                        }
                    }
                }
            } else if (requestCode==CAMERA_WITH_DATA) {
                if (null==mCurrentPhotoFile) {
                    NotifyUtils.showToast("内存不足.");
                    return;
                }

                if (shouldCrop) {
                    doCropPhoto();
                } else {
                    String path=mCurrentPhotoFile.getAbsolutePath();
                    System.out.println("path:"+path);
                    if (!TextUtils.isEmpty(path)) {
                        imgUrl=path;
                        showPhoto(imgUrl, true);
                    }
                }
            } else if (requestCode==CHOOSE_LOCAL_PICTURE) {
                WeiboLog.d(TAG, "CHOOSE_BIG_PICTURE:");
                if (shouldCrop) {
                    doCropPhoto(data.getData());
                } else {
                    processGalleryData(data.getData());
                }
            }
        }
    }

    //-------------------------------------------
    private String imgUrl="";

    /**
     * 编辑
     */
    public static final int EDIT_PHOTO_PICKED_WITH_DATA=3029;

    public static final int CAMERA_WITH_DATA_TO_THUMB=3025;
    /*用来标识请求照相功能的 activity*/
    public static final int CAMERA_WITH_DATA=3023;
    /*用来标识请求 gallery 的 activity*/
    public static final int PHOTO_PICKED_WITH_DATA=3021;
    /*拍照的照片存储位置*/
    private static final File PHOTO_DIR=new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera");
    private File mCurrentPhotoFile;//照相机拍照得到的图片
    private static final int MAX_IMAGE_SIZE=5000000;

    /**
     * 拍照获取图片
     */
    public void doTakePhoto() {
        try {
            imgUrl="";
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            // 创建照片的存储目录
            mCurrentPhotoFile=new File(PHOTO_DIR, getPhotoFileName());
            // 给新照的照片文件命名

            final Intent intent=getTakePickIntent(mCurrentPhotoFile);
            mActivity.startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            NotifyUtils.showToast("没有相机程序");
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * 用当前时间给取得的图片命名,需要注意，如果文件名有空格，这货还取不到返回值
     */
    private String getPhotoFileName() {
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat=new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date)+".jpg";
    }

    // 请求 Gallery 程序
    public void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            //final Intent intent=getPhotoPickIntent();
            //startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            Intent choosePictureIntent=new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            mActivity.startActivityForResult(choosePictureIntent, CAMERA_WITH_DATA_TO_THUMB);
        } catch (ActivityNotFoundException e) {
            NotifyUtils.showToast("没有相机程序");
        }
    }

    public void processGalleryData(Uri imageFileUri) {
        /*String[] proj={MediaStore.Images.Media.DATA};
        Cursor cur=null;

        try {
            System.out.println("imageFileUri:"+imageFileUri);
            cur=GJApplication.getContext().getContentResolver().query(imageFileUri, proj, null, null, null);
            int imageIdx=cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cur.moveToFirst();
            imgUrl=cur.getString(imageIdx);
            System.out.println("imgUrl:"+imgUrl);

            File file=new File(imgUrl);
            if (file.exists()) {
                mPhotoUri=imageFileUri;
                showPhoto(imgUrl, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cur) {
                cur.close();
            }
        }*/
        showPhoto(imageFileUri);
    }

    protected void doCropPhoto() {
        doCropPhoto(mCurrentPhotoFile);
    }

    protected void doCropPhoto(Uri uri) {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(uri);
            mActivity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            NotifyUtils.showToast("没有编辑照片程序.");
        }
    }

    protected void doCropPhoto(File f) {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(Uri.fromFile(f));
            mActivity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            NotifyUtils.showToast("没有编辑照片程序.");
        }
    }

    /**
     * Constructs an intent for image cropping. 调用图片剪辑程序
     */
    public Intent getCropImageIntent(Uri photoUri) {
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mOutputX);
        intent.putExtra("outputY", mOutputY);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        return intent;
    }

    void showPhoto(String filename, boolean store) {
        /*final Bitmap bitmap=CThumbnailUtil.decodeSampledBitmapFromFile(filename, mWidth, mHeight, Bitmap.Config.RGB_565);
        if (null!=bitmap) {
        }*/
        if (null!=mPhotoLisener) {
            mPhotoLisener.onSelected(filename);
        }
    }

    void showPhoto(Uri uri) {
        if (null!=mPhotoLisener) {
            mPhotoLisener.onSelected(uri);
        }
    }

    public interface PhotoLisener {

        /**
         * 选择完了照片,返回一个路径
         *
         * @param path 照片的路径
         */
        public void onSelected(String path);

        /**
         * 选择照片,返回uri
         *
         * @param uri 照片的uri
         */
        public void onSelected(Uri uri);

        /**
         * 选择照片,返回位图,在三星手机上,如果裁剪后可能会产生位图,而不是uri
         *
         * @param bitmap 位图
         */
        public void onSelected(Bitmap bitmap);
    }
}
