package cn.archko.microblog.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.settings.AppSettings;
import cn.archko.microblog.smiley.AKSmileyParser;
import cn.archko.microblog.ui.PrefsActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.TagsViewGroup;
import com.andrew.apollo.cache.ImageCache;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.AKSpannableStringBuilder;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.ImageManager;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;

/*import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;*/

/**
 * 显示一条微博完整的信息.不包含它的评论列表.
 *
 * @author root date:2012-12-15
 */
public class StatusDetailFragment extends AbstractBaseFragment {

    public static final String TAG="StatusDetailFragment";
    /**
     * 当前登录用户的id
     */
    private ImageView mPortrait;
    private TextView mName;
    TextView mRepostNum, mCommentNum, mCreateAt, mSourceFrom;
    //TextView mRepostLabel, mCommentLabel;
    /**
     * 用户头像
     */
    String portraitUrl;

    /**
     * 微博的标题与转发微博的标题（如果存在）
     */
    private TextView mContentFirst, mContentSencond;
    protected LinearLayout mContentSecondLayout;
    //private ImageView mStatusPicture;

    private Status mStatus=null;
    /**
     * 显示的是微博内容
     */
    private RelativeLayout mHeaderLayout;
    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what=msg.what;
            Integer progress=(Integer) msg.obj;
            switch (what) {
                case 1:
                    //mProgressBar.setProgress(progress);
                    break;

                case 2:
                    //mProgressBar.setMax(progress);
                    break;

                case 3:
                    //mProgressBar.setVisibility(View.GONE);
                    break;
            }
        }
    };
    /**
     * 下载进度
     */
    //private ProgressBar mProgressBar;
    int lastItem=0;   //ListView中最后一项位置
    boolean autoLoading=true;   //暂时无用
    int page=1;//当前页序号,需要靠它识别已经加载的页.
    //ImageView mStatusPictureLay;
    String mBmiddlePic; //中等图片url。
    TextView mRetRepostNum, mRetCommentNum;
    View mLayRetNum;
    /**
     * 是否需要下载
     */
    boolean downloadImage=true;
    /**
     * 是否正在下载原图，如果正在下载就不会再下载了。
     */
    boolean isDownloadingOri=false;
    /**
     * 是否正在刷新微博
     */
    boolean isRefreshing=false;
    /**
     * 是否显示大图片
     */
    protected boolean showBitmap=true;
    /**
     * 是否在列表中显示大的位图，只有在下面的显示列表图片时，才有效。
     */
    protected boolean showLargeBitmap=false;
    //RelativeLayout mTitleBar;
    LinearLayout mViewComment;
    private String mCacheDir;
    //protected DisplayImageOptions options;

    //TODO 需要更新主页的存储数据。
    private void refreshStatus() {
        if (!isRefreshing) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    isRefreshing=true;
                    try {
                        //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
                        SinaStatusApi weiboApi2=new SinaStatusApi();
                        weiboApi2.updateToken();
                        Status status=weiboApi2.getStatusShow(mStatus.id);
                        if (null!=status) {
                            mStatus=status;
                            if (isResumed()) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setStatusContent();
                                    }
                                }, 0l);
                            }
                        }
                    } catch (Exception e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotifyUtils.showToast("刷新失败！");
                            }
                        });
                    } finally {
                        isRefreshing=false;
                    }
                }
            }).start();
        } else {
            NotifyUtils.showToast("刷新未完成，请稍候！");
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "刷新未完成，请稍候！");
            }
        }
    }

    /**
     * 调用系统的图片查看软件来查看图片
     *
     * @param file
     */
    void showBitmapBySys(final File file) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(file), "image/png");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    NotifyUtils.showToast(R.string.image_activity_not_found);
                    showBitmapLocal(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 显示本地图片在微博内容中。
     *
     * @param file
     */
    private void showBitmapLocal(File file) {
        /*try {
            mStatusPicture.setImageBitmap(null);
            Bitmap bitmap=null;
            bitmap=ImageCache2.getInstance().getImageManager().loadFullBitmapFromSys(file.getAbsolutePath(), -1);
            if (null!=bitmap) {
                WeiboLog.d(TAG, "width："+bitmap.getWidth()+" height:"+bitmap.getHeight());
                mStatusPicture.setImageBitmap(bitmap);
                mStatusPicture.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 用图库显示大图
     */
    private void viewLargeBitmap() {
        String dir=mCacheDir;
        String name=mBmiddlePic;
        if (name.endsWith("gif")) {
            dir+=Constants.GIF;
        } else {
            dir+=Constants.PICTURE_DIR;
        }

        String path=dir+WeiboUtils.getWeiboUtil().getMd5(name)+WeiboUtils.getExt(name);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "viewLargeBitmap:"+path);
        }

        File file=new File(path);
        showBitmapBySys(file);
    }

    /**
     * 下载原图，用系统图库显示，如果没有图库，不作处理。
     */
    private void downOriImage() {
        if (isDownloadingOri) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("正在下载原图。");
            }
            return;
        }

        String dir=AppSettings.current().mCacheDir+Constants.PICTURE_DIR;
        String originalPic=mStatus.originalPic;

        if (TextUtils.isEmpty(originalPic)) {  //认为如果原创内容没有图片，就用转发的。
            Status rStatus=mStatus.retweetedStatus;
            if (null!=rStatus) {
                originalPic=rStatus.originalPic;
            }
        }

        if (TextUtils.isEmpty(originalPic)) {
            WeiboLog.e("可惜没有看到原图的url。");
            return;
        }

        if (originalPic.endsWith("gif")) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("gif的图片不下载原图。");
            }
            //return;
        }

        String path=dir+WeiboUtils.getWeiboUtil().getMd5(originalPic)+WeiboUtils.getExt(originalPic);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "原图.path:"+path);
        }
        final File file=new File(path);
        if (file.exists()) {
            showBitmapBySys(file);
            return;
        }

        isDownloadingOri=true;
        //mProgressBar.setVisibility(View.VISIBLE);
        //mProgressBar.setProgress(0);
        doDownloadOrig(originalPic, file);
    }

    void doDownloadOrig(final String url, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result=downloadFile(url, file);
                isDownloadingOri=false;
                if (result) {
                    showBitmapBySys(file);
                } else {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotifyUtils.showToast("下载图片失败。");
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 创建收藏.
     */
    public void createFavorite() {
        /*Status status = mStatus;
        Intent taskService = new Intent(getActivity(), SendTaskService.class);
        SendTask task = new SendTask();
        task.uid = currentUserId;
        task.userId = currentUserId;
        task.content = status.text;
        task.source = String.valueOf(status.id);
        task.type = TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV;
        task.createAt = new Date().getTime();
        taskService.putExtra("send_task", task);
        getActivity().startService(taskService);
        NotifyUtils.showToast("新收藏任务添加到队列服务中了。");*/
        mWeiboController.createFavorite(mStatus, currentUserId, TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV, getActivity());
    }

    /**
     * 快速转发
     */
    public void quickRepostStatus() {
        /*Intent taskService = new Intent(getActivity(), SendTaskService.class);
        SendTask task = new SendTask();
        task.uid = currentUserId;
        task.userId = currentUserId;
        task.content = "";
        task.source = String.valueOf(mStatus.id);
        task.data = "0";
        task.type = TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
        task.createAt = new Date().getTime();
        taskService.putExtra("send_task", task);
        getActivity().startService(taskService);
        NotifyUtils.showToast("转发任务添加到队列服务中了。");*/
        mStatus.text="";
        mWeiboController.quickRepostStatus(mStatus, currentUserId, getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        this.currentUserId=aUserId;

        Intent intent=getActivity().getIntent();
        Serializable status=intent.getSerializableExtra("status");
        if (status==null) {
            WeiboLog.e(TAG, "没有传来微博.");
            NotifyUtils.showToast("没有微博");
            //this.finish();
            return;
        }
        mStatus=(Status) status;
        showBitmap=mPrefs.getBoolean(PrefsActivity.PREF_COMMENT_STATUS_BM, true);
        showLargeBitmap=mPrefs.getBoolean(PrefsActivity.PREF_COMMENT_STATUS_BM, false);

        mCacheDir=AppSettings.current().mCacheDir;

        /*options = new DisplayImageOptions.Builder()

            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();*/
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickMethod(view);
        }

        private void clickMethod(View view) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "click view:"+view);
            }
            int id=view.getId();
            /*if (id==R.id.status_picture) {
                String dir=mCacheDir;
                String name=mBmiddlePic;
                if (name.endsWith("gif")) {
                    dir+=Constants.GIF;

                    String path=dir+WeiboUtils.getWeiboUtil().getMd5(name)+WeiboUtils.getExt(name);
                    WeiboLog.d(TAG, "viewLargeBitmap:"+path);
                    File gif=new File(path);
                    if (gif.exists()) {
                        //TODO 重复计算了文件的路径。
                        Intent intent=new Intent(StatusDetailFragment.this.getActivity(), ImageViewerActivity.class);

                        intent.putExtra("thumbs", new String[]{path});
                        intent.putExtra("pos", 0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        StatusDetailFragment.this.startActivity(intent);
                    } else {
                        NotifyUtils.showToast("请等待图片下载完成才可查看gif动画。");
                    }
                } else {
                    if (!showBitmap) {
                        //showToast("开始下载中等图片！");
                        //mProgressBar.setVisibility(View.VISIBLE);
                        new Thread(pictureRunnable).start();
                    }
                }
            } else if (id==R.id.repost_label) {
                repostStatus();
            } else if (id==R.id.comment_label) {
                commentStatus();
            } else if (id==R.id.ly_view_comment) {

            }*/
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.status_detail, container, false);
        initViews(root);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setStatusContent();
        boolean shouldRefresh=getActivity().getIntent().getBooleanExtra("refresh", false);
        if (shouldRefresh) {
            refreshStatus();
        }

        //mStatusPicture.setOnClickListener(clickListener);
        mPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareMenu(mPortrait);
            }
        });
    }

    private void initViews(View view) {
        mName=(TextView) view.findViewById(R.id.tv_name);
        mRepostNum=(TextView) view.findViewById(R.id.repost_num);
        mCommentNum=(TextView) view.findViewById(R.id.comment_num);
        mCreateAt=(TextView) view.findViewById(R.id.txt_send_time);
        mCreateAt.setVisibility(View.VISIBLE);
        mSourceFrom=(TextView) view.findViewById(R.id.source_from);
        mPortrait=(ImageView) view.findViewById(R.id.iv_portrait);
        //mProgressBar=(ProgressBar) view.findViewById(R.id.progress_bar);

        /*mRepostLabel=(TextView) view.findViewById(R.id.repost_label);
        mCommentLabel=(TextView) view.findViewById(R.id.comment_label);
        mRepostLabel.setOnClickListener(clickListener);
        mCommentLabel.setOnClickListener(clickListener);*/

        view.findViewById(R.id.lay_from).setVisibility(View.GONE);

        // content
        mHeaderLayout=(RelativeLayout) view.findViewById(R.id.header_layout);
        mContentFirst=(TextView) view.findViewById(R.id.tv_content_first);
        mContentSencond=(TextView) view.findViewById(R.id.tv_content_sencond);
        mContentSecondLayout=(LinearLayout) view.findViewById(R.id.tv_content_sencond_layout);
        /*mStatusPicture=(ImageView) view.findViewById(R.id.status_picture);
        mStatusPictureLay=(ImageView) view.findViewById(R.id.status_picture_lay);*/
        mRetRepostNum=(TextView) view.findViewById(R.id.ret_repost_num);
        mRetCommentNum=(TextView) view.findViewById(R.id.ret_comment_num);
        mLayRetNum=view.findViewById(R.id.lay_ret_num);

        //mTitleBar=(RelativeLayout) view.findViewById(R.id.title_bar);
        mViewComment=(LinearLayout) view.findViewById(R.id.ly_view_comment);
        //mViewComment.setOnClickListener(clickListener);
        mTagsViewGroup=(TagsViewGroup) view.findViewById(R.id.tags);
        mLeftSlider=(TextView) view.findViewById(R.id.left_slider);

        //mHeaderLayout.setOnClickListener(clickListener);
        //mStatusPicture.setOnClickListener(clickListener);
        mPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareMenu(mPortrait);
            }
        });

        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        float pref_content_font_size=options.getInt(PreferenceUtils.PREF_CONTENT_FONT_SIZE, 16);
        float pref_ret_content_font_size=options.getInt(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE, 16);

        int pref_content_color=PreferenceUtils.getInstace(App.getAppContext()).getDefaultStatusThemeColor(App.getAppContext());
        int pref_ret_content_color=PreferenceUtils.getInstace(App.getAppContext()).getDefaultRetContentThemeColor(App.getAppContext());

        if (mContentFirst.getTextSize()!=pref_content_font_size) {
            mContentFirst.setTextSize(pref_content_font_size);
        }
        if (mContentSencond.getTextSize()!=pref_ret_content_font_size) {
            mContentSencond.setTextSize(pref_ret_content_font_size);
        }
        mContentFirst.setTextColor(pref_content_color);
        mContentSencond.setTextColor(pref_ret_content_color);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("onStop");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadImage=false;
    }

    /**
     * 设置微博的内容
     */
    private void setStatusContent() {
        if (null==mStatus) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "Fragment结束了,或者微博内容为空.不更新内容");
            }
            return;
        }

        User user=mStatus.user;
        try {
            mName.setText(user.screenName);
        } catch (Exception e) {
        }

        mRepostNum.setText(getResources().getString(R.string.text_repost_num, mStatus.r_num));
        mCommentNum.setText(getResources().getString(R.string.text_comment_num, mStatus.c_num));

        WeiboLog.v(TAG, "createAt:"+mStatus.createdAt);
        mCreateAt.setText(DateUtils.getDateString(mStatus.createdAt));

        String source=mStatus.source;
        if (!TextUtils.isEmpty(source)) {
            Matcher atMatcher=WeiboUtils.comeFrom.matcher(source);
            if (atMatcher.find()) {
                int start=atMatcher.start();
                int end=atMatcher.end();
                String cfString=source.substring(end, source.length()-4);
                mSourceFrom.setText(cfString);
            }
        }
        WeiboLog.v(TAG, "source:"+source+" showBitmap:"+showBitmap);

        String title=mStatus.text;
        WeiboLog.v(TAG, "title:"+title);

        AKSpannableStringBuilder spannableString=(AKSpannableStringBuilder) mStatus.mStatusSpannable;
        if (null==spannableString) {
            spannableString=new AKSpannableStringBuilder(buildSmile(title));
            AKUtils.highlightAtClickable(getActivity(), spannableString, WeiboUtils.ATPATTERN);
            AKUtils.highlightUrlClickable(getActivity(), spannableString, WeiboUtils.getWebPattern());
            mStatus.mStatusSpannable=spannableString;
        }
        mContentFirst.setText(spannableString, TextView.BufferType.SPANNABLE);
        mContentFirst.setMovementMethod(LinkMovementMethod.getInstance());

        String imgUrl=null;

        if (!TextUtils.isEmpty(imgUrl)) {
            mBmiddlePic=imgUrl;
        }

        if (null!=user) {
            imgUrl=user.profileImageUrl;
        }

        WeiboLog.v(TAG, "imgUrl:"+imgUrl);
        if (!TextUtils.isEmpty(imgUrl)) {
            portraitUrl=imgUrl;
            //new Thread(portraitRunnable).start();
            /*ImageLoader imageLoader=ImageLoader.getInstance();
            imageLoader.displayImage(portraitUrl, mPortrait, options);*/
            ApolloUtils.getImageFetcher(getActivity()).startLoadImage(portraitUrl, mPortrait);
        }

        Status retweetStatus=mStatus.retweetedStatus;
        if (null!=retweetStatus) {
            user=retweetStatus.user;
            if (null==user) {
                title=retweetStatus.text;
            } else {
                title="@"+retweetStatus.user.screenName+":"+retweetStatus.text;
            }
            //WeiboLog.i(TAG, "retweetTitle:"+title);
            spannableString=(AKSpannableStringBuilder) mStatus.mRetweetedSpannable;
            if (null==spannableString) {
                spannableString=new AKSpannableStringBuilder(title);
                AKUtils.highlightAtClickable(getActivity(), spannableString, WeiboUtils.ATPATTERN);
                AKUtils.highlightUrlClickable(getActivity(), spannableString, WeiboUtils.getWebPattern());
                mStatus.mRetweetedSpannable=spannableString;
            }
            mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
            mContentSencond.setMovementMethod(LinkMovementMethod.getInstance());
            mLayRetNum.setVisibility(View.VISIBLE);

            try {
                mRetRepostNum.setText(getString(R.string.text_repost_num, retweetStatus.r_num));
                mRetCommentNum.setText(getString(R.string.text_comment_num, retweetStatus.c_num));
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "r_num:"+retweetStatus.r_num+" c_num:"+retweetStatus.c_num);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mContentSencond.setVisibility(View.GONE);
            mContentSecondLayout.setVisibility(View.GONE);
            mHeaderLayout.findViewById(R.id.ret_layout).setVisibility(View.GONE);
            mLayRetNum.setVisibility(View.GONE);
        }

        loadPicture(true, true);
    }

    /**
     * 构建表情
     *
     * @param charToBuild
     * @return
     */
    private CharSequence buildSmile(String charToBuild) {
        AKSmileyParser parser=AKSmileyParser.getInstance(getActivity());
        CharSequence newChar=parser.addSmileySpans(charToBuild);
        return newChar;
    }

    //--------------------- 菜单操作 ---------------------

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, 0, R.string.user_view_user);
        menuBuilder.getMenu().add(0, Constants.OP_ID_STATUS, 0, R.string.opb_user_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_AT, 0, R.string.opb_at);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        if (menuId==Constants.OP_ID_VIEW_USER) {
            Status status=mStatus;
            mWeiboController.viewUser(status.user, getActivity(), UserFragmentActivity.TYPE_USER_INFO);
        } else if (menuId==Constants.OP_ID_STATUS) {
            Status status=mStatus;
            mWeiboController.viewUser(status.user, getActivity(), UserFragmentActivity.TYPE_USER_TIMELINE);
        } else if (menuId==Constants.OP_ID_AT) {
            try {
                Status status=mStatus;
                User user=status.user;
                WeiboOperation.toAtUser(getActivity(), user.screenName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (null!=mStatus) {
            boolean hasImage=false;
            String thumbUrl=mStatus.thumbnailPic;
            if (!TextUtils.isEmpty(thumbUrl)) {
                hasImage=true;
            } else {
                Status retStatus=mStatus.retweetedStatus;
                if (null!=retStatus) {
                    thumbUrl=retStatus.thumbnailPic;
                    if (!TextUtils.isEmpty(thumbUrl)) {
                        hasImage=true;
                    }
                }
            }

            //if (!hasImage) {
            menu.findItem(R.id.menu_show_in_gallery).setVisible(false);
            menu.findItem(R.id.menu_download_ori_img).setVisible(false);
            //}

            //menu.findItem(R.id.menu_more).setVisible(true);
        }

        /*menu.add("test").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(R.id.menu_nav, 100, 0, "test group").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "item:"+item);
        }
        processMenuItemSelected(itemId);

        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理菜单，因为多了一个菜单，所以要放在一起，如果这个可行，以后就不用硬件菜单按钮了。
     * Activity的菜单事件也会传入这里.
     *
     * @param itemId
     */
    private void processMenuItemSelected(int itemId) {
        if (itemId==R.id.menu_refresh) {
            refreshStatus();
        } else if (itemId==R.id.menu_quick_comment) {
        } else if (itemId==R.id.menu_quick_repost) {
            //quickRepostStatus();  //activity中已经有了.
        } else if (itemId==R.id.menu_comment) {
            //commentStatus();
        } else if (itemId==R.id.menu_favorite) {
            createFavorite();
        } else if (itemId==R.id.menu_download_ori_img) {
            downOriImage();
        } else if (itemId==R.id.menu_show_in_gallery) {
            viewLargeBitmap();
        } else if (itemId==R.id.menu_steal) {
            Status retStatus=mStatus.retweetedStatus;
        } else if (itemId==R.id.menu_more) {
            //moreAction();
        }
    }

    //--------------------- 认证 ---------------------

    /**
     * 下载头像线程
     */
    Runnable portraitRunnable=new Runnable() {

        @Override
        public void run() {
            loadPortrait();
        }

        private void loadPortrait() {
            Bitmap bitmap=ImageCache.getInstance(getActivity()).getBitmapFromMemCache(portraitUrl);
            if (null!=bitmap) {
                udpatePortrait(bitmap);
                return;
            }
            String dir=mCacheDir;
            dir+=Constants.ICON_DIR;

            if (showBitmap) {
                bitmap=new ImageManager().getBitmapFromDiskOrNet(portraitUrl, dir, true);
                if (null!=bitmap) {
                    udpatePortrait(bitmap);
                    return;
                }
            }
        }
    };

    Runnable pictureRunnable=new Runnable() {

        @Override
        public void run() {
            loadPicture();
        }

        private void loadPicture() {
            String dir=mCacheDir+Constants.PICTURE_DIR;
            if (mBmiddlePic.endsWith("gif")) {
                dir=mCacheDir+Constants.GIF;
            }

            String path=dir+WeiboUtils.getWeiboUtil().getMd5(mBmiddlePic)+WeiboUtils.getExt(mBmiddlePic);
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "DownloadThread.path:"+path);
            }
            final File file=new File(path);
            if (file.exists()) {
                showImage(file);
                return;
            }
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "需要下载图片！");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NotifyUtils.showToast("开始下载中等图片！");
                }
            });

            boolean result=downloadFile(mBmiddlePic, file);
            if (!isResumed()) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "Fragment结束了,图片下载失败.");
                }
                return;
            }

            if (result) {
                showImage(file);
            } else {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        NotifyUtils.showToast("下载图片失败。");
                    }
                });
            }
        }
    };

    private void udpatePortrait(final Bitmap bitmap) {
        if (!isResumed()) {
            return;
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mPortrait.setImageBitmap(bitmap);
            }
        });
    }

    private void udpatePicture(final Bitmap bitmap) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                //mProgressBar.setVisibility(View.GONE);
                /*mStatusPicture.setVisibility(View.VISIBLE);
                mStatusPicture.setImageBitmap(bitmap);*/
            }
        });
    }

    /**
     * 下载图片
     *
     * @param downloadUrl  图片地址
     * @param saveFilePath 存储的绝对路径
     * @return
     */
    public boolean downloadFile(String downloadUrl, File saveFilePath) {
        int fileSize=-1;
        int downFileSize=0;
        boolean result=false;
        int progress=0;

        try {
            URL url=new URL(downloadUrl);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            if (null==conn) {
                try {
                    saveFilePath.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            // 读取超时时间 毫秒级
            conn.setReadTimeout(180000);
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", AbsApiImpl.USERAGENT);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode()==HttpURLConnection.HTTP_OK) {
                fileSize=conn.getContentLength();
                InputStream is=conn.getInputStream();
                FileOutputStream fos=new FileOutputStream(saveFilePath);
                byte[] buffer=new byte[2048];
                int i=0;
                Message msg;

                msg=Message.obtain();
                msg.what=2;
                msg.obj=fileSize;

                while ((i=is.read(buffer))!=-1) {
                    if (!downloadImage) {
                        try {
                            saveFilePath.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    downFileSize=downFileSize+i;
                    // 下载进度
                    progress=(int) (downFileSize*100.0/fileSize);
                    fos.write(buffer, 0, i);

                    msg=Message.obtain();
                    msg.what=1;
                    msg.obj=progress;
                    mHandler.sendMessage(msg);
                }
                fos.flush();
                fos.close();
                is.close();

                /*msg=Message.obtain();
                msg.what=3;
                mHandler.sendMessage(msg);*/
                result=true;
            } else {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "code:"+conn.getResponseCode()+" message:"+conn.getResponseMessage());
                }
                result=false;
            }
        } catch (Exception e) {
            result=false;
            WeiboLog.e(TAG, "downloadFile catch Exception:", e);
        }
        return result;
    }

    void showImage(final File file) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                //mProgressBar.setProgress(100);
                //StatusDetailFragment.this.mProgressBar.setVisibility(View.GONE);
                if (mBmiddlePic.endsWith("gif")) {
                    Bitmap bitmap=null;
                    bitmap=new ImageManager().loadFullBitmapFromSys(file.getAbsolutePath(), -1);
                    if (null!=bitmap) {
                        if (WeiboLog.isDEBUG()) {
                            WeiboLog.d(TAG, "width："+bitmap.getWidth()+" height:"+bitmap.getHeight());
                        }
                        /*mStatusPicture.setImageBitmap(bitmap);
                        mStatusPicture.setVisibility(View.VISIBLE);*/
                    } else {
                        deleteFileIfNeeded(file);
                    }
                } else {
                    Bitmap bitmap=null;
                    bitmap=new ImageManager().loadFullBitmapFromSys(file.getAbsolutePath(), -1);
                    if (null!=bitmap) {
                        if (WeiboLog.isDEBUG()) {
                            WeiboLog.d(TAG, "width："+bitmap.getWidth()+" height:"+bitmap.getHeight());
                        }
                        /*mStatusPicture.setImageBitmap(bitmap);
                        mStatusPicture.setVisibility(View.VISIBLE);*/
                    } else {
                        deleteFileIfNeeded(file);
                    }
                }
            }
        });
    }

    /**
     * 在列表中如果显示大图，在详细页面查看，会得到未下载完成的图片，但是文件是存在的，不能删除
     *
     * @param file
     */
    private void deleteFileIfNeeded(File file) {
        try {
            boolean slb="1".equals(mPrefs.getString(PrefsActivity.PREF_RESOLUTION, getString(R.string.default_resolution)));
            if (!slb) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TagsViewGroup mTagsViewGroup;
    ImageAdapter mAdapter;
    protected TextView mLeftSlider; //转发内容的左侧

    void loadPicture(boolean updateFlag, boolean cache) {
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
            mAdapter=new ImageAdapter(getActivity(), thumbs);
            mTagsViewGroup.setAdapter(mAdapter);
        } else {
        }
        mAdapter.setUpdateFlag(updateFlag);
        mAdapter.setCache(cache);
        mAdapter.setImageUrls(thumbs);
        mAdapter.notifyDataSetChanged();

        if (null==thumbs||thumbs.length==0) {
            //mTagsViewGroup.setAdapter(null);
            mTagsViewGroup.setVisibility(View.GONE);
            //WeiboLog.v(TAG, "setAdapter.没有图片需要显示。"+mStatus.text);
            return;
        }

        if (mTagsViewGroup.getVisibility()==View.GONE) {
            mTagsViewGroup.setVisibility(View.VISIBLE);
        }
    }
}
