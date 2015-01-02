package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.UserFollowersGridFragment;
import cn.archko.microblog.fragment.UserFriendsGridFragment;
import cn.archko.microblog.fragment.UserInfoFragment;
import cn.archko.microblog.fragment.UserTimelineFragment;
import cn.archko.microblog.fragment.abs.AbstractBaseFragment;
import cn.archko.microblog.view.SlidingTabLayout;
import com.andrew.apollo.utils.PreferenceUtils;
import com.bulletnoid.android.widget.SwipeAwayLayout;
import com.me.microblog.App;
import com.me.microblog.bean.User;
import com.me.microblog.util.WeiboLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.00.00
 * @description: 这是一个Fragment，用户信息，主要由原来的用户详细信息Activity与用户微博，用户关注与粉丝Activity组成的。
 * 现在由Fragment显示。
 * @author: archko 12-05-5
 */
public class UserFragmentActivity extends SkinFragmentActivity {

    public static final String TAG="UserFragmentActivity";
    User mUser; //当前查看的用户。
    public static final int TYPE_USER_INFO=0;   //显示用户信息tab
    public static final int TYPE_USER_FRIENDS=1;    //显示用户关注列表
    public static final int TYPE_USER_FOLLOWERS=2;  //显示用户粉丝列表
    public static final int TYPE_USER_TIMELINE=3;   //显示用户微博列表
    public static final int TYPE_USER_SELF=11;   //显示用户自己
    ViewPager mViewPager;
    TabsAdapter mPagerAdapter;
    final String[] titles=new String[2];

    /**
     * List of {@link SamplePagerItem} which represent this sample's tabs.
     */
    protected List<SamplePagerItem> mTabs=new ArrayList<SamplePagerItem>();
    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    protected SlidingTabLayout mSlidingTabLayout;

    /**
     * This class represents a tab to be displayed by {@link android.support.v4.view.ViewPager} and it's associated
     * {@link cn.me.archko.pdf.SlidingTabLayout}.
     */
    static class SamplePagerItem {

        private final CharSequence mTitle;
        private final int mIndicatorColor;
        private final int mDividerColor;
        private final Class<?> clss;
        private final Bundle args;

        SamplePagerItem(Class<?> _class, Bundle _args, CharSequence title) {
            clss=_class;
            args=_args;
            mTitle=title;
            mIndicatorColor=App.getAppContext().getResources().getColor(R.color.tab_indicator_selected_color);
            mDividerColor=Color.GRAY;
        }

        SamplePagerItem(Class<?> _class, Bundle _args, CharSequence title, int indicatorColor, int dividerColor) {
            clss=_class;
            args=_args;
            mTitle=title;
            mIndicatorColor=indicatorColor;
            mDividerColor=dividerColor;
        }

        /**
         * @return the title which represents this tab. In this sample this is used directly by
         * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
         */
        CharSequence getTitle() {
            return mTitle;
        }

        /**
         * @return the color to be used for indicator on the {@link SlidingTabLayout}
         */
        int getIndicatorColor() {
            return mIndicatorColor;
        }

        /**
         * @return the color to be used for right divider on the {@link SlidingTabLayout}
         */
        int getDividerColor() {
            return mDividerColor;
        }
    }

    protected void postSlidingTabLayout() {
        int changeConfig=getChangingConfigurations();
        int orientation=getResources().getConfiguration().orientation;
        //WeiboLog.v(TAG, "changeConfig:"+changeConfig+" orientation:"+orientation);
        if (orientation==Configuration.ORIENTATION_PORTRAIT) {
            mSlidingTabLayout.setMatchWidth(true);
        } else {
            mSlidingTabLayout.setMatchWidth(false);
        }
        // BEGIN_INCLUDE (tab_colorizer)
        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
//                LLog.d("JSSlidingTabsColorsFragment", "*****getIndicatorColor******"+mTabs.get(position).getTitle());
                return mTabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
//                LLog.d("JSSlidingTabsColorsFragment", "******getDividerColor*****"+mTabs.get(position).getTitle());
                return mTabs.get(position).getDividerColor();
            }

        });
    }

    protected void addTab() {
        int user_type=getIntent().getIntExtra("user_type", -1);
        WeiboLog.d("user_type:"+user_type);
        Bundle bundle;
        String title;
        if (user_type==-1) {
            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.user_info);
            mTabs.add(new SamplePagerItem(UserInfoFragment.class, bundle, title, Color.BLUE, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.friends);
            mTabs.add(new SamplePagerItem(UserFriendsGridFragment.class, bundle, title, Color.RED, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.followers);
            mTabs.add(new SamplePagerItem(UserFollowersGridFragment.class, bundle, title, Color.YELLOW, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.statuses);
            mTabs.add(new SamplePagerItem(UserTimelineFragment.class, bundle, title, Color.GREEN, Color.GRAY));
        } else if (user_type==TYPE_USER_SELF) {
            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.user_info);
            mTabs.add(new SamplePagerItem(UserInfoFragment.class, bundle, title, Color.BLUE, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.friends);
            mTabs.add(new SamplePagerItem(UserFriendsGridFragment.class, bundle, title, Color.RED, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.followers);
            mTabs.add(new SamplePagerItem(UserFollowersGridFragment.class, bundle, title, Color.YELLOW, Color.GRAY));

            bundle=new Bundle();
            bundle.putInt("type", 1);
            title=getString(R.string.statuses);
            mTabs.add(new SamplePagerItem(UserTimelineFragment.class, bundle, title, Color.GREEN, Color.GRAY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter} used to display pages in this sample. The individual pages
     * are instances of {@link ContentFragment} which just display three lines of text. Each page is
     * created by the relevant {@link SamplePagerItem} for the requested position.
     * <p/>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link SlidingTabLayout}.
     */
    public class TabsAdapter extends FragmentPagerAdapter
        implements ViewPager.OnPageChangeListener {

        Handler mHandler=new Handler();
        private final Context mContext;
        private final SparseArray<WeakReference<Fragment>> mFragmentArray=new SparseArray<WeakReference<Fragment>>();
        int position=0;

        public TabsAdapter(Activity activity) {
            super(activity.getFragmentManager());
            mContext=activity;
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                return mWeakFragment.get();
            }

            SamplePagerItem info=mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            Log.v(TAG, "instantiateItem:"+position);
            WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                //mWeakFragment.clear();
                return mWeakFragment.get();
            }

            final Fragment mFragment=(Fragment) super.instantiateItem(container, position);
            mFragmentArray.put(position, new WeakReference<Fragment>(mFragment));
            return mFragment;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            super.destroyItem(container, position, object);
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null) {
                mWeakFragment.clear();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mViewPager.setCurrentItem(position);
            //delayUpdate(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }

        void delayUpdate(final int position) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, 500L);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置ActionBar 浮动到view 上层来
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //设置ActionBar 背景色 透明
        //getActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        //设置半透明的底色
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        //getActionBar().setSplitBackgroundDrawable(new ColorDrawable(R.color.transparent));
        Intent intent=getIntent();
        if (intent==null) {
            Toast.makeText(UserFragmentActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.ak_sliding_tabs_pager);
        mActionBar=getActionBar();
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

        mViewPager=(ViewPager) findViewById(R.id.pager);

        mSlidingTabLayout=(SlidingTabLayout) findViewById(R.id.sliding_tabs);
        addTab();
        mPagerAdapter=new TabsAdapter(this);
        mViewPager.setOnPageChangeListener(mPagerAdapter);
        mViewPager.setAdapter(mPagerAdapter);

        postSlidingTabLayout();
        // END_INCLUDE (tab_colorizer)
        // END_INCLUDE (setup_slidingtablayout)
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectPageListener(new SlidingTabLayout.SelectPageListener() {
            @Override
            public void updateSubView(int position) {
                ((AbstractBaseFragment) mPagerAdapter.getItem(position)).refresh();
            }
        });

        switchTab(type);
        SwipeAwayLayout view_root=(SwipeAwayLayout) findViewById(R.id.view_root);
        view_root.setSwipeOrientation(SwipeAwayLayout.LEFT_RIGHT);

        view_root.setOnSwipeAwayListener(new SwipeAwayLayout.OnSwipeAwayListener() {
            @Override
            public void onSwipedAway(int mCloseOrientation) {
                finish();
                int animId=R.anim.exit_left;
                if (mCloseOrientation==SwipeAwayLayout.RIGHT_ONLY) {
                    animId=R.anim.exit_to_left;
                }
                overridePendingTransition(0, animId);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT) {
            mSlidingTabLayout.setMatchWidth(true);
        } else {
            mSlidingTabLayout.setMatchWidth(false);
        }
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
        AbstractBaseFragment current=(AbstractBaseFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+index);
        WeiboLog.d(TAG, "refresh.current:"+current);
        current.refresh();
    }

    protected void selectPage(int position) {
        ((AbstractBaseFragment) mPagerAdapter.getItem(position)).refresh();
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

}
