package cn.archko.microblog.fragment;

import android.view.View;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 本地数据的Fragment，不需要网络，footerview也不是加载更多
 * 像这样的加载本地数据，需要覆盖loadData，直接调用newTaskNoNet，否则本先判断网络。
 * 需要覆盖fetchMore，通常不是加载更多，这里采取一次性加载。
 * 需要覆盖showMoreView，展示底部的footerview不同的内容,同时需要覆盖onCreateView来显示出moreview。
 * 需要覆盖baseBackgroundOperation，因为没有参数，会出错。如果不覆盖baseBackgroundOperation，需要调用newTask时，要传入5个参数。
 * 如果是静态数据，不需要刷新的，需要修改onCreateView，将ListView设置成下拉刷新失效的。
 * 覆盖basePostOperation方法，因为它与网络数据相关，而且当数据为空时，会在footerview中显示
 * @author: archko 12-10-17
 */
public abstract class AbstractLocalListFragment<T> extends AbsBaseListFragment<T> {

    public static final String TAG="AccountUsersFragment";

    protected void pullToRefreshData() {
        isRefreshing=true;
        newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
    }

    //TODO 需要强制刷新数据，避免编辑后的问题。
    @Override
    protected void loadData() {
        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (!isLoading) {
                //newTask(new Object[]{}, null);
                newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        //WeiboLog.d(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        //mMoreProgressBar.setVisibility(View.GONE);

        addNewData();
    }

    /**
     * 添加新的帐户。
     */
    public void addNewData() {
        NotifyUtils.showToast("not implemented!");
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        WeiboLog.v(TAG, "showMoreView");

        super.showMoreView();
        mMoreProgressBar.setVisibility(View.GONE);
        mMoreTxt.setText(R.string.more_add_account_user);
    }
}
