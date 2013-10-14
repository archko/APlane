package cn.archko.microblog.settings;

import cn.archko.microblog.settings.listeners.IAppSettingsChangeListener;

public class AppSettings {

    private static AppSettings current;


    /* =============================================== */

    private AppSettings() {

    }

    public static void init() {
        current = new AppSettings();
    }

    public static AppSettings current() {
        SettingsManager.lock.readLock().lock();
        try {
            return current;
        } finally {
            SettingsManager.lock.readLock().unlock();
        }
    }

    static void onSettingsChanged() {
        final AppSettings oldAppSettings = current;
        current = new AppSettings();
        applySettingsChanges(oldAppSettings, current);
    }

    static void applySettingsChanges(final AppSettings oldSettings, final AppSettings newSettings) {
        final IAppSettingsChangeListener l = SettingsManager.listeners.getListener();
        l.onAppSettingsChanged(oldSettings);
    }
}
