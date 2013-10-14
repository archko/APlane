package cn.archko.microblog.fragment.impl;

import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;

/**
 * @author: archko Date: 13-1-28 Time: 下午6:39
 * @description: 抽象数据获取，通过没事的api获取。包含查询，保存，网络数据获取
 */
public abstract class AbsStatusImpl<T> {

    AbsApiImpl mAbsApi;

    public void setApiImpl(AbsApiImpl apiImpl) {
        this.mAbsApi=apiImpl;
    }

    public void updateToken() {
        if (null!=mAbsApi) {
            mAbsApi.updateToken();
        }
    }

    public abstract SStatusData<T> loadData(Object... params) throws WeiboException;

    public Object[] queryData(Object... params) throws WeiboException {
        return null;
    }

    public abstract void saveData(SStatusData<T> data);
}
