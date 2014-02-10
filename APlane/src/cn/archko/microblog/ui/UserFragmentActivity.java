package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.UserFollowersGridFragment;
import cn.archko.microblog.fragment.UserFriendsGridFragment;
import cn.archko.microblog.fragment.UserInfoFragment;
import cn.archko.microblog.fragment.UserTimelineFragment;
import cn.archko.microblog.fragment.abs.BaseFragment;
import com.andrew.apollo.utils.PreferenceUtils;
import com.bulletnoid.android.widget.SwipeAwayLayout;
import com.me.microblog.App;
import com.me.microblog.bean.User;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 这是一个Fragment，用户信息，主要由原来的用户详细信息Activity与用户微博，用户关注与粉丝Activity组成的。
 * 现在由Fragment显示。
 * @author: archko 12-05-5
 */
public class UserFragmentActivity extends AbstractFragmentTabsPager {

    public static final String TAG="UserFragmentActivity";
    User mUser; //当前查看的用户。
    public static final int TYPE_USER_INFO=0;   //显示用户信息tab
    public static final int TYPE_USER_FRIENDS=1;    //显示用户关注列表
    public static final int TYPE_USER_FOLLOWERS=2;  //显示用户粉丝列表
    public static final int TYPE_USER_TIMELINE=3;   //显示用户微博列表
    public static final int TYPE_USER_SELF=11;   //显示用户自己

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        if (intent==null) {
            Toast.makeText(UserFragmentActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        //mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(R.string.user_info);

        int type=intent.getIntExtra("type", TYPE_USER_INFO);

        /*User user=new User("草根原创精选");
        user.id=3443049411850278L;
        intent.putExtra("nickName", user.screenName);
        intent.putExtra("user_id",user.id);*/

        setIntent(intent);
        switchTab(type);
    }

    @Override
    protected void _onCreate(Bundle bundle) {
        super._onCreate(bundle);
        //mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mViewPager.setOffscreenPageLimit(3);
        SwipeAwayLayout view_root=(SwipeAwayLayout) findViewById(R.id.view_root);
        view_root.setSwipeOrientation(SwipeAwayLayout.LEFT_RIGHT);

        view_root.setOnSwipeAwayListener(new SwipeAwayLayout.OnSwipeAwayListener() {
            @Override
            public void onSwipedAway() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.id.menu_refresh, 1, R.string.opb_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int refreshId=R.drawable.navigation_refresh_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            refreshId=R.drawable.navigation_refresh_light;
        }
        menu.findItem(R.id.menu_refresh).setIcon(refreshId);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            finish();
        } else if (itemId==R.id.menu_refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        int index=mViewPager.getCurrentItem();
        BaseFragment current=(BaseFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+index);
        WeiboLog.d(TAG, "refresh.current:"+current);
        current.refresh();
    }

    @Override
    protected void addTab(ActionBar bar) {
        int user_type=getIntent().getIntExtra("user_type", -1);
        WeiboLog.d("user_type:"+user_type);
        Bundle bundle=new Bundle();
        bundle.putInt("type", 1);
        if (user_type==-1) {
            addItem(UserInfoFragment.class, -1, getString(R.string.user_info), R.string.user_info, bar, null);
            //addItem(UserInfoFragment.class, -1, getString(R.string.user_info), R.string.user_info, bar);
            //addItem(UserFriendsFragment.class, -1, getString(R.string.friends), R.string.friends, bar);
            addItem(UserFriendsGridFragment.class, -1, getString(R.string.friends), R.string.friends, bar, bundle);
            addItem(UserFollowersGridFragment.class, -1, getString(R.string.followers), R.string.followers, bar, bundle);
            addItem(UserTimelineFragment.class, -1, getString(R.string.statuses), R.string.statuses, bar, null);
        } else if (user_type==TYPE_USER_SELF) {
            addItem(UserInfoFragment.class, -1, getString(R.string.user_info), R.string.user_info, bar, null);
            //addItem(UserInfoFragment.class, -1, getString(R.string.user_info), R.string.user_info, bar);
            //addItem(UserFriendsFragment.class, -1, getString(R.string.friends), R.string.friends, bar);
            addItem(UserFriendsGridFragment.class, -1, getString(R.string.friends), R.string.friends, bar, bundle);
            addItem(UserFollowersGridFragment.class, -1, getString(R.string.followers), R.string.followers, bar, bundle);
            addItem(UserTimelineFragment.class, -1, getString(R.string.statuses), R.string.statuses, bar, null);
        }
    }

    @Override
    protected void selectPage(int position) {
        ((BaseFragment) mTabsAdapter.getItem(position)).refresh();
    }

    /**
     * 切换不同的tab
     *
     * @param index tab索引，与上面的四个静态常量同。
     */
    public void switchTab(int index) {
        WeiboLog.d("switchTab,"+index);
        mViewPager.setCurrentItem(index);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        WeiboLog.d("onNewIntent:"+intent);
    }

    //-----------------------

    @Override
    public void onRefreshStarted() {
    }

    @Override
    public void onRefreshFinished() {
    }

    @Override
    public void onRefreshFailed() {
    }
}
