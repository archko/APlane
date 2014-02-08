package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.action.SFavAction;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.fragment.impl.SinaMyFavStatusImpl;
import com.andrew.apollo.utils.PreferenceUtils;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.FavItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.action.ActionResult;
import com.me.microblog.action.AsyncActionTask;
import com.me.microblog.bean.Favorite;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.factory.SinaApiFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 我的收藏列表
 * @author: archko 13-2-19
 */
public class MyFavFragment extends AbsBaseListFragment<Favorite> {

    public static final String TAG="MyFavFragment";
    long mUserId=-1l;

    boolean isDeleting=false;
    private ActionMode mMode;

    //--------------------- 数据加载 ---------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);

        WeiboLog.v(TAG, "onCreate:"+this);
        mStatusImpl=new SinaMyFavStatusImpl();
        weibo_count=15; //加载15条数据
        page=1;     //收藏默认从1开始页码
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaMyFavStatusImpl();

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        mListView.setItemsCanFocus(false);
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为true,其它的不缓存,比如随便看看.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //WeiboLog.d(TAG, "getView.pos:"+position+" getCount():"+getCount()+" lastItem:");

        FavItemView itemView=null;
        Favorite status=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new FavItemView(getActivity(), mListView, mCacheDir, status, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView=(FavItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);

        return itemView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null!=mMode) {
            mListView.clearChoices();
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            mMode.finish();
            mMode=null;
        }
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    @Override
    protected void loadData() {
        //super.loadData();   //TODO 需要保存本地数据，暂时先不存储
        if (mDataList!=null&&mDataList.size()>0) {
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
            AKUtils.showToast(R.string.network_error);
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

    //--------------------- 数据获取 ---------------------

    /**
     * 获取更多数据的操作，统一处理了，就是获取当前列表最后一项，用它的id获取更多数据。
     * //TODO 因为更多数据有可能就是为空，如果获取到空数据，是没有更多数据，下次不应该再调用。
     */
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            page++;
            fetchData(-1, -1, false, false);
        }
    }

    /**
     * 查看Status原文信息,包括评论.
     */
    @Override
    protected void viewOriginalStatus(View achor) {
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            Favorite favorite=mDataList.get(selectedPos);

            WeiboOperation.toViewOriginalStatus(getActivity(), favorite.mStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- 微博操作 ---------------------
    @Override
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_destroy_fav);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_destroy_batch);
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
        }
        return false;
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
                    Object obj=actionResult.obj;
                    if (obj instanceof Favorite) {
                        AKUtils.showToast(String.format(getResources().getString(R.string.fav_delete_suc), 1));
                        ArrayList<Long> ids=new ArrayList<Long>(1);
                        Favorite f=(Favorite) obj;
                        ids.add(f.mStatus.id);
                        updateList(ids);
                    } else if (obj instanceof ArrayList) {
                        ArrayList<Long> ids=(ArrayList<Long>) obj;
                        AKUtils.showToast(String.format(getResources().getString(R.string.fav_delete_suc), ids.size()));

                        updateList(ids);
                    }
                    break;
                }

                case ActionResult.ACTION_FALL:
                    ActionResult actionResult=(ActionResult) msg.obj;
                    AKUtils.showToast(actionResult.reslutMsg, Toast.LENGTH_LONG);
                    WeiboLog.d(TAG, "delete status failed."+actionResult.reslutMsg);

                    ArrayList<Long> sucIds=(ArrayList<Long>) actionResult.obj;
                    ArrayList<Long> failedIds=(ArrayList<Long>) (actionResult.results)[0];
                    WeiboLog.i(TAG, "成功："+sucIds.size()+" 失败:"+failedIds.size());
                    AKUtils.showToast("成功："+sucIds.size()+"个 失败:"+failedIds.size()+"个");

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
            if (sucIds.size()>0) {
                clearSelection();
                ArrayList<Favorite> dataList=mDataList;
                for (Long id : sucIds) {
                    for (Favorite favorite : dataList) {
                        if (id==favorite.mStatus.id) {
                            dataList.remove(favorite);
                            break;
                        }
                    }
                }
                WeiboLog.d(TAG, "新的数据集合为："+dataList.size());
            }
        }
    };

    @Override
    protected void itemClick(View achor) {
        if (isDeleting) {
            AKUtils.showToast("正在处理删除操作，不能查看！");
            return;
        }

        if (null==mMode) {
            viewOriginalStatus(achor);
        }
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected boolean itemLongClick(View achor) {
        if (isDeleting) {
            AKUtils.showToast("正在处理删除操作，不能查看！");
            return true;
        }

        if (null==mMode) {
            return super.itemLongClick(achor);
        }

        return false;
    }

    /**
     * 快速转发,因为自己发布的收藏不能转发,所以在这里用于删除.
     */
    protected void quickRepostStatus() {
        WeiboLog.d(TAG, "delete status.");
        if (selectedPos==-1) {
            return;
        }

        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error);

            return;
        }

        try {
            AKUtils.showToast("开始删除，请稍等！");
            Favorite favorite=mDataList.get(selectedPos);
            SFavAction action=new SFavAction();

            isDeleting=true;
            AsyncActionTask task=new AsyncActionTask(getActivity(), action);
            task.execute(0, favorite.mStatus.id, mStatusHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面 因为自己发布的微博不能转发,所以在这里用于批量删除.
     * 批量删除用ActionMode来处理.
     */
    protected void commentStatus() {
        WeiboLog.d(TAG, "batch delete status.");
        turnOnActionMode();
        /*ListView listView=mListView;
        listView.setItemChecked(selectedPos, true);*/
        mMode.invalidate();
    }

    //--------------------- action mode ---------------------
    private void turnOnActionMode() {
        WeiboLog.d(TAG, "turnOnActionMode");
        mMode=getActivity().startActionMode(new StatusActionMode());
        ListView lv=mListView;
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    //TODO 在这里用有些问题，比如应用 Mode时，不能将列表选中项清空。不能在第一次长按时选中某项。
    private class StatusActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            WeiboLog.v(TAG, "onCreateActionMode");
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
            WeiboLog.d(TAG, "onActionItemClicked:"+item);
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
            WeiboLog.d(TAG, "onDestroyActionMode");

            mMode=null;
            clearSelection();
        }
    }

    private void actionModeDelete() {
        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error);

            return;
        }

        ListView lv=mListView;

        ArrayList<Long> checkedIds=new ArrayList<Long>();

        long[] selectedIds=lv.getCheckItemIds();
        WeiboLog.d(TAG, " selectedIds:"+selectedIds.length);
        int pos=0;
        Favorite tmp;
        try {   //TODO 下面的pos可能是-1，大概是前一次 选择后没有清除
            for (long id : selectedIds) {
                pos=(int) id;
                //WeiboLog.v(TAG, "pos:"+pos);
                tmp=(Favorite) mAdapter.getItem(pos);
                checkedIds.add(tmp.mStatus.id);
                WeiboLog.v(TAG, "title:"+tmp.mStatus.id+" "+tmp.mStatus.text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (checkedIds.size()>0) {
            //String strCheckedIds=TextUtils.join(", ", checkedIds);
            //WeiboLog.d(TAG, "strCheckedIds:"+strCheckedIds);
            try {
                AKUtils.showToast("开始批量删除，请稍等！");
                SFavAction action=new SFavAction();
                isDeleting=true;

                AsyncActionTask task=new AsyncActionTask(getActivity(), action);
                task.execute(1, checkedIds, mStatusHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMode.finish();
        }
    }

    private void actionModeInvertSelection() {
        ListView lv=mListView;

        for (int i=0; i<lv.getCount(); i++) {
            lv.setItemChecked(i, !lv.isItemChecked(i));
        }
        mMode.invalidate();
    }

    void clearSelection() {
        ListView lv=mListView;
        // Uncheck all
        int count=lv.getAdapter().getCount();
        for (int i=0; i<count; i++) {
            lv.setItemChecked(i, false);
        }
        lv.clearChoices();
        lv.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        mAdapter.notifyDataSetChanged();
    }
}
