package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import cn.archko.microblog.fragment.impl.SinaUserFollowersImpl;
import cn.archko.microblog.ui.SkinFragmentActivity;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.sina.SinaUnreadApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 获取用户粉丝列表及每个粉丝的最新一条微博，返回结果按粉丝的关注时间倒序排列，最新关注的粉丝排在最前面。每次返回20个,
 * 通过cursor参数来取得多于20的粉丝。注意目前接口最多只返回5000个粉丝。
 * 需要添加移除粉丝功能
 * @author: archko 12-9-1
 */
public class UserFollowersGridFragment extends UserFriendsGridFragment {

    public static final String TAG="UserFollowersGridFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaUserFollowersImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaUserFollowersImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.userApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    //--------------------- 数据加载 ---------------------

    @Override
    public void refreshNewData(SStatusData<User> sStatusData, Boolean isRefresh) {
        ArrayList<User> list=sStatusData.mStatusData;
        if (mDataList.size()>0) {
            try {
                User first=list.get(0);
                User last=mDataList.get(mDataList.size()-1);

                if (first.id==last.id) {
                    list.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isRefresh) {
            int len=list.size();
            NotifyUtils.showToast("为您更新了"+len+"条最新信息！");
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

    //--------------------- 操作 ---------------------

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        if (isRefresh&&load) {
            clearHomeNotify();
        }
    }

    //--------------------- 清除未读消息操作 ---------------------

    /**
     * 清除主页的消息计数通知
     */
    private void clearHomeNotify() {
        if (mUserId!=currentUserId) {
            WeiboLog.d(TAG, "查看的用户不是当前用户,不刷新粉丝数.");
            return;
        }
        //int newFollower=mPrefs.getInt(Constants.PREF_SERVICE_FOLLOWER, 0);
        mPrefs.edit().remove(Constants.PREF_SERVICE_FOLLOWER).commit();
        try {
            SkinFragmentActivity parent=(SkinFragmentActivity) getActivity();
            parent.refreshSidebar();

            newOperationTask(new Object[]{Constants.REMIND_FOLLOWER}, null);
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
                NotifyUtils.showToast(sStatusData.errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
