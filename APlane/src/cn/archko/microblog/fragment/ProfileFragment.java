package cn.archko.microblog.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.action.GroupAction;
import cn.archko.microblog.fragment.abs.AbsStatusAbstraction;
import cn.archko.microblog.fragment.impl.SinaUserImpl;
import cn.archko.microblog.settings.AppSettings;
import cn.archko.microblog.utils.WeiboOperation;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.action.ActionResult;
import com.me.microblog.action.AsyncActionTask;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: TODO 这里还有一些需要处理，因为用户查看需要详细更新。
 * @author: archko 11-11-17
 */
public class ProfileFragment extends AbsStatusAbstraction<User> {

    public static final String TAG="ProfileFragment";

    private ImageView mPortrait;
    private TextView mName;
    private Button mEditBtn;
    Button mRefreshBtn;
    private TextView mAddress, mLoginame;
    private TextView mFollwwerCount, mStatusCount, mFriendCount, mTopicCount;
    String profileImageUrl;
    boolean isUserLoaded=false;
    User user=null;
    Spinner mSpinner;
    Button mUpdateSpinner;
    Button mGetFriends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.v(TAG, "onCreate:"+this);
        mPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
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
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WeiboLog.v(TAG, "onPause:"+this);
    }

    @Override
    public void onResume() {
        super.onResume();
        WeiboLog.v(TAG, "onResume:"+this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        WeiboLog.v(TAG, "onCreateView:"+this);

        View view=inflater.inflate(R.layout.ak_self_profile, container, false);
        mPortrait=(ImageView) view.findViewById(R.id.iv_portrait);
        mName=(TextView) view.findViewById(R.id.tv_name);
        mEditBtn=(Button) view.findViewById(R.id.editBtn);
        mRefreshBtn=(Button) view.findViewById(R.id.refreshBtn);
        mAddress=(TextView) view.findViewById(R.id.address);
        mLoginame=(TextView) view.findViewById(R.id.login_name);
        mFollwwerCount=(TextView) view.findViewById(R.id.followerCount);
        mStatusCount=(TextView) view.findViewById(R.id.statusCount);
        mFriendCount=(TextView) view.findViewById(R.id.friendsCount);
        mTopicCount=(TextView) view.findViewById(R.id.topicCount);
        mSpinner=(Spinner) view.findViewById(R.id.spinner);
        mUpdateSpinner=(Button) view.findViewById(R.id.spinner_btn);
        mGetFriends=(Button) view.findViewById(R.id.get_friend_btn);

        ThemeUtils.getsInstance().themeBackground(view, getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEditBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                NotifyUtils.showToast("Not implemented,");
            }
        });
        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshProfile();
            }
        });
        mUpdateSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadGroup(true);
            }
        });
        mGetFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFriends();
            }
        });

        String sn=mPrefs.getString(Constants.PREF_SCREENNAME_KEY, "");
        String location=mPrefs.getString(Constants.PREF_LOCATION, "");
        int fc=mPrefs.getInt(Constants.PREF_FOLLWWERCOUNT_KEY, 0);
        int frc=mPrefs.getInt(Constants.PREF_FRIENDCOUNT_KEY, 0);
        int sc=mPrefs.getInt(Constants.PREF_STATUSCOUNT_KEY, 0);

        String loginname=mPrefs.getString(Constants.PREF_USERNAME_KEY, "");
        mLoginame.setText(sn);

        mName.setText(sn);
        mAddress.setText(location);
        mFollwwerCount.setText(String.valueOf(fc));
        mFriendCount.setText(String.valueOf(frc));
        mStatusCount.setText(String.valueOf(sc));
        mTopicCount.setText(String.valueOf(0));

        profileImageUrl=mPrefs.getString(Constants.PREF_PORTRAIT_URL, "");
        String url=profileImageUrl;
        String avatar_large=mPrefs.getString(Constants.PREF_AVATAR_LARGE, "");
        if (!TextUtils.isEmpty(avatar_large)&&!"null".equals(avatar_large)) {
            url=avatar_large;
        }

        if (!TextUtils.isEmpty(url)) {
            WeiboLog.d(TAG, "profileImageUrl:"+url);
            LoadImageTask loadImageTask=new LoadImageTask();
            loadImageTask.execute(new Object[]{url});
        }

        if (isUserLoaded&&user!=null) {
            WeiboLog.d("用户已经加载过一次，不再重复加载。");
            setUserInfo(user);
            LoadImageTask loadImageTask=new LoadImageTask();
            loadImageTask.execute();
            return;
        }

        //--------- 上面这段几乎无用。----------
        long userId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+String.valueOf(userId)+Constants.USER_SELF_FILE;
        File file=new File(filename);
        WeiboLog.d(TAG, "filename:"+filename+" file:"+file.exists());
        if (file.exists()) {
            FileInputStream fis=null;
            try {
                fis=new FileInputStream(filename);
                BufferedInputStream br=new BufferedInputStream(fis);
                ObjectInputStream in=new ObjectInputStream(br);
                User u=(User) in.readObject();
                setUserInfo(u);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*long now=new Date().getTime()-72*3600000;
            WeiboLog.d(TAG, "lastModified:"+file.lastModified()+" now;"+now);
            if ((file.lastModified()<now)) {
                Object[] params=new Object[]{null, 1, userId};
                newTask(params, null);
            }*/
        } else {
            Object[] params=new Object[]{null, 1, userId};
            newTask(params, null);
        }

        mGroupList=new ArrayList<Group>();
        updateGroup(null);
    }

    @Override
    protected SStatusData<User> getData(Object... params) throws WeiboException {
        return mStatusImpl.loadData(params);
    }

    @Override
    public void refresh() {
        refreshProfile();
    }

    /**
     * 刷新个人资料
     */
    private void refreshProfile() {
        NotifyUtils.showToast("开始刷新，请稍等.");
        long userId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        Object[] params=new Object[]{null, 1, userId};
        newTask(params, null);
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void basePostOperation(Object[] result) {
        if (null==result) {
            WeiboLog.w(TAG, "加载数据异常。");
            NotifyUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        SStatusData<User> sStatusData=(SStatusData<User>) result[0];
        if (null==sStatusData) {
            WeiboLog.w(TAG, "请求数据异常。");
            NotifyUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        if (!TextUtils.isEmpty(sStatusData.errorMsg)) {
            WeiboLog.w(TAG, "请求数据异常。"+sStatusData.errorMsg);
            String msg=sStatusData.errorMsg;
            NotifyUtils.showToast(msg, Toast.LENGTH_LONG);
            return;
        }

        if (null==sStatusData.mData) {
            WeiboLog.w(TAG, "加载数据为空。");
            NotifyUtils.showToast(R.string.more_loaded_failed, Toast.LENGTH_LONG);
            return;
        }

        User user=sStatusData.mData;
        SharedPreferences.Editor editor=mPrefs.edit();
        editor.putString(Constants.PREF_SCREENNAME_KEY, user.screenName);
        // add 存储当前用户的id
        editor.putLong("user_id", user.id);
        editor.putLong(Constants.PREF_CURRENT_USER_ID, user.id);
        editor.putString(Constants.PREF_LOCATION, user.location);
        editor.putInt(Constants.PREF_FOLLWWERCOUNT_KEY, user.followersCount);
        editor.putInt(Constants.PREF_FRIENDCOUNT_KEY, user.friendsCount);
        editor.putInt(Constants.PREF_STATUSCOUNT_KEY, user.statusesCount);
        editor.putInt(Constants.PREF_FAVOURITESCOUNT_KEY, user.favouritesCount);
        editor.putString(Constants.PREF_TOPICCOUNT_KEY, "");
        String url=user.profileImageUrl;
        String avatar_large=user.avatar_large;
        if (!TextUtils.isEmpty(avatar_large)) {
            url=avatar_large;
        }

        editor.putString(Constants.PREF_PORTRAIT_URL, user.profileImageUrl);
        editor.putString(Constants.PREF_AVATAR_LARGE, user.avatar_large);
        editor.putBoolean(Constants.PREF_NEES_TO_UPDATE, false);
        editor.putLong(Constants.PREF_TIMESTAMP, System.currentTimeMillis());

        editor.commit();

        setUserInfo(user);

        if (!TextUtils.isEmpty(url)||!url.equals(profileImageUrl)) {
            LoadImageTask loadImageTask=new LoadImageTask();
            loadImageTask.execute(new Object[]{url});
        }
        //profileImageUrl=url;
    }

    void setUserInfo(User user) {
        ProfileFragment.this.user=user;
        mName.setText(user.screenName);
        mAddress.setText(user.location);
        mFollwwerCount.setText(String.valueOf(user.followersCount));
        mFriendCount.setText(String.valueOf(user.friendsCount));
        mStatusCount.setText(String.valueOf(user.statusesCount));
        mTopicCount.setText(String.valueOf(0));
    }

    /**
     * 下载好友,使用DialogFragment
     */
    public void getFriends() {
        FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
        Fragment prev=getActivity().getFragmentManager().findFragmentByTag("friends_dialog");
        if (prev!=null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        UserFriendListFragment friendListFragment=new UserFriendListFragment();
        friendListFragment.show(ft, "friends_dialog");
    }

    class LoadImageTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... params) {
            Bitmap bitmap=null;
            try {
                String url=(String) params[0];
                bitmap=ImageCache2.getInstance().getBitmapFromMemCache(url);
                Object[] resultObj=new Object[3];
                if (null!=bitmap) {
                    resultObj[0]=bitmap;
                    return resultObj;
                } else {
                    bitmap=ImageCache2.getInstance().getImageManager().getBitmapFromDiskOrNet(url,
                        AppSettings.current().mCacheDir+Constants.ICON_DIR, true);

                    WeiboLog.i(TAG, "profileImageurl:"+url);
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
            if (resultObj==null) {
                WeiboLog.w(TAG, "can't not find the image.");
                return;
            }
            Bitmap bitmap=(Bitmap) resultObj[0];
            mPortrait.setImageBitmap(bitmap);
        }
    }

    //--------------------- 分组操作 ---------------------
    ArrayAdapter<Group> mGroupAdapter;
    ArrayList<Group> mGroupList;
    Handler mGroupHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            //isDeleting=false;
            if (!isResumed()) {
                WeiboLog.w(TAG, "已经结束了Fragment，不需要通知消息");
                return;
            }

            switch (msg.what) {
                case ActionResult.ACTION_SUCESS: {
                    ActionResult actionResult=(ActionResult) msg.obj;
                    ArrayList<Group> groups=(ArrayList<Group>) actionResult.obj;
                    NotifyUtils.showToast("update group successfully!");
                    updateGroup(groups);
                    break;
                }

                case ActionResult.ACTION_FALL:
                    ActionResult actionResult=(ActionResult) msg.obj;
                    NotifyUtils.showToast(actionResult.reslutMsg, Toast.LENGTH_LONG);
                    WeiboLog.d(TAG, "load group failed."+actionResult.reslutMsg);

                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 加载分组
     *
     * @param force 如果为true表示需要从网络加载,如果为false表示从本地文件加载.
     */
    void loadGroup(boolean force) {
        if (force) {
            GroupAction action=new GroupAction(true);
            //isDeleting=true;

            NotifyUtils.showToast("loading!");
            String filepath=getActivity().getFilesDir().getAbsolutePath()+"/"+String.valueOf(currentUserId)+Constants.GROUP_FILE;
            AsyncActionTask task=new AsyncActionTask(getActivity(), action);
            task.execute(filepath, mGroupHandler);
        } else {
            updateGroup(null);
        }
    }

    /**
     * 成功后更新列表。
     *
     * @param data 如果不为空,就直接使用,如果为空就从文件加载
     */
    private void updateGroup(ArrayList<Group> data) {
        ArrayList<Group> groups=null;
        if (null==data) {
            String filepath=getActivity().getFilesDir().getAbsolutePath()+"/"+String.valueOf(currentUserId)+Constants.GROUP_FILE;
            groups=WeiboOperation.readLocalData(filepath);
        } else {
            groups=data;
        }

        WeiboLog.d(TAG, "updateGroup:"+data+" groups:"+groups);
        if (null!=groups&&groups.size()>0) {
            mGroupList.clear();
            mGroupList.addAll(groups);
            if (null==mGroupAdapter) {
                mGroupAdapter=new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_spinner_item, mGroupList);
                mGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }
            mSpinner.setAdapter(mGroupAdapter);
            mGroupAdapter.notifyDataSetChanged();
        }
    }
}
