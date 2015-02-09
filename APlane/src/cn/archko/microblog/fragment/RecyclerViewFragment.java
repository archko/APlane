package cn.archko.microblog.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.bean.Status;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

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
    public void viewOriginalStatus(View achor) {
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        Status status=mDataList.get(selectedPos);

        mWeiboController.viewOriginalStatus(achor, status, getActivity());
    }

    /**
     * 创建收藏.
     */
    public void createFavorite() {
        WeiboLog.d(TAG, "selectedPos:"+selectedPos);
        if (selectedPos==-1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        Status status=mDataList.get(selectedPos);
        mWeiboController.createFavorite(status, currentUserId, TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV, getActivity());
    }

    /**
     * 跳转到到评论界面
     */
    public void commentStatus() {
        Status status=mDataList.get(selectedPos);
        mWeiboController.commentStatus(status, getActivity());
    }

    /**
     * 到转发界面
     */
    public void repostStatus() {
        Status status=mDataList.get(selectedPos);

        mWeiboController.repostStatus(status, getActivity());
    }

    /**
     * 查看用户信息
     */
    public void viewStatusUser() {
        WeiboLog.d(TAG, "not implemented.");
        if (selectedPos==-1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        Status status=mDataList.get(selectedPos);
        mWeiboController.viewStatusUser(status, getActivity(), UserFragmentActivity.TYPE_USER_INFO);
    }

    /**
     * 快速转发
     */
    public void quickRepostStatus() {
        WeiboLog.d(TAG, "quickRepostStatus.");
        if (selectedPos==-1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        Status status=mDataList.get(selectedPos);
        mWeiboController.quickRepostStatus(status, currentUserId, getActivity());
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
            itemView=new ThreadBeanItemView(getActivity(), mCacheDir, updateFlag, true);
        } else {
            itemView=(ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true);
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
        itemView=new ThreadBeanItemView(getActivity(), mCacheDir, updateFlag, true);
        return itemView;
    }

    //------------------------------------------

}
