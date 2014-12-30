package cn.archko.microblog.fragment.abs;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import cn.archko.microblog.listeners.FragmentListListener;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.oauth.Oauth2Handler;
import com.me.microblog.oauth.OauthCallback;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;

/**
 * @version 1.00.00
 * @description: 基础的Fragment，
 * @author: archko 11-11-17
 */
public abstract class BaseFragment extends Fragment implements FragmentListListener {

    public static final String TAG = "BaseFragment";
    public SharedPreferences mPrefs;
    public String mCacheDir;   //缓存图片存储上级目录.
    /**
     * 用于主题设置背景的,需要子类初始化.
     */
    public View mRoot;

    //Activity mActivity;
    /**
     * 监听器用于显示进度
     */
    public OnRefreshListener mRefreshListener;
    //--------------------- 认证 ---------------------
    public Oauth2Handler mOauth2Handler;
    public OauthCallback mOauthCallback = new OauthCallback() {
        @Override
        public void postOauthSuc(Object[] params) {
            postOauth(params);
        }

        @Override
        public void postOauthFailed(int oauthCode) {
            oauthFailed(oauthCode);
        }
    };

    /**
     * 认证失败后的操作，如果是用户名与密码未保存,则删除数据库用户信息,重新登录.
     *
     * @param oauthCode 认证失败的代码,如果是特定的,就需要重新登录.
     */
    public void oauthFailed(int oauthCode) {
        if (oauthCode == Constants.USER_PASS_IS_NULL) {
            NotifyUtils.showToast(R.string.oauth_runtime_user_pass_is_null);
        } else {
            NotifyUtils.showToast(R.string.oauth_runtime_failed);
        }
    }

    /**
     * 认证后期处理，如果使用web认证，需要覆盖此方法，否则无法得到key
     *
     * @param params 前一次请求数据的参数。
     */
    public abstract void postOauth(Object[] params);
    //--------------------- 认证 ---------------------

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mOauth2Handler = new Oauth2Handler(getActivity(), mOauthCallback);

        mCacheDir = ((App) getActivity().getApplicationContext()).mCacheDir;
        File file = new File(mCacheDir);
        if (! file.exists()) {
            file.mkdir();
        }
        WeiboLog.v(TAG, "onCreate:" + this);
    }

    @Override
    public void onPause() {
        super.onPause();
        WeiboLog.v(TAG, "onPause:" + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        WeiboLog.v(TAG, "onResume:" + this);
    }

    /**
     * 这是一个可刷新的方法,当ActionBar中的按钮按下时,就可以刷新它了.
     */
    public void refresh() {
    }

    /**
     *
     */
    public void clear() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        WeiboLog.v(TAG, "onAttach:" + this);
        try {
            mRefreshListener = (OnRefreshListener) activity;
        } catch (ClassCastException e) {
            //throw new ClassCastException(activity.toString()+" must implement OnRefreshListener");
            mRefreshListener = null;
        }
    }

    //------------------------------------

    /**
     * 查看Status原文信息,包括评论.
     */
    protected void viewOriginalStatus(View achor) {
    }

    /**
     * 创建收藏.
     */
    protected void createFavorite() {
    }

    /**
     * 跳转到到评论界面
     */
    protected void commentStatus() {
    }

    /**
     * 到转发界面
     */
    protected void repostStatus() {
    }

    /**
     * 删除，需要根据不同的类型的列表处理。不是所有的微博都可以删除
     */
    protected void viewStatusUser() {
    }

    /**
     * 快速转发
     */
    protected void quickRepostStatus() {
        //throw new IllegalArgumentException("not implemented!");
    }

    //--------------------- theme ---------------------
    public void themeBackground() {
        if (null != mRoot) {
            ThemeUtils.getsInstance().themeBackground(mRoot, getActivity());
        }
    }
}
