package cn.archko.microblog.fragment.place;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.UserListFragment;
import cn.archko.microblog.fragment.impl.SinaPlaceUserImpl;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.AKUtils;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.UserItemView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 公共的用户Fragment，默认实现了所有的QuickAction项的功能与加载更多的内容。
 * getStatuses需要子类来实现，暂时不设为抽象类。
 * @author: archko 12-9-12
 */
public class PlaceUserListFragment extends UserListFragment {

    public static final String TAG="PlaceUserListFragment";

    /*protected double longitude=0.0;
    protected double latitude=0.0;
    protected int range=10000;*/
    /**
     * 数据是否加载成功,对于位置来说,使用百度定位,它会在20秒内再执行一次定位,所以要去除不必要的加载.
     */
    protected boolean isLoaded=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPlaceUserImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaPlaceUserImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.placeApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            AKUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    /**
     * 启动定位
     */
    protected void startMap() {
        WeiboLog.d(TAG, "startMap.");
        mLocClient.start();
        mIsStart=true;

        mPullRefreshListView.setRefreshing();
        if (mRefreshListener!=null) {
            mRefreshListener.onRefreshStarted();
        }
    }

    protected void stopMap() {
        WeiboLog.d(TAG, "stopMap.");
        mLocClient.stop();
        mIsStart=false;
    }

    @Override
    public void onPause() {
        super.onPause();
        WeiboLog.d(TAG, "onPause:"+this);
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
        isLoaded=true;
    }

    /**
     * 下拉刷新数据
     */
    @Override
    protected void pullToRefreshData() {
        isRefreshing=true;
        if ((((App) App.getAppContext()).mLocationTimestamp-System.currentTimeMillis())>600000||
            (((App) App.getAppContext()).latitude==0.0||((App) App.getAppContext()).longitude==0.0)) {
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
     */
    @Override
    protected void loadData() {
        if ((((App) App.getAppContext()).mLocationTimestamp-System.currentTimeMillis())>600000||
            (((App) App.getAppContext()).latitude==0.0||((App) App.getAppContext()).longitude==0.0)) {
            WeiboLog.i(TAG, "loadData.没有找到地点,需要重新定位.");
            startMap();
            return;
        }

        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                fetchData(-1, -1, true, false);
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.d(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            User st;
            st=(User) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
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
        WeiboLog.i(TAG, "sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
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
            //count++;
        } else {
            //page=1;
        }
        if (count>50) {
            count=50;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //WeiboLog.d(TAG, "getView.pos:"+position+" getCount():"+getCount()+" lastItem:");

        UserItemView itemView=null;
        User user=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new UserItemView(getActivity(), mListView, mCacheDir, user, updateFlag);
        } else {
            itemView=(UserItemView) convertView;
        }
        itemView.update(user, updateFlag, true);

        return itemView;
    }

    //--------------------- 用户操作 ---------------------
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

    /**
     * 处理关系，关注或取消关注，不作粉丝移除处理。
     */
    protected void followUser() {
        AKUtils.showToast("not implemented.");
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

    //--------------------- geo ---------------------
    private LocationClient mLocClient;
    public MyLocationListenner myListener=new MyLocationListenner();
    private boolean mIsStart;

    /**
     * 获取地理位置，由地图api获取
     */
    private void initLocation() {
        mLocClient=new LocationClient(App.getAppContext());
        mLocClient.registerLocationListener(myListener);
    }

    private void setLocationOption() {
        LocationClientOption option=new LocationClientOption();
        //option.setOpenGps();                //打开gps
        //option.setCoorType("");        //设置坐标类型
        //option.setAddrType("all");        //设置地址信息，仅设置为“all”时有地址信息，默认无地址信息
        option.setScanSpan(1);    //设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
        option.setServiceName("com.baidu.location.service_v2.9");
        option.setPoiNumber(10);
        option.disableCache(true);
        mLocClient.setLocOption(option);
    }

    /**
     * 监听函数，又新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location==null) {
                return;
            }

            StringBuilder sb=new StringBuilder(256);
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
            if (location.getLocType()==BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType()==BDLocation.TypeNetWorkLocation) {
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
            WeiboLog.v(TAG, " sb:"+sb.toString());

            ((App) App.getAppContext()).longitude=location.getLongitude();
            ((App) App.getAppContext()).latitude=location.getLatitude();
            ((App) App.getAppContext()).mLocationTimestamp=System.currentTimeMillis();

            stopMap();
            WeiboLog.v(TAG, " geo:"+sb.toString());
            pullToRefreshData();
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation==null) {
                return;
            }
            StringBuffer sb=new StringBuffer(256);
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
            if (poiLocation.getLocType()==BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(poiLocation.getAddrStr());
            }
            if (poiLocation.hasPoi()) {
                sb.append("\nPoi:");
                sb.append(poiLocation.getPoi());
            } else {
                sb.append("noPoi information");
            }
            //logMsg(sb.toString());
        }
    }
}
