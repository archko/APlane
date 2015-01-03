package cn.archko.microblog.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.util.Date;

/**
 * 微博的转发,查看等操作控制器,只处理新浪微博的.应该也不会有很多种类的.
 *
 * @author: archko 15/1/3 :15:19
 */
public class AKSinaWeiboController extends AKBaseWeiboController {

    private static final String TAG="AKSinaWeiboController";

    @Override
    public void viewOriginalStatus(View achor, Object status, Activity activity) {
        if (null!=status) {
            try {
                WeiboOperation.toViewOriginalStatus(activity, (Status) status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createFavorite(Status status, long currentUserId, int type, Activity activity) {
        if (null!=status) {
                /*String type="0";
                Long statusId=status.id;
                OperationTask task=new OperationTask();
                task.execute(new Object[]{type, statusId});*/
            Intent taskService=new Intent(activity, SendTaskService.class);
            SendTask task=new SendTask();
            task.uid=currentUserId;
            task.userId=currentUserId;
            task.content=status.text;
            task.source=String.valueOf(status.id);
            task.type=type;
            task.createAt=new Date().getTime();
            taskService.putExtra("send_task", task);
            activity.startService(taskService);
            NotifyUtils.showToast("新收藏任务添加到队列服务中了。");
        }
    }

    @Override
    public void commentStatus(Status status, Activity activity) {
        if (null!=status) {
            WeiboOperation.toCommentStatus(activity, status);
        }
    }

    @Override
    public void repostStatus(Status status, Activity activity) {
        if (null!=status) {
            WeiboOperation.toRepostStatus(activity, status);
        }
    }

    @Override
    public void viewStatusUser(Status status, Activity activity, int type) {
        if (null!=status) {
            User user=status.user;
            WeiboOperation.toViewStatusUser(activity, user, type);
        }
    }

    public void viewUser(User user, Activity activity, int type) {
        if (null!=user) {
            WeiboOperation.toViewStatusUser(activity, user, type);
        }
    }

    @Override
    public void quickRepostStatus(Status status, long currentUserId, Activity activity) {
        if (null!=status) {
            //WeiboOperation.quickRepostStatus(status.id);
            Intent taskService=new Intent(activity, SendTaskService.class);
            SendTask task=new SendTask();
            task.uid=currentUserId;
            task.userId=currentUserId;
            task.content="";
            task.source=String.valueOf(status.id);
            task.data="0";
            task.type=TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
            task.text=status.text;
            task.createAt=new Date().getTime();
            taskService.putExtra("send_task", task);
            activity.startService(taskService);
            NotifyUtils.showToast("转发任务添加到队列服务中了。");
        }
    }
}
