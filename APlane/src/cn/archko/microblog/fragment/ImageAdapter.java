package cn.archko.microblog.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import cn.archko.microblog.R;
import com.andrew.apollo.cache.ImageFetcher;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.thread.DownloadPool;
import cn.archko.microblog.ui.ImageViewerActivity;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.ImageViewerDialog;

import java.lang.ref.WeakReference;

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
    boolean isShowLargeBitmap=false;
    boolean cache=true;

    public ImageAdapter(Context c, String cacheDir, String[] thumbs) {
        mContext=c;
        mDensity=c.getResources().getDisplayMetrics().density;
        mInflater=LayoutInflater.from(c);
        mCacheDir=cacheDir;
        imageUrls=thumbs;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls=imageUrls;
    }

    public void setUpdateFlag(boolean updateFlag) {
        this.updateFlag=updateFlag;
    }

    public void setShowLargeBitmap(boolean showLargeBitmap) {
        isShowLargeBitmap=showLargeBitmap;
    }

    public void setCache(boolean cache) {
        this.cache=cache;
    }

    @Override
    public int getCount() {
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
            holder.picture.setImageResource(R.drawable.image_loading);
        }

        return convertView;
    }

    public void onClickItem(View view, int pos) {
        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setTitle("显示图片");

        /*String thumb=imageUrls[pos];

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

        if (!isShowLargeBitmap) {
            tmp=ImageCache2.getInstance().getBitmapFromMemCache(mPictureUrl);
        } else {
            mPictureUrl=mPictureUrl.replace("thumbnail", "bmiddle");
            LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
            tmp=lruCache.get(mPictureUrl);
        }

        //WeiboLog.v(TAG, "cached.tmp:"+tmp+" mPictureUrl:"+mPictureUrl);
        if (null!=tmp&&!tmp.isRecycled()) {
            picture.setImageBitmap(tmp);
        } else {
            if (!updateFlag) {
                picture.setImageResource(R.drawable.image_loading);
                return;
            }

            String dir=Constants.PICTURE_DIR;
            String ext=WeiboUtil.getExt(mPictureUrl);
            if (ext.equals(".gif")) {
                dir=Constants.GIF;
            }

            if (isShowLargeBitmap) {
                cache=true; //大图要缓存sdcard中，不然每次都下载，太慢了。
            }
            picture.setImageResource(R.drawable.image_loading);
            /*DownloadPool.downloading.put(mPictureUrl, new WeakReference<View>(picture));
            ((App) App.getAppContext()).mDownloadPool.Push(
                mHandler, mPictureUrl, Constants.TYPE_PICTURE, null, cache, mCacheDir+dir);*/
            DownloadPool.DownloadPiece piece=((App) App.getAppContext()).mDownloadPool.new DownloadPiece(
                mHandler, mPictureUrl, Constants.TYPE_PICTURE, cache, mCacheDir+dir, isShowLargeBitmap, picture);
            ((App) App.getAppContext()).mDownloadPool.Push(piece);
            /*ImageFetcher fetcher=ImageFetcher.getInstance(App.getAppContext());
            fetcher.loadHomeImage(mPictureUrl, picture, piece);*/
        }
    }

    Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateBitmap(msg);
        }
    };

    public void updateBitmap(Message msg) {
        /*Bundle bundle=msg.getData();

        String imgUrl=(String) msg.obj;

        Bitmap bitmap;//=BitmapFactory.decodeFile(bundle.getString("name"));
        bitmap=bundle.getParcelable("name");
        if (TextUtils.isEmpty(imgUrl)||"null".equals(imgUrl)||null==bitmap) {
            WeiboLog.w(TAG, "图片url不对，"+imgUrl);
            return;
        }

        if (bitmap!=null&&!bitmap.isRecycled()) {
            if (!isShowLargeBitmap) {   //大图暂时不缓存内存，但是缓存小图
                ImageCache2.getInstance().addBitmapToMemCache(imgUrl, bitmap);
            } else {
                LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
                lruCache.put(imgUrl, bitmap);
            }

            WeakReference<View> viewWeakReference=DownloadPool.downloading.get(imgUrl);

            if (null==viewWeakReference||viewWeakReference.get()==null) {
                DownloadPool.downloading.remove(imgUrl);
                WeiboLog.i(TAG, "listview is null:"+imgUrl);
                return;
            }

            ImageView itemView=(ImageView) viewWeakReference.get();

            itemView.setImageBitmap(bitmap);
        } else {
            WeiboLog.d(TAG, "bitmap is null:"+imgUrl);
        }
        DownloadPool.downloading.remove(imgUrl);*/
    }
}