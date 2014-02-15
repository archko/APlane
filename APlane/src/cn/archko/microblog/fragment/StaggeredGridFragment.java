package cn.archko.microblog.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.bulletnoid.android.widget.StaggeredGridView.StaggeredGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshStaggeredGridView;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;

import java.util.Date;

/**
 * @version 1.00.00  用户的网格，这个是超类，需要修改原来的ListView的行为。这里包含了所有的用户列表，
 *          不像之前的UserListFragment
 * @description:
 * @author: archko 14-2-9
 */
public abstract class StaggeredGridFragment extends AbsBaseListFragment<Status> {

    public static final String TAG="StaggeredGridFragment";
    protected PullToRefreshStaggeredGridView mPullRefreshListView;
    protected StaggeredGridView mGridView;

    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root=(ViewGroup) inflater.inflate(R.layout.ak_grid_list, null);
        mEmptyTxt=(TextView) root.findViewById(R.id.empty_txt);
        mPullRefreshListView=(PullToRefreshStaggeredGridView) root.findViewById(R.id.pull_refresh_grid);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mGridView=mPullRefreshListView.getRefreshableView();
        mGridView.setSelector(R.drawable.list_selector_holo_light);
        mGridView.setColumnCount(1);

        up=(ImageView) root.findViewById(R.id.up);
        down=(ImageView) root.findViewById(R.id.down);
        up.setOnClickListener(navClickListener);
        down.setOnClickListener(navClickListener);

        up.setBackgroundColor(Color.TRANSPARENT);
        down.setBackgroundColor(Color.TRANSPARENT);
        zoomLayout=(RelativeLayout) root.findViewById(R.id.zoomLayout);
        zoomAnim=AnimationUtils.loadAnimation(getActivity(), R.anim.zoom);

        return root;
    }

    @Override
    protected void navClick(View view) {
        if (view.getId()==R.id.up) {
            showZoom();
            if (showNavPageBtn) {
                mGridView.performAccessibilityAction(0x00002000);
            }
        } else if (view.getId()==R.id.down) {
            showZoom();
            if (showNavPageBtn) {
                mGridView.performAccessibilityAction(0x00001000);
            }
        }
    }

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.d(TAG, "_onActivityCreated");
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<StaggeredGridView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<StaggeredGridView> refreshView) {
                pullToRefreshData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<StaggeredGridView> refreshView) {
                fetchMore();
            }
        });
        //mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(new StaggeredGridView.OnItemClickListener() {
            @Override
            public void onItemClick(StaggeredGridView parent, View view, int pos, long id) {
                WeiboLog.d(TAG, "itemClick:"+pos);
                selectedPos=pos;

                itemClick(view);
            }
        });
        /*mGridView.setOnItemLongClickListener(new StaggeredGridView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(StaggeredGridView parent, View view, int pos, long id) {
                WeiboLog.d(TAG, "itemLongClick:"+pos);
                selectedPos=pos;
                //showButtonBar(view);
                itemLongClick(view);
                return true;
            }
        });*/

        if (mAdapter==null) {
            mAdapter=new TimeLineAdapter();
        }

        mGridView.setAdapter(mAdapter);

        WeiboLog.i(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
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

        ThreadBeanItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView=(ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);

        return itemView;
    }

    //--------------------- 数据获取 ---------------------

    /**
     * 获取更多数据的操作，统一处理了，就是获取当前列表最后一项，用它的id获取更多数据。
     * //TODO 因为更多数据有可能就是为空，如果获取到空数据，是没有更多数据，下次不应该再调用。
     */
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            Status st;
            st=(Status) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
        }
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter.load:"+load+" isRefresh:"+isRefresh);
        mPullRefreshListView.onRefreshComplete();
        if (load) {
            mAdapter.notifyDataSetChanged();
        }

        if (isRefresh) {
            mPullRefreshListView.setLastUpdatedLabel(getString(R.string.pull_to_refresh_label)+DateUtils.longToDateTimeString(System.currentTimeMillis()));
        }

        if (mDataList.size()>0) {
            if (mEmptyTxt.getVisibility()==View.VISIBLE) {
                mEmptyTxt.setVisibility(View.GONE);
            }
        } else {
            mEmptyTxt.setVisibility(View.VISIBLE);
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
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);

            WeiboOperation.toViewOriginalStatus(getActivity(), status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_quick_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_ORITEXT, index++, R.string.opb_origin_text);
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPOST, index++, R.string.opb_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FAVORITE, index++, R.string.opb_favorite);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        WeiboLog.d(TAG, "onMenuItemClick:"+menuId);
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
        WeiboLog.d(TAG, "selectedPos:"+selectedPos);
        if (selectedPos==-1) {
            AKUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);
            if (null!=status) {
                /*String type="0";
                Long statusId=status.id;
                OperationTask task=new OperationTask();
                task.execute(new Object[]{type, statusId});*/
                Intent taskService=new Intent(getActivity(), SendTaskService.class);
                SendTask task=new SendTask();
                task.uid=currentUserId;
                task.userId=currentUserId;
                task.content=status.text;
                task.source=String.valueOf(status.id);
                task.type=TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV;
                task.createAt=new Date().getTime();
                taskService.putExtra("send_task", task);
                getActivity().startService(taskService);
                AKUtils.showToast("新收藏任务添加到队列服务中了。");
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
            Status status=mDataList.get(selectedPos);

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
            Status status=mDataList.get(selectedPos);

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
        if (selectedPos==-1) {
            AKUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);
            if (null!=status) {
                User user=status.user;
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
        if (selectedPos==-1) {
            AKUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);
            //WeiboOperation.quickRepostStatus(status.id);
            Intent taskService=new Intent(getActivity(), SendTaskService.class);
            SendTask task=new SendTask();
            task.uid=currentUserId;
            task.userId=currentUserId;
            task.content="";
            task.source=String.valueOf(status.id);
            task.data="0";
            task.type=TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
            task.text=status.text;
            task.createAt=new Date().getTime();
            taskService.putExtra("send_task", task);
            getActivity().startService(taskService);
            AKUtils.showToast("转发任务添加到队列服务中了。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
