package cn.archko.microblog.fragment.impl;

import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaHotPostStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaAtMeStatusImpl";

    public SinaHotPostStatusImpl() {
        mAbsApi=new SinaStatusApi();
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        WeiboLog.d(TAG, "loadData.");
        SStatusData<Status> sStatusData=null;
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            ArrayList<Status> statuses=sWeiboApi2.getHotRepost(c, "repost_daily");
            sStatusData.mStatusData=statuses;
        }

        return sStatusData;
    }

    public Object[] queryData(Object... params) throws WeiboException {

        return null;
    }

    @Override
    public void saveData(SStatusData<Status> data) {

    }
}
