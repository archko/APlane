package cn.archko.microblog.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import cn.archko.microblog.fragment.impl.SinaHomeStatusImpl;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:26
 * @description:
 */
public class TestHomeFragment extends AbsBaseListFragment<Status> {

    Group mGroup;
    /**
     * 分组更新了，需要删除所有本地数据，而不是像之前那样还保留旧数据。
     */
    boolean isGroupUpdated = false;

    @Override
    public void initApi() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusImpl = new SinaHomeStatusImpl();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThreadBeanItemView itemView = null;
        Status status = mDataList.get(position);

        boolean updateFlag = true;
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            updateFlag = false;
        }

        if (convertView == null) {
            itemView = new ThreadBeanItemView(getActivity(), mListView, mCacheDir, status, updateFlag, true, showLargeBitmap, showBitmap);
        } else {
            itemView = (ThreadBeanItemView) convertView;
        }
        itemView.update(status, updateFlag, true, showLargeBitmap, showBitmap);

        return itemView;
    }

    @Override
    protected void loadData() {
        if (mDataList != null && mDataList.size() > 0) {
            //setListShown(true);

            //mProgressContainer.setVisibility(View.GONE);
            //mListContainer.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        } else {
            if (! isLoading) {
                loadLocalData();
            } else {
                mEmptyTxt.setText(R.string.list_pre_empty_txt);
                mEmptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    void loadLocalData() {
        if (! isLoading) {
            Object[] params = new Object[]{false, currentUserId};
            newTaskNoNet(params, null);
        }
    }

    @Override
    protected Object[] baseQueryBackgroundOperation(Object... params) throws WeiboException {
        return mStatusImpl.queryData(params);
    }

    protected void saveData(SStatusData<Status> statusData) {
        try {
            ArrayList<Status> newList = statusData.mStatusData;
            SqliteWrapper.saveFriendUser(App.getAppContext(), newList, currentUserId, TwitterTable.UserTbl.TYPE_FRIEND);
            int len = 0;
            List<Status> oldList = SqliteWrapper.queryStatuses(mResolver, currentUserId);
            if (null == oldList) {
                oldList = new ArrayList<Status>();
            }
            if (isGroupUpdated || (null != mGroup && ! mGroup.id.equals(Constants.TAB_ID_HOME))) {
                len = mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID + "=" + currentUserId, null);
                WeiboLog.i(TAG, "分组更新，删除微博记录:" + len);
                oldList.clear();
            }

            len = newList.size();
            if (len >= weibo_count) {
                len = mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID + "=" + currentUserId, null);
                WeiboLog.d(TAG, "新数据足够多，删除微博记录:" + len);
                oldList.clear();
            }

            len = newList.size();
            int oldSize = oldList.size();
            int delta = weibo_count - len;
            WeiboLog.d(TAG, "新数据长度：" + len + " oldSize:" + oldSize + " delta:" + delta);
            if (delta > 0) {
                delta = oldSize - delta;
                if (delta > 0) {
                    oldList = oldList.subList(delta, oldSize);
                }
                WeiboLog.d(TAG, "去除旧数据中多余的：" + oldList.size() + " delta:" + delta);
            }

            oldList.addAll(newList);
            len = mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID + "=" + currentUserId, null);
            WeiboLog.i(TAG, "删除微博记录:" + len);

            len = oldList.size();
            WeiboLog.d(TAG, "当前所有数据长度：" + len);

            ContentValues[] contentValueses = new ContentValues[ len ];
            ContentValues cv;
            Status status;
            Status retStatus;
            for (int i = len - 1; i >= 0; i--) {
                status = oldList.get(i);
                cv = new ContentValues();
                cv.put(TwitterTable.SStatusTbl.STATUS_ID, status.id);
                cv.put(TwitterTable.SStatusTbl.CREATED_AT, status.createdAt.getTime());
                cv.put(TwitterTable.SStatusTbl.TEXT, status.text);
                cv.put(TwitterTable.SStatusTbl.SOURCE, status.source);
                if (! TextUtils.isEmpty(status.thumbnailPic)) {
                    cv.put(TwitterTable.SStatusTbl.PIC_THUMB, status.thumbnailPic);
                    cv.put(TwitterTable.SStatusTbl.PIC_MID, status.bmiddlePic);
                    cv.put(TwitterTable.SStatusTbl.PIC_ORIG, status.originalPic);
                }
                cv.put(TwitterTable.SStatusTbl.R_NUM, status.r_num);
                cv.put(TwitterTable.SStatusTbl.C_NUM, status.c_num);
                cv.put(TwitterTable.SStatusTbl.UID, currentUserId);

                retStatus = status.retweetedStatus;
                if (retStatus != null) {
                    cv.put(TwitterTable.SStatusTbl.R_STATUS_ID, retStatus.id);
                    User user = retStatus.user;
                    if (null != user) {
                        try {
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_USERID, retStatus.user.id);
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_NAME, retStatus.user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cv.put(TwitterTable.SStatusTbl.R_STATUS, retStatus.text);
                    if (! TextUtils.isEmpty(retStatus.thumbnailPic)) {
                        cv.put(TwitterTable.SStatusTbl.R_PIC_THUMB, retStatus.thumbnailPic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_MID, retStatus.bmiddlePic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_ORIG, retStatus.originalPic);
                    }
                }
                cv.put(TwitterTable.SStatusTbl.USER_ID, status.user.id);
                cv.put(TwitterTable.SStatusTbl.USER_SCREEN_NAME, status.user.screenName);
                cv.put(TwitterTable.SStatusTbl.PORTRAIT, status.user.profileImageUrl);
                contentValueses[ i ] = cv;
            }

            len = mResolver.bulkInsert(TwitterTable.SStatusTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存微博记录:" + len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
