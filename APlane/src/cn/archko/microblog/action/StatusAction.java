package cn.archko.microblog.action;

import android.content.Context;
import cn.archko.microblog.R;
import com.me.microblog.WeiboException;
import com.me.microblog.action.Action;
import com.me.microblog.action.ActionResult;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 删除微博的Action包含了删除单条与批量操作.
 * 第一个参数mode=0表示是删除单条,第二个参数就是微博,mode=1或其它表示是批量删除.后面跟着id数组.
 * @author: archko 12-12-13
 */
public class StatusAction implements Action {

    public static final String TAG = "StatusAction";

    @Override
    public ActionResult doAction(Context context, Object... params) {
        Integer mode = (Integer) params[ 0 ];
        ActionResult actionResult = new ActionResult();
        try {
            SinaStatusApi statusApi = new SinaStatusApi();
            statusApi.updateToken();
            if (mode == 0) {
                Long id = (Long) params[ 1 ];
                deleteStatus(actionResult, id, context, statusApi);
                ArrayList<Long> failedIds = new ArrayList<Long>();
                ArrayList<Long> sucIds = new ArrayList<Long>();
                if (actionResult.resoultCode == ActionResult.ACTION_SUCESS) {
                    sucIds.add(id);
                } else {
                    failedIds.add(id);
                }
                actionResult.obj = sucIds;
                actionResult.results = new Object[]{failedIds};
            } else {
                ArrayList<Long> ids = (ArrayList<Long>) params[ 1 ];
                batchDeleteStatus(actionResult, ids, context, statusApi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionResult;
    }

    /**
     * 删除一条微博.
     *
     * @param actionResult
     * @param context
     */
    void deleteStatus(ActionResult actionResult, Long id, Context context, SinaStatusApi sWeiboApi2) {
        try {
            //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
            if (null == sWeiboApi2) {
                actionResult.resoultCode = ActionResult.ACTION_FALL;
                actionResult.reslutMsg = context.getString(R.string.err_api_error);
            } else {
                WeiboLog.d(TAG, "destroy status:" + id);
                Status status = sWeiboApi2.deleteStatus(id);
                if (null != status) {
                    actionResult.resoultCode = ActionResult.ACTION_SUCESS;
                }
            }
        } catch (WeiboException e) {
            int code = e.getStatusCode();
            if (code == 200) {
                actionResult.resoultCode = ActionResult.ACTION_SUCESS;
            }
            e.printStackTrace();
            actionResult.reslutMsg = e.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量删除微博.
     *
     * @param actionResult
     * @param status       要删除的微博,必须是自己发布的.
     */
    void batchDeleteStatus(ActionResult actionResult, ArrayList<Long> ids, Context context, SinaStatusApi sWeiboApi2) {
        ArrayList<Long> failedIds = new ArrayList<Long>();
        ArrayList<Long> sucIds = new ArrayList<Long>();
        ActionResult ar;
        for (Long id : ids) {
            ar = new ActionResult();
            deleteStatus(ar, id, context, sWeiboApi2);
            if (ar.resoultCode == ActionResult.ACTION_FALL) {
                failedIds.add(id);
                actionResult.reslutMsg = ar.reslutMsg;
            } else {
                sucIds.add(id);
            }
        }

        actionResult.obj = sucIds;
        if (failedIds.size() < 1) {
            actionResult.resoultCode = ActionResult.ACTION_SUCESS;
        } else {
            actionResult.results = new Object[]{failedIds};
        }
    }
}
