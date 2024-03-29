package cn.archko.microblog.fragment.abs;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.settings.AppSettings;
import com.andrew.apollo.cache.ImageCache;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午6:44
 * @description:
 */
public abstract class AbsBaseListFragment<T> extends AbsStatusAbstraction<T> implements
    SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG="AbsBaseListFragment";
    protected ContentResolver mResolver;
    protected LayoutAdapter mAdapter;

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
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeLayout;
    protected boolean mLastItemVisible;
    /**
     * 空数据时显示的内容。
     */
    protected TextView mEmptyTxt;
    //protected View mHeader;

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
     * 是否已经添加到Activity中,新的fragment会有这样的问题.
     */
    protected boolean hasAttach=false;

    @Override
    public void onDetach() {
        super.onDetach();
        hasAttach=false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        footerView=new RelativeLayout(getActivity());
        mResolver=App.getAppContext().getContentResolver();

        if (null==mDataList) {
            mDataList=new ArrayList<T>();
        }
        initLayoutAdapter();

        mStatusData=new SStatusData<T>();
        mStatusData.mStatusData=mDataList;

        this.zoomRunnable=new Runnable() {
            public void run() {
                fadeZoom();
            }
        };
    }

    protected void initLayoutAdapter() {
        if (null==mAdapter) {
            mAdapter=new LayoutAdapter(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppSettings appSettings=AppSettings.current();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "onResume:"+appSettings.showBitmap+" showLargeBitmap:"+appSettings.showLargeBitmap);
        }

        if (null!=zoomLayout) {
            if (appSettings.showNavBtn) {
                showZoom();
            } else {
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
        state=false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoadingLayout=null;
        if (null!=footerView&&null!=footerView.getParent()) {
            ((ViewGroup) footerView.getParent()).removeView(footerView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "onCreateView.");
        }
        View view=_onCreateView(inflater, container, savedInstanceState);
        mRoot=view;
        themeBackground();

        return view;
    }

    /**
     * 必须由子类实现，如果没有实现此方法就没有mRecyclerView，所以子类一定要初始化mRecyclerView。
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root=(RelativeLayout) inflater.inflate(R.layout.ak_layout_recycler_view, null);
        mEmptyTxt=(TextView) root.findViewById(R.id.empty_txt);
        mRecyclerView=(RecyclerView) root.findViewById(R.id.statusList);

        // ------------------------------------------------------------------

        up=(ImageView) root.findViewById(R.id.up);
        down=(ImageView) root.findViewById(R.id.down);
        up.setOnClickListener(navClickListener);
        down.setOnClickListener(navClickListener);

        up.setBackgroundColor(Color.TRANSPARENT);
        down.setBackgroundColor(Color.TRANSPARENT);
        zoomLayout=(RelativeLayout) root.findViewById(R.id.zoomLayout);
        zoomAnim=AnimationUtils.loadAnimation(getActivity(), R.anim.zoom);

        //mHeader=inflater.inflate(R.layout.ak_overlay_header, null);

        mSwipeLayout=(SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        AppSettings appSettings=AppSettings.current();
        if (appSettings.showLargeBitmap) {
            //mRecyclerView.setRecyclerListener(new RecyclerViewHolder());
        }
        mRecyclerView.setOnScrollListener(getScrollListener());
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null!=mMoreProgressBar) {
                    mMoreProgressBar.setVisibility(View.VISIBLE);
                }
                mSwipeLayout.setRefreshing(true);
                fetchMore();
            }
        });

        return root;
    }

    public RecyclerView.OnScrollListener getScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                //WeiboLog.d(TAG, "onScrollStateChanged.scrollState:"+scrollState+" mLastItemVisible:"+mLastItemVisible);
                if (scrollState==RecyclerView.SCROLL_STATE_SETTLING) {
                    ImageCache.getInstance(getActivity()).setPauseDiskCache(true);
                } else if (scrollState==RecyclerView.SCROLL_STATE_IDLE) {
                    ImageCache.getInstance(getActivity()).setPauseDiskCache(false);
                    /*isEndOfList();
                    if (mLastItemVisible) {
                        showMoreView();
                        //scrollToFooter();
                    } else {
                    }*/
                    mAdapter.notifyDataSetChanged();
                } else {
                    ImageCache.getInstance(getActivity()).setPauseDiskCache(true);
                    /*isEndOfList();
                    if (mLastItemVisible) {
                        showMoreView();
                        //scrollToFooter();
                    } else {
                    }*/
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisibleItem = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
                //WeiboLog.d(TAG, "lastVisibleItem:"+lastVisibleItem+" total:"+totalItemCount);
                if (lastVisibleItem >= totalItemCount - 2 && dy > 0) {
                    if(mLastItemVisible){
                        if (WeiboLog.isDEBUG()) {
                            WeiboLog.d(TAG,"ignore manually update!");
                        }
                    } else{
                        showMoreView();
                        mLastItemVisible=true;
                    }
                }
            }
        };
    }

    public void scrollToHeader() {
        if (mAdapter.getCount()>0) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    public void scrollToFooter() {
        if (mAdapter.getItemCount()>0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount()-1);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null==mStatusImpl) {
            throw new IllegalArgumentException("StatusImpl should not be null.");
        }
        isLoading=false;
        _onActivityCreated(savedInstanceState);
    }

    /**
     * 这个方法可以由子类覆盖，这样可以有一些变化
     *
     * @param savedInstanceState
     */
    public void _onActivityCreated(Bundle savedInstanceState) {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        //final Drawable divider = getResources().getDrawable(R.drawable.divider);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        initLayoutAdapter();
        //showMoreView();
        mAdapter.addFooterView(footerView);
        mRecyclerView.setAdapter(mAdapter);

        WeiboLog.v(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        mSwipeLayout.setRefreshing(true);
        loadData();
    }

    public boolean isEndOfList() {
        if (mRecyclerView.getChildCount()==0) {
            mLastItemVisible=false;
            return false;
        }

        int totalCount=mAdapter.getCount()-1;
        int lastItemPositionOnScreen=((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        //WeiboLog.d(TAG, "lastItemPositionOnScreen:"+lastItemPositionOnScreen+" total:"+totalCount);
        if (totalCount>lastItemPositionOnScreen) {
            mLastItemVisible=false;
            return false;
        }

        final int lastItemBottomPosition=mRecyclerView.getChildAt(mRecyclerView.getChildCount()-1).getBottom();
        //WeiboLog.d(TAG, "lastItemBottomPosition:"+lastItemBottomPosition+" height:"+mRecyclerView.getHeight());
        if (lastItemBottomPosition<=mRecyclerView.getHeight()) {
            mLastItemVisible=true;
            return true;
        }

        mLastItemVisible=false;
        return false;
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected boolean itemLongClick(int pos, View achor) {
        selectedPos=pos;
        //showButtonBar(achor);
        /*View view=up;
        if (up.getVisibility()==View.GONE) {    //竖向时，菜单项会因为内容太长而显示部分。
            view=achor;
        }*/
        prepareMenu(up);
        return true;
    }

    //--------------------- datas ---------------------
    @Override
    public void onRefresh() {
        refresh();
    }

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
        mSwipeLayout.setRefreshing(true);
        pullToRefreshData();
    }

    /**
     * clear list's datas
     */
    @Override
    public void clear() {
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
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "null==mLoadingLayout.");
            }
            mLoadingLayout=(RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext())
                .inflate(R.layout.ak_more_progressbar, null);
            mMoreProgressBar=(ProgressBar) mLoadingLayout.findViewById(R.id.progress_bar);
            mMoreTxt=(TextView) mLoadingLayout.findViewById(R.id.more_txt);
        }

        mMoreTxt.setText(R.string.more);
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
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    /**
     * 线程执行前期的操作
     */
    public void basePreOperation() {
        //WeiboLog.v(TAG, "basePreOperation:"+mPullRefreshListView);
        if (null!=mSwipeLayout) {
            mSwipeLayout.setRefreshing(true);
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
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "baseBackgroundOperation:"+objects);
            }
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
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "basePostOperation");
        }
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

            NotifyUtils.showToast(msg, Toast.LENGTH_LONG);
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
            NotifyUtils.showToast(msg, Toast.LENGTH_LONG);
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
        isLoading=false;
        mLastItemVisible=false;
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "refreshAdapter.load:"+load+" isRefresh:"+isRefresh);
        }
        if (load) {
            mAdapter.notifyDataSetChanged();
        }
        mSwipeLayout.setRefreshing(false);

        if (isRefresh) {
            scrollToHeader();
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
    protected void navClick(View view) {
        AppSettings appSettings=AppSettings.current();
        if (view.getId()==R.id.up) {
            showZoom();
            if (appSettings.showNavPageBtn) {
                int scrollY=mRecyclerView.getScrollY();
                scrollY-=mRecyclerView.getHeight();
                mRecyclerView.scrollBy(0, scrollY);
            } else {
                scrollToHeader();
            }
        } else if (view.getId()==R.id.down) {
            showZoom();
            if (appSettings.showNavPageBtn) {
                int scrollY=mRecyclerView.getScrollY();
                scrollY+=mRecyclerView.getHeight();
                mRecyclerView.scrollBy(0, scrollY);
            } else {
                scrollToFooter();
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
     * @param convertView
     * @param parent
     * @param position
     * @param itemType
     * @return
     */
    public abstract View getView(SimpleViewHolder holder, final int position, int itemType);

    public abstract View newView(ViewGroup parent, int viewType);

    public int getItemViewType(int position) {
        return -1;
    }

    //--------------------- adapter ---------------------
    public class LayoutAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

        /**
         * 页眉
         */
        public static final int TYPE_HEADERVIEW=0x001;
        /**
         * 页脚
         */
        public static final int TYPE_FOOTERVIEW=0x002;
        /**
         * 基础的,没有包含图片的项
         */
        public static final int TYPE_BASE=0x003;
        /**
         * 包含图片的列表项.
         */
        public static final int TYPE_PICTURE=0x004;
        public static final int TYPE_NORET=0x005;
        public static final int TYPE_LOCATION=0x006;
        private final Context mContext;
        protected View footerView;

        public View getFooterView() {
            return footerView;
        }

        public void addFooterView(View footerView) {
            this.footerView=footerView;
        }

        public void removeFooterView() {
            this.footerView=null;
        }

        public LayoutAdapter(Context context) {
            mContext=context;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mView;
            switch (viewType) {
                /*case TYPE_HEADERVIEW:
                    //mView=getHeaderView();
                    break;*/
                case TYPE_FOOTERVIEW:
                    mView=getFooterView();
                    //WeiboLog.d(TAG, "getFooterView");
                    break;
                default:
                    mView=AbsBaseListFragment.this.newView(parent, viewType);
                    break;
            }
            //mView=RecyclerViewFragment.this.newView(parent, viewType);
            // LayoutInflater.from(mContext).inflate(R.layout.test_row_staggered_demo, parent, false);
            return new SimpleViewHolder(mView);
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            int itemType=getItemViewType(position);
            if (itemType==TYPE_FOOTERVIEW) {

            } else {
                AbsBaseListFragment.this.getView(holder, position, itemType);
            }
        }

        @Override
        public int getItemCount() {
            return mDataList.size()+(footerView==null ? 0 : 1);
        }

        public int getCount() {
            return mDataList.size();
        }

        public T getItem(int pos) {
            if (mDataList!=null&&pos<mDataList.size()) {
                return mDataList.get(pos);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            int count=getItemCount()-1;
            /*if (isHeaderView(position)) {
                return TYPE_HEADERVIEW;
            } else*/
            if (isFooterView(position, count)) {
                return TYPE_FOOTERVIEW;
            } else {
                int viewType=AbsBaseListFragment.this.getItemViewType(position);
                if (-1!=viewType) {
                    return viewType;
                }
                return super.getItemViewType(position);
            }
        }

        private boolean isFooterView(int position, int count) {
            return position==count&&footerView!=null;
        }
    }
}
