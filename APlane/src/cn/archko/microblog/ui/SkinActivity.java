package cn.archko.microblog.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;

/**
 * User: archko Date: 12-9-10 Time: 上午9:17
 */
public class SkinActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    //--------------------- popupMenu ---------------------
    /*MenuBuilder mMenu=null;
    MenuPopupHelper mMenuHelper=null;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (!Util.isHoneycombOrLater()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }*/

        int theme=R.style.Theme_AK;
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
            theme=R.style.Theme_AK;
        } else if ("2".equals(themeId)) {
            theme=R.style.Theme_AK_Light;
        } else if ("3".equals(themeId)) {
            //theme=R.style.Theme_AndroidDevelopers;
        }
        theme=R.style.Theme_AK_Light;
        setTheme(theme);
        getActionBar().hide();
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
