package cn.archko.microblog.fragment.place;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.UserListFragment;
import cn.archko.microblog.fragment.impl.SinaPlaceUserImpl;
import cn.archko.microblog.location.BaiduLocation;
import cn.archko.microblog.location.Command;
import cn.archko.microblog.location.LocationCommand;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.view.UserItemView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.AKLocation;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 公共的用户Fragment，默认实现了所有的QuickAction项的功能与加载更多的内容。
 * getStatuses需要子类来实现，暂时不设为抽象类。
 * @author: archko 12-9-12
 */
public class PlaceUserListFragment extends UserListFragment {

    public static final String TAG = "PlaceUserListFragment";

    /**
     * 数据是否加载成功,对于位置来说,使用百度定位,它会在20秒内再执行一次定位,所以要去除不必要的加载.
     */
    protected boolean isLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPlaceUserImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl = new SinaPlaceUserImpl();

        AbsApiFactory absApiFactory = null;//new SinaApiFactory();
        try {
            absApiFactory = ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.placeApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    /**
     * 启动定位
     */
    protected void startMap() {
        WeiboLog.d(TAG, "startMap.");

        mSwipeLayout.setRefreshing(true);
        if (mRefreshListener != null) {
            mRefreshListener.onRefreshStarted();
        }
        final BaiduLocation location = new BaiduLocation();
        //下面这个应该放在EmployeeBaiduLocation里面处理的.
        location.setMyListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null) {
                    if (isResumed()) {
                        refreshAdapter(false, true);
                    }
                    return;
                }
                AKLocation akLocation = new AKLocation(bdLocation.getLongitude(), bdLocation.getLatitude());
                akLocation.mLocationTimestamp=System.currentTimeMillis();
                akLocation.addr=bdLocation.getAddrStr();
                ((App)App.getAppContext()).setLocation(akLocation);
                if (isResumed()) {
                    pullToRefreshData();
                }
            }
        });
        Command command = new LocationCommand(location);
        command.execute();
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        isLoaded = true;
    }

    /**
     * 下拉刷新数据
     */
    @Override
    protected void pullToRefreshData() {
        isRefreshing = true;
        AKLocation akLocation = ((App) App.getAppContext()).getLocation(600000);
        if (akLocation == null || akLocation.latitude == 0.0 || akLocation.longitude == 0.0) {
            WeiboLog.i(TAG, "pullToRefreshData.没有找到地点,需要重新定位.");
            startMap();
            isRefreshing=false;
            return;
        }

        //page=1;
        fetchData(- 1, - 1, true, false);
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    @Override
    protected void loadData() {
        AKLocation akLocation = ((App) App.getAppContext()).getLocation(600000);
        if (akLocation == null || akLocation.latitude == 0.0 || akLocation.longitude == 0.0) {
            WeiboLog.i(TAG, "loadData.没有找到地点,需要重新定位.");
            startMap();
            return;
        }

        if (mDataList != null && mDataList.size() > 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (! isLoading) {
                fetchData(- 1, - 1, true, false);
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.d(TAG, "fetchMore.lastItem:" + lastItem + " selectedPos:" + selectedPos);
        if (mAdapter.getCount() > 0) {
            User st;
            st = (User) mAdapter.getItem(mAdapter.getCount() - 1);
            fetchData(- 1, st.id, false, false);
        }
    }

    /**
     * 位置信息数量限制在50个内
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i(TAG, "sinceId:" + sinceId + ", maxId:" + maxId + ", isRefresh:" + isRefresh + ", isHomeStore:" + isHomeStore);
        if (! App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count = weibo_count;
        if (! isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。
            //count++;
        } else {
            //page=1;
        }
        if (count > 50) {
            count = 50;
        }

        if (! isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        View convertView=holder.baseItemView;
        UserItemView itemView = null;
        User user = mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView == null) {
            itemView = new UserItemView(getActivity(), mCacheDir, updateFlag);
        } else {
            itemView = (UserItemView) convertView;
        }
        itemView.update(user, updateFlag, true);
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

    @Override
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

    //TODO需要处理token过期的状况。

    /**
     * 处理关系，关注或取消关注，不作粉丝移除处理。
     */
    protected void followUser() {
        NotifyUtils.showToast("not implemented.");
    }

    protected void atUser() {
        try {
            User user = mDataList.get(selectedPos);
            Intent intent = new Intent(getActivity(), NewStatusActivity.class);
            intent.putExtra("at_some", user.screenName);
            intent.setAction(Constants.INTENT_NEW_BLOG);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
