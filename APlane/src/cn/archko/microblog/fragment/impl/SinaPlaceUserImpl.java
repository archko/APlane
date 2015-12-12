package cn.archko.microblog.fragment.impl;

import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.AKLocation;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaPlaceApi;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 我的评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaPlaceUserImpl extends AbsStatusImpl<User> {

    public static final String TAG="SinaPlaceUserImpl";

    public SinaPlaceUserImpl() {
        //mAbsApi=new SinaPlaceApi();
    }

    @Override
    public SStatusData<User> loadData(Object... params) throws WeiboException {
        SStatusData<User> sStatusData=null;
        SinaPlaceApi sWeiboApi2=(SinaPlaceApi) mAbsApi;
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<User>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            try {
                Long sinceId=(Long) params[1];
                Long maxId=(Long) params[2];
                Integer c=(Integer) params[3];
                Integer p=(Integer) params[4];
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "loadData."+c);
                }

                AKLocation akLocation=((App) App.getAppContext()).getLocation();
                sStatusData=sWeiboApi2.getPlacesNearbyUsers(akLocation.latitude,
                    akLocation.longitude, akLocation.range, c, p);
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }

        return sStatusData;
    }

    @Override
    public void saveData(SStatusData<User> statusData) {

    }
}
