package cn.archko.microblog.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.service.SendTaskService;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.WeiboUtils;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.WeiboLog;
import cn.archko.microblog.utils.AKUtils;

import java.util.Date;

/**
 * 显示一条微博信息,进行转发,先完成转发,评论暂时不作处理,可以评论给微博的作者,如果还有转发微博,还可以评论它.
 * 处理了 是否在转发的同时发表评论。
 * is_comment:0表示不发表评论，1表示发表评论给当前微博，2表示发表评论给原微博，3是1、2都发表。默认为0。
 *
 * @author root date:2011-8-9
 * @author archko date:2012-5-12
 */
public class RepostStatusActivity extends CommentStatusActivity {

    public static final String TAG="RepostStatusActivity";
    public static final String prefix="//@";
    CheckBox repost_cur_btn, repost_ori_btn;
    final int MENU_REPOST=MENU_FIRST+10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle(R.string.repost_status);
    }

    @Override
    protected void _onCreate() {
        setContentView(R.layout.status_repost);
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
        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), RepostStatusActivity.this);
        setStatusContent();
    }

    /**
     * 初始化按钮工具栏
     */
    @Override
    protected void initOperationBar() {
        mCharNum=(TextView) findViewById(R.id.char_num);
        repost_cur_btn=(CheckBox) findViewById(R.id.repost_cur_btn);
        repost_ori_btn=(CheckBox) findViewById(R.id.repost_ori_btn);
        /*commentBtn=(Button) findViewById(R.id.repost_btn);
        commentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                doRepost();
            }
        });*/
        btn_trend=(Button) findViewById(R.id.btn_trend);
        btn_at=(Button) findViewById(R.id.btn_at);
        mEmoBtn=(Button) findViewById(R.id.btn_emo);
        btn_trend.setOnClickListener(clickListener);
        btn_at.setOnClickListener(clickListener);
        mEmoBtn.setOnClickListener(clickListener);
    }

    private void doRepost() {
        int repost=0;

        if (repost_cur_btn.isChecked()) {
            repost+=1;
        }

        if (repost_ori_btn.getVisibility()==View.VISIBLE&&repost_ori_btn.isChecked()) {
            repost+=2;
        }
        comment(String.valueOf(repost));
    }

    @Override
    protected void setStatusContent() {
        String title=mStatus.user.screenName;
        mName.setText(title);
        mContentFirst.setText(mStatus.text);
        comment_num.setText(getString(R.string.text_comment)+mStatus.c_num);
        repost_num.setText(getString(R.string.text_repost)+mStatus.r_num);

        commentET.setText(prefix+mStatus.user.screenName+":"+mStatus.text);
        repost_cur_btn.setText(mStatus.user.screenName);

        setRetweetStatus();
    }

    @Override
    protected void setRetweetStatus() {
        Status retweetedStatus=mStatus.retweetedStatus;
        if (retweetedStatus!=null) {
            String title="@"+retweetedStatus.user.screenName+":"+retweetedStatus.text+" ";
            SpannableString spannableString=new SpannableString(title);
            WeiboUtils.highlightContent(RepostStatusActivity.this, spannableString, getResources().getColor(R.color.holo_light_item_highliht_link));
            mContentSencond.setText(spannableString, TextView.BufferType.SPANNABLE);
            repost_ori_btn.setText(retweetedStatus.user.screenName);
            mContentSecondLayout.setVisibility(View.VISIBLE);
        } else {
            mContentSecondLayout.setVisibility(View.GONE);
            findViewById(R.id.retweet_content).setVisibility(View.GONE);
            repost_ori_btn.setVisibility(View.GONE);
        }
    }

    private void comment(String comment_ori) {
        WeiboLog.d("comment.comment_ori:"+comment_ori);
        imm.hideSoftInputFromWindow(commentET.getWindowToken(), 0);
        if (isPostingComment) {
            AKUtils.showToast(R.string.in_progress);
            return;
        }

        String commentString=commentET.getEditableText().toString();
        if (TextUtils.isEmpty(commentString)) {
            AKUtils.showToast(R.string.content_is_null, Toast.LENGTH_LONG);
            return;
        }

        doComment(comment_ori, commentString);
    }

    protected void addTask(String is_comment, String content) {
        Intent taskService=new Intent(RepostStatusActivity.this, SendTaskService.class);
        SendTask task=new SendTask();
        task.uid=currentUserId;
        task.userId=currentUserId;
        task.content=content;
        task.source=String.valueOf(mStatus.id);
        task.data=is_comment;
        task.type=TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS;
        task.createAt=new Date().getTime();
        String txt=mStatus.text;
        try {
            if ("2".equals(is_comment)||"3".equals(is_comment)) {
                txt=mStatus.retweetedStatus.text;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        task.text=txt;
        taskService.putExtra("send_task", task);
        RepostStatusActivity.this.startService(taskService);
        AKUtils.showToast("转发任务添加到队列服务中了。");
        RepostStatusActivity.this.finish();
    }

    protected void exitConfirm() {
        AlertDialog.Builder builder=new AlertDialog.Builder(RepostStatusActivity.this);
        builder.setTitle(R.string.app_name).setMessage(R.string.repost_exit_msg)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_REPOST, 0, R.string.repost_btn).
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            exitConfirm();
        } else if (MENU_REPOST==itemId) {    //
            doRepost();
        }

        return super.onOptionsItemSelected(item);
    }
}
