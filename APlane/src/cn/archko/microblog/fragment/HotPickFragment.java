package cn.archko.microblog.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.R;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

/**
 * @version 1.00.00
 * @description: 微博精选，类别有很多，可以用list导航来处理。 api过期
 * @author: archko 12-9-12
 */
@Deprecated
public class HotPickFragment extends StatusListFragment {

    public static final String TAG="HotPickFragment";
    /**
     * 是否是新的分类，默认是true,表示当前要获取该分类数据，否则不重新获取。
     */
    boolean isNewCategory=true;
    String category="1";

    //--------------------- 数据加载 ---------------------
    @Override
    protected void loadData() {
        try {
            Intent intent=getActivity().getIntent();
            String c=intent.getStringExtra("category");
            WeiboLog.i("现在的分类是："+c+" 原来的分类："+category);
            if (!TextUtils.isEmpty(c)) {
                if (!category.equals(c)) {
                    isNewCategory=true;
                }
                category=c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mDataList!=null&&mDataList.size()>0) {
            //setListShown(true);
            mAdapter.notifyDataSetChanged();
            if (isNewCategory) {
                fetchData(-1, -1, true, false);
            }
        } else {
            if (!isLoading) {
                fetchData(-1, -1, true, false);
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
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

    /**
     * 获取数据 TODO，需要改进的地方，如果要改变加载数据的行为，要覆盖的类太多了。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
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
        if (!isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。
            //count++;
        } else {
            page=1;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    public SStatusData<Status> getStatuses(Long sinceId, Long maxId, int c, int p)
        throws WeiboException {
        WeiboLog.d(TAG, " HotPickFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p+" category:"+category);
        isNewCategory=false;
        SStatusData<Status> sStatusData=null;
        /*SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            sStatusData=sWeiboApi2.getHotStatus(Integer.valueOf(category), 0, c, p);
            page++;
        }*/

        return sStatusData;
    }

    @Override
    public void initApi() {

    }
}
