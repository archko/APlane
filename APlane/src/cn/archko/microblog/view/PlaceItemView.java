package cn.archko.microblog.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.ImageAdapter;
import cn.archko.microblog.settings.AppSettings;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.AKSpannableStringBuilder;
import com.me.microblog.bean.Status;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.view.IBaseItemView;

import java.util.regex.Matcher;

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class PlaceItemView extends BaseItemView implements IBaseItemView {

    private static final String TAG="ThreadBeanItemView";
    private TagsViewGroup mTagsViewGroup;
    ImageAdapter mAdapter;

    public PlaceItemView(Context context, boolean updateFlag,
        boolean cache) {
        super(context, updateFlag);

        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.home_time_line_item, this);

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
        mContentFirst.setOnTouchListener(this);
        mContentSencond.setOnTouchListener(this);

        mLeftSlider.setBackgroundResource(R.color.orange500);
    }

    @Override
    public void update(final Status bean, boolean updateFlag, boolean cache) {
        if (mStatus==bean) {
            WeiboLog.v(TAG, "相同的内容不更新。");
            if (updateFlag) {   //需要加载数据,否则会无法更新列表的图片.
                loadPicture(updateFlag, cache);
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

            String title=(mStatus.text);
            AKSpannableStringBuilder spannableString=(AKSpannableStringBuilder) mStatus.mStatusSpannable;
            if (null==spannableString) {
                spannableString=new AKSpannableStringBuilder(mStatus.text);
                AKUtils.highlightAtClickable(mContext, spannableString, WeiboUtils.ATPATTERN);
                AKUtils.highlightUrlClickable(mContext, spannableString, WeiboUtils.getWebPattern());
                mStatus.mStatusSpannable=spannableString;
            }
            mContentFirst.setText(spannableString, TextView.BufferType.SPANNABLE);
            //mContentFirst.setMovementMethod(LinkMovementMethod.getInstance());

            if (null==mStatus.user) {
                WeiboLog.i(TAG, "微博可能被删除，无法显示！");
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
                if (null!=mContentSecondLayout) {
                    mContentSecondLayout.setVisibility(GONE);
                }
                return;
            }

            String source=mStatus.source;
            Matcher atMatcher=WeiboUtils.comeFrom.matcher(source);
            if (atMatcher.find()) {
                int start=atMatcher.start();
                int end=atMatcher.end();
                String cfString=source.substring(end, source.length()-4);
                mSourceFrom.setText(mContext.getString(R.string.text_come_from, cfString));
            }

            mCreateAt.setText(DateUtils.getDateString(mStatus.createdAt));

            mRepostNum.setText(getResources().getString(R.string.text_repost_num, mStatus.r_num));
            mCommentNum.setText(getResources().getString(R.string.text_comment_num, mStatus.c_num));

            //处理转发的微博
            if (mRetweetedStatus!=null) {
                mContentSencond.setVisibility(View.VISIBLE);

                try {
                    title="@"+mRetweetedStatus.user.screenName+":"+mRetweetedStatus.text+" ";
                    spannableString=(AKSpannableStringBuilder) mStatus.mRetweetedSpannable;
                    if (null==spannableString) {
                        spannableString=new AKSpannableStringBuilder(title);
                        AKUtils.highlightAtClickable(mContext, spannableString, WeiboUtils.ATPATTERN);
                        AKUtils.highlightUrlClickable(mContext, spannableString, WeiboUtils.getWebPattern());
                        mStatus.mRetweetedSpannable=spannableString;
                    }
                    mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
                    //mContentSencond.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mContentSencond.setVisibility(View.GONE);
                if (null!=mContentSecondLayout) {
                    mContentSecondLayout.setVisibility(GONE);
                }
            }

            //location
            sAnnotation=mStatus.annotations;
            if (null==sAnnotation||sAnnotation.place==null) {
                if (mStatus.distance>0) {
                    if (mLoctationlayout.getVisibility()==GONE) {
                        mLoctationlayout.setVisibility(VISIBLE);
                    }
                    mLocation.setText(mStatus.distance+"m");
                } else {
                    mLoctationlayout.setVisibility(GONE);
                }
            } else {
                if (mLoctationlayout.getVisibility()==GONE) {
                    mLoctationlayout.setVisibility(VISIBLE);
                }
                String loc=sAnnotation.place.title;
                if (mStatus.distance>0) {
                    loc=loc+" "+mStatus.distance+"m";
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

    @Override
    public void onClick(View view) {
        if (mPortrait==view) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "onClick:");
            }
            WeiboOperation.toViewStatusUser(mContext, mStatus.user, UserFragmentActivity.TYPE_USER_INFO);
            return;
        }
    }

    /**
     * 由于现在只显示一张微博图片，就是如果原创内容有图片，转发的就没有，如果转发的有图片，原创的就没有。
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    void loadPicture(boolean updateFlag, boolean cache) {
        String thumImageUrl=mStatus.thumbnailPic;

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
        mStatus.thumbs=new String[]{thumImageUrl};
        String[] thumbs=mStatus.thumbs; //不重复检查,在解析完成后处理.
        //WeiboLog.d(TAG, "loadPicture:"+thumbs+" o:"+mStatus.thumbnailPic);
        /*if (null==thumbs||thumbs.length==0) {
            if (null!=mStatus.retweetedStatus) {
                thumbs=mStatus.retweetedStatus.thumbs;
            }
        }*/

        AppSettings appSettings=AppSettings.current();
        if (!appSettings.showBitmap||null==thumbs||thumbs.length==0) {
            mTagsViewGroup.setAdapter(null);
            mTagsViewGroup.setVisibility(GONE);
            //WeiboLog.v(TAG, "setAdapter.没有图片需要显示。"+mStatus.text);
            return;
        }
        //WeiboLog.v(TAG, "setAdapter.有图片显示。"+mStatus.thumbs[0]);

        mTagsViewGroup.setVisibility(VISIBLE);
        //ImageAdapter adapter=(ImageAdapter) mTagsViewGroup.getAdapter();
        if (null==mAdapter) {
            mAdapter=new ImageAdapter(mContext, mStatus.thumbs);
            //mTagsViewGroup.setAdapter(mAdapter);
        } /*else*/
        {
            mAdapter.setUpdateFlag(updateFlag);
            mAdapter.setCache(cache);
            mAdapter.setImageUrls(mStatus.thumbs);
            //mAdapter.notifyDataSetInvalidated();
        }
        //不能使用更新的,需要重新设置Adapter
        /*ImageAdapter adapter=new ImageAdapter(mContext, mCacheDir, mStatus.thumbs);
        adapter.setUpdateFlag(updateFlag);
        adapter.setCache(cache);
        adapter.setShowLargeBitmap(isShowLargeBitmap);*/
        mTagsViewGroup.setAdapter(mAdapter);
    }

    @Override
    public void updateBitmap(Message msg) {
    }
}