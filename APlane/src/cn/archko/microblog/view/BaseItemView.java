package cn.archko.microblog.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.settings.AppSettings;
import com.andrew.apollo.cache.ImageCache;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.bean.SAnnotation;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.IBaseItemView;

/**
 * 作为基础类，可以直接使用，但没有头像点击功能。
 *
 * @author: archko 12-6-24
 */
public abstract class BaseItemView extends LinearLayout implements IBaseItemView, View.OnClickListener, View.OnTouchListener {

    private static final String TAG="BaseItemView";
    protected Context mContext;
    protected String mCacheDir;    //图片缓存目录

    protected ImageView mPortrait;    //微博作者头像
    protected TextView mName;    //微博的作者
    protected TextView mRepostNum;//转发数
    protected TextView mCommentNum;//评论数
    protected TextView mContentFirst;//微博的内容
    protected ImageView mStatusPicture;   //微博内容图片，因为不管有没有转发，最多只显示一张图片。
    protected ImageView mStatusPictureLay;   //播放图标
    protected LinearLayout mContentSecondLayout;
    protected TextView mContentSencond;  //转发微博内容
    protected TextView mLeftSlider; //转发内容的左侧
    protected TextView mSourceFrom;    //来自
    protected TextView mCreateAt;  //发表时间

    protected String mPortraitUrl=null;
    protected String mPictureUrl;    //微博的内容图片url
    //private String retweetUrl;    //转发微博内容中的图片url.

    protected Status mStatus;    //微博
    protected Status mRetweetedStatus;    //转发的微博

    protected LinearLayout mLoctationlayout;    //位置布局
    protected TextView mLocation;   //位置信息
    protected SAnnotation sAnnotation;
    int mResId;
    //protected DisplayImageOptions options;

    public BaseItemView(Context context, boolean updateFlag) {
        super(context);
        mCacheDir=AppSettings.current().mCacheDir;
        mContext=context;

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
        //this.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    /**
     * 更新微博内容
     *
     * @param bean            微博实体
     * @param updateFlag      更新标志，如果为true表示更新图片
     * @param cache           是否缓存图片资源，如果是随便看看，为了节约sdcard，为false
     * @param showLargeBitmap 是否显示列表大图，默认显示小图
     * @param showBitmap      是否显示列表图片，默认显示。
     */
    @Override
    public void update(final Status bean, boolean updateFlag, boolean cache) {
        /*if (mStatus==bean) {
            WeiboLog.v(TAG, "相同的内容不更新。");
            if (updateFlag) {   //需要加载数据,否则会无法更新列表的图片.
                loadPicture(updateFlag, cache);
                isShowBitmap=showBitmap;
                loadPortrait(updateFlag, cache);
            }
            return;
        }

        try {
            mStatus=bean;
            mRetweetedStatus=bean.retweetedStatus;
            //TODO 因为现在的微博可能没有包user属性。可能被删除了。
            try {
                mName.setText(mStatus.user.screenName);
            } catch (Exception e) {
            }

            mContentFirst.setText(mStatus.text);

            if (null==mStatus.user) {
                WeiboLog.i(TAG, "微博可能被删除，无法显示！");
                mName.setText(null);
                mSourceFrom.setText(null);
                mCreateAt.setText(null);
                mRepostNum.setText(null);
                mCommentNum.setText(null);
                mContentSencond.setText(null);
                mLocation.setText(null);
                if (null!=mStatusPicture&&mStatusPictureLay!=null) {
                    mStatusPicture.setVisibility(View.GONE);
                    mStatusPictureLay.setVisibility(GONE);
                }
                mContentSencond.setVisibility(GONE);
                if (null!=mContentSecondLayout){
                    mContentSecondLayout.setVisibility(GONE);
                }
                return;
            }

            String source=mStatus.source;
            Matcher atMatcher=WeiboUtil.comeFrom.matcher(source);
            if (atMatcher.find()) {
                int start=atMatcher.start();
                int end=atMatcher.end();
                String cfString=source.substring(end, source.length()-4);
                mSourceFrom.setText("来自："+cfString);
            }

            mCreateAt.setText(DateUtils.getDateString(mStatus.createdAt));

            mRepostNum.setText(getResources().getString(R.string.text_repost_num, mStatus.r_num));
            mCommentNum.setText(getResources().getString(R.string.text_comment_num, mStatus.c_num));

            //处理转发的微博
            if (mRetweetedStatus!=null) {
                mContentSencond.setVisibility(View.VISIBLE);
                if (null!=mContentSecondLayout){
                    mContentSecondLayout.setVisibility(VISIBLE);
                }

                try {
                    String title="@"+mRetweetedStatus.user.screenName+":"+mRetweetedStatus.text+" ";
                    SpannableString spannableString=new SpannableString(title);
                    WeiboUtil.highlightContent(mContext, spannableString, getResources().getColor(R.color.holo_light_item_highliht_link));
                    mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mContentSencond.setVisibility(View.GONE);
                if (null!=mContentSecondLayout){
                    mContentSecondLayout.setVisibility(GONE);
                }
            }

            //location
            sAnnotation=mStatus.annotations;
            if (null==sAnnotation||sAnnotation.place==null) {
                mLoctationlayout.setVisibility(GONE);
            } else {
                if (mLoctationlayout.getVisibility()==GONE) {
                    mLoctationlayout.setVisibility(VISIBLE);
                }
                mLocation.setText(sAnnotation.place.title);
            }

            //WeiboLog.d("update,updateFlag:"+updateFlag);
            loadPicture(updateFlag, cache);

            loadPortrait(updateFlag, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 由于现在只显示一张微博图片，就是如果原创内容有图片，转发的就没有，如果转发的有图片，原创的就没有。
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    void loadPicture(boolean updateFlag, boolean cache) {
    }

    void setPictureLay(String url) {
        if (url.endsWith("gif")) {
            mStatusPictureLay.setVisibility(View.VISIBLE);
        } else {
            mStatusPictureLay.setVisibility(View.GONE);
        }
    }

    /**
     * 加载头像
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    protected void loadPortrait(boolean updateFlag, boolean cache) {
        AppSettings appSettings=AppSettings.current();
        if (appSettings.showBitmap) {
            User user=mStatus.user;
            if (null==user||TextUtils.isEmpty(user.profileImageUrl)) {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                return;
            }
            String profileImgUrl=mStatus.user.profileImageUrl;

            mPortraitUrl=profileImgUrl;
            //获取头像.
            Bitmap bitmap=ImageCache.getInstance(mContext).getBitmapFromMemCache(mPortraitUrl);
            if (null!=bitmap&&!bitmap.isRecycled()) {
                mPortrait.setImageBitmap(bitmap);
            } else {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                if (updateFlag) {
                    /*ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(mPortraitUrl, mPortrait, options);*/
                    ApolloUtils.getImageFetcher(mContext).startLoadImage(mPortraitUrl, mPortrait);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        /*String imgUrl;

        imgUrl=mStatus.bmiddlePic;
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl=mRetweetedStatus.bmiddlePic;
        }

        if (TextUtils.isEmpty(imgUrl)) {
            WeiboLog.d(TAG, "图片为空.");
            return;
        }

        if (isShowLargeBitmap&&!imgUrl.endsWith("gif")) {
            WeiboLog.i("已经在列表中显示大图了，且不是gif图，不用再显示。");
            return;
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setTitle("显示图片");

        String thumb=mStatus.thumbnailPic;
        if (TextUtils.isEmpty(thumb)) {
            thumb=mRetweetedStatus.thumbnailPic;
        }

        ImageViewerDialog imageViewerDialog=new ImageViewerDialog(mContext, imgUrl, mCacheDir, null, thumb);
        imageViewerDialog.setCanceledOnTouchOutside(true);
        imageViewerDialog.show();

        imageViewerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface dialogInterface) {
                WeiboLog.d(TAG, "dialog,onCancel.");
            }
        });*/
    }

    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            updateBitmap(msg);
        }
    };

    public void updateBitmap(Message msg) {
        int what=msg.what;
        Bundle bundle=msg.getData();

        String imgUrl=(String) msg.obj;
        if (what!=Constants.TYPE_PORTRAIT) {
            //WeiboLog.d(TAG, "imgUrl:"+imgUrl+" bundle:"+bundle);
        }
        Bitmap bitmap;//=BitmapFactory.decodeFile(bundle.getString("name"));
        bitmap=bundle.getParcelable("name");
        if (TextUtils.isEmpty(imgUrl)||"null".equals(imgUrl)||null==bitmap) {
            WeiboLog.w(TAG, "图片url不对，"+imgUrl);
            return;
        }

        if (bitmap!=null&&!bitmap.isRecycled()) {
            //if (!isShowLargeBitmap) {   //大图暂时不缓存内存，但是缓存小图
            //ImageCache2.getInstance().addBitmapToMemCache(imgUrl, bitmap);
            /*} else {
                LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
                lruCache.put(imgUrl, bitmap);
            }*/

            /*WeakReference<View> viewWeakReference=DownloadPool.downloading.get(imgUrl);

            if (null==viewWeakReference||viewWeakReference.get()==null) {
                DownloadPool.downloading.remove(imgUrl);
                WeiboLog.i(TAG, "listview is null:"+imgUrl);
                return;
            }

            try {
                ImageView imageView=(ImageView) viewWeakReference.get();
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*ListView listView=(ListView) viewWeakReference.get();
            //从第一个可见的项开始更新.
            int firstVisiblePosition=listView.getFirstVisiblePosition();
            int childCount=listView.getChildCount();
            //WeiboLog.d(TAG, "firstVisiblePosition:" + firstVisiblePosition+" childCount:" + childCount));
            if (0>=childCount) {
                DownloadPool.downloading.remove(imgUrl);
                WeiboLog.i(TAG, "listview has no children.");
                return;
            }

            BaseItemView itemView;
            View view;
            if (Constants.TYPE_PORTRAIT==what) {
                for (int i=0; i<childCount; i++) {
                    view=listView.getChildAt(i);
                    if (view instanceof BaseItemView) {
                        itemView=(BaseItemView) view;
                        if (itemView.mPortraitUrl!=null&&itemView.mPortraitUrl.equals(imgUrl)) {
                            itemView.mPortrait.setImageBitmap(bitmap);
                            break;
                        }
                    }
                }
            } else if (Constants.TYPE_PICTURE==what||Constants.TYPE_RETWEET_PICTURE==what) {
                for (int i=0; i<childCount; i++) {
                    view=listView.getChildAt(i);
                    if (view instanceof BaseItemView) {
                        itemView=(BaseItemView) view;
                        if (null!=itemView.mPictureUrl&&itemView.mPictureUrl.equals(imgUrl)) {
                            WeiboLog.v(TAG, "下载完成 TYPE_PICTURE:"+imgUrl);
                            itemView.mStatusPicture.setImageBitmap(bitmap);
                            setPictureLay(imgUrl);
                            break;
                        }
                    }
                }
            }*/
        } else {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "bitmap is null:"+imgUrl);
            }
        }
        //DownloadPool.downloading.remove(imgUrl);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret=false;
        CharSequence text=((TextView) v).getText();
        Spannable stext=Spannable.Factory.getInstance().newSpannable(text);
        TextView widget=(TextView) v;
        int action=event.getAction();

        if (action==MotionEvent.ACTION_UP||
            action==MotionEvent.ACTION_DOWN) {
            int x=(int) event.getX();
            int y=(int) event.getY();

            x-=widget.getTotalPaddingLeft();
            y-=widget.getTotalPaddingTop();

            x+=widget.getScrollX();
            y+=widget.getScrollY();

            Layout layout=widget.getLayout();
            int line=layout.getLineForVertical(y);
            int off=layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link=stext.getSpans(off, off, ClickableSpan.class);
            //Log.d(TAG, "onTouch:"+link.length+" text:");
            if (link.length!=0) {
                int start=stext.getSpanStart(link[0]);
                int end=stext.getSpanEnd(link[0]);
                //Log.d(TAG, "onTouch start:"+start+" end:"+end);
                if (action==MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                    Selection.removeSelection(stext);
                } else if (action==MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(stext, start, end);
                }
                ret=true;
            } else {
                Selection.removeSelection(stext);
            }
            //Log.d(TAG, "onTouch:ret:"+ret);
        }
        return ret;
    }
}