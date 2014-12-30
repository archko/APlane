package cn.archko.microblog.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.PopupMenu;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import com.me.microblog.util.Constants;

/**
 * 包含认证的,目前的操作都放入队列中,不需要在这里认证.
 *
 * @author : archko Date: 12-9-4 Time: 上午10:57
 */
public class BaseOauthFragmentActivity extends SkinFragmentActivity implements
    PopupMenu.OnMenuItemClickListener, OnRefreshListener {

    public final int MENU_FIRST = Menu.FIRST;
    public final int MENU_SECOND = Menu.FIRST + 1;
    SharedPreferences mPrefs;
    /**
     * 当前登录用户的id
     */
    long currentUserId = - 1l;
    //--------------------- 认证 ---------------------
    /*Oauth2Handler mOauth2Handler;
    OauthCallback mOauthCallback=new OauthCallback() {
        @Override
        public void postOauthSuc(Object[] params) {
            oauthSuccessfully(params);
        }

        @Override
        public void postOauthFailed(int oauthCode) {
            oauthFailed(oauthCode);
        }
    };*/

    /**
     * 认证成功后的操作
     */
    void oauthSuccessfully(Object[] params) {
    }

    /**
     * 认证失败后的操作，如果是列表，默认是刷新
     *
     * @param oauthCode 认证失败的代码,如果是特定的,就需要重新登录.
     */
    public void oauthFailed(int oauthCode) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        long aUserId = mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
        this.currentUserId = aUserId;

        //mOauth2Handler=new Oauth2Handler(this, mOauthCallback);

        mActionBar = getActionBar();
    }

    @Override
    public void onRefreshStarted() {
    }

    @Override
    public void onRefreshFinished() {
    }

    @Override
    public void onRefreshFailed() {
    }

    //--------------------- popupMenu ---------------------

    /**
     * 初始化自定义菜单
     *
     * @param anchorView 菜单显示的锚点View。
     */
    /*public void prepareMenu(View anchorView) {
        PopupMenu popupMenu=new PopupMenu(this, anchorView);

        onCreateCustomMenu(popupMenu);
        onPrepareCustomMenu(popupMenu);
        //return showCustomMenu(anchorView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }*/

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
    /*public void onCreateCustomMenu(PopupMenu menuBuilder) {
        *//*menuBuilder.add(0, 1, 0, "title1");*//*
    }*/

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    /*public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        *//*menuBuilder.add(0, 1, 0, "title1");*//*
    }*/

    /*public boolean onMenuItemSelected(PopupMenu menu, MenuItem item) {
        return false;
    }*/

    /*@Override
    public boolean onMenuItemClick(MenuItem item) {

        return false;
    }*/
}
