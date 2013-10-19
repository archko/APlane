package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import cn.archko.microblog.R;
import cn.archko.microblog.action.AtMeAction;
import cn.archko.microblog.fragment.impl.SinaAtMeStatusImpl;
import cn.archko.microblog.ui.SkinFragmentActivity;
import com.me.microblog.App;
import com.me.microblog.action.ActionResult;
import com.me.microblog.action.AsyncActionTask;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.SinaUnreadApi;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.SinaApiFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-11-17
 */
public class AtmeFragment extends StatusListFragment {

    public static final String TAG="AtmeFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaAtMeStatusImpl();

        AbsApiFactory absApiFactory=new SinaApiFactory();
        mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
    }

    /**
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 这里作为数据刷新的标志，对于下拉刷新的，就清除原来的数据，获取固定的条数，在进入时，这个值为false
     *                    处理方式是先得到当前的未读消息数，然后再获取指定的数目。需要注意这个参数不再传入下一个调用。
     */
    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error);
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count=weibo_count;
        /*if (isHomeStore) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。TODO
            //count++;
        } else {*/
        //page=1;
        int status=mPrefs.getInt(Constants.PREF_SERVICE_AT, 0);
        WeiboLog.d(TAG, "新提及我的微博数:"+status);
        if (status>0) {
            if (status>100) {
                status=100;
            }

            count=status;
        }
        //}

        if (!isLoading) {
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    protected void loadData() {
        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                //fetchData(-1, -1, true);
                loadLocalData();
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
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
     * 下拉刷新数据
     */
    protected void pullToRefreshData() {
        isRefreshing=true;
        //page=1;
        fetchData(-1, -1, true, true);
    }

    @Override
    public void refreshNewData(SStatusData<Status> sStatusData, Boolean isRefresh) {
        //TODO 还需要处理获取更多的数据
        ArrayList<Status> list=sStatusData.mStatusData;
        /*if (mDataList.size()>0) {
            try {
                Status first=list.get(0);
                Status last=mDataList.get(mDataList.size()-1);

                if (first.id==last.id) {
                    list.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        if (isRefresh) {
            int len=list.size();
            AKUtils.showToast("为您更新了"+len+"条最新信息！");

            /*if (len>=weibo_count-1) {
                mDataList.clear();
                mDataList.addAll(list);
            } else {
                mDataList.addAll(0, list);
            }*/
            mDataList.clear();
            mDataList.addAll(list);
            WeiboLog.i(TAG, "notify data changed."+mDataList.size()+" isRefresh:"+isRefresh);
        } else {
            mDataList.addAll(list);
        }
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        if (isRefresh&&load) {
            clearHomeNotify();
        }
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_quick_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_ORITEXT, index++, R.string.opb_origin_text);
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPOST, index++, R.string.opb_repost);
        menuBuilder.getMenu().add(0, Constants.OP_ID_FAVORITE, index++, R.string.opb_favorite);
        //menuBuilder.add(0, Constants.OP_ID_SHIELD_ONE, index++, R.string.opb_shield_one);
        menuBuilder.getMenu().add(0, Constants.OP_ID_SHIELD_ALL, index++, R.string.opb_shield_all);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_FAVORITE: {
                createFavorite();
                break;
            }
            case Constants.OP_ID_REPOST: {
                repostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
            case Constants.OP_ID_ORITEXT: {
                viewOriginalStatus(null);
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                viewStatusUser();
                break;
            }
            /*case Constants.OP_ID_SHIELD_ONE: {
                shield(0);
                break;
            }*/
            case Constants.OP_ID_SHIELD_ALL: {
                shield(1);
                break;
            }
        }
        return false;
    }

    /**
     * 0：仅屏蔽当前@提到我的微博；1：屏蔽当前@提到我的微博，以及后续对其转发而引起的@提到我的微博。默认1。
     *
     * @param follow_up
     */
    private void shield(int follow_up) {
        AtMeAction action=new AtMeAction();
        long id=mDataList.get(selectedPos).id;
        AsyncActionTask task=new AsyncActionTask(App.getAppContext(), action);
        task.execute(id, follow_up, mStatusHandler);
    }

    Handler mStatusHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (!isResumed()) {
                WeiboLog.w(TAG, "已经结束了Fragment，不需要通知消息");
                return;
            }

            switch (msg.what) {
                case ActionResult.ACTION_SUCESS: {
                    AKUtils.showToast(R.string.text_shield_suc);
                    break;
                }

                case ActionResult.ACTION_FALL:
                    AKUtils.showToast(R.string.text_shield_failed);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //--------------------- 清除未读消息操作 ---------------------

    /**
     * 清除主页的消息计数通知
     */
    private void clearHomeNotify() {
        //int newAt=mPrefs.getInt(Constants.PREF_SERVICE_AT, 0);
        mPrefs.edit().remove(Constants.PREF_SERVICE_AT).commit();
        try {
            SkinFragmentActivity parent=(SkinFragmentActivity) getActivity();
            parent.refreshSidebar();

            newOperationTask(new Object[]{Constants.REMIND_MENTION_STATUS}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程中的操作。
     *
     * @param params
     * @return
     */
    protected Object[] baseBackgroundOperation2(Object... params) {
        try {
            String type=(String) params[0];
            SStatusData sStatusData=new SStatusData();
            //String rs=((SWeiboApi2) App.getMicroBlog(App.getAppContext())).setUnread(type);
            SinaUnreadApi sinaUnreadApi=new SinaUnreadApi();
            sinaUnreadApi.updateToken();
            String rs=sinaUnreadApi.setUnread(type);
            sStatusData.errorMsg=rs;
            return new Object[]{sStatusData};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void basePostOperation2(Object[] resultObj) {
        try {
            SStatusData sStatusData=(SStatusData) resultObj[0];
            WeiboLog.i(TAG, TAG+sStatusData);
            if (null==sStatusData) {
                return;
            }

            if (sStatusData.errorCode>0&&!TextUtils.isEmpty(sStatusData.errorMsg)) {
                AKUtils.showToast(sStatusData.errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
