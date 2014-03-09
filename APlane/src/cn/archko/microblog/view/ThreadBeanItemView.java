package cn.archko.microblog.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.ImageAdapter;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.Status;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.IBaseItemView;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class ThreadBeanItemView extends BaseItemView implements IBaseItemView {

    private static final String TAG="ThreadBeanItemView";
    public TagsViewGroup mTagsViewGroup;
    public ImageAdapter mAdapter;

    public ThreadBeanItemView(Context context, ListView view, String cacheDir, Status status, boolean updateFlag,
        boolean cache, boolean showLargeBitmap, boolean showBitmap) {
        super(context, view, cacheDir, status, updateFlag);

        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.home_time_line_item, this);

        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mPortrait.setOnClickListener(this);
        mName=(TextView) findViewById(R.id.tv_name);
        mRepostNum=(TextView) findViewById(R.id.repost_num);
        mCommentNum=(TextView) findViewById(R.id.comment_num);
        mContentFirst=(TextView) findViewById(R.id.tv_content_first);
        /*mStatusPicture=(ImageView) findViewById(R.id.status_picture);
        mStatusPicture.setOnClickListener(this);
        mStatusPictureLay=(ImageView) findViewById(R.id.status_picture_lay);*/
        mTagsViewGroup=(TagsViewGroup) findViewById(R.id.tags);
        mContentSencond=(TextView) findViewById(R.id.tv_content_sencond);
        mContentSecondLayout=(LinearLayout) findViewById(R.id.tv_content_sencond_layout);
        mLeftSlider=(TextView) findViewById(R.id.left_slider);
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
        mLeftSlider.setBackgroundResource(sliderColors[mIndex]);
        mIndex++;
        if (mIndex>=8) {
            mIndex=0;
        }
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
    public void update(final Status bean, boolean updateFlag, boolean cache, boolean showLargeBitmap,
        boolean showBitmap) {
        if (mStatus==bean) {
            WeiboLog.v(TAG, "相同的内容不更新。"+updateFlag);
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

            SpannableStringBuilder spannableString=(SpannableStringBuilder) mStatus.mStatusSpannable;
            if (null==spannableString) {
                spannableString=new SpannableStringBuilder(mStatus.text);
                AKUtils.highlightAtClickable(mContext, spannableString, WeiboUtil.ATPATTERN);
                AKUtils.highlightUrlClickable(mContext, spannableString, WeiboUtil.getWebPattern());
                mStatus.mStatusSpannable=spannableString;
            }
            mContentFirst.setText(spannableString, TextView.BufferType.SPANNABLE);
            mContentFirst.setMovementMethod(LinkMovementMethod.getInstance());

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
                if (mContentSencond.getVisibility()==VISIBLE) {
                    mContentSencond.setVisibility(GONE);
                }
                if (null!=mContentSecondLayout&&mContentSecondLayout.getVisibility()==VISIBLE) {
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
                mSourceFrom.setText(mContext.getString(R.string.text_come_from, cfString));
            }

            if (null!=mStatus.createdAt) {
                mCreateAt.setText(DateUtils.getDateString(mStatus.createdAt));
            } else {
                mCreateAt.setText(null);
            }

            mRepostNum.setText(getResources().getString(R.string.text_repost_num, mStatus.r_num));
            mCommentNum.setText(getResources().getString(R.string.text_comment_num, mStatus.c_num));

            //处理转发的微博
            if (mRetweetedStatus!=null) {
                if (mContentSencond.getVisibility()==GONE) {
                    mContentSencond.setVisibility(View.VISIBLE);
                }
                if (null!=mContentSecondLayout&&mContentSecondLayout.getVisibility()==GONE) {
                    mContentSecondLayout.setVisibility(VISIBLE);
                }

                try {
                    String title="@"+mRetweetedStatus.user.screenName+":"+mRetweetedStatus.text+" ";
                    spannableString=(SpannableStringBuilder) mStatus.mRetweetedSpannable;
                    if (null==spannableString) {
                        spannableString=new SpannableStringBuilder(title);
                        //WeiboUtil.highlightContent(mContext, spannableString, getResources().getColor(R.color.holo_light_item_highliht_link));
                        AKUtils.highlightAtClickable(mContext, spannableString, WeiboUtil.ATPATTERN);
                        AKUtils.highlightUrlClickable(mContext, spannableString, WeiboUtil.getWebPattern());
                        mStatus.mRetweetedSpannable=spannableString;
                    }
                    mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
                    mContentSencond.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (mContentSencond.getVisibility()==VISIBLE) {
                    mContentSencond.setVisibility(View.GONE);
                }
                if (null!=mContentSecondLayout&&mContentSecondLayout.getVisibility()==VISIBLE) {
                    mContentSecondLayout.setVisibility(GONE);
                }
            }

            //location
            sAnnotation=mStatus.annotations;
            if (null==sAnnotation||sAnnotation.place==null) {
                if (mLoctationlayout.getVisibility()==VISIBLE) {
                    mLoctationlayout.setVisibility(GONE);
                }
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
        }
    }

    /**
     * 由于现在只显示一张微博图片，就是如果原创内容有图片，转发的就没有，如果转发的有图片，原创的就没有。
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    void loadPicture(boolean updateFlag, boolean cache) {
        /*String thumImageUrl=mStatus.thumbnailPic;

        //处理转发的微博
        if (TextUtils.isEmpty(thumImageUrl)) {
            if (mRetweetedStatus!=null) {
                //获取转发微博内容图片.
                thumImageUrl=mRetweetedStatus.thumbnailPic;
            }
        }

        if (TextUtils.isEmpty(thumImageUrl)) {
            WeiboLog.v(TAG, "没有图片需要显示。");
            mTagsViewGroup.setAdapter(null);
            return;
        }
        mStatus.thumbs=new String[]{thumImageUrl};*/
        String[] thumbs=mStatus.thumbs; //不重复检查,在解析完成后处理.
        /*if (null==thumbs||thumbs.length==0) {
            if (null!=mStatus.retweetedStatus) {
                thumbs=mStatus.retweetedStatus.thumbs;
            }
        }*/
        if (null==thumbs) {
            thumbs=new String[]{};
        }

        ImageAdapter adapter=(ImageAdapter) mTagsViewGroup.getAdapter();
        //WeiboLog.d(TAG, "update adapter:"+mAdapter+" tvg:"+adapter+" mTagsViewGroup:"+mTagsViewGroup);
        mAdapter=adapter;
        if (null==mAdapter) {
            mAdapter=new ImageAdapter(mContext, mCacheDir, thumbs);
            mTagsViewGroup.setAdapter(mAdapter);
        } else {
        }
        mAdapter.setUpdateFlag(updateFlag);
        mAdapter.setCache(cache);
        mAdapter.setShowLargeBitmap(isShowLargeBitmap);
        mAdapter.setImageUrls(thumbs);
        mAdapter.notifyDataSetChanged();

        if (!isShowBitmap||null==thumbs||thumbs.length==0) {
            //mTagsViewGroup.setAdapter(null);
            if (mTagsViewGroup.getVisibility()==VISIBLE) {
                mTagsViewGroup.setVisibility(GONE);
            }
            //WeiboLog.v(TAG, "setAdapter.没有图片需要显示。"+mStatus.text);
            return;
        }
        //WeiboLog.v(TAG, "setAdapter.有图片显示。"+mStatus.thumbs[0]);

        if (mTagsViewGroup.getVisibility()==GONE) {
            mTagsViewGroup.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        WeiboLog.d(TAG, "onClick:"+view);
        if (mPortrait==view) {
            /*Intent intent=new Intent(mContext, UserFragmentActivity.class);
            intent.putExtra("nickName", mStatus.user.screenName);
            intent.putExtra("user_id", mStatus.user.id);
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_INFO);
            mContext.startActivity(intent);*/
            WeiboOperation.toViewStatusUser(mContext, mStatus.user, UserFragmentActivity.TYPE_USER_INFO);
            return;
        }
    }

    @Override
    public void updateBitmap(Message msg) {
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
            try {
                ImageView imageView=(ImageView) viewWeakReference.get();
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            WeiboLog.d(TAG, "bitmap is null:"+imgUrl);
        }
        DownloadPool.downloading.remove(imgUrl);*/
    }
}