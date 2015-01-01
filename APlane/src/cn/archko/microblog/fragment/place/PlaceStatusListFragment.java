package cn.archko.microblog.fragment.place;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.RecyclerViewFragment;
import cn.archko.microblog.fragment.impl.SinaPlaceStatusImpl;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.PlaceItemView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.Date;

/**
 * @version 1.00.00
 * @description: 公共的位置微博列博Fragment。
 * @author: archko 12-9-12
 */
public class PlaceStatusListFragment extends RecyclerViewFragment {

    public static final String TAG = "PlaceStatusListFragment";

    /*protected double longitude=0.0;
    protected double latitude=0.0;
    protected int range=10000;*/
    /**
     * 数据是否加载成功,对于位置来说,使用百度定位,它会在20秒内再执行一次定位,所以要去除不必要的加载.
     */
    protected boolean isLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPlaceStatusImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl = new SinaPlaceStatusImpl();

        AbsApiFactory absApiFactory = null;//new SinaApiFactory();
        try {
            absApiFactory = ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
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
        mLocClient.start();
        mIsStart = true;

        mSwipeLayout.setRefreshing(true);
        if (mRefreshListener != null) {
            mRefreshListener.onRefreshStarted();
        }
    }

    protected void stopMap() {
        WeiboLog.d(TAG, "stopMap.");
        mLocClient.stop();
        mIsStart = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        WeiboLog.d(TAG, "onPause:" + this);
        try {
            mLocClient.unRegisterLocationListener(myListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WeiboLog.d(TAG, "onResume:"+this);
    }

    @Override
    public void _onActivityCreated(Bundle savedInstanceState) {
        initLocation();
        setLocationOption();

        /*if (!isLoaded) {
            startMap();
        }*/

        super._onActivityCreated(savedInstanceState);
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
        if ((((App) App.getAppContext()).mLocationTimestamp - System.currentTimeMillis()) > 600000 ||
            (((App) App.getAppContext()).latitude == 0.0 || ((App) App.getAppContext()).longitude == 0.0)) {
            WeiboLog.i(TAG, "pullToRefreshData.没有找到地点,需要重新定位.");
            startMap();
            isRefreshing = false;
            return;
        }

        //page=1;
        fetchData(- 1, - 1, true, false);
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     * 这是数据的入口,一切从此开始.
     */
    @Override
    protected void loadData() {
        if ((((App) App.getAppContext()).mLocationTimestamp - System.currentTimeMillis()) > 600000 ||
            (((App) App.getAppContext()).latitude == 0.0 || ((App) App.getAppContext()).longitude == 0.0)) {
            WeiboLog.i(TAG, "loadData.没有找到地点,需要重新定位.");
            startMap();
            return;
        }

        if (mDataList != null && mDataList.size() > 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (! isLoading) {
                fetchData(- 1, - 1, true, false);
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.d(TAG, "fetchMore.lastItem:" + lastItem + " selectedPos:" + selectedPos);
        if (mAdapter.getCount() > 0) {
            Status st;
            st = (Status) mAdapter.getItem(mAdapter.getCount() - 1);
            fetchData(- 1, st.id, false, false);
        }
    }

    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        PlaceItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new PlaceItemView(getActivity(), mCacheDir, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView=(PlaceItemView) convertView;
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

    public View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        PlaceItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new PlaceItemView(getActivity(), mCacheDir, updateFlag, true, showLargeBitmap, showBitmap);
        return itemView;
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(View achor) {
        super.itemClick(achor);
    }

    /**
     * 创建收藏.
     */
    protected void createFavorite() {
        WeiboLog.i(TAG, "selectedPos:" + selectedPos);
        if (selectedPos == - 1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);
            if (null != status) {
                String type = "0";
                Long statusId = status.id;
                /*OperationTask task=new OperationTask();
                task.execute(new Object[]{type, statusId});*/
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
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    protected void deleteStatus() {
        WeiboLog.d(TAG, "not implemented.");
        if (selectedPos == - 1) {
            NotifyUtils.showToast("您需要先选中一个项!");
            return;
        }

        try {
            Status status = mDataList.get(selectedPos);
            if (null != status) {
                String type = "0";
                Long statusId = status.id;
            }
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
            task.createAt = new Date().getTime();
            taskService.putExtra("send_task", task);
            getActivity().startService(taskService);
            NotifyUtils.showToast("转发任务添加到队列服务中了。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- geo ---------------------
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private boolean mIsStart;

    /**
     * 获取地理位置，由地图api获取
     */
    private void initLocation() {
        mLocClient = new LocationClient(App.getAppContext());
        mLocClient.registerLocationListener(myListener);
    }

    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        //option.setOpenGps();                //打开gps
        //option.setCoorType("");        //设置坐标类型
        //option.setAddrType("all");        //设置地址信息，仅设置为“all”时有地址信息，默认无地址信息
        option.setScanSpan(1);    //设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
        mLocClient.setLocOption(option);
    }

    /**
     * 监听函数，又新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }

            StringBuilder sb = new StringBuilder(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\n省：");
                sb.append(location.getProvince());
                sb.append("\n市：");
                sb.append(location.getCity());
                sb.append("\n区/县：");
                sb.append(location.getDistrict());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }
            sb.append("\nsdk version : ");
            sb.append(mLocClient.getVersion());
            //logMsg(sb.toString());
            WeiboLog.v(TAG, " sb:" + sb.toString());

            ((App) App.getAppContext()).longitude = location.getLongitude();
            ((App) App.getAppContext()).latitude = location.getLatitude();
            ((App) App.getAppContext()).mLocationTimestamp = System.currentTimeMillis();

            stopMap();
            WeiboLog.v(TAG, " geo:" + sb.toString());
            pullToRefreshData();
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
            StringBuffer sb = new StringBuffer(256);
            sb.append("Poi time : ");
            sb.append(poiLocation.getTime());
            sb.append("\nerror code : ");
            sb.append(poiLocation.getLocType());
            sb.append("\nlatitude : ");
            sb.append(poiLocation.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(poiLocation.getLongitude());
            sb.append("\nradius : ");
            sb.append(poiLocation.getRadius());
            if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(poiLocation.getAddrStr());
            }
            //logMsg(sb.toString());
        }
    }
}
