package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.UserGridItemView;
import com.andrew.apollo.utils.ApolloUtils;
import com.me.microblog.App;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00  用户的网格，这个是超类，需要修改原来的ListView的行为。这里包含了所有的用户列表，
 *          不像之前的UserListFragment
 * @description:
 * @author: archko 11-12-29
 */
public abstract class UserGridFragment extends AbsBaseListFragment<User> {   //TODO 需要与UserListFragment合并

    public static final String TAG="UserGridFragment";
    protected int nextCursor=-1;//下一页索引，第一页为-1，不是0
    protected long mUserId=-1l; //查看用户的id
    protected boolean isFollowing=false;    //是否正在处理关系,默认只一个一个处理,不并行处理.
    /**
     * 类型是自己，表示查看的用户是登录者
     */
    public static final int TYPE_SELF=0;
    /**
     * 查看的是其它用户
     */
    public static final int TYPE_USER=1;
    /**
     * 类型不同，加载数据的方式也不同。
     */
    int type=TYPE_SELF;

    public UserGridFragment() {
    }

    public UserGridFragment(Bundle bundle) {
        if (null!=bundle) {
            type=bundle.getInt("type", 0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=getArguments();
        if (null!=bundle) {
            type=bundle.getInt("type", 0);
        }
        WeiboLog.d(TAG, "onCreate:"+bundle+" type:"+type);

        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        mUserId=aUserId;
    }

    @Override
    public void onResume() {
        super.onResume();
        weibo_count=Constants.WEIBO_COUNT_MIN;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!hasAttach) {   //不在onAttach中处理,因为refresh可能先调用,以保证数据初始化.
            hasAttach=true;
        }
    }

    public void _onActivityCreated(Bundle savedInstanceState) {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        GridLayoutManager lm = new GridLayoutManager(getActivity(), 2);
        //lm.setReverseLayout(true);
        //SpanSizeLookup mSpanSizeLookup = new DefaultSpanSizeLookup();
        //lm.setSpanSizeLookup(mSpanSizeLookup);
        mRecyclerView.setLayoutManager(lm);
        //final Drawable divider = getResources().getDrawable(R.drawable.divider);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        mAdapter=new LayoutAdapter(getActivity());
        mAdapter.addFooterView(footerView);
        mRecyclerView.setAdapter(mAdapter);

        WeiboLog.v(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        mSwipeLayout.setRefreshing(true);
        loadData();
    }

    private void initGridView() {
        // Set up the helpers
        if (ApolloUtils.isLandscape(getActivity())) {
            /*if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(TWO);
            } else {
                mGridView.setNumColumns(FOUR);
            }*/
        } else {
            /*if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(ONE);
            } else {
                mGridView.setNumColumns(TWO);
            }*/
        }
    }

    @Override
    public void refresh() {
        WeiboLog.i(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        loadData();
    }

    protected void showMoreView() {
        WeiboLog.v(TAG, "showMoreView");
        if (null==mLoadingLayout) {
            WeiboLog.d(TAG, "null==mLoadingLayout.");
            mLoadingLayout=(RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext())
                .inflate(R.layout.ak_grid_more_item, null);
            mMoreProgressBar=(ProgressBar) mLoadingLayout.findViewById(R.id.progress_bar);
            mMoreTxt=(TextView) mLoadingLayout.findViewById(R.id.more_txt);
        }

        mMoreTxt.setText(R.string.more);
        footerView.removeAllViews();
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        footerView.addView(mLoadingLayout, layoutParams);

        mMoreProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        UserGridItemView itemView=null;
        User user=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new UserGridItemView(getActivity(), mCacheDir, updateFlag);
        } else {
            itemView=(UserGridItemView) convertView;
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
        UserGridItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new UserGridItemView(getActivity(), mCacheDir, updateFlag);
        return itemView;
    }

    //--------------------- 数据加载 ---------------------

    /**
     * 下拉刷新数据
     */
    protected void pullToRefreshData() {
        //page=1;
        fetchData(-1, -1, true, false);
    }

    @Override
    public void fetchMore() {
        WeiboLog.d(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos+" footView:");
        if (mAdapter.getCount()>0) {
            User st;
            st=(User) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
        }
    }

    @Override
    protected void loadData() {
        Intent intent=getActivity().getIntent();

        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (hasAttach) {
                long userId=intent.getLongExtra("user_id", -1);
                //userScreenName=intent.getStringExtra("screen_name");

                if (userId==mUserId) {
                    WeiboLog.i(TAG, "相同的用户不加载");
                    if (mDataList!=null&&mDataList.size()>0) {
                        mAdapter.notifyDataSetChanged();
                    }
                    return;
                }

                if (userId==-1) {
                    WeiboLog.w(TAG, "用户的id错误，无法获取数据,现在获取登录用户信息。");
                    //showToast("用户的id错误，无法获取数据。", Toast.LENGTH_SHORT);
                    //return;
                    userId=currentUserId;
                }

                if (userId==100) {
                    NotifyUtils.showToast(R.string.user_id_error);
                    WeiboLog.w(TAG, "人员的id没有，有可能是只传来昵称！");
                    return;
                }

                mDataList.clear();
                mUserId=userId;
                if (!isLoading) {
                    fetchData(-1, -1, true, false);
                } else {
                    mEmptyTxt.setText(R.string.list_pre_empty_txt);
                    mEmptyTxt.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 获取数据，覆盖此方法，因为用户的获取与其它不一样。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
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
            count++;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, mUserId, maxId, count, page, isHomeStore}, null);
        }
    }

    //--------------------- 微博操作 ---------------------

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

    /**
     * 单击，这里是查看用户资料。
     *
     * @param achor 用于显示QuickAction
     */
    @Override
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            User user=mDataList.get(selectedPos);

            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    @Override
    protected boolean itemLongClick(int pos, View achor) {
        selectedPos=pos;
        prepareMenu(achor);
        return true;
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
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_TIMELINE);
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

        try {
            User user=mDataList.get(selectedPos);
            mWeiboController.viewUser(user, getActivity(), UserFragmentActivity.TYPE_USER_INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO需要处理token过期的状况。

    /**
     * 处理关系，关注或取消关注，不作粉丝移除处理。
     */
    protected void followUser() {
        if (isFollowing) {
            WeiboLog.d("正在处理关系.");
            return;
        }
        FollwingTask follwingTask=new FollwingTask();
        follwingTask.execute(0);
    }

    class FollwingTask extends AsyncTask<Integer, Void, User> {

        int followingType=1;//0表示未关注,1表示已关注

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isFollowing=true;
            User user=mDataList.get(selectedPos);
            followingType=user.following ? 1 : 0;
        }

        @Override
        protected User doInBackground(Integer... params) {
            try {
                User now=null;

                //SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(getActivity());
                SinaUserApi weiboApi2=new SinaUserApi();
                weiboApi2.updateToken();
                /*if (null==weiboApi2) {
                    showToast(R.string.err_api_error);
                    return now;
                }*/

                User user=mDataList.get(selectedPos);
                if (followingType==0) {
                    now=weiboApi2.createFriendships(user.id);
                } else if (followingType==1) {
                    now=weiboApi2.deleteFriendships(user.id);
                }

                return now;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User resultObj) {
            if (!isResumed()) {
                return;
            }

            isFollowing=false;
            if (resultObj==null) {
                NotifyUtils.showToast("处理关系失败", Toast.LENGTH_LONG);
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            if (followingType==0) {
                NotifyUtils.showToast("follow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            } else if (followingType==1) {
                NotifyUtils.showToast("unfollow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            }
        }

    }
}
