package cn.archko.microblog.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.me.microblog.App;
import com.me.microblog.oauth.SOauth2;

/**
 * @description: 认证对话框, 登录与添加帐户共同使用.
 * @author: archko 13-8-31 :上午7:48
 */
public class OauthDialogFragment extends DialogFragment {

    Handler mHandler;

    public OauthDialogFragment(Handler handler) {
        mHandler=handler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.oauth_token_expired_dialog_title);

        //imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SOauth2 ouath2=new SOauth2();
        WebView webView=ouath2.oauthByWebView(new Object[]{"", "", App.getAppContext(), mHandler, null});
        return webView;
    }
}
