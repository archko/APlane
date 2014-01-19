package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.UserGridItemView;
import com.andrew.apollo.utils.ApolloUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.me.microblog.App;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

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
    protected PullToRefreshGridView mPullRefreshGridView;
    protected GridView mGridView;
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

    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout root=(FrameLayout) inflater.inflate(R.layout.user_grid, null);
        mEmptyTxt=(TextView) root.findViewById(R.id.empty_txt);
        mPullRefreshGridView=(PullToRefreshGridView) root.findViewById(R.id.pull_refresh_grid);
        mGridView=mPullRefreshGridView.getRefreshableView();
        mGridView.setDrawSelectorOnTop(true);
        mGridView.setOnScrollListener(this);

        /*SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(getActivity());
        String themeId=options.getString(PrefsActivity.PREF_THEME, "0");
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else if ("2".equals(themeId)) {
        } else if ("3".equals(themeId)) {
        }*/

        // ------------------------------------------------------------------
        /*nav_up=(ImageView) root.findViewById(R.id.nav_up);
        nav_down=(ImageView) root.findViewById(R.id.nav_down);
        nav_up.setOnClickListener(navClickListener);
        nav_down.setOnClickListener(navClickListener);

        nav_up.setBackgroundColor(Color.TRANSPARENT);
        nav_down.setBackgroundColor(Color.TRANSPARENT);
        zoomLayout=(RelativeLayout) root.findViewById(R.id.zoomLayout);
        zoomAnim=AnimationUtils.loadAnimation(getActivity(), R.anim.zoom);*/
        //initGridView();
        return root;
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

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.d(TAG, "onActivityCreated");
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                pullToRefreshData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                fetchMore();
            }
        });
        //mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                WeiboLog.d(TAG, "itemClick:"+pos);
                selectedPos=pos;

                itemClick(view);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                WeiboLog.d(TAG, "itemLongClick:"+pos);
                selectedPos=pos;
                //showButtonBar(view);
                itemLongClick(view);
                return true;
            }
        });

        if (mAdapter==null) {
            mAdapter=new TimeLineAdapter();
        }

        mGridView.setAdapter(mAdapter);

        WeiboLog.i(TAG, "isLoading:"+isLoading+" status:"+(null==mDataList ? "null" : mDataList.size()));
        loadData();
    }

    protected void showMoreView() {
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //WeiboLog.d(TAG, "getView.pos:"+position+" getCount():"+getCount()+" lastItem:");

        UserGridItemView itemView=null;
        User user=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new UserGridItemView(getActivity(), mGridView, mCacheDir, user, updateFlag);
        } else {
            itemView=(UserGridItemView) convertView;
        }
        itemView.update(user, updateFlag, false);

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

    /**
     * 线程执行前期的操作
     */
    public void basePreOperation() {
        mPullRefreshGridView.setRefreshing();
        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshStarted();
        }

        if (null==mDataList||mDataList.size()<1) {
            mEmptyTxt.setText(R.string.list_pre_empty_txt);
            mEmptyTxt.setVisibility(View.VISIBLE);
        }

        isLoading=true;
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter.load:"+load+" isRefresh:"+isRefresh);
        mPullRefreshGridView.onRefreshComplete();
        if (load) {
            mAdapter.notifyDataSetChanged();
        }

        if (isRefresh) {
            mPullRefreshGridView.setLastUpdatedLabel(getString(R.string.pull_to_refresh_label)+DateUtils.longToDateTimeString(System.currentTimeMillis()));
        }

        if (mDataList.size()>0) {
            if (mEmptyTxt.getVisibility()==View.VISIBLE) {
                mEmptyTxt.setVisibility(View.GONE);
            }
        } else {
            mEmptyTxt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void loadData() {
        Intent intent=getActivity().getIntent();

        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
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
                AKUtils.showToast(R.string.user_id_error);
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
            AKUtils.showToast(R.string.network_error);
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
    protected void itemClick(View achor) {
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            User user=mDataList.get(selectedPos);

            WeiboOperation.toViewStatusUser(getActivity(), user, UserFragmentActivity.TYPE_USER_INFO);
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
    protected boolean itemLongClick(View achor) {
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
            WeiboOperation.toViewStatusUser(getActivity(), user, UserFragmentActivity.TYPE_USER_TIMELINE);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void viewUserFollows() {
        try {
            User user=mDataList.get(selectedPos);
            //intent.putExtra("screen_name", user.screenName);
            WeiboOperation.toViewStatusUser(getActivity(), user, UserFragmentActivity.TYPE_USER_FOLLOWERS);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void viewUserFriends() {
        try {
            User user=mDataList.get(selectedPos);
            //intent.putExtra("screen_name", user.screenName);
            WeiboOperation.toViewStatusUser(getActivity(), user, UserFragmentActivity.TYPE_USER_FRIENDS);
            //getActivity().finish(); //这里结束当前的Activity,因为可能造成内存不足.
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
     * 创建收藏.
     */
    protected void createFavorite() {
    }

    /**
     * 跳转到到评论界面
     */
    protected void commentStatus() {
    }

    /**
     * 到转发界面
     */
    protected void repostStatus() {
    }

    /**
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    protected void deleteStatus() {
    }

    /**
     * 查看用户信息
     */
    protected void viewStatusUser() {
        WeiboLog.d(TAG, "viewStatusUser.");
        if (selectedPos==-1) {
            AKUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            User user=mDataList.get(selectedPos);
            if (null!=user) {
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
                AKUtils.showToast("处理关系失败", Toast.LENGTH_LONG);
                WeiboLog.e(TAG, "can't not follow.");
                return;
            }

            if (followingType==0) {
                AKUtils.showToast("follow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            } else if (followingType==1) {
                AKUtils.showToast("unfollow "+resultObj.screenName+" successfully!", Toast.LENGTH_LONG);
            }
        }

    }
}
