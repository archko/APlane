package cn.archko.microblog.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.Unread;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-11-17
 */
public class TestFragment extends AbstractBaseFragment {

    public static final String TAG="TestFragment";

    private ImageView profileImg;
    private TextView name;
    private Button editBtn;
    private TextView address, login_name;
    private TextView follwwerCount, statusCount, friendCount, topicCount;
    String profileImageUrl;
    private SharedPreferences mPreferences;
    boolean isLoading=false;
    View.OnClickListener clickListener=new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int id=view.getId();
            switch (id) {
                /*case R.id.editBtn:
                    if (!isLoading) {
                        FetchStatusesTask task=new FetchStatusesTask();
                        task.execute(new Object[]{});
                    }
                    break;*/
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.d(TAG, "onCreate:"+this);
        mPreferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    ArrayList<Status> getStatuses(Long sinceId, Long maxId, int c, int p) throws WeiboException {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        WeiboLog.d(TAG, "onCreateView:"+this);

        View view=inflater.inflate(R.layout.self_profile, container, false);
        profileImg=(ImageView) view.findViewById(R.id.iv_portrait);
        name=(TextView) view.findViewById(R.id.tv_name);
        editBtn=(Button) view.findViewById(R.id.editBtn);
        address=(TextView) view.findViewById(R.id.address);
        login_name=(TextView) view.findViewById(R.id.login_name);
        follwwerCount=(TextView) view.findViewById(R.id.followerCount);
        statusCount=(TextView) view.findViewById(R.id.statusCount);
        friendCount=(TextView) view.findViewById(R.id.friendsCount);
        topicCount=(TextView) view.findViewById(R.id.topicCount);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        editBtn.setOnClickListener(clickListener);

        String loginname=mPreferences.getString(Constants.PREF_USERNAME_KEY, "");
        login_name.setText(loginname);

        String sn=mPreferences.getString(Constants.PREF_SCREENNAME_KEY, "");
        String location=mPreferences.getString(Constants.PREF_LOCATION, "");
        int fc=mPreferences.getInt(Constants.PREF_FOLLWWERCOUNT_KEY, 0);
        int frc=mPreferences.getInt(Constants.PREF_FRIENDCOUNT_KEY, 0);
        int sc=mPreferences.getInt(Constants.PREF_STATUSCOUNT_KEY, 0);

        name.setText(sn);
        address.setText(location);
        follwwerCount.setText(String.valueOf(fc));
        friendCount.setText(String.valueOf(frc));
        statusCount.setText(String.valueOf(sc));
        topicCount.setText(String.valueOf(0));

        profileImageUrl=mPreferences.getString(Constants.PREF_PORTRAIT_URL, null);
    }

    /**
     * 只是获取用户信息。需要传入三个参数
     * Integer cur = (Integer) params[0];页索引
     * Integer count = (Integer) params[1];一页数量
     * Boolean isRefresh=(Boolean) params[2];是否刷新
     */
    class FetchStatusesTask extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WeiboLog.d(TAG, "onPreExecute.userId:");
            isLoading=true;
        }

        @Override
        protected Object doInBackground(Object... params) {
            return pre(params);
        }

        @Override
        protected void onPostExecute(Object result) {
            WeiboLog.d(TAG, "onPostExecute");
            isLoading=false;

            post(result);
        }
    }

    Unread getUnread() throws WeiboException {
        //return ((WeiboApi)App.getMicroBlog(getActivity())).getUnread(1,3445174628207537L);//3445181561309446l
        return null;
    }

    Object pre(Object... params) {
        WeiboLog.d("pre doinbackground.");
        try {
            /*Integer cur=(Integer) params[0];
            Integer count=(Integer) params[1];
            Boolean isRefresh=(Boolean) params[2];*/
            Unread list=getUnread();
            WeiboLog.d(list.toString());

            return new Object[]{list};
        } catch (WeiboException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    void post(Object result) {
        WeiboLog.d("post.");
        if (null==result) {
            WeiboLog.d(TAG, "加载数据异常。");

            return;
        }
    }

    @Override
    public void postOauth(Object[] params) {
        AKUtils.showToast("认证成功，请稍候。", Toast.LENGTH_SHORT);
        //do task
    }
}
