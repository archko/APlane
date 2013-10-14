package cn.archko.microblog.fragment;

import android.text.TextUtils;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AbsBaseListFragment;
import com.me.microblog.App;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.utils.AKUtils;

/**
 * @version 1.00.00
 * @description: Oauth2高级key的认证
 * @author: archko 12-11-4
 */
public abstract class AdvancedOauth2ListFragment<T> extends AbsBaseListFragment<T> {

    /**
     * 网络操作的任务
     *
     * @param params 参数
     * @param msg    线程已经在运行中的提示信息
     */
    @Override
    protected void newTask(final Object[] params, String msg) {
        App app=(App) App.getAppContext();
        WeiboLog.v(TAG, "AdvancedOauth2ListFragment.newTask:"+App.OAUTH_MODE+
            " App.mAdvancedOauth2Timestampe:"+app.mAdvancedOauth2Timestampe);
        if (!App.hasInternetConnection(getActivity())) {
            AKUtils.showToast(R.string.network_error, Toast.LENGTH_LONG);
            if (mRefreshListener!=null) {
                mRefreshListener.onRefreshFailed();
            }
            basePostOperation(null);

            return;
        }

        if (mThreadStatus==THREAD_RUNNING||(mCommonTask!=null)){//&&mCommonTask.getStatus()==AsyncTask.Status.RUNNING)) {
            if (!TextUtils.isEmpty(msg)) {
                AKUtils.showToast(msg, Toast.LENGTH_SHORT);
            }
            return;
        }

        if (System.currentTimeMillis()>=app.mAdvancedOauth2Timestampe&&app.mAdvancedOauth2Timestampe<100) {
            WeiboLog.i(TAG, "认证，高级token过期了.");
            /*OauthBean bean=mOauth2Handler.advancedOauth2();
            if (null!=bean) {
                WeiboLog.d(TAG, "重新认证成功。");
                mCommonTask=new CommonTask();
                mCommonTask.execute(params);
            }*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OauthBean bean=mOauth2Handler.advancedOauth2();
                    if (null!=bean) {
                        WeiboLog.d(TAG, "重新认证成功。");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCommonTask=new CommonTask();
                                mCommonTask.execute(params);
                            }
                        },0l);
                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AKUtils.showToast("认证失败，您暂时无法使用此功能。");
                            }
                        },0l);
                    }
                }
            }).start();
        } else {
            WeiboLog.d(TAG, "认证，但高级token有效。");
            mCommonTask=new CommonTask();
            mCommonTask.execute(params);
        }
    }
}
