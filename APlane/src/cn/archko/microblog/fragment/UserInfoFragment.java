package cn.archko.microblog.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsStatusAbstraction;
import cn.archko.microblog.fragment.impl.SinaUserImpl;
import cn.archko.microblog.ui.ImageViewerActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.ui.WebviewActivity;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.00.00
 * @description: 显示用户信息的Fragment
 * @author: archko 12-5-7
 */
public class UserInfoFragment extends AbsStatusAbstraction<User> {

    public static final String TAG="UserInfoFragment";
    private TextView screeName;
    private ImageView profileImage;
    private TextView mContentFirst;
    TextView mContentSencond;
    protected LinearLayout mContentSecondLayout;
    TextView mName;
    TextView comment_num;
    TextView repost_num;
    private String profileImageUrl, pictureUrl;
    private TextView description;
    private TextView statuses, friends, followers, favourites;
    ImageView mGender;

    User mUser=null;
    private Button followButton, atButton;
    long currentUserId=-1;

    LinearLayout statusBtn, friendsBtn, followersBtn;

    private int followingType=-1;   //0表示未关注,1表示已关注,-1表示未知
    Handler mHandler=new Handler();
    /**
     * 用户是否已经加载，因为切换不同的tab时会重新调用onActivityCreated，不必每次重新加载用户
     */
    boolean isUserLoaded=false;
    /**
     * 其它信息的布局
     */
    LinearLayout mTrack;
    LayoutInflater mLayoutInflater;
    /**
     * 中间按钮的而已，默认有四个按钮，其它的需要在加载用户的信息后才有的。
     */
    LinearLayout mController;
    protected DisplayImageOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeiboLog.d(TAG, "onCreate:"+this);
        mLayoutInflater=(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //mStatusImpl=new SinaUserImpl();

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
    public void initApi() {
        mStatusImpl=new SinaUserImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.userApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            AKUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=_onCreateView(inflater, container, savedInstanceState);
        ThemeUtils.getsInstance().themeBackground(root, getActivity());
        return root;
    }

    View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView root=(ScrollView) inflater.inflate(R.layout.user_introduction, null);

        currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);

        mPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());

        screeName=(TextView) root.findViewById(R.id.screen_name);
        profileImage=(ImageView) root.findViewById(R.id.iv_portrait);
        description=(TextView) root.findViewById(R.id.description);

        followers=(TextView) root.findViewById(R.id.follower_count);
        friends=(TextView) root.findViewById(R.id.friend_count);
        favourites=(TextView) root.findViewById(R.id.favourite_count);
        statuses=(TextView) root.findViewById(R.id.status_count);

        statusBtn=(LinearLayout) root.findViewById(R.id.status_btn);
        friendsBtn=(LinearLayout) root.findViewById(R.id.friends_btn);
        followersBtn=(LinearLayout) root.findViewById(R.id.followers_btn);

        mContentFirst=(TextView) root.findViewById(R.id.tv_content_first);
        mContentSencond=(TextView) root.findViewById(R.id.tv_content_sencond);
        mName=(TextView) root.findViewById(R.id.tv_name);
        comment_num=(TextView) root.findViewById(R.id.comment_num);
        repost_num=(TextView) root.findViewById(R.id.repost_num);
        mContentSecondLayout=(LinearLayout) root.findViewById(R.id.tv_content_sencond_layout);
        mContentSecondLayout.setVisibility(View.GONE);

        followButton=(Button) root.findViewById(R.id.follow);

        mGender=(ImageView) root.findViewById(R.id.gender);

        followButton.setOnClickListener(listener);
        atButton=(Button) root.findViewById(R.id.at_btn);
        atButton.setOnClickListener(listener);

        mTrack=(LinearLayout) root.findViewById(R.id.tracks);
        mController=(LinearLayout) root.findViewById(R.id.controller);

        //loadOtherLayout();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl=mUser.profileImageUrl;
                if (!TextUtils.isEmpty(mUser.avatar_hd)) {
                    imageUrl=mUser.avatar_hd;
                } else if (!TextUtils.isEmpty(mUser.avatar_large)) {
                    imageUrl=mUser.avatar_large;
                }
                if (!TextUtils.isEmpty(imageUrl)) {
                    String[] imageUrls=new String[]{imageUrl};
                    Intent intent=new Intent(getActivity(), ImageViewerActivity.class);
                    intent.putExtra("thumbs", imageUrls);
                    intent.putExtra("pos", 0);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            }
        });

        return root;
    }

    void loadOtherLayout() {
        if (mTrack.getChildCount()>0) {
            mTrack.removeAllViews();
        }

        View container;
        TextView info;
        TextView txt;

        String mark=mUser.remark;
        container=mLayoutInflater.inflate(R.layout.user_profile_item, null);
        mTrack.addView(container);
        txt=(TextView) container.findViewById(R.id.txt);
        info=(TextView) container.findViewById(R.id.info);
        txt.setText(R.string.user_remark);
        info.setText(mark);

        String gender=mUser.gender;
        container=mLayoutInflater.inflate(R.layout.user_profile_item, null);
        mTrack.addView(container);
        txt=(TextView) container.findViewById(R.id.txt);
        info=(TextView) container.findViewById(R.id.info);
        txt.setText(R.string.user_gender);

        String genderStr=null;
        if ("f".equals(gender)) {
            genderStr=getString(R.string.user_female);
        } else if ("m".equals(gender)) {
            genderStr=getString(R.string.user_male);
        } else {
            genderStr=getString(R.string.user_unknown);
        }
        info.setText(genderStr);

        String location=mUser.location;
        container=mLayoutInflater.inflate(R.layout.user_profile_item, null);
        mTrack.addView(container);
        txt=(TextView) container.findViewById(R.id.txt);
        info=(TextView) container.findViewById(R.id.info);
        txt.setText(R.string.user_loc);
        info.setText(location);

        String url=mUser.url;
        if (!TextUtils.isEmpty(url)) {
            container=mLayoutInflater.inflate(R.layout.user_profile_item, null);
            mTrack.addView(container);
            txt=(TextView) container.findViewById(R.id.txt);
            info=(TextView) container.findViewById(R.id.info);
            txt.setText(R.string.user_url);
            info.setText(url);
        }

        String user_create_at=DateUtils.getFullDateString(mUser.createdAt);
        container=mLayoutInflater.inflate(R.layout.user_profile_item, null);
        mTrack.addView(container);
        txt=(TextView) container.findViewById(R.id.txt);
        info=(TextView) container.findViewById(R.id.info);
        txt.setText(R.string.user_create_at);
        info.setText(user_create_at);
    }

    /**
     * api无法获取其它信息
     */
    void loadOtherController() {
        /*if(mController.getChildCount()>4){
            mController.removeViews(4,mController.getChildCount());
        }*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WeiboLog.d(TAG, "onActivityCreated");

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        //setEmptyText("No phone numbers");

        // We have a menu item to show in action bar.
        //setHasOptionsMenu(true);

        Intent intent=getActivity().getIntent();
        int user_type=intent.getIntExtra("user_type", -1);
        if (!isUserLoaded) {
            Serializable u=intent.getSerializableExtra("user");
            if (u!=null) {
                mUser=(User) u;
                WeiboLog.i(TAG, "user bundle:"+mUser.screenName);
                setContent(mUser);
                loadUserAsync();
            } else {
                loadUserAsync();
            }
        } else {    //如果已经加载，因为布局重新加载了，只是用户缓存，所以需要重新载入数据。避免不了关系重载。
            //user=((UserFragmentActivity) getActivity()).mUser;
            if (null!=mUser) {
                setContent(mUser);
            }
        }

        /*if (user==null) {
            WeiboLog.i(TAG, "getUser error.");
        }*/
    }

    /**
     * 异步加载用户信息
     *
     * @param intent
     */
    private void loadUserAsync() {
        Intent intent=getActivity().getIntent();
        newTask(new Object[]{intent, 0}, null);
        /*new Thread(new Runnable() {

            @Override
            public void run() {
                Intent intent=getActivity().getIntent();
                String nickName=intent.getStringExtra("nickName");

                WeiboLog.d(TAG, "nickName:"+nickName);
                if (!TextUtils.isEmpty(nickName)) {
                    loadUserByName(nickName);
                } else {
                    loadUserById(intent);
                }
            }
        }).start();*/
    }

    private void setContent(final User user) {
        isUserLoaded=true;
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                WeiboLog.d(TAG, "setContent.user:"+user.screenName);
                if (user!=null) {
                    try {
                        mUser=user;
                        screeName.setText(user.screenName);

                        if (user.id>0) {
                            Intent intent=getActivity().getIntent();
                            intent.putExtra("user_id", user.id);
                        }

                        description.setText("简介:"+user.description);

                        followers.setText(String.valueOf(user.followersCount));
                        friends.setText(String.valueOf(user.friendsCount));
                        favourites.setText(String.valueOf(user.favouritesCount));
                        statuses.setText(String.valueOf(user.statusesCount));

                        profileImageUrl=TextUtils.isEmpty(user.avatar_large) ? user.profileImageUrl : user.avatar_large;
                        /*LoadImageTask loadImageTask=new LoadImageTask();
                        loadImageTask.execute();*/
                        ImageLoader imageLoader=ImageLoader.getInstance();
                        imageLoader.displayImage(profileImageUrl, profileImage, options);

                        Status status=user.status;
                        if (null!=status) {
                            mName.setText(DateUtils.getDateString(status.createdAt));
                            String title=status.text+" ";
                            SpannableString spannableString=new SpannableString(title);
                            //WeiboUtil.highlightContent(getActivity(), spannableString, getResources().getColor(R.color.holo_dark_item_at));
                            highlightAtClickable(spannableString, WeiboUtil.ATPATTERN);
                            highlightUrlClickable(spannableString, WeiboUtil.getWebPattern());
                            mContentFirst.setText(spannableString, TextView.BufferType.SPANNABLE);
                            mContentFirst.setMovementMethod(LinkMovementMethod.getInstance());

                            comment_num.setText(getString(R.string.text_comment)+status.c_num);
                            repost_num.setText(getString(R.string.text_repost)+status.r_num);

                            Status retStatus=status.retweetedStatus;
                            if (null!=retStatus) {
                                if (retStatus.user!=null) {
                                    title="@"+retStatus.user.screenName+" ";
                                }
                                title=retStatus.text+" ";
                                spannableString=new SpannableString(title);
                                //WeiboUtil.highlightContent(getActivity(), spannableString, getResources().getColor(R.color.holo_dark_item_highliht_link));
                                highlightAtClickable(spannableString, WeiboUtil.ATPATTERN);
                                highlightUrlClickable(spannableString, WeiboUtil.getWebPattern());
                                mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
                                mContentSencond.setMovementMethod(LinkMovementMethod.getInstance());
                                mContentSecondLayout.setVisibility(View.VISIBLE);
                            } else {
                                mContentSecondLayout.setVisibility(View.GONE);
                            }
                        } else {
                            mName.setText(null);
                        }

                        followButton.setText("");
                        if (currentUserId!=user.id) {
                            followButton.setVisibility(View.VISIBLE);
                            if (user.following) {
                                setUnFollowingButton();
                            } else {
                                setFollowingButton();
                            }
                        } else {
                            mContentFirst.setText(null);
                            //mContentSencond.setVisibility(View.GONE);
                        }

                        addButtonListener();

                        String gender=user.gender;
                        if ("f".equals(gender)) {
                            mGender.setImageResource(R.drawable.icon_female);
                        } else if ("m".equals(gender)) {
                            mGender.setImageResource(R.drawable.icon_male);
                        }

                        loadOtherLayout();
                        loadOtherController();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    AKUtils.showToast("用户信息获取失败，", Toast.LENGTH_LONG);
                }
            }
        });
    }

    /**
     * 只有在获取到用户信息后才处理按钮监听
     */
    private void addButtonListener() {
        statusBtn.setOnClickListener(listener);
        friendsBtn.setOnClickListener(listener);
        followersBtn.setOnClickListener(listener);
    }

    View.OnClickListener listener=new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (null==mUser) {
                AKUtils.showToast("需要先加载用户信息！");
                return;
            }

            switch (view.getId()) {
                case R.id.follow:
                    doFollow();
                    break;

                case R.id.status_btn:
                    changeToUserTimeline();
                    break;

                case R.id.friends_btn:
                    changeToUserFriends();
                    break;

                case R.id.followers_btn:
                    changeToUserFollowers();
                    break;

                case R.id.at_btn:
                    atStatus();
                    break;

                default:
                    break;
            }
        }

        private void atStatus() {
            if (null==mUser) {
                return;
            }

            String atString="@"+mUser.screenName+" ";
            /*Intent intent=new Intent(getActivity(), NewStatusActivity.class);
            intent.putExtra("at_some", atString);
            getActivity().startActivity(intent);*/
            WeiboOperation.toAtUser(getActivity(), atString);
        }
    };

    private void doFollow() {
        WeiboLog.i(TAG, "follow listener:"+followingType);
        if (followingType==-1) {
            return;
        }

        App app=(App) App.getAppContext();
        if (app.getOauthBean().oauthType==Oauth2.OAUTH_TYPE_WEB) {
            FollwingTask follwingTask=new FollwingTask();
            follwingTask.execute(new Integer[]{followingType});
        } else {
            if (System.currentTimeMillis()>=app.getOauthBean().expireTime&&app.getOauthBean().expireTime!=0) {
                WeiboLog.d(TAG, "web认证，token过期了.");
                mOauth2Handler.oauth2(null);
            } else {
                FollwingTask follwingTask=new FollwingTask();
                follwingTask.execute(new Integer[]{followingType});
            }
        }
    }

    /**
     * 切换到用户关注列表
     */
    private void changeToUserFriends() {
        try {
            Intent intent=getActivity().getIntent();
            intent.putExtra("user_id", mUser.id);
            intent.putExtra("screen_name", mUser.screenName);
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_FRIENDS);
            getActivity().setIntent(intent);
            ((UserFragmentActivity) getActivity()).switchTab(UserFragmentActivity.TYPE_USER_FRIENDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到用户粉丝列表
     */
    private void changeToUserFollowers() {
        try {
            Intent intent=getActivity().getIntent();
            intent.putExtra("user_id", mUser.id);
            intent.putExtra("screen_name", mUser.screenName);
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_FOLLOWERS);
            getActivity().setIntent(intent);
            ((UserFragmentActivity) getActivity()).switchTab(UserFragmentActivity.TYPE_USER_FOLLOWERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到用户微博
     */
    private void changeToUserTimeline() {
        try {
            Intent intent=getActivity().getIntent();
            intent.putExtra("user_id", mUser.id);
            intent.putExtra("screen_name", mUser.screenName);
            intent.putExtra("type", UserFragmentActivity.TYPE_USER_TIMELINE);
            getActivity().setIntent(intent);
            ((UserFragmentActivity) getActivity()).switchTab(UserFragmentActivity.TYPE_USER_TIMELINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class LoadImageTask extends AsyncTask<Void, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Void... params) {
            Bitmap bitmap=null;
            try {
                bitmap=ImageCache2.getInstance().getBitmapFromMemCache(profileImageUrl);
                Object[] resultObj=new Object[3];
                if (null!=bitmap) {
                    resultObj[0]=bitmap;
                    return resultObj;
                } else {
                    bitmap=ImageCache2.getInstance().getImageManager().getBitmapFromDiskOrNet(profileImageUrl,
                        mCacheDir+Constants.ICON_DIR, true);

                    WeiboLog.i(TAG, "profileImageurl:"+profileImageUrl);
                    resultObj[0]=bitmap;
                    return resultObj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object[] resultObj) {
            if (resultObj==null||resultObj[0]==null) {
                WeiboLog.w(TAG, "can't not find the image.");
                return;
            }

            WeiboLog.i(TAG, "bitmap:"+resultObj[0]);
            Bitmap bitmap=(Bitmap) resultObj[0];
            profileImage.setImageBitmap(bitmap);
            profileImage.setVisibility(View.VISIBLE);
        }
    }

    private void setFollowingButton() {
        followButton.setText(R.string.follow);
        followingType=0;
    }

    private void setUnFollowingButton() {
        followButton.setText(R.string.unfollow);
        followingType=1;
    }

    class FollwingTask extends AsyncTask<Integer, Void, User> {

        @Override
        protected User doInBackground(Integer... params) {
            try {
                User now=null;
                /*SWeiboApi2 weiboApi2=null;//weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
                if (null==weiboApi2) {
                    return now;
                }*/
                SinaUserApi weiboApi2=new SinaUserApi();
                weiboApi2.updateToken();

                if (followingType==0) {
                    now=weiboApi2.createFriendships(mUser.id);
                } else if (followingType==1) {
                    now=weiboApi2.deleteFriendships(mUser.id);
                }

                return now;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User resultObj) {
            if (!isResumed()) {
                return;
            }

            if (resultObj==null) {
                AKUtils.showToast("处理失败", Toast.LENGTH_LONG);
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            if (followingType==0) {
                setUnFollowingButton();
                //Toast.makeText(getApplicationContext(),"follow "+user.screenName+" successfully!",Toast.LENGTH_LONG);
                AKUtils.showToast("follow "+mUser.screenName+" successfully!", Toast.LENGTH_LONG);
            } else if (followingType==1) {
                setFollowingButton();
                //Toast.makeText(getApplicationContext(),"unfollow "+user.screenName+" successfully!",Toast.LENGTH_LONG);
                AKUtils.showToast("unfollow "+mUser.screenName+" successfully!", Toast.LENGTH_LONG);
            }
        }

    }

    /**
     * 线程结束后的操作。
     *
     * @param result
     */
    protected void basePostOperation(Object[] result) {
        if (null==result) {
            WeiboLog.w(TAG, "加载数据异常。");
            AKUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        SStatusData<User> sStatusData=(SStatusData<User>) result[0];
        if (null==sStatusData) {
            WeiboLog.w(TAG, "请求数据异常。");
            AKUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        if (!TextUtils.isEmpty(sStatusData.errorMsg)) {
            WeiboLog.w(TAG, "请求数据异常。"+sStatusData.errorMsg);
            String msg=sStatusData.errorMsg;
            AKUtils.showToast(msg, Toast.LENGTH_LONG);
            return;
        }

        if (null==sStatusData.mData) {
            WeiboLog.w(TAG, "加载数据为空。");
            AKUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        setContent(sStatusData.mData);
    }

    @Override
    protected SStatusData<User> getData(Object[] params) throws WeiboException {
        return mStatusImpl.loadData(params);
    }

    //--------------------- 内容点击器 ---------------------

    private class AtClicker extends WeiboUtil.MyClicker {

        @Override
        public void updateDrawState(TextPaint textPaint) {
            try {
                if (isResumed()) {
                    textPaint.setColor(getResources().getColor(R.color.holo_light_item_highliht_link));
                    textPaint.setUnderlineText(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            WeiboLog.d("AtClicker:"+name);
            if (TextUtils.isEmpty(name)) {
                WeiboLog.e(TAG, "nick name is null.");
                return;
            }
            WeiboOperation.toViewStatusUser(UserInfoFragment.this.getActivity(), name,
                -1, UserFragmentActivity.TYPE_USER_INFO);
        }

    }

    public void highlightAtClickable(Spannable spannable, Pattern pattern) {
        Matcher atMatcher=pattern.matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            //WeiboLog.d("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
            } else {
                if (end-start<=2) {
                    break;
                }
            }

            String name=spannable.subSequence(start, end).toString();
            AtClicker clicker=new AtClicker();
            clicker.name=name;
            spannable.setSpan(clicker, start, end, 34);
        }
    }

    public void highlightUrlClickable(Spannable spannable, Pattern pattern) {
        Matcher atMatcher=pattern.matcher(spannable);

        while (atMatcher.find()) {
            int start=atMatcher.start();
            int end=atMatcher.end();
            //WeiboLog.d("weibo", "start:"+start+" end:"+end);
            if (end-start==2) {
            } else {
                if (end-start<=2) {
                    break;
                }
            }

            String name=spannable.subSequence(start, end).toString();
            UrlClicker clicker=new UrlClicker();
            clicker.name=name;
            spannable.setSpan(clicker, start, end, 34);
        }
    }

    private class UrlClicker extends WeiboUtil.MyClicker {

        @Override
        public void updateDrawState(TextPaint textPaint) {
            try {
                if (isResumed()) {
                    textPaint.setColor(getResources().getColor(R.color.holo_light_item_highliht_link));
                    textPaint.setUnderlineText(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            WeiboLog.d("UrlClicker:"+name);
            if (TextUtils.isEmpty(name)) {
                WeiboLog.e(TAG, "url is null.");
                return;
            }
            //String str1=URLEncoder.encode(this.name);
            boolean prefWebview=mPrefs.getBoolean(PreferenceUtils.PREF_WEBVIEW, true);
            if (!prefWebview) {
                WeiboUtil.openUrlByDefaultBrowser(getActivity(), name);
            } else {
                /*Intent intent=new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra("url", name);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_right, R.anim.enter_left);*/
                WeiboOperation.startWebview(getActivity(), name);
            }
        }
    }
}
