package cn.archko.microblog.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * @version 1.00.00
 * @description: 关于页面。用于显示一些信息。
 * @author: archko 12-12-9
 */
public class AboutAppFragment extends AbstractBaseFragment {

    public static final String TAG = "AboutAppFragment";
    public static final long AKWBO_USER_ID = 2532909203l;

    private Button mFollow, mSugguest, mChkUpdate, mFeedback;
    TextView mVersion;
    boolean isFollowing = false;
    String mCurrVersionName = "1.3.0";
    LinearLayout authorLayout;
    View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            clickMethod(view);
        }

        private void clickMethod(View view) {
            int id = view.getId();
            if (id == R.id.follow_btn) {
                if (! isFollowing) {
                    FollwingTask follwingTask = new FollwingTask();
                    follwingTask.execute(new Integer[]{0});
                }
            } else if (id == R.id.sugguest_btn) {
                String atString = getString(R.string.about_app_suggesst, mCurrVersionName);
                Intent intent = new Intent(getActivity(), NewStatusActivity.class);
                intent.putExtra("at_some", atString);
                intent.setAction(Constants.INTENT_NEW_BLOG);
                startActivity(intent);
            } else if (id == R.id.chk_udpate_btn) {
                checkUpdate();
            } else if (id == R.id.feedback_btn) {
                NotifyUtils.showToast("not implemented!");
                //WeiboUtil.openUrlByDefaultBrowser(getActivity(), getString(R.string.about_app_feedback_url));
            } else if (id == R.id.author_layout) {
                WeiboOperation.toViewStatusUser(getActivity(), "", AKWBO_USER_ID, UserFragmentActivity.TYPE_USER_INFO);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ak_about_app, container, false);
        mFollow = (Button) root.findViewById(R.id.follow_btn);
        mSugguest = (Button) root.findViewById(R.id.sugguest_btn);
        mChkUpdate = (Button) root.findViewById(R.id.chk_udpate_btn);
        mFeedback = (Button) root.findViewById(R.id.feedback_btn);
        mVersion = (TextView) root.findViewById(R.id.version);

        authorLayout = (LinearLayout) root.findViewById(R.id.author_layout);

        mRoot = root;
        themeBackground();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFollow.setOnClickListener(clickListener);
        mSugguest.setOnClickListener(clickListener);
        mChkUpdate.setOnClickListener(clickListener);
        mFeedback.setOnClickListener(clickListener);
        authorLayout.setOnClickListener(clickListener);

        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            mCurrVersionName = info.versionName;
            mVersion.setText(mCurrVersionName + "-" + info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    class FollwingTask extends AsyncTask<Integer, Void, User> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isFollowing = true;
            NotifyUtils.showToast("开始关注AKWBO，请稍候！");
        }

        @Override
        protected User doInBackground(Integer... params) {
            try {
                User now = null;
                SinaUserApi weiboApi2 = new SinaUserApi();
                weiboApi2.updateToken();
                //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
                /*if (null==weiboApi2) {
                    showToast(R.string.err_api_error);
                    return now;
                }*/
                now = weiboApi2.createFriendships(AKWBO_USER_ID);

                return now;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User resultObj) {
            /*if (!isResumed()) {
                return;
            }*/

            isFollowing = false;
            if (resultObj == null) {
                NotifyUtils.showToast("处理失败");
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            NotifyUtils.showToast("follow AKWBO successfully!");
        }
    }

    //--------------------- 自动更新操作 ---------------------
    private void checkUpdate() {
        UmengUpdateAgent.setUpdateOnlyWifi(false); // 目前我们默认在Wi-Fi接入情况下才进行自动提醒。如需要在其他网络环境下进行更新自动提醒，则请添加该行代码
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(updateListener);

        /*UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

            @Override
            public void OnDownloadEnd(int result) {
                WeiboLog.i(TAG, "download result : "+result);
                showToast("download result : "+result);
            }
        });*/

        UmengUpdateAgent.update(getActivity());
    }

    UmengUpdateListener updateListener = new UmengUpdateListener() {
        @Override
        public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
            switch (updateStatus) {
                case 0: // has update
                    WeiboLog.i("callback result");
                    if (isResumed()) {
                        UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                    }
                    break;
                case 1: // has no update
                    NotifyUtils.showToast("没有更新");
                    break;
                case 2: // none wifi
                    NotifyUtils.showToast("没有wifi连接， 只在wifi下更新");
                    break;
                case 3: // time out
                    NotifyUtils.showToast("超时");
                    break;
            }

        }
    };
}
