package cn.archko.microblog.fragment;

import android.content.Intent;
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
import cn.archko.microblog.fragment.impl.SinaAtMeCommentImpl;
import cn.archko.microblog.ui.SkinFragmentActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.CommentDialog;
import cn.archko.microblog.view.CommentItemView;
import cn.archko.microblog.listeners.CommentListener;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.sina.SinaCommentApi;
import com.me.microblog.core.sina.SinaUnreadApi;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: @我的评论,
 * @author: archko 12-12-22
 */
public class AtMeCommentsFragment extends AbsBaseListFragment<Comment> {

    public static final String TAG="AtMeCommentsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initApi() {
        mStatusImpl=new SinaAtMeCommentImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.commentApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            AKUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
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
        int status=mPrefs.getInt(Constants.PREF_SERVICE_AT_COMMENT, 0);
        WeiboLog.d(TAG, "新提及我的评论数:"+status);
        if (status>0) {
            if (status>Constants.WEIBO_COUNT*8) {
                status=Constants.WEIBO_COUNT*8;
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
                //queryData();
                loadLocalData();
            }
        }
    }

    protected void loadLocalData() {
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
    public void refreshNewData(SStatusData<Comment> sStatusData, Boolean isRefresh) {
        //TODO 还需要处理获取更多的数据
        ArrayList<Comment> list=sStatusData.mStatusData;
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
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.d(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        if (mAdapter.getCount()>0) {
            Comment st;
            st=(Comment) mAdapter.getItem(mAdapter.getCount()-1);
            fetchData(-1, st.id, false, false);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentItemView itemView=null;
        Comment status=mDataList.get(position);
        boolean updateFlag=true;
        if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag=false;
        }

        if (convertView==null) {
            itemView=new CommentItemView(getActivity(), mListView, mCacheDir, status, updateFlag, true, showBitmap, true);
        } else {
            itemView=(CommentItemView) convertView;
        }

        itemView.update(status, updateFlag, true, showBitmap);

        return itemView;
    }

    //--------------------- 评论操作，删除或回复 ---------------------
    CommentDialog mCommentDialog;
    CommentListener mCommentListener;

    void initDialog() {
        if (null==mCommentDialog) {
            mCommentDialog=new CommentDialog(getActivity());
            mCommentListener=new CommentListener() {

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
                                Comment comment=mDataList.get(selectedPos);
                                long cid=comment.id;
                                long id=comment.status.id;
                                try {
                                    //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
                                    SinaCommentApi sWeiboApi2=new SinaCommentApi();
                                    sWeiboApi2.updateToken();
                                    Comment result=sWeiboApi2.commentReply(cid, id, content, null);
                                    if (null!=result&&result.id>0) {
                                        mCommentDialog.dismiss();
                                        showUIToast(R.string.comment_reply_suc);
                                    } else {
                                        showUIToast(R.string.comment_reply_failed);
                                    }
                                } catch (WeiboException e) {
                                    int code=e.getStatusCode();
                                    if (code==WeiboException.EX_CODE_TOKEN_EXPIRE) {
                                        showUIToast(R.string.comment_reply_token_isexpired);
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
                AKUtils.showToast(resId, Toast.LENGTH_SHORT);
            }
        });
    }

    void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommentDialog.hideProgressBar();
            }
        });
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPLY_COMMENT, index++, R.string.opb_reply_comment);
        //menuBuilder.add(0, Constants.OP_ID_OPB_DESTROY_COMMENT, index++, R.string.opb_destroy_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, index++, R.string.user_view_user);
        menuBuilder.getMenu().add(0, Constants.OP_ID_STATUS, index++, R.string.opb_user_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_AT, index++, R.string.opb_at);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT_STATUS, index++, R.string.opb_comment_status);
    }

    @Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        super.onPrepareCustomMenu(menuBuilder);
        try {
            Comment comment=mDataList.get(selectedPos);
            if (comment.user.id==currentUserId) {
                menuBuilder.getMenu().findItem(Constants.OP_ID_REPLY_COMMENT).setVisible(false);
            } else {
                menuBuilder.getMenu().findItem(Constants.OP_ID_REPLY_COMMENT).setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_REPLY_COMMENT: {   //回复评论
                replyComment();
                break;
            }
            case Constants.OP_ID_OPB_DESTROY_COMMENT: { //删除评论
                destroyComment();
                break;
            }
            case Constants.OP_ID_DESTROY_BATCH_COMMENT: {  //批量删除
                destroyBatchComment();
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                final Comment comment=mDataList.get(selectedPos);
                /*Intent intent=new Intent(getActivity(), UserFragmentActivity.class);
                intent.putExtra("nickName", comment.user.screenName);
                intent.putExtra("user_id", comment.user.id);
                intent.putExtra("type", UserFragmentActivity.TYPE_USER_INFO);
                startActivity(intent);
                ((Activity)context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);*/

                WeiboOperation.toViewStatusUser(getActivity(), comment.user, UserFragmentActivity.TYPE_USER_INFO);
                break;
            }
            case Constants.OP_ID_STATUS: {
                final Comment comment=mDataList.get(selectedPos);
                WeiboOperation.toViewStatusUser(getActivity(), comment.user, UserFragmentActivity.TYPE_USER_TIMELINE);
                break;
            }
            case Constants.OP_ID_AT: {
                try {
                    final Comment comment=mDataList.get(selectedPos);
                    User user=comment.user;
                    WeiboOperation.toAtUser(getActivity(), user.screenName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case Constants.OP_ID_COMMENT_STATUS: {
                try {
                    final Comment comment=mDataList.get(selectedPos);
                    Status status=comment.status;
                    WeiboOperation.toViewOriginalStatus(getActivity(), status, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    @Override
    protected void itemClick(View achor) {
        //showButtonBar(achor);
        //replyComment();
        //prepareMenu(up);
        try {
            Comment comment=mDataList.get(selectedPos);
            if (comment.user.id!=currentUserId) {
                replyComment();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 回复评论
     */
    protected void replyComment() {
        WeiboLog.d(TAG, "replyComment:"+selectedPos);
        try {
            Comment comment=mDataList.get(selectedPos);
            initDialog();
            mCommentDialog.setComment(comment);
            mCommentDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除评论
     */
    protected void destroyComment() {
        AKUtils.showToast("not implemented!");
    }

    /**
     * 删除评论
     */
    protected void destroyBatchComment() {
        AKUtils.showToast("not implemented!");
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        super.refreshAdapter(load, isRefresh);
        if (isRefresh&&load) {
            clearHomeNotify();
        }
    }

    /**
     * 清除主页的消息计数通知
     */
    protected void clearHomeNotify() {
        try {
            int mention_cmt=mPrefs.getInt(Constants.PREF_SERVICE_AT_COMMENT, 0);
            mPrefs.edit().remove(Constants.PREF_SERVICE_AT_COMMENT).commit();
            WeiboLog.i(TAG, "清除评论的标记 @评论："+mention_cmt);
            newOperationTask(new Object[]{Constants.REMIND_MENTION_CMT}, null);

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

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    @Override
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
