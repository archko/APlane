package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Trend;
import com.me.microblog.bean.Trends;
import com.me.microblog.core.SinaTrendApi;
import com.me.microblog.util.WeiboLog;

import java.util.Arrays;
import java.util.List;

/**
 * @version 1.00.00
 * @description: 显示话题Fragment
 * @author: archko 12-9-6
 */
@Deprecated
public class TrendsFragment extends AbsBaseListFragment<Trend> {

    public static final String TAG="TrendsFragment";
    String[] types={"hourly", "daily", "weekly"};
    String[] from={"name", "query"};

    int[] to={android.R.id.text1, android.R.id.text2};
    int nextCursor=-1;//下一页索引，第一页为-1，不是0
    long userId;//要查询的关注列表的用户id
    protected PullToRefreshListView mPullRefreshListView;
    protected ListView mListView;
    String mType=types[1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeiboLog.d(TAG, "onCreate:"+this);
    }

    @Override
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root=(RelativeLayout) inflater.inflate(R.layout.friend_list, null);
        mEmptyTxt=(TextView) root.findViewById(R.id.empty_txt);
        mPullRefreshListView=(PullToRefreshListView) root.findViewById(R.id.statusList);
        mListView=mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        return root;
    }

    /**
     * 查看用户发布的微博信息。
     */
    void viewUserStatuses() {
        if (selectedPos>=mAdapter.getCount()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }
    }

    /**
     * 获取数据
     *
     * @param next_cursor
     * @param isRefresh
     */
    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        if (!App.hasInternetConnection(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        if (!isLoading) {
            newTask(new Object[]{mType}, null);
        }
    }

    //////////////////----------------

    /**
     * 线程中的操作。
     * 只是获取微博信息。需要传入五个参数
     *
     * @param params
     * @return
     */
    @Override
    public Object[] baseBackgroundOperation(Object... objects) {
        try {
            WeiboLog.d(TAG, "baseBackgroundOperation:"+objects);
            Object[] params=objects;
            SinaTrendApi sinaTrendApi=new SinaTrendApi();
            sinaTrendApi.updateToken();
            Trends tmp=sinaTrendApi.getTrends((String) params[0]);

            return new Object[]{false, tmp, false};
        } catch (WeiboException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    public void basePostOperation(Object[] result) {
        WeiboLog.d(TAG, "basePostOperation");
        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshFinished();
        }

        isLoading=false;

        if (null==result) {
            WeiboLog.d(TAG, "加载数据异常。");
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            refreshAdapter(false, false);
            return;
        }

        Trends sStatusData=(Trends) result[1];
        if (null==sStatusData||null==sStatusData.trends) {
            WeiboLog.d(TAG, "加载数据为空。");
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            refreshAdapter(false, false);
            return;
        }

        //only remove footerView when load succefully
        footerView.removeAllViews();

        List<Trend> list=addValue(sStatusData);

        mListView.clearChoices();
        mDataList.clear();
        mDataList.addAll(list);
        WeiboLog.i(TAG, "notify data changed."+mDataList.size()+" isRefresh:");

        refreshAdapter(true, false);
    }

    private List<Trend> addValue(Trends trends) {
        WeiboLog.d("addValue:"+trends);
        List<Trend> trendList=Arrays.asList(trends.trends);
        return trendList;
    }

    /**
     * 覆盖baseBackgroundOperation就可以了，不需要在这里覆盖它。
     *
     * @return
     * @throws WeiboException
     */
    //@Override
    public SStatusData<Trend> getStatuses(Long sinceId, Long maxId, int c, int p) {
        WeiboLog.d(TAG, " TrendsFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        //Trends tmp=((SWeiboApi2) App.getMicroBlog(getActivity())).getTrends((String) params[0]);
        return null;
    }

    /**
     * 这个方法由Adapter中取出，子类如果是列表，需要覆盖此方法
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null) {
            convertView=((LayoutInflater) getActivity().getSystemService("layout_inflater")).
                inflate(android.R.layout.simple_list_item_2, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder=new ViewHolder();
            holder.text1=(TextView) convertView.findViewById(android.R.id.text1);
            holder.text2=(TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView and the ImageView.
            holder=(ViewHolder) convertView.getTag();
        }

        Trend trend=mDataList.get(position);
        holder.text1.setText(trend.name);
        holder.text2.setText(trend.query);

        return convertView;
    }

    static class ViewHolder {

        TextView text1, text2;
    }
}
