package cn.archko.microblog.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.UserFragmentActivity;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.thread.DownloadPool;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;

/**
 * 评论列表项，评论没有图片可以添加。只有文字
 *
 * @author: archko 12-9-2
 */
public class CommentItemView extends LinearLayout implements View.OnClickListener {

    public static final String TAG="CommentItemView";
    protected Context mContext;
    protected ListView parent;
    protected String mCacheDir;    //图片缓存目录
    private TextView mName;
    private TextView mContentFirst;    //微博的内容
    protected TextView mContentSencond;  //转发微博内容
    protected LinearLayout mContentSecondLayout;
    private ImageView mPortrait;    //微博作者头像
    protected TextView mSourceFrom;    //来自
    protected TextView mCreateAt;  //发表时间
    protected String mPortraitUrl=null;
    Comment mComment;

    protected boolean isShowBitmap=true;
    protected DisplayImageOptions options;

    /**
     * 评论用到的.
     *
     * @param context
     * @param view
     * @param cacheDir           缓存目录
     * @param comment            评论实体
     * @param updateFlag         是否更新图片,如果是滚动的,不更新.
     * @param cache              是否缓存
     * @param showBitmap         是否显示图像
     * @param showSencondContent 是否显示tv_content_sencond布局,如果是在详细页面,就不需要,私信也可以考虑下.
     */
    public CommentItemView(Context context, ListView view, String cacheDir, Comment comment, boolean updateFlag,
        boolean cache, boolean showBitmap, boolean showSencondContent) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.comment_item, this);

        parent=view;
        mCacheDir=cacheDir;
        mContext=context;

        mName=(TextView) findViewById(R.id.tv_name);
        mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        mPortrait.setOnClickListener(this);
        mContentFirst=(TextView) findViewById(R.id.tv_content_first);
        mContentSencond=(TextView) findViewById(R.id.tv_content_sencond);
        mContentSecondLayout=(LinearLayout) findViewById(R.id.tv_content_sencond_layout);
        if (showSencondContent) {
            mContentSecondLayout.setVisibility(VISIBLE);
        }

        mSourceFrom=(TextView) findViewById(R.id.source_from);
        mCreateAt=(TextView) findViewById(R.id.send_time);

        isShowBitmap=showBitmap;
        //update(comment, updateFlag, cache, showBitmap);

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        float pref_title_font_size=prefs.getInt(PreferenceUtils.PREF_TITLE_FONT_SIZE, 14);
        float pref_content_font_size=prefs.getInt(PreferenceUtils.PREF_CONTENT_FONT_SIZE, 16);
        float pref_ret_content_font_size=prefs.getInt(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE, 16);

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

        options = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub)
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_error)*/
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();
    }

    @Override
    public void onClick(View view) {
        if (mPortrait==view) {
            WeiboOperation.toViewStatusUser(mContext, mComment.user, UserFragmentActivity.TYPE_USER_INFO);
            return;
        }
    }

    /**
     * 更新内容,
     *
     * @param comment    评论实体
     * @param updateFlag 是否更新图片,如果是滚动的,不更新.
     * @param cache      是否缓存图片
     * @param showBitmap 是否显示图片
     */
    public void update(final Comment comment, boolean updateFlag, boolean cache, boolean showBitmap) {
        if (mComment==comment) {
            WeiboLog.v(TAG, "相同的内容不更新。");
            if (updateFlag) {   //需要加载数据,否则会无法更新列表的图片.
                loadPortrait(updateFlag, cache);
            }
            return;
        }

        try {
            mComment=comment;
            User user=comment.user;

            if (null!=user) {
                mName.setText(user.screenName);
            }
            String txt=comment.text;
            mContentFirst.setText(txt);

            String source=comment.source;
            Matcher atMatcher=WeiboUtil.comeFrom.matcher(source);
            if (atMatcher.find()) {
                int start=atMatcher.start();
                int end=atMatcher.end();
                String cfString=source.substring(end, source.length()-4);
                mSourceFrom.setText(getResources().getString(R.string.text_come_from, cfString));
            }

            mCreateAt.setText(DateUtils.getDateString(comment.createdAt));

            txt=mComment.status.text;
            mContentSencond.setText(txt);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        loadPortrait(updateFlag, cache);
    }

    /**
     * 加载头像
     *
     * @param updateFlag 是否更新图片标记,在滚动时为false
     * @param cache      是否缓存头像.
     */
    protected void loadPortrait(boolean updateFlag, boolean cache) {
        if (isShowBitmap) {
            User user=mComment.user;
            if (null==user||TextUtils.isEmpty(user.profileImageUrl)) {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                return;
            }
            String profileImgUrl=mComment.user.profileImageUrl;

            mPortraitUrl=profileImgUrl;
            //获取头像.
            Bitmap bitmap=ImageCache2.getInstance().getBitmapFromMemCache(mPortraitUrl);
            if (null!=bitmap&&!bitmap.isRecycled()) {
                mPortrait.setImageBitmap(bitmap);
            } else {
                mPortrait.setImageResource(R.drawable.user_default_photo);
                if (updateFlag) {
                    //DownloadPool.downloading.put(mPortraitUrl, new WeakReference<View>(parent));
                    /*((App) App.getAppContext()).mDownloadPool
                        .Push(mHandler, mPortraitUrl, Constants.TYPE_PORTRAIT, cache, mCacheDir+Constants.ICON_DIR, mPortrait);*/
                    /*ImageLoader imageLoader=ImageLoader.getInstance();
                    imageLoader.displayImage(mPortraitUrl, mPortrait, options);*/
                    ApolloUtils.getImageFetcher(mContext).startLoadImage(mPortraitUrl, mPortrait);
                }
            }
        }
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
            Bitmap bitmap;//=BitmapFactory.decodeFile(bundle.getString("mName"));
            bitmap=bundle.getParcelable("name");
            if (TextUtils.isEmpty(imgUrl)||"null".equals(imgUrl)||null==bitmap) {
                WeiboLog.w(TAG, "图片url不对，"+imgUrl);
                return;
            }

            if (bitmap!=null&&!bitmap.isRecycled()) {
                ImageCache2.getInstance().addBitmapToMemCache(imgUrl, bitmap);
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

                CommentItemView itemView;
                View view;
                for (int i=0; i<childCount; i++) {
                    view=listView.getChildAt(i);
                    if (view instanceof CommentItemView) {
                        itemView=(CommentItemView) view;
                        if (itemView.mPortraitUrl!=null&&itemView.mPortraitUrl.equals(imgUrl)) {
                            itemView.mPortrait.setImageBitmap(bitmap);
                            break;
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