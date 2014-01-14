package cn.archko.microblog.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Fragment;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 处理照片,可以拍照,也可以从图庘中选择
 *
 * @author: archko 14-1-14 :13:37
 */
public class TakePictureUtil {
    //--------------------- 图片 ---------------------
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
    private static final String TAG="TakePictureUtil";
    private File mCurrentPhotoFile;//照相机拍照得到的图片
    public static final int MAX_IMAGE_SIZE=5000000;
    Uri mPhotoUri=null;

    Activity mActivity;
    Context mContext;
    Fragment mFragment;

    public void setActivity(Activity mActivity) {
        this.mActivity=mActivity;
    }

    public void setContext(Context mContext) {
        this.mContext=mContext;
    }

    public void setFragment(Fragment mFragment) {
        this.mFragment=mFragment;
    }

    public File getCurrentPhotoFile() {
        return mCurrentPhotoFile;
    }

    /**
     * 编辑照片
     */
    private void doEditPhoto() {
        try { // 启动 gallery 去剪辑这个照片
            Intent intent=new Intent("android.intent.action.EDIT");
            intent.setDataAndType(mPhotoUri, "image/*");
            if (null!=mFragment) {
                mFragment.startActivityForResult(intent, EDIT_PHOTO_PICKED_WITH_DATA);
            } else {
                mActivity.startActivityForResult(intent, EDIT_PHOTO_PICKED_WITH_DATA);
            }
        } catch (ActivityNotFoundException e) {
            AKUtils.showToast("没有找到相关的编辑程序，现在裁剪。", Toast.LENGTH_LONG);
            startCropPhoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理从相册中选择图片
     *
     * @param data
     */
    /*private void processGalleryData(Uri imageFileUri) {
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cur=null;

        try {
            WeiboLog.i(TAG, "imageFileUri:"+imageFileUri);
            cur=mContext.getContentResolver().query(imageFileUri, proj, null, null, null);
            int imageIdx=cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cur.moveToFirst();
            imgUrl=cur.getString(imageIdx);
            WeiboLog.i(TAG, "imgUrl:"+imgUrl);

            File file=new File(imgUrl);
            if (file.exists()) {
                if (file.length()>MAX_IMAGE_SIZE) {
                    AKUtils.showToast("上传的图片超过了5m，新浪不支持！");
                    //clearImagePreview();
                    return;
                }
                mPhotoUri=imageFileUri;
                showPhoto(imageFileUri);
            }

        } catch (Exception e) {
            WeiboLog.e(e.toString());
        } finally {
            if (null!=cur) {
                cur.close();
            }
        }
    }*/

    /**
     * 外部调用入口
     */
    public void doPickPhotoAction() {
        // Wrap our context to inflate list items using correct theme
        final Context dialogContext=new ContextThemeWrapper(mContext, android.R.style.Theme_Light);
        String cancel=mContext.getString(R.string.new_back);
        String[] choices;
        choices=new String[2];
        choices[0]=mContext.getString(R.string.new_take_photo);            //拍照
        choices[1]=mContext.getString(R.string.new_pick_photo);        //从相册中选择
        final ListAdapter adapter=new ArrayAdapter<String>(dialogContext, android.R.layout.simple_list_item_1, choices);
        final AlertDialog.Builder builder=new AlertDialog.Builder(dialogContext);
        builder.setTitle(R.string.app_name);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0: {
                        String status=Environment.getExternalStorageState();
                        if (status.equals(Environment.MEDIA_MOUNTED)) {//判断是否有 SD 卡
                            doTakePhoto();
                            // 用户点击了从照相机 获取
                        } else {
                            AKUtils.showToast(R.string.new_no_sdcard, Toast.LENGTH_SHORT);
                        }
                        break;
                    }

                    case 1:
                        doPickPhotoFromGallery();// 从相册中去获 取
                        break;
                }
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 拍照获取图片
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            // 创建照片的存储目录
            mCurrentPhotoFile=new File(PHOTO_DIR, getPhotoFileName());
            // 给新照的照片文件命名

            final Intent intent=getTakePickIntent(mCurrentPhotoFile);
            if (null!=mFragment) {
                mFragment.startActivityForResult(intent, CAMERA_WITH_DATA);
            } else {
                mActivity.startActivityForResult(intent, CAMERA_WITH_DATA);
            }
        } catch (ActivityNotFoundException e) {
            AKUtils.showToast(R.string.new_photo_picker_not_found, Toast.LENGTH_LONG);
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
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            //final Intent intent=getPhotoPickIntent();
            //startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            Intent choosePictureIntent=new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (null!=mFragment) {
                mFragment.startActivityForResult(choosePictureIntent, CAMERA_WITH_DATA_TO_THUMB);
            } else {
                mActivity.startActivityForResult(choosePictureIntent, CAMERA_WITH_DATA_TO_THUMB);
            }
        } catch (ActivityNotFoundException e) {
            AKUtils.showToast(R.string.new_photo_picker_not_found, Toast.LENGTH_LONG);
        }
    }

    // 封装请求 Gallery 的 intent
    public static Intent getPhotoPickIntent() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);
        return intent;
    }

    protected void doCropPhoto() {
        doCropPhoto(mCurrentPhotoFile);
    }

    protected void doCropPhoto(File f) {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(Uri.fromFile(f));
            if (null!=mFragment) {
                mFragment.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            } else {
                mActivity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            }
        } catch (Exception e) {
            //Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 裁剪，直接使用uri来处理。在没有图片编辑软件后调用这个裁剪功能。
     */
    protected void startCropPhoto() {
        try { // 启动 gallery 去剪辑这个照片
            final Intent intent=getCropImageIntent(mPhotoUri);
            if (null!=mFragment) {
                mFragment.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            } else {
                mActivity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            }
        } catch (Exception e) {
            AKUtils.showToast("系统没有裁剪的程序！", Toast.LENGTH_LONG);
        }
    }

    /**
     * Constructs an intent for image cropping. 调用图片剪辑程序
     */
    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        /*intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);*/
        intent.putExtra("return-data", true);
        return intent;
    }

    void showPhoto(Uri imageFileUri) throws FileNotFoundException {
        /*Display currentDisplay=getWindowManager().getDefaultDisplay();
        int dw=currentDisplay.getWidth();
        int dh=currentDisplay.getHeight()/2-100;

        // Load up the image's dimensions not the image itself
        BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds=true;
        Bitmap bmp=BitmapFactory.decodeStream(
            getContentResolver().openInputStream(imageFileUri), null,
            bmpFactoryOptions);

        int heightRatio=(int) Math.ceil(bmpFactoryOptions.outHeight/(float) dh);
        int widthRatio=(int) Math.ceil(bmpFactoryOptions.outWidth/(float) dw);

        if (heightRatio>1&&widthRatio>1) {
            if (heightRatio>widthRatio) {
                bmpFactoryOptions.inSampleSize=heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize=widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds=false;
        bmp=BitmapFactory.decodeStream(
            getContentResolver().openInputStream(imageFileUri), null,
            bmpFactoryOptions);

        mPreview.setImageBitmap(bmp);

        mPreview.setVisibility(View.VISIBLE);
        mCloseImage.setVisibility(View.VISIBLE);
        mImageOperaBar.setVisibility(View.VISIBLE);*/
    }

    /*void showPhoto(String filename) {
        Display currentDisplay=getWindowManager().getDefaultDisplay();
        int dw=currentDisplay.getWidth();
        int dh=currentDisplay.getHeight()/2-100;

        // Load up the image's dimensions not the image itself
        BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds=true;
        Bitmap bmp=BitmapFactory.decodeFile(filename, bmpFactoryOptions);

        int heightRatio=(int) Math.ceil(bmpFactoryOptions.outHeight/(float) dh);
        int widthRatio=(int) Math.ceil(bmpFactoryOptions.outWidth/(float) dw);

        if (heightRatio>1&&widthRatio>1) {
            if (heightRatio>widthRatio) {
                bmpFactoryOptions.inSampleSize=heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize=widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds=false;
        bmp=BitmapFactory.decodeFile(filename, bmpFactoryOptions);

        mPreview.setImageBitmap(bmp);

        mPreview.setVisibility(View.VISIBLE);
        mCloseImage.setVisibility(View.VISIBLE);
        mImageOperaBar.setVisibility(View.VISIBLE);
    }*/
}
