package cn.archko.microblog.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import cn.archko.microblog.R;

/**
 * 处理不同的导航Activity
 *
 * @author archko date:2012-10-12
 */
public class NavModeActivity extends SkinFragmentActivity {

    public static final String TAG="NavModeActivity";

    //----------------------------
    protected SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        int theme=R.style.Theme_AK_Light;

        setTheme(theme);
        getActionBar().hide();
    }

    protected void startIntent() {
        boolean navTab=false;//mPreferences.getBoolean(PrefsActivity.PREF_NAV_TAB, false);

        Intent intent=null;
        if (navTab) {
            //intent=new Intent(NavModeActivity.this, FragmentTabActivity.class);
            //intent=new Intent(NavModeActivity.this, HomeTabActivity.class);
        } else {
            intent=new Intent(NavModeActivity.this, HomeActivity.class);
        }
        startActivity(intent);

        //WeiboLog.v("导航类型为："+navTab+" --"+intent);

        this.finish();
    }
}
