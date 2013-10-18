package cn.archko.microblog.fragment;

import android.content.Intent;
import android.text.TextUtils;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.SkinFragmentActivity;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.SinaUnreadApi;
import com.me.microblog.core.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 获取用户粉丝列表及每个粉丝的最新一条微博，返回结果按粉丝的关注时间倒序排列，最新关注的粉丝排在最前面。每次返回20个,
 * 通过cursor参数来取得多于20的粉丝。注意目前接口最多只返回5000个粉丝。
 * 需要添加移除粉丝功能
 * @author: archko 12-9-1
 */
@Deprecated
public class UserFollowersFragment extends UserFriendsFragment {

    public static final String TAG="UserFollowersFragment";

    //--------------------- 数据加载 ---------------------
    @Override
    protected void loadData() {
        Intent intent=getActivity().getIntent();
        long userId=intent.getLongExtra("user_id", -1);

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

        if (userId==100) {
            AKUtils.showToast(R.string.user_id_error);
            WeiboLog.w(TAG, "人员的id没有，有可能是只传来昵称！");
            return;
        }

        mDataList.clear();
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
            //count++;
        } else {
            //page=1;
            int status=mPrefs.getInt(Constants.PREF_SERVICE_AT, 0);
            WeiboLog.d(TAG, "新提及我的微博数:"+status);
            if (status>0) {
                if (status>=weibo_count) {
                    count=weibo_count;
                } else {
                    count=status;
                }
            }
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, false}, null);
        }
    }

    @Override
    public void refreshNewData(SStatusData<User> sStatusData, Boolean isRefresh) {
        ArrayList<User> list=sStatusData.mStatusData;
        if (mDataList.size()>0) {
            try {
                User first=list.get(0);
                User last=mDataList.get(mDataList.size()-1);

                if (first.id==last.id) {
                    list.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isRefresh) {
            int len=list.size();
            AKUtils.showToast("为您更新了"+len+"条最新信息！");
            if (list.size()<weibo_count) {
                mDataList.addAll(0, list);
            } else {
                mDataList.clear();
                mDataList.addAll(list);
            }
            WeiboLog.i(TAG, "notify data changed."+mDataList.size()+" isRefresh:"+isRefresh);
        } else {
            mDataList.addAll(list);
        }
    }

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
    public SStatusData<User> getStatuses(Long sinceId, Long maxId, int c, int p) throws WeiboException {
        WeiboLog.d(TAG, " UserFollowersFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);

        SStatusData<User> sStatusData=null;

        try {
            //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
            SinaUserApi weiboApi2=new SinaUserApi();
            weiboApi2.updateToken();
            if (null!=weiboApi2) {
                sStatusData=weiboApi2.getMyFollowers(mUserId, nextCursor++, weibo_count);
            }
            int nCur=sStatusData.next_cursor;
            WeiboLog.i(TAG, "cur:"+nCur+" nextCur:"+nextCursor);
            nextCursor=nCur;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sStatusData;
    }*/

    /**
     * 获取粉丝列表，这里无用，作为上面的参考
     *
     * @param cur   页索引
     * @param count 一页数量
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    @Deprecated
    SStatusData<User> getUsers(int cur, int count) throws WeiboException {
        WeiboLog.d(TAG, TAG+" getStatuses.");
        SStatusData<User> objects=null;

        //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
        SinaUserApi weiboApi2=new SinaUserApi();
        weiboApi2.updateToken();
        if (null!=weiboApi2) {
            objects=weiboApi2.getMyFollowers(mUserId, cur, count);
            WeiboLog.d(TAG, "objs:"+objects);
        }

        return objects;
    }

    //--------------------- 操作 ---------------------

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        if (isRefresh&&load) {
            clearHomeNotify();
        }
    }

    //--------------------- 清除未读消息操作 ---------------------

    /**
     * 清除主页的消息计数通知
     */
    private void clearHomeNotify() {
        int newFollower=mPrefs.getInt(Constants.PREF_SERVICE_FOLLOWER, 0);
        mPrefs.edit().remove(Constants.PREF_SERVICE_FOLLOWER).commit();
        try {
            SkinFragmentActivity parent=(SkinFragmentActivity) getActivity();
            parent.refreshSidebar();

            newOperationTask(new Object[]{Constants.REMIND_FOLLOWER}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程中的操作。
     *
     * @param params
     * @return
     */
    protected Object[] baseBackgroundOperation2(Object... params) {
        try {
            String type=(String) params[0];
            SStatusData sStatusData=new SStatusData();
            SinaUnreadApi sinaUnreadApi=new SinaUnreadApi();
            sinaUnreadApi.updateToken();
            String rs=sinaUnreadApi.setUnread(type);
            sStatusData.errorMsg=rs;
            return new Object[]{sStatusData};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void basePostOperation2(Object[] resultObj) {
        try {
            SStatusData sStatusData=(SStatusData) resultObj[0];
            WeiboLog.i(TAG, TAG+sStatusData);
            if (null==sStatusData) {
                return;
            }

            if (sStatusData.errorCode>0&&!TextUtils.isEmpty(sStatusData.errorMsg)) {
                AKUtils.showToast(sStatusData.errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
