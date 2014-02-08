package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.LazyViewPager;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.EmptyFragment;
import cn.archko.microblog.fragment.StatusCommentsFragment;
import cn.archko.microblog.fragment.StatusDetailFragment;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.ThemeUtils;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.bean.ContentItem;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

/**
 * 显示一条微博完整的信息.以及它的评论列表.
 *
 * @author root date:2011-8-9
 */
public class ViewStatusDetailActivity extends BaseOauthFragmentActivity implements LazyViewPager.OnPageChangeListener {

    public static final String TAG="ViewStatusDetailActivity";
    private Status mStatus=null;

    private ActionMode mMode;
    View mCustomModeView;
    ImageView mQuickCommentBtn;
    EditText mQuickComnet;
    InputMethodManager imm;
    LazyViewPager mViewPager;

    /**
     * Pager adpater
     */
    private PagerAdapter mPagerAdapter;

    /**
     * 跳转到到评论界面
     */
    protected void commentStatus() {
        WeiboOperation.toCommentStatus(getApplicationContext(), mStatus);
    }

    /**
     * 到转发界面
     */
    protected void repostStatus() {
        WeiboOperation.toRepostStatus(getApplicationContext(), mStatus);
    }

    /**
     * 快速转发
     */
    protected void quickRepostStatus() {
        Intent taskService=new Intent(ViewStatusDetailActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;
        task.userId=currentUserId;
        task.content="";
        task.source=String.valueOf(mStatus.id);
        task.data="0";
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
        task.createAt=new Date().getTime();
        taskService.putExtra("send_task", task);
        startService(taskService);
        AKUtils.showToast("转发任务添加到队列服务中了。");
        //WeiboOperation.quickRepostStatus(mStatus.id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(R.string.text_title_content);

        Intent intent=getIntent();
        Serializable status=intent.getSerializableExtra("status");
        if (status==null) {
            WeiboLog.e(TAG, "没有传来微博.");
            AKUtils.showToast("没有微博");
            //this.finish();
            return;
        }
        mStatus=(Status) status;

        setContentView(R.layout.status_details);
        imm=(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        mPagerAdapter=new PagerAdapter(this);
        mViewPager=(LazyViewPager) findViewById(R.id.pager);

        mPagerAdapter.add(EmptyFragment.class, null);
        mPagerAdapter.add(StatusDetailFragment.class, null);
        mPagerAdapter.add(StatusCommentsFragment.class, null);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(1);
        // Offscreen limit
        mViewPager.setOffscreenPageLimit(2);
        // Attach the page change listener
        mViewPager.setOnPageChangeListener(this);
        //MobclickAgent.onError(this);

        setCustomActionBar();

        /*int theme=R.color.holo_dark_bg_view;
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {//if ("2".equals(themeId)) {
            theme=R.color.holo_light_bg_view;
        }

        //findViewById(R.id.fragment_placeholder).setBackgroundResource(R.drawable.ic_album_default_bg_blue);
        mViewPager.setBackgroundResource(theme);*/
        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), ViewStatusDetailActivity.this);

        try {
            final User user=mStatus.user;
            mActionBar.setTitle(user.screenName);
            mActionBar.setSubtitle(R.string.text_title_content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCustomActionBar() {
        View cusActionBar=getLayoutInflater().inflate(R.layout.status_detail_action_bar, null);
        mActionBar.setCustomView(cusActionBar);
        mActionBar.setDisplayShowCustomEnabled(true);
        TextView more=(TextView) cusActionBar.findViewById(R.id.menu_more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareMenu(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this);

        IntentFilter f=new IntentFilter();
        f.addAction(Constants.TASK_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));
    }

    @Override
    protected void onPause() {
        super.onPause();

        //MobclickAgent.onPause(this);
        try {
            unregisterReceiver(mStatusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem()==2) {
            mViewPager.setCurrentItem(1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position==0) {
            finish();
        } else if (position==2) {
            getStatusCommentsFragment().refresh();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private final SparseArray<WeakReference<Fragment>> mFragmentArray=new SparseArray<WeakReference<Fragment>>();

        private final ArrayList<Holder> mHolderList=new ArrayList<Holder>();

        private final ViewStatusDetailActivity mFragmentActivity;

        private int mCurrentPage;

        /**
         * Constructor of <code>PagerAdatper<code>
         *
         * @param fragmentActivity The {@link SherlockFragmentActivity} of the
         *                         {@link SherlockFragment}.
         */
        public PagerAdapter(final ViewStatusDetailActivity fragmentActivity) {
            super(fragmentActivity.getFragmentManager());
            mFragmentActivity=fragmentActivity;
        }

        /**
         * Method that adds a new fragment class to the viewer (the fragment is
         * internally instantiate)
         *
         * @param className The full qualified name of fragment class.
         * @param params    The instantiate params.
         */
        @SuppressWarnings("synthetic-access")
        public void add(final Class<? extends Fragment> className, final Bundle params) {
            final Holder mHolder=new Holder();
            mHolder.mClassName=className.getName();
            mHolder.mParams=params;

            final int mPosition=mHolderList.size();
            mHolderList.add(mPosition, mHolder);
            notifyDataSetChanged();
        }

        /**
         * Method that returns the {@link SherlockFragment} in the argument
         * position.
         *
         * @param position The position of the fragment to return.
         * @return Fragment The {@link SherlockFragment} in the argument position.
         */
        public Fragment getFragment(final int position) {
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null&&mWeakFragment.get()!=null) {
                return mWeakFragment.get();
            }
            return getItem(position);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final Fragment mFragment=(Fragment) super.instantiateItem(container, position);
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null) {
                mWeakFragment.clear();
            }
            mFragmentArray.put(position, new WeakReference<Fragment>(mFragment));
            return mFragment;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Fragment getItem(final int position) {
            final Holder mCurrentHolder=mHolderList.get(position);
            final Fragment mFragment=Fragment.instantiate(mFragmentActivity,
                mCurrentHolder.mClassName, mCurrentHolder.mParams);
            return mFragment;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            super.destroyItem(container, position, object);
            final WeakReference<Fragment> mWeakFragment=mFragmentArray.get(position);
            if (mWeakFragment!=null) {
                mWeakFragment.clear();
            }
        }

        @Override
        public int getCount() {
            return mHolderList.size();
        }

        /*@Override
        public CharSequence getPageTitle(final int position) {
            return mFragmentActivity.getResources().getStringArray(R.array.page_titles)[position]
                .toUpperCase(Locale.getDefault());
        }*/

        /**
         * Method that returns the current page position.
         *
         * @return int The current page.
         */
        public int getCurrentPage() {
            return mCurrentPage;
        }

        /**
         * Method that sets the current page position.
         *
         * @param currentPage The current page.
         */
        protected void setCurrentPage(final int currentPage) {
            mCurrentPage=currentPage;
        }

        /**
         * A private class with information about fragment initialization
         */
        private final class Holder {

            String mClassName;

            Bundle mParams;
        }
    }

    private StatusDetailFragment getViewStatusDetailFragment() {
        return (StatusDetailFragment) mPagerAdapter.getFragment(1);
    }

    private StatusCommentsFragment getStatusCommentsFragment() {
        return (StatusCommentsFragment) mPagerAdapter.getFragment(2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null!=mMode) {
            mMode.finish();
            mMode=null;
        }
    }

    //--------------------- 菜单操作 ---------------------

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        menuBuilder.getMenu().clear();
        WeiboLog.d(TAG, "onPrepareCustomMenu:"+hasInitMoreAction+" size:"+menuBuilder.getMenu().size());
        if (!hasInitMoreAction) {
            hasInitMoreAction=true;
            initMoreAction(menuBuilder);
        }
        for (ActionItem item : actionItems) {
            menuBuilder.getMenu().add(0, item.actionId, 0, item.title);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        WeiboLog.d(TAG, "onMenuItemSelected:"+" itemId:"+item.getItemId()+" item:"+item);
        int actionId=item.getItemId();
        switch (actionId) {
            case Constants.OP_ID_MORE_CONTENT_COPY_STATUS: {    //原内容复制
                ClipboardManager clipboard=(ClipboardManager) ViewStatusDetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(mStatus.text);

                AKUtils.showToast(R.string.text_copy_status);
                break;
            }
            case Constants.OP_ID_MORE_CONTENT_COPY_RET_STATUS: {   //转发内容复制
                ClipboardManager clipboard=(ClipboardManager) ViewStatusDetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                if (null!=mStatus.retweetedStatus&&!TextUtils.isEmpty(mStatus.retweetedStatus.text)) {
                    clipboard.setText(mStatus.retweetedStatus.text);

                    AKUtils.showToast(R.string.text_copy_ret_status);
                }
                break;
            }
            case Constants.OP_ID_MORE_CONTENT_OTHER: { //其它的操作，@，#，url
                processContent(item);
                break;
            }
            case Constants.OP_ID_MORE_RETSTATUS: {
                try {
                    Status status=mStatus;

                    Intent intent=new Intent();
                    intent.setClass(ViewStatusDetailActivity.this, ViewStatusDetailActivity.class);
                    if (null!=status) {
                        intent.putExtra("status", status.retweetedStatus);
                        intent.putExtra("refresh", true);
                        ViewStatusDetailActivity.this.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void processContent(MenuItem item) {
        String title=item.getTitle().toString();
        if (title.startsWith("@")) {
            WeiboOperation.toAtUser(ViewStatusDetailActivity.this, title);
        } else if (title.startsWith("^")) {
            WeiboOperation.toViewStatusUser(ViewStatusDetailActivity.this, title.substring(1), 100, UserFragmentActivity.TYPE_USER_INFO);
        }//else if(title.startsWith("#"))
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.status_menu, menu);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        //int resId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark;
        int favId=R.drawable.rating_favorite_light;
        int refreshId=R.drawable.navigation_refresh_light;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            //resId=R.drawable.abs__ic_menu_moreoverflow_normal_holo_light;
            favId=R.drawable.rating_favorite_light;
            refreshId=R.drawable.navigation_refresh_light;
        }
        //menu.findItem(R.id.menu_nav).setIcon(resId);
        menu.findItem(R.id.menu_favorite).setIcon(favId);
        menu.findItem(R.id.menu_refresh).setIcon(refreshId);

        /*if (null!=mStatus) {  //Fragment实现
            boolean hasImage=false;
            String thumbUrl=mStatus.thumbnailPic;
            if (!TextUtils.isEmpty(thumbUrl)) {
                hasImage=true;
            } else {
                Status retStatus=mStatus.retweetedStatus;
                if (null!=retStatus) {
                    thumbUrl=retStatus.thumbnailPic;
                    if (!TextUtils.isEmpty(thumbUrl)) {
                        hasImage=true;
                    }
                }
            }

            if (!hasImage) {
                menu.findItem(R.id.menu_show_in_gallery).setVisible(false);
                menu.findItem(R.id.menu_download_ori_img).setVisible(false);
            }
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        WeiboLog.d(TAG, "super.item:"+item);
        if (itemId==android.R.id.home) {
            finish();
        } else {
            processMenuItemSelected(itemId);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理菜单，因为多了一个菜单，所以要放在一起，如果这个可行，以后就不用硬件菜单按钮了。
     *
     * @param itemId
     */
    private void processMenuItemSelected(int itemId) {
        if (itemId==R.id.menu_refresh) {
            //refreshStatus();
        } else if (itemId==R.id.menu_quick_comment) {
            mMode=startActionMode(new StatusActionMode());
            if (null==mCustomModeView) {
                initActionModeView();
            }
            mMode.setCustomView(mCustomModeView);
        } else if (itemId==R.id.menu_quick_repost) {
            quickRepostStatus();
        } else if (itemId==R.id.menu_comment) {
            commentStatus();
        } else if (itemId==R.id.menu_repost) {
            repostStatus();
        } else if (itemId==R.id.menu_favorite) {
            //createFavorite();
        } else if (itemId==R.id.menu_download_ori_img) {
            //downOriImage();
        } else if (itemId==R.id.menu_show_in_gallery) {
            //viewLargeBitmap();
        }
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickMethod(view);
        }

        private void clickMethod(View view) {
            int id=view.getId();
            if (id==R.id.btn_comment) {
                quickComment();
            }
        }
    };

    /**
     * 显示快速评论的View
     */
    private void initActionModeView() {
        mCustomModeView=(RelativeLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater"))
            .inflate(R.layout.status_quick_comment, null);
        mQuickCommentBtn=(ImageView) mCustomModeView.findViewById(R.id.btn_comment);
        mQuickComnet=(EditText) mCustomModeView.findViewById(R.id.et_quick_comment);
        mQuickCommentBtn.setOnClickListener(clickListener);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int resId=R.drawable.send_light;

        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            resId=R.drawable.send_light;
        }
        mQuickCommentBtn.setImageResource(resId);
    }

    /**
     * 快速评论
     */
    private void quickComment() {
        imm.hideSoftInputFromWindow(mQuickComnet.getWindowToken(), 0);
        String content=mQuickComnet.getEditableText().toString();
        if (TextUtils.isEmpty(content)) {
            AKUtils.showToast(R.string.content_is_null);
            return;
        }
        int len=content.length();
        if (len>Constants.INPUT_STRING_COUNT) {
            AKUtils.showToast(R.string.text_exceed_max_num);
            return;
        }
        mMode.finish();
        mMode=null;

        Intent taskService=new Intent(ViewStatusDetailActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;     //在评论成功后，可能值为评论的id。
        task.userId=currentUserId;
        task.content=content;
        task.source=String.valueOf(mStatus.id);
        task.data="0";
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT;
        task.createAt=new Date().getTime();
        String txt=mStatus.text;

        task.text=txt;
        taskService.putExtra("send_task", task);
        AKUtils.showToast("评论任务添加到队列服务中了。");
        ViewStatusDetailActivity.this.startService(taskService);
    }

    private class StatusActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            WeiboLog.d(TAG, "onCreateActionMode");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            WeiboLog.d(TAG, "onActionItemClicked:"+item);
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            WeiboLog.d(TAG, "onDestroyActionMode");
        }
    }

    /**
     * Update comments when a comment was posted succuessfully
     */
    private final BroadcastReceiver mStatusListener=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.TASK_CHANGED)) {
                SendTask task=(SendTask) intent.getSerializableExtra("task");
                if (task.type==TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT&&task.uid!=0) {
                    //showToast("评论成功."+task.content);
                    try {
                        getStatusCommentsFragment().addComment(task);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ViewStatusDetailActivity.this.removeStickyBroadcast(intent);
            }
        }
    };

    //--------------------- 对内容的更多操作 ---------------------
    boolean hasInitMoreAction=false;
    ArrayList<ActionItem> actionItems=new ArrayList<ActionItem>();

    private void initMoreAction(PopupMenu menuBuilder) {
        ActionItem item;
        item=new ActionItem(Constants.OP_ID_MORE_CONTENT_COPY_STATUS, getString(R.string.opb_more_content_copy_status));
        actionItems.add(item);

        if (null!=mStatus) {
            Status retStatus=mStatus.retweetedStatus;
            if (null!=retStatus) {
                item=new ActionItem(Constants.OP_ID_MORE_CONTENT_COPY_RET_STATUS, getString(R.string.opb_more_content_copy_ret_status));
                actionItems.add(item);
            }

            StringBuilder sb=new StringBuilder();
            sb.append(mStatus.text);
            if (null!=retStatus) {
                if (retStatus.id>0) {
                    item=new ActionItem(Constants.OP_ID_MORE_RETSTATUS, getString(R.string.opb_more_view_ret_status));
                    actionItems.add(item);
                }

                sb.append("@"+retStatus.user.screenName)
                    .append(":")
                    .append(retStatus.text);
                //WeiboLog.d(TAG, "sn:"+retStatus.user.screenName+" text:"+retStatus.text);
            }

            SpannableString spannableString=new SpannableString(sb);
            ArrayList<ContentItem> contentItems=WeiboUtil.getAtHighlightContent(this, spannableString);
            if (contentItems.size()>0) {
                for (ContentItem ci : contentItems) {
                    item=new ActionItem(Constants.OP_ID_MORE_CONTENT_OTHER, String.valueOf(ci.content));
                    actionItems.add(item);
                    if (String.valueOf(ci.content).startsWith("@")) {
                        item=new ActionItem(Constants.OP_ID_MORE_CONTENT_OTHER, "^"+String.valueOf(ci.content).substring(1));
                        actionItems.add(item);
                        WeiboLog.v(TAG, "添加用户操作:"+item);
                    }
                }
            }
        }
    }

    static class ActionItem {

        public String title;
        public int actionId=-1;

        ActionItem(int actionId, String title) {
            this.title=title;
            this.actionId=actionId;
        }
    }
}
