package cn.archko.microblog.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaTaskImpl;
import cn.archko.microblog.service.SendTaskService;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.bean.SendTask;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.util.Constants;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 队列管理Fragment
 * @author: archko 12-11-19
 */
public class TaskListFragment extends AbstractLocalListFragment<SendTask> {

    public static final String TAG = "TaskListFragment";
    public static final int GET_DRAFT = 1;
    /**
     * 当前的模式，如果为get_draft，就是获取数据用的。
     */
    int mode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        mode = intent.getIntExtra("mode", 0);
        /*if (mode==GET_DRAFT) {  //在获取数据时，不能再有编辑操作。
            mQuickAction.setActionItemVisible(View.GONE, 0);
            mQuickAction.setActionItemVisible(View.GONE, 1);
        }*/
        //mStatusImpl=new SinaTaskImpl();
    }

    public void initApi() {
        mStatusImpl = new SinaTaskImpl();
    }

    /**
     * Update everything as the meta or playstate changes
     */
    private final BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.TASK_CHANGED)) {
                NotifyUtils.showToast("有任务完成，队列已经发生变化了。");
                newTaskNoNet(new Object[]{true, - 1l, - 1l, 1, page, false}, null);
                getActivity().removeStickyBroadcast(intent);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter();
        f.addAction(Constants.TASK_CHANGED);
        getActivity().registerReceiver(mStatusListener, new IntentFilter(f));
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            getActivity().unregisterReceiver(mStatusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        WeiboLog.d(TAG, "showMoreView");

        //super.showMoreView();
        //mMoreProgressBar.setVisibility(View.GONE);
        //mMoreTxt.setText(R.string.more_add_status);
    }

    /**
     * 需要注意,在主页时,需要缓存图片数据.所以cache为true,其它的不缓存,比如随便看看.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DraftItemView itemView = null;
        SendTask draft = mDataList.get(position);

        if (convertView == null) {
            itemView = new DraftItemView(getActivity());
        } else {
            itemView = (DraftItemView) convertView;
        }
        itemView.update(draft);

        return itemView;
    }

    private class DraftItemView extends LinearLayout {

        private TextView mName;    //类型，新微博，转发，评论
        private TextView mCreateAt;
        protected TextView mContentFirst;//微博的内容
        protected TextView mContentSencond;  //转发微博内容
        private TextView mErrorMsg; //错误信息，如果处理失败，会有错误信息留下。

        private DraftItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item, this);
            mName = (TextView) findViewById(R.id.tv_name);
            mContentFirst = (TextView) findViewById(R.id.tv_content_first);
            mContentSencond = (TextView) findViewById(R.id.tv_content_sencond);
            mErrorMsg = (TextView) findViewById(R.id.error_msg);
            mCreateAt = (TextView) findViewById(R.id.send_time);

            SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            float pref_title_font_size = options.getInt(PreferenceUtils.PREF_TITLE_FONT_SIZE, 14);
            float pref_content_font_size = options.getInt(PreferenceUtils.PREF_CONTENT_FONT_SIZE, 16);
            float pref_ret_content_font_size = options.getInt(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE, 16);

            int pref_content_color = PreferenceUtils.getInstace(App.getAppContext()).getDefaultStatusThemeColor(App.getAppContext());
            int pref_ret_content_color = PreferenceUtils.getInstace(App.getAppContext()).getDefaultRetContentThemeColor(App.getAppContext());

            if (mName.getTextSize() != pref_title_font_size) {
                mName.setTextSize(pref_title_font_size);
            }
            if (mContentFirst.getTextSize() != pref_content_font_size) {
                mContentFirst.setTextSize(pref_content_font_size);
            }
            if (mContentSencond.getTextSize() != pref_ret_content_font_size) {
                mContentSencond.setTextSize(pref_ret_content_font_size);
            }
            mContentFirst.setTextColor(pref_content_color);
            mContentSencond.setTextColor(pref_ret_content_color);
        }

        public void update(SendTask task) {
            mCreateAt.setText(DateUtils.getShortDateString(task.createAt));
            String taskType = "";
            int type = task.type;
            String ori_txt = task.text;

            mContentFirst.setText(task.content);
            if (type == TwitterTable.SendQueueTbl.SEND_TYPE_STATUS) {
                taskType = getString(R.string.task_status);
                mContentSencond.setVisibility(View.GONE);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_REPOST_STATUS) {
                taskType = getString(R.string.task_repost);
                mContentSencond.setVisibility(View.VISIBLE);
                mContentSencond.setText(getString(R.string.task_ori_text) + ori_txt);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_COMMENT) {
                taskType = getString(R.string.task_comment);
                mContentSencond.setVisibility(View.VISIBLE);
                mContentSencond.setText(getString(R.string.task_ori_text) + ori_txt);
            } else if (type == TwitterTable.SendQueueTbl.SEND_TYPE_ADD_FAV) {
                taskType = getString(R.string.task_favorite);
                mContentSencond.setVisibility(View.GONE);
                mContentFirst.setText(getString(R.string.task_ori_text) + task.content);
            }

            mName.setText(taskType);

            int resultCode = task.resultCode;
            String resultMsg = task.resultMsg;
            if (resultCode > 0 && ! TextUtils.isEmpty(resultMsg)) {
                StringBuilder content = new StringBuilder();
                content.append(getString(R.string.task_err_header)).
                    append(getString(R.string.task_err_code)).append(resultCode).
                    append(getString(R.string.task_err_msg)).append(resultMsg);
                mErrorMsg.setText(content.toString());
                mErrorMsg.setVisibility(View.VISIBLE);
            } else {
                mErrorMsg.setText(R.string.task_in_progress);
            }
        }
    }

    //--------------------- 微博操作 ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index = 0;
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_task_edit);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_task_delete);
        menuBuilder.getMenu().add(0, Constants.OP_ID_VIEW_USER, index++, R.string.opb_task_delete_all);
        menuBuilder.getMenu().add(0, Constants.OP_ID_REPOST, index++, R.string.opb_task_restart);
    }

    @Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        if (mode != GET_DRAFT) {  //在获取数据时，不能再有编辑操作。
            if (selectedPos >= mDataList.size()) {
                WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
                return;
            }
            SendTask task = mDataList.get(selectedPos);
            int resultCode = task.resultCode;
            String resultMsg = task.resultMsg;
            if (resultCode > 0 && ! TextUtils.isEmpty(resultMsg)) {
                menuBuilder.getMenu().getItem(3).setVisible(true);
            } else {
                menuBuilder.getMenu().getItem(3).setVisible(false);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_REPOST: {
                repostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
            case Constants.OP_ID_ORITEXT: {
                viewOriginalStatus(null);
                break;
            }
            case Constants.OP_ID_VIEW_USER: {
                viewStatusUser();
                break;
            }
        }
        return false;
    }

    /**
     * 快速转发，在这里是编辑任务
     */
    protected void quickRepostStatus() {
        if (selectedPos >= mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            //SendTask data=mDataList.get(selectedPos);

            WeiboLog.d(TAG, "编辑任务，未实现。");
            NotifyUtils.showToast("not implemented!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面，在这里是删除任务
     */
    protected void commentStatus() {
        if (selectedPos >= mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            SendTask data = mDataList.get(selectedPos);

            int res = SqliteWrapper.deleteSendTask(App.getAppContext(), data);
            if (res > 0) {
                newTaskNoNet(new Object[]{true, - 1l, - 1l, 1, page, false}, null);
            } else {
                NotifyUtils.showToast("删除任务失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在这里用于删除所有的任务。
     */
    @Override
    protected void viewStatusUser() {
        try {
            int res = SqliteWrapper.deleteAllSendTask(App.getAppContext(), currentUserId);
            if (res > 0) {
                newTaskNoNet(new Object[]{true, - 1l, - 1l, 1, page, false}, null);
            } else {
                NotifyUtils.showToast("删除任务失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 达里是重新执行任务，必须是失败的任务才能重新执行。
     */
    @Override
    protected void repostStatus() {
        if (selectedPos >= mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            SendTask task = mDataList.get(selectedPos);
            Intent taskService = new Intent(getActivity(), SendTaskService.class);
            taskService.putExtra("type", SendTaskService.TYPE_RESTART_TASK);
            taskService.putExtra("send_task", task);
            getActivity().startService(taskService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
