package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaHomeStatusImpl;
import cn.archko.microblog.service.WeiboService;
import cn.archko.microblog.ui.PrefsActivity;
import cn.archko.microblog.ui.SkinFragmentActivity;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.factory.SinaApiFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.bean.Status;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 主页, 显示登录用户与其关注对象所发的微博.
 * @author: archko 11-11-17
 */
public class HomeFragment extends StatusListFragment {

    public static final String TAG="HomeFragment";
    /**
     * 是否是要刷新，不刷新时获取下一页数据不停止服务，暂时没有解决问题。
     */
    boolean isRefreshData=false;
    /**
     * 是否增量更新.增量的策略由获取数据中,新微博的数量 决定的.
     */
    boolean isUpdateIncrement=true;
    Group mGroup;
    /**
     * 分组更新了，需要删除所有本地数据，而不是像之前那样还保留旧数据。
     */
    boolean isGroupUpdated=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaHomeStatusImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            AKUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WeiboLog.v(TAG, "onResume:"+this);

        isUpdateIncrement=mPrefs.getBoolean(PrefsActivity.PREF_UPDATE_INCREMENT, true);
    }

    /**
     * clear list's datas
     */
    @Override
    public void clear() {
        super.clear();
        /*App app=(App) App.getAppContext();
        app.mDownloadPool.cleanAllQuery();*/
    }

    /**
     * 下拉刷新数据
     */
    protected void pullToRefreshData() {
        isRefreshing=true;
        //page=1;
        fetchData(-1, -1, true, true);
        isRefreshData=true;
    }

    /**
     * 获取数据
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+
            ", isHomeStore:"+isHomeStore+" isGroupUpdated:"+isGroupUpdated);
        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error);
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count=weibo_count;
        if (!isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。TODO
            //count++;
        } else {
            if (isUpdateIncrement&&(null==mGroup||mGroup.id.equals(Constants.TAB_ID_HOME))) {
                //page=1;
                int status=mPrefs.getInt(Constants.PREF_SERVICE_STATUS, 0);
                WeiboLog.d(TAG, "新消息数:"+status);
                if (status>0) {
                    if (status>100) {
                        status=100;
                    }

                    count=status;
                }
            }
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    @Override
    protected void loadData() {
        WeiboLog.d(TAG, "home.loaddata:");
        if (mDataList!=null&&mDataList.size()>0) {
            //setListShown(true);

            //mProgressContainer.setVisibility(View.GONE);
            //mListContainer.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                loadLocalData();
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void notifyChanged() {
        WeiboLog.d(TAG, "prefs changed.");
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 添加这个方法是为了子类可以自动处理新的数据应该怎样加入列表中,暂时只有在主页使用到.
     *
     * @param sStatusData 列表数据
     * @param isRefresh   是否是刷新列表
     */
    @Override
    public void refreshNewData(SStatusData<Status> sStatusData, Boolean isRefresh) {
        ArrayList<Status> list=sStatusData.mStatusData;
        if (mDataList.size()>0) {
            try {
                Status first=list.get(0);
                Status last=mDataList.get(mDataList.size()-1);

                if (first.id==last.id) {
                    list.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isRefresh) {
            int len=list.size();
            AKUtils.showToast("为您更新了"+len+"条最新信息！", Toast.LENGTH_LONG);
            //mListView.clearChoices();
            if (list.size()<weibo_count) {
                mDataList.addAll(0, list);
            } else {
                mDataList.clear();
                mDataList.addAll(list);
            }
            WeiboLog.i(TAG, "notify data changed."+mDataList.size()+" isRefresh:"+isRefresh);
        } else {
            mDataList.addAll(list);
        }
    }

    /**
     * 刷新适配器
     *
     * @param load      是否加载数据成功
     * @param isRefresh 是否要刷新列表选中的位置
     */
    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter isGroupUpdated:"+isGroupUpdated);
        super.refreshAdapter(load, isRefresh);
        isGroupUpdated=false;
        if (isRefresh&&load) {
            mListView.setSelection(1);
            clearHomeNotify();
        }
        isRefreshData=false;
        //notifyService(false);
    }

    /**
     * 清除主页的消息计数通知
     */
    private void clearHomeNotify() {
        mPrefs.edit().remove(Constants.PREF_SERVICE_STATUS).commit();
        try {
            SkinFragmentActivity parent=(SkinFragmentActivity) getActivity();
            parent.refreshSidebar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存中查询数据.
     */
    void loadLocalData() {
        if (!isLoading) {
            Object[] params=new Object[]{false, currentUserId};
            newTaskNoNet(params, null);
        }
    }

    /**
     * 发出通知，让服务不需要检索新的微博。
     *
     * @param isStop 是否停止，在获取新微博时停止服务，结束后再打开服务
     */
    @Deprecated
    private void notifyService(boolean isStop) {
        WeiboLog.d("notifyService.isStop:"+isStop+" isRefreshData:"+isRefreshData);
        /*Intent i = new Intent();
        i.setAction(Constants.SERVICE_NOTIFY_RETASK);
        getActivity().sendBroadcast(i);*/
        boolean chk_new_status=PreferenceManager.getDefaultSharedPreferences(getActivity())
            .getBoolean(PrefsActivity.PREF_AUTO_CHK_NEW_STATUS, true);
        if (!chk_new_status) {
            WeiboLog.d(TAG, "no chk_new_status.");
            return;
        }

        Intent intent=new Intent(getActivity(), WeiboService.class);
        intent.setAction(WeiboService.REFRESH);
        if (isStop) {
            getActivity().stopService(intent);
        } else {
            getActivity().startService(intent);
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        isRefreshData=false;
    }

    //--------------------- 分组操作 ---------------------
    public void updateGroupTimeline(Group group) {
        WeiboLog.d(TAG, "updateGroupTimeline:"+group+" old:"+mGroup);
        if (null!=group) {
            if (null==mGroup||!mGroup.id.equals(group.id)) {
                if (null!=mGroup) {
                    isGroupUpdated=true;
                    mGroup=group;
                    isRefreshing=true;

                    SinaHomeStatusImpl homeStatusImpl=(SinaHomeStatusImpl) mStatusImpl;
                    homeStatusImpl.updateGroup(group, isGroupUpdated);
                    fetchData(-1, -1, true, true);
                }
            }
        }
        mGroup=group;
    }

    //--------------------- 微博操作 ---------------------

    /*@Override
    protected Object[] baseQueryBackgroundOperation(Object... params) throws WeiboException {
        return mStatusImpl.queryData(params);
    }*/

    /*@Override
    protected void saveData(SStatusData<Status> statusData) {

    }*/
}
