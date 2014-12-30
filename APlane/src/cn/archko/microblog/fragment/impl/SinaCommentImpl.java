package cn.archko.microblog.fragment.impl;

import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.sina.SinaCommentApi;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaCommentImpl extends AbsStatusImpl<Comment> {

    public static final String TAG = "SinaCommentImpl";
    protected int page = 1;//当前页,这个值不能随便地变，目前来说不需要用到。
    //public Status mStatus=null;
    //protected int weibo_count=15;   //一次显示微博数量

    public SinaCommentImpl() {
        AbsApiImpl absApi = new SinaCommentApi();
        mAbsApi = absApi;
    }

    @Override
    public SStatusData<Comment> loadData(Object... params) throws WeiboException {
        SStatusData<Comment> sStatusData = null;
        //SWeiboApi2 sWeiboApi2=((SWeiboApi2) App.getMicroBlog(App.getAppContext()));
        SinaCommentApi sWeiboApi2 = (SinaCommentApi) mAbsApi;
        if (null == sWeiboApi2) {
            sStatusData = new SStatusData<Comment>();
            sStatusData.errorCode = WeiboException.API_ERROR;
            sStatusData.errorMsg = App.getAppContext().getString(R.string.err_api_error);
        } else {
            Long id = (Long) params[ 1 ];
            Integer c = (Integer) params[ 2 ];
            sStatusData = sWeiboApi2.getComments(id, c, page, - 1, - 1, - 1);
            ArrayList<Comment> list = sStatusData.mStatusData;
            WeiboLog.i(TAG, "list:" + list.size() + " page:" + page);
            page++;
        }

        return sStatusData;
    }

    @Override
    public void saveData(SStatusData<Comment> data) {
    }
}
