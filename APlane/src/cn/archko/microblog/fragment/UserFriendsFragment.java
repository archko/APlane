package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.User;
import com.me.microblog.core.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.utils.AKUtils;

/**
 * @version 1.00.00  用户的关注列表
 * @description:
 * @author: archko 11-11-17
 */
@Deprecated
public class UserFriendsFragment extends UserListFragment {

    public static final String TAG="UserFriendsFragment";
    int nextCursor=-1;//下一页索引，第一页为-1，不是0
    long mUserId=-1l; //查看用户的id
    long currentUserId=-1l;  //当前登录用户的id
    boolean following=false;    //是否正在处理关系,默认只一个一个处理,不并行处理.

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.d(TAG, "onCreate:");

        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        mUserId=aUserId;
        this.currentUserId=aUserId;
    }

    //--------------------- 数据加载 ---------------------
    @Override
    protected void loadData() {
        Intent intent=getActivity().getIntent();
        long userId=intent.getLongExtra("user_id", -1);
        //userScreenName=intent.getStringExtra("screen_name");

        if (userId==mUserId) {
            WeiboLog.i(TAG, "相同的用户不加载");
            if (mDataList!=null&&mDataList.size()>0) {
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        if (userId==-1) {
            WeiboLog.e(TAG, "用户的id错误，无法获取数据,现在获取登录用户信息。");
            //showToast("用户的id错误，无法获取数据。", Toast.LENGTH_SHORT);
            //return;
            userId=currentUserId;
        }

        if(userId==100){
            AKUtils.showToast(R.string.user_id_error);
            WeiboLog.w(TAG, "人员的id没有，有可能是只传来昵称！");
            return;
        }

        //mDataList.clear();
        mUserId=userId;
        if (!isLoading) {
            fetchData(-1, -1, true, false);
        }
    }

    /**
     * 获取数据，覆盖此方法，因为用户的获取与其它不一样。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error);
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count=weibo_count;
        if (!isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。
            count++;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    ///////////////////-----------------

    /**
     * 覆盖baseBackgroundOperation就可以了，不需要在这里覆盖它。
     *
     * @param sinceId 起始id
     * @param maxId   最大id
     * @param c       数量
     * @param p       页面索引
     * @return
     * @throws WeiboException
     */
    /*@Override
    public SStatusData<User> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, " UserFriendsFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);

        try {
            SinaUserApi weiboApi2=new SinaUserApi();
            weiboApi2.updateToken();
            SStatusData<User> sStatusData=weiboApi2.getFriends(mUserId, nextCursor++, weibo_count, 1);
            int nCur=sStatusData.next_cursor;
            WeiboLog.i(TAG, "cur:"+nCur+" nextCur:"+nextCursor);
            nextCursor=nCur;
            return sStatusData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }*/

    //TODO需要处理token过期的状况。

    /**
     * 处理关系，关注或取消关注，不作粉丝移除处理。
     */
    protected void followUser() {
        /*if (App.OAUTH_MODE==Constants.SOAUTH_TYPE_CLIENT) {
            mCommonTask=new CommonTask();
            mCommonTask.execute(params);
        } else {
            if (System.currentTimeMillis()>=App.oauth2_timestampe&&App.oauth2_timestampe!=0) {
                WeiboLog.d(TAG, "web认证，token过期了.");
                //oauth2(params);
                mOauth2Handler.oauth2(params);
            } else {
                WeiboLog.d(TAG, "web认证，但token有效。");
                mCommonTask=new CommonTask();
                mCommonTask.execute(params);
            }
        }*/

        if (following) {
            WeiboLog.d("正在处理关系.");
            return;
        }
        FollwingTask follwingTask=new FollwingTask();
        follwingTask.execute(0);
    }

    class FollwingTask extends AsyncTask<Integer, Void, User> {

        int followingType=1;//0表示未关注,1表示已关注

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            following=true;
            User user=mDataList.get(selectedPos);
            followingType=user.following?1:0;
        }

        @Override
        protected User doInBackground(Integer... params) {
            try {
                User now=null;

                //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
                SinaUserApi weiboApi2=new SinaUserApi();
                weiboApi2.updateToken();
                if (null==weiboApi2) {
                    AKUtils.showToast(R.string.err_api_error);
                    return now;
                }

                User user=mDataList.get(selectedPos);
                if (followingType==0) {
                    now=weiboApi2.createFriendships(user.id);
                } else if (followingType==1) {
                    now=weiboApi2.deleteFriendships(user.id);
                }

                return now;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User resultObj) {
            following=false;
            if (resultObj==null) {
                AKUtils.showToast("处理关系失败", Toast.LENGTH_LONG);
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            if (followingType==0) {
                AKUtils.showToast("follow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            } else if (followingType==1) {
                AKUtils.showToast("unfollow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            }
        }

    }
}
