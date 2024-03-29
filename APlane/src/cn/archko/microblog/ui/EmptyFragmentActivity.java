package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 一个FragmentActivity，用于启动不同的Fragment。
 * @author: archko 12-12-3
 */
public class EmptyFragmentActivity extends SkinFragmentActivity implements OnRefreshListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar=getActionBar();
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        //mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        initFragment();
    }

    public void initFragment() {
        Intent intent=getIntent();
        if (null==intent) {
            NotifyUtils.showToast("System error, no intent!");
            finish();
            return;
        }

        String title=intent.getStringExtra("title");
        mActionBar.setTitle(title);
        String className=intent.getStringExtra("fragment_class");

        try {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("start a fragment:"+title+" fragment Class:"+className);
            }
            Fragment old=getFragmentManager().findFragmentById(android.R.id.content);
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("initFragment."+className+" old:"+old);
            }
            if (null==old) {
                Fragment newFragment=Fragment.instantiate(this, className);
                Bundle args=intent.getExtras();
                if (null!=args) {
                    newFragment.setArguments(args);
                }
                FragmentTransaction ft=getFragmentManager().beginTransaction();
                ft.add(android.R.id.content, newFragment).commit();
            } else {
                Fragment newFragment=Fragment.instantiate(this, className);
                FragmentTransaction ft=getFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, newFragment);
                //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                //ft.addToBackStack(null);
                /*if (bundle.getBoolean("add_to_back_stack", false)) {
                    ft.addToBackStack(null);
                }*/
                ft.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNewFragment(Fragment newFragment) {
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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
}