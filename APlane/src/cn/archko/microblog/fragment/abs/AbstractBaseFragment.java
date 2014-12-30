package cn.archko.microblog.fragment.abs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 基础的Fragment，
 * @author: archko 11-11-17
 */
public abstract class AbstractBaseFragment extends BaseFragment implements PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "AbstractBaseFragment";
    public static final int THREAD_INIT = 1;
    public static final int THREAD_RUNNING = 2;
    public static final int THREAD_CANCELED = 3;
    public static final int THREAD_FINISHED = 4;
    public static final int THREAD_DEAD = 5;
    /**
     * 需要维护线程的状态，因为当阻塞时，退出Activity，线程返回后继续操作会引起异常。
     */
    protected int mThreadStatus = THREAD_INIT;
    protected CommonTask mCommonTask;
    protected QueryTask mQueryTask;
    protected OperationTask mOperationTask;
    /**
     * 当前登录用户的id
     */
    public long currentUserId = - 1l;

    //--------------------- popupMenu ---------------------
    /**
     * 列表选中的位置
     */
    public int selectedPos = 0;
    /*MenuBuilder mMenuBuilder=null;
    MenuPopupHelper mMenuHelper=null;*/
    PopupWindowListener mPopupWindowListener = new PopupWindowListener() {
        @Override
        public void show(View view, int pos) {
            selectedPos = pos;
            prepareMenu(view);
        }
    };

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long aUserId = mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
        this.currentUserId = aUserId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WeiboLog.v(TAG, "onDestroy:" + this);
        mThreadStatus = THREAD_DEAD;
    }

    //---------------------------------

    /**
     * 网络操作的任务
     *
     * @param params 参数
     * @param msg    线程已经在运行中的提示信息
     */
    protected void newTask(Object[] params, String msg) {
        WeiboLog.d(TAG, "newTask:");
        if (! App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error, Toast.LENGTH_LONG);
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshFailed();
            }
            basePostOperation(null);

            return;
        }

        if (mThreadStatus == THREAD_RUNNING || (mCommonTask != null && mCommonTask.getStatus() == AsyncTask.Status.RUNNING)) {
            if (! TextUtils.isEmpty(msg)) {
                NotifyUtils.showToast(msg, Toast.LENGTH_SHORT);
            }
            return;
        }

        App app = (App) App.getAppContext();
        if (app.getOauthBean().oauthType == Oauth2.OAUTH_TYPE_WEB) {
            mCommonTask = new CommonTask();
            mCommonTask.execute(params);
        } else {
            if (System.currentTimeMillis() >= app.getOauthBean().expireTime && app.getOauthBean().expireTime != 0) {
                WeiboLog.i(TAG, "web认证，token过期了.");
                NotifyUtils.showToast("token过期了,需要重新认证，如果认证失败，请注销再登陆！");
                //oauth2(params);
                mOauth2Handler.oauth2(params);
            } else {
                WeiboLog.d(TAG, "web认证，但token有效。");
                mCommonTask = new CommonTask();
                mCommonTask.execute(params);
            }
        }
    }

    /**
     * 无网络操作的任务，用于本地数据获取
     *
     * @param params 参数
     * @param msg    线程已经在运行中的提示信息
     */
    protected void newTaskNoNet(Object[] params, String msg) {
        WeiboLog.d(TAG, "newTaskNoNet:");

        if (mThreadStatus == THREAD_RUNNING || (mQueryTask != null && mQueryTask.getStatus() == AsyncTask.Status.RUNNING)) {
            if (! TextUtils.isEmpty(msg)) {
                NotifyUtils.showToast(msg, Toast.LENGTH_SHORT);
            }
            return;
        }

        mQueryTask = new QueryTask();
        mQueryTask.execute(params);
    }

    /**
     * 认证失败后的操作，如果是列表，默认是刷新
     *
     * @param oauthCode 认证失败的代码,如果是特定的,就需要重新登录.
     */
    public void oauthFailed(int oauthCode) {
    }

    @Override
    public void postOauth(Object[] params) {
        NotifyUtils.showToast(R.string.oauth_runtime_suc);
        mCommonTask = new CommonTask();
        mCommonTask.execute(params);
    }

    //--------------------- 主列表数据操作 ---------------------
    public class CommonTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mThreadStatus = THREAD_RUNNING;
            basePreOperation();
        }

        @Override
        protected Object[] doInBackground(Object... params) {
            return baseBackgroundOperation(params);
        }

        protected void onPostExecute(Object[] resultObj) {
            if (mThreadStatus == THREAD_DEAD || isCancelled() || ! isResumed()) {
                WeiboLog.i("程序退出，线程死亡。");
                return;
            }

            mThreadStatus = THREAD_FINISHED;
            basePostOperation(resultObj);
        }
    }

    /**
     * 线程执行前期的操作
     */
    protected void basePreOperation() {
    }

    /**
     * 线程中的操作。
     *
     * @param params
     * @return
     */
    protected Object[] baseBackgroundOperation(Object... params) {
        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void basePostOperation(Object[] resultObj) {
    }

    //--------------------- 主列表数据操作 ---------------------
    class QueryTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mThreadStatus = THREAD_RUNNING;
            baseQueryPreOperation();
        }

        @Override
        protected Object[] doInBackground(Object... params) {
            try {
                return baseQueryBackgroundOperation(params);
            } catch (WeiboException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Object[] resultObj) {
            if (mThreadStatus == THREAD_DEAD || isCancelled() || ! isResumed()) {
                WeiboLog.i("程序退出，线程死亡。");
                return;
            }

            mThreadStatus = THREAD_FINISHED;
            basePostOperation(resultObj);
        }
    }

    /**
     * 线程执行前期的操作
     */
    protected void baseQueryPreOperation() {
        basePreOperation();
    }

    /**
     * 线程中的操作。
     *
     * @param params
     * @return
     */
    protected Object[] baseQueryBackgroundOperation(Object... params) throws WeiboException {
        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void baseQueryPostOperation(Object[] resultObj) {
        basePostOperation(resultObj);
    }

    //--------------------- 其它操作，如未读消息清零 ---------------------

    /**
     * 网络操作的任务
     *
     * @param params 参数
     * @param msg    线程已经在运行中的提示信息
     */
    protected void newOperationTask(Object[] params, String msg) {
        WeiboLog.d(TAG, "newTask:");
        if (! App.hasInternetConnection(getActivity())) {
            NotifyUtils.showToast(R.string.network_error, Toast.LENGTH_LONG);
            /*if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFailed();
            }*/
            basePostOperation2(null);

            return;
        }

        /*if (mThreadStatus==THREAD_RUNNING||(mOperationTask!=null&&mOperationTask.getStatus()==AsyncTask.Status.RUNNING)) {
            return;
        }*/

        App app = (App) App.getAppContext();
        if (app.getOauthBean().oauthType == Oauth2.OAUTH_TYPE_WEB) {
            mOperationTask = new OperationTask();
            mOperationTask.execute(params);
        } else {
            if (System.currentTimeMillis() >= app.getOauthBean().expireTime && app.getOauthBean().expireTime != 0) {
                WeiboLog.i(TAG, "web认证，token过期了.");
                //mOauth2Handler.oauth2(params);
            } else {
                WeiboLog.d(TAG, "web认证，但token有效。");
                mOperationTask = new OperationTask();
                mOperationTask.execute(params);
            }
        }
    }

    class OperationTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            basePreOperation2();
        }

        @Override
        protected Object[] doInBackground(Object... params) {
            return baseBackgroundOperation2(params);
        }

        protected void onPostExecute(Object[] resultObj) {
            if (isCancelled() || ! isResumed()) {
                WeiboLog.i("程序退出，线程死亡。");
                return;
            }

            basePostOperation2(resultObj);
        }
    }

    /**
     * 线程执行前期的操作
     */
    protected void basePreOperation2() {
    }

    /**
     * 线程中的操作。
     *
     * @param params
     * @return
     */
    protected Object[] baseBackgroundOperation2(Object... params) {
        return null;
    }

    /**
     * 线程结束后的操作。
     *
     * @param resultObj
     */
    protected void basePostOperation2(Object[] resultObj) {
    }

    //--------------------- popupMenu ---------------------

    /**
     * 初始化自定义菜单
     *
     * @param anchorView 菜单显示的锚点View。
     */
    public void prepareMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), anchorView);

        onCreateCustomMenu(popupMenu);
        onPrepareCustomMenu(popupMenu);
        //return showCustomMenu(anchorView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    /**
     * 显示自定义菜单
     *
     * @param anchor
     */
    /*public boolean showCustomMenu(View anchor) {
        if (mMenuBuilder.size()<0) {
            WeiboLog.w(TAG, "no menu item!");
            return false;
        }

        View anchorView=anchor;
        if (null==mMenuHelper) {
            mMenuHelper=new MenuPopupHelper(getActivity(), mMenuBuilder, null, false);
        }
        mMenuHelper.setAnchorView(anchorView);
        return mMenuHelper.tryShow();
    }*/

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        /*menuBuilder.add(0, 1, 0, "title1");*/
    }

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        /*menuBuilder.add(0, 1, 0, "title1");*/
    }

    /*public boolean onMenuItemSelected(PopupMenu menu, MenuItem item) {
        return false;
    }*/

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return false;
    }
}
