package cn.archko.microblog.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.listeners.CommentListener;
import com.andrew.apollo.utils.ThemeUtils;
import com.me.microblog.bean.Comment;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 评论对话框，这里只用于评论，如果要有其它的对话框，需要另外处理。
 * @author: archko 12-09-5
 */
public class CommentDialog extends Dialog {

    public static final String TAG = "CommentDialog";
    Context mContext;

    CommentListener mListener;
    TextView mTitle;
    EditText mContent;
    LinearLayout linearLayout;
    /**
     * 是否正在执行事务。在执行事务时按取消按钮不取消对话框
     */
    boolean isDoing = false;
    Comment mComment;
    Handler mHandler = new Handler();
    InputMethodManager imm;
    /**
     * 字数文本
     */
    TextView mCharNum;
    EmojiPanelView mEmojiPanelView;
    Button mEmoBtn;

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable e) {
            WeiboLog.i(TAG, "");
            String string = e.toString();
            int len = string.length();

            mCharNum.setText(String.valueOf(Constants.INPUT_STRING_COUNT - len));
        }
    };

    public CommentDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    public CommentDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    public CommentDialog(Context context) {
        super(context, android.R.style.Theme_Translucent);
        mContext = context;

        setContentView(R.layout.comment_dialog);
        setCanceledOnTouchOutside(false);

        mTitle = (TextView) findViewById(R.id.title);
        mContent = (EditText) findViewById(R.id.content);
        linearLayout = (LinearLayout) findViewById(R.id.loading);
        mCharNum = (TextView) findViewById(R.id.char_num);
        mEmojiPanelView = (EmojiPanelView) findViewById(R.id.emoji_panel);
        mEmojiPanelView.setContent(mContent);
        mContent.addTextChangedListener(watcher);

        mEmoBtn = (Button) findViewById(R.id.btn_emo);
        mEmoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibily = mEmojiPanelView.getVisibility();
                if (visibily == View.VISIBLE) {
                    mEmojiPanelView.setVisibility(View.GONE);
                } else {
                    mEmojiPanelView.setVisibility(View.VISIBLE);
                }
            }
        });

        // 设置window属性
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams atts = getWindow().getAttributes();
        atts.gravity = Gravity.TOP;
        atts.dimAmount = 0.6f; // 去背景遮盖
        getWindow().setAttributes(atts);
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        ThemeUtils.getsInstance().themeBackground(findViewById(R.id.root), mContext);
    }

    public void setPosition(int x, int y) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        //Log.d("", "a.x:" + attrs.x + " a.y:" + attrs.y);
        if (- 1 != x) {
            attrs.x = x;
        }
        if (- 1 != y) {
            attrs.y = y;
        }
        getWindow().setAttributes(attrs);
        //Log.d("", "attrs.x:" + attrs.x + " attrs.y:" + attrs.y);
    }

    public void setListener(CommentListener listener) {
        this.mListener = listener;
    }

    public void setCustomTitle(int resId) {
        mTitle.setText(resId);
    }

    /**
     * 之前的评论专用，
     *
     * @param comment
     */
    public void setComment(Comment comment) {
        this.mComment = comment;
        mContent.setText("回复@" + comment.user.screenName + ":");
    }

    /**
     * 设置输入框的内容。
     *
     * @param content
     */
    public void setContent(String content) {
        mContent.setText(content);
        String txt = mContent.getText().toString();
        if (! TextUtils.isEmpty(txt)) {
            mContent.setSelection(txt.length());
        }
    }

    public void bindEvent(Activity activity) {
        setOwnerActivity(activity);

        // 绑定监听器
        Button close_button = (Button) findViewById(R.id.btn_back);
        close_button.setText(R.string.cancel);
        close_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                mListener.cancel();
            }
        });

        Button complete_button = (Button) findViewById(R.id.btn_save);
        complete_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String content = mContent.getEditableText().toString();
                if (! TextUtils.isEmpty(content)) {
                    doReply(content.length() > Constants.INPUT_STRING_COUNT ? content.substring(0, Constants.INPUT_STRING_COUNT) : content);
                } else {
                    Toast.makeText(mContext, "请输入内容。", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 回复评论
     *
     * @param content 回复评论的内容
     */
    protected void doReply(final String content) {
        WeiboLog.i(TAG, "doReply:" + content);
        linearLayout.setVisibility(View.VISIBLE);
        isDoing = true;
        //Toast.makeText(mContext, R.string.comment_reply_start, Toast.LENGTH_LONG).show();
        imm.hideSoftInputFromWindow(mContent.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        mListener.finish(null, content);
    }

    public void hideProgressBar() {
        linearLayout.setVisibility(View.GONE);
    }

    /*protected void doReply(final String content) {
        WeiboLog.i(TAG, "doReply:"+content);
        linearLayout.setVisibility(View.VISIBLE);
        isDoing=true;
        Toast.makeText(mContext, R.string.comment_reply_start, Toast.LENGTH_LONG).show();
        imm.hideSoftInputFromWindow(mContent.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        try {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    long cid=mComment.id;
                    long id=mComment.status.id;
                    SWeiboApi2 sWeiboApi2=(SWeiboApi2) App.getMicroBlog(mContext);
                    try {
                        Comment result=sWeiboApi2.commentReply(cid, id, content, null);
                        if (null!=result&&result.id>0) {
                            replyCommentEnd(R.string.comment_reply_suc);
                        } else {
                            replyCommentEnd(R.string.comment_reply_failed);
                        }
                    } catch (WeiboException e) {
                        e.printStackTrace();
                    } finally {
                        replyCommentEnd(R.string.comment_reply_suc);
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replyCommentEnd(final int resId) {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                linearLayout.setVisibility(View.GONE);
                isDoing=false;
                mListener.finish(null, content);
                if (isShowing()) {
                    dismiss();
                }
                Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
            }
        }, 0l);
    }*/
}
