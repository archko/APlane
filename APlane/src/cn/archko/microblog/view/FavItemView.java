package cn.archko.microblog.view;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.utils.AKUtils;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.AKSpannableStringBuilder;
import com.me.microblog.bean.Favorite;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
//import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.regex.Matcher;

/**
 * 修改后继承ThreadBeanItemView,多了一个Touch,左边的头像点击后的处理.
 *
 * @author: archko 11-8-24
 */
public class FavItemView extends ThreadBeanItemView implements Checkable {

    private static final String TAG="FavItemView";

    private boolean checked=false;

    protected Favorite mFavorite;    //微博收藏

    //protected DisplayImageOptions options;

    public FavItemView(Context context, ListView view, String cacheDir, Favorite status, boolean updateFlag,
        boolean cache, boolean showLargeBitmap, boolean showBitmap) {
        super(context, view, cacheDir, null, updateFlag, cache, showLargeBitmap, showBitmap);

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
            mRetweetedStatus=bean.mStatus.retweetedStatus;
            //TODO 因为现在的微博可能没有包user属性。可能被删除了。
            try {
                mName.setText(mStatus.user.screenName);
            } catch (Exception e) {
            }

            String title=mStatus.text;
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
            Matcher atMatcher= WeiboUtils.comeFrom.matcher(source);
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
}