package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaUserStatusImpl;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.factory.SinaApiFactory;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

/**
 * @version 1.00.00
 * @description: 显示用户的微博Fragment，根据传入的用户id
 * @author: archko 12-5-7
 */
public class UserTimelineFragment extends StatusListFragment {

    public static final String TAG="UserTimelineFragment";
    long userId=-1;
    String userScreenName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeiboLog.v(TAG, "onCreate:"+this);
        /*Intent intent=getActivity().getIntent();
        userId=intent.getLongExtra("user_id", -1);
        userScreenName=intent.getStringExtra("screen_name");
        if (userId==-1) {
            WeiboLog.d(TAG, "用户的id错误，无法查看其微博信息。");
            showToast("用户的id错误，无法查看其微博信息。", Toast.LENGTH_SHORT);

            return;
        }*/
        //mStatusImpl=new SinaUserStatusImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaUserStatusImpl();

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

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.v(TAG, "onActivityCreated");

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        //setEmptyText("No phone numbers");

        // We have a menu item to show in action bar.
        //setHasOptionsMenu(true);

        //mDataList=(ArrayList<Status>) getActivity().getLastNonConfigurationInstance();

        //mListView.setFocusable(true);

        //mListView.setOnScrollListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                int position=pos;
                if (mListView.getHeaderViewsCount()>0) {
                    position-=mListView.getHeaderViewsCount();
                }
                if (position==-1) {
                    WeiboLog.v("选中的是头部，不可点击");
                    return;
                }

                selectedPos=position;
                WeiboLog.v(TAG, "itemClick:"+pos+" selectedPos:"+selectedPos);

                if (view==footerView) {   //if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                    mMoreProgressBar.setVisibility(View.VISIBLE);
                    fetchMore();
                    return;
                }
                UserTimelineFragment.this.itemClick(view);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                WeiboLog.v(TAG, "itemLongClick:"+pos);
                int position=pos;
                if (mListView.getHeaderViewsCount()>0) {
                    position-=mListView.getHeaderViewsCount();
                }
                selectedPos=position;

                if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                    WeiboLog.v(TAG, "footerView.click.");
                    return true;
                }

                if (view!=footerView) {
                    //showButtonBar(view);
                    return UserTimelineFragment.this.itemLongClick(view);
                }
                return true;
            }
        });
        /*mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                pullToRefreshData();
            }
        });*/
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                pullToRefreshData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                pullUpRefreshData();
            }
        });

        // Add an end-of-list listener
        mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                //showMoreView();
            }
        });
        //mPullRefreshListView.setOnScrollListener(this);
        mListView.addFooterView(footerView);
        //mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (mAdapter==null) {
            mAdapter=new TimeLineAdapter();
        }
        mListView.setAdapter(mAdapter);

        //loadData();
        if (!hasAttach) {   //不在onAttach中处理,因为refresh可能先调用,以保证数据初始化.
            hasAttach=true;
            int type=getActivity().getIntent().getIntExtra("type", UserFragmentActivity.TYPE_USER_INFO);
            if (type==UserFragmentActivity.TYPE_USER_TIMELINE) {
                refresh();
            }
        }
    }

    @Override
    public void refresh() {
        WeiboLog.v(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        loadData();
    }

    @Override
    protected void loadData() {
        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (hasAttach) {
                Intent intent=getActivity().getIntent();
                userId=intent.getLongExtra("user_id", -1);
                userScreenName=intent.getStringExtra("screen_name");
                if (userId==-1) {
                    WeiboLog.d(TAG, "用户的id错误，无法查看其微博信息。");
                    AKUtils.showToast("用户的id错误，无法查看其微博信息。");

                    return;
                }

                if (!isLoading) {
                    fetchData(-1, -1, true, false);
                } else {
                    mEmptyTxt.setText(R.string.list_pre_empty_txt);
                    mEmptyTxt.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        int count=mAdapter.getCount();
        if (count<1) {
            WeiboLog.w(TAG, "no other data");
            return;
        }

        boolean isRefresh=false;
        if (count>=weibo_count*3) {   //refresh list
            isRefresh=true;
        }
        Status st;
        st=(Status) mAdapter.getItem(mAdapter.getCount()-1);
        fetchData(-1, st.id, isRefresh, false);
    }

    /**
     * 获取数据，如果要改变传入的参数，需要覆盖此方法，以便修改。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i(TAG, "sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore+" userId:"+userId);
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

        if (!isLoading) {   //这里多了一个当前查询的用户id
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, userId, isHomeStore}, null);
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
}
