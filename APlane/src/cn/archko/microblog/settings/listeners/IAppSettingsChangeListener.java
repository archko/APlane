package cn.archko.microblog.settings.listeners;

import cn.archko.microblog.settings.AppSettings;

public interface IAppSettingsChangeListener {

    //void onAppSettingsChanged(AppSettings oldSettings, AppSettings newSettings, AppSettings.Diff diff);
    void onAppSettingsChanged(AppSettings oldSettings);

}
