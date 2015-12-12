package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.TaskListFragment;
import cn.archko.microblog.fragment.abs.OnRefreshListener;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-9-8
 */
public class TaskQueueActivity extends SkinFragmentActivity implements OnRefreshListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar=getActionBar();
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        //mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        mActionBar.setTitle(R.string.tab_label_send_task);

        Fragment newFragment=Fragment.instantiate(this, TaskListFragment.class.getName());
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.add(android.R.id.content, newFragment).commit();
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