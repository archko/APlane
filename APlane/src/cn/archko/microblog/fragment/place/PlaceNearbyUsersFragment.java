package cn.archko.microblog.fragment.place;

import android.view.View;

/**
 * @version 1.00.00
 * @description: 获取附近发位置微博的人, 返回的数据带微博内容, 但不带距离信息,
 * 但是带最后的时间信息
 * @author: archko 12-09-4
 */
public class PlaceNearbyUsersFragment extends PlaceUserListFragment {

    public static final String TAG = "PlaceNearbyUsersFragment";
    //--------------------- 数据加载 ---------------------

    /*public SStatusData<User> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, "getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        SStatusData<User> sStatusData=null;
        SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<User>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            sStatusData=sWeiboApi2.getPlacesNearbyUsers(latitude, longitude, range, c, p);

            WeiboLog.d(TAG, " user:"+sStatusData.mStatusData.get(0).toString());
        }

        return sStatusData;
    }*/

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论，但这里是一个位置微博。可能会有些不一样.
     */
    @Override
    protected void itemClick(View achor) {
        super.itemClick(achor);
    }
}
