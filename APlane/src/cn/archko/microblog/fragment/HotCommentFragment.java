package cn.archko.microblog.fragment;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 热门评论微博
 * @author: archko 12-9-12
 */
@Deprecated
public class HotCommentFragment extends RecyclerViewFragment {

    public static final String TAG = "HotCommentFragment";

    //--------------------- 数据加载 ---------------------
    public SStatusData<Status> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, " HotCommentFragment.getStatuses." + sinceId + " maxId:" + maxId + " count:" + c + " page:" + p);
        SStatusData<Status> sStatusData = null;
        /*SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            ArrayList<Status> statuses=sWeiboApi2.getHotComment(c, p, "comments_daily");
            sStatusData=new SStatusData<Status>();
            sStatusData.mStatusData=statuses;
        }*/

        return sStatusData;
    }

    protected void showMoreView() {
        //热门评论没有几页的，只有一页。
    }

    @Override
    public void initApi() {

    }

    //--------------------- 微博操作 ---------------------
}
