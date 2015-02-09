package cn.archko.microblog.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.AccountUsersFragment;
import cn.archko.microblog.ui.CommentStatusActivity;
import cn.archko.microblog.ui.EmptyFragmentActivity;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.RepostStatusActivity;
import cn.archko.microblog.ui.SplashActivity;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.ui.ViewStatusDetailActivity;
import cn.archko.microblog.ui.WebviewActivity;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaStatusApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.PinYin;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * 处理微博的一些常用操作，如跳转到转发，回复等。后期考虑把所有这些操作都放到这里，
 * 但因为涉及到线程可能会有一些麻烦 。
 * User: archko Date: 12-7-10 Time: 上午8:39
 */
public class WeiboOperation {

    /**
     * 跳转到转发页面
     *
     * @param context
     * @param status  要处理的微博
     */
    public static void toRepostStatus(Context context, Status status) {
        try {
            Intent intent=new Intent();
            intent.setClass(context, RepostStatusActivity.class);
            if (null!=status) {
                status.mStatusSpannable=null;
                status.mRetweetedSpannable=null;
                intent.putExtra("status", status);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到转发页面
     *
     * @param context
     * @param status  要处理的微博
     */
    public static void quickRepostStatus(long id) {
        try {
            RepostTask task=new RepostTask();
            task.execute(new Object[]{"", 0, id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到微博的原文
     *
     * @param context
     * @param status  要处理的微博
     */
    public static void toViewOriginalStatus(Context context, Status status) {
        try {
            Intent intent=new Intent();
            intent.setClass(context, ViewStatusDetailActivity.class);
            if (null!=status) {
                status.mStatusSpannable=null;
                status.mRetweetedSpannable=null;
                intent.putExtra("status", status);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);   //只动画新的,从右到左.
                //((Activity)context).overridePendingTransition(R.anim.enter_left, R.anim.enter_right);//连续的动画.两个页面从右到左
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toViewOriginalStatus(Context context, Status status, boolean refresh) {
        try {
            Intent intent=new Intent();
            intent.setClass(context, ViewStatusDetailActivity.class);
            if (null!=status) {
                status.mStatusSpannable=null;
                status.mRetweetedSpannable=null;
                intent.putExtra("status", status);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("refresh", refresh);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面
     *
     * @param context
     * @param status  要处理的微博
     */
    public static void toCommentStatus(Context context, Status status) {
        try {
            Intent intent=new Intent();
            intent.setClass(context, CommentStatusActivity.class);
            if (null!=status) {
                status.mStatusSpannable=null;
                status.mRetweetedSpannable=null;
                intent.putExtra("status", status);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到查看用户信息
     *
     * @param context
     * @param user    要处理的用户
     */
    public static void toViewStatusUser(Context context, User user, int type) {
        try {
            if (null!=user) {
                Intent intent=new Intent(context, UserFragmentActivity.class);
                intent.putExtra("nickName", user.screenName);
                intent.putExtra("user_id", user.id);
                intent.putExtra("user", user);
                intent.putExtra("type", type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);

                AtUser atUser=new AtUser();
                atUser.name=user.screenName;
                atUser.id=user.id;
                atUser.pinyin=PinYin.getPinYin(user.screenName);
                AddAtUserThread addAtUserThread=new AddAtUserThread(atUser);
                addAtUserThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到查看用户信息
     *
     * @param context
     * @param user    要处理的用户
     */
    public static void toViewStatusUser(Context context, String screenName, long id, int type) {
        try {
            Intent intent=new Intent(context, UserFragmentActivity.class);
            intent.putExtra("nickName", screenName);
            intent.putExtra("type", type);
            intent.putExtra("user_id", id);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);

            AtUser atUser=new AtUser();
            atUser.name=screenName;
            atUser.id=id;
            atUser.pinyin=PinYin.getPinYin(screenName);
            AddAtUserThread addAtUserThread=new AddAtUserThread(atUser);
            addAtUserThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到@用户,发新微博.
     *
     * @param context
     * @param user    要处理的用户
     */
    public static void toAtUser(Context context, String atString) {
        try {
            if (!atString.startsWith("@")) {
                atString="@"+atString;
            }
            Intent intent=new Intent(context, NewStatusActivity.class);
            intent.putExtra("at_some", atString);
            intent.putExtra("user_id", 100);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Constants.INTENT_NEW_BLOG);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建收藏.
     */
    public static void createFavorite(final Context context, Status status, Handler handler) {
        try {
            if (null!=status) {
                String type="0";
                Long statusId=status.id;
                OperaThread operaThread=new OperaThread(context, handler, new Object[]{type, statusId});
                //task.execute(new Object[]{type, statusId});
                operaThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startNewHome(final Activity activity) {
        Intent intent=new Intent(activity, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void startAccountActivity(Activity activity) {
        Intent loginIntent=new Intent(activity, EmptyFragmentActivity.class);
        loginIntent.putExtra("title", "添加帐户");
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.putExtra("fragment_class", AccountUsersFragment.class.getName());
        loginIntent.putExtra("mode", "1");
        activity.startActivity(loginIntent);
        activity.overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
    }

    public static void startWebview(Activity activity, String name) {
        Intent intent=new Intent(activity, WebviewActivity.class);
        intent.putExtra("url", name);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
    }

    /**
     * 暂时处理收藏，不处理其它操作。
     */
    static class OperaThread extends Thread {

        Context mContext;
        Handler mHandler;
        Object params;

        OperaThread(Context context, Handler handler, Object[] objects) {
            mContext=context;
            mHandler=handler;
            params=objects;
        }

        @Override
        public void run() {
            preOperation();
            Object result=null;
            try {
                result=backgroundTrans(0);
            } catch (WeiboException e) {
                e.printStackTrace();
            } finally {
                postOperation(result);
            }
        }

        /**
         * 操作前的行为
         */
        void preOperation() {
        }

        /**
         * 操作后的行为。
         */
        void postOperation(Object result) {
            if (result==null) {
                WeiboLog.i("createFavorite failed!");
            } else {
                final com.me.microblog.bean.Status status=(com.me.microblog.bean.Status) result;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "收藏成功! "+status.text, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        /**
         * 后台线程处理的事务
         *
         * @return
         */
        com.me.microblog.bean.Status backgroundTrans(long sid) throws WeiboException {
            /*SWeiboApi2 weiboApi2=(SWeiboApi2) App.getMicroBlog(mContext);
            if (null==weiboApi2) {
                return null;
            }*/
            SinaStatusApi weiboApi2=new SinaStatusApi();
            weiboApi2.updateToken();
            return weiboApi2.createFavorite(sid).mStatus;
        }
    }

    static class RepostTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object[] doInBackground(Object... params) {
            return pre(params);
        }

        protected void onPostExecute(Object[] resultObj) {
            post(resultObj);
        }

        Object[] pre(Object... params) {
            WeiboLog.d("pre,repost.");
            com.me.microblog.bean.Status status=null;
            try {
                String content=String.valueOf(params[0]);
                String is_comment=String.valueOf(params[1]);
                long id=(Long) params[2];
                SinaStatusApi statusApi=new SinaStatusApi();
                statusApi.updateToken();
                //status=((SWeiboApi2) App.getMicroBlog(App.getAppContext())).repostStatus(id, content, is_comment);
                status=statusApi.repostStatus(id, content, is_comment);
                return new Object[]{status};
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        void post(Object[] resultObj) {
            WeiboLog.d("转发结束.");
            if (resultObj==null) {
                Toast.makeText(App.getAppContext(), R.string.repost_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            com.me.microblog.bean.Status status=(com.me.microblog.bean.Status) resultObj[0];
            WeiboLog.d("转发结束.status:"+status);
            if (status!=null) {
                Toast.makeText(App.getAppContext(), R.string.repost_suc, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(App.getAppContext(), R.string.repost_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 添加最近查看的用户线程.
     */
    static class AddAtUserThread extends Thread {

        AtUser atUser;

        AddAtUserThread(AtUser atUser) {
            this.atUser=atUser;
        }

        @Override
        public void run() {
            try {
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
                long currentUserId=prefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
                SqliteWrapper.saveAtUser(App.getAppContext(), atUser, currentUserId, TwitterTable.UserTbl.TYPE_RECENT_AT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件绝对路径名读取列表.反序列化
     *
     * @param filepath 文件绝对路径
     * @return 对象列表
     */
    public static ArrayList readLocalData(String filepath) {
        WeiboLog.v("readLocalData,path:"+filepath);
        File file=new File(filepath);
        ArrayList data=null;
        if (file.exists()) {
            FileInputStream fis=null;
            try {
                fis=new FileInputStream(file);
                BufferedInputStream br=new BufferedInputStream(fis);
                ObjectInputStream in=new ObjectInputStream(br);
                data=(ArrayList) in.readObject();
                in.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
            } finally {
                if (null!=fis) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return data;
    }

    /**
     * 根据绝对路径将对象列表序列化到文件
     *
     * @param data     待序列化的对象列表
     * @param filepath 文件绝对路径
     */
    public static void writeLocalData(ArrayList data, String filepath) {
        if (null!=data&&data.size()>0) {
            FileOutputStream fos=null;
            ObjectOutputStream out=null;
            try {
                File file=new File(filepath);
                fos=new FileOutputStream(file);
                out=new ObjectOutputStream(fos);
                out.writeObject(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null!=fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
