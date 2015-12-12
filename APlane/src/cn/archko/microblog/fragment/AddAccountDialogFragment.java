package cn.archko.microblog.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import cn.archko.microblog.R;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.oauth.SOauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;

/**
 * @description:
 * @author: archko 13-8-31 :上午7:52
 */
public class AddAccountDialogFragment extends DialogFragment {

    public static final String TAG="AddAccountDialogFragment";
    public static final String KEY_USERNAME="key_username";
    public static final String KEY_PASSWORD="key_password";
    public static final String KEY_OAUTH_KEY="key_oauth_key";
    public static final String KEY_OAUTH_SECRET="key_oauth_secret";
    public static final String KEY_OAUTH_URL="key_oauth_url";

    private EditText email, pwd;
    InputMethodManager imm;
    private static final int OAUTH_TYPE_CLIENT=0;
    private static final int OAUTH_TYPE_WEBVIEW=1;
    int type=OAUTH_TYPE_CLIENT;
    ProgressDialog mProgressDialog;
    AccountOauthListener mAccountOauthListener;
    Spinner mSpinner;

    public AddAccountDialogFragment() {
        super();
    }

    public AddAccountDialogFragment(AccountOauthListener accountOauthListener) {
        super();
        mAccountOauthListener=accountOauthListener;
    }

    public void setAccountOauthListener(AccountOauthListener accountOauthListener) {
        this.mAccountOauthListener=accountOauthListener;
    }

    interface AccountOauthListener {

        void oauthed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int resId=android.R.style.Theme_Holo_Light_NoActionBar;
        if ("2".equals(themeId)) {
            resId=android.R.style.Theme_Holo_Light_NoActionBar;
        }
        setStyle(DialogFragment.STYLE_NORMAL, resId);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id=v.getId();
            if (id==R.id.exit) {
                dismiss();
            } else if (id==R.id.login2) {
                type=OAUTH_TYPE_CLIENT;
                addAccount();
            } else if (id==R.id.login_show_webview_btn) {
                type=OAUTH_TYPE_WEBVIEW;
                addAccount();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.ak_account_add, container, false);

        Button button=(Button) v.findViewById(R.id.exit);
        button.setOnClickListener(clickListener);

        button=(Button) v.findViewById(R.id.login2);
        button.setOnClickListener(clickListener);

        button=(Button) v.findViewById(R.id.login_show_webview_btn);
        button.setOnClickListener(clickListener);

        email=(EditText) v.findViewById(R.id.email);
        SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        String name=mPrefs.getString(Constants.PREF_USERNAME_KEY, "");
        if (!"".equals(name)) {
            email.setText(name);
        }
        pwd=(EditText) v.findViewById(R.id.pwd);
        mSpinner=(Spinner) v.findViewById(R.id.app_group);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
            getActivity(), R.array.app_label, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (WeiboLog.isDEBUG()) {
                        WeiboLog.d("Spinner1: position="+position+" id="+id);
                    }
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    if (WeiboLog.isDEBUG()) {
                        WeiboLog.d("Spinner1: unselected");
                    }
                }
            });
    }

    /**
     * 添加帐户
     */
    private void addAccount() {
        String username=email.getEditableText().toString();
        String password=pwd.getEditableText().toString();

        //TODO search the username first
        OauthBean oauthBean=SqliteWrapper.queryAccount(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA, username);
        if (null!=oauthBean) {
            WeiboLog.i(TAG, "已经存在用户:"+oauthBean);
            NotifyUtils.showToast(R.string.oauth_account_exist);
            return;
        }

        if (type==OAUTH_TYPE_WEBVIEW) { //打开网页认证
            FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            Fragment prev=getActivity().getFragmentManager().findFragmentByTag("oauth_dialog");
            if (prev!=null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            String key=SOauth2.CONSUMER_KEY;
            String url=SOauth2.CALLBACK_URL;
            String secret="";
            if (mSpinner.getSelectedItemPosition()==1) {
                key=SOauth2.DESKTOP_KEY;
                secret=SOauth2.DESKTOP_SECRET;
                url=SOauth2.DESKTOP_CALLBACK;
            }
            Bundle bundle=new Bundle();
            bundle.putString(KEY_USERNAME, username);
            bundle.putString(KEY_PASSWORD, password);
            bundle.putString(KEY_OAUTH_SECRET, secret);
            bundle.putString(KEY_OAUTH_KEY, key);
            bundle.putString(KEY_OAUTH_URL, url);

            // Create and show the dialog.
            OauthDialogFragment oauthDialogFragment=new OauthDialogFragment(mOauthHandler);
            oauthDialogFragment.setArguments(bundle);
            oauthDialogFragment.show(ft, "oauth_dialog");
        } else {    //后台认证
            if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)) {
                NotifyUtils.showToast("请输入帐户及密码");
                return;
            }

            Object[] params=new Object[]{username, password};
            SOauth2 ouath2=new SOauth2();

            String key=SOauth2.CONSUMER_KEY;
            String url=SOauth2.CALLBACK_URL;
            String secret="";
            if (mSpinner.getSelectedItemPosition()==1) {
                key=SOauth2.DESKTOP_KEY;
                secret=SOauth2.DESKTOP_SECRET;
                url=SOauth2.DESKTOP_CALLBACK;
            }
            ouath2.oauthByWebView(new Object[]{username, password, App.getAppContext(), mOauthHandler, params,
                key, secret, url});
            if (null==mProgressDialog) {
                mProgressDialog=new ProgressDialog(getActivity());
            }
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle(R.string.account_add_dialog_title);
            mProgressDialog.setMessage(getString(R.string.account_add_dialog_msg));
            mProgressDialog.show();
        }
    }

    Handler mOauthHandler=new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (null!=mProgressDialog) {
                mProgressDialog.dismiss();
            }

            if (type==1) {
                try {
                    FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
                    Fragment prev=getActivity().getFragmentManager().findFragmentByTag("oauth_dialog");
                    if (prev!=null) {
                        ft.remove(prev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!isResumed()) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d("!isResumed()");
                }
                return;
            }

            int what=msg.what;
            if (what==0) {
                oauthResult(msg);
            }
        }
    };

    /**
     * Oauth2认证结果
     *
     * @param msg
     */
    void oauthResult(Message msg) {
        final Object[] objects=(Object[]) msg.obj;
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "objects:"+objects);
        }

        if (null==objects||objects[0]==null) {
            NotifyUtils.showToast(R.string.login2_error);
            WeiboLog.e(TAG, "运行中认证失败。");
            return;
        }

        try {
            OauthBean oauthBean=(OauthBean) objects[0];
            Object[] params=(Object[]) objects[1];
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("bean:"+oauthBean+" params:"+params);
            }
            if (oauthBean!=null) {
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "认证成功。");
                }
                if (null!=params) {
                    String username=(String) params[0];
                    String password=(String) params[1];
                    if (type==1) {
                        username=password="";
                    }
                    oauthBean.name=username;
                    oauthBean.pass=password;
                }

                Uri uri=SqliteWrapper.addAccount(App.getAppContext(), oauthBean, "-1");
                if (WeiboLog.isDEBUG()) {
                    WeiboLog.d(TAG, "保存新用户："+uri+" bean:"+oauthBean);
                }
                if (null!=uri) {
                    NotifyUtils.showToast(R.string.account_add_suc);
                    //newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
                    if (null!=mAccountOauthListener) {
                        mAccountOauthListener.oauthed();
                    }
                    dismiss();
                } else {
                    NotifyUtils.showToast(R.string.account_add_failed);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}