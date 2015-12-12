package cn.archko.microblog.fragment.impl;

import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaPublucStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaPublucStatusImpl";

    public SinaPublucStatusImpl() {
        /*AbsApiImpl absApi=new SinaStatusApi();
        mAbsApi=absApi;*/
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "loadData.");
        }
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        SStatusData<Status> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            /*Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];*/
            Integer c=(Integer) params[3];
            //Integer p=(Integer) params[4];
            sStatusData=sWeiboApi2.getPublicTimeline(c);
        }

        return sStatusData;
    }

    @Override
    public void saveData(SStatusData<Status> data) {
    }
}
