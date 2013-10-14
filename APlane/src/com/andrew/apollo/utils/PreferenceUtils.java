package com.andrew.apollo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import cn.archko.microblog.R;
import cn.archko.microblog.ui.PrefsActivity;

/**
 * A collection of helpers designed to get and set various preferences across
 * Apollo.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class PreferenceUtils {

    /* Default start page (Artist page) */
    public static final int DEFFAULT_PAGE = 2;

    /* Saves the last page the pager was on in {@link MusicBrowserPhoneFragment} */
    public static final String START_PAGE = "start_page";

    // Sets the type of layout to use for the artist list
    public static final String ARTIST_LAYOUT = "artist_layout";

    // Sets the type of layout to use for the album list
    public static final String ALBUM_LAYOUT = "album_layout";

    // Sets the type of layout to use for the recent list
    public static final String RECENT_LAYOUT = "recent_layout";

    // Key used to download images only on Wi-Fi
    public static final String ONLY_ON_WIFI = "only_on_wifi";

    // Enables lock screen controls on Honeycomb and above
    public static final String USE_LOCKSREEN_CONTROLS = "use_lockscreen_controls";

    // Key used to set the overall theme color
    public static final String DEFAULT_THEME_COLOR = "default_theme_color";

    //--------------------- 这三个配置不能在apicore中引用 ---------------------
    public final static String PREF_TITLE_FONT_SIZE="pref_title_font_size"; //微博标题字体
    public final static String PREF_CONTENT_FONT_SIZE="pref_content_font_size"; //微博内容字体
    public final static String PREF_RET_CONTENT_FONT_SIZE="pref_ret_content_font_size"; //转发内容字体

    // Key used to set the overall theme color
    public static final String DEFAULT_STATUS_THEME_COLOR = "default_status_theme_color";
    // Key used to set the overall theme color
    public static final String DEFAULT_RET_STATUS_THEME_COLOR = "default_ret_status_theme_color";
    public static final String DEFAULT_SIDEBAR_THEME_COLOR = "default_sidebar_theme_color";
    //sidebar item
    public static final String PREF_SIDEBAR_MY_POST = "pref_sidebar_my_post";
    public static final String PREF_SIDEBAR_MY_FAV = "pref_sidebar_my_fav";
    public static final String PREF_SIDEBAR_PUBLIC = "pref_sidebar_public";
    public static final String PREF_SIDEBAR_PROFILE = "pref_sidebar_profile";
    public static final String PREF_SIDEBAR_PLACE_NEARBY_PHOTOS = "pref_sidebar_place_nearby_photos";
    public static final String PREF_SIDEBAR_PLACE_NEARBY_USERS = "pref_sidebar_place_nearby_users";
    public static final String PREF_SIDEBAR_PLACE_FRIEND_TIMELINE = "pref_sidebar_place_friend_timeline";
    public static final String PREF_SIDEBAR_DM = "pref_sidebar_dm";

    //load url
    public static final String PREF_WEBVIEW="pref_webview";
    public String PREF_DEFAULT_THEME="2";

    private static PreferenceUtils sInstance;

    private final SharedPreferences mPreferences;

    /**
     * Constructor for <code>PreferenceUtils</code>
     * 
     * @param context The {@link android.content.Context} to use.
     */
    public PreferenceUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        PREF_DEFAULT_THEME=context.getString(R.string.default_theme);
    }

    /**
     * @param context The {@link android.content.Context} to use.
     * @return A singelton of this class
     */
    public static final PreferenceUtils getInstace(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Returns the last page the user was on when the app was exited.
     *
     * @return The page to start on when the app is opened.
     */
    public final int getStartPage() {
        return mPreferences.getInt(START_PAGE, DEFFAULT_PAGE);
    }

    /**
     * Returns the current theme.
     *
     * @param context The {@link android.content.Context} to use.
     * @return The default theme.
     */
    public final String getDefaultTheme() {
        String themeId=mPreferences.getString(PrefsActivity.PREF_THEME, PREF_DEFAULT_THEME);
        return themeId;
    }

    /**
     * Sets the new theme color.
     *
     * @param value The new theme color to use.
     */
    public void setDefaultThemeColor(final int value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(DEFAULT_THEME_COLOR, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[])null);
    }

    /**
     * Returns the current theme color.
     *
     * @param context The {@link android.content.Context} to use.
     * @return The default theme color.
     */
    public final int getDefaultThemeColor(final Context context) {
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(context);
        String themeId=options.getString(PrefsActivity.PREF_THEME, "0");
        int colorId=R.color.holo_dark_item_title;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else if ("2".equals(themeId)) {
            colorId=R.color.holo_light_item_title;
        } else if ("3".equals(themeId)) {
        }

        return mPreferences.getInt(DEFAULT_THEME_COLOR, context.getResources().getColor(colorId));
    }

    /**
     * Sets the new theme color.status content
     *
     * @param value The new theme color to use.
     */
    public void setDefaultFontThemeColor(final String key, final int value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor=mPreferences.edit();
                editor.putInt(key, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[]) null);
    }

    /**
     * Sets the new theme color.status content
     *
     * @param value The new theme color to use.
     */
    public void setDefaultStatusThemeColor(final int value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor=mPreferences.edit();
                editor.putInt(DEFAULT_STATUS_THEME_COLOR, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[]) null);
    }

    /**
     * Returns the current theme color. status content
     *
     * @param context The {@link android.content.Context} to use.
     * @return The default theme color.
     */
    public final int getDefaultStatusThemeColor(final Context context) {
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(context);
        String themeId=options.getString(PrefsActivity.PREF_THEME, "0");
        int colorId=R.color.holo_dark_item_status;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else if ("2".equals(themeId)) {
            colorId=R.color.holo_light_item_status;
        } else if ("3".equals(themeId)) {
        }

        return mPreferences.getInt(DEFAULT_STATUS_THEME_COLOR, context.getResources().getColor(colorId));
    }

    /**
     * Sets the new theme color.retweetstatus content
     *
     * @param value The new theme color to use.
     */
    public void setDefaultRetContentThemeColor(final int value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor=mPreferences.edit();
                editor.putInt(DEFAULT_RET_STATUS_THEME_COLOR, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[]) null);
    }

    /**
     * Returns the current theme color.retweetstatus content
     *
     * @param context The {@link android.content.Context} to use.
     * @return The default theme color.
     */
    public final int getDefaultRetContentThemeColor(final Context context) {
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(context);
        String themeId=options.getString(PrefsActivity.PREF_THEME, "0");
        int colorId=R.color.holo_dark_item_ret_status;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else if ("2".equals(themeId)) {
            colorId=R.color.holo_light_item_ret_status;
        } else if ("3".equals(themeId)) {
        }

        return mPreferences.getInt(DEFAULT_RET_STATUS_THEME_COLOR, context.getResources().getColor(colorId));
    }

    /**
     * Returns the current theme color.retweetstatus content
     *
     * @param context The {@link android.content.Context} to use.
     * @return The default theme color.
     */
    public final int getDefaultSidebarThemeColor(final Context context) {
        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(context);
        String themeId=options.getString(PrefsActivity.PREF_THEME, "0");
        int colorId=R.color.holo_dark_item_sidebar;
        if ("0".equals(themeId)) {
        } else if ("1".equals(themeId)) {
        } else if ("2".equals(themeId)) {
            colorId=R.color.holo_light_item_sidebar;
        } else if ("3".equals(themeId)) {
        }

        return mPreferences.getInt(DEFAULT_SIDEBAR_THEME_COLOR, context.getResources().getColor(colorId));
    }

    /**
     * @return True if the user has checked to only download images on Wi-Fi,
     *         false otherwise
     */
    public final boolean onlyOnWifi() {
        return mPreferences.getBoolean(ONLY_ON_WIFI, true);
    }

    /**
     * @param value True if the user only wants to download images on Wi-Fi,
     *            false otherwise
     */
    public void setOnlyOnWifi(final boolean value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(ONLY_ON_WIFI, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[])null);
    }

    /**
     * @return True if the user has checked to use lockscreen controls, false
     *         otherwise.
     */
    public final boolean enableLockscreenControls() {
        return mPreferences.getBoolean(USE_LOCKSREEN_CONTROLS, true);
    }

    /**
     * @param value True if the user has checked to use lockscreen controls,
     *            false otherwise.
     */
    public void setLockscreenControls(final boolean value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(USE_LOCKSREEN_CONTROLS, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[])null);
    }

    /**
     * Saves the layout type for a list
     *
     * @param key Which layout to change
     * @param value The new layout type
     */
    private void setLayoutType(final String key, final String value) {
        ApolloUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(key, value);
                SharedPreferencesCompat.apply(editor);

                return null;
            }
        }, (Void[])null);
    }

    /**
     * Sets the layout type for the artist list
     *
     * @param value The new layout type
     */
    public void setArtistLayout(final String value) {
        setLayoutType(ARTIST_LAYOUT, value);
    }

    /**
     * Sets the layout type for the album list
     *
     * @param value The new layout type
     */
    public void setAlbumLayout(final String value) {
        setLayoutType(ALBUM_LAYOUT, value);
    }

    /**
     * Sets the layout type for the recent list
     *
     * @param value The new layout type
     */
    public void setRecentLayout(final String value) {
        setLayoutType(RECENT_LAYOUT, value);
    }

    /**
     * @param context The {@link android.content.Context} to use.
     * @param which Which list to check.
     * @return True if the layout type is the simple layout, false otherwise.
     */
    public boolean isSimpleLayout(final String which, final Context context) {
        final String simple = "simple";
        final String defaultValue = "grid";
        return mPreferences.getString(which, defaultValue).equals(simple);
    }

    /**
     * @param context The {@link android.content.Context} to use.
     * @param which Which list to check.
     * @return True if the layout type is the simple layout, false otherwise.
     */
    public boolean isDetailedLayout(final String which, final Context context) {
        final String detailed = "detailed";
        final String defaultValue = "grid";
        return mPreferences.getString(which, defaultValue).equals(detailed);
    }

    /**
     * @param context The {@link android.content.Context} to use.
     * @param which Which list to check.
     * @return True if the layout type is the simple layout, false otherwise.
     */
    public boolean isGridLayout(final String which, final Context context) {
        final String grid = "grid";
        final String defaultValue = "simple";
        return mPreferences.getString(which, defaultValue).equals(grid);
    }

}
