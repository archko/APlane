package cn.archko.microblog.fragment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaHotPostStatusImpl;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 热门转发微博，
 * @author: archko 12-9-12
 */
@Deprecated
public class HotRepostFragment extends StatusListFragment {

    public static final String TAG="HotRepostFragment";

    @Override
    public void initApi() {
        mStatusImpl=new SinaHotPostStatusImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            AKUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

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

        ThreadBeanItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, false, showLargeBitmap, showBitmap);
        } else {
            itemView=(ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, false, showLargeBitmap, showBitmap);

        return itemView;
    }

    //--------------------- 数据加载 ---------------------
    public SStatusData<Status> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, " HotRepostFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        SStatusData<Status> sStatusData=null;
        /*SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            ArrayList<Status> statuses=sWeiboApi2.getHotRepost(c, "repost_daily");
            sStatusData.mStatusData=statuses;
        }*/

        return sStatusData;
    }

    @Override
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
        /*if (isHomeStore) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。TODO
            //count++;
        } else {*/
        //page=1;
        int status=mPrefs.getInt(Constants.PREF_SERVICE_AT, 0);
        WeiboLog.d(TAG, "新提及我的微博数:"+status);
        if (status>0) {
            if (status>((App) App.getAppContext()).getPageCount()) {
                status=((App) App.getAppContext()).getPageCount();
            }

            count=status;
        }
        //}

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    //--------------------- 微博操作 ---------------------
}
