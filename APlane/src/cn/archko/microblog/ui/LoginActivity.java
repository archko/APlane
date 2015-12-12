package cn.archko.microblog.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.fragment.OauthDialogFragment;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.service.WeiboService;
import com.me.microblog.App;
import com.me.microblog.db.MyHelper;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.Oauth2;
import com.me.microblog.oauth.Oauth2Handler;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.oauth.SOauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * 处理用户登录信息,有一些是需要登录才可执行的. 先检查是否已经登录了,如果登录就直接跳过.
 * 然后检查是否已经获取了access_token与access_secret,如果存在就登录,然后使用这两个值.
 * 未登录也无token就显示登录框,然后登录后把token,secret与用户名保存不需要存密码,因为是Oauth认证方式.
 *
 * @author archko date:2011-7-5
 */
@Deprecated
public class LoginActivity extends NavModeActivity {

    public static final String TAG="LoginActivity";

    //----------------------------

    private EditText mName, mPass;
    private Button mLoginBtn, mExitBtn, mRegistBtn, mLogin2Btn;
    Button mLoginShowWebviewBtn;
    InputMethodManager imm;
    ProgressDialog mProgressDialog;
    AlertDialog mAlertDialog;
    private static final int OAUTH_TYPE_CLIENT=0;
    private static final int OAUTH_TYPE_WEBVIEW=1;
    /**
     * 0表示默认的方式,不显式地处理,1表示需要显示webview,但是无法自动认证,
     */
    int type=OAUTH_TYPE_CLIENT;
    RelativeLayout mContextFrame;
    TextView mAccountName;
    ArrayList<OauthBean> mAccounts;
    Handler mHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            try {
                if (null!=mProgressDialog&&mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (type==OAUTH_TYPE_WEBVIEW) {
                try {
                    FragmentTransaction ft=getFragmentManager().beginTransaction();
                    Fragment prev=getFragmentManager().findFragmentByTag("oauth_dialog");
                    if (prev!=null) {
                        ft.remove(prev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isFinishing()) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d("isFinishing()");
                }
                return;
            }

            super.handleMessage(msg);
            int what=msg.what;
            if (what==0) {
                oauthResult(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_AK_Light);
        super.onCreate(savedInstanceState);

        Intent intent=new Intent(LoginActivity.this, WeiboService.class);
        stopService(intent);
        intent=new Intent(LoginActivity.this, SendTaskService.class);
        stopService(intent);
        cleanUnread();

        setContentView(R.layout.login);
        mProgressDialog=new ProgressDialog(LoginActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mName=(EditText) findViewById(R.id.email);
        String name=mPreferences.getString(Constants.PREF_USERNAME_KEY, "");
        if (!"".equals(name)) {
            mName.setText(name);
        }
        mPass=(EditText) findViewById(R.id.pwd);

        mLoginBtn=(Button) findViewById(R.id.btn);
        mExitBtn=(Button) findViewById(R.id.exit);
        mRegistBtn=(Button) findViewById(R.id.regist_btn);
        mLogin2Btn=(Button) findViewById(R.id.login2);
        mLoginShowWebviewBtn=(Button) findViewById(R.id.login_show_webview_btn);
        mContextFrame=(RelativeLayout) findViewById(R.id.track_list_context_frame);
        mAccountName=(TextView) findViewById(R.id.quick_context_line);

        mLoginBtn.setOnClickListener(clickListener);
        mExitBtn.setOnClickListener(clickListener);
        mRegistBtn.setOnClickListener(clickListener);
        mLogin2Btn.setOnClickListener(clickListener);
        mLoginShowWebviewBtn.setOnClickListener(clickListener);
        mContextFrame.setOnClickListener(clickListener);

        //MobclickAgent.onError(this);
        initAccounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initAccounts() {
        try {
            ArrayList<OauthBean> objects=SqliteWrapper.queryAccounts(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA);
            if (null!=objects&&objects.size()>0) {
                mContextFrame.setVisibility(View.VISIBLE);
                mAccounts=objects;
                String name=objects.get(0).name;
                mAccountName.setText(name);
                NotifyUtils.showToast(String.format("您已有%d个帐户，可以直接登录！", objects.size()), Toast.LENGTH_LONG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除上一次的设置，重新登录后所有的未读消息需要清零
     */
    private void cleanUnread() {
        SharedPreferences.Editor editor=mPreferences.edit();
        editor.remove(Constants.PREF_SERVICE_COMMENT);
        editor.remove(Constants.PREF_SERVICE_STATUS);
        editor.remove(Constants.PREF_SERVICE_FOLLOWER);
        editor.remove(Constants.PREF_SERVICE_AT);
        editor.remove(Constants.PREF_SERVICE_AT_COMMENT);
        editor.remove(Constants.PREF_SERVICE_DM);
        editor.commit();
    }

    private View.OnClickListener clickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            clickMethod(v);
        }
    };

    private void clickMethod(View v) {
        if (v.getId()==R.id.exit) {
            finish();
            return;
        } else if (v.getId()==R.id.login_show_webview_btn) {
            type=OAUTH_TYPE_WEBVIEW;
            oauth2("", "");
            return;
        } else if (v.getId()==R.id.track_list_context_frame) {
            prepareMenu(mContextFrame);
            return;
        }

        String emailTxt=mName.getEditableText().toString();
        String passwordTxt=mPass.getEditableText().toString();

        if (TextUtils.isEmpty(emailTxt)||TextUtils.isEmpty(passwordTxt)) {
            Toast.makeText(App.getAppContext(), "请输入帐户及密码", Toast.LENGTH_LONG).show();
            return;
        }

        if (v.getId()==R.id.btn) {  //客户端认证
            imm.hideSoftInputFromWindow(mName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mPass.getWindowToken(), 0);

            if (!App.hasInternetConnection(this)) {
                Toast.makeText(App.getAppContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();

                return;
            }

            /*mProgressDialog.setTitle(R.string.login);
            mProgressDialog.show();
            LoginTask loginTask=new LoginTask();
            loginTask.execute(new Object[]{emailTxt, passwordTxt});*/
            NotifyUtils.showToast("not implemented");
        } else if (v.getId()==R.id.exit) {
            finish();
        } else if (v.getId()==R.id.regist_btn) {  //注册
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://weibo.cn"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                NotifyUtils.showToast("您的系统没有浏览器.");
            }
            this.finish();
        } else if (v.getId()==R.id.login2) {  //web认证
            type=OAUTH_TYPE_CLIENT;
            oauth2(emailTxt, passwordTxt);
        }
    }

    /**
     * 登录成功的后期处理。
     *
     * @param resultObj
     */
    private void postLogin(Object[] resultObj) {
        Oauth2Handler.saveWeiboApi(resultObj, LoginActivity.this, true);

        String username=(String) resultObj[0];
        String password=(String) resultObj[2];
        //advancedOauth(username, password);

        startIntent();
    }

    //--------------------------

    /**
     * 授权登录
     */
    void oauth2(String username, String password) {
        SOauth2 ouath2=new SOauth2();

        if (type==OAUTH_TYPE_WEBVIEW) {
            /*AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this)
                .setTitle(R.string.app_name)
                .setView(webView);
            AlertDialog dialog=builder.create();
            dialog.show();
            mAlertDialog=dialog;*/
            FragmentTransaction ft=getFragmentManager().beginTransaction();
            Fragment prev=getFragmentManager().findFragmentByTag("oauth_dialog");
            if (prev!=null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            OauthDialogFragment oauthDialogFragment=new OauthDialogFragment(mHandler);
            oauthDialogFragment.show(ft, "oauth_dialog");
        } else {
            ouath2.oauthByWebView(new Object[]{username, password, getApplicationContext(), mHandler, null});
            mAlertDialog=null;
            if (null==mProgressDialog) {
                mProgressDialog=new ProgressDialog(LoginActivity.this);
            }
            mProgressDialog.setTitle(R.string.login2_title);
            mProgressDialog.show();
        }
    }

    /**
     * Oauth2认证结果
     *
     * @param msg
     */
    void oauthResult(Message msg) {
        final Object[] objects=(Object[]) msg.obj;
        WeiboLog.d(TAG, "oauthResult objects:"+objects);

        if (null==objects||objects[0]==null) {
            NotifyUtils.showToast(R.string.login2_error, Toast.LENGTH_LONG);
            WeiboLog.e(TAG, "运行中认证失败。");
            return;
        }
        WeiboLog.d(TAG, "bean:"+msg);

        try {
            OauthBean oauthBean=(OauthBean) objects[0];
            //Object[] params=(Object[]) objects[1];
            if (oauthBean!=null) {
                WeiboLog.d(TAG, "认证成功。");
                SharedPreferences.Editor editor=mPreferences.edit();
                editor.putString(Constants.PREF_SOAUTH_TYPE, String.valueOf(Oauth2.OAUTH_TYPE_WEB));
                editor.commit();
                String username=mName.getEditableText().toString();
                String password=mPass.getEditableText().toString();
                if (type==OAUTH_TYPE_WEBVIEW) {
                    username=password="";
                }
                App.isLogined=true;
                postLogin(new Object[]{username, oauthBean, password});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------- 帐户操作 ---------------------

    /**
     * 创建菜单项，供子类覆盖，以便动态地添加菜单项。
     *
     * @param menuBuilder
     */
    public void onPrepareCustomMenu(PopupMenu menuBuilder) {
        //WeiboLog.v(TAG, "onPrepareCustomMenu:"+mAccounts);
        for (OauthBean bean : mAccounts) {
            menuBuilder.getMenu().add(0, (int) bean.id, 0, bean.name);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        WeiboLog.d(item.getItemId()+" uid:");
        int uid;
        for (OauthBean bean : mAccounts) {
            uid=(int) bean.id;
            if (item.getItemId()==uid) {
                selectAccount(bean);
            }
        }
        return true;
    }

    private void selectAccount(OauthBean oauthBean) {
        WeiboLog.d(TAG, "selectAccount:"+oauthBean);
        Intent intent=new Intent(LoginActivity.this, SendTaskService.class);
        stopService(intent);
        intent=new Intent(LoginActivity.this, WeiboService.class);
        stopService(intent);

        long newUserId=Long.valueOf(oauthBean.openId);
        boolean dbFlag=false;

        MyHelper databaseHelper=MyHelper.getMyHelper(App.getAppContext());
        SQLiteDatabase db=null;
        try {
            db=databaseHelper.getReadableDatabase();
            db.beginTransaction();
            String where=TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT+"="+TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT+
                " and "+TwitterTable.AUTbl.ACCOUNT_TYPE+"="+TwitterTable.AUTbl.WEIBO_SINA;
            ContentValues cv=new ContentValues();
            cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, TwitterTable.AUTbl.ACCOUNT_IS_NOT_DEFAULT);
            WeiboLog.d(TAG, "unset default account sql:"+where);
            db.update(TwitterTable.AUTbl.ACCOUNT_TBNAME, cv, where, null);

            where=TwitterTable.AUTbl.ACCOUNT_USERID+"="+newUserId+
                " and "+TwitterTable.AUTbl.ACCOUNT_TYPE+"="+TwitterTable.AUTbl.WEIBO_SINA;
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

        if (dbFlag) {
            mPreferences.edit().putLong(Constants.PREF_CURRENT_USER_ID, newUserId)
                .putString(Constants.PREF_SCREENNAME_KEY, oauthBean.name)
                .commit();
            App app=(App) App.getAppContext();
            app.initOauth2(true);
            startIntent();
        }
    }
}
