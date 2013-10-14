package cn.archko.microblog.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.TestHomeFragment;

/**
 * @author: archko Date: 13-1-28 Time: 下午8:07
 * @description:
 */
public class TestApiActivity extends EmptyFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            TextView txt=new TextView(TestApiActivity.this);
            setContentView(txt);
            txt.setTextSize(30f);
            txt.setTextColor(R.color.DeepSkyBlue);
            String action=getIntent().getStringExtra("action");
            Log.d("", "action:"+action);
            txt.setText(action);
        } catch (Exception e) {
        }
    }

    public void initFragment() {
        /*String title=intent.getStringExtra("title");
        mActionBar.setTitle(title);
        String className=intent.getStringExtra("fragment_class");

        try {
            WeiboLog.d("start a fragment:"+title+" fragment Class:"+className);
            Fragment newFragment=Fragment.instantiate(this, className);
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, newFragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            Fragment newFragment=Fragment.instantiate(this, TestHomeFragment.class.getName());
            FragmentTransaction ft=getFragmentManager().beginTransaction();
            ft.add(android.R.id.content, newFragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
