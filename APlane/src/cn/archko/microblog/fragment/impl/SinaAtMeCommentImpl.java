package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.SinaCommentApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: at我的评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaAtMeCommentImpl extends AbsStatusImpl<Comment> {

    public static final String TAG="SinaAtMeCommentImpl";

    public SinaAtMeCommentImpl() {
        /*AbsApiImpl absApi=new SinaCommentApi();
        mAbsApi=absApi;*/
    }

    @Override
    public SStatusData<Comment> loadData(Object... params) throws WeiboException {
        SStatusData<Comment> sStatusData=null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        SinaCommentApi sWeiboApi2=(SinaCommentApi) mAbsApi;
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<Comment>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long sinceId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            WeiboLog.d(TAG, "更新@我的评论："+c);
            sStatusData=sWeiboApi2.getAtMeComments(sinceId, maxId, c, p, 0, 0);
        }

        return sStatusData;
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        ArrayList<Comment> datas=SqliteWrapper.queryAtComments(resolver, currentUserId, TwitterTable.SStatusCommentTbl.TYPE_AT_COMMENT);
        SStatusData<Comment> sStatusData=new SStatusData<Comment>();
        sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }

    @Override
    public void saveData(SStatusData<Comment> statusData) {
        try {
            int weibo_count=25;
            ArrayList<Comment> newList=statusData.mStatusData;
            if (null==newList||newList.size()<1) {
                WeiboLog.w(TAG, "no datas");
                return;
            }
            SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            long currentUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
            ContentResolver mResolver=App.getAppContext().getContentResolver();
            int len=0;
            List<Comment> oldList;
            if (newList.size()<weibo_count) {
                oldList=SqliteWrapper.queryAtComments(mResolver, currentUserId, TwitterTable.SStatusCommentTbl.TYPE_AT_COMMENT);
                if (null==oldList) {
                    oldList=new ArrayList<Comment>();
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
                        TwitterTable.SStatusCommentTbl.TYPE+"="+TwitterTable.SStatusCommentTbl.TYPE_AT_COMMENT, null);
                WeiboLog.i(TAG, "删除微博记录:"+len);
            } else {
                mResolver.delete(TwitterTable.SStatusCommentTbl.CONTENT_URI,
                    TwitterTable.SStatusCommentTbl.UID+"="+currentUserId+" and "+
                        TwitterTable.SStatusCommentTbl.TYPE+"="+TwitterTable.SStatusCommentTbl.TYPE_AT_COMMENT, null);
                WeiboLog.d(TAG, "新数据足够多，删除微博记录:"+len);
                oldList=newList;
            }

            len=oldList.size();
            WeiboLog.d(TAG, "当前所有数据长度："+len);

            ContentValues[] contentValueses=new ContentValues[len];
            ContentValues cv;
            Comment comment;
            Comment replyComment;
            Status status;
            User user;
            for (int i=len-1; i>=0; i--) {
                comment=oldList.get(i);
                cv=new ContentValues();
                cv.put(TwitterTable.SStatusCommentTbl.STATUS_ID, comment.id);
                cv.put(TwitterTable.SStatusCommentTbl.CREATED_AT, comment.createdAt.getTime());
                cv.put(TwitterTable.SStatusCommentTbl.TEXT, comment.text);
                cv.put(TwitterTable.SStatusCommentTbl.SOURCE, comment.source);
                cv.put(TwitterTable.SStatusCommentTbl.UID, currentUserId);
                cv.put(TwitterTable.SStatusCommentTbl.TYPE, TwitterTable.SStatusCommentTbl.TYPE_AT_COMMENT);

                cv.put(TwitterTable.SStatusCommentTbl.USER_ID, comment.user.id);
                cv.put(TwitterTable.SStatusCommentTbl.USER_SCREEN_NAME, comment.user.screenName);
                cv.put(TwitterTable.SStatusCommentTbl.PORTRAIT, comment.user.profileImageUrl);

                status=comment.status;
                if (status!=null) {
                    cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_ID, status.id);

                    if (!TextUtils.isEmpty(status.thumbnailPic)) {
                        cv.put(TwitterTable.SStatusCommentTbl.PIC_THUMB, status.thumbnailPic);
                        cv.put(TwitterTable.SStatusCommentTbl.PIC_MID, status.bmiddlePic);
                        cv.put(TwitterTable.SStatusCommentTbl.PIC_ORIG, status.originalPic);
                    }
                    cv.put(TwitterTable.SStatusCommentTbl.R_NUM, status.r_num);
                    cv.put(TwitterTable.SStatusCommentTbl.C_NUM, status.c_num);

                    user=status.user;
                    if (null!=user) {
                        try {
                            cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_USERID, status.user.id);
                            cv.put(TwitterTable.SStatusCommentTbl.R_STATUS_NAME, status.user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String[] thumbs=status.thumbs;
                    if (null!=thumbs&&thumbs.length>0) {
                        StringBuilder sb=new StringBuilder();
                        for (String s : thumbs) {
                            sb.append(s).append(",");
                        }
                        sb.setLength(sb.length()-1);
                        cv.put(TwitterTable.SStatusTbl.PIC_URLS, sb.toString());
                    }
                    cv.put(TwitterTable.SStatusCommentTbl.R_STATUS, status.text);
                }

                replyComment=comment.replyComment;
                if (null!=replyComment) {
                    cv.put(TwitterTable.SStatusCommentTbl.R_PIC_THUMB, replyComment.text);
                    user=replyComment.user;
                    if (null!=user) {
                        try {
                            cv.put(TwitterTable.SStatusCommentTbl.R_PIC_MID, user.id);
                            cv.put(TwitterTable.SStatusCommentTbl.R_PIC_ORIG, user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                contentValueses[i]=cv;
            }

            len=mResolver.bulkInsert(TwitterTable.SStatusCommentTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存@我的评论记录:"+len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
