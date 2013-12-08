package cn.archko.microblog.action;

import android.content.Context;

import cn.archko.microblog.R;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.action.Action;
import com.me.microblog.action.ActionResult;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.sina.SinaGroupApi;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.util.ArrayList;

/**
 * 添加会议成员操作,只能一次一次添加。
 *
 * @author archko
 */
public class GroupAction implements Action {

    public static final String TAG="GroupAction";
    /**
     * 强制加载分组
     */
    boolean forceLoad=false;

    public GroupAction() {
    }

    public GroupAction(boolean forceLoad) {
        this.forceLoad=forceLoad;
    }

    @Override
    public ActionResult doAction(Context context, Object... params) {
        String filepath=(String) params[0];

        ActionResult actionResult=new ActionResult();

        File file=new File(filepath);
        WeiboLog.d(TAG, "loadGroup:"+filepath);
        if (file.exists()&&!forceLoad) {
            ArrayList<Group> groups=WeiboOperation.readLocalData(filepath);
            if (null!=groups) {
                actionResult.resoultCode=ActionResult.ACTION_SUCESS;
                actionResult.obj=groups;
            } else {
                loadGroup(actionResult, filepath, context);
            }
        } else {
            App app=(App) App.getAppContext();
            if (System.currentTimeMillis()>=app.getOauthBean().expireTime&&app.getOauthBean().expireTime!=0) {
                WeiboLog.d(TAG, "不下载分组，token过期了。");
                actionResult.reslutMsg="不下载分组，token过期了。";
            } else {
                loadGroup(actionResult, filepath, context);
            }
        }

        return actionResult;
    }

    /**
     * 从网络下载分组便利店 。
     *
     * @param actionResult
     * @param filepath     分组序列化文件的存储路径。
     * @param context
     */
    void loadGroup(ActionResult actionResult, String filepath, Context context) {
        try {
            SinaGroupApi sWeiboApi2=new SinaGroupApi();
            sWeiboApi2.updateToken();
            SStatusData<Group> sStatusData=null;
            //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
            if (null==sWeiboApi2) {
                sStatusData=new SStatusData<Group>();
                sStatusData.errorCode=WeiboException.API_ERROR;
                sStatusData.errorMsg=context.getString(R.string.err_api_error);
                actionResult.resoultCode=ActionResult.ACTION_FALL;
                actionResult.reslutMsg=context.getString(R.string.err_api_error);
            } else {
                sStatusData=sWeiboApi2.getGroups();
            }

            WeiboLog.d(TAG, "下载分组："+sStatusData);
            ArrayList<Group> groups=sStatusData.mStatusData;
            if (null!=groups&&groups.size()>0) {
                WeiboOperation.writeLocalData(groups, filepath);
                actionResult.obj=groups;
                actionResult.resoultCode=ActionResult.ACTION_SUCESS;
            }
        } catch (WeiboException e) {
            e.printStackTrace();
            actionResult.reslutMsg=e.toString();
        }
    }
}
