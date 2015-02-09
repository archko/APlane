package cn.archko.microblog.fragment;

import android.os.Bundle;
import cn.archko.microblog.fragment.impl.SinaPublucStatusImpl;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 随便看看
 * @author: archko 11-11-17
 */
public class PublicFragment extends RecyclerViewFragment {

    public static final String TAG="PublicFragment";
    private static final int MAX_PAGE=4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaPublucStatusImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaPublucStatusImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    //--------------------- 数据加载 ---------------------
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        int count=mAdapter.getCount();
        if (count<1) {
            WeiboLog.w(TAG, "no other data.");
            return;
        }

        boolean isRefresh=false;
        if (count>=weibo_count*MAX_PAGE) {   //refresh list
            isRefresh=true;
        }
        Status st;
        st=(Status) mAdapter.getItem(mAdapter.getCount()-1);
        fetchData(-1, st.id, isRefresh, false);
    }
}
