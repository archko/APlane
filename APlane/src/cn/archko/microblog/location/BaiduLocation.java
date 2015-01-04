package cn.archko.microblog.location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.me.microblog.App;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko 2014/12/4 :16:32
 */
public class BaiduLocation extends AbsReceiver {

    //private static BaiduLocation mInstance;
    protected LocationClient mLocClient;
    public BDLocationListener myListener;
    MyLocationListenner myLocationListenner = new MyLocationListenner();

    public void setMyListener(BDLocationListener myListener) {
        this.myListener = myListener;
    }

    public BDLocationListener getMyListener() {
        return myListener;
    }

    /*public static BaiduLocation getInstance() {
        if (null == mInstance) {
            mInstance = new BaiduLocation();
        }
        return mInstance;
    }*/

    public BaiduLocation() {
        mLocClient = new LocationClient(App.getAppContext());
    }

    @Override
    public void action(Command command) {
        setCommand(command);
        LocationCommand locationCommand = (LocationCommand) command;
        mLocClient.registerLocationListener(myLocationListenner);
        LocationClientOption option = mLocClient.getLocOption();
        if (null == option) {
            option = new LocationClientOption();
        }
        option.setOpenGps(locationCommand.isOpenGps());// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(locationCommand.getScanSpan());
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            receiveLocation(location);
        }
    }

    public void receiveLocation(BDLocation location) {
        LocationCommand command = (LocationCommand) getCommand();
        if (null != command && command.isOnlyOnce()) {
            mLocClient.unRegisterLocationListener(myLocationListenner);
            mLocClient.stop();
        }
        if (null != myListener) {
            myListener.onReceiveLocation(location);
            return;
        }

        if (location == null) {
            WeiboLog.d("", "location is null.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("lat:").append(location.getLatitude()).append(" lng:").append(location.getLongitude())
            .append(" city:").append(location.getCity()).append(" cityCode:").append(location.getCityCode())
            .append(" province:").append(location.getProvince())
            .append(" district:").append(location.getDistrict())
            .append(" speed:").append(location.getSpeed())
            .append(" street:").append(location.getStreet())
            .append(" time:").append(location.getTime())
            .append(" addr:").append(location.getAddrStr());
        WeiboLog.d("location", sb.toString());
    }
}
