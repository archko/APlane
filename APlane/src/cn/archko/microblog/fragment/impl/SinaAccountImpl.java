package cn.archko.microblog.fragment.impl;

import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaAccountImpl extends AbsStatusImpl<OauthBean> {

    public static final String TAG="SinaAccountImpl";

    public SinaAccountImpl() {
    }

    @Override
    public SStatusData<OauthBean> loadData(Object... params) throws WeiboException {
        WeiboLog.v(TAG, "loadData.");
        /*try {
            ArrayList<OauthBean> objects=SqliteWrapper.queryAccounts(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA);
            SStatusData<OauthBean> sStatusData=new SStatusData<OauthBean>();
            sStatusData.mStatusData=objects;
            return sStatusData;
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return null;
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        try {
            ArrayList<OauthBean> objects=SqliteWrapper.queryAccounts(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA);
            SStatusData<OauthBean> sStatusData=new SStatusData<OauthBean>();
            sStatusData.mStatusData=objects;
            return new Object[]{sStatusData, params};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveData(SStatusData<OauthBean> data) {
    }
}
