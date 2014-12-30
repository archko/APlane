package com.me.microblog.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.me.microblog.R;
import com.me.microblog.bean.Trend;
import com.me.microblog.bean.Trends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 主要用于测试.
 *
 * @author archko
 */
public class TestFunctionActivity extends Activity {

    private static final String TAG = "TestFunctionActivity";
    private ListView mListView;
    private Button hourly, daily, weekly;
    private SimpleAdapter mAdapter;
    private Button test, testurlBtn;
    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.test) {
                changeScreen();
                return;
            } else if (v.getId() == R.id.testurl) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://weibo.cn"));
                startActivity(intent);
            }
            /*String type;
               switch (v.getId()) {
               case R.id.hourly:
                   type = TestFunctionActivity.this.types[0];
                   break;
               case R.id.daily:
                   type = types[1];
                   break;
               case R.id.weekly:
                   type = types[2];
                   break;
               default:
                   type = types[2];
                   break;
               }
               LoadTrendsTask task = new LoadTrendsTask();
               task.execute(type);*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topics);

        mListView = (ListView) findViewById(R.id.topicList);

        hourly = (Button) findViewById(R.id.hourly);
        daily = (Button) findViewById(R.id.daily);
        weekly = (Button) findViewById(R.id.weekly);
        test = (Button) findViewById(R.id.test);
        testurlBtn = (Button) findViewById(R.id.testurl);

        hourly.setOnClickListener(clickListener);
        daily.setOnClickListener(clickListener);
        weekly.setOnClickListener(clickListener);
        test.setOnClickListener(clickListener);
        testurlBtn.setOnClickListener(clickListener);
    }

    private boolean fullScreen = true;

    /**
     * 改变屏幕,可以隐藏下方的导航栏
     */
    private void changeScreen() {
        Intent intent = new Intent();
        intent.setAction("tab_fi");
        intent.putExtra("fullScreen", fullScreen);
        sendBroadcast(intent);
        fullScreen = ! fullScreen;
    }

    String[] types = {"hourly", "daily", "weekly"};
    String[] from = {"name", "query"};
    int[] to = {android.R.id.text1, android.R.id.text2};
    private ArrayList trendList = new ArrayList<Map<String, String>>();

    class LoadTrendsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, String> itemMap = new HashMap<String, String>();
                /*Trends tmp=((WeiboApi)App.getMicroBlog(TestFunctionActivity.this)).getTrends(params[0]);
                Log.i(TAG,"trends:"+tmp);
                addValue(tmp);*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void resultObj) {
            int len = trendList.size();
            if (len == 0) {
                Toast.makeText(getApplicationContext(), "no results", Toast.LENGTH_LONG)
                    .show();
            } else {
                Log.i(TAG, "size:" + trendList.size());
                mAdapter = new SimpleAdapter(getApplicationContext(), trendList,
                    android.R.layout.simple_list_item_2, from, to);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void addValue(Trends trends) {
        trendList.clear();
        if (trends != null && trends.trends != null && trends.trends.length > 0) {
            for (Trend trend : trends.trends) {
                Map<String, String> itemMap = new HashMap<String, String>();
                itemMap.put("name", trend.name);
                itemMap.put("query", trend.query);
                trendList.add(itemMap);
            }
        }
    }
}
