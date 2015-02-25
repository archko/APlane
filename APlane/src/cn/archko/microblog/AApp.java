package cn.archko.microblog;

import cn.archko.microblog.settings.AppSettings;
import com.me.microblog.App;

/**
 * @author: archko 2015/2/9 :17:31
 */
public class AApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings.init();
    }
}
