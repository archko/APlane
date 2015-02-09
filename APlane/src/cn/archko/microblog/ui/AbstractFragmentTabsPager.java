package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.LazyViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import com.andrew.apollo.utils.ThemeUtils;
import com.bulletnoid.android.widget.SwipeAwayLayout;
import com.me.microblog.util.WeiboLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @description:
 * @author: archko 13-6-20 :上午9:29
 */
public abstract class AbstractFragmentTabsPager extends SkinFragmentActivity implements OnRefreshListener {

    public static final String TAG="AbstractFragmentTabsPager";
    protected LazyViewPager mViewPager;
    protected ActionTabsAdapter mTabsAdapter;
    protected ActionBar.TabListener mTabListener;
    protected int currentTabIdx=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _onCreate(savedInstanceState);
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

    /**
     * 应该在onCreate里调用，不能放在别处，否则会出错。
     *
     * @param bundle
     */
    protected void _onCreate(Bundle bundle) {
        setContentView(R.layout.ak_tabs_pager);

        final ActionBar bar=getActionBar();
        mActionBar=bar;
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowTitleEnabled(false);  //隐藏主面的标题
        bar.setDisplayShowHomeEnabled(false);   //隐藏整个标题栏

        mViewPager=(LazyViewPager) findViewById(R.id.pager);
        mTabsAdapter=new ActionTabsAdapter(this, bar, mViewPager);

        mTabListener=new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mTabsAdapter.onTabItemChanged(bar.getSelectedNavigationIndex());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };
        addTab(bar);

        /*int theme=R.color.holo_dark_bg_view;
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {//if ("2".equals(themeId)) {
            theme=R.color.holo_light_bg_view;
        }

        //findViewById(R.id.fragment_placeholder).setBackgroundResource(R.drawable.ic_album_default_bg_blue);
        mViewPager.setBackgroundResource(theme);*/
        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), AbstractFragmentTabsPager.this);
    }

    protected abstract void addTab(ActionBar bar);

    protected void addItem(Class<?> fragmentClass, int iconId, String tag, int txtId, ActionBar bar, Bundle bundle) {
        ActionBar.Tab tab=null;
        tab=bar.newTab();
        if (iconId!=-1) {
            tab.setIcon(iconId);
        }

        if (txtId!=-1) {
            tab.setText(txtId);
        }
        tab.setTabListener(mTabListener);
        mTabsAdapter.addTab(tab, fragmentClass, bundle, tag);
    }

    protected void addItem(Class<?> fragmentClass, String tag, String txt, ActionBar bar) {
        ActionBar.Tab tab=null;
        tab=bar.newTab();
        tab.setText(txt);
        tab.setTabListener(mTabListener);
        mTabsAdapter.addTab(tab, fragmentClass, null, tag);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //TODO 这是一个bug。
        //super.onSaveInstanceState(outState);
        //outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    public class ActionTabsAdapter extends FragmentPagerAdapter
        implements LazyViewPager.OnPageChangeListener {

        private final Context mContext;
        private final ActionBar mTabHost;
        private final LazyViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs=new ArrayList<TabInfo>();
        private final SparseArray<WeakReference<Fragment>> mFragmentArray=new SparseArray<WeakReference<Fragment>>();

        final class TabInfo {

            private String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag=_tag;
                clss=_class;
                args=_args;
            }
        }

        public ActionTabsAdapter(Activity activity, ActionBar actionBar, LazyViewPager pager) {
            super(activity.getFragmentManager());
            mContext=activity;
            mTabHost=actionBar;
            mViewPager=pager;
            //mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tabSpec, Class<?> clss, Bundle args, String tag) {
            /*tabSpec.setContent(new DummyTabFactory(mContext));
            String tag=tabSpec.getTag();*/

            TabInfo info=new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        /**
         * 获取Fragment对应的位置，针对热门用户或微博的
         *
         * @param clazz 需要定货的Fragment
         * @return
         */
        public int getFragmentItem(Class clazz) {
            int index=-1;
            TabInfo tabInfo;
            for (int i=0; i<mTabs.size(); i++) {
                tabInfo=mTabs.get(i);
                if (tabInfo.clss==clazz) {
                    WeiboLog.d("找到对应的Fragment为："+clazz+" 位置:"+i);
                    index=i;
                    break;
                }
            }
            return index;
        }

        /**
         * 更新tab的数据，现在用于消息计数
         */
        public void updateTab(int pos, String newTag) {
            mTabHost.getTabAt(pos).setText(newTag);
            WeiboLog.i("pos:"+pos+" newTag:"+newTag);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            WeiboLog.v(TAG, "getItem:"+position);
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                return mWeakFragment.get();
            }

            TabInfo info=mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            WeiboLog.v("instantiateItem:"+position);
            WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                //mWeakFragment.clear();
                return mWeakFragment.get();
            }

            final Fragment mFragment=(Fragment) super.instantiateItem(container, position);
            mFragmentArray.put(position, new WeakReference<Fragment>(mFragment));
            return mFragment;
        }

        /**
         * {@inheritDoc}
         */
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
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            /*TabWidget widget=mTabHost.getTabWidget();
            int oldFocusability=widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);*/
            WeiboLog.d("onPageSelected:"+position);
            mTabHost.selectTab(mTabHost.getTabAt(position));
            updateTitle(position);
            selectPage(position);
            /*final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                Fragment fragment=mWeakFragment.get();
                if (fragment instanceof AbsStatusAbstraction) {
                    AbsStatusAbstraction homeBaseFragment=(AbsStatusAbstraction) fragment;
                    homeBaseFragment.onSelected();
                }
            }*/
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        public void onTabItemChanged(int index) {
            mViewPager.setCurrentItem(index);
            WeiboLog.d("onTabItemChanged.index:"+index);
        }

        /**
         * 在使用TabPageIndicator时有用到，否则Tab无法显示标题。
         *
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).tag;
        }
    }

    protected void selectPage(int position) {
    }

    protected void updateTitle(int index) {
    }
}
