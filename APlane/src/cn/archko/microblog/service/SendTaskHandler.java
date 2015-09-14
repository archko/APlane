package cn.archko.microblog.service;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 队列线程
 *
 * @author archko
 */
public class SendTaskHandler extends Handler {

    public static final String TAG="SendTaskHandler";
    private static final long UPLOAD_DELAY=600000L;
    private long mUploadDelay=UPLOAD_DELAY;
    private final WeakReference<SendTaskService> mService;

    public SendTaskHandler(final SendTaskService service, final Looper looper) {
        super(looper);
        mService=new WeakReference<SendTaskService>(service);
    }

    @Override
    public void handleMessage(final Message msg) {
        //WeiboLog.d(TAG, "SendTaskPool:" + msg);
        final SendTaskService service=mService.get();
        if (service==null) {
            return;
        }

        switch (msg.what) {
            case SendTaskService.MSG_START_TASK:
            case SendTaskService.MSG_RESTART_TASK:
            default: {
                upload(msg);
            }
            case SendTaskService.MSG_STOP_TASK: {

            }

            break;
        }
    }

    public void restart() {
        removeMessages(SendTaskService.MSG_RESTART_TASK);
        Message msg=obtainMessage();
        msg.what=SendTaskService.MSG_RESTART_TASK;
        sendMessageDelayed(msg, mUploadDelay);
    }

    private void upload(Message msg) {
        ArrayList<SendTask> mQuery=queryAllTask();
        if (null!=mQuery&&mQuery.size()>0) {
            for (SendTask task : mQuery) {
                doTask_Impl(task);
            }
        }
    }

    /**
     * 处理任务，因为这些都是网络任务，所以需要检测网络的状况。
     *
     * @param task 要造执行的任务
     */
    private void doTask_Impl(SendTask task) {
        App app=(App) App.getAppContext();
        if (app.getOauthBean().oauthType==Oauth2.OAUTH_TYPE_WEB) {
        } else {
            if (System.currentTimeMillis()>=app.getOauthBean().expireTime&&app.getOauthBean().expireTime!=0) {
                WeiboLog.w(TAG, "web认证，token过期了.不能执行任务。");
                return;
            } else {
                WeiboLog.d(TAG, "web认证，但token有效，开始任务。");
            }
        }

        WeiboLog.d(TAG, "执行一个任务。"+task);

        int type=task.type;
        if (type==TwitterTable.SendQueueTbl.SEND_TYPE_STATUS) {
            WeiboLog.d(TAG, "发布的微博 task."+task);
            sendStatus(task);
        } else if (type==TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS) {
            WeiboLog.d(TAG, "转发的微博 task."+task);
            sendRepostStatus(task);
        } else if (type==TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT) {
            WeiboLog.d(TAG, "发布的评论 task."+task);
            sendComment(task);
        } else if (type==TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV) {
            WeiboLog.d(TAG, "添加收藏 task."+task);
            addFavorite(task);
        }

        WeiboLog.d(TAG, "任务完成。");
    }

    private void sendStatus(SendTask task) {
        try {
            String imgUrl=task.imgUrl;
            String content=task.content;
            String data=task.data;
            double latitude=0;
            double longitude=0;
            if (null!=data&&data.contains("-")) {
                String[] datas=data.split("-");
                latitude=Double.valueOf(datas[0]);
                longitude=Double.valueOf(datas[1]);
            }
            String txt=task.text;
            int visible=0;
            if (!TextUtils.isEmpty(txt)||!"null".equals(txt)) {
                visible=Integer.valueOf(txt);
            }

            SinaStatusApi statusApi=new SinaStatusApi();

            Status status=null;
            int code=0;
            String msg="";
            if (!TextUtils.isEmpty(imgUrl)&&!"null".equals(imgUrl)) {
                try {
                    status=statusApi.upload(imgUrl, content, latitude, longitude, visible);
                } catch (WeiboException e) {
                    code=e.getStatusCode();
                    msg=e.toString();
                }
            } else {
                try {
                    status=statusApi.updateStatus(content, latitude, longitude, visible);
                } catch (WeiboException e) {
                    code=e.getStatusCode();
                    msg=e.toString();
                }
            }

            WeiboLog.d(TAG, "发送微博."+status);
            if (null!=status) {
                int res=SqliteWrapper.deleteSendTask(App.getAppContext(), task);
                WeiboLog.d(TAG, "删除完成的任务："+res);
                notifyTaskResult(R.string.new_status_suc);
            } else {
                notifyTaskResult(R.string.new_status_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode=code;
                task.resultMsg=msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRepostStatus(SendTask task) {
        try {
            Status status=null;
            String content=task.content;
            String is_comment=task.data;
            long statusId=Long.valueOf(task.source);

            SinaStatusApi statusApi=new SinaStatusApi();
            int code=0;
            String msg="";
            try {
                status=statusApi.repostStatus(statusId, content, is_comment);
            } catch (WeiboException e) {
                code=e.getStatusCode();
                msg=e.toString();
            }

            WeiboLog.i(TAG, "转发."+status);
            if (null!=status) {
                int res=SqliteWrapper.deleteSendTask(App.getAppContext(), task);
                WeiboLog.d(TAG, "删除完成的任务："+res);
                notifyTaskResult(R.string.repost_suc);
            } else {
                notifyTaskResult(R.string.repost_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode=code;
                task.resultMsg=msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendComment(SendTask task) {
        try {
            Comment comment=null;
            String content=task.content;
            String comment_ori=task.data;
            long statusId=Long.valueOf(task.source);

            int code=0;
            String msg="";
            SinaCommentApi commentApi=new SinaCommentApi();
            try {
                comment=commentApi.commentStatus(statusId, content, comment_ori);
            } catch (WeiboException e) {
                code=e.getStatusCode();
                msg=e.toString();
            }
            WeiboLog.i(TAG, "发送评论."+comment);

            if (null!=comment) {
                int res=SqliteWrapper.deleteSendTask(App.getAppContext(), task);
                WeiboLog.d(TAG, "删除完成的任务："+res);
                notifyTaskResult(R.string.comment_suc);
                task.uid=comment.id;    //在评论成功后，可能值为评论的id。
            } else {
                notifyTaskResult(R.string.comment_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode=code;
                task.resultMsg=msg;
                task.uid=0;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFavorite(SendTask task) {
        try {
            Favorite favorite=null;
            long statusId=Long.valueOf(task.source);

            int code=0;
            String msg="";
            SinaStatusApi statusApi=new SinaStatusApi();
            try {
                favorite=statusApi.createFavorite(statusId);
            } catch (WeiboException e) {
                code=e.getStatusCode();
                msg=e.toString();
            }
            WeiboLog.i(TAG, "添加收藏."+favorite);

            if (null!=favorite) {
                int res=SqliteWrapper.deleteSendTask(App.getAppContext(), task);
                WeiboLog.d(TAG, "删除完成的收藏任务："+res);
                notifyTaskResult(R.string.favorite_add_suc);
            } else {
                notifyTaskResult(R.string.favorite_add_failed);
                SqliteWrapper.updateSendTask(App.getAppContext(), code, msg, task);
                task.resultCode=code;
                task.resultMsg=msg;
            }
            notifyTaskChanged(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyTaskChanged(SendTask task) {
        try {
            Intent i=new Intent(Constants.TASK_CHANGED);
            i.putExtra("task", task);
            i.putExtra("id", task.id);
            App.getAppContext().sendStickyBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void notifyTaskResult(final int msgId) {
        if (null!=mService&&null!=mService.get()) {
            SendTaskService uploadService=mService.get();
            uploadService.doNotify(msgId);
        }
    }

    /**
     * 查询所有的数据库中未执行的任务。不处理其它状态的任务,可以手动执行,以后可以添加自动执行.
     */
    ArrayList<SendTask> queryAllTask() {
        SharedPreferences settings;
        settings=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        long aUserId=settings.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        ArrayList<SendTask> sendTasks=SqliteWrapper.queryAllTasks(App.getAppContext(), String.valueOf(aUserId), SendTask.CODE_INIT);
        WeiboLog.d(TAG, "queryAllTask:"+aUserId+" task count:"+sendTasks.size());
        ArrayList<SendTask> mQuery=new ArrayList<SendTask>();
        for (SendTask task : sendTasks) {
            mQuery.add(task);
        }
        return mQuery;
    }

    public static void addTask(SendTask task) {
        try {
            ContentValues cv=new ContentValues();
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

            Uri uri=App.getAppContext().getContentResolver().insert(TwitterTable.SendQueueTbl.CONTENT_URI, cv);
            task.id=Long.valueOf(uri.getLastPathSegment());
            WeiboLog.d(TAG, "插入新的任务:"+uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
