package cn.archko.microblog.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
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
import cn.archko.microblog.ui.UserFragmentActivity;
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
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.io.Serializable;

/**
 * @version 1.00.00
 * @description: 显示用户信息的Fragment
 * @author: archko 12-5-7
 */
public class UserInfoFragment extends AbsStatusAbstraction<User> {

    public static final String TAG="UserInfoFragment";
    private TextView screeName;
    private ImageView profileImage;
    private TextView statusTitle;
    TextView mRetweetText;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeiboLog.d(TAG, "onCreate:"+this);
        mLayoutInflater=(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //mStatusImpl=new SinaUserImpl();
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

        statusTitle=(TextView) root.findViewById(R.id.tv_content_first);
        mRetweetText=(TextView) root.findViewById(R.id.tv_content_sencond);

        followButton=(Button) root.findViewById(R.id.follow);

        mGender=(ImageView) root.findViewById(R.id.gender);

        followButton.setOnClickListener(listener);
        atButton=(Button) root.findViewById(R.id.at_btn);
        atButton.setOnClickListener(listener);

        mTrack=(LinearLayout) root.findViewById(R.id.tracks);
        mController=(LinearLayout) root.findViewById(R.id.controller);

        //loadOtherLayout();

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

    /**
     * 根据用户id加载用户信息
     *
     * @param intent
     */
    /*private void loadUserById(Intent intent) {
        long userid=intent.getLongExtra("user_id", -1);
        if (-1!=userid) {
            try {
                mUser=((SWeiboApi2) App.getMicroBlog(getActivity())).getUser(userid);
                setContent(mUser);
            } catch (Exception e) {
                processGetUserError(new WeiboException(e.toString(), 500));
            }
        } else {
            String screeName=intent.getStringExtra("scree_name");
            if (null!=screeName&&!"".equals(screeName)) {
                try {
                    mUser=((SWeiboApi2) App.getMicroBlog(getActivity())).getUser(screeName);
                    setContent(mUser);
                } catch (Exception e) {
                    processGetUserError(new WeiboException(e.toString(), 500));
                }
            } else {
                WeiboLog.i(TAG, "screeName is null.");
            }
        }
    }*/

    /**
     * 根据用户昵称加载用户信息
     *
     * @param nickName 昵称
     */
    /*private void loadUserByName(String nickName) {
        try {
            if (nickName.indexOf("：")!=-1) {
                nickName=nickName.substring(0, nickName.indexOf("："));
            }
            if (nickName.startsWith("@")) {
                nickName=nickName.substring(1);
            }
            nickName.trim();
            WeiboLog.i(TAG, "要查询的nickName为:"+nickName+" ...");
            mUser=((SWeiboApi2) App.getMicroBlog(getActivity())).getUser(nickName);
            setContent(mUser);
        } catch (Exception e) {
            processGetUserError(new WeiboException(e.toString(), 500));
        }
    }*/
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

                        profileImageUrl=user.profileImageUrl;
                        LoadImageTask loadImageTask=new LoadImageTask();
                        loadImageTask.execute();

                        Status status=user.status;
                        if (null!=status) {
                            String title=status.text+" ";
                            SpannableString spannableString=new SpannableString(title);
                            WeiboUtil.highlightContent(getActivity(), spannableString, getResources().getColor(R.color.holo_dark_item_at));
                            statusTitle.setText(spannableString, TextView.BufferType.SPANNABLE);
                            statusTitle.setMovementMethod(LinkMovementMethod.getInstance());

                            Status retStatus=status.retweetedStatus;
                            if (null!=retStatus) {
                                if (retStatus.user!=null) {
                                    title="@"+retStatus.user.screenName+" ";
                                }
                                title=retStatus.text+" ";
                                spannableString=new SpannableString(title);
                                WeiboUtil.highlightContent(getActivity(), spannableString, getResources().getColor(R.color.holo_dark_item_highliht_link));
                                mRetweetText.setText(spannableString, TextView.BufferType.SPANNABLE);
                                mRetweetText.setMovementMethod(LinkMovementMethod.getInstance());
                                mRetweetText.setVisibility(View.VISIBLE);
                            }
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
                            statusTitle.setText(null);
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

    /*private void processGetUserError(WeiboException e) {
        if (e.getStatusCode()!=-1&&e.getStatusCode()==400) {
            showToast("对不起,您所查找的用户不存在.", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }*/

    View.OnClickListener listener=new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if(null==mUser){
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

        if (App.OAUTH_MODE.equalsIgnoreCase(Constants.SOAUTH_TYPE_CLIENT)) {
            FollwingTask follwingTask=new FollwingTask();
            follwingTask.execute(new Integer[]{followingType});
        } else {
            App app=(App) App.getAppContext();
            if (System.currentTimeMillis()>=app.oauth2_timestampe&&app.oauth2_timestampe!=0) {
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

    /**
     * 加载两个用户id对应用户的关系，新的api中返回的用户信息里面已经包含了是否关注的关系。
     */
    /*@Deprecated
    class LoadRelationTask extends AsyncTask<Long, Void, Relationship> {

        @Override
        protected Relationship doInBackground(Long... params) {
            try {
                long sourceId=params[0];
                long targetId=params[1];
                Relationship relationship=null;
                //relationship=((SWeiboApi2) App.getMicroBlog(getActivity())).getFriendship(sourceId, targetId);
                WeiboLog.i(TAG, "relationship:"+relationship);
                return relationship;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Relationship resultObj) {
            if (resultObj==null) {
                WeiboLog.e(TAG, "can't not find the relationship.");
                return;
            }

            Relationship relationship=resultObj;
            RelationInfo source=relationship.source;
            if (source.following) {
                setUnFollowingButton();
            } else {
                setFollowingButton();
            }
        }
    }*/
}
