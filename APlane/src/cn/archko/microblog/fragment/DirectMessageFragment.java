package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.fragment.impl.SinaDMImpl;
import cn.archko.microblog.listeners.CommentListener;
import cn.archko.microblog.ui.SkinFragmentActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.CommentDialog;
import cn.archko.microblog.view.DirectMessageItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.DirectMessage;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.sina.SinaDMApi;
import com.me.microblog.core.sina.SinaUnreadApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 私信，需要使用全功能的key。
 * @author: archko 12-11-2
 */
public class DirectMessageFragment extends AbsBaseListFragment<DirectMessage> {

    public static final String TAG = "DirectMessageFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaDMImpl();
    }

    public void initApi() {
        mStatusImpl = new SinaDMImpl();

        AbsApiFactory absApiFactory = null;//new SinaApiFactory();
        try {
            absApiFactory = ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.dmApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        weibo_count = Constants.WEIBO_COUNT_MIN;
    }

    //--------------------- 微博操作 ---------------------
    /*public SStatusData<DirectMessage> getStatuses(Long sinceId, Long maxId, int c, int p) throws WeiboException {
        WeiboLog.d(TAG, " DirectMessageFragment.getStatuses."+sinceId+" maxId:"+maxId+" count:"+c+" page:"+p);
        SStatusData<DirectMessage> sStatusData=null;
        SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<DirectMessage>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=getString(R.string.err_api_error);
        } else {
            c=10;   //私信不像其它，不用太多条。
            sStatusData=sWeiboApi2.getDirectMessages(sinceId, maxId, c, p);
        }

        return sStatusData;
    }*/

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    @Override
    protected void loadData() {
        if (mDataList != null && mDataList.size() > 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            if (! isLoading) {
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
        if (! isLoading) {
            Object[] params = new Object[]{false, currentUserId};
            newTaskNoNet(params, null);
        }
    }

    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:" + lastItem + " selectedPos:" + selectedPos);
        if (mAdapter.getCount() > 0) {
            DirectMessage st;
            st = (DirectMessage) mAdapter.getItem(mAdapter.getCount() - 1);
            fetchData(- 1, st.id, false, false);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DirectMessageItemView itemView = null;
        DirectMessage directMessage = mDataList.get(position);
        boolean updateFlag = true;
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag = false;
        }

        if (convertView == null) {
            itemView = new DirectMessageItemView(getActivity(), mListView, mCacheDir, directMessage, updateFlag, true, showBitmap);
        } else {
            itemView = (DirectMessageItemView) convertView;
        }

        itemView.update(directMessage, updateFlag, true, showBitmap);

        return itemView;
    }

    //--------------------- 评论操作，删除或回复 ---------------------
    CommentDialog mCommentDialog;
    CommentListener mCommentListener;

    void initDialog() {
        if (null == mCommentDialog) {
            mCommentDialog = new CommentDialog(getActivity());
            mCommentListener = new CommentListener() {

                @Override
                public void cancel() {
                    WeiboLog.d("mCommentListener.cancel");
                }

                @Override
                public void finish(Object receiver, final String content) {
                    WeiboLog.d("mCommentListener!");
                    try {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                DirectMessage message = mDataList.get(selectedPos);
                                long uid = message.senderId;
                                try {
                                    //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
                                    SinaDMApi sWeiboApi2 = new SinaDMApi();
                                    sWeiboApi2.updateToken();
                                    DirectMessage result = sWeiboApi2.sendDirectMessage(uid, content);
                                    if (null != result) {
                                        mCommentDialog.dismiss();
                                        showUIToast(R.string.dm_new_suc);
                                    } else {
                                        showUIToast(R.string.dm_new_failed);
                                    }
                                } catch (WeiboException e) {
                                    int code = e.getStatusCode();
                                    if (code == WeiboException.EX_CODE_TOKEN_EXPIRE) {
                                        showUIToast(R.string.dm_new_token_isexpired);
                                    }
                                    e.printStackTrace();
                                } catch (Exception e) {
                                } finally {
                                    hideProgressBar();
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mCommentDialog.setListener(mCommentListener);
            mCommentDialog.bindEvent(getActivity());
        }
    }

    public void showUIToast(final int resId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotifyUtils.showToast(resId, Toast.LENGTH_SHORT);
            }
        });
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index = 0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPLY_DM, index++, R.string.opb_reply_dm);
        menuBuilder.getMenu().add(0, Constants.OP_ID_OPB_DESTROY_DM, index++, R.string.opb_destroy_dm);
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, index++, R.string.user_view_user);
        menuBuilder.getMenu().add(0, Constants.OP_ID_STATUS, index++, R.string.opb_user_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_AT, index++, R.string.opb_at);
    }

    @Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        try {
            DirectMessage message = mDataList.get(selectedPos);
            if (message.senderId == currentUserId) {
                menuBuilder.getMenu().findItem(Constants.OP_ID_REPLY_DM).setVisible(false);
            } else {
                menuBuilder.getMenu().findItem(Constants.OP_ID_REPLY_DM).setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_REPLY_DM: {   //回复私信
                replyComment();
                break;
            }
            case Constants.OP_ID_OPB_DESTROY_DM: { //删除私信
                destroyComment();
                break;
            }
            case Constants.OP_ID_DESTROY_BATCH_COMMENT: {  //批量删除
                destroyBatchComment();
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                final DirectMessage message = mDataList.get(selectedPos);
                String sn = message.senderScreenName;
                long uid = message.senderId;
                if (uid == currentUserId) {
                    sn = message.recipientScreenName;
                    uid = message.recipientId;
                }
                WeiboOperation.toViewStatusUser(getActivity(), sn, uid, UserFragmentActivity.TYPE_USER_INFO);
                break;
            }
            case Constants.OP_ID_STATUS: {
                final DirectMessage message = mDataList.get(selectedPos);
                String sn = message.senderScreenName;
                long uid = message.senderId;
                if (uid == currentUserId) {
                    sn = message.recipientScreenName;
                    uid = message.recipientId;
                }
                WeiboOperation.toViewStatusUser(getActivity(), sn, uid, UserFragmentActivity.TYPE_USER_TIMELINE);
                break;
            }
            case Constants.OP_ID_AT: {
                try {
                    final DirectMessage message = mDataList.get(selectedPos);
                    String sn = message.senderScreenName;
                    if (message.senderId == currentUserId) {
                        sn = message.recipientScreenName;
                    }
                    WeiboOperation.toAtUser(getActivity(), sn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected void itemClick(View achor) {
        replyComment();
    }

    /**
     * 回复评论
     */
    protected void replyComment() {
        WeiboLog.i(TAG, "replyDirectMessage:" + selectedPos);
        try {
            DirectMessage message = mDataList.get(selectedPos);
            initDialog();
            mCommentDialog.setCustomTitle(R.string.dm_new);
            //mCommentDialog.setContent(message.sender.screenName);
            mCommentDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除评论
     */
    protected void destroyComment() {
        NotifyUtils.showToast("not implemented!");
    }

    /**
     * 删除评论
     */
    protected void destroyBatchComment() {
        NotifyUtils.showToast("not implemented!");
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        if (isRefresh && load) {
            clearHomeNotify();
        }
    }

    void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommentDialog.hideProgressBar();
            }
        });
    }

    //--------------------- 清除未读消息操作 ---------------------

    /**
     * 清除主页的消息计数通知
     */
    private void clearHomeNotify() {
        int newDm = mPrefs.getInt(Constants.PREF_SERVICE_DM, 0);
        mPrefs.edit().remove(Constants.PREF_SERVICE_DM).commit();
        try {
            SkinFragmentActivity parent = (SkinFragmentActivity) getActivity();
            parent.refreshSidebar();

            newOperationTask(new Object[]{Constants.REMIND_DM}, null);
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
            String type = (String) params[ 0 ];
            SStatusData sStatusData = new SStatusData();
            //String rs=((SWeiboApi2) App.getMicroBlog(getActivity())).setUnread(type);
            SinaUnreadApi sinaUnreadApi = new SinaUnreadApi();
            sinaUnreadApi.updateToken();
            String rs = sinaUnreadApi.setUnread(type);
            sStatusData.errorMsg = rs;
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
    @Override
    protected void basePostOperation2(Object[] resultObj) {
        try {
            SStatusData sStatusData = (SStatusData) resultObj[ 0 ];
            WeiboLog.i(TAG, TAG + sStatusData);
            if (null == sStatusData) {
                return;
            }

            if (sStatusData.errorCode > 0 && ! TextUtils.isEmpty(sStatusData.errorMsg)) {
                NotifyUtils.showToast(sStatusData.errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
