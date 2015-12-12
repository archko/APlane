package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.action.StatusAction;
import cn.archko.microblog.fragment.impl.SinaMyPostStatusImpl;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.view.ActionModeItemView;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.action.ActionResult;
import com.me.microblog.action.AsyncActionTask;
import com.me.microblog.bean.Status;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 我发布的微博，可以有删除选项。
 * @author: archko 11-11-17
 */
public class MyPostFragment extends RecyclerViewFragment {

    public static final String TAG="MyPostFragment";
    long mUserId=-1l;

    boolean isDeleting=false;
    private ActionMode mMode;

    //--------------------- 数据加载 ---------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);

        WeiboLog.v(TAG, "onCreate:"+this);
        //mStatusImpl=new SinaMyPostStatusImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaMyPostStatusImpl();

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        //mListView.setItemsCanFocus(false);
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为true,其它的不缓存,比如随便看看.
     *
     * @param convertView
     * @param parent
     * @param position
     * @param itemType
     * @return
     */
    @Override
    public View getView(SimpleViewHolder holder, final int position, int itemType) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        ActionModeItemView itemView=null;
        Status status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new ActionModeItemView(getActivity(), updateFlag, true);
        } else {
            itemView=(ActionModeItemView) convertView;
        }
        itemView.update(status, updateFlag, true);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(position, view);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPos=position;
                prepareMenu(up);
                return true;
            }
        });

        return itemView;
    }

    public View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        ActionModeItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new ActionModeItemView(getActivity(), updateFlag, true);
        return itemView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null!=mMode) {
            /*mListView.clearChoices();
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);*/
            mMode.finish();
            mMode=null;
        }
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    @Override
    protected void loadData() {
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
     * 获取数据，覆盖此方法，因为用户的获取与其它不一样。
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
        if (!App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFinished();
            }
            refreshAdapter(false, false);
            return;
        }

        int count=weibo_count;
        if (!isRefresh) {  //如果不是刷新，需要多加载一条数据，解析回来时，把第一条略过。
            count++;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, mUserId, sinceId, maxId, count, page, isHomeStore}, null);
        }
    }

    //--------------------- 微博操作 ---------------------
    @Override
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_destroy_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_destroy_batch);
    }

    Handler mStatusHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            isDeleting=false;
            if (!isResumed()) {
                WeiboLog.w(TAG, "已经结束了Fragment，不需要通知消息");
                return;
            }

            switch (msg.what) {
                case ActionResult.ACTION_SUCESS: {
                    ActionResult actionResult=(ActionResult) msg.obj;
                    ArrayList<Long> sucIds=(ArrayList<Long>) actionResult.obj;
                    NotifyUtils.showToast(String.format(getResources().getString(R.string.status_delete_suc), sucIds.size()));

                    updateList(sucIds);
                    break;
                }

                case ActionResult.ACTION_FALL:
                    ActionResult actionResult=(ActionResult) msg.obj;
                    NotifyUtils.showToast(actionResult.reslutMsg, Toast.LENGTH_LONG);
                    if (WeiboLog.isDEBUG()) {
                        WeiboLog.d(TAG, "delete status failed."+actionResult.reslutMsg);
                    }

                    ArrayList<Long> sucIds=(ArrayList<Long>) actionResult.obj;
                    ArrayList<Long> failedIds=(ArrayList<Long>) (actionResult.results)[0];
                    WeiboLog.i(TAG, "成功："+sucIds.size()+" 失败:"+failedIds.size());
                    NotifyUtils.showToast("成功："+sucIds.size()+"个 失败:"+failedIds.size()+"个");

                    updateList(sucIds);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }

        /**
         * 删除成功后更新列表。
         * @param sucIds 成功删除的id列表
         * @param dataList 原来的数据
         */
        private void updateList(ArrayList<Long> sucIds) {
            ArrayList<Status> dataList=mDataList;
            boolean find=false;
            Status tmp=null;
            if (sucIds.size()>0) {
                clearSelection();
                for (Long id : sucIds) {
                    for (Status status : dataList) {
                        if (id==status.id) {
                            find=true;
                            tmp=status;
                            break;
                        }
                    }

                    if (find) {
                        dataList.remove(tmp);
                    }
                    find=false;
                }
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "新的数据集合为："+dataList.size());
                }
            }
        }
    };

    @Override
    protected void itemClick(int pos, View achor) {
        if (isDeleting) {
            NotifyUtils.showToast("正在处理删除操作，不能查看！");
            return;
        }

        if (null==mMode) {
            super.itemClick(pos, achor);
        }
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected boolean itemLongClick(int pos, View achor) {
        if (isDeleting) {
            NotifyUtils.showToast("正在处理删除操作，不能查看！");
            return true;
        }

        if (null==mMode) {
            //showButtonBar(achor);
            return super.itemLongClick(pos, achor);
        }

        return false;
    }

    /**
     * 快速转发,因为自己发布的微博不能转发,所以在这里用于删除.
     */
    public void quickRepostStatus() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "delete status.");
        }
        if (selectedPos==-1) {
            return;
        }

        if (!App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);

            return;
        }

        try {
            NotifyUtils.showToast("开始删除，请稍等！");
            Status status=mDataList.get(selectedPos);
            StatusAction action=new StatusAction();

            isDeleting=true;
            AsyncActionTask task=new AsyncActionTask(getActivity(), action);
            task.execute(0, status.id, mStatusHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面 因为自己发布的微博不能转发,所以在这里用于批量删除.
     * 批量删除用ActionMode来处理.
     */
    public void commentStatus() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "batch delete status.");
        }
        turnOnActionMode();
        /*ListView listView=mListView;
        listView.setItemChecked(selectedPos, true);*/
        mMode.invalidate();
    }

    //--------------------- action mode ---------------------
    private void turnOnActionMode() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "turnOnActionMode");
        }
        mMode=getActivity().startActionMode(new StatusActionMode());
        /*ListView lv = mListView;
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);*/
    }

    //TODO 在这里用有些问题，比如应用 Mode时，不能将列表选中项清空。不能在第一次长按时选中某项。
    private class StatusActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "onCreateActionMode");
            }
            getActivity().getMenuInflater().inflate(R.menu.status_mode_menu, menu);

            int selectId=R.drawable.ic_action_select_invert_light;
            String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
            if ("0".equals(themeId)) {
            } else if ("1".equals(themeId)) {
            } else if ("2".equals(themeId)) {
                selectId=R.drawable.ic_action_select_invert_light;
            }
            menu.findItem(R.id.invert_selection).setIcon(selectId);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            /*WeiboLog.d(TAG, "onPrepareActionMode");
            ListView lv=mListView;
            int nbrCheckedItem=0;

            for (int i=0; i<lv.getCount(); i++) {
                if (lv.isItemChecked(i)) {
                    nbrCheckedItem++;
                }
            }
            menu.findItem(R.id.delete).setVisible(nbrCheckedItem>0);*/
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "onActionItemClicked:"+item);
            }
            int itemId=item.getItemId();
            if (itemId==R.id.delete) {
                actionModeDelete();
                return true;
            } else if (itemId==R.id.invert_selection) {
                actionModeInvertSelection();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "onDestroyActionMode");
            }

            mMode=null;
            clearSelection();
        }
    }

    private void actionModeDelete() {
        if (!App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error);

            return;
        }

        /*ListView lv = mListView;

        ArrayList<Long> checkedIds = new ArrayList<Long>();

        long[] selectedIds = lv.getCheckItemIds();
        WeiboLog.d(TAG, " selectedIds:" + selectedIds.length);
        int pos = 0;
        Status tmp;
        try {   //TODO 下面的pos可能是-1，大概是前一次 选择后没有清除
            for (long id : selectedIds) {
                pos = (int) id;
                WeiboLog.d(TAG, "pos:" + pos);
                tmp = (Status) mAdapter.getItem(pos);
                checkedIds.add(tmp.id);
                WeiboLog.v(TAG, "title:" + tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (checkedIds.size() > 0) {
            //String strCheckedIds=TextUtils.join(", ", checkedIds);
            //WeiboLog.d(TAG, "strCheckedIds:"+strCheckedIds);
            try {
                NotifyUtils.showToast("开始批量删除，请稍等！");
                StatusAction action = new StatusAction();
                isDeleting = true;

                AsyncActionTask task = new AsyncActionTask(getActivity(), action);
                task.execute(1, checkedIds, mStatusHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMode.finish();
        }*/
    }

    private void actionModeInvertSelection() {
        /*ListView lv = mListView;

        for (int i = 0; i < lv.getCount(); i++) {
            lv.setItemChecked(i, ! lv.isItemChecked(i));
        }
        mMode.invalidate();*/
    }

    void clearSelection() {
        /*ListView lv = mListView;
        // Uncheck all
        int count = lv.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            lv.setItemChecked(i, false);
        }
        lv.clearChoices();
        lv.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        mAdapter.notifyDataSetChanged();*/
    }
}
