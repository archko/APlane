package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.AccountUsersFragment;
import cn.archko.microblog.fragment.DraftListFragment;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 帐户管理
 * @author: archko 12-10-17
 */
public class AccountUserActivity extends AbstractFragmentTabsPager {

    public static final String TAG="AccountUserActivity";
    public static final int TYPE_DRAFT_ONLY=1;   //只显示草稿tab
    public static final int TYPE_ACCOUNT_ONLY=2;   //只显示帐户tab
    public static final int TYPE_DRAFT_ACCOUNT=3;   //显示两个。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        /*Intent intent=getIntent();
        if (intent==null) {
            Toast.makeText(AccountUserActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int type=intent.getIntExtra("type", TYPE_USER_INFO);

        setIntent(intent);
        switchTab(type);*/

        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(R.string.action_account_user_manager);
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
    protected void addTab(ActionBar bar) {
        Intent intent=getIntent();
        if (intent==null) {
            Toast.makeText(AccountUserActivity.this, "抱歉，系统错误!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int type=intent.getIntExtra("type", TYPE_DRAFT_ACCOUNT);

        if (type==TYPE_DRAFT_ONLY) {
            addItem(DraftListFragment.class, -1, "草稿", R.string.tab_label_draft, bar, null);
        } else if (type==TYPE_ACCOUNT_ONLY) {
            addItem(AccountUsersFragment.class, -1, "帐户", R.string.tab_label_account, bar, null);
        } else {
            addItem(AccountUsersFragment.class, -1, "帐户", R.string.tab_label_account, bar, null);
            addItem(DraftListFragment.class, -1, "草稿", R.string.tab_label_draft, bar, null);
        }
    }

    /**
     * 切换不同的tab
     *
     * @param index tab索引，与上面的四个静态常量同。
     */
    void switchTab(int index) {
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