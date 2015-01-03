package cn.archko.microblog.controller;

import android.app.Activity;
import android.view.View;
import cn.archko.microblog.listeners.IAKWeiboController;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;

/**
 * 处理菜单控制器.
 *
 * @author: archko 15/1/3 :15:15
 */
public class AKBaseWeiboController implements IAKWeiboController {

    /**
     * 查看Status原文信息,包括评论.
     */
    public void viewOriginalStatus(View achor, Object status, Activity activity) {
    }

    /**
     * 创建收藏.
     */
    public void createFavorite(Status status, long currentUserId, int type, Activity activity) {
    }

    /**
     * 跳转到到评论界面
     */
    public void commentStatus(Status status, Activity activity) {
    }

    /**
     * 到转发界面
     */
    public void repostStatus(Status status, Activity activity) {
    }

    /**
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    public void viewStatusUser(Status status, Activity activity, int type) {
    }

    public void viewUser(User status, Activity activity, int type) {
    }

    /**
     * 快速转发
     */
    public void quickRepostStatus(Status status, long currentUserId, Activity activity) {
        //throw new IllegalArgumentException("not implemented!");
    }

}
