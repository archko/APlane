package cn.archko.microblog.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import com.me.microblog.App;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.sina.SinaUserApi;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description: 获取用户的好友, 主要用于@
 * @author: archko 12-2-24
 */
public class UserFriendListFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public static final String TAG="UserFriendListFragment";

    public SharedPreferences mPrefs;
    public long currentUserId=-1l;
    SimpleAdapter mSimpleAdapter;
    ListView mListView;
    TextView mTitle;
    ImageButton mDownload;
    ImageButton mRefresh;
    /**
     * 是否在搜索的标志位。
     */
    boolean isSearching=false;

    LinearLayout mLoadingLayout;
    RelativeLayout mFooterLayout;
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
    QueryTask mQueryTask;
    /**
     * 0表示搜索当前的数据库,1表示搜索用户的好友
     */
    int mSearchMode=0;
    private final int MODE_LOCALE=0;
    private final int MODE_NET=1;
    protected int nextCursor=0;//下一页索引，第一页为 0

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_NoTitleBar);
        mPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        long aUserId=mPrefs.getLong(Constants.PREF_CURRENT_USER_ID, -1);
        this.currentUserId=aUserId;

        mSinaUserApi=new SinaUserApi();
        mSinaUserApi.updateToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.search_dialog, container, false);
        ListView listView=(ListView) root.findViewById(R.id.statusList);
        mListView=listView;

        mTitle=(TextView) root.findViewById(R.id.tv_title);
        mDownload=(ImageButton) root.findViewById(R.id.profile_download);
        mRefresh=(ImageButton) root.findViewById(R.id.profile_refresh);
        root.findViewById(R.id.profile_layout).setVisibility(View.VISIBLE);
        root.findViewById(R.id.content_layout).setVisibility(View.GONE);
        root.findViewById(R.id.button_bar).setVisibility(View.GONE);

        mLoadingLayout=(LinearLayout) root.findViewById(R.id.loading);

        mFooterLayout=new RelativeLayout(getActivity());
        listView.addFooterView(mFooterLayout);
        showMoreView();

        mTitle.setText(R.string.profile_dialog_title);
        return root;
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        WeiboLog.d("showMoreView");
        if (null==mRelativeLoadingLayout) {
            WeiboLog.d("null==mLoadingLayout.");
            mRelativeLoadingLayout=(RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext())
                .inflate(R.layout.ak_more_progressbar, null);
            mMoreProgressBar=(ProgressBar) mRelativeLoadingLayout.findViewById(R.id.progress_bar);
            mMoreTxt=(TextView) mRelativeLoadingLayout.findViewById(R.id.more_txt);
        }

        //mMoreTxt.setText(R.string.search_user_friends);

        WeiboLog.v("mListView.getFooterViewsCount():"+mListView.getFooterViewsCount());

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
        mDownload.setOnClickListener(clickListener);
        mRefresh.setOnClickListener(clickListener);

        if (null==mSimpleAdapter) {
            mSimpleAdapter=new SimpleAdapter(getActivity());
        }

        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mSimpleAdapter);

        doLocalSearch(MODE_LOCALE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view==mFooterLayout) {   //if (mAdapter.getCount()>0&&position>=mAdapter.getCount()) {
            //mMoreProgressBar.setVisibility(View.VISIBLE);
            fetchMore();
            return;
        }
    }

    /**
     * 本地查询
     *
     * @param searchMode
     */
    private void doLocalSearch(int searchMode) {
        if (!isSearching) {
            if (null!=mQueryTask) {
                mQueryTask.cancel(true);
            }

            mQueryTask=new QueryTask();
            mQueryTask.execute(new Object[]{searchMode});
        }
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickMethod(view);
        }

        private void clickMethod(View view) {
            int id=view.getId();
            if (id==R.id.profile_download) {
                clearAdapter();
                nextCursor=0;
                mSearchMode=MODE_NET;
                doLocalSearch(mSearchMode);
            } else if (id==R.id.profile_refresh) {
                clearAdapter();
                mSearchMode=MODE_LOCALE;
                doLocalSearch(mSearchMode);
            }
        }
    };

    /**
     * 获取网络的关注列表。
     */
    private void fetchMore() {
        if (!isSearching) {
            if (null!=mQueryTask) {
                mQueryTask.cancel(true);
            }

            mQueryTask=new QueryTask();
            mQueryTask.execute(new Object[]{mSearchMode});
        }
    }

    //--------------------- 查询用户操作 ---------------------

    /**
     * 搜索关注的用户，主要来源是主页
     *
     * @param keyword    搜索关键字
     * @param searchMode 0表示搜索本地数据，4表示搜索关注列表
     */
    private Object[] searchFriends(int searchMode) {
        ArrayList<AtUser> atUsers=null;
        if (searchMode==MODE_LOCALE) {
            atUsers=SqliteWrapper.queryAtUsers(App.getAppContext(), currentUserId, TwitterTable.UserTbl.TYPE_FRIEND, "");
        } else if (searchMode==MODE_NET) {
            try {
                //SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(App.getAppContext());
                int c=40;
                final SStatusData<User> data=mSinaUserApi.getFriends(currentUserId, nextCursor, c, 1);
                if (data.mStatusData!=null&&data.mStatusData.size()>0) {
                    nextCursor++;
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
            Integer searchMode=(Integer) params[0];
            return searchFriends(searchMode);
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

            WeiboLog.d("list.size:"+list.size());

            mSimpleAdapter.setAtUserList(list);
            mSimpleAdapter.notifyDataSetChanged();
        }
    }

    private void clearAdapter() {
        mSimpleAdapter.clearAtUserList();
        mSimpleAdapter.notifyDataSetChanged();
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
            ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(
                R.layout.at_user_item, this);
            mName=(TextView) findViewById(R.id.tv_name);
            mPortrait=(ImageView) findViewById(R.id.iv_portrait);
        }

        public void update(final AtUser atUser) {
            mName.setText(atUser.name);
        }
    }
}
