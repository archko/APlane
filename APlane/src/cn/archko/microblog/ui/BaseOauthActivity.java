package cn.archko.microblog.ui;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import com.me.microblog.oauth.Oauth2Handler;
import com.me.microblog.oauth.OauthCallback;
import com.me.microblog.util.Constants;

/**
 * 包含认证的
 * User: archko Date: 12-9-4 Time: 上午10:57
 */
public class BaseOauthActivity extends SkinActivity {

    SharedPreferences mPreferences;
    /**
     * 当前登录用户的id
     */
    long currentUserId = - 1l;
    //--------------------- 认证 ---------------------
    Oauth2Handler mOauth2Handler;
    OauthCallback mOauthCallback = new OauthCallback() {
        @Override
        public void postOauthSuc(Object[] params) {
            oauthSuccessfully(params);
        }

        @Override
        public void postOauthFailed(int oauthCode) {
            oauthFailed(oauthCode);
        }
    };

    /**
     * 认证成功后的操作
     */
    void oauthSuccessfully(Object[] params) {
    }

    /**
     * 认证失败后的操作，如果是列表，默认是刷新
     *
     * @param oauthCode 认证失败的代码,如果是特定的,就需要重新登录.
     */
    public void oauthFailed(int oauthCode) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long aUserId = mPreferences.getLong(Constants.PREF_CURRENT_USER_ID, - 1);
        this.currentUserId = aUserId;

        mOauth2Handler = new Oauth2Handler(this, mOauthCallback);
        final ActionBar bar = getActionBar();
        bar.hide();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
