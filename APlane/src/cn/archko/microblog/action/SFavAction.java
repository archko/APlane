package cn.archko.microblog.action;

import android.content.Context;
import android.text.TextUtils;
import cn.archko.microblog.R;
import com.me.microblog.WeiboException;
import com.me.microblog.action.Action;
import com.me.microblog.action.ActionResult;
import com.me.microblog.bean.Favorite;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 删除收藏的Action包含了删除收藏与批量操作.
 * 第一个参数mode=0表示是删除单条,第二个参数就是收藏,mode=1或其它表示是批量删除.后面跟着id数组.
 * @author: archko 12-12-13
 */
public class SFavAction implements Action {

    public static final String TAG = "SFavAction";

    @Override
    public ActionResult doAction(Context context, Object... params) {
        Integer mode = (Integer) params[ 0 ];
        ActionResult actionResult = new ActionResult();
        try {
            SinaStatusApi statusApi = new SinaStatusApi();
            statusApi.updateToken();
            if (mode == 0) {
                Long id = (Long) params[ 1 ];
                deleteFavorite(actionResult, id, context, statusApi);
            } else {
                ArrayList<Long> ids = (ArrayList<Long>) params[ 1 ];
                batchDeleteFavorite(actionResult, ids, statusApi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionResult;
    }

    /**
     * 删除一条收藏.
     *
     * @param actionResult
     * @param context
     */
    void deleteFavorite(ActionResult actionResult, Long id, Context context, SinaStatusApi sWeiboApi2) {
        try {
            //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
            if (null == sWeiboApi2) {
                actionResult.resoultCode = ActionResult.ACTION_FALL;
                actionResult.reslutMsg = context.getString(R.string.err_api_error);
            } else {
                WeiboLog.d(TAG, "destroy status:" + id);
                Favorite favorite = sWeiboApi2.destroyFavorite(id);
                if (null != favorite) {
                    actionResult.resoultCode = ActionResult.ACTION_SUCESS;
                    actionResult.obj = favorite;
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
     * 批量删除收藏.
     *
     * @param actionResult
     * @param status
     */
    void batchDeleteFavorite(ActionResult actionResult, ArrayList<Long> ids, SinaStatusApi sWeiboApi2) {
        String deleteIds = TextUtils.join(",", ids);
        WeiboLog.v(TAG, "ids:" + deleteIds);
        try {
            boolean flag = sWeiboApi2.destroyFavorites(deleteIds);
            if (flag) {
                actionResult.resoultCode = ActionResult.ACTION_SUCESS;
                actionResult.obj = ids;
            } else {
                actionResult.resoultCode = ActionResult.ACTION_FALL;
            }
        } catch (WeiboException e) {
            e.printStackTrace();
        }
    }
}
