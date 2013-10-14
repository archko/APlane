package cn.archko.microblog.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.UserFragmentActivity;
import com.andrew.apollo.utils.PreferenceUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.Favorite;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.ImageViewerDialog;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class FavItemView extends LinearLayout implements View.OnClickListener, Checkable {

    private static final String TAG="FavItemView";

    private boolean checked=false;

    protected Context mContext;
    protected ListView parent;
    protected String mCacheDir;    //图片缓存目录

    protected ImageView mPortrait;    //微博作者头像
    protected TextView mName;    //微博的作者
    protected TextView mRepostNum;//转发数
    protected TextView mCommentNum;//评论数
    protected TextView mContentFirst;//微博的内容
    protected ImageView mStatusPicture;   //微博内容图片，因为不管有没有转发，最多只显示一张图片。
    protected ImageView mStatusPictureLay;   //播放图标
    protected TextView mContentSencond;  //转发微博内容
    protected TextView mSourceFrom;    //来自
    protected TextView mCreateAt;  //发表时间

    protected String mPortraitUrl=null;
    protected String mPictureUrl;    //微博的内容图片url
    //private String retweetUrl;    //转发微博内容中的图片url.

    protected Favorite mFavorite;    //微博收藏
    protected Status mRetweetedStatus;    //转发的微博

    protected boolean isShowLargeBitmap=false;
    protected boolean isShowBitmap=true;
    protected LinearLayout mLoctationlayout;    //位置布局
    protected TextView mLocation;   //位置信息

    public FavItemView(Context context, ListView view, String cacheDir, Favorite status, boolean updateFlag,
        boolean cache, boolean showLargeBitmap, boolean showBitmap) {
        super(context);

        parent=view;
        mCacheDir=cacheDir;
        mContext=context;

        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.time_line_item, this);

        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mPortrait.setOnClickListener(this);
        mName=(TextView) findViewById(R.id.tv_name);
        mRepostNum=(TextView) findViewById(R.id.repost_num);
        mCommentNum=(TextView) findViewById(R.id.comment_num);
        mContentFirst=(TextView) findViewById(R.id.tv_content_first);
        mStatusPicture=(ImageView) findViewById(R.id.status_picture);
        mStatusPicture.setOnClickListener(this);
        mStatusPictureLay=(ImageView) findViewById(R.id.status_picture_lay);
        mContentSencond=(TextView) findViewById(R.id.tv_content_sencond);
        mSourceFrom=(TextView) findViewById(R.id.source_from);
        mCreateAt=(TextView) findViewById(R.id.send_time);

        mLoctationlayout=(LinearLayout) findViewById(R.id.loctation_ll);
        mLocation=(TextView) findViewById(R.id.location);

        isShowLargeBitmap=showLargeBitmap;
        isShowBitmap=showBitmap;

        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(mContext);
        float pref_title_font_size=options.getInt(PreferenceUtils.PREF_TITLE_FONT_SIZE, 14);
        float pref_content_font_size=options.getInt(PreferenceUtils.PREF_CONTENT_FONT_SIZE, 16);
        float pref_ret_content_font_size=options.getInt(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE, 16);

        int pref_content_color=PreferenceUtils.getInstace(App.getAppContext()).getDefaultStatusThemeColor(App.getAppContext());
        int pref_ret_content_color=PreferenceUtils.getInstace(App.getAppContext()).getDefaultRetContentThemeColor(App.getAppContext());

        if (mName.getTextSize()!=pref_title_font_size) {
            mName.setTextSize(pref_title_font_size);
        }
        if (mContentFirst.getTextSize()!=pref_content_font_size) {
            mContentFirst.setTextSize(pref_content_font_size);
        }
        if (mContentSencond.getTextSize()!=pref_ret_content_font_size) {
            mContentSencond.setTextSize(pref_ret_content_font_size);
        }
        mContentFirst.setTextColor(pref_content_color);
        mContentSencond.setTextColor(pref_ret_content_color);

        //update(status, updateFlag, cache, showLargeBitmap, showBitmap);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean aChecked) {
        if (checked==aChecked) {
            return;
        }
        checked=aChecked;
        setBackgroundResource(checked ? R.drawable.abs__list_longpressed_holo : android.R.color.transparent);
    }

    @Override
    public void toggle() {
        setChecked(!checked);
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
    public void update(final Favorite bean, boolean updateFlag, boolean cache, boolean showLargeBitmap,
        boolean showBitmap) {
        if (mFavorite==bean) {
            WeiboLog.v(TAG, "相同的内容不更新。");
            if (updateFlag) {   //需要加载数据,否则会无法更新列表的图片.
                loadPicture(updateFlag, cache);
                isShowBitmap=showBitmap;
                loadPortrait(updateFlag, cache);
            }
            return;
        }

        try {
            mFavorite=bean;
            Status mStatus=mFavorite.mStatus;

            mRetweetedStatus=mStatus.retweetedStatus;
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
                mStatusPicture.setVisibility(View.GONE);
                mStatusPictureLay.setVisibility(GONE);
                mContentSencond.setVisibility(GONE);
                return;
            }

            String source=mStatus.source;
            Matcher atMatcher=WeiboUtil.comeFrom.matcher(source);
            if (atMatcher.find()) {
                int start=atMatcher.start();
                int end=atMatcher.end();
                String cfString=source.substring(end, source.length()-4);
                mSourceFrom.setText(getResources().getString(R.string.text_come_from, cfString));
            }

            mCreateAt.setText(DateUtils.getDateString(WeiboParser.parseDate(mFavorite.favorited_time)));

            mRepostNum.setText(getResources().getString(R.string.text_repost_num, mStatus.r_num));
            mCommentNum.setText(getResources().getString(R.string.text_comment_num, mStatus.c_num));

            //处理转发的微博
            if (mRetweetedStatus!=null) {
                mContentSencond.setVisibility(View.VISIBLE);

                try {
                    String title="@"+mRetweetedStatus.user.screenName+":"+mRetweetedStatus.text+" ";
                    SpannableString spannableString=new SpannableString(title);
                    WeiboUtil.highlightContent(mContext, spannableString, getResources().getColor(R.color.holo_dark_item_highliht_link));
                    mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mContentSencond.setVisibility(View.GONE);
            }

            //location
            /*sAnnotation=mStatus.annotations;
            if (null==sAnnotation||sAnnotation.place==null) {
                mLoctationlayout.setVisibility(GONE);
            } else {
                if (mLoctationlayout.getVisibility()==GONE) {
                    mLoctationlayout.setVisibility(VISIBLE);
                }
                mLocation.setText(sAnnotation.place.title);
            }*/

            //WeiboLog.d("update,updateFlag:"+updateFlag);
            loadPicture(updateFlag, cache);

            loadPortrait(updateFlag, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 由于现在只显示一张微博图片，就是如果原创内容有图片，转发的就没有，如果转发的有图片，原创的就没有。
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    void loadPicture(boolean updateFlag, boolean cache) {
        //获取原创微博内容图片
        String reBmid=null;
        String reThum=null;
        //result url
        Status mStatus=mFavorite.mStatus;
        String midImageUrl=mStatus.bmiddlePic;
        String thumImageUrl=mStatus.thumbnailPic;

        //处理转发的微博
        if (mRetweetedStatus!=null) {
            //获取转发微博内容图片.
            reBmid=mRetweetedStatus.bmiddlePic;
            reThum=mRetweetedStatus.thumbnailPic;
        }

        if (TextUtils.isEmpty(thumImageUrl)) {  //认为如果原创内容没有图片，就用转发的。
            thumImageUrl=reThum;
            midImageUrl=reBmid;
        }

        if (TextUtils.isEmpty(thumImageUrl)) {
            WeiboLog.v(TAG, "没有图片需要显示。");
            mStatusPicture.setVisibility(View.GONE);
            mStatusPictureLay.setVisibility(GONE);
            return;
        }

        /*if(thumImageUrl.equals(mPictureUrl)){
            WeiboLog.v(TAG, "tmp 相同的图片:"+mPictureUrl);
            return;
        }*/

        if (isShowBitmap) {
            mStatusPicture.setVisibility(View.VISIBLE);

            mPictureUrl=thumImageUrl;
            if (isShowLargeBitmap&&!mPictureUrl.endsWith("gif")) {   //gif不显示大图
                mPictureUrl=midImageUrl;
            }

            setPictureLay(mPictureUrl);

            Bitmap tmp=null;
            if (!isShowLargeBitmap) {
                tmp=ImageCache2.getInstance().getBitmapFromMemCache(mPictureUrl);
            } else {
                LruCache<String, Bitmap> lruCache=((App) App.getAppContext()).getLargeLruCache();
                tmp=lruCache.get(mPictureUrl);
            }

            //WeiboLog.v(TAG, "cached.tmp:"+tmp+" mPictureUrl:"+mPictureUrl);
            if (null!=tmp&&!tmp.isRecycled()) {
                mStatusPicture.setImageBitmap(tmp);
            } else {
                if (!updateFlag) {
                    mStatusPicture.setImageResource(R.drawable.image_loading);
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
                mStatusPicture.setImageResource(R.drawable.image_loading);
                //DownloadPool.downloading.put(mPictureUrl, new WeakReference<View>(parent));
                ((App) App.getAppContext()).mDownloadPool.Push(
                    mHandler, mPictureUrl, Constants.TYPE_PICTURE, cache, mCacheDir+dir, mStatusPicture);
            }
        } else {
            mStatusPicture.setVisibility(View.GONE);
            mStatusPictureLay.setVisibility(GONE);
        }
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
        if (isShowBitmap) {
            Status mStatus=mFavorite.mStatus;
            User user=mStatus.user;
            if (null==user||TextUtils.isEmpty(user.profileImageUrl)) {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                return;
            }
            String profileImgUrl=mStatus.user.profileImageUrl;

            mPortraitUrl=profileImgUrl;
            //获取头像.
            Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(mPortraitUrl);
            if (null!=bitmap&&!bitmap.isRecycled()) {
                mPortrait.setImageBitmap(bitmap);
            } else {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                if (updateFlag) {
                    //DownloadPool.downloading.put(mPortraitUrl, new WeakReference<View>(parent));
                    ((App) App.getAppContext()).mDownloadPool
                        .Push(mHandler, mPortraitUrl, Constants.TYPE_PORTRAIT, cache, mCacheDir+Constants.ICON_DIR, mPortrait);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        Status mStatus=mFavorite.mStatus;
        if (mPortrait==view) {
            WeiboLog.d(TAG, "onClick:");
            WeiboOperation.toViewStatusUser(mContext, mStatus.user, UserFragmentActivity.TYPE_USER_INFO);
            return;
        }

        String imgUrl;

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
        });
    }

    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*int what=msg.what;
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

                ListView listView=(ListView) viewWeakReference.get();
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
                }
            } else {
                WeiboLog.d(TAG, "bitmap is null:"+imgUrl);
            }
            DownloadPool.downloading.remove(imgUrl);*/
        }
    };

}