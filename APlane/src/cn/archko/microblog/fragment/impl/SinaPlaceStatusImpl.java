package cn.archko.microblog.fragment.impl;

import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaPlaceApi;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaPlaceStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG = "SinaPlaceStatusImpl";

    public SinaPlaceStatusImpl() {
        //mAbsApi=new SinaPlaceApi();
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        SStatusData<Status> sStatusData = null;
        SinaPlaceApi sWeiboApi2 = (SinaPlaceApi) mAbsApi;
        if (null == sWeiboApi2) {
            sStatusData = new SStatusData<Status>();
            sStatusData.errorCode = WeiboException.API_ERROR;
            sStatusData.errorMsg = App.getAppContext().getString(R.string.err_api_error);
        } else {
            try {
                Long sinceId = (Long) params[ 1 ];
                Long maxId = (Long) params[ 2 ];
                Integer c = (Integer) params[ 3 ];
                Integer p = (Integer) params[ 4 ];
                String type = (String) params[ 6 ];
                WeiboLog.d(TAG, "loadData." + type);

                if ("nearby_photos".equals(type)) {
                    sStatusData = sWeiboApi2.getNearbyPhotos(((App) App.getAppContext()).latitude,
                        ((App) App.getAppContext()).longitude, ((App) App.getAppContext()).range, c, p);
                } else {
                    sStatusData = sWeiboApi2.getPlaceFriendTimeline(sinceId, maxId, c, p, 0);
                }
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }

        return sStatusData;
    }

    public Object[] queryData(Object... params) throws WeiboException {
        /*try {
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ContentResolver resolver=App.getAppContext().getContentResolver();
            ArrayList<Status> objects=SqliteWrapper.queryAtStatuses(resolver, currentUserId);
            SStatusData<Status> sStatusData=new SStatusData<Status>();
            sStatusData.mStatusData=objects;
            return new Object[]{sStatusData, params};
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return null;
    }

    @Override
    public void saveData(SStatusData<Status> data) {

    }
}
