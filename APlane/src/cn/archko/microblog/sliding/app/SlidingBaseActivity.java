package cn.archko.microblog.sliding.app;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author archko
 */
@Deprecated
public class SlidingBaseActivity extends SlidingFragmentActivity {

    private int mTitleRes;
    //protected ListFragment mFrag;

    public SlidingBaseActivity() {
    }

    public SlidingBaseActivity(int titleRes) {
        mTitleRes=titleRes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (mTitleRes>0) {
            setTitle(mTitleRes);
        }

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        FragmentTransaction t=this.getSupportFragmentManager().beginTransaction();
        mFrag=new SampleListFragment();
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        // customize the SlidingMenu
        SlidingMenu sm=getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);*/

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class BasePagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments=new ArrayList<Fragment>();
        private ViewPager mPager;

        public BasePagerAdapter(FragmentManager fm, ViewPager vp) {
            super(fm);
            mPager=vp;
            mPager.setAdapter(this);
            for (int i=0; i<3; i++) {
                //addTab(new SampleListFragment());
            }
        }

        public void addTab(Fragment frag) {
            mFragments.add(frag);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }

}
