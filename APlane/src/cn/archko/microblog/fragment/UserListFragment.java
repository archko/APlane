package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.UserItemView;
import com.me.microblog.bean.User;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 公共的用户Fragment，默认实现了所有的QuickAction项的功能与加载更多的内容。
 * getStatuses需要子类来实现，暂时不设为抽象类。
 * @author: archko 12-9-12
 */
public abstract class UserListFragment extends AbsBaseListFragment<User> {   //TODO 需要与UserGridFragment合并

    public static final String TAG="UserListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPlaceUserImpl();
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            User st;
            st=(User) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
        }
    }

    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        UserItemView itemView=null;
        User user=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new UserItemView(getActivity(), mCacheDir, updateFlag);
        } else {
            itemView=(UserItemView) convertView;
        }
        itemView.update(user, updateFlag, false);
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
        UserItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new UserItemView(getActivity(), mCacheDir, updateFlag);
        return itemView;
    }

    //--------------------- 用户操作 ---------------------

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_STATUS, index++, R.string.opb_user_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_UNFOLLOW, index++, R.string.opb_unfollow);
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, index++, R.string.opb_user_info);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FOLLOWS, index++, R.string.opb_follows);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FRIENDS, index++, R.string.opb_friends);
        menuBuilder.getMenu().add(0, Constants.OP_ID_AT, index++, R.string.opb_at);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FOLLOW, index++, R.string.opb_follow);
    }

    @Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        try {
            User user=mDataList.get(selectedPos);
            if (user.following) {
                menuBuilder.getMenu().findItem(Constants.OP_ID_UNFOLLOW).setVisible(true);
                menuBuilder.getMenu().findItem(Constants.OP_ID_FOLLOW).setVisible(false);
            } else {
                menuBuilder.getMenu().findItem(Constants.OP_ID_UNFOLLOW).setVisible(false);
                menuBuilder.getMenu().findItem(Constants.OP_ID_FOLLOW).setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_STATUS: {
                viewUserStatuses();
                break;
            }
            case Constants.OP_ID_UNFOLLOW: {
                /*if (isFollowing) {
                    WeiboLog.d("正在处理关系.");
                    return;
                }
                FollwingTask follwingTask=new FollwingTask();
                follwingTask.execute(0);*/
                followUser();
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                viewStatusUser();
                break;
            }
            case Constants.OP_ID_FOLLOWS: {
                viewUserFollows();
                break;
            }
            case Constants.OP_ID_FRIENDS: {
                viewUserFriends();
                break;
            }
            case Constants.OP_ID_FOLLOW: {
                followUser();
                break;
            }
            case Constants.OP_ID_AT: {
                atUser();
            }
        }
        return false;
    }

    @Override
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
        //showButtonBar(achor);
        viewStatusUser();
    }

    /**
     * 查看用户发布的微博信息。
     */
    protected void viewUserStatuses() {
        try {
            if (selectedPos>=mAdapter.getCount()) {
                WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
                return;
            }

            User user=mDataList.get(selectedPos);
            WeiboLog.d(TAG, "viewUserStatuses."+user.screenName);
            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_TIMELINE);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void viewUserFollows() {
        try {
            User user=mDataList.get(selectedPos);
            //intent.putExtra("screen_name", user.screenName);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_FOLLOWERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void viewUserFriends() {
        try {
            User user=mDataList.get(selectedPos);
            //intent.putExtra("screen_name", user.screenName);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_FRIENDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO需要处理token过期的状况。

    /**
     * 处理关系，关注或取消关注，不作粉丝移除处理。
     */
    protected void followUser() {
        NotifyUtils.showToast("not implemented.");
    }

    protected void atUser() {
        try {
            User user=mDataList.get(selectedPos);
            Intent intent=new Intent(getActivity(), NewStatusActivity.class);
            intent.putExtra("at_some", user.screenName);
            intent.setAction(Constants.INTENT_NEW_BLOG);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    protected void deleteStatus() {
    }

    /**
     * 查看用户信息
     */
    public void viewStatusUser() {
        WeiboLog.d(TAG, "viewStatusUser.");
        if (selectedPos==-1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        User user=mDataList.get(selectedPos);
        mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_INFO);
    }
}
