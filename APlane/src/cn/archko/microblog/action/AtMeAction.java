package cn.archko.microblog.action;

import android.content.Context;
import cn.archko.microblog.R;
import com.me.microblog.WeiboException;
import com.me.microblog.action.Action;
import com.me.microblog.action.ActionResult;
import com.me.microblog.bean.Status;
import com.me.microblog.core.SinaStatusApi;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 删除微博的Action包含了删除单条与批量操作.
 * 第一个参数mode=0表示是删除单条,第二个参数就是微博,mode=1或其它表示是批量删除.后面跟着id数组.
 * @author: archko 12-12-13
 */
public class AtMeAction implements Action {

    public static final String TAG="AtMeAction";

    @Override
    public ActionResult doAction(Context context, Object... params) {
        Long id=(Long) params[0];
        Integer follow_up=(Integer) params[1];
        ActionResult actionResult=new ActionResult();
        try {
            SinaStatusApi statusApi=new SinaStatusApi();
            statusApi.updateToken();
            mentionsShield(actionResult, id, follow_up, context, statusApi);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionResult;
    }

    /**
     * @param actionResult
     * @param id
     * @param follow_up
     * @param context
     * @param sWeiboApi2
     */
    void mentionsShield(ActionResult actionResult, Long id, Integer follow_up, Context context, SinaStatusApi sWeiboApi2) {
        try {
            if (null==sWeiboApi2) {
                actionResult.resoultCode=ActionResult.ACTION_FALL;
                actionResult.reslutMsg=context.getString(R.string.err_api_error);
            } else {
                WeiboLog.d(TAG, "Shield:"+id);
                boolean rs=sWeiboApi2.mentionsShield(id, follow_up);
                if (rs) {
                    actionResult.resoultCode=ActionResult.ACTION_SUCESS;
                }
            }
        } catch (WeiboException e) {
            int code=e.getStatusCode();
            if (code==200) {
                actionResult.resoultCode=ActionResult.ACTION_SUCESS;
            }
            e.printStackTrace();
            actionResult.reslutMsg=e.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
