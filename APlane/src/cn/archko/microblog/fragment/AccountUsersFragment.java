package cn.archko.microblog.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.impl.SinaAccountImpl;
import cn.archko.microblog.recycler.SimpleViewHolder;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.utils.WeiboOperation;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.db.MyHelper;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @version 1.00.00
 * @description: 帐户管理Fragment，用OauthBean作为实体，只是有部分属性数据库有存储的。
 * 像这样的加载本地数据，需要覆盖loadData，直接调用newTask，否则本先判断网络。
 * 需要覆盖fetchMore，通常不是加载更多，这里采取一次性加载。
 * 需要覆盖showMoreView，展示底部的footerview不同的内容
 * 如果是静态数据，不需要刷新的，需要修改onCreateView，将ListView设置成下拉刷新失效的。
 * 覆盖basePostOperation方法，因为它与网络数据相关，而且当数据为空时，会在footerview中显示
 * @author: archko 12-10-17
 */
public class AccountUsersFragment extends AbstractLocalListFragment<OauthBean> implements AddAccountDialogFragment.AccountOauthListener {

    public static final String TAG="AccountUsersFragment";
    ProgressDialog mProgressDialog;
    private static final int MENU_ADD=Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusImpl=new SinaAccountImpl();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem actionItem=menu.add(0, MENU_ADD, 0, "Add");

        // Items that show as actions should favor the "if room" setting, which will
        // prevent too many buttons from crowding the bar. Extra items will show in the
        // overflow area.
        MenuItemCompat.setShowAsAction(actionItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        // Items that show as actions are strongly encouraged to use an icon.
        // These icons are shown without a text description, and therefore should
        // be sufficiently descriptive on their own.

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int resId=R.drawable.content_new_dark;
        if ("2".equals(themeId)) {
            //resId=R.drawable.content_new_dark;
        }
        actionItem.setIcon(resId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==MENU_ADD) {
            addNewData();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initApi() {
        mStatusImpl=new SinaAccountImpl();
    }

    /**
     * 显示更多
     */
    protected void showMoreView() {
        super.showMoreView();
        mMoreProgressBar.setVisibility(View.INVISIBLE);
        mMoreTxt.setText(R.string.more_add_account_user);
    }

    /**
     * 添加新的帐户。
     */
    @Override
    public void addNewData() {
        WeiboLog.d(TAG, "add new account.");
        mSwipeLayout.setRefreshing(true);
        FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
        Fragment prev=getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev!=null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddAccountDialogFragment addAccountDialogFragment=new AddAccountDialogFragment(this);
        addAccountDialogFragment.show(ft, "dialog");
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
    public View getView(SimpleViewHolder holder, final int position) {
        View convertView=holder.baseItemView;
        AUItemView itemView=null;
        OauthBean oauthBean=mDataList.get(position);

        if (convertView==null) {
            itemView=new AUItemView(getActivity());
        } else {
            itemView=(AUItemView) convertView;
        }
        itemView.update(oauthBean);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(position, view);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPos=position;
                prepareMenu(up);
                return true;
            }
        });

        return itemView;
    }

    @Override
    public View newView(ViewGroup parent, int viewType) {
        //WeiboLog.d(TAG, "newView:" + parent + " viewType:" + viewType);
        AUItemView itemView=null;
        itemView=new AUItemView(getActivity());
        return itemView;
    }

    @Override
    public void _onActivityCreated(Bundle savedInstanceState) {
        super._onActivityCreated(savedInstanceState);
        showMoreView();
    }

    @Override
    public void oauthed() {
        newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
    }

    private class AUItemView extends LinearLayout {

        private TextView mTitle;    //帐号名字，登录号。
        private TextView mMsg;    //
        private ImageView icon; //头像

        private AUItemView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sidebar_item, this);
            setMinimumHeight(48);
            mTitle=(TextView) findViewById(R.id.title);
            mMsg=(TextView) findViewById(R.id.msg);
            icon=(ImageView) findViewById(R.id.image);
            mMsg.setVisibility(VISIBLE);
        }

        public void update(String text1, String text2) {
            mTitle.setText(text1);
            mMsg.setText("粉丝："+text2);
        }

        public void update(OauthBean oauthBean) {
            mTitle.setText(oauthBean.name);
            mMsg.setText(oauthBean.isDefault==1 ? "默认帐户" : "");
        }
    }

    //--------------------- 操作 ---------------------
    public void onCreateCustomMenu(PopupMenu menuBuilder) {
        int index=0;
        menuBuilder.getMenu().clear();
        menuBuilder.getMenu().add(0, Constants.OP_ID_QUICK_REPOST, index++, R.string.opb_account_set_default);
        menuBuilder.getMenu().add(0, Constants.OP_ID_COMMENT, index++, R.string.opb_account_delete);
    }

    /*@Override
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
    }*/

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int menuId=item.getItemId();
        switch (menuId) {
            case Constants.OP_ID_QUICK_REPOST: {
                quickRepostStatus();
                break;
            }
            case Constants.OP_ID_COMMENT: {
                commentStatus();
                break;
            }
        }
        return false;
    }

    /**
     * 列表短按事件。
     *
     * @param achor 用于显示QuickAction
     */
    protected void itemClick(int pos, View achor) {
        selectedPos=pos;
        //showButtonBar(achor);
        prepareMenu(up);
    }

    /**
     * 快速转发，在这里是设置默认帐户
     */
    public void quickRepostStatus() {
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            OauthBean oauthBean=mDataList.get(selectedPos);
            WeiboLog.d(TAG, "changeAccount:"+oauthBean);
            changeAccount(oauthBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到到评论界面，在这里是删除帐户，默认帐户不能删除。
     */
    public void commentStatus() {
        if (selectedPos>=mDataList.size()) {
            WeiboLog.d(TAG, "超出了Adapter数量.可能是FooterView.");
            return;
        }

        try {
            OauthBean oauthBean=mDataList.get(selectedPos);
            if (oauthBean.isDefault==1) {
                NotifyUtils.showToast("默认帐户不能删除，您需要先设置一个默认帐户！");
                return;
            }

            deleteAccount(oauthBean);
            WeiboLog.d(TAG, "deleteAccount.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeProgressDialog() {
        if (null!=mProgressDialog&&mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 修改当前的默认帐户
     *
     * @param oauthBean 要成为默认帐户的帐户，只有id与用户名有用
     */
    private void changeAccount(final OauthBean oauthBean) {
        if (null==mProgressDialog) {
            mProgressDialog=new ProgressDialog(getActivity());
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.account_change_dialog_title);
        mProgressDialog.setMessage(getString(R.string.account_change_dialog_msg));
        mProgressDialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    doExChange(oauthBean);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                        }
                    }, 0l);
                }
            }
        }).start();
    }

    private void doExChange(OauthBean oauthBean) {
        Intent intent=new Intent(getActivity(), SendTaskService.class);
        getActivity().stopService(intent);

        long newUserId=Long.valueOf(oauthBean.openId);
        boolean dbFlag=false;

        MyHelper databaseHelper=MyHelper.getMyHelper(App.getAppContext());
        SQLiteDatabase db=null;
        try {
            db=databaseHelper.getReadableDatabase();
            db.beginTransaction();
            String where=TwitterTable.AUTbl.ACCOUNT_USERID+"="+currentUserId+" and "+
                TwitterTable.AUTbl.ACCOUNT_TYPE+"="+TwitterTable.AUTbl.WEIBO_SINA;
            ContentValues cv=new ContentValues();
            cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT);
            WeiboLog.d(TAG, "unset default account sql:"+where);
            db.update(TwitterTable.AUTbl.ACCOUNT_TBNAME, cv, where, null);

            where=TwitterTable.AUTbl.ACCOUNT_USERID+"="+newUserId+" and "+
                TwitterTable.AUTbl.ACCOUNT_TYPE+"="+TwitterTable.AUTbl.WEIBO_SINA;
            cv=new ContentValues();
            cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT);
            WeiboLog.d(TAG, "set new default account sql:"+where);
            db.update(TwitterTable.AUTbl.ACCOUNT_TBNAME, cv, where, null);
            dbFlag=true;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (!dbFlag) {
            WeiboLog.i(TAG, "切换数据库帐户失败。");
            return;
        }

        //在这里，认为可以查询到的用户都是已经通过认证的。
        SharedPreferences.Editor editor=mPrefs.edit();
        editor.remove(Constants.PREF_USERNAME_KEY);
        // add 存储当前用户的id
        editor.remove(Constants.PREF_CURRENT_USER_ID);
        editor.remove(Constants.PREF_TIMESTAMP);
        editor.remove(Constants.PREF_TOKEN);
        editor.remove(Constants.PREF_SECRET);
        editor.remove(Constants.PREF_SCREENNAME_KEY);
        editor.remove(Constants.PREF_FOLLWWERCOUNT_KEY);
        editor.remove(Constants.PREF_FRIENDCOUNT_KEY);
        editor.remove(Constants.PREF_FAVOURITESCOUNT_KEY);
        editor.remove(Constants.PREF_STATUSCOUNT_KEY);
        editor.remove(Constants.PREF_TOPICCOUNT_KEY);
        editor.remove(Constants.PREF_PORTRAIT_URL);
        editor.remove(Constants.PREF_NEES_TO_UPDATE);

        //清除未读消息
        editor.remove(Constants.PREF_SERVICE_STATUS);
        editor.remove(Constants.PREF_SERVICE_COMMENT);
        editor.remove(Constants.PREF_SERVICE_FOLLOWER);
        editor.remove(Constants.PREF_SERVICE_AT);
        editor.remove(Constants.PREF_SERVICE_AT_COMMENT);
        editor.remove(Constants.PREF_SERVICE_DM);

        //清除认证
        editor.remove(Constants.PREF_SOAUTH_TYPE);

        editor.commit();

        editor.putString(Constants.PREF_SCREENNAME_KEY, oauthBean.name);
        editor.putLong(Constants.PREF_CURRENT_USER_ID, newUserId);
        editor.putLong(Constants.PREF_TIMESTAMP, System.currentTimeMillis());
        editor.commit();

        ((App) App.getAppContext()).logout();

        currentUserId=newUserId;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotifyUtils.showToast(R.string.account_change_suc);
                if (!WeiboUtils.isHoneycombOrLater()) {
                    Intent intent;
                    intent=new Intent(Constants.EXIT_APP);
                    getActivity().sendBroadcast(intent);
                }
                WeiboOperation.startNewHome(getActivity());
            }
        });
    }

    /**
     * 删除帐户
     *
     * @param oauthBean 要删除的帐户实体，只有id与用户名有用
     */
    private void deleteAccount(final OauthBean oauthBean) {
        if (null==mProgressDialog) {
            mProgressDialog=new ProgressDialog(getActivity());
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.account_delete_dialog_title);
        mProgressDialog.setMessage(getString(R.string.account_delete_dialog_msg));
        mProgressDialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int res=doDeleteAccount(oauthBean);
                    if (res>0) {
                        NotifyUtils.showToast(R.string.account_delete_suc);
                        newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
                    } else {
                        NotifyUtils.showToast(R.string.account_delete_failed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                        }
                    }, 0l);
                }
            }
        }).start();
    }

    private int doDeleteAccount(OauthBean oauthBean) {
        try {
            String uid=oauthBean.openId;
            ContentResolver resolver=getActivity().getContentResolver();
            //清除当前用户的主页数据
            resolver.delete(TwitterTable.SStatusTbl.CONTENT_URI, TwitterTable.SStatusTbl.UID+"='"+uid+"'", null);
            //清除当前用户的认证数据
            resolver.delete(TwitterTable.AUTbl.CONTENT_URI, TwitterTable.AUTbl.ACCOUNT_USERID+"='"+uid+"'", null);
            //清除当前用户的@用户数据
            resolver.delete(TwitterTable.UserTbl.CONTENT_URI, TwitterTable.UserTbl.UID+"='"+uid+"'", null);
            //清除当前用户的草稿数据
            resolver.delete(TwitterTable.DraftTbl.CONTENT_URI, TwitterTable.DraftTbl.UID+"='"+uid+"'", null);
            //清除当前用户的队列数据
            resolver.delete(TwitterTable.SendQueueTbl.CONTENT_URI, TwitterTable.SendQueueTbl.USER_ID+"='"+currentUserId+"'", null);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
