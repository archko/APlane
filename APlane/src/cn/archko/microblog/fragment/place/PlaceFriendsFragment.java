package cn.archko.microblog.fragment.place;

import android.os.Bundle;
import android.view.View;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.bean.Status;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00  获取当前登录用户与其好友的位置动态
 * @description:
 * @author: archko 12-09-4
 */
public class PlaceFriendsFragment extends PlaceStatusListFragment {

    public static final String TAG="PlaceFriendsFragment";

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.d(TAG, "onCreate:");
    }

    ///////////////////-----------------
    /*public SStatusData<Status> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, " PlaceFriendsFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        SStatusData<Status> sStatusData=null;
        SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            sStatusData=sWeiboApi2.getPlaceFriendTimeline(sinceId, maxId, c, p, 0);
        }

        return sStatusData;
    }*/

    /**
     * 获取数据，如果要改变传入的参数，需要覆盖此方法，以便修改。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i(TAG, "sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
        if (!App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);
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
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore, "friends"}, null);
        }
    }
    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论，但这里是一个位置微博。可能会有些不一样.
     */
    protected void itemClick(View achor) {
        NotifyUtils.showToast("not implemented!");
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);

            //WeiboOperation.toViewOriginalStatus(getActivity(), status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
