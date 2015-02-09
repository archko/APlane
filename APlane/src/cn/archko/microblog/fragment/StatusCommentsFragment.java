package cn.archko.microblog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.fragment.impl.SinaCommentImpl;
import cn.archko.microblog.listeners.CommentListener;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.ui.PrefsActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.CommentDialog;
import cn.archko.microblog.view.CommentItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.factory.AbsApiFactory;
import com.me.microblog.core.factory.ApiConfigFactory;
import com.me.microblog.core.sina.SinaCommentApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.Serializable;
import java.util.Date;

/**
 * @version 1.00.00  用户的评论，发出，收到，与合并的
 *          由于两种数据的合并，导致刷新时出现问题，现在先更新一种，然后再更新另一种。一次清除一个标志。
 * @description:
 * @author: archko 11-11-17
 */
public class StatusCommentsFragment extends AbsBaseListFragment<Comment> {

    public static final String TAG="StatusCommentsFragment";
    private Status mStatus=null;
    boolean hasInitialed=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaCommentImpl();
    }

    @Override
    public void initApi() {
        mStatusImpl=new SinaCommentImpl();

        AbsApiFactory absApiFactory=null;//new SinaApiFactory();
        try {
            absApiFactory=ApiConfigFactory.getApiConfig(((App) App.getAppContext()).getOauthBean());
            mStatusImpl.setApiImpl((AbsApiImpl) absApiFactory.commentApiFactory());
        } catch (WeiboException e) {
            e.printStackTrace();
            NotifyUtils.showToast("初始化api异常.");
            //getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //这里为是否显示列表的评论者头像。
        showBitmap=mPrefs.getBoolean(PrefsActivity.PREF_COMMENT_USER_BM, true);
        weibo_count=Constants.WEIBO_COUNT_MIN;
    }

    @Override
    public View _onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super._onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void _onActivityCreated(Bundle savedInstanceState) {
        super._onActivityCreated(savedInstanceState);
        Intent intent=getActivity().getIntent();
        Serializable status=intent.getSerializableExtra("status");
        mStatus=(Status) status;
        /*SinaCommentImpl commentImpl=(SinaCommentImpl) mStatusImpl;
        commentImpl.mStatus=mStatus;*/

        mEmptyTxt.setText("下拉获取评论！");
        mEmptyTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //--------------------- 数据加载 ---------------------
    @Override
    public void fetchMore() {
        super.fetchMore();
        WeiboLog.v(TAG, "fetchMore.lastItem:"+lastItem+" selectedPos:"+selectedPos);
        int count=mAdapter.getCount();
        if (count<1) {
            WeiboLog.w(TAG, "no other data.");
            return;
        }

        boolean isRefresh=false;
        /*if (count>=weibo_count*3) {   //refresh list
            isRefresh=true;
        }*/
        Comment st;
        st=(Comment) mAdapter.getItem(mAdapter.getCount()-1);
        fetchData(-1, st.id, isRefresh, false);
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为false,其它的不缓存,比如随便看看.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(SimpleViewHolder holder, final int position) {
        //WeiboLog.d(TAG, "getView.pos:" + position + " holder:" + holder);

        View convertView=holder.baseItemView;
        CommentItemView itemView=null;
        final Comment bean=mDataList.get(position);

        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }

        if (null==convertView) {
            itemView=new CommentItemView(getActivity(), mCacheDir, updateFlag, false, showBitmap, false);
        } else {
            itemView=(CommentItemView) convertView;
        }

        itemView.update(bean, updateFlag, false, showBitmap);
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
        CommentItemView itemView=null;
        boolean updateFlag=true;
        if (mScrollState!=RecyclerView.SCROLL_STATE_IDLE) {
            updateFlag=false;
        }
        itemView=new CommentItemView(getActivity(), mCacheDir, updateFlag, false, showBitmap, false);
        return itemView;
    }

    /**
     * 加载数据，可以供子类覆盖，分别加载不同类型的数据。
     */
    protected void loadData() {
        if (mDataList!=null&&mDataList.size()>0) {
            mAdapter.notifyDataSetChanged();
        }/* else {
            if (!isLoading) {
                isRefreshing=true;
                fetchData(-1, -1);
            }
        }*/
    }

    /**
     * 获取数据
     *
     * @param sinceId
     * @param maxId
     * @param isRefresh   是否是更新的，如果是更新的，应该把原来的列表清空。
     * @param isHomeStore 是否是主页,只有主页有存储
     */
    public void fetchData(long sinceId, long maxId, boolean isRefresh, boolean isHomeStore) {
        WeiboLog.i(TAG, "sinceId:"+sinceId+", maxId:"+maxId+", isRefresh:"+isRefresh+", isHomeStore:"+isHomeStore);
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
            //count++;
        } else {
            //page=1;
        }

        if (!isLoading) {
            newTask(new Object[]{isRefresh, mStatus.id, count, page, isHomeStore}, null);
        }
    }

    @Override
    public void refreshAdapter(boolean load, boolean isRefresh) {
        WeiboLog.d(TAG, "refreshAdapter.load:"+load+" isRefresh:"+isRefresh);
        try {
            mSwipeLayout.setRefreshing(fastScroll);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (load) {
            //mPullRefreshListView.setLastUpdatedLabel(getString(R.string.pull_to_refresh_label)+DateUtils.longToDateTimeString(System.currentTimeMillis()));
            mAdapter.notifyDataSetChanged();
            hasInitialed=true;
        }

        if (isRefresh) {
            scrollToHeader();
        }

        if (null!=mDataList&&mDataList.size()>0) {
            if (mEmptyTxt.getVisibility()==View.VISIBLE) {
                mEmptyTxt.setVisibility(View.GONE);
            }
        } else {
            mEmptyTxt.setText(R.string.list_empty_txt);
            mEmptyTxt.setVisibility(View.VISIBLE);
        }
    }

    //--------------------- 微博操作 ---------------------

    /**
     * 刷新数据，因为不是自动加载评论，所以在选中时，需要处理
     */
    public void refresh() {
        WeiboLog.d(TAG, "refresh:"+hasInitialed);
        if (!hasInitialed) {
            hasInitialed=true;
            mSwipeLayout.setRefreshing(true);
            pullToRefreshData();
        }
    }

    /**
     * 查看Status原文信息,包括评论.
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
        //showButtonBar(achor);
        replyComment();
    }

    //--------------------- popupMenu ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, index++, R.string.user_view_user);
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPLY_COMMENT, index++, R.string.opb_reply_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_OPB_DESTROY_COMMENT, index++, R.string.opb_destroy_comment);
        menuBuilder.getMenu().add(0, Constants.OP_ID_STATUS, index++, R.string.opb_user_status);
        menuBuilder.getMenu().add(0, Constants.OP_ID_AT, index++, R.string.opb_at);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_VIEW_USER: {
                Comment comment=mDataList.get(selectedPos);
                WeiboOperation.toViewStatusUser(getActivity(), comment.user, UserFragmentActivity.TYPE_USER_INFO);
                break;
            }
            case Constants.OP_ID_STATUS: {
                Comment comment=mDataList.get(selectedPos);
                WeiboOperation.toViewStatusUser(getActivity(), comment.user, UserFragmentActivity.TYPE_USER_TIMELINE);
                break;
            }
            case Constants.OP_ID_REPLY_COMMENT: {   //回复评论
                replyComment();
                break;
            }
            case Constants.OP_ID_OPB_DESTROY_COMMENT: { //删除评论
                //destroyComment();
                break;
            }
            case Constants.OP_ID_AT: {
                try {
                    Status status=mStatus;
                    User user=status.user;
                    WeiboOperation.toAtUser(getActivity(), user.screenName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    //--------------------- 评论操作，删除或回复 ---------------------
    CommentDialog mCommentDialog;
    CommentListener mCommentListener;

    public void showUIToast(final int resId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotifyUtils.showToast(resId, Toast.LENGTH_SHORT);
            }
        });
    }

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
                                    } else {
                                        showUIToast(R.string.comment_reply_failed);
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

    void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommentDialog.hideProgressBar();
            }
        });
    }

    /**
     * 回复评论
     */
    protected void replyComment() {
        WeiboLog.i(TAG, "replyComment:"+selectedPos);
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
     * add a comment to datalist
     *
     * @param task
     */
    public void addComment(SendTask task) {
        WeiboLog.d(TAG, "addComment:"+task);
        try {
            if (task.uid>0) {
                Comment comment=new Comment();
                comment.id=task.uid;
                Status status=new Status();
                status.text=task.text;
                status.id=Long.valueOf(task.source);
                comment.status=status;
                comment.source="AKWBO";
                comment.text=task.content;
                comment.createdAt=new Date();
                mDataList.add(0, comment);
                mAdapter.notifyDataSetChanged();
                if (!hasInitialed) {
                    hasInitialed=true;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
