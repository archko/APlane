package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.OnRefreshListener;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import cn.archko.microblog.utils.WeiboOperation;
import cn.archko.microblog.view.ThreadBeanItemView;
import com.me.microblog.App;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.core.sina.SinaSearchApi;
import cn.archko.microblog.utils.AKUtils;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.Map;

/**
 * @version 1.00.00
 * @description: 这是一个主界面，采用Fragment与修改过的ViewPager
 * @author: archko 11-12-25
 */
public class SearchActivity extends BaseOauthActivity implements OnRefreshListener {

    public static final String TAG="SearchActivity";
    private SharedPreferences mPreferences;
    EditText mSearchText;
    ImageView mSearchBtn;
    RadioButton mStatusRb, mUserRb;
    RadioGroup mRadioGroup;
    ListView mListView;
    ArrayList<Map<String, String>> mDataList;
    /**
     * 0表示搜索微博，1为用户
     */
    int mode=0;
    boolean isSearching=false;
    Handler mHandler=new Handler();
    TimeLineAdapter mAdapter;
    ProgressDialog mProgressDialog;
    InputMethodManager imm;

    /**
     * 话题的搜索结果
     */
    protected ArrayList<Status> mTopicStatus;
    protected SStatusData<Status> mStatusData;
    protected TopicLineAdapter mTopicAdapter;
    int mTopicCount=30;
    int page=1;
    SinaSearchApi mSinaSearchApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);

        WeiboLog.d(TAG, "onCreate");
        mPreferences=PreferenceManager.getDefaultSharedPreferences(SearchActivity.this);
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        doInit();
        mDataList=new ArrayList<Map<String, String>>();
        mAdapter=new TimeLineAdapter();
        mListView.setAdapter(mAdapter);

        mSinaSearchApi=new SinaSearchApi();
        mSinaSearchApi.updateToken();
    }

    private void doInit() {
        mSearchText=(EditText) findViewById(R.id.search_et);
        mSearchBtn=(ImageView) findViewById(R.id.search_btn);
        //TODO需要处理搜索按钮的切换，黑色时看不清楚。
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        WeiboLog.d(TAG, "themeid;"+themeId);
        /*if ("0".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_dark);
        } else if ("1".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_dark);
        } else if ("2".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_light);
        } else if ("3".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_light);
        }*/

        mStatusRb=(RadioButton) findViewById(R.id.status_rb);
        mUserRb=(RadioButton) findViewById(R.id.user_rb);
        mRadioGroup=(RadioGroup) findViewById(R.id.search_group);
        mListView=(ListView) findViewById(R.id.statusList);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doItemClick(parent, view, position, id);
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(v);
            }
        });

        mStatusRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WeiboLog.d(TAG, "微博按钮选择状态改变了。"+isChecked);
                if (null==mTopicStatus) {
                    mTopicStatus=new ArrayList<Status>();
                } else {
                    mTopicStatus.clear();
                }

                if (null==mDataList) {
                    mDataList=new ArrayList<Map<String, String>>();
                } else {
                    mDataList.clear();
                }

                if (isChecked) {
                    if (null==mTopicAdapter) {
                        mTopicAdapter=new TopicLineAdapter();
                    }
                    mTopicAdapter.notifyDataSetChanged();
                    mListView.setAdapter(mTopicAdapter);
                } else {
                    if (null==mAdapter) {
                        mAdapter=new TimeLineAdapter();
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setAdapter(mAdapter);
                }
            }
        });

        final ActionBar bar=getActionBar();
        bar.show();
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowTitleEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setTitle(R.string.action_search);

        ThemeUtils.getsInstance().themeActionBar(getActionBar(), this);
        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), this);
    }

    /**
     * 搜索按钮点击时处理搜索，但需要注意，mode作为搜索的关键，点击搜索按钮后内容正常才可以搜索，才改变mode
     *
     * @param view
     */
    private void search(View view) {
        final String q=mSearchText.getEditableText().toString();
        if (TextUtils.isEmpty(q)) {
            Toast.makeText(SearchActivity.this, "请输入要搜索的内容。", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mProgressDialog==null) {
            mProgressDialog=new ProgressDialog(SearchActivity.this);
        }

        mProgressDialog.show();

        //if (!isSearching) { //只有搜索才改变当前的搜索状态。
        int id=mRadioGroup.getCheckedRadioButtonId();
        if (id==R.id.status_rb) {
            mode=0;
            doSearchTopic(q);
            return;
        } else if (id==R.id.user_rb) {
            mode=1;
        }

        mDataList.clear();
        mAdapter.notifyDataSetChanged();
        doSearch(q);
        /*} else {
            WeiboLog.d(TAG, "正在搜索。");
        }*/
    }

    /**
     * 搜索话题 。
     *
     * @param q
     */
    private void doSearchTopic(final String q) {
        WeiboLog.d(TAG, "doSearchTopic");
        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        isSearching=true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(SearchActivity.this);
                    final SStatusData<Status> data=mSinaSearchApi.searchTopics(q, mTopicCount, page);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishSearchTopic(data);
                        }
                    }, 0l);
                } catch (Exception e) {
                    final String msg=e.toString();
                    e.printStackTrace();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AKUtils.showToast("搜索失败："+msg);
                        }
                    }, 0l);
                } finally {
                    isSearching=false;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null!=mProgressDialog&&mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                        }
                    }, 0l);
                }
            }
        }).start();
    }

    void finishSearchTopic(SStatusData<Status> data) {
        if (null==data) {
            AKUtils.showToast("搜索失败：");
            return;
        }
        WeiboLog.d(TAG, "finishSearchTopic:");
        if (data.errorCode>0&&!TextUtils.isEmpty(data.errorMsg)) {
            AKUtils.showToast("搜索失败："+data.errorMsg);
            WeiboLog.i(TAG, "搜索失败："+data.errorCode+" msg:"+data.errorMsg);
            return;
        }

        ArrayList<Status> statuses=data.mStatusData;
        if (null!=data&&(null==statuses||statuses.size()<1)) {
            AKUtils.showToast("搜索结果为空：");
            WeiboLog.i(TAG, "搜索结果为空：");
            return;
        }

        mTopicStatus.clear();
        mTopicStatus.addAll(statuses);
        mTopicAdapter.notifyDataSetChanged();
    }

    void doSearch(final String q) {
        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        isSearching=true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(SearchActivity.this);
                    final ArrayList<Map<String, String>> data=mSinaSearchApi.getSearchSuggestions(q, 20, mode);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishSearch(data);
                        }
                    }, 0l);
                } catch (Exception e) {
                    final String msg=e.toString();
                    e.printStackTrace();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AKUtils.showToast("搜索失败："+msg);
                        }
                    }, 0l);
                } finally {
                    isSearching=false;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null!=mProgressDialog&&mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                        }
                    }, 0l);
                }
            }
        }).start();
    }

    void finishSearch(ArrayList<Map<String, String>> data) {
        WeiboLog.d(TAG, "data:"+data.size());
        mDataList.clear();
        if (null==data||data.size()<1) {
            AKUtils.showToast("搜索结果为空！");
            WeiboLog.w(TAG, "搜索结果为空");
            mAdapter.notifyDataSetChanged();
            return;
        }

        mDataList.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    private void doItemClick(AdapterView<?> parent, View view, int position, long id) {
        WeiboLog.d(TAG, "pos:"+position+" mode:"+mode);
        try {
            if (mode==0) {
                doStatusClick(position);
            } else if (mode==1) {
                doUserClick(mDataList.get(position));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doStatusClick(int position) {
        Status status=mTopicStatus.get(position);
        WeiboOperation.toViewOriginalStatus(SearchActivity.this, status);
    }

    private void doUserClick(Map<String, String> map) {
        String screenName=map.get("screen_name");
        String uid=map.get("uid");
        WeiboLog.d(TAG, "screen_name:"+screenName+" followers_count:"+map.get("followers_count")+" uid:"+uid);
        Intent intent=new Intent(SearchActivity.this, UserFragmentActivity.class);
        intent.putExtra("nickName", screenName);
        intent.putExtra("user_id", uid);
        intent.putExtra("type", UserFragmentActivity.TYPE_USER_INFO);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_right, R.anim.enter_left);
    }

    @Override
    public void onRefreshStarted() {
        //setSupportProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onRefreshFinished() {
        //WeiboLog.d(TAG, "onRefreshFinished.");
        //setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onRefreshFailed() {
        WeiboLog.d(TAG, "onRefreshFailed.");
        //setSupportProgressBarIndeterminateVisibility(false);
    }

    public class TimeLineAdapter extends BaseAdapter {

        public TimeLineAdapter() {
            WeiboLog.d(TAG, "CommentsFragment.TimeLineAdapter:");
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String, String> data=mDataList.get(position);

            SearchItemView itemView=null;
            if (null==convertView) {
                itemView=new SearchItemView(SearchActivity.this);
            } else {
                itemView=(SearchItemView) convertView;
            }

            if (mode==0) {
                itemView.update(String.valueOf(data.get("suggestion")), String.valueOf(data.get("count")));
            } else if (mode==1) {
                itemView.update(String.valueOf(data.get("screen_name")), String.valueOf(data.get("followers_count")));
            }

            return itemView;
        }
    }

    public class TopicLineAdapter extends BaseAdapter {

        public TopicLineAdapter() {
            WeiboLog.d(TAG, "TopicLineAdapter");
        }

        @Override
        public int getCount() {
            return mTopicStatus.size();
        }

        @Override
        public Object getItem(int i) {
            return mTopicStatus.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ThreadBeanItemView itemView=null;
            Status status=mTopicStatus.get(position);

            boolean updateFlag=true;
            /*if (mScrollState==AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                updateFlag=false;
            }*/

            if (convertView==null) {
                itemView=new ThreadBeanItemView(SearchActivity.this, mListView, App.mCacheDir, status, updateFlag, false, false, true);
            } else {
                itemView=(ThreadBeanItemView) convertView;
            }
            itemView.update(status, updateFlag, false, false, true);

            return itemView;
        }
    }

    private class SearchItemView extends LinearLayout {

        private ListView parent;
        private TextView mTitle;
        private TextView mMsg;    //

        private SearchItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.sidebar_item, this);
            setMinimumHeight(40);
            mTitle=(TextView) findViewById(R.id.title);
            mMsg=(TextView) findViewById(R.id.msg);
            findViewById(R.id.image).setVisibility(View.GONE);
        }

        public void update(String text1, String text2) {
            mTitle.setText(text1);
            mMsg.setText("粉丝："+text2);
        }
    }
}
