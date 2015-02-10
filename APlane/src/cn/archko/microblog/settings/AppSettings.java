package cn.archko.microblog.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.PrefsActivity;
import com.me.microblog.App;

import java.io.File;

public class AppSettings {

    private static AppSettings current;

    /* =============== UI settings =============== */

    public final boolean showLargeBitmap;
    public final boolean showBitmap;
    public final boolean fastScroll;
    public final boolean showNavBtn;
    public final boolean showNavPageBtn;
    public final boolean showDetailLargeBitmap;
    public final boolean showDetailBitmap;
    public final boolean autoCheckUpdate;
    public final boolean updateIncrement;
    public final String mCacheDir;   //缓存图片存储上级目录.

    private AppSettings() {
        final SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        /* =============== UI settings =============== */
        showLargeBitmap="1".equals(prefs.getString(PrefsActivity.PREF_RESOLUTION, App.getAppContext().getString(R.string.default_resolution)));
        showBitmap=prefs.getBoolean(PrefsActivity.PREF_SHOW_BITMAP, true);
        fastScroll=false;
        showNavBtn=prefs.getBoolean(PrefsActivity.PREF_SHOW_NAV_BTN, true);
        showNavPageBtn=prefs.getBoolean(PrefsActivity.PREF_SHOW_NAV_PAGE_BTN, true);
        showDetailBitmap=prefs.getBoolean(PrefsActivity.PREF_COMMENT_STATUS_BM, true);
        showDetailLargeBitmap=prefs.getBoolean(PrefsActivity.PREF_COMMENT_STATUS_BM, false);
        autoCheckUpdate=prefs.getBoolean(PrefsActivity.PREF_AUTO_CHK_UPDATE, true);
        updateIncrement=prefs.getBoolean(PrefsActivity.PREF_UPDATE_INCREMENT, true);

        mCacheDir=((App) App.getAppContext()).mCacheDir;
        File file=new File(mCacheDir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static void init() {
        current=new AppSettings();
    }

    public static AppSettings current() {
        return current;
    }
}
