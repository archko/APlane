package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import cn.archko.microblog.R;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.App;
import com.me.microblog.util.Constants;
import com.umeng.analytics.MobclickAgent;

/**
 * 作为主题的Activity
 * User: archko Date: 12-9-10 Time: 上午8:25
 */
public class SkinFragmentActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    //--------------------- popupMenu ---------------------

    public static final int MENU_FIRST=Menu.FIRST;
    public static final int MENU_SECOND=Menu.FIRST+1;

    /*MenuBuilder mMenu=null;
    MenuPopupHelper mMenuHelper=null;*/
    protected ActionBar mActionBar;
    public SharedPreferences mPrefs;
    public String mThemeId="0";
    /**
     * 当前登录用户的id
     */
    protected long currentUserId=-1l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*if (!Util.isHoneycombOrLater()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }*/
        mPrefs=PreferenceManager.getDefaultSharedPreferences(this);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        applyThemeId(themeId);

        //requestWindowFeature(com.actionbarsherlock.view.Window.FEATURE_PROGRESS);
        //requestWindowFeature(com.actionbarsherlock.view.Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        ThemeUtils.getsInstance().themeActionBar(getActionBar(), this);
        //MobclickAgent.onError(this);

        mPrefs=PreferenceManager.getDefaultSharedPreferences(this);
        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        this.currentUserId=aUserId;

        mActionBar=getActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /**
     * 应用主题
     *
     * @param themeId 主题的id
     */
    public void applyThemeId(String themeId) {
        int theme=R.style.Theme_AK;
        if ("0".equals(themeId)) {
            mThemeId="0";
        } else if ("1".equals(themeId)) {
            theme=R.style.Theme_AK;
            mThemeId="1";
        } else if ("2".equals(themeId)) {
            theme=R.style.Theme_AK_Light;
            mThemeId="2";
        } else if ("3".equals(themeId)) {
            //theme=R.style.Theme_AndroidDevelopers;
            mThemeId="3";
        }
        mThemeId="2";
        theme=R.style.Theme_AK_Light;
        setTheme(theme);
    }

    /**
     * 对于新加入Tab浏览模式，也要刷新新的消息数，所以在这里增加超类实现
     */
    public void refreshSidebar() {
    }

    //--------------------- popupMenu ---------------------

    /**
     * 初始化自定义菜单
     *
     * @param anchorView 菜单显示的锚点View。
     */
    public void prepareMenu(View anchorView) {
        PopupMenu popupMenu=new PopupMenu(this, anchorView);

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
