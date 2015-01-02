package cn.archko.microblog.fragment;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.PopupMenu;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.ThreadBeanItemView;
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
public abstract class RecyclerViewFragment extends AbsBaseListFragment<Status> {

    public static final String TAG="RecyclerViewFragment";

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

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
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
            NotifyUtils.showToast("您需要先选中一个项!");
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
            NotifyUtils.showToast("您需要先选中一个项!");
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
            NotifyUtils.showToast("您需要先选中一个项!");
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
            NotifyUtils.showToast("转发任务添加到队列服务中了。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        ThreadBeanItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new ThreadBeanItemView(getActivity(), mCacheDir, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView=(ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(position, view);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPos=position;
                prepareMenu(up);
                return true;
            }
        });

        return itemView;
    }

    public View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        ThreadBeanItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new ThreadBeanItemView(getActivity(), mCacheDir, updateFlag, true, showLargeBitmap, showBitmap);
        return itemView;
    }

    //------------------------------------------

}
