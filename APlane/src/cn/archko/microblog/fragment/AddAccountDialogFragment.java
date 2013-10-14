package com.me.microblog.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.me.microblog.App;
import com.me.microblog.R;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.oauth.SOauth2;
import com.me.microblog.util.Constants;
import com.me.microblog.util.SqliteWrapper;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.utils.AKUtils;

/**
 * @description:
 * @author: archko 13-8-31 :上午7:52
 */
public class AddAccountDialogFragment extends DialogFragment {

    public static final String TAG="AddAccountDialogFragment";
    private EditText email, pwd;
    InputMethodManager imm;
    int type=0;
    ProgressDialog mProgressDialog;
    AccountOauthListener mAccountOauthListener;

    public AddAccountDialogFragment(AccountOauthListener accountOauthListener) {
        mAccountOauthListener=accountOauthListener;
    }

    public void setAccountOauthListener(AccountOauthListener mAccountOauthListener) {
        this.mAccountOauthListener=mAccountOauthListener;
    }

    interface AccountOauthListener {

        void oauthed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id=v.getId();
            if (id==R.id.exit) {
                dismiss();
            } else if (id==R.id.login2) {
                type=0;
                addAccount();
            } else if (id==R.id.login_show_webview_btn) {
                type=1;
                addAccount();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.login, container, false);
        v.findViewById(R.id.regist_btn).setVisibility(View.GONE);   //hide regist_btn
        v.findViewById(R.id.desc).setVisibility(View.GONE); //hide desc
        v.findViewById(R.id.other_login_btn).setVisibility(View.GONE); //hide desc
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

        return v;
    }

    /**
     * 添加帐户
     */
    private void addAccount() {
        String username=email.getEditableText().toString();
        String password=pwd.getEditableText().toString();

        if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)) {
            AKUtils.showToast("请输入帐户及密码");
            return;
        }

        //TODO search the username first
        OauthBean oauthBean=SqliteWrapper.queryAccount(App.getAppContext(), TwitterTable.AUTbl.WEIBO_SINA, username);
        if (null!=oauthBean) {
            WeiboLog.i(TAG, "已经存在用户:"+oauthBean);
            AKUtils.showToast(R.string.oauth_account_exist);
            return;
        }

        if (type==1) {
            FragmentTransaction ft=getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev=getActivity().getSupportFragmentManager().findFragmentByTag("oauth_dialog");
            if (prev!=null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            OauthDialogFragment oauthDialogFragment=new OauthDialogFragment(mOauthHandler);
            oauthDialogFragment.show(ft, "oauth_dialog");
        } else {
            Object[] params=new Object[]{username, password};
            SOauth2 ouath2=new SOauth2();

            ouath2.oauthByWebView(new Object[]{username, password, App.getAppContext(), mOauthHandler, params});
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
                    FragmentTransaction ft=getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev=getActivity().getSupportFragmentManager().findFragmentByTag("oauth_dialog");
                    if (prev!=null) {
                        ft.remove(prev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!isResumed()) {
                WeiboLog.d("!isResumed()");
                return;
            }

            super.handleMessage(msg);
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
        WeiboLog.d(TAG, "objects:"+objects);

        if (null==objects||objects[0]==null) {
            AKUtils.showToast(R.string.login2_error);
            WeiboLog.e(TAG, "运行中认证失败。");
            return;
        }

        try {
            OauthBean oauthBean=(OauthBean) objects[0];
            Object[] params=(Object[]) objects[1];
            WeiboLog.d("bean:"+oauthBean+" params:"+params);
            if (oauthBean!=null) {
                WeiboLog.d(TAG, "认证成功。");
                String username=(String) params[0];
                String password=(String) params[1];
                if (type==1) {
                    username=password="";
                }

                Uri uri=SqliteWrapper.addAccount(App.getAppContext(), oauthBean, username, password, TwitterTable.AUTbl.WEIBO_SINA, "-1");
                WeiboLog.d(TAG, "保存新用户："+uri);
                if (null!=uri) {
                    AKUtils.showToast(R.string.account_add_suc);
                    //newTaskNoNet(new Object[]{true, -1l, -1l, 1, page, false}, null);
                    if (null!=mAccountOauthListener) {
                        mAccountOauthListener.oauthed();
                    }
                    dismiss();
                } else {
                    AKUtils.showToast(R.string.account_add_failed);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}