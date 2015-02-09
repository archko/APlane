package cn.archko.microblog.fragment.place;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.RecyclerViewFragment;
import cn.archko.microblog.fragment.impl.SinaPlaceStatusImpl;
import cn.archko.microblog.location.BaiduLocation;
import cn.archko.microblog.location.Command;
import cn.archko.microblog.location.LocationCommand;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.view.PlaceItemView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.AKLocation;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 公共的位置微博列博Fragment。
 * @author: archko 12-9-12
 */
public class PlaceStatusGridFragment extends RecyclerViewFragment {

    public static final String TAG="PlaceStatusListFragment";

    /**
     * 数据是否加载成功,对于位置来说,使用百度定位,它会在20秒内再执行一次定位,所以要去除不必要的加载.
     */
    protected boolean isLoaded=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPlaceStatusImpl();
    }

    @Override
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=super._onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaPlaceStatusImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
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
        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshStarted();
        }
        final BaiduLocation location=new BaiduLocation();
        //下面这个应该放在EmployeeBaiduLocation里面处理的.
        location.setMyListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation==null) {
                    if (isResumed()) {
                        refreshAdapter(false, true);
                    }
                    return;
                }
                AKLocation akLocation=new AKLocation(bdLocation.getLongitude(), bdLocation.getLatitude());
                akLocation.mLocationTimestamp=System.currentTimeMillis();
                akLocation.addr=bdLocation.getAddrStr();
                ((App) App.getAppContext()).setLocation(akLocation);
                if (isResumed()) {
                    pullToRefreshData();
                }
            }
        });
        Command command=new LocationCommand(location);
        command.execute();
    }

    @Override
    protected SStatusData getData(Object... params) throws WeiboException {
        return null;
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        isLoaded=true;
    }

    /**
     * 下拉刷新数据
     */
    @Override
    protected void pullToRefreshData() {
        isRefreshing=true;
        AKLocation akLocation=((App) App.getAppContext()).getLocation(600000);
        if (akLocation==null||akLocation.latitude==0.0||akLocation.longitude==0.0) {
            WeiboLog.i(TAG, "pullToRefreshData.没有找到地点,需要重新定位.");
            startMap();
            isRefreshing=false;
            return;
        }

        //page=1;
        fetchData(-1, -1, true, false);
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     * 这是数据的入口,一切从此开始.
     */
    @Override
    protected void loadData() {
        AKLocation akLocation=((App) App.getAppContext()).getLocation(600000);
        if (akLocation==null||akLocation.latitude==0.0||akLocation.longitude==0.0) {
            WeiboLog.i(TAG, "loadData.没有找到地点,需要重新定位.");
            startMap();
            return;
        }

        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                fetchData(-1, -1, true, false);
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.d(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            Status st;
            st=(Status) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
        }
    }

    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        PlaceItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new PlaceItemView(getActivity(), mCacheDir, updateFlag, true);
        } else {
            itemView=(PlaceItemView) convertView;
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
        PlaceItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new PlaceItemView(getActivity(), mCacheDir, updateFlag, true);
        return itemView;
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    protected void deleteStatus() {
        WeiboLog.d(TAG, "not implemented.");
        if (selectedPos==-1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status=mDataList.get(selectedPos);
            if (null!=status) {
                String type="0";
                Long statusId=status.id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
