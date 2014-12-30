package com.me.microblog.thread;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.Favorite;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaCommentApi;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * 队列线程
 *
 * @author archko
 */
public class SendTaskPool extends Thread {

    public static final String TAG = "SendTaskPool";
    private ArrayList<SendTask> mQuery;
    Context mContext;
    private boolean isStop = false;
    Handler mHandler;

    public SendTaskPool(Context context) {
        this.mQuery = new ArrayList<SendTask>();
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        queryAllTask();
    }

    /**
     * 停止所有任务
     *
     * @param stop
     */
    public void setStop(boolean stop) {
        isStop = stop;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * 处理任务，因为这些都是网络任务，所以需要检测网络的状况。
     *
     * @param task 要造执行的任务
     */
    private void doTask_Impl(SendTask task) {
        synchronized (this) {
            if (! App.hasInternetConnection(mContext)) {
                WeiboLog.d(TAG, "no internet connection,failed to execute task.");
                return;
            }

            App app = (App) App.getAppContext();
            if (app.getOauthBean().oauthType == Oauth2.OAUTH_TYPE_WEB) {
            } else {
                if (System.currentTimeMillis() >= app.getOauthBean().expireTime && app.getOauthBean().expireTime != 0) {
                    WeiboLog.w(TAG, "web认证，token过期了.不能执行任务。");
                    return;
                } else {
                    WeiboLog.d(TAG, "web认证，但token有效，开始任务。");
                }
            }

            WeiboLog.d(TAG, "执行一个任务。" + task);

            int type = task.type;
            if (type == TwitterTable.SendQueueTbl.SEND_TYPE_STATUS) {
                WeiboLog.d(TAG, "发布的微博 task." + task);
                sendStatus(task);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS) {
                WeiboLog.d(TAG, "转发的微博 task." + task);
                sendRepostStatus(task);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT) {
                WeiboLog.d(TAG, "发布的评论 task." + task);
                sendComment(task);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV) {
                WeiboLog.d(TAG, "添加收藏 task." + task);
                addFavorite(task);
            }

            WeiboLog.d(TAG, "任务完成。");
            notifyAll();
        }
    }

    private void sendStatus(SendTask task) {
        try {
            String imgUrl = task.imgUrl;
            String content = task.content;
            String data = task.data;
            String[] datas = data.split("-");
            double latitude = Double.valueOf(datas[ 0 ]);
            double longitude = Double.valueOf(datas[ 1 ]);
            String txt = task.text;
            int visible = 0;
            if (! TextUtils.isEmpty(txt) || ! "null".equals(txt)) {
                visible = Integer.valueOf(txt);
            }

            SinaStatusApi statusApi = new SinaStatusApi();

            Status status = null;
            int code = 0;
            String msg = "";
            if (! TextUtils.isEmpty(imgUrl) && ! "null".equals(imgUrl)) {
                try {
                    status = statusApi.upload(imgUrl, content, latitude, longitude, visible);
                } catch (WeiboException e) {
                    code = e.getStatusCode();
                    msg = e.toString();
                }
            } else {
                try {
                    status = statusApi.updateStatus(content, latitude, longitude, visible);
                } catch (WeiboException e) {
                    code = e.getStatusCode();
                    msg = e.toString();
                }
            }

            WeiboLog.d(TAG, "发送微博." + status);
            if (null != status) {
                int res = SqliteWrapper.deleteSendTask(mContext, task);
                //mContext.getContentResolver().delete(TwitterTable.SendQueueTbl.CONTENT_URI, " _id='"+task.id+"'", null);
                WeiboLog.d(TAG, "删除完成的任务：" + res);
                showToast(R.string.new_status_suc);
            } else {
                showToast(R.string.new_status_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode = code;
                task.resultMsg = msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRepostStatus(SendTask task) {
        try {
            Status status = null;
            String content = task.content;
            String is_comment = task.data;
            long statusId = Long.valueOf(task.source);

            SinaStatusApi statusApi = new SinaStatusApi();
            int code = 0;
            String msg = "";
            try {
                status = statusApi.repostStatus(statusId, content, is_comment);
            } catch (WeiboException e) {
                code = e.getStatusCode();
                msg = e.toString();
            }

            WeiboLog.i(TAG, "转发." + status);
            if (null != status) {
                int res = SqliteWrapper.deleteSendTask(mContext, task);
                //mContext.getContentResolver().delete(TwitterTable.SendQueueTbl.CONTENT_URI, " _id='"+task.id+"'", null);
                WeiboLog.d(TAG, "删除完成的任务：" + res);
                showToast(R.string.repost_suc);
            } else {
                showToast(R.string.repost_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode = code;
                task.resultMsg = msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendComment(SendTask task) {
        try {
            Comment comment = null;
            String content = task.content;
            String comment_ori = task.data;
            long statusId = Long.valueOf(task.source);

            int code = 0;
            String msg = "";
            SinaCommentApi commentApi = new SinaCommentApi();
            try {
                comment = commentApi.commentStatus(statusId, content, comment_ori);
            } catch (WeiboException e) {
                code = e.getStatusCode();
                msg = e.toString();
            }
            WeiboLog.i(TAG, "发送评论." + comment);

            if (null != comment) {
                int res = SqliteWrapper.deleteSendTask(mContext, task);
                //mContext.getContentResolver().delete(TwitterTable.SendQueueTbl.CONTENT_URI, " _id='"+task.id+"'", null);
                WeiboLog.d(TAG, "删除完成的任务：" + res);
                showToast(R.string.comment_suc);
                task.uid = comment.id;    //在评论成功后，可能值为评论的id。
            } else {
                showToast(R.string.comment_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode = code;
                task.resultMsg = msg;
                task.uid = 0;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFavorite(SendTask task) {
        try {
            Favorite favorite = null;
            long statusId = Long.valueOf(task.source);

            int code = 0;
            String msg = "";
            SinaStatusApi statusApi = new SinaStatusApi();
            try {
                favorite = statusApi.createFavorite(statusId);
            } catch (WeiboException e) {
                code = e.getStatusCode();
                msg = e.toString();
            }
            WeiboLog.i(TAG, "添加收藏." + favorite);

            if (null != favorite) {
                int res = SqliteWrapper.deleteSendTask(mContext, task);
                WeiboLog.d(TAG, "删除完成的收藏任务：" + res);
                showToast(R.string.favorite_add_suc);
            } else {
                showToast(R.string.favorite_add_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode = code;
                task.resultMsg = msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyTaskChanged(SendTask task) {
        try {
            Intent i = new Intent(Constants.TASK_CHANGED);
            i.putExtra("task", task);
            i.putExtra("id", task.id);
            mContext.sendStickyBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showToast(final int msg) {
        try {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doShowToast(msg);
                }

                private void doShowToast(int msg) {
                    Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }, 0l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SendTask Get(int paramInt) {
        synchronized (this) {
            int size = this.mQuery.size();
            if (paramInt <= size) {
                return mQuery.get(paramInt);
            }
        }
        return null;
    }

    public int GetCount() {
        synchronized (this) {
            return mQuery.size();
        }
    }

    public SendTask Pop() {
        synchronized (this) {
            SendTask task = this.mQuery.remove(0);
            return task;
        }
    }

    /**
     * 添加队列任务对象
     *
     * @param task 待处理的任务，
     */
    public void Push(SendTask task) {
        synchronized (this) {
            addTask(task);
            notifyAll();
        }
    }

    /**
     * 添加队列任务对象
     *
     * @param task 待处理的任务，
     */
    public void restart(SendTask task) {
        synchronized (this) {
            mQuery.add(task);
            notifyAll();
        }
    }

    private void addTask(SendTask task) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TwitterTable.SendQueueTbl.USER_ID, task.userId);
            cv.put(TwitterTable.SendQueueTbl.CONTENT, task.content);
            cv.put(TwitterTable.SendQueueTbl.IMG_URL, task.imgUrl);
            cv.put(TwitterTable.SendQueueTbl.CREATED_AT, task.createAt);
            cv.put(TwitterTable.SendQueueTbl.TEXT, task.text);
            cv.put(TwitterTable.SendQueueTbl.SOURCE, task.source);
            cv.put(TwitterTable.SendQueueTbl.DATA, task.data);
            //cv.put(TwitterTable.SendQueueTbl.UID, task.uid);
            cv.put(TwitterTable.SendQueueTbl.TYPE, task.type);
            cv.put(TwitterTable.SendQueueTbl.SEND_RESULT_CODE, 0);

            Uri uri = mContext.getContentResolver().insert(TwitterTable.SendQueueTbl.CONTENT_URI, cv);
            task.id = Long.valueOf(uri.getLastPathSegment());
            WeiboLog.d(TAG, "插入新的任务:" + uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mQuery.add(task);
    }

    /**
     * 删除队列任务对象
     *
     * @param task 待处理的任务，
     */
    public void delete(SendTask task) {
        synchronized (this) {
            deleteTask(task);
            notifyAll();
        }
    }

    private void deleteTask(SendTask task) {
        for (SendTask sendTask : mQuery) {
            if (task.id == sendTask.id) {
                mQuery.remove(sendTask);
                break;
            }
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WeiboLog.d(TAG, "start task.");
        while (true) {
            synchronized (this) {
                if (isStop) {
                    WeiboLog.d(TAG, "threadpool stop.");
                    break;
                }

                notifyAll();
                if (GetCount() != 0) {
                    SendTask piece = Pop();
                    doTask_Impl(piece);
                } else {
                    try {
                        WeiboLog.d(TAG, "任务等待中。");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 查询所有的数据库中未执行的任务。
     */
    void queryAllTask() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        long aUserId = settings.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
        ArrayList<SendTask> sendTasks = SqliteWrapper.queryAllTasks(mContext, String.valueOf(aUserId), 0);
        WeiboLog.d(TAG, "queryAllTask:" + aUserId + " task count:" + sendTasks.size());

        synchronized (this) {
            for (SendTask task : sendTasks) {
                if (! mQuery.contains(task)) {
                    mQuery.add(task);
                }
            }
            notifyAll();
        }
    }
}
