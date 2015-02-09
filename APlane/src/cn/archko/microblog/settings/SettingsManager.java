package cn.archko.microblog.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.archko.microblog.settings.listeners.IAppSettingsChangeListener;
import cn.archko.microblog.utils.listeners.ListenerProxy;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SettingsManager {

    public static final int INITIAL_FONTS=1<<0;

    private static final String INITIAL_FLAGS="initial_flags";

    static Context ctx;

    static SharedPreferences prefs;

    static final ReentrantReadWriteLock lock=new ReentrantReadWriteLock();

    static ListenerProxy listeners=new ListenerProxy(IAppSettingsChangeListener.class);

    public static void init(final Context context) {
        if (ctx==null) {
            ctx=context;
            prefs=PreferenceManager.getDefaultSharedPreferences(context);

            AppSettings.init();
        }
    }

    public static void addListener(final Object l) {
        listeners.addListener(l);
    }

    public static void removeListener(final Object l) {
        listeners.removeListener(l);
    }

    public static boolean isInitialFlagsSet(final int flag) {
        if (prefs.contains(INITIAL_FLAGS)) {
            return (prefs.getInt(INITIAL_FLAGS, 0)&flag)==flag;
        }
        return false;
    }

    public static void setInitialFlags(final int flag) {
        final int old=prefs.getInt(INITIAL_FLAGS, 0);
        prefs.edit().putInt(INITIAL_FLAGS, old|flag).commit();
    }
}
