package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaGroupApi;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 主页数据获取实现，有查询与网络数据
 */
public class SinaHomeStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaHomeStatusImpl";

    int random=0;
    Group mGroup;
    /**
     * 分组更新了，需要删除所有本地数据，而不是像之前那样还保留旧数据。
     */
    boolean isGroupUpdated=false;
    SinaGroupApi mSinaGroupApi;

    public SinaHomeStatusImpl() {
        /*AbsApiImpl absApi=new SinaStatusApi();
        mAbsApi=absApi;*/
        mSinaGroupApi=new SinaGroupApi();
    }

    public void updateToken() {
        super.updateToken();
        mSinaGroupApi.updateToken();
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        WeiboLog.d(TAG, "loadData.");
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        SStatusData<Status> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            WeiboLog.i("sinceId:"+sinceId+", maxId:"+maxId+", count:"+c+", page:"+p);
            if (null==mGroup||Constants.TAB_ID_HOME.equals(mGroup.id)) {
                WeiboLog.d(TAG, "获取普通数据:"+mGroup);
                if (random==0) {
                    sStatusData=sWeiboApi2.getFriendsTimeline(sinceId, maxId, c, p, -1);
                    random=1;
                } else {
                    sStatusData=sWeiboApi2.getHomeTimeline(sinceId, maxId, c, p, -1);
                    random=0;
                }
            } else {
                WeiboLog.d(TAG, "获取分组数据："+mGroup);
                sStatusData=mSinaGroupApi.getGroupTimeLine(mGroup.id, sinceId, maxId, c, p, -1);
            }
            /*sStatusData=sWeiboApi2.getFriendsTimeline(sinceId, maxId, c, p, -1);*/
            if (null!=sStatusData&&sStatusData.mStatusData!=null&&sStatusData.mStatusData.size()>0) {
                WeiboLog.d(TAG, "微博数:"+sStatusData.mStatusData.size());
                final ArrayList<Status> statuses=sStatusData.mStatusData;
                final ArrayList<Status> removeStatuses=new ArrayList<Status>();
                OauthBean oauthBean=((App) App.getAppContext()).getOauthBean();
                for (Status s : statuses) {
                    if (!s.user.following) {
                        if (!oauthBean.userId.equals(String.valueOf(s.user.id))) {
                            removeStatuses.add(s);
                        }
                    }
                }
                if (removeStatuses.size()>0) {
                    for (Status s : removeStatuses) {
                        statuses.remove(s);
                        WeiboLog.d(TAG, "去除广告d:"+s);
                    }
                }
            }
        }

        return sStatusData;
    }

    @Override
    public void saveData(SStatusData<Status> statusData) {
        try {
            ArrayList<Status> newList=statusData.mStatusData;
            if (null==newList||newList.size()<1) {
                WeiboLog.w(TAG, "no datas");
                return;
            }

            if (null==newList||newList.size()<1) {
                WeiboLog.w(TAG, "no datas");
                return;
            }
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ContentResolver mResolver=App.getAppContext().getContentResolver();

            SqliteWrapper.saveFriendUser(App.getAppContext(), newList, currentUserId, TwitterTable.UserTbl.TYPE_FRIEND);
            int len=0;
            List<Status> oldList=SqliteWrapper.queryStatuses(mResolver, currentUserId);
            if (null==oldList) {
                oldList=new ArrayList<Status>();
            }
            if (isGroupUpdated||(null!=mGroup&&!mGroup.id.equals(Constants.TAB_ID_HOME))) {
                len=mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID+"="+currentUserId, null);
                WeiboLog.i(TAG, "分组更新，删除微博记录:"+len);
                oldList.clear();
            }

            len=newList.size();
            if (len>=25) {
                /*len=mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID+"="+currentUserId, null);
                WeiboLog.d(TAG, "新数据足够多，删除微博记录:"+len);*/
                oldList.clear();
            }

            len=newList.size();
            int oldSize=oldList.size();
            int delta=25-len;
            WeiboLog.d(TAG, "新数据长度："+len+" oldSize:"+oldSize+" delta:"+delta);
            if (delta>0) {
                delta=oldSize-delta;
                if (delta>0) {
                    oldList=oldList.subList(delta, oldSize);
                }
                WeiboLog.d(TAG, "去除旧数据中多余的："+oldList.size()+" delta:"+delta);
            }

            oldList.addAll(newList);
            len=mResolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID+"="+currentUserId, null);
            WeiboLog.i(TAG, "删除微博记录:"+len);

            len=oldList.size();
            WeiboLog.d(TAG, "当前所有数据长度："+len);

            ContentValues[] contentValueses=new ContentValues[len];
            ContentValues cv;
            Status status;
            Status retStatus;
            for (int i=len-1; i>=0; i--) {
                status=oldList.get(i);
                cv=new ContentValues();
                cv.put(TwitterTable.SStatusTbl.STATUS_ID, status.id);
                cv.put(TwitterTable.SStatusTbl.CREATED_AT, status.createdAt.getTime());
                cv.put(TwitterTable.SStatusTbl.TEXT, status.text);
                cv.put(TwitterTable.SStatusTbl.SOURCE, status.source);
                if (!TextUtils.isEmpty(status.thumbnailPic)) {
                    cv.put(TwitterTable.SStatusTbl.PIC_THUMB, status.thumbnailPic);
                    cv.put(TwitterTable.SStatusTbl.PIC_MID, status.bmiddlePic);
                    cv.put(TwitterTable.SStatusTbl.PIC_ORIG, status.originalPic);
                }
                cv.put(TwitterTable.SStatusTbl.R_NUM, status.r_num);
                cv.put(TwitterTable.SStatusTbl.C_NUM, status.c_num);
                cv.put(TwitterTable.SStatusTbl.UID, currentUserId);

                retStatus=status.retweetedStatus;
                if (retStatus!=null) {
                    cv.put(TwitterTable.SStatusTbl.R_STATUS_ID, retStatus.id);
                    User user=retStatus.user;
                    if (null!=user) {
                        try {
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_USERID, retStatus.user.id);
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_NAME, retStatus.user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cv.put(TwitterTable.SStatusTbl.R_STATUS, retStatus.text);
                    if (!TextUtils.isEmpty(retStatus.thumbnailPic)) {
                        cv.put(TwitterTable.SStatusTbl.R_PIC_THUMB, retStatus.thumbnailPic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_MID, retStatus.bmiddlePic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_ORIG, retStatus.originalPic);
                    }
                }

                String[] thumbs=status.thumbs;
                if (null==thumbs||thumbs.length==0) {
                    if (null!=retStatus) {
                        thumbs=retStatus.thumbs;
                    }
                }
                if (null!=thumbs&&thumbs.length>0) {
                    StringBuilder sb=new StringBuilder();
                    for (String s : thumbs) {
                        sb.append(s).append(",");
                    }
                    sb.setLength(sb.length()-1);
                    //WeiboLog.d(TAG, "thumbs:"+sb.toString());
                    cv.put(TwitterTable.SStatusTbl.PIC_URLS, sb.toString());
                }
                cv.put(TwitterTable.SStatusTbl.USER_ID, status.user.id);
                cv.put(TwitterTable.SStatusTbl.USER_SCREEN_NAME, status.user.screenName);
                cv.put(TwitterTable.SStatusTbl.PORTRAIT, status.user.profileImageUrl);
                contentValueses[i]=cv;
            }

            len=mResolver.bulkInsert(TwitterTable.SStatusTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存微博记录:"+len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        ArrayList<Status> datas=SqliteWrapper.queryStatuses(resolver, currentUserId);
        SStatusData<Status> sStatusData=new SStatusData<Status>();
        sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }

    public void updateGroup(Group group, boolean groupUpdated) {
        mGroup=group;
        isGroupUpdated=groupUpdated;
    }
}
