package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.archko.microblog.R;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import cn.archko.microblog.utils.AKUtils;

/**
 * @author archko date:2013-2-21
 */
public class WebviewActivity extends SkinFragmentActivity {

    WebView mWebView;
    EditText mUrl;
    TextView mEmptyTxt;
    ProgressBar mProgressBar;
    String mOriginUrl;

    ImageButton mRefresh, mStop, mBack, mForward;
    ImageButton mGo;
    InputMethodManager imm;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar=getActionBar();
        mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        //mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setHomeButtonEnabled(true);

        setProgressBarVisibility(true);
        setProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.ak_webview);

        mWebView=(WebView) findViewById(R.id.webview);
        mUrl=(EditText) findViewById(R.id.url_et);
        mEmptyTxt=(TextView) findViewById(R.id.empty_txt);
        mProgressBar=(ProgressBar) findViewById(R.id.progress_bar);
        mGo=(ImageButton) findViewById(R.id.webview_forward);
        mGo.setOnClickListener(clickListener);

        setCustomActionBar();

        WebSettings settings=mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        //settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSavePassword(true);

        WebViewClient wvc=new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mEmptyTxt.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mUrl.setText(url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                mEmptyTxt.setVisibility(View.VISIBLE);
                mEmptyTxt.setText(R.string.webview_receive_err);
            }

            /*@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }*/

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); 默认的处理方式，WebView变成空白页
                handler.proceed();//接受证书
                //handleMessage(Message msg); 其他处理
            }
        };
        mWebView.setWebViewClient(wvc);

        WebChromeClient webChromeClient=new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                //WeiboLog.v("newProgress:"+newProgress);
                setProgress(newProgress);
            }
        };
        mWebView.setWebChromeClient(webChromeClient);

        String url=getIntent().getStringExtra("url");
        mOriginUrl=url;
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }

        mUrl.clearFocus();
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUrl.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void setCustomActionBar() {
        View cusActionBar=getLayoutInflater().inflate(R.layout.ak_webview_nav, null);
        mActionBar.setCustomView(cusActionBar);
        mActionBar.setDisplayShowCustomEnabled(true);
        mRefresh=(ImageButton) cusActionBar.findViewById(R.id.menu_refresh);
        mStop=(ImageButton) cusActionBar.findViewById(R.id.menu_stop);
        mBack=(ImageButton) cusActionBar.findViewById(R.id.menu_back);
        mForward=(ImageButton) cusActionBar.findViewById(R.id.menu_forward);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int refreshId=R.drawable.navigation_refresh_dark;
        int backId=R.drawable.navigation_back_dark;
        int forwardId=R.drawable.navigation_forward_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            refreshId=R.drawable.navigation_refresh_light;
            backId=R.drawable.navigation_back_light;
            forwardId=R.drawable.navigation_forward_light;
        }

        mRefresh.setImageResource(refreshId);
        mBack.setImageResource(backId);
        mForward.setImageResource(forwardId);
        mGo.setImageResource(forwardId);

        mRefresh.setOnClickListener(clickListener);
        mStop.setOnClickListener(clickListener);
        mBack.setOnClickListener(clickListener);
        mForward.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clickMethod(v);
        }
    };

    private void clickMethod(View v) {
        int itemId=v.getId();
        if (itemId==R.id.menu_refresh) {
            AKUtils.showToast("reload.");
            String url=mUrl.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            } else {
                mWebView.loadUrl(mOriginUrl);
            }
        } else if (itemId==R.id.menu_stop) {
            mWebView.stopLoading();
            AKUtils.showToast("Stop loading.");
        } else if (itemId==R.id.menu_back) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                AKUtils.showToast("can not go back.");
            }
        } else if (itemId==R.id.menu_forward) {
            if (mWebView.canGoForward()) {
                mWebView.goForward();
            } else {
                AKUtils.showToast("can not go forward.");
            }
        } else if (itemId==R.id.webview_forward) {
            String url=mUrl.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            }
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_update).setVisible(false);

        menu.add(0, R.id.menu_refresh, 1, R.string.opb_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, R.id.menu_exit, 1, R.string.action_stop)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        String themeId=mPrefs.getString(PrefsActivity.PREF_THEME, "0");
        int menuStop=android.R.drawable.ic_delete;
        int refreshId=R.drawable.navigation_refresh_dark;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else {
            refreshId=R.drawable.navigation_refresh_light;
        }
        menu.findItem(R.id.menu_exit).setIcon(menuStop);
        menu.findItem(R.id.menu_refresh).setIcon(refreshId);
        menu.add(0, R.id.menu_update, 1, R.string.webview_out_app)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            finish();
        } else if (itemId==R.id.menu_update) {
            WeiboUtil.openUrlByDefaultBrowser(WebviewActivity.this, mOriginUrl);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*mWebView.clearView();
        mWebView.destroy();*/
    }
}
