package cn.archko.microblog.fragment.impl;

import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.SinaStatusApi;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description:
 */
@Deprecated
public class SinaStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaStatusImpl";

    public SinaStatusImpl() {
        /*AbsApiImpl absApi=new SinaStatusApi();
        setApiImpl(absApi);*/
    }

    @Override
    public SStatusData<Status> loadData(Object... params) {

        return null;
    }

    @Override
    public void saveData(SStatusData<Status> data) {
    }
}
