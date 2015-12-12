package cn.archko.microblog.fragment.impl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Draft;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaDraftImpl extends AbsStatusImpl<Draft> {

    public static final String TAG="SinaDraftImpl";

    public SinaDraftImpl() {
    }

    @Override
    public SStatusData<Draft> loadData(Object... params) throws WeiboException {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "loadData.");
        }
        /*try {
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ArrayList<SendTask> objects=SqliteWrapper.queryAllTasks(App.getAppContext(), String.valueOf(currentUserId), -1);
            SStatusData<SendTask> sStatusData=new SStatusData<SendTask>();
            sStatusData.mStatusData=objects;
            return sStatusData;
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return null;
    }

    public Object[] queryData(Object... params) throws WeiboException {
        try {
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ArrayList<Draft> objects=SqliteWrapper.queryAllDraft(App.getAppContext(), String.valueOf(currentUserId));
            SStatusData<Draft> sStatusData=new SStatusData<Draft>();
            sStatusData.mStatusData=objects;
            return new Object[]{sStatusData, params};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveData(SStatusData<Draft> data) {
    }
}
