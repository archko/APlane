package cn.archko.microblog.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.recycler.RecyclerViewHolder;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.andrew.apollo.cache.ImageCache;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.Date;

/**
 * @author: archko 30-12-12
 */
public abstract class RecyclerViewFragment extends AbsBaseListFragment<Status> implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "RecyclerViewFragment";
    private RecyclerView mRecyclerView;
    LayoutAdapter mAdapter;
    SwipeRefreshLayout mSwipeLayout;

    @Override
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.ak_layout_recycler_view, null);
        mEmptyTxt = (TextView) root.findViewById(R.id.empty_txt);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.statusList);

        // ------------------------------------------------------------------

        up = (ImageView) root.findViewById(R.id.up);
        down = (ImageView) root.findViewById(R.id.down);
        up.setOnClickListener(navClickListener);
        down.setOnClickListener(navClickListener);

        up.setBackgroundColor(Color.TRANSPARENT);
        down.setBackgroundColor(Color.TRANSPARENT);
        zoomLayout = (RelativeLayout) root.findViewById(R.id.zoomLayout);
        zoomAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom);

        mHeader = inflater.inflate(R.layout.ak_overlay_header, null);

        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        mRecyclerView.setRecyclerListener(new RecyclerViewHolder());
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                WeiboLog.d(TAG, "onScrollStateChanged.scrollState:" + scrollState);
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
                    || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    ImageCache.getInstance(getActivity()).setPauseDiskCache(true);
                    //WeiboLog.v(TAG, "onScrollStateChanged.scroll");
                } else {
                    ImageCache.getInstance(getActivity()).setPauseDiskCache(false);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return root;
    }

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.v(TAG, "onActivityCreated");

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        /*final ItemClickSupport itemClick = ItemClickSupport.addTo(mRecyclerView);

        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int pos, long id) {
                int position = pos;
                if (position == - 1) {
                    WeiboLog.v("选中的是头部，不可点击");
                    return;
                }

                selectedPos = position;
                WeiboLog.v(TAG, "itemClick:" + pos + " selectedPos:" + selectedPos);

                if (view == footerView) {   //if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                    mMoreProgressBar.setVisibility(View.VISIBLE);
                    fetchMore();
                    return;
                }
                RecyclerViewFragment.this.itemClick(view);
            }
        });

        itemClick.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int pos, long id) {
                WeiboLog.v(TAG, "itemLongClick:" + pos);
                int position = pos;
                selectedPos = position;

                if (mAdapter.getItemCount() > 0 && position >= mAdapter.getItemCount()) {
                    WeiboLog.v(TAG, "footerView.click.");
                    return true;
                }

                if (view != footerView) {
                    //showButtonBar(view);
                    return RecyclerViewFragment.this.itemLongClick(view);
                }
                return true;
            }
        });*/

        //final Drawable divider = getResources().getDrawable(R.drawable.divider);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        mAdapter = new LayoutAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        WeiboLog.v(TAG, "isLoading:" + isLoading + " status:" + (null == mDataList ? "null" : mDataList.size()));
        mSwipeLayout.setRefreshing(true);
        loadData();
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为true,其它的不缓存,比如随便看看.
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
            itemView = new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView = (ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);

        return itemView;
    }

    @Override
    public void onRefresh() {
        /*if (mSwipeLayout.getCurrentPullMode() == SwipeRefreshLayoutUpDown.PullMode.PULL_FROM_START) {
            pullToRefreshData();
        } else if (mSwipeLayout.getCurrentPullMode() == SwipeRefreshLayoutUpDown.PullMode.PULL_FROM_END) {
            pullUpRefreshData();
        }*/
        pullToRefreshData();
    }

    protected void navClick(View view) {
        if (view.getId() == R.id.up) {
            showZoom();
            if (showNavPageBtn) {
                int scrollY = mRecyclerView.getScrollY();
                scrollY -= mRecyclerView.getHeight();
                mRecyclerView.scrollBy(0, scrollY);
            } else {
                mRecyclerView.smoothScrollToPosition(0);
            }
        } else if (view.getId() == R.id.down) {
            showZoom();
            if (showNavPageBtn) {
                int scrollY = mRecyclerView.getScrollY();
                scrollY += mRecyclerView.getHeight();
                mRecyclerView.scrollBy(0, scrollY);
            } else {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }

    //--------------------- 数据获取 ---------------------

    /**
     * 获取更多数据的操作，统一处理了，就是获取当前列表最后一项，用它的id获取更多数据。
     * //TODO 因为更多数据有可能就是为空，如果获取到空数据，是没有更多数据，下次不应该再调用。
     */
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:" + lastItem + " selectedPos:" + selectedPos);
        if (mAdapter.getItemCount() > 0) {
            Status st;
            st = (Status) mDataList.get(mAdapter.getItemCount() - 1);
            fetchData(- 1, st.id, false, false);
        }
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(View achor) {
        viewOriginalStatus(achor);
    }

    /**
     * 查看Status原文信息,包括评论.
     */
    @Override
    protected void viewOriginalStatus(View achor) {
        if (selectedPos >= mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);

            WeiboOperation.toViewOriginalStatus(getActivity(), status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index = 0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_quick_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_ORITEXT, index++, R.string.opb_origin_text);
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPOST, index++, R.string.opb_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FAVORITE, index++, R.string.opb_favorite);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId = item.getItemId();
        WeiboLog.d(TAG, "onMenuItemClick:" + menuId);
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_FAVORITE: {
                createFavorite();
                break;
            }
            case Constants.OP_ID_REPOST: {
                repostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
            case Constants.OP_ID_ORITEXT: {
                viewOriginalStatus(null);
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                viewStatusUser();
                break;
            }
        }
        return true;
    }

    /**
     * 创建收藏.
     */
    protected void createFavorite() {
        WeiboLog.d(TAG, "selectedPos:" + selectedPos);
        if (selectedPos == - 1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);
            if (null != status) {
                /*String type="0";
                Long statusId=status.id;
                OperationTask task=new OperationTask();
                task.execute(new Object[]{type, statusId});*/
                Intent taskService = new Intent(getActivity(), SendTaskService.class);
                SendTask task = new SendTask();
                task.uid = currentUserId;
                task.userId = currentUserId;
                task.content = status.text;
                task.source = String.valueOf(status.id);
                task.type = TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV;
                task.createAt = new Date().getTime();
                taskService.putExtra("send_task", task);
                getActivity().startService(taskService);
                NotifyUtils.showToast("新收藏任务添加到队列服务中了。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面
     */
    protected void commentStatus() {
        try {
            Status status = mDataList.get(selectedPos);

            WeiboOperation.toCommentStatus(getActivity(), status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 到转发界面
     */
    protected void repostStatus() {
        try {
            Status status = mDataList.get(selectedPos);

            WeiboOperation.toRepostStatus(getActivity(), status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看用户信息
     */
    protected void viewStatusUser() {
        WeiboLog.d(TAG, "not implemented.");
        if (selectedPos == - 1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);
            if (null != status) {
                User user = status.user;
                WeiboOperation.toViewStatusUser(getActivity(), user, UserFragmentActivity.TYPE_USER_INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 快速转发
     */
    protected void quickRepostStatus() {
        WeiboLog.d(TAG, "quickRepostStatus.");
        if (selectedPos == - 1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);
            //WeiboOperation.quickRepostStatus(status.id);
            Intent taskService = new Intent(getActivity(), SendTaskService.class);
            SendTask task = new SendTask();
            task.uid = currentUserId;
            task.userId = currentUserId;
            task.content = "";
            task.source = String.valueOf(status.id);
            task.data = "0";
            task.type = TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
            task.text = status.text;
            task.createAt = new Date().getTime();
            taskService.putExtra("send_task", task);
            getActivity().startService(taskService);
            NotifyUtils.showToast("转发任务添加到队列服务中了。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View getView(SimpleViewHolder holder, int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView = holder.baseItemView;
        ThreadBeanItemView itemView = null;
        Status status = mDataList.get(position);

        boolean updateFlag = true;
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag = false;
        }

        if (convertView == null) {
            itemView = new ThreadBeanItemView(getActivity(), null, mCacheDir, status, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView = (ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(view);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                prepareMenu(up);
                return true;
            }
        });

        return itemView;
    }

    protected View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        ThreadBeanItemView itemView = null;
        boolean updateFlag = true;
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag = false;
        }
        itemView = new ThreadBeanItemView(getActivity(), null, mCacheDir, null, updateFlag, true, showLargeBitmap, showBitmap);
        return itemView;
    }

    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter.load:" + load + " isRefresh:" + isRefresh);
        if (load) {
            mAdapter.notifyDataSetChanged();
        }
        mSwipeLayout.setRefreshing(false);

        if (isRefresh) {
        }

        if (mDataList.size() > 0) {
            if (mEmptyTxt.getVisibility() == View.VISIBLE) {
                mEmptyTxt.setVisibility(View.GONE);
            }
        } else {
            mEmptyTxt.setText(R.string.list_empty_txt);
            mEmptyTxt.setVisibility(View.VISIBLE);
        }
    }

    //------------------------------------------
    public class LayoutAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

        private final Context mContext;

        public LayoutAdapter(Context context) {
            mContext = context;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = RecyclerViewFragment.this.newView(parent, viewType);
            // LayoutInflater.from(mContext).inflate(R.layout.test_row_staggered_demo, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            RecyclerViewFragment.this.getView(holder, position);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }

}
