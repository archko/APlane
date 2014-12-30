package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.action.GroupAction;
import cn.archko.microblog.fragment.HomeFragment;
import cn.archko.microblog.fragment.HomeGridFragment;
import cn.archko.microblog.fragment.PrefsFragment;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.service.WeiboService;
import cn.archko.microblog.sliding.app.SidebarAdapter;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.action.ActionResult;
import com.me.microblog.action.AsyncActionTask;
import com.me.microblog.bean.Group;
import com.me.microblog.bean.Unread;
import com.me.microblog.core.ImageManager;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 由侧边栏导航方式的主页，主要展示登录者相关的内容
 *
 * @author archko
 */
public class HomeActivity extends SkinFragmentActivity implements OnRefreshListener {

    public final static String TAG = "HomeActivity";

    private ActionBar mActionBar;
    private SidebarAdapter mSidebarAdapter;
    boolean isInitialized = false;
    Handler mHandler;
    /**
     * 消息的缓存View
     */
    HashMap<String, TextView> mActionMsgView = new HashMap<String, TextView>(8);
    Spinner mGroupItem;
    RelativeLayout mGrouplayout;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawer;
    ListView mRightDrawer;
    /**
     * 是否已经选中一个位置了.第一次为未选中.
     */
    boolean hasFocused = false;

    /**
     * 根据屏幕设置下载图片的分辨率，因为内存的限制，不处理横屏时的大小，全部按照竖屏处理
     */
    void setImageD() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WeiboLog.d(TAG, "setImageD.width:" + width + " height:" + height + " dm.wp:" + dm.widthPixels + " dm.hp:" + dm.heightPixels + " density:" + dm.density);
        if (width > height) {
            ImageManager.IMAGE_MAX_WIDTH = height;
            ImageManager.IMAGE_MAX_HEIGHT = width;
        } else {
            ImageManager.IMAGE_MAX_WIDTH = width;
            ImageManager.IMAGE_MAX_HEIGHT = height;
        }
    }

    public HomeActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //设置ActionBar 浮动到view 上层来
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //设置ActionBar 背景色 透明
        //getActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        super.onCreate(savedInstanceState);
        setProgressBarIndeterminateVisibility(false);
        setProgressBarVisibility(false);

        mHandler = new Handler();

        setContentView(R.layout.ak_main_drawer_layout);

        final ActionBar bar = getActionBar();
        mActionBar = bar;
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mActionBar.setDisplayShowTitleEnabled(true);  //隐藏主面的标题
        mActionBar.setDisplayShowHomeEnabled(true);   //整个标题栏
        mActionBar.setTitle(R.string.tab_label_home);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (ListView) findViewById(R.id.start_drawer);
        mDrawer.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        //mRightDrawer=(ListView) findViewById(R.id.right_drawer);

        // set the Behind View
        mSidebarAdapter = new SidebarAdapter(getFragmentManager(), HomeActivity.this);
        int home = mSidebarAdapter.addFragment(true);
        mDrawer.setAdapter(mSidebarAdapter);
        //mRightDrawer.setAdapter(mSidebarAdapter);
        mDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                navigationFragment(position);
            }
        });

        Fragment oldFragment = getFragmentManager().findFragmentById(R.id.fragment_placeholder);
        WeiboLog.v(TAG, "old:" + oldFragment + " instance:" + savedInstanceState);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (oldFragment == null) {
            mSidebarAdapter.getFragment(0, ft);
        } else {
            ft.attach(oldFragment);
        }
        ft.commit();

        setCustomActionBar();
        reloadPreferences();
        doInit();
        addGroupNav();
    }

    View.OnClickListener mActionItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onActionItemClick(view.getId());
        }
    };

    private void onActionItemClick(int id) {
        if (R.id.action_new_status == id) {
            newStatus();
        } else if (R.id.action_refresh == id) {
            refresh();
        } else if (R.id.action_clear == id) {
            clear();
        } else if (R.id.action_home == id) {
            int pos = mSidebarAdapter.getFragmentPos(Constants.TAB_ID_HOME);
            selectItem(pos);
            navigationFragment(pos);
        } else if (R.id.action_at_comment == id) {
            int pos = mSidebarAdapter.getFragmentPos(Constants.TAB_ID_AT_COMMENT);
            selectItem(pos);
            navigationFragment(pos);
        } else if (R.id.action_at_status == id) {
            int pos = mSidebarAdapter.getFragmentPos(Constants.TAB_ID_AT_STATUS);
            selectItem(pos);
            navigationFragment(pos);
        } else if (R.id.action_comment == id) {
            int pos = mSidebarAdapter.getFragmentPos(Constants.TAB_ID_COMMENT);
            selectItem(pos);
            navigationFragment(pos);
        } else if (R.id.action_follower == id) {
            int pos = mSidebarAdapter.getFragmentPos(Constants.TAB_ID_FOLLOWER);
            selectItem(pos);
            navigationFragment(pos);
        }
    }

    private void selectItem(int pos) {
        mDrawer.setItemChecked(pos, true);
        mSidebarAdapter.notifyDataSetChanged();
    }

    private void setCustomActionBar() {
        View cusActionBar = getLayoutInflater().inflate(R.layout.home_action_bar, null);
        mActionBar.setCustomView(cusActionBar);
        mActionBar.setDisplayShowCustomEnabled(true);

        mGroupItem = (Spinner) cusActionBar.findViewById(R.id.action_group);
        mGrouplayout = (RelativeLayout) cusActionBar.findViewById(R.id.action_group_layout);
        //ImageButton group=(ImageButton) cusActionBar.findViewById(R.id.action_group_bg);
        ImageButton newStatusItem = (ImageButton) cusActionBar.findViewById(R.id.action_new_status);
        newStatusItem.setOnClickListener(mActionItemListener);
        ImageButton refreshItem = (ImageButton) cusActionBar.findViewById(R.id.action_refresh);
        refreshItem.setOnClickListener(mActionItemListener);
        ImageButton clearItem = (ImageButton) cusActionBar.findViewById(R.id.action_clear);
        clearItem.setOnClickListener(mActionItemListener);

        String themeId = PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int menuNewStatus = R.drawable.content_edit_light;
        int refreshId = R.drawable.navigation_refresh_light;
        int clearId = R.drawable.content_discard_light;
        //int groupdId=R.drawable.social_group_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            menuNewStatus = R.drawable.content_edit_light;
            refreshId = R.drawable.navigation_refresh_light;
            //groupdId=R.drawable.social_group_light;
            clearId = R.drawable.content_discard_light;
        }
        newStatusItem.setImageResource(menuNewStatus);
        refreshItem.setImageResource(refreshId);
        clearItem.setImageResource(clearId);
        //group.setImageResource(groupdId);

        //init action items
        View layout = cusActionBar.findViewById(R.id.action_home);
        TextView msgView = (TextView) layout.findViewById(R.id.msg);
        TextView titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(R.string.action_item_home);
        mActionMsgView.put(Constants.TAB_ID_HOME, msgView);
        layout.setOnClickListener(mActionItemListener);

        layout = cusActionBar.findViewById(R.id.action_comment);
        msgView = (TextView) layout.findViewById(R.id.msg);
        titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(R.string.action_item_comment);
        mActionMsgView.put(Constants.TAB_ID_COMMENT, msgView);
        layout.setOnClickListener(mActionItemListener);

        layout = cusActionBar.findViewById(R.id.action_at_status);
        msgView = (TextView) layout.findViewById(R.id.msg);
        titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(R.string.action_item_at_status);
        mActionMsgView.put(Constants.TAB_ID_AT_STATUS, msgView);
        layout.setOnClickListener(mActionItemListener);

        layout = cusActionBar.findViewById(R.id.action_at_comment);
        msgView = (TextView) layout.findViewById(R.id.msg);
        titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(R.string.action_item_at_comment);
        mActionMsgView.put(Constants.TAB_ID_AT_COMMENT, msgView);
        layout.setOnClickListener(mActionItemListener);

        layout = cusActionBar.findViewById(R.id.action_follower);
        msgView = (TextView) layout.findViewById(R.id.msg);
        titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(R.string.action_item_follower);
        mActionMsgView.put(Constants.TAB_ID_FOLLOWER, msgView);
        layout.setOnClickListener(mActionItemListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        WeiboLog.v(TAG, "onRestoreInstanceState:" + savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        WeiboLog.v(TAG, "onSaveInstanceState:" + outState);
    }

    private void doInit() {
        isInitialized = true;
        setImageD();

        delayStartService();

        if (! WeiboUtils.isHoneycombOrLater()) {
            if (null == mExitReceiver) {
                mExitReceiver = new ExitBroadcastReceiver();
            }
            registerReceiver(mExitReceiver, new IntentFilter(Constants.EXIT_APP));
        }
    }

    private void delayStartService() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean chk_new_status = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                    .getBoolean(PrefsActivity.PREF_AUTO_CHK_NEW_STATUS, true);

                Intent intent = new Intent(HomeActivity.this, WeiboService.class);
                intent.setAction(WeiboService.REFRESH);
                if (chk_new_status) {
                    startService(intent);
                } else {
                    stopService(intent);
                }

                intent = new Intent(HomeActivity.this, SendTaskService.class);
                startService(intent);

                /*intent=new Intent(HomeActivity.this, AKWidgetService.class);
                startService(intent);*/
            }
        }, 4000l);
    }

    /**
     * 侧边栏的Fragment导航
     *
     * @param position 当前的Fragment位置
     */
    void navigationFragment(int position) {
        mDrawerLayout.closeDrawer(mDrawer);
        SidebarAdapter.SidebarEntry entry = (SidebarAdapter.SidebarEntry) mSidebarAdapter.getItem(position);
        if (entry.navType == SidebarAdapter.SidebarEntry.NAV_TYPE_INTENT) {
            Intent intent = new Intent(HomeActivity.this, entry.clazz);
            startActivity(intent);
            return;
        }

        Fragment current = getFragmentManager().findFragmentById(R.id.fragment_placeholder);
        if (current.getTag().equals(entry.id)) {// Already selected
            return;
        }

        //getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        WeiboLog.v(TAG, "currenttag:" + current.getTag() + " navmode;" + mActionBar.getNavigationMode());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (null != current) {
            ft.detach(current);
        }
        Fragment next = mSidebarAdapter.getFragment(entry, position, ft);
        ft.commit();
        if (Constants.TAB_ID_HOME.equals(next.getTag())) {
            if (mGrouplayout.getVisibility() == View.GONE) {
                mGrouplayout.setVisibility(View.VISIBLE);
                ((HorizontalScrollView) mActionBar.getCustomView()).fullScroll(View.FOCUS_RIGHT);
            }
            /*if (mActionBar.getNavigationMode()==ActionBar.NAVIGATION_MODE_STANDARD) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            }*/
        } else {
            if (mGrouplayout.getVisibility() == View.VISIBLE) {
                mGrouplayout.setVisibility(View.GONE);
            }
            /*if (mActionBar.getNavigationMode()==ActionBar.NAVIGATION_MODE_LIST) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }*/
        }

        /*if (getFragmentManager().getBackStackEntryCount()>0) {
            for (int i=0; i<getFragmentManager().getBackStackEntryCount(); i++) {
                getFragmentManager().popBackStack();
            }
        }*/
        /*ft.attach(next);
        ft.commit();*/
        //mCurrentFragment=entry.id;
        mActionBar.setTitle(entry.name);
        selectItem(position);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null == receiver) {
            receiver = new MsgBroadcastReceiver();
        }
        registerReceiver(receiver, new IntentFilter(Constants.SERVICE_NOTIFY_UNREAD));

        //apply theme
        applyTheme();
        refreshSidebar();

        if (! hasFocused) {
            selectItem(0);
            hasFocused = true;
        }
    }

    /**
     * 应用主题.
     */
    void applyTheme() {
        String themeId = PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if (! mThemeId.equals(themeId)) {
            ThemeUtils.getsInstance().themeActionBar(getActionBar(), this);
            //mMenuFragment.themeBackground(true);
            mThemeId = themeId;
            applyThemeId(themeId);
            final Fragment current = getFragmentManager().findFragmentById(R.id.fragment_placeholder);
            if (current != null && current instanceof AbstractBaseFragment) {
                AbstractBaseFragment baseFragment = (AbstractBaseFragment) current;
                baseFragment.themeBackground();
            }

            //theme custom action bar
            mActionBar.setCustomView(null);
            setCustomActionBar();
            addGroupNav();
        }
    }

    /**
     * 刷新侧边栏的消息
     */
    public void refreshSidebar() {
        Unread unread = new Unread();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        unread.status = sp.getInt(Constants.PREF_SERVICE_STATUS, 0);
        unread.comments = sp.getInt(Constants.PREF_SERVICE_COMMENT, 0);
        unread.followers = sp.getInt(Constants.PREF_SERVICE_FOLLOWER, 0);
        unread.mention_status = sp.getInt(Constants.PREF_SERVICE_AT, 0);
        unread.mention_cmt = sp.getInt(Constants.PREF_SERVICE_AT_COMMENT, 0);
        unread.dm = sp.getInt(Constants.PREF_SERVICE_DM, 0);
        receiveUnread(unread);
    }

    @Override
    protected void onPause() {
        /*SharedPreferences.Editor editor=getSharedPreferences("MainActivity", MODE_PRIVATE).edit();
        editor.putString("fragment", mCurrentFragment);
        editor.commit();*/
        super.onPause();

        if (null != receiver) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mExitReceiver) {
            unregisterReceiver(mExitReceiver);
            mExitReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {
        boolean pref_back_pressed = mPrefs.getBoolean(PrefsActivity.PREF_BACK_PRESSED, false);
        if (pref_back_pressed) {
            super.onBackPressed();
            Intent intent = new Intent(HomeActivity.this, WeiboService.class);
            HomeActivity.this.stopService(intent);
            intent = new Intent(HomeActivity.this, SendTaskService.class);
            stopService(intent);
            ((App) App.getAppContext()).logout();
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            try {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory("android.intent.category.HOME");
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //menu.findItem(R.id.menu_home_hot).setVisible(false);
        menu.findItem(R.id.menu_home_user).setVisible(false);
        menu.findItem(R.id.menu_about).setVisible(false);
        menu.findItem(R.id.menu_mode).setVisible(false);
        menu.findItem(R.id.menu_task).setVisible(false);
        menu.findItem(R.id.menu_update).setVisible(false);*/

        /*menu.add(0, R.id.menu_new_status, 1, R.string.action_new_status)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, R.id.menu_refresh, 1, R.string.opb_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
        menu.add(0, R.id.menu_search, 1, R.string.action_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, R.id.menu_exit, 1, R.string.action_exit)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, R.id.menu_pref, 1, R.string.action_pref)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        /*String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int menuNewStatus=R.drawable.content_edit_dark;
        int resId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark;
        int refreshId=R.drawable.navigation_refresh_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            resId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_light;
            menuNewStatus=R.drawable.content_edit_light;
            refreshId=R.drawable.navigation_refresh_light;
        }
        menu.findItem(R.id.menu_new_status).setIcon(menuNewStatus);
        //menu.findItem(R.id.menu_nav).setIcon(resId);
        menu.findItem(R.id.menu_refresh).setIcon(refreshId);*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Intent to start new Activity

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawer)) {
                mDrawerLayout.closeDrawer(mDrawer);
            } else {
                mDrawerLayout.openDrawer(mDrawer);
            }
        } else if (itemId == R.id.menu_new_status) {
            newStatus();
        } /*else if (itemId==R.id.menu_home_hot) {
            navigateToHot();
        } */ else if (itemId == R.id.menu_home_user) {
            //showSelf();
        } else if (itemId == R.id.menu_at_author) {
            atStatus();
        } else if (itemId == R.id.menu_pref) {
            showPrefs();
        } else if (itemId == R.id.menu_logout) {
            mode = PrefsFragment.MODE_LOGOUT;
            exitConfirm(R.string.app_logout_title, R.string.app_logout_msg);
        } else if (itemId == R.id.menu_search) {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.menu_account_user_manager) {
            Intent intent = new Intent(HomeActivity.this, AccountUserActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
        } else if (itemId == R.id.menu_update) {
            checkUpdate();
        } else if (itemId == R.id.menu_exit) {
            mode = PrefsFragment.MODE_EXIT;
            //exitConfirm(R.string.exit_title, R.string.exit_msg);
            AKUtils.exit(HomeActivity.this);
        } else if (itemId == R.id.menu_refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        AbstractBaseFragment current = (AbstractBaseFragment) getFragmentManager().findFragmentById(R.id.fragment_placeholder);
        WeiboLog.d(TAG, "refresh.current:" + current);
        current.refresh();
    }

    private void clear() {
        AbstractBaseFragment current = (AbstractBaseFragment) getFragmentManager().findFragmentById(R.id.fragment_placeholder);
        WeiboLog.d(TAG, "clear.current:" + current);
        current.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WeiboLog.d(TAG, "onConfigurationChanged.o:" + newConfig.orientation);
    }

    private void reloadPreferences() {
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        boolean autoChkUpdate = options.getBoolean(PrefsActivity.PREF_AUTO_CHK_UPDATE, true);
        boolean pref_auto_chk_update_wifi_only = options.getBoolean(PrefsActivity.PREF_AUTO_CHK_UPDATE_WIFI_ONLY, false);
        if (! pref_auto_chk_update_wifi_only) {//if (autoChkUpdate) {
            long time = mPrefs.getLong(Constants.UPDATE_TIMESTAMP, - 1);
            long now = System.currentTimeMillis();
            long delta = now - time - Constants.UPDATE_DELTA;
            WeiboLog.i("update.time:" + time + " now:" + now);

            if (delta < 0 && time != - 1) {
                WeiboLog.d(TAG, "不需要检查更新，近一天刚检查过，delta:" + delta + " time:" + time);
            } else {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putLong(Constants.UPDATE_TIMESTAMP, now);
                editor.commit();
                checkUpdate();
            }
        }

        /*getSlidingMenu().setBehindScrollScale(0.0f);
        getSlidingMenu().setFadeDegree(0.0f);

        String nav_sidebar_touch=mPrefs.getString(PrefsActivity.PREF_NAV_SIDEBAR_TOUCH, getString(R.string.default_nav_sidebar_touch));
        if ("0".equals(nav_sidebar_touch)) {
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        } else if ("1".equals(nav_sidebar_touch)) {
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        } else if ("2".equals(nav_sidebar_touch)) {
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        }*/

        /*int theme=R.color.holo_dark_bg_view;
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {//if ("2".equals(themeId)) {
            theme=R.color.holo_light_bg_view;
        }

        //findViewById(R.id.fragment_placeholder).setBackgroundResource(R.drawable.bg);
        findViewById(R.id.fragment_placeholder).setBackgroundResource(theme);*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Unread unread = (Unread) intent.getSerializableExtra("unread");
        WeiboLog.d(TAG, "onNewIntent.intent:" + unread);
    }

    int mode = PrefsFragment.MODE_EXIT;

    /**
     * 退出确认，有注销与退出程序确认
     *
     * @param title
     * @param msg
     */
    private void exitConfirm(int title, int msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(msg)
            .setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                    }
                }
            ).setPositiveButton(getResources().getString(R.string.confirm),
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.cancel();
                    if (mode == PrefsFragment.MODE_EXIT) {
                        AKUtils.exit(HomeActivity.this);
                    } else {
                        AKUtils.logout(HomeActivity.this);
                    }
                }
            }
        ).create().show();
    }

    @Override
    public void onRefreshStarted() {
        //setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onRefreshFinished() {
        //setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onRefreshFailed() {
        WeiboLog.d(TAG, "onRefreshFailed.");
        //setProgressBarIndeterminateVisibility(false);
    }

    //------------------------
    private void navigateToHot() {
        /*Intent intent=new Intent(HomeActivity.this, HotFragmentActivity.class);
        startActivity(intent);*/
    }

    /**
     * 跳转到发新微博
     */
    private void newStatus() {
        Intent intent = new Intent(HomeActivity.this, NewStatusActivity.class);
        startActivity(intent);
    }

    /**
     * 显示设置
     */
    private void showPrefs() {
        Intent intent = new Intent(HomeActivity.this, PrefsActivity.class);
        startActivity(intent);
    }

    /**
     * 反馈信息，也是发新微博
     */
    private void atStatus() {
        String atString = getString(R.string.feedback_at_name);
        Intent intent = new Intent(HomeActivity.this, NewStatusActivity.class);
        intent.putExtra("at_some", atString);
        intent.setAction(Constants.INTENT_NEW_BLOG);
        startActivity(intent);
    }

    //------------------------
    MsgBroadcastReceiver receiver = null;

    class MsgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (Constants.SERVICE_NOTIFY_UNREAD.equals(intent.getAction())) {
                receiveUnread((Unread) intent.getSerializableExtra("unread"));
            }
        }
    }

    ExitBroadcastReceiver mExitReceiver = null;

    class ExitBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (Constants.EXIT_APP.equals(intent.getAction())) {
                HomeActivity.this.finish();
            }
        }
    }

    /**
     * 处理收到的未计消息
     *
     * @param intent
     */
    private void receiveUnread(Unread unread) {
        if (null == unread && isFinishing()) {
            return;
        }

        int count = mSidebarAdapter.getCount();
        SidebarAdapter.SidebarEntry entry;
        boolean updateFlag = false;

        for (int i = 0; i < count; i++) {
            entry = (SidebarAdapter.SidebarEntry) mSidebarAdapter.getItem(i);
            if (Constants.TAB_ID_HOME.equals(entry.id)) {
                String msg = String.valueOf(unread.status);
                entry.setMsg(msg);
                TextView textView = mActionMsgView.get(Constants.TAB_ID_HOME);
                if (null != textView) {
                    if (unread.status > 0) {
                        textView.setText(msg);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setText(null);
                        textView.setVisibility(View.GONE);
                    }
                }
                WeiboLog.i(TAG, "新微博数:" + unread.status);
                updateFlag = true;
                continue;
            }

            if (Constants.TAB_ID_COMMENT.equals(entry.id)) {
                int total = unread.comments;
                String msg = String.valueOf(total);
                entry.setMsg(msg);
                TextView textView = mActionMsgView.get(Constants.TAB_ID_COMMENT);
                if (null != textView) {
                    if (total > 0) {
                        textView.setText(msg);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setText(null);
                        textView.setVisibility(View.GONE);
                    }
                }
                WeiboLog.i(TAG, "新评论数:" + total);
                updateFlag = true;
                continue;
            }

            if (Constants.TAB_ID_AT_COMMENT.equals(entry.id)) {
                int total = unread.mention_cmt;
                String msg = String.valueOf(total);
                entry.setMsg(msg);
                TextView textView = mActionMsgView.get(Constants.TAB_ID_AT_COMMENT);
                if (null != textView) {
                    if (total > 0) {
                        textView.setText(msg);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setText(null);
                        textView.setVisibility(View.GONE);
                    }
                }
                WeiboLog.i(TAG, "新评论数:" + total);
                updateFlag = true;
                continue;
            }

            if (Constants.TAB_ID_AT_STATUS.equals(entry.id)) {
                int total = unread.mention_status + unread.mention_cmt;
                String msg = String.valueOf(total);
                entry.setMsg(msg);
                TextView textView = mActionMsgView.get(Constants.TAB_ID_AT_STATUS);
                if (null != textView) {
                    if (total > 0) {
                        textView.setText(msg);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setText(null);
                        textView.setVisibility(View.GONE);
                    }
                }
                WeiboLog.i(TAG, "新at总数:" + entry.getMsg());
                updateFlag = true;
                continue;
            }

            if (Constants.TAB_ID_FOLLOWER.equals(entry.id)) {
                String msg = String.valueOf(unread.followers);
                entry.setMsg(msg);
                TextView textView = mActionMsgView.get(Constants.TAB_ID_FOLLOWER);
                if (null != textView) {
                    if (unread.followers > 0) {
                        textView.setText(msg);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setText(null);
                        textView.setVisibility(View.GONE);
                    }
                }
                WeiboLog.i(TAG, "新粉丝数:" + unread.followers);
                updateFlag = true;
            }

            if (Constants.TAB_ID_DIRECT_MSG.equals(entry.id)) {
                entry.setMsg(String.valueOf(unread.dm));
                WeiboLog.i(TAG, "新私信数:" + unread.dm);
                updateFlag = true;
            }
        }

        if (updateFlag) {
            mSidebarAdapter.notifyDataSetChanged();
        }
    }

    //--------------------- 自动更新操作 ---------------------
    private void checkUpdate() {
        UmengUpdateAgent.setUpdateOnlyWifi(false); // 目前我们默认在Wi-Fi接入情况下才进行自动提醒。如需要在其他网络环境下进行更新自动提醒，则请添加该行代码
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(updateListener);

        /*UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

            @Override
            public void OnDownloadEnd(int result) {
                WeiboLog.i(TAG, "download result : "+result);
                showToast("download result : "+result);
            }

        });*/

        UmengUpdateAgent.update(HomeActivity.this);
    }

    UmengUpdateListener updateListener = new UmengUpdateListener() {
        @Override
        public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
            if (isFinishing()) {
                return;
            }

            switch (updateStatus) {
                case 0: // has update
                    WeiboLog.i("callback result");
                    UmengUpdateAgent.showUpdateDialog(HomeActivity.this, updateInfo);
                    break;
                case 1: // has no update
                    //showToast("没有更新");
                    break;
                case 2: // none wifi
                    //showToast("没有wifi连接， 只在wifi下更新");
                    break;
                case 3: // time out
                    //showToast("超时");
                    break;
            }

        }
    };

    //--------------------- 分组操作 ---------------------

    ArrayAdapter<Group> mGroupAdapter;
    ArrayList<Group> mGroupList;

    private void addGroupNav() {
        if (null == mGroupList) {
            mGroupList = new ArrayList<Group>();
            Group homeGroup = new Group();
            homeGroup.name = getString(R.string.tab_label_home);
            homeGroup.id = Constants.TAB_ID_HOME;
            mGroupList.add(homeGroup);
        }

        WeiboLog.d(TAG, "mGroupList.size()" + mGroupList.size());
        if (mGroupList.size() == 1) {
            loadGroup();
        }

        mGroupAdapter = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, mGroupList);
        mGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mGroupItem.setAdapter(mGroupAdapter);
        mGroupItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                navigation(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mActionBar.setListNavigationCallbacks(mGroupAdapter, navigationListener);*/
    }

    ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            WeiboLog.d("Selected: " + itemPosition);
            navigation(itemPosition);
            return true;
        }
    };

    void navigation(int itemPosition) {
        final Fragment current = getFragmentManager().findFragmentById(R.id.fragment_placeholder);
        WeiboLog.v(TAG, "current:" + current.getTag());
        if (Constants.TAB_ID_HOME.equals(current.getTag())) {
            final int pos = itemPosition;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (current instanceof HomeFragment) {
                            HomeFragment homeFragment = (HomeFragment) current;
                            homeFragment.updateGroupTimeline(mGroupList.get(pos));
                        } else if (current instanceof HomeGridFragment) {
                            HomeGridFragment homeFragment = (HomeGridFragment) current;
                            homeFragment.updateGroupTimeline(mGroupList.get(pos));
                        }
                    } catch (Exception e) {
                        NotifyUtils.showToast("分组切换异常！");
                        e.printStackTrace();
                    }
                }
            }, 500l);
        }
    }

    private void loadGroup() {
        Handler groudHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ActionResult.ACTION_SUCESS: {
                        ActionResult actionResult = (ActionResult) msg.obj;
                        ArrayList<Group> groups = (ArrayList<Group>) actionResult.obj;
                        updateGroup(groups);
                        break;
                    }

                    case ActionResult.ACTION_FALL:
                        WeiboLog.d(TAG, "load group failed.");
                        break;

                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        long userId = mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
        String filepath = getFilesDir().getAbsolutePath() + "/" + String.valueOf(userId) + Constants.GROUP_FILE;
        AsyncActionTask task = new AsyncActionTask(HomeActivity.this, new GroupAction());
        task.execute(filepath, groudHandler);
    }

    private void updateGroup(final ArrayList<Group> mStatusData) {
        WeiboLog.d(TAG, "updateGroup:" + mStatusData.size());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGroupList.clear();
                Group homeGroup = new Group();
                homeGroup.name = getString(R.string.tab_label_home);
                homeGroup.id = Constants.TAB_ID_HOME;
                mGroupList.add(homeGroup);
                mGroupList.addAll(mStatusData);
                mGroupAdapter.notifyDataSetChanged();
            }
        }, 0l);
    }
}
