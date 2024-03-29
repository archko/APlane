package cn.archko.microblog.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.archko.microblog.R;
import cn.archko.microblog.settings.AppSettings;
import cn.archko.microblog.ui.ImageViewerActivity;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.util.WeiboLog;
/*import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;*/

/**
 * @description:
 * @author: archko 13-6-6 :下午4:15
 */
public class ImageAdapter extends BaseAdapter {

    public static final String TAG="ImageAdapter";
    private LayoutInflater mInflater;
    private final Context mContext;
    private final float mDensity;
    /**
     * 图片的url
     */
    String[] imageUrls;
    String mCacheDir;
    boolean updateFlag=true;
    boolean cache=true;
    int mResId;
    //DisplayImageOptions options;
    int imageWidth;
    int imageHeight;
    AppSettings appSettings=AppSettings.current();

    public ImageAdapter(Context c, String[] thumbs) {
        mContext=c;
        mDensity=c.getResources().getDisplayMetrics().density;
        mInflater=LayoutInflater.from(c);
        mCacheDir=appSettings.mCacheDir;
        imageUrls=thumbs;

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("1".equals(themeId)) {
            mResId=R.drawable.image_loading_dark;
        } else if ("2".equals(themeId)) {
            mResId=R.drawable.image_loading_light;
        } else if ("0".equals(themeId)) {
            mResId=R.drawable.image_loading_dark;
        }
        /*options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();*/
        imageWidth=(mContext.getResources().getDimensionPixelOffset(R.dimen.home_timeline_img_width));
        imageHeight=(mContext.getResources().getDimensionPixelOffset(R.dimen.home_timeline_img_height));
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls=imageUrls;
    }

    public void setUpdateFlag(boolean updateFlag) {
        this.updateFlag=updateFlag;
    }

    public void setCache(boolean cache) {
        this.cache=cache;
    }

    @Override
    public int getCount() {
        if (null==imageUrls) {
            return 0;
        }
        return imageUrls.length;
    }

    @Override
    public Object getItem(int position) {
        return imageUrls[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView==null) {
            convertView=mInflater.inflate(R.layout.home_time_line_item_img, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder=new ViewHolder();
            holder.picture=(ImageView) convertView.findViewById(R.id.status_picture);
            holder.pictureLay=(ImageView) convertView.findViewById(R.id.status_picture_lay);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder=(ViewHolder) convertView.getTag();
        }

        //WeiboLog.d(TAG, "getView:"+imageUrls[position]);
        if (null!=imageUrls&&imageUrls.length>0) {
            final int pos=position;
            String thumb=imageUrls[pos];

            // Bind the data efficiently with the holder.
            if (thumb.endsWith("gif")) {
                holder.pictureLay.setVisibility(View.VISIBLE);
            } else {
                holder.pictureLay.setVisibility(View.GONE);
            }

            holder.picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(view, pos);
                }
            });

            if (null!=thumb&&!"".equals(thumb)) {
                if (thumb.startsWith("http")) {
                    loadPicture(holder.picture, thumb, pos);
                }
            }
        } else {
            holder.picture.setImageResource(mResId);
        }

        return convertView;
    }

    public void onClickItem(View view, int pos) {
        /*AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setTitle("显示图片");

        String thumb=imageUrls[pos];

        if (!TextUtils.isEmpty(thumb)) {
            String imgUrl=thumb.replace("thumbnail", "bmiddle");
            ImageViewerDialog imageViewerDialog=new ImageViewerDialog(mContext, imgUrl, mCacheDir, null, thumb);
            imageViewerDialog.setCanceledOnTouchOutside(true);
            imageViewerDialog.show();

            imageViewerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialogInterface) {
                    WeiboLog.d(TAG, "dialog,onCancel.");
                }
            });
        }*/
        Intent intent=new Intent(mContext, ImageViewerActivity.class);
        intent.putExtra("thumbs", imageUrls);
        intent.putExtra("pos", pos);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    static class ViewHolder {

        ImageView picture;
        ImageView pictureLay;
    }

    private void loadPicture(ImageView picture, String mPictureUrl, int pos) {
        Bitmap tmp=null;

        if (!appSettings.showLargeBitmap) {
            if (picture.getScaleType()!=ImageView.ScaleType.FIT_XY) {
                RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) picture.getLayoutParams();
                int width=imageWidth;
                int height=imageHeight;
                if (null==lp) {
                    lp=new RelativeLayout.LayoutParams((width), (height));
                    picture.setLayoutParams(lp);
                } else {
                    if (lp.width!=width||lp.height!=height) {
                        lp.width=width;
                        lp.height=height;
                        picture.setLayoutParams(lp);
                    }
                }
                picture.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        } else {
            if (picture.getScaleType()!=ImageView.ScaleType.CENTER_CROP) {
                RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) picture.getLayoutParams();
                if (null==lp) {
                    lp=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                } else {
                    if (lp.width!=ViewGroup.LayoutParams.WRAP_CONTENT||lp.height!=ViewGroup.LayoutParams.WRAP_CONTENT) {
                        lp.width=ViewGroup.LayoutParams.WRAP_CONTENT;
                        lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                }
                picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            if (!mPictureUrl.endsWith("gif")) {
                mPictureUrl=mPictureUrl.replace("thumbnail", "bmiddle");
            }
        }

        //WeiboLog.v(TAG, "cached.tmp:"+tmp+" mPictureUrl:"+mPictureUrl);
        if (null!=tmp&&!tmp.isRecycled()) {
            picture.setImageBitmap(tmp);
        } else {
            if (!updateFlag) {
                picture.setImageResource(mResId);
                return;
            }

            if (appSettings.showLargeBitmap) {
                cache=true; //大图要缓存sdcard中，不然每次都下载，太慢了。
            }
            picture.setImageResource(mResId);
            /*ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mPictureUrl, picture, options);*/
            ApolloUtils.getImageFetcher(mContext).startLoadImage(mPictureUrl, picture);
        }
    }
}