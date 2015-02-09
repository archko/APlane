package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 主页数据获取实现，有查询与网络数据
 */
public class SinaUserStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaUserStatusImpl";

    public SinaUserStatusImpl() {
        /*AbsApiImpl absApi=new SinaStatusApi();
        mAbsApi=absApi;*/
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        WeiboLog.d(TAG, "loadData.");
        SStatusData<Status> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        sWeiboApi2.updateToken();
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            Long userId=(Long) params[5];
            //TODO 也可以直接使用用户昵称
            sStatusData=sWeiboApi2.getUserTimeLine(userId, sinceId, maxId, c, p, -1);
        }

        return sStatusData;
    }

    @Override
    public void saveData(SStatusData<Status> data) {
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        ArrayList<Status> datas=SqliteWrapper.queryStatuses(resolver, currentUserId);
        SStatusData<Status> sStatusData=new SStatusData<Status>();
        sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }
}
