package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaUserStatusImpl;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
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
 * @description: 显示用户的微博Fragment，根据传入的用户id
 * @author: archko 12-5-7
 */
public class UserTimelineFragment extends RecyclerViewFragment {

    public static final String TAG = "UserTimelineFragment";
    long userId = - 1;
    String userScreenName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeiboLog.v(TAG, "onCreate:" + this);
        /*Intent intent=getActivity().getIntent();
        userId=intent.getLongExtra("user_id", -1);
        userScreenName=intent.getStringExtra("screen_name");
        if (userId==-1) {
            WeiboLog.d(TAG, "用户的id错误，无法查看其微博信息。");
            showToast("用户的id错误，无法查看其微博信息。", Toast.LENGTH_SHORT);

            return;
        }*/
        //mStatusImpl=new SinaUserStatusImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl = new SinaUserStatusImpl();

        AbsApiFactory absApiFactory = null;//new SinaApiFactory();
        try {
            absApiFactory = ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.statusApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    public void _onActivityCreated(Bundle savedInstanceState) {
        WeiboLog.v(TAG, "onActivityCreated");
         super._onActivityCreated(savedInstanceState);

        //loadData();
        if (! hasAttach) {   //不在onAttach中处理,因为refresh可能先调用,以保证数据初始化.
            hasAttach = true;
            int type = getActivity().getIntent().getIntExtra("type", UserFragmentActivity.TYPE_USER_INFO);
            if (type == UserFragmentActivity.TYPE_USER_TIMELINE) {
                refresh();
            }
        }
    }

    @Override
    public void refresh() {
        WeiboLog.v(TAG, "isLoading:" + isLoading + " status:" + (null == mDataList ? "null" : mDataList.size()));
        loadData();
    }

    @Override
    protected void loadData() {
        if (mAdapter == null) {
            mAdapter = new LayoutAdapter(getActivity());
        }
        if (mDataList != null && mDataList.size() > 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (hasAttach) {
                Intent intent = getActivity().getIntent();
                userId = intent.getLongExtra("user_id", - 1);
                userScreenName = intent.getStringExtra("screen_name");
                if (userId == - 1) {
                    WeiboLog.d(TAG, "用户的id错误，无法查看其微博信息。");
                    NotifyUtils.showToast("用户的id错误，无法查看其微博信息。");

                    return;
                }

                if (! isLoading) {
                    fetchData(- 1, - 1, true, false);
                } else {
                    mEmptyTxt.setText(R.string.list_pre_empty_txt);
                    mEmptyTxt.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:" + lastItem + " selectedPos:" + selectedPos);
        int count = mAdapter.getCount();
        if (count < 1) {
            WeiboLog.w(TAG, "no other data");
            return;
        }

        boolean isRefresh = false;
        if (count >= weibo_count * 3) {   //refresh list
            isRefresh = true;
        }
        Status st;
        st = (Status) mAdapter.getItem(mAdapter.getCount() - 1);
        fetchData(- 1, st.id, isRefresh, false);
    }

    /**
     * 获取数据，如果要改变传入的参数，需要覆盖此方法，以便修改。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i(TAG, "sinceId:" + sinceId + ", maxId:" + maxId + ", isRefresh:" + isRefresh + ", isHomeStore:" + isHomeStore + " userId:" + userId);
        if (! App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count = weibo_count;
        if (! isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。
            //count++;
        } else {
            //page=1;
        }

        if (! isLoading) {   //这里多了一个当前查询的用户id
            newTask(new Object[]{isRefresh, sinceId, maxId, count, page, userId, isHomeStore}, null);
        }
    }
}
