package cn.archko.microblog.fragment.abs;

import android.os.Bundle;
import cn.archko.microblog.fragment.impl.AbsStatusImpl;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko Date: 13-1-28 Time: 下午6:42
 * @description:
 */
public abstract class AbsStatusAbstraction<T> extends AbstractBaseFragment {

    public AbsStatusImpl<T> mStatusImpl;

    /**
     * 初始化api.子类必须覆盖,否则无法执行
     */
    public abstract void initApi();

    public void onSelected() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null==mStatusImpl) {
            throw new IllegalArgumentException("需要设置数据获取实现!");
        }
        mStatusImpl.updateToken();
    }

    public void setStatusImpl(AbsStatusImpl<T> statusImpl) {
        mStatusImpl=statusImpl;
    }

    @Override
    public void postOauth(Object[] params) {
        super.postOauth(params);
        mStatusImpl.updateToken();
    }

    /**
     * 线程中的操作。
     *
     * Boolean isRefresh=(Boolean) params[0];是否刷新，因为如果是刷新数据，在获取到新数据后会清除原来的。
     * 如果不是刷新数据，会添加在原来的数据末尾。
     * params[i]接着跟着参数。
     *
     * @param params
     * @return
     */
    public Object[] baseBackgroundOperation(Object... objects) {
        try {
            WeiboLog.d(TAG, "baseBackgroundOperation:"+objects);
            SStatusData<T> sStatusData=(SStatusData<T>) getData(objects);

            saveData(sStatusData);
            return new Object[]{sStatusData, objects};
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected void queryData() {
    }

    protected abstract SStatusData<T> getData(Object... params) throws WeiboException;

    protected void saveData(SStatusData<T> list) {
        mStatusImpl.saveData(list);
    }
}
