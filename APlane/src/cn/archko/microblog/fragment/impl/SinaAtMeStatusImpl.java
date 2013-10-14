package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.SinaStatusApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 随便看看数据获取实现
 */
public class SinaAtMeStatusImpl extends AbsStatusImpl<Status> {

    public static final String TAG="SinaAtMeStatusImpl";

    public SinaAtMeStatusImpl() {
        mAbsApi=new SinaStatusApi();
    }

    @Override
    public SStatusData<Status> loadData(Object... params) throws WeiboException {
        WeiboLog.d(TAG, "loadData.");
        SStatusData<Status> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        SinaStatusApi sWeiboApi2=(SinaStatusApi) mAbsApi;
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Status>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            sStatusData=sWeiboApi2.getMentions(sinceId, maxId, c, p, -1, -1, -1);
        }

        return sStatusData;
    }

    public Object[] queryData(Object... params) throws WeiboException {
        try {
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ContentResolver resolver=App.getAppContext().getContentResolver();
            ArrayList<Status> objects=SqliteWrapper.queryAtStatuses(resolver, currentUserId);
            SStatusData<Status> sStatusData=new SStatusData<Status>();
            sStatusData.mStatusData=objects;
            return new Object[]{sStatusData, params};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveData(SStatusData<Status> data) {
        try {
            int weibo_count=25;
            ArrayList<Status> newList=data.mStatusData;
            if (null==newList||newList.size()<1) {
                WeiboLog.w(TAG, "no datas");
                return;
            }
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ContentResolver mResolver=App.getAppContext().getContentResolver();
            int len=0;
            List<Status> oldList;
            if (newList.size()<weibo_count) {
                oldList=SqliteWrapper.queryAtStatuses(mResolver, currentUserId);
                if (null==oldList) {
                    oldList=new ArrayList<Status>();
                }

                len=newList.size();
                int oldSize=oldList.size();
                int delta=50-len;
                WeiboLog.d(TAG, "新数据长度："+len+" oldSize:"+oldSize+" delta:"+delta);
                if (delta>0) {
                    delta=oldSize-delta;
                    if (delta>0) {
                        oldList=oldList.subList(delta, oldSize);
                    }
                    WeiboLog.d(TAG, "去除旧数据中多余的："+oldList.size()+" delta:"+delta);
                }

                oldList.addAll(newList);
                len=mResolver.delete(TwitterTable.SStatusCommentTbl.CONTENT_URI,
                    TwitterTable.SStatusCommentTbl.UID+"="+currentUserId+" and "+
                        TwitterTable.SStatusCommentTbl.TYPE+"="+TwitterTable.SStatusCommentTbl.TYPE_STATUT, null);
                WeiboLog.i(TAG, "删除微博记录:"+len);

            } else {
                mResolver.delete(TwitterTable.SStatusCommentTbl.CONTENT_URI,
                    TwitterTable.SStatusCommentTbl.UID+"="+currentUserId+" and "+
                        TwitterTable.SStatusCommentTbl.TYPE+"="+TwitterTable.SStatusCommentTbl.TYPE_STATUT, null);
                WeiboLog.d(TAG, "新数据足够多，删除微博记录:"+len);
                oldList=newList;
            }
            len=oldList.size();
            WeiboLog.d(TAG, "当前所有数据长度："+len);

            ContentValues[] contentValueses=new ContentValues[len];
            ContentValues cv;
            Status status;
            Status retStatus;
            for (int i=len-1; i>=0; i--) {
                status=oldList.get(i);
                cv=new ContentValues();
                cv.put(TwitterTable.SStatusCommentTbl.STATUS_ID, status.id);
                cv.put(TwitterTable.SStatusCommentTbl.CREATED_AT, status.createdAt.getTime());
                cv.put(TwitterTable.SStatusCommentTbl.TEXT, status.text);
                cv.put(TwitterTable.SStatusCommentTbl.SOURCE, status.source);
                if (!TextUtils.isEmpty(status.thumbnailPic)) {
                    cv.put(TwitterTable.SStatusCommentTbl.PIC_THUMB, status.thumbnailPic);
                    cv.put(TwitterTable.SStatusCommentTbl.PIC_MID, status.bmiddlePic);
                    cv.put(TwitterTable.SStatusCommentTbl.PIC_ORIG, status.originalPic);
                }
                cv.put(TwitterTable.SStatusCommentTbl.R_NUM, status.r_num);
                cv.put(TwitterTable.SStatusCommentTbl.C_NUM, status.c_num);
                cv.put(TwitterTable.SStatusCommentTbl.UID, currentUserId);
                cv.put(TwitterTable.SStatusCommentTbl.TYPE, TwitterTable.SStatusCommentTbl.TYPE_STATUT);

                retStatus=status.retweetedStatus;
                if (retStatus!=null) {
                    cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_ID, retStatus.id);
                    User user=retStatus.user;
                    if (null!=user) {
                        try {
                            cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_USERID, retStatus.user.id);
                            cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_NAME, retStatus.user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cv.put(TwitterTable.SStatusCommentTbl.R_STATUS, retStatus.text);
                    if (!TextUtils.isEmpty(retStatus.thumbnailPic)) {
                        cv.put(TwitterTable.SStatusCommentTbl.R_PIC_THUMB, retStatus.thumbnailPic);
                        cv.put(TwitterTable.SStatusCommentTbl.R_PIC_MID, retStatus.bmiddlePic);
                        cv.put(TwitterTable.SStatusCommentTbl.R_PIC_ORIG, retStatus.originalPic);
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
                    cv.put(TwitterTable.SStatusTbl.PIC_URLS, sb.toString());
                }
                cv.put(TwitterTable.SStatusCommentTbl.USER_ID, status.user.id);
                cv.put(TwitterTable.SStatusCommentTbl.USER_SCREEN_NAME, status.user.screenName);
                cv.put(TwitterTable.SStatusCommentTbl.PORTRAIT, status.user.profileImageUrl);
                contentValueses[i]=cv;
            }

            len=mResolver.bulkInsert(TwitterTable.SStatusCommentTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存微博记录:"+len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
