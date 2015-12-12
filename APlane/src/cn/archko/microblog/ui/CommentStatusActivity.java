package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.SearchDialogFragment;
import cn.archko.microblog.fragment.abs.AtUserListener;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.view.EmojiPanelView;
import com.andrew.apollo.utils.ThemeUtils;
import com.bulletnoid.android.widget.SwipeAwayLayout;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

import java.io.Serializable;
import java.util.Date;

/**
 * 显示一条微博信息,进行评论,暂时先完成评论,转发不处理.也不评论原作者内容.
 * 添加回复转发微博内容,优化界面显示,
 * comment_ori当评论一条转发微博时，是否评论给原微博。0:不评论给原微博。1：评论给原微博。默认0.
 *
 * @author root date:2011-8-9
 * @author archko date:2012-5-12
 */
public class CommentStatusActivity extends SkinFragmentActivity {

    public static final String TAG="CommentStatusActivity";
    Status mStatus;
    boolean isPostingComment=false;
    InputMethodManager imm;

    /**
     * 评论的内容
     */
    EditText commentET;
    /**
     * 字数文本
     */
    TextView mCharNum;
    /**
     * 微博标题,作者
     */
    TextView mName;
    TextView comment_num;
    TextView repost_num;
    /**
     * 原创微博的内容
     */
    TextView mContentFirst;
    /**
     * 转发微博的内容
     */
    TextView mContentSencond;
    protected LinearLayout mContentSecondLayout;
    TextView ret_comment_num, ret_repost_num;
    /**
     * 是否处理完成了
     */
    boolean isDone=false;

    //---------------- operation bar ----------------

    //Button commentBtn, commentAndPostBtn, commentRetBtn;
    Button btn_trend, btn_at, mEmoBtn;

    EmojiPanelView mEmojiPanelView;
    boolean showCommentRetBtn=true;

    //--------------------- 认证 ---------------------

    /**
     * 认证失败后的操作
     */
    void oauthFailed() {
        NotifyUtils.showToast(R.string.new_status_failed, Toast.LENGTH_LONG);
    }

    //--------------------- autocomplete listview ---------------------
    AtUserListener mAtUserListener=new AtUserListener() {
        @Override
        public void getAtUser(AtUser atUser) {
            if (null!=atUser) {
                completeText(atUser.name);
            }
        }
    };

    /**
     * 显示自动完成的Fragment
     *
     * @param type 类型,是搜索用户还是话题
     */
    private void showCompleteFragment(int type) {
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        Fragment prev=getFragmentManager().findFragmentByTag("dialog");
        if (prev!=null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args=new Bundle();
        args.putInt("type", type);
        SearchDialogFragment searchDialogFragment=new SearchDialogFragment();
        searchDialogFragment.setArguments(args);
        searchDialogFragment.setAtUserListener(mAtUserListener);
        searchDialogFragment.show(ft, "dialog");
    }

    protected void autoCompleteAt() {
        Editable editable=commentET.getText();
        String txt=editable.toString();
        int start=commentET.getSelectionStart();
        int end=commentET.getSelectionEnd();
        //WeiboLog.d(TAG, "start:"+start+" txt:"+txt+" end:"+end);

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);
        //WeiboLog.d(TAG, "startTxt:"+startTxt+"->endTxt:"+endTxt);

        /*NewStatusActivity(11798): start:5 txt:gdgjmngddgn end:5
        D/NewStatusActivity(11798): startTxt:gdgjm->endTxt:ngddgn*/

        txt=startTxt+" @ "+endTxt;
        commentET.setText(txt);
        commentET.setSelection(start+2);

        /*queryUsernames();
        if (usernames.size()>0) {
            updateAdapter(usernames);
        }*/
        showCompleteFragment(0);
    }

    protected void autoCompleteTrends() {
        Editable editable=commentET.getText();
        String txt=editable.toString();
        int start=commentET.getSelectionStart();
        int end=commentET.getSelectionEnd();
        //WeiboLog.d(TAG, "start:"+start+" txt:"+txt+" end:"+end);

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);

        txt=startTxt+" ## "+endTxt;
        commentET.setText(txt);
        commentET.setSelection(start+2);

        //getTrends();
        showCompleteFragment(2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void completeText(String item) {
        Editable editable=commentET.getText();
        String txt=editable.toString();
        int start=commentET.getSelectionStart();
        int end=commentET.getSelectionEnd();
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "start:"+start+" txt:"+txt+" end:"+end);
        }

        String startTxt=txt.substring(0, start);
        String endTxt=txt.substring(end);
        //WeiboLog.d(TAG, "startTxt:"+startTxt+"->endTxt:"+endTxt);

        /*NewStatusActivity(11798): start:7 txt:gdgjm @ ngddgn end:7
        NewStatusActivity(11798): startTxt:gdgjm @->endTxt: ngddgn*/

        String result=startTxt+item+endTxt;
        commentET.setText(result);
        // make sure we keep the caret at the end of the text view
        /*Editable spannable=content.getText();
        Selection.setSelection(spannable, spannable.length());*/
        commentET.setSelection(start+item.length()+1);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int id=view.getId();
            switch (id) {
                /*case R.id.comment_btn:  //评论
                    comment("0");
                    break;

                case R.id.comment_and_post_btn: //评论且发布
                    commentAndPost();
                    break;

                case R.id.comment_retweet_btn:  //评论原微博
                    commentRetweet();
                    break;*/

                case R.id.btn_trend:
                    autoCompleteTrends();
                    break;

                case R.id.btn_at:
                    autoCompleteAt();
                    break;

                case R.id.btn_emo:
                    int visibily=mEmojiPanelView.getVisibility();
                    if (visibily==View.VISIBLE) {
                        mEmojiPanelView.setVisibility(View.GONE);
                    } else {
                        mEmojiPanelView.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent=getIntent();
        Serializable status=intent.getSerializableExtra("status");
        if (status==null) {
            NotifyUtils.showToast(R.string.comment_status_not_found, Toast.LENGTH_LONG);
            this.finish();
            return;
        }

        mStatus=(Status) status;
        imm=(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        final ActionBar bar=getActionBar();
        mActionBar=bar;
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(false);

        mActionBar.setDisplayShowTitleEnabled(true);  //隐藏主面的标题
        mActionBar.setDisplayShowHomeEnabled(false);   //整个标题栏
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setTitle(R.string.send_comment);
        _onCreate();

        SwipeAwayLayout view_root=(SwipeAwayLayout) findViewById(R.id.view_root);
        view_root.setSwipeOrientation(SwipeAwayLayout.LEFT_RIGHT);

        view_root.setOnSwipeAwayListener(new SwipeAwayLayout.OnSwipeAwayListener() {
            @Override
            public void onSwipedAway(int mCloseOrientation) {
                finish();
                int animId=R.anim.exit_left;
                if (mCloseOrientation==SwipeAwayLayout.RIGHT_ONLY) {
                    animId=R.anim.exit_to_left;
                }
                overridePendingTransition(0, animId);
            }
        });
    }

    protected void _onCreate() {
        setContentView(R.layout.status_comment);
        initViews();

        /*int theme=R.color.holo_dark_bg_view;
        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {//if ("2".equals(themeId)) {
            theme=R.color.holo_light_bg_view;
        }

        //findViewById(R.id.fragment_placeholder).setBackgroundResource(R.drawable.ic_album_default_bg_blue);
        findViewById(R.id.root).setBackgroundResource(theme);*/
        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), CommentStatusActivity.this);
        setStatusContent();
    }

    /**
     * 初始化布局元素
     */
    protected void initViews() {
        commentET=(EditText) findViewById(R.id.status_comment_content);
        commentET.addTextChangedListener(watcher);

        mCharNum=(TextView) findViewById(R.id.char_num);

        mName=(TextView) findViewById(R.id.tv_name);
        comment_num=(TextView) findViewById(R.id.comment_num);
        repost_num=(TextView) findViewById(R.id.repost_num);
        mContentFirst=(TextView) findViewById(R.id.tv_content_first);
        mContentSencond=(TextView) findViewById(R.id.tv_content_sencond);
        mContentSecondLayout=(LinearLayout) findViewById(R.id.tv_content_sencond_layout);
        ret_comment_num=(TextView) findViewById(R.id.ret_comment_num);
        ret_repost_num=(TextView) findViewById(R.id.ret_repost_num);
        initOperationBar();

        mEmojiPanelView=(EmojiPanelView) findViewById(R.id.emoji_panel);
        mEmojiPanelView.setContent(commentET);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_FIRST, 0, R.string.comment_btn).
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        if (showCommentRetBtn) {
            menu.add(0, MENU_SECOND, 0, R.string.comment_retweet_btn).
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|
                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            exitConfirm();
        } else if (MENU_FIRST==itemId) {    //评论
            comment("0");
        } else if (MENU_SECOND==itemId) {   //评论原微博
            commentRetweet();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void exitConfirm() {
        AlertDialog.Builder builder=new AlertDialog.Builder(CommentStatusActivity.this);
        builder.setTitle(R.string.app_name).setMessage(R.string.comment_exit_msg)
            .setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.confirm),
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                    arg0.cancel();
                }
            }).create().show();
    }

    private TextWatcher watcher=new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable e) {
            WeiboLog.i(TAG, "");
            String string=e.toString();
            int len=string.length();

            mCharNum.setText(String.valueOf(Constants.INPUT_STRING_COUNT-len));
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        WeiboLog.i(TAG, "dispatchKeyEvent.code:"+event.getKeyCode());
        if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            if (event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0) {
                if (!isDone) {
                    exitConfirm();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 初始化按钮工具栏
     */
    protected void initOperationBar() {
        /*commentBtn=(Button) findViewById(R.id.comment_btn);

        commentAndPostBtn=(Button) findViewById(R.id.comment_and_post_btn);

        commentRetBtn=(Button) findViewById(R.id.comment_retweet_btn);*/

        btn_trend=(Button) findViewById(R.id.btn_trend);
        btn_at=(Button) findViewById(R.id.btn_at);
        mEmoBtn=(Button) findViewById(R.id.btn_emo);

        /*commentBtn.setOnClickListener(clickListener);
        commentAndPostBtn.setOnClickListener(clickListener);
        commentRetBtn.setOnClickListener(clickListener);*/
        btn_trend.setOnClickListener(clickListener);
        btn_at.setOnClickListener(clickListener);
        mEmoBtn.setOnClickListener(clickListener);
    }

    /**
     * 显示原来的微博与其转发内容(如果存在),但是不显示图片.
     */
    protected void setStatusContent() {
        String title=mStatus.user.screenName;
        mName.setText(title);
        mContentFirst.setText(mStatus.text);
        comment_num.setText(getString(R.string.text_comment)+mStatus.c_num);
        repost_num.setText(getString(R.string.text_repost)+mStatus.r_num);

        setRetweetStatus();
    }

    protected void setRetweetStatus() {
        Status retweetedStatus=mStatus.retweetedStatus;
        if (retweetedStatus!=null) {
            String title="@"+retweetedStatus.user.screenName+":"+retweetedStatus.text+" ";
            SpannableString spannableString=new SpannableString(title);
            WeiboUtils.highlightContent(CommentStatusActivity.this, spannableString, getResources().getColor(R.color.holo_light_item_highliht_link));
            mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
            mContentSecondLayout.setVisibility(View.VISIBLE);
        } else {
            mContentSecondLayout.setVisibility(View.GONE);
            findViewById(R.id.retweet_content).setVisibility(View.GONE);
            //commentRetBtn.setVisibility(View.GONE);
            showCommentRetBtn=false;
            invalidateOptionsMenu();
        }
    }

    private void preComment() {
    }

    /**
     * 评论微博内容
     */
    private void comment(String comment_ori) {
        imm.hideSoftInputFromWindow(commentET.getWindowToken(), 0);
        if (isPostingComment) {
            NotifyUtils.showToast(R.string.in_progress, Toast.LENGTH_SHORT);
            return;
        }

        String commentString=commentET.getEditableText().toString();
        if (TextUtils.isEmpty(commentString)) {
            NotifyUtils.showToast(R.string.content_is_null, Toast.LENGTH_LONG);
            return;
        }

        doComment(comment_ori, commentString);
    }

    /**
     * 所有的操作从这里进入，转发也一样，所以在这里拦截认证就可以了
     *
     * @param comment_ori   是否评论原微博，如果是1，表示是。
     * @param commentString
     */
    protected void doComment(String comment_ori, String commentString) {
        int len=commentString.length();
        if (len>Constants.INPUT_STRING_COUNT) {
            NotifyUtils.showToast(R.string.text_exceed_max_num);
            return;
        }

        addTask(comment_ori, commentString);
        //doTask(new Object[]{commentString, comment_ori});
    }

    protected void addTask(String comment_ori, String commentString) {
        Intent taskService=new Intent(CommentStatusActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;
        task.userId=currentUserId;
        task.content=commentString;
        task.source=String.valueOf(mStatus.id);
        task.data=comment_ori;
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT;
        task.createAt=new Date().getTime();
        String txt=mStatus.text;
        try {
            if ("1".equals(comment_ori)) {
                txt=mStatus.retweetedStatus.text;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        task.text=txt;
        taskService.putExtra("send_task", task);
        CommentStatusActivity.this.startService(taskService);
        NotifyUtils.showToast("评论任务添加到队列服务中了。");
        CommentStatusActivity.this.finish();
    }

    /**
     * 评论且转发微博
     */
    private void commentAndPost() {
        comment("0");
        //post a status
    }

    /**
     * 评论原微博
     */
    private void commentRetweet() {
        comment("1");
    }
}

