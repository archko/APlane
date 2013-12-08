package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.util.WeiboLog;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 我的评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaUserFriendsImpl extends AbsStatusImpl<User> {

    public static final String TAG="SinaUserImpl";
    protected int nextCursor=0;//下一页索引，第一页为-1，不是0

    public SinaUserFriendsImpl() {
        //mAbsApi=new SinaUserApi();
    }

    @Override
    public SStatusData<User> loadData(Object... params) throws WeiboException {
        SStatusData<User> sStatusData=null;
        SinaUserApi sWeiboApi2=(SinaUserApi) mAbsApi;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        if (null==sWeiboApi2) {
            sStatusData=new SStatusData<User>();
            sStatusData.errorCode=WeiboException.API_ERROR;
            sStatusData.errorMsg=App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long mUserId=(Long) params[1];
            Long maxId=(Long) params[2];
            Integer c=(Integer) params[3];
            Integer p=(Integer) params[4];
            sStatusData=sWeiboApi2.getFriends(mUserId, nextCursor++, c, 1);
            int nCur=sStatusData.next_cursor;
            WeiboLog.i(TAG, "cur:"+nCur+" nextCur:"+nextCursor);
            nextCursor=nCur;
        }

        return sStatusData;
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        //ArrayList<User> datas=SqliteWrapper.queryAtUsers(resolver, currentUserId, TwitterTable.SStatusUserTbl.TYPE_User);
        SStatusData<User> sStatusData=new SStatusData<User>();
        //sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }

    @Override
    public void saveData(SStatusData<User> statusData) {

    }
}
