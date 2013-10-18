package cn.archko.microblog.fragment;

import android.os.Bundle;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaMyCommentImpl;
import cn.archko.microblog.ui.SkinFragmentActivity;
import com.me.microblog.App;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.SinaUnreadApi;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.SinaApiFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

/**
 * @version 1.00.00  用户的评论，发出，收到，与合并的
 *          由于两种数据的合并，导致刷新时出现问题，现在先更新一种，然后再更新另一种。一次清除一个标志。
 * @description: 现在是我收到的评论
 * @author: archko 11-11-17
 */
public class CommentsFragment extends AtMeCommentsFragment {

    public static final String TAG="CommentsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboLog.v(TAG, "onCreate:");
        //mStatusImpl=new SinaMyCommentImpl();
    }

    public void initApi() {
        mStatusImpl=new SinaMyCommentImpl();

        AbsApiFactory absApiFactory=new SinaApiFactory();
        mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.commentApiFactory());
    }

    //--------------------- 微博操作 ---------------------

    /**
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 这里作为数据刷新的标志，对于下拉刷新的，就清除原来的数据，获取固定的条数，在进入时，这个值为false
     *                    处理方式是先得到当前的未读消息数，然后再获取指定的数目。需要注意这个参数不再传入下一个调用。
     */
    @Override
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("AtMeCommentsFragment.sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
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
        int status=mPrefs.getInt(Constants.PREF_SERVICE_COMMENT, 0);
        WeiboLog.d(TAG, "新提及我的评论数:"+status);
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
     * 删除评论
     */
    protected void destroyComment() {
        AKUtils.showToast("not implemented!");
    }

    /**
     * 清除主页的消息计数通知
     */
    @Override
    protected void clearHomeNotify() {
        try {
            int comments=mPrefs.getInt(Constants.PREF_SERVICE_COMMENT, 0);
            mPrefs.edit().remove(Constants.PREF_SERVICE_COMMENT).commit();
            WeiboLog.i(TAG, "清除评论的标记："+comments);
            newOperationTask(new Object[]{Constants.REMIND_CMT}, null);

            SkinFragmentActivity parent=(SkinFragmentActivity) getActivity();
            parent.refreshSidebar();
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
    @Override
    protected Object[] baseBackgroundOperation2(Object... params) {
        try {
            String type=(String) params[0];
            SStatusData sStatusData=new SStatusData();
            //String rs=((SWeiboApi2) App.getMicroBlog(getActivity())).setUnread(type);
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
}
