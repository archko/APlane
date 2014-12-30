package cn.archko.microblog.fragment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.view.ThreadBeanItemView;
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
public class HotCommentFragment extends StatusListFragment {

    public static final String TAG = "HotCommentFragment";

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为false,其它的不缓存,比如随便看看.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //WeiboLog.d(TAG, "getView.pos:"+position+" getCount():"+getCount()+" lastItem:");

        ThreadBeanItemView itemView = null;
        Status status = mDataList.get(position);

        boolean updateFlag = true;
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag = false;
        }

        if (convertView == null) {
            itemView = new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, false, showLargeBitmap, showBitmap);
        } else {
            itemView = (ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, false, showLargeBitmap, showBitmap);

        return itemView;
    }

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
