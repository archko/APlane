package cn.archko.microblog.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.abs.AtUserListener;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Trend;
import com.me.microblog.bean.Trends;
import com.me.microblog.bean.User;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.sina.SinaSearchApi;
import com.me.microblog.core.sina.SinaTrendApi;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.StreamUtils;
import com.me.microblog.util.WeiboLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * 弹出的对话框,使用Fragment为了更好地处理数据,这里需要处理@,#.不仅要查询数据库的,还要查询网络的.
 * 目前只处理一次记录没有分页.
 *
 * @author archko Date: 12-12-6 Time: 下午3:21
 */
public class SearchDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public SharedPreferences mPrefs;
    public long currentUserId=-1l;
    AtUserListener mAtUserListener;
    SimpleAdapter mSimpleAdapter;
    ListView mListView;
    TextView mTitle;
    EditText mSearch;
    Button mFriendBtn;
    Button mSugguestBtn;
    Button mTrendsBtn;
    Button mRecentBtn;
    RelativeLayout mHeaderLayout;
    RelativeLayout mFooterLayout;
    TextView mName;
    LinearLayout mLoadingLayout;
    ImageView mSearchBtn;
    ImageView mSearchCloseBtn;
    /**
     * 当前的搜索模式，是搜索分类的处理，搜索主页的用户或是从网络上搜索，
     * 0表示搜索用户建议，1表示默认搜索主页关注的对象。2表示搜索话题。3,表示最近@的人.
     * 最近@的功能先隐藏.
     */
    int mSearchMode=0;
    /**
     * 是否在搜索的标志位。
     */
    boolean isSearching=false;
    QueryTask mQueryTask;
    InputMethodManager imm;

    /**
     * 更多的FooterView
     */
    protected RelativeLayout mRelativeLoadingLayout;
    protected ProgressBar mMoreProgressBar;

    /**
     * 显示更多,如果加载失败,要显示不同的文字.
     */
    protected TextView mMoreTxt;
    SinaUserApi mSinaUserApi;
    SinaSearchApi mSinaSearchApi;
    SinaTrendApi mSinaTrendApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_NoTitleBar);
        mPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        this.currentUserId=aUserId;
        imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        mSinaUserApi=new SinaUserApi();
        mSinaSearchApi=new SinaSearchApi();
        mSinaTrendApi=new SinaTrendApi();
        mSinaUserApi.updateToken();
        mSinaSearchApi.updateToken();
        mSinaTrendApi.updateToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.search_dialog, container, false);
        ListView listView=(ListView) root.findViewById(R.id.statusList);
        mListView=listView;
        listView.setOnItemClickListener(SearchDialogFragment.this);

        mTitle=(TextView) root.findViewById(R.id.tv_title);
        mSearch=(EditText) root.findViewById(R.id.content);
        mFriendBtn=(Button) root.findViewById(R.id.btn_friends);
        mSugguestBtn=(Button) root.findViewById(R.id.btn_sugguest);
        mTrendsBtn=(Button) root.findViewById(R.id.btn_trends);
        mRecentBtn=(Button) root.findViewById(R.id.btn_recent);

        mSearchBtn=(ImageView) root.findViewById(R.id.search_btn);
        mSearchCloseBtn=(ImageView) root.findViewById(R.id.search_close_btn);

        mHeaderLayout=(RelativeLayout) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.at_user_item, null);
        mHeaderLayout.findViewById(R.id.iv_portrait).setVisibility(View.GONE);
        mName=(TextView) mHeaderLayout.findViewById(R.id.tv_name);
        mLoadingLayout=(LinearLayout) root.findViewById(R.id.loading);

        mFooterLayout=new RelativeLayout(getActivity());

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        /*if ("0".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_dark);
        } else if ("1".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_dark);
        } else if ("2".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_light);
        } else if ("3".equals(themeId)) {
            mSearchBtn.setImageResource(R.drawable.action_search_light);
        }*/

        listView.addHeaderView(mHeaderLayout);
        listView.addFooterView(mFooterLayout);
        showMoreView();
        return root;
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("showMoreView");
        }
        if (null==mRelativeLoadingLayout) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("null==mLoadingLayout.");
            }
            mRelativeLoadingLayout=(RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext())
                .inflate(R.layout.ak_more_progressbar, null);
            mMoreProgressBar=(ProgressBar) mRelativeLoadingLayout.findViewById(R.id.progress_bar);
            mMoreTxt=(TextView) mRelativeLoadingLayout.findViewById(R.id.more_txt);
        }

        mMoreTxt.setText(R.string.search_user_friends);

        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("mListView.getFooterViewsCount():"+mListView.getFooterViewsCount());
        }

        mFooterLayout.removeAllViews();
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        mFooterLayout.addView(mRelativeLoadingLayout, layoutParams);

        mMoreProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFriendBtn.setOnClickListener(clickListener);
        mSugguestBtn.setOnClickListener(clickListener);
        mRecentBtn.setOnClickListener(clickListener);

        mSearch.addTextChangedListener(textWatcher);
        mHeaderLayout.setOnClickListener(clickListener);
        mSearchBtn.setOnClickListener(clickListener);
        mSearchCloseBtn.setOnClickListener(clickListener);

        if (null==mSimpleAdapter) {
            mSimpleAdapter=new SimpleAdapter(getActivity());
        }

        mListView.setAdapter(mSimpleAdapter);

        int type=getArguments().getInt("type");
        mSearchMode=type;
        if (type==2) {    //search trends
            mFriendBtn.setVisibility(View.GONE);
            mSugguestBtn.setVisibility(View.GONE);
            mTrendsBtn.setVisibility(View.VISIBLE);
            mTrendsBtn.setSelected(true);
            mSearch.setVisibility(View.GONE);
            mSearchBtn.setVisibility(View.GONE);
            mSearchCloseBtn.setVisibility(View.GONE);
            mTitle.setText(R.string.search_trend_title);
            mListView.removeHeaderView(mHeaderLayout);
            mListView.removeFooterView(mFooterLayout);

            if (null!=mQueryTask) {
                mQueryTask.cancel(true);
            }

            mQueryTask=new QueryTask();
            mQueryTask.execute(new Object[]{null, mSearchMode});
        } else {
            mFriendBtn.setVisibility(View.VISIBLE);
            mSugguestBtn.setVisibility(View.VISIBLE);
            mTrendsBtn.setVisibility(View.GONE);
            mFriendBtn.setSelected(true);
            mTrendsBtn.setSelected(false);
            mSugguestBtn.setSelected(false);
            mSearch.setVisibility(View.VISIBLE);
            mSearchBtn.setVisibility(View.VISIBLE);
            mSearchCloseBtn.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.search_user_title);
            doLocalSearch("");
        }
    }

    TextWatcher textWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!TextUtils.isEmpty(charSequence)) {
                mSearchCloseBtn.setVisibility(View.VISIBLE);
                doLocalSearch(charSequence.toString());
            } else {
                clearAdapter();
                mSearchCloseBtn.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    /**
     * 本地查询
     *
     * @param keyword
     */
    private void doLocalSearch(String keyword) {
        mName.setText(keyword);
        if (!isSearching) {
            if (null!=mQueryTask) {
                mQueryTask.cancel(true);
            }

            mQueryTask=new QueryTask();
            mQueryTask.execute(new Object[]{keyword, mSearchMode});
        }
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickMethod(view);
        }

        private void clickMethod(View view) {
            int id=view.getId();
            if (id==R.id.btn_recent) {
                if (!mRecentBtn.isSelected()) {
                    mRecentBtn.setSelected(true);
                    mSugguestBtn.setSelected(false);
                    mFriendBtn.setSelected(false);
                    mSearchMode=3;
                    clearAdapter();
                }
            } else if (id==R.id.btn_friends) {
                if (!mFriendBtn.isSelected()) {
                    showMoreView();
                    mFriendBtn.setSelected(true);
                    mSugguestBtn.setSelected(false);
                    mRecentBtn.setSelected(false);
                    mSearchMode=0;
                    clearAdapter();
                    requery();
                }
            } else if (id==R.id.btn_sugguest) {
                if (!mSugguestBtn.isSelected()) {
                    mFooterLayout.removeAllViews();
                    mSugguestBtn.setSelected(true);
                    mFriendBtn.setSelected(false);
                    mRecentBtn.setSelected(false);
                    mSearchMode=1;
                    clearAdapter();
                    requery();
                }
            } else if (id==R.id.search_btn) {
                String keyword=mSearch.getEditableText().toString();
                if (!TextUtils.isEmpty(keyword)) {
                    doLocalSearch(keyword);
                }
            } else if (id==R.id.search_close_btn) {
                mSearch.setText(null);
                mName.setText(null);
                mSearchCloseBtn.setVisibility(View.GONE);
                if (mSearchMode==0) {
                    doLocalSearch("");
                }
            } else if (view==mHeaderLayout) {
                String content=mSearch.getEditableText().toString();
                if (!TextUtils.isEmpty(content)) {
                    AtUser atUser=new AtUser();
                    atUser.name=content;
                    mAtUserListener.getAtUser(atUser);
                    SearchDialogFragment.this.dismiss();
                }
            }
        }
    };

    /**
     * 重新查询,只要输入框中有内容,在切换按钮时就自动查询.
     */
    private void requery() {
        String keyWord=mSearch.getEditableText().toString();
        if (!TextUtils.isEmpty(keyWord)) {
            doLocalSearch(keyWord);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null!=mAtUserListener) {
            int pos=position;
            if (mListView.getHeaderViewsCount()>0) {
                pos=position-1;
            }

            if (view==mFooterLayout) {   //if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
                //mMoreProgressBar.setVisibility(View.VISIBLE);
                fetchMore();
                return;
            }

            mAtUserListener.getAtUser((AtUser) mSimpleAdapter.getItem(pos));
            imm.hideSoftInputFromWindow(mSearch.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
        dismiss();
    }

    /**
     * 获取网络的关注列表。
     */
    private void fetchMore() {
        String keyword=mSearch.getEditableText().toString();
        if (TextUtils.isEmpty(keyword)) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("can't find friends.");
            }
            Toast.makeText(App.getAppContext(), "请输入人名。", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isSearching) {
            if (null!=mQueryTask) {
                mQueryTask.cancel(true);
            }

            mQueryTask=new QueryTask();
            mQueryTask.execute(new Object[]{keyword, 4});
        }
    }

    public void setAtUserListener(AtUserListener atUserListener) {
        this.mAtUserListener=atUserListener;
    }

    //--------------------- 查询用户操作 ---------------------

    /**
     * 搜索关注的用户，主要来源是主页
     *
     * @param keyword    搜索关键字
     * @param searchMode 0表示搜索本地数据，4表示搜索关注列表
     */
    private Object[] searchFriends(String keyword, Integer searchMode) {
        ArrayList<AtUser> atUsers=null;
        if (searchMode==0) {
            atUsers=SqliteWrapper.queryAtUsers(App.getAppContext(), currentUserId, TwitterTable.UserTbl.TYPE_FRIEND, keyword);
        } else if (searchMode==4) {
            try {
                //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
                final SStatusData<User> data=mSinaUserApi.getFriends(keyword, -1l, 18, 1);
                if (data.mStatusData!=null&&data.mStatusData.size()>0) {
                    atUsers=new ArrayList<AtUser>();
                    AtUser atUser;
                    ArrayList<User> users=data.mStatusData;
                    for (User user : users) {
                        atUser=new AtUser();
                        atUser.uid=user.id;
                        atUser.name=user.screenName;
                        atUsers.add(atUser);
                    }

                    saveFriends(atUsers);
                    return new Object[]{searchMode, atUsers};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Object[]{searchMode, atUsers};
    }

    /**
     * 保存新搜索的用户
     *
     * @param users
     */
    private void saveFriends(ArrayList<AtUser> users) {
        try {
            SqliteWrapper.saveFriendUsers(App.getAppContext(), users, currentUserId, TwitterTable.UserTbl.TYPE_FRIEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 搜索api的用户建议
     *
     * @param keyword    搜索关键字
     * @param searchMode
     */
    private Object[] searchUserSuggestion(String keyword, Integer searchMode) {
        try {
            //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
            final ArrayList<Map<String, String>> data=mSinaSearchApi.getSearchSuggestions(keyword, 15, 1);
            if (data.size()>0) {
                ArrayList<AtUser> atUsers=new ArrayList<AtUser>();
                AtUser atUser;
                for (Map<String, String> tmp : data) {
                    atUser=new AtUser();
                    atUser.uid=Long.valueOf(tmp.get("uid"));
                    atUser.name=tmp.get("screen_name");
                    atUsers.add(atUser);
                }
                return new Object[]{searchMode, atUsers};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 搜索话题,如果关键字为空,则显示当天热门话题.
     *
     * @param keyWord    话题关键字
     * @param searchMode
     * @return
     */
    private Object[] searchTrends(String keyWord, Integer searchMode) {
        String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+Constants.TREND_FILE;
        File file=new File(filename);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("filename:"+filename+" file:"+file.exists());
        }
        if (file.exists()) {
            long now=DateUtils.parseDateString(DateUtils.formatDate(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd").getTime();
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("lastModified:"+file.lastModified()+" now;"+now);
            }
            try {
                String content=StreamUtils.parseInputStream(new FileInputStream(file));
                Trends trends=WeiboParser.parseTrends(content);
                updateTrendsTmp(trends, searchMode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WeiboException e) {
                e.printStackTrace();
            }

            if ((file.lastModified()<now)) {
                return fetchNewTrends(searchMode);
            }
        } else {
            return fetchNewTrends(searchMode);
        }

        return null;
    }

    /**
     * 先更新话题列表，再获取新的
     *
     * @param trends
     */
    private void updateTrendsTmp(final Trends trends, final Integer searchMode) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Object[] objects=updateCompleteTrendAdapter(trends, searchMode);
                if (null!=objects) {
                    ArrayList<AtUser> list=(ArrayList<AtUser>) objects[1];
                    mSimpleAdapter.setAtUserList(list);
                    mSimpleAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 获取话题
     *
     * @return
     */
    Object[] fetchNewTrends(Integer searchMode) {
        try {
            Trends tmp=mSinaTrendApi.getTrends("daily");
            return updateCompleteTrendAdapter(tmp, searchMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Object[] updateCompleteTrendAdapter(final Trends trends, Integer searchMode) {
        if (trends==null||trends.trends==null||trends.trends.length<1) {
            WeiboLog.w("查询话题失败，或线程结束。"+trends);
            return null;
        }

        ArrayList<AtUser> atUsers=new ArrayList<AtUser>();
        AtUser atUser;
        for (Trend trend : trends.trends) {
            atUser=new AtUser();
            atUser.name=trend.name;
            atUsers.add(atUser);
        }

        return new Object[]{searchMode, atUsers};
    }

    /**
     * 获取数据，只是从数据库表中查询。
     *
     * @author archko
     */
    private class QueryTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isSearching=true;
            mLoadingLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            String keyWord=(String) params[0];
            Integer searchMode=(Integer) params[1];
            if (searchMode==0) {
                return searchFriends(keyWord, searchMode);
            } else if (searchMode==1) {
                return searchUserSuggestion(keyWord, searchMode);
            } else if (searchMode==2) {
                return searchTrends(keyWord, searchMode);
            } else if (searchMode==3) { //recent at
            } else if (searchMode==4) {  //search friends
                return searchFriends(keyWord, searchMode);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            isSearching=false;
            if (!isResumed()) {
                return;
            }

            mLoadingLayout.setVisibility(View.GONE);

            if (null==result) {
                Toast.makeText(App.getAppContext(), "no result!", Toast.LENGTH_LONG).show();
                return;
            }

            Integer searchMode=(Integer) ((Object[]) result)[0];
            ArrayList<AtUser> list=(ArrayList<AtUser>) ((Object[]) result)[1];
            if (null==list||list.size()<1) {
                if (searchMode==2) {
                    if (mSimpleAdapter.getCount()<1) {
                        Toast.makeText(App.getAppContext(), "no result!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    clearAdapter();
                }
                return;
            }

            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("list.size:"+list.size());
            }

            mSimpleAdapter.setAtUserList(list);
            mSimpleAdapter.notifyDataSetChanged();
            /*if (mListView.getVisibility()==View.GONE) {
                mListView.setVisibility(View.VISIBLE);
            }*/
        }
    }

    private void clearAdapter() {
        mSimpleAdapter.clearAtUserList();
        mSimpleAdapter.notifyDataSetChanged();
        /*if (mListView.getVisibility()==View.VISIBLE) {
            mListView.setVisibility(View.GONE);
        }*/
    }

    //--------------------- adapter ---------------------

    class SimpleAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<AtUser> mAtUserList;

        SimpleAdapter(Context ctx) {
            this.mContext=ctx;
            mAtUserList=new ArrayList<AtUser>();
        }

        public void setAtUserList(ArrayList<AtUser> mAtUserList) {
            this.mAtUserList=mAtUserList;
        }

        public void clearAtUserList() {
            mAtUserList.clear();
        }

        @Override
        public int getCount() {
            return mAtUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAtUserList.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AtUser atUser=mAtUserList.get(position);
            UserItemView itemView=null;
            if (null==convertView) {
                itemView=new UserItemView(mContext);
            } else {
                itemView=(UserItemView) convertView;
            }

            itemView.update(atUser);

            return itemView;
        }
    }

    private static class UserItemView extends LinearLayout {// implements Checkable {

        private TextView mName;
        private ImageView mPortrait;

        private UserItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.at_user_item, this);
            mName=(TextView) findViewById(R.id.tv_name);
            mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        }

        public void update(final AtUser atUser) {
            mName.setText(atUser.name);
        }

            /*private boolean checked=false;

            @Override
            public boolean isChecked() {
                return checked;
            }

            @Override
            public void setChecked(boolean aChecked) {
                if (checked==aChecked) {
                    return;
                }
                checked=aChecked;
                setBackgroundResource(checked ? R.drawable.abs__list_longpressed_holo : android.R.color.transparent);
            }

            @Override
            public void toggle() {
                setChecked(!checked);
            }*/
    }
}
