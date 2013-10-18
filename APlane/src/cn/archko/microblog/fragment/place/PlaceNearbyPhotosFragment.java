package cn.archko.microblog.fragment.place;

import android.os.Bundle;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

/**
 * @version 1.00.00
 * @description: 获取附近照片列表, 返回的数据带用户和距离信息.
 * @author: archko 12-09-16
 */
public class PlaceNearbyPhotosFragment extends PlaceStatusListFragment {

    public static final String TAG="PlaceNearbyPhotosFragment";

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.d(TAG, "onCreate:");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*@Override
    public SStatusData<Status> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, "getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        SStatusData<Status> sStatusData=null;
        SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            sStatusData=sWeiboApi2.getNearbyPhotos(latitude, longitude, range, c, p);
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
        }
        if (count>50){
            count=50;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore, "nearby_photos"}, null);
        }
    }
    //--------------------- 微博操作 ---------------------
}
