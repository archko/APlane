package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.DirectMessage;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.sina.SinaDMApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaDMImpl extends AbsStatusImpl<DirectMessage> {

    public static final String TAG="SinaDMImpl";

    public SinaDMImpl() {
        AbsApiImpl absApi=new SinaDMApi();
        mAbsApi=absApi;
    }

    @Override
    public SStatusData<DirectMessage> loadData(Object... params) throws WeiboException {
        SStatusData<DirectMessage> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        SinaDMApi sWeiboApi2=(SinaDMApi) mAbsApi;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<DirectMessage>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            //Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            int c=10;   //私信不像其它，不用太多条。
            WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", count:"+c+", page:"+p);
            sStatusData=sWeiboApi2.getDirectMessages(sinceId, maxId, c, p);
        }

        return sStatusData;
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        ArrayList<DirectMessage> datas=SqliteWrapper.queryDirectMsgs(resolver, currentUserId);
        SStatusData<DirectMessage> sStatusData=new SStatusData<DirectMessage>();
        sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }

    @Override
    public void saveData(SStatusData<DirectMessage> data) {
        try {
            ArrayList<DirectMessage> newList=data.mStatusData;
            if (null!=newList&&newList.size()>0) {
                SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
                long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
                SqliteWrapper.saveDirectMsgs(App.getAppContext(), newList, currentUserId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
