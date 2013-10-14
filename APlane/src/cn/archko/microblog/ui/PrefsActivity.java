package cn.archko.microblog.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.service.WeiboService;
import cn.archko.microblog.utils.AKUtils;
import com.andrew.apollo.utils.PreferenceUtils;
import com.andrew.apollo.utils.SharedPreferencesCompat;
import cn.archko.microblog.view.SeekBarPref;
import com.me.microblog.App;
import com.andrew.apollo.utils.ApolloUtils;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;
import com.me.microblog.utils.AKUtils;

/**
 * @author archko
 */
public class PrefsActivity extends PreferenceActivity implements
    SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String PREF_TAG="Options";
    /**
     * 图片的分辨率
     */
    public final static String PREF_RESOLUTION="resolution";
    /**
     * 是否显示图片,TODO,与上面的整合在一起,如果分辨率为0,表示不显示图片.
     */
    public final static String PREF_SHOW_BITMAP="show_bitmap";
    /**
     * 微博数量,暂时只针对主页
     */
    public final static String PREF_WEIBO_COUNT="pref_weibo_count";

    //--------------------- comments ---------------------
    /**
     * 详细页面的微博内容图片
     */
    public final static String PREF_COMMENT_STATUS_BM="pref_comment_status_bm";
    /**
     * 微博评论的用户头像。
     */
    public final static String PREF_COMMENT_USER_BM="pref_comment_user_bm";

    //--------------------- navigation ---------------------
    /**
     * 快速滚动的滑块
     */
    public final static String PREF_FAST_SCROLL="fast_scroll";
    /**
     * 显示右侧的导航按钮,可以快速地首尾导航.
     */
    public final static String PREF_SHOW_NAV_BTN="show_nav_btn";
    /**
     * 上下翻页(默认)/首尾导航
     */
    public final static String PREF_SHOW_NAV_PAGE_BTN="show_nav_page_btn";
    /**
     * 导航模式，有两种，标签的与侧边栏的，默认使用侧边栏，不用列表选择
     */
    public final static String PREF_NAV_MODE="nav_mode";
    /**
     * 显示侧边栏栏导航
     */
    public final static String PREF_NAV_SIDEBAR="nav_tab";
    /**
     * 显示tab标签栏导航
     */
    public final static String PREF_NAV_TAB="nav_tab";
    /**
     * 显示tab标签栏,现在已经废除,只有在使用tabs时有用到.
     */
    public final static String PREF_SHOW_NAV_TAB="show_nav_tab";
    /**
     * 侧边栏右侧的手势。
     */
    public final static String PREF_NAV_SIDEBAR_TOUCH="nav_sidebar_touch";

    //--------------------- other ---------------------
    /**
     * 自动更新
     */
    public final static String PREF_AUTO_CHK_UPDATE="auto_chk_update";
    /**
     * 只要wifi情况下检测更新,更新的频率还是一天一次
     */
    public final static String PREF_AUTO_CHK_UPDATE_WIFI_ONLY="pref_auto_chk_update_wifi_only";
    /**
     * 主题
     */
    public final static String PREF_THEME="theme";
    /**
     * 是否自动检查有无新的微博.
     */
    public final static String PREF_AUTO_CHK_NEW_STATUS="auto_chk_new_status";
    /**
     * 自动检查新微博的时间,TODO,需要将它与上面的整合在一起,时间也可以用seekbar来处理,如果为0表示不自动获取.
     */
    public final static String PREF_CHK_NEW_STATUS_TIME="chk_new_status_time";
    /**
     * 增量刷新,只要主页有用,当它为true时,下拉刷新只获取新的,而不是整页获取
     */
    public final static String PREF_UPDATE_INCREMENT="pref_update_increment";
    /**
     * 后退键的行为,直接退出,结束服务.
     */
    public final static String PREF_BACK_PRESSED="pref_back_pressed";

    private Resources resources;

    /**
     * 配置数组,主要用于有列表选择的配置项.
     */
    private static final String[] summaryKeys={PREF_RESOLUTION, PREF_THEME, PREF_CHK_NEW_STATUS_TIME};
    /**
     * 配置的列表值.
     */
    private static final int[] summaryEntryValues={R.array.resolutions, R.array.themes, R.array.default_chk_new_status_times, R.array.default_nav_sidebar_touch_val};
    /**
     * 配置列表项显示的提示
     */
    private static final int[] summaryEntries={R.array.resolutions_labels, R.array.themes_labels, R.array.default_chk_new_status_time_labels, R.array.default_nav_sidebar_touch_labels};
    private static final int[] summaryDefaults={R.string.default_resolution, R.string.default_theme, R.string.default_chk_new_status_time, R.string.default_nav_sidebar_touch};

    public String getString(SharedPreferences options, String key) {
        return getString(this.resources, options, key);
    }

    public static String getString(Resources resources, SharedPreferences options, String key) {
        for (int i=0; i<summaryKeys.length; i++)
            if (summaryKeys[i].equals(key))
                return options.getString(key, resources.getString(summaryDefaults[i]));
        return options.getString(key, "");
    }

    public void setSummaries() {
        for (int i=0; i<summaryKeys.length; i++) {
            setSummary(i);
        }
    }

    public void setSummary(String key) {
        for (int i=0; i<summaryKeys.length; i++) {
            if (summaryKeys[i].equals(key)) {
                setSummary(i);
                return;
            }
        }
    }

    public void setSummary(int i) {
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(this);

        Preference pref=findPreference(summaryKeys[i]);
        String value=options.getString(summaryKeys[i], resources.getString(summaryDefaults[i]));

        String[] valueArray=resources.getStringArray(summaryEntryValues[i]);
        String[] entryArray=resources.getStringArray(summaryEntries[i]);

        for (int j=0; j<valueArray.length; j++)
            if (valueArray[j].equals(value)) {
                pref.setSummary(entryArray[j]);
                return;
            }
    }

    public static int getIntFromString(SharedPreferences pref, String option, int def) {
        return Integer.parseInt(pref.getString(option, ""+def));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // UP
        getActionBar().setDisplayHomeAsUpEnabled(true);

        this.resources=getResources();
        addPreferencesFromResource(R.xml.prefs);

        /*try {
            setPreferenceScreen(createPreferences());
        } catch (final ClassCastException e) {
            WeiboLog.e("Shared preferences are corrupt! Resetting to default values.");

            final SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor=preferences.edit();
            editor.clear();
            editor.commit();

            setPreferenceScreen(createPreferences());
        }*/

        initInterface();
    }

    /**
     * 这里的xml资源需要以PreferenceScreen开头,否则无效.
     *
     * @return
     */
    PreferenceScreen createPreferences() {
        final PreferenceScreen root=getPreferenceManager().createPreferenceScreen(this);    //一些rom会崩溃，找不到res/drawable/list_selector_background.xml

        root.setTitle(R.string.pref_list);

        loadPreferences(root, R.xml.prefs_list);
        loadPreferences(root, R.xml.prefs_nav);
        loadPreferences(root, R.xml.prefs_msg);
        loadPreferences(root, R.xml.prefs_other);
        loadPreferences(root, R.xml.prefs_theme);

        return root;
    }

    void loadPreferences(final PreferenceScreen root, final int... resourceIds) {
        for (final int id : resourceIds) {
            setPreferenceScreen(null);
            addPreferencesFromResource(id);
            root.addPreference(getPreferenceScreen());
            setPreferenceScreen(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setSummaries();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences options, String key) {
        setSummary(key);
        updatePreference(key);
    }

    void updatePreference(final String key) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(PrefsActivity.this);
                if (PREF_AUTO_CHK_NEW_STATUS.equals(key)) {
                    boolean auto_chk_new_status=prefs.getBoolean(PREF_AUTO_CHK_NEW_STATUS, true);
                    Intent intent=new Intent(PrefsActivity.this, WeiboService.class);
                    if (auto_chk_new_status) {
                        WeiboLog.d(PREF_TAG, "sp changed,startservice.");
                        startService(intent);
                    } else {
                        WeiboLog.d(PREF_TAG, "sp changed,stopservice.");
                        stopService(intent);
                    }
                } else if (PREF_CHK_NEW_STATUS_TIME.equals(key)) {
                    String chk_new_status_time=prefs.getString(PREF_CHK_NEW_STATUS_TIME, "1");
                    if (chk_new_status_time.equals("0")) {
                        WeiboService.DELAY_TIME=1*1000*60;
                    } else if (chk_new_status_time.equals("1")) {
                        WeiboService.DELAY_TIME=2*1000*60;
                    } else if (chk_new_status_time.equals("2")) {
                        WeiboService.DELAY_TIME=5*1000*60;
                    } else if (chk_new_status_time.equals("3")) {
                        WeiboService.DELAY_TIME=20*1000*60;
                    }
                } else if (PREF_RESOLUTION.equals(key)) {
                    boolean slb="0".equals(prefs.getString(PrefsActivity.PREF_RESOLUTION, getString(R.string.default_resolution)));
                    if (slb) {
                        ((App) App.getAppContext()).clearLargeLruCache();
                    } else {
                        final int memClass=((ActivityManager) PrefsActivity.this.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
                        WeiboLog.d("memClass:"+memClass);
                        if (memClass<48) {
                            Toast.makeText(PrefsActivity.this, "您当前使用的是大图浏览模式,但内存过小,有可能会内存溢出!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (PREF_WEIBO_COUNT.equals(key)) {
                    int weiboCount=prefs.getInt(PREF_WEIBO_COUNT, Constants.WEIBO_COUNT);
                    WeiboLog.d(PREF_TAG ,"wc:"+weiboCount);
                    ((App) App.getAppContext()).loadAccount(prefs);
                }
            }
        });
    }

    //--------------------- color ---------------------

    /**
     * Initializes the preferences under the "Interface" category
     */
    private void initInterface() {
        updateWeiboCount();
        updateTitleFont();
        updateContentFont();
        updateRetContentFont();

        // Color scheme picker
        updateColorScheme();

        updateRetColorScheme();

        updateSidebarColorScheme();

        final Preference clearPrefs=findPreference("prefs_reset");
        clearPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(PrefsActivity.this, R.string.prefs_reset_toast, Toast.LENGTH_SHORT).show();
                //PrefsActivity.this.finish();
                WeiboLog.d("onPreferenceClick:"+preference);
                ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void... unused) {
                        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
                        final SharedPreferences.Editor editor=prefs.edit();
                        editor.remove(PREF_RESOLUTION)
                            .remove(PREF_SHOW_BITMAP)
                            .remove(PREF_COMMENT_STATUS_BM)
                            .remove(PREF_COMMENT_USER_BM)
                            .remove(PREF_FAST_SCROLL)
                            .remove(PREF_SHOW_NAV_BTN)
                            .remove(PREF_NAV_TAB)
                            .remove(PREF_SHOW_NAV_TAB)
                            .remove(PREF_AUTO_CHK_UPDATE)
                            .remove(PREF_AUTO_CHK_UPDATE_WIFI_ONLY)
                            .remove(PREF_THEME)
                            .remove(PREF_AUTO_CHK_NEW_STATUS)
                            .remove(PREF_CHK_NEW_STATUS_TIME)
                            .remove(PREF_UPDATE_INCREMENT)
                            .remove(PreferenceUtils.PREF_TITLE_FONT_SIZE)
                            .remove(PreferenceUtils.PREF_CONTENT_FONT_SIZE)
                            .remove(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE)
                            .remove(PreferenceUtils.DEFAULT_STATUS_THEME_COLOR)
                            .remove(PreferenceUtils.DEFAULT_RET_STATUS_THEME_COLOR)
                            .remove(PreferenceUtils.DEFAULT_SIDEBAR_THEME_COLOR);
                        ;
                        SharedPreferencesCompat.apply(editor);

                        return null;
                    }
                }, (Void[]) null);
                return false;
            }
        });
    }

    private void updateWeiboCount() {
        final SeekBarPref seekBarPref=(SeekBarPref) findPreference("pref_weibo_count");
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(this);
        int pref_weibo_count=options.getInt(PREF_WEIBO_COUNT, Constants.WEIBO_COUNT);
        seekBarPref.setInitialValue(pref_weibo_count, false, 12, Constants.WEIBO_COUNT*4);
    }

    private void updateTitleFont() {
        final SeekBarPref seekBarPref=(SeekBarPref) findPreference("pref_title_font_size");
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(this);
        float pref_title_font_size=options.getInt(PreferenceUtils.PREF_TITLE_FONT_SIZE, 14);
        seekBarPref.setInitialValue((int) pref_title_font_size);
    }

    private void updateContentFont() {
        final SeekBarPref seekBarPref=(SeekBarPref) findPreference("pref_content_font_size");
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(this);
        float pref_content_font_size=options.getInt(PreferenceUtils.PREF_CONTENT_FONT_SIZE, 16);
        seekBarPref.setInitialValue((int) pref_content_font_size);
    }

    private void updateRetContentFont() {
        final SeekBarPref seekBarPref=(SeekBarPref) findPreference("pref_ret_content_font_size");
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(this);
        float pref_ret_content_font_size=options.getInt(PreferenceUtils.PREF_RET_CONTENT_FONT_SIZE, 16);
        seekBarPref.setInitialValue((int) pref_ret_content_font_size);
    }

    /**
     * Shows the {@link ColorSchemeDialog} and then saves the changes.
     */
    private void updateColorScheme() {
        final Preference colorScheme=findPreference("status_color_scheme");
        colorScheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                AKUtils.showColorPicker(PrefsActivity.this, PreferenceUtils.DEFAULT_STATUS_THEME_COLOR,
                    PreferenceUtils.getInstace(PrefsActivity.this).getDefaultStatusThemeColor(PrefsActivity.this));
                return true;
            }
        });
    }

    /**
     * Shows the {@link ColorSchemeDialog} and then saves the changes.
     */
    private void updateRetColorScheme() {
        final Preference colorScheme=findPreference("ret_status_color_scheme");
        colorScheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                AKUtils.showColorPicker(PrefsActivity.this, PreferenceUtils.DEFAULT_RET_STATUS_THEME_COLOR,
                    PreferenceUtils.getInstace(PrefsActivity.this).getDefaultRetContentThemeColor(PrefsActivity.this));
                return true;
            }
        });
    }

    /**
     * sidebar font color
     */
    private void updateSidebarColorScheme() {
        final Preference colorScheme=findPreference("sidebar_color_scheme");
        colorScheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                AKUtils.showColorPicker(PrefsActivity.this, PreferenceUtils.DEFAULT_SIDEBAR_THEME_COLOR,
                    PreferenceUtils.getInstace(PrefsActivity.this).getDefaultRetContentThemeColor(PrefsActivity.this));
                return true;
            }
        });
    }

}
