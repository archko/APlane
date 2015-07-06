package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
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
import com.bulletnoid.android.widget.SwipeAwayLayout;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.util.DisplayUtils;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko date:2013-2-21
 */
public class WebviewActivity extends SkinFragmentActivity {

    WebView mWebView;
    EditText mUrl;
    View url_layout;
    TextView mEmptyTxt;
    ProgressBar mProgressBar;
    String mOriginUrl;

    ImageButton mEdit, mRefresh, mStop, mBack, mForward;
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
        url_layout=findViewById(R.id.url_layout);
        mEmptyTxt=(TextView) findViewById(R.id.empty_txt);
        mProgressBar=(ProgressBar) findViewById(R.id.progress_bar);
        mGo=(ImageButton) findViewById(R.id.webview_forward);
        mGo.setOnClickListener(clickListener);

        setCustomActionBar();
        changeOriention(getResources().getConfiguration().orientation);

        WebSettings settings=mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        //settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSavePassword(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

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

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://video.weibo.com")&&!url.contains("type=")) {
                    url+="&type=mp4";
                    mWebView.loadUrl(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            /*@Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }*/

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); 默认的处理方式，WebView变成空白页
                handler.proceed();//接受证书
                //handleMessage(Message msg); 其他处理
            }

            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                resend.sendToTarget();
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

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri=Uri.parse(url);
                Intent intent=new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setCustomActionBar() {
        WeiboLog.d("setCustomActionBar");
        View cusActionBar=getLayoutInflater().inflate(R.layout.ak_webview_nav, null);
        mActionBar.setCustomView(cusActionBar);
        mActionBar.setDisplayShowCustomEnabled(true);
        mEdit=(ImageButton) cusActionBar.findViewById(R.id.menu_edit);
        mRefresh=(ImageButton) cusActionBar.findViewById(R.id.menu_refresh);
        mStop=(ImageButton) cusActionBar.findViewById(R.id.menu_stop);
        mBack=(ImageButton) cusActionBar.findViewById(R.id.menu_back);
        mForward=(ImageButton) cusActionBar.findViewById(R.id.menu_forward);

        String themeId=PreferenceUtils.getInstace(App.getAppContext()).getDefaultTheme();
        int refreshId=R.drawable.navigation_refresh_light;
        int backId=R.drawable.navigation_back_light;
        int forwardId=R.drawable.navigation_forward_light;
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

        mEdit.setOnClickListener(clickListener);
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
        if (itemId==R.id.menu_edit) {
            if (url_layout.getVisibility()==View.VISIBLE) {
                url_layout.setVisibility(View.GONE);
            } else {
                url_layout.setVisibility(View.VISIBLE);
            }
        } else if (itemId==R.id.menu_refresh) {
            NotifyUtils.showToast("reload.");
            String url=mUrl.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            } else {
                mWebView.loadUrl(mOriginUrl);
            }
        } else if (itemId==R.id.menu_stop) {
            mWebView.stopLoading();
            NotifyUtils.showToast("Stop loading.");
        } else if (itemId==R.id.menu_back) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                NotifyUtils.showToast("can not go back.");
            }
        } else if (itemId==R.id.menu_forward) {
            if (mWebView.canGoForward()) {
                mWebView.goForward();
            } else {
                NotifyUtils.showToast("can not go forward.");
            }
        } else if (itemId==R.id.webview_forward) {
            String url=mUrl.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeOriention(newConfig.orientation);
    }

    private void changeOriention(int orientation) {
        WeiboLog.d("changeOriention:"+orientation);
        if (orientation==Configuration.ORIENTATION_PORTRAIT) {
            mActionBar.show();
            DisplayUtils.setFullscreen(getWindow(), false);
        } else if (orientation==Configuration.ORIENTATION_LANDSCAPE) {
            mActionBar.hide();
            DisplayUtils.setFullscreen(getWindow(), true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuItem actionItem=menu.add(0, Menu.FIRST, 0, R.string.webview_out_app);

        // Items that show as actions should favor the "if room" setting, which will
        // prevent too many buttons from crowding the bar. Extra items will show in the
        // overflow area.
        MenuItemCompat.setShowAsAction(actionItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if (itemId==android.R.id.home) {
            finish();
        } else if (itemId==Menu.FIRST) {
            WeiboUtils.openUrlByDefaultBrowser(WebviewActivity.this, mOriginUrl);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mWebView.clearView();
        mWebView.stopLoading();
        mWebView.destroy();
    }
}
