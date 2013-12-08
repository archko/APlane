package cn.archko.microblog.fragment.abs;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.recycler.RecycleHolder;
import cn.archko.microblog.ui.PrefsActivity;
import com.andrew.apollo.utils.ThemeUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.andrew.apollo.utils.ApolloUtils;
import com.me.microblog.cache.ImageCache2;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午6:44
 * @description:
 */
public abstract class AbsBaseListFragment<T> extends AbsStatusAbstraction<T> implements AbsListView.OnScrollListener{

    public static final String TAG="AbsBaseListFragment";
    protected ContentResolver mResolver;
    protected TimeLineAdapter mAdapter;

    /**
     * 是否自动加载更多内容
     */
    protected boolean autoLoading=true;

    /**
     * ListView中最后一项位置
     */
    protected int lastItem=0;
    protected volatile boolean state=false;   //暂时无用

    /**
     * 更多的FooterView
     */
    protected RelativeLayout mLoadingLayout;
    protected ProgressBar mMoreProgressBar;

    /**
     * 显示更多,如果加载失败,要显示不同的文字.
     */
    protected TextView mMoreTxt;

    /**
     * 底部的View，添加上面的loadingLayout
     */
    protected RelativeLayout footerView;
    protected PullToRefreshListView mPullRefreshListView;
    protected ListView mListView;
    /**
     * 空数据时显示的内容。
     */
    protected TextView mEmptyTxt;

    /**
     * 微博数据，这个比较特殊，因为Fragment不同，需要保存不同的数据，为了节省内存消耗，
     * 可以在onStop中取消内容
     */
    protected ArrayList<T> mDataList;
    protected SStatusData<T> mStatusData;
    protected Handler mHandler=new Handler();
    protected int weibo_count=25;   //一次显示微博数量
    protected int page=1;//当前页,这个值不能随便地变，目前来说不需要用到。
    protected int mScrollState=0;   //滚动的状态,为0时表示当前静止,滚动结束

    /**
     * 列表选中的位置
     */
    //protected int selectedPos=-1;
    /**
     * 是否是要刷新，因为新的ListView调用显示头部，会选中头部。
     */
    protected boolean isRefreshing=false;

    /**
     * 是否正在加载，暂时无用
     */
    protected boolean isLoading=false;
    /**
     * 是否在列表中显示大的位图，只有在下面的显示列表图片时，才有效。
     */
    protected boolean showLargeBitmap=false;
    /**
     * 是否显示列表图片
     */
    protected boolean showBitmap=true;
    /**
     * 是否显示快速滚动块。
     * 在增加上下导航按钮后，快速滚动似乎不是那么必要的
     */
    protected boolean fastScroll=true;
    /**
     * 是否显示上下导航按钮
     */
    protected boolean showNavBtn=true;
    /**
     * 是否显示上下导航按钮
     */
    protected boolean showNavPageBtn=true;

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.v(TAG, "onCreate:");

        footerView=new RelativeLayout(getActivity());
        mResolver=App.getAppContext().getContentResolver();

        /*boolean slb, sb;
        slb="1".equals(mPrefs.getString(PrefsActivity.PREF_RESOLUTION, "0"));
        sb=mPrefs.getBoolean(PrefsActivity.PREF_SHOW_BITMAP, true);
        showLargeBitmap=slb;
        showBitmap=sb;*/

        if (null==mDataList) {
            mDataList=new ArrayList<T>();
            mAdapter=new TimeLineAdapter();
        } else {
            mAdapter=new TimeLineAdapter();
        }

        //mDataList=new ArrayList<T>();
        mStatusData=new SStatusData<T>();
        mStatusData.mStatusData=mDataList;

        this.zoomRunnable=new Runnable() {
            public void run() {
                fadeZoom();
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        WeiboLog.v(TAG, "onPause:"+this);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean slb, sb;

        fastScroll=mPrefs.getBoolean(PrefsActivity.PREF_FAST_SCROLL, true);
        if (null!=mListView) {
            mListView.setFastScrollEnabled(fastScroll);
        }

        slb="1".equals(mPrefs.getString(PrefsActivity.PREF_RESOLUTION, getString(R.string.default_resolution)));
        sb=mPrefs.getBoolean(PrefsActivity.PREF_SHOW_BITMAP, true);

        if (showLargeBitmap!=slb||showBitmap!=sb) {
            notifyChanged();
        }
        showLargeBitmap=slb;
        showBitmap=sb;

        showNavBtn=mPrefs.getBoolean(PrefsActivity.PREF_SHOW_NAV_BTN, true);
        showNavPageBtn=mPrefs.getBoolean(PrefsActivity.PREF_SHOW_NAV_PAGE_BTN, true);

        WeiboLog.d(TAG, "onResume:"+showNavBtn+" showLargeBitmap:"+showLargeBitmap);

        if (null!=zoomLayout) {
            if (showNavBtn) {
                showZoom();
            } else {
                showNavPageBtn=false;
                zoomHandler.removeCallbacks(zoomRunnable);
                zoomLayout.clearAnimation();
                zoomLayout.setVisibility(View.GONE);
            }
        }

        weibo_count=((App) App.getAppContext()).getPageCount();
    }

    protected void notifyChanged() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WeiboLog.v(TAG, "onDestroy:"+this);
        state=false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WeiboLog.v(TAG, "onDestroyView:"+this);
        mLoadingLayout=null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WeiboLog.d(TAG, "onCreateView.");
        View view=_onCreateView(inflater, container, savedInstanceState);
        mRoot=view;
        themeBackground();

        return view;
    }

    /**
     * 必须由子类实现，如果没有实现此方法就没有ListView，所以子类一定要初始化ListView。
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    //abstract public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root=(RelativeLayout) inflater.inflate(R.layout.status_list, null);
        mEmptyTxt=(TextView) root.findViewById(R.id.empty_txt);
        mPullRefreshListView=(PullToRefreshListView) root.findViewById(R.id.statusList);
        mListView=mPullRefreshListView.getRefreshableView();

        // ------------------------------------------------------------------
        /*root.setLayoutParams(new FrameLayout.LayoutParams(
       ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));*/

        up=(ImageView) root.findViewById(R.id.up);
        down=(ImageView) root.findViewById(R.id.down);
        up.setOnClickListener(navClickListener);
        down.setOnClickListener(navClickListener);

        up.setBackgroundColor(Color.TRANSPARENT);
        down.setBackgroundColor(Color.TRANSPARENT);
        zoomLayout=(RelativeLayout) root.findViewById(R.id.zoomLayout);
        zoomAnim=AnimationUtils.loadAnimation(getActivity(), R.anim.zoom);

        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnScrollListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null==mStatusImpl) {
            throw new IllegalArgumentException("StatusImpl should not be null.");
        }
        isLoading=false;
        if (null!=mPullRefreshListView) {
            mPullRefreshListView.onRefreshComplete();
        }
        _onActivityCreated(savedInstanceState);
    }

    /**
     * 这个方法可以由子类覆盖，这样可以有一些变化
     *
     * @param savedInstanceState
     */
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
                    position--;
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
                AbsBaseListFragment.this.itemClick(view);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                WeiboLog.v(TAG, "itemLongClick:"+pos);
                int position=pos;
                if (mListView.getHeaderViewsCount()>0) {
                    position--;
                }
                selectedPos=position;

                if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                    WeiboLog.v(TAG, "footerView.click.");
                    return true;
                }

                if (view!=footerView) {
                    //showButtonBar(view);
                    return AbsBaseListFragment.this.itemLongClick(view);
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

        WeiboLog.v(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        loadData();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
            || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            ImageCache2.getInstance().setPauseDiskCache(true);
            //WeiboLog.v(TAG, "onScrollStateChanged.scroll");
        } else {
            ImageCache2.getInstance().setPauseDiskCache(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(View achor) {
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected boolean itemLongClick(View achor) {
        //showButtonBar(achor);
        /*View view=up;
        if (up.getVisibility()==View.GONE) {    //竖向时，菜单项会因为内容太长而显示部分。
            view=achor;
        }*/
        prepareMenu(up);
        return true;
    }

    //--------------------- datas ---------------------

    /**
     * 下拉刷新数据
     */
    protected void pullToRefreshData() {
        isRefreshing=true;
        //page=1;
        fetchData(-1, -1, true, true);
    }

    /**
     * 上拉刷新
     */
    protected void pullUpRefreshData() {
        if (null!=mDataList&&mDataList.size()>0) {
            fetchMore();
        } else {
            WeiboLog.w(TAG, "no data,pull up failed.now pull to refresh.");
            pullToRefreshData();
        }
    }

    /**
     * 这是一个可刷新的方法,当ActionBar中的按钮按下时,就可以刷新它了.
     */
    public void refresh() {
        if (null!=mListView) {
            mListView.setSelection(1);
        }
        pullToRefreshData();
    }

    /**
     * clear list's datas
     */
    @Override
    public void clear() {
        AKUtils.showToast(R.string.clear_data);
        mDataList.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     * 这是数据的入口,一切从此开始.
     */
    protected void loadData() {
        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                isRefreshing=true;
                fetchData(-1, -1, true, true);
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 获取更多数据，需要子类覆盖，因为有不同的类型。这里不作数据获取操作.
     */
    public void fetchMore() {
        isRefreshing=false;
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        WeiboLog.v(TAG, "showMoreView");
        if (null==mLoadingLayout) {
            WeiboLog.d(TAG, "null==mLoadingLayout.");
            mLoadingLayout=(RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext())
                .inflate(R.layout.ak_more_progressbar, null);
            mMoreProgressBar=(ProgressBar) mLoadingLayout.findViewById(R.id.progress_bar);
            mMoreTxt=(TextView) mLoadingLayout.findViewById(R.id.more_txt);
        }

        mMoreTxt.setText(R.string.more);

        WeiboLog.v(TAG, "mListView.getFooterViewsCount():"+mListView.getFooterViewsCount());
        /*if (mListView.getFooterViewsCount()<1) {
            footerView.removeAllViews();
            RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT
            );
            footerView.addView(mLoadingLayout, layoutParams);
        } else {
            if (footerView.getChildCount()<1) {
                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT
                );
                footerView.addView(mLoadingLayout, layoutParams);
            }else {
        }*/
        footerView.removeAllViews();
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        footerView.addView(mLoadingLayout, layoutParams);

        mMoreProgressBar.setVisibility(View.GONE);
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

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    /**
     * 线程执行前期的操作
     */
    public void basePreOperation() {
        //WeiboLog.v(TAG, "basePreOperation:"+mPullRefreshListView);
        if (null!=mPullRefreshListView) {
            mPullRefreshListView.setRefreshing();
        }

        if (null==mDataList||mDataList.size()<1) {
            mEmptyTxt.setText(R.string.list_pre_empty_txt);
            mEmptyTxt.setVisibility(View.VISIBLE);
        }

        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshStarted();
        }
        isLoading=true;
    }

    /**
     * 线程中的操作。
     *
     * Boolean isRefresh=(Boolean) params[0];是否刷新，因为如果是刷新数据，在获取到新数据后会清除原来的。
     * 如果不是刷新数据，会添加在原来的数据末尾。
     * params[i]接着跟着参数。
     *
     * @param params
     * @return
     */
    public Object[] baseBackgroundOperation(Object... objects) {
        try {
            WeiboLog.d(TAG, "baseBackgroundOperation:"+objects);
            SStatusData<T> sStatusData=(SStatusData<T>) getData(objects);

            saveData(sStatusData);
            return new Object[]{sStatusData, objects};
        } catch (Exception ex) {
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
        isRefreshing=false;
        //page++;
        WeiboLog.d(TAG, "basePostOperation");
        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshFinished();
        }

        isLoading=false;
        if (isResumed()) {
            //setListShown(true);
        } else {
            //setListShownNoAnimation(true);
        }

        if (null==result) {
            WeiboLog.w(TAG, "加载数据异常。");
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            refreshAdapter(false, false);
            return;
        }

        SStatusData<T> sStatusData=(SStatusData<T>) result[0];
        if (null==sStatusData) {
            WeiboLog.w(TAG, "请求数据异常。");
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            String msg=getString(R.string.more_loaded_failed);

            AKUtils.showToast(msg, Toast.LENGTH_LONG);
            refreshAdapter(false, false);
            return;
        }

        if (!TextUtils.isEmpty(sStatusData.errorMsg)) {
            WeiboLog.w(TAG, "请求数据异常。"+sStatusData.errorMsg);
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            String msg=sStatusData.errorMsg;
            AKUtils.showToast(msg, Toast.LENGTH_LONG);
            refreshAdapter(false, false);
            return;
        }

        if (null==sStatusData.mStatusData) {
            WeiboLog.w(TAG, "加载数据为空。");
            if (null!=mMoreTxt&&null!=mMoreProgressBar) {
                mMoreTxt.setText(R.string.more_loaded_failed);
                mMoreProgressBar.setVisibility(View.GONE);
            }
            refreshAdapter(false, false);
            return;
        }

        //only remove footerView when load succefully
        footerView.removeAllViews();

        Boolean isRefresh=(Boolean) ((Object[]) ((Object[]) result)[1])[0];
        //Boolean isHomeStore=(Boolean) ((Object[]) result)[2];

        refreshNewData(sStatusData, isRefresh);

        refreshAdapter(true, isRefresh);
    }

    @Override
    protected Object[] baseQueryBackgroundOperation(Object... params) throws WeiboException {
        return mStatusImpl.queryData(params);
    }

    @Override
    protected SStatusData<T> getData(Object... params) throws WeiboException {
        return mStatusImpl.loadData(params);
    }

    /**
     * 添加这个方法是为了子类可以自动处理新的数据应该怎样加入列表中,暂时只有在主页使用到.
     *
     * @param sStatusData 列表数据
     * @param isRefresh   是否是刷新列表
     */
    public void refreshNewData(SStatusData<T> sStatusData, Boolean isRefresh) {
        ArrayList<T> list=sStatusData.mStatusData;

        if (null!=list) {
            if (isRefresh) {
                //mListView.clearChoices();
                mDataList.clear();
                mDataList.addAll(list);
                //WeiboLog.i(TAG, "notify data changed."+mDataList.size()+" isRefresh:"+isRefresh);
            } else {
                mDataList.addAll(list);
            }
        }
    }

    /**
     * 刷新列表
     *
     * @param load      是否加载成功，
     * @param isRefresh 是否是刷新数据。
     */
    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter.load:"+load+" isRefresh:"+isRefresh);
        if (load) {
            mPullRefreshListView.setLastUpdatedLabel(getString(R.string.pull_to_refresh_label)+DateUtils.longToDateTimeString(System.currentTimeMillis()));
            mAdapter.notifyDataSetChanged();
        }
        mPullRefreshListView.onRefreshComplete();

        if (isRefresh) {
            if (null!=mListView) {
                mListView.setSelection(1);
            }
        }

        if (mDataList.size()>0) {
            if (mEmptyTxt.getVisibility()==View.VISIBLE) {
                mEmptyTxt.setVisibility(View.GONE);
            }
        } else {
            mEmptyTxt.setText(R.string.list_empty_txt);
            mEmptyTxt.setVisibility(View.VISIBLE);
        }
    }

    //--------------------- 增加上下导航按钮 ---------------------
    protected RelativeLayout zoomLayout;
    protected Handler zoomHandler=new Handler();
    protected Runnable zoomRunnable;
    protected Animation zoomAnim;

    protected ImageView up, down;
    protected View.OnClickListener navClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            navClick(view);
        }
    };

    /**
     * 导航的两个按钮事件，不能在这里实现是因为这个抽象类没有Adapter.
     *
     * @param view
     */
    //protected abstract void navClick(View view);
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void navClick(View view) {
        if (view.getId()==R.id.up) {
            showZoom();
            if (showNavPageBtn&&ApolloUtils.hasJellyBean()) {
                mListView.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, null);
            } else {
                mListView.setSelection(1);
            }
        } else if (view.getId()==R.id.down) {
            showZoom();
            if (showNavPageBtn&&ApolloUtils.hasJellyBean()) {
                mListView.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, null);
            } else {
                mListView.setSelection(mAdapter.getCount()-1);
            }
        }
    }

    public void showZoom() {
        zoomLayout.clearAnimation();
        zoomLayout.setVisibility(View.VISIBLE);
        zoomHandler.removeCallbacks(zoomRunnable);
        zoomHandler.postDelayed(zoomRunnable, 3000);
    }

    private void fadeZoom() {
        zoomAnim.setStartOffset(0);
        zoomAnim.setFillAfter(true);
        zoomLayout.startAnimation(zoomAnim);
    }

    /**
     * 这个方法由Adapter中取出，子类如果是列表，需要覆盖此方法
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public abstract View getView(int position, View convertView, ViewGroup parent);

    //--------------------- adapter ---------------------
    public class TimeLineAdapter extends BaseAdapter {

        public TimeLineAdapter() {
            WeiboLog.v(TAG, "TimeLineAdapter:");
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return AbsBaseListFragment.this.getView(position, convertView, parent);
        }
    }
}
