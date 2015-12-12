package com.me.microblog.core.factory;

import com.me.microblog.WeiboException;
import com.me.microblog.oauth.OauthBean;
import com.me.microblog.oauth.ServiceProvider;
import com.me.microblog.util.WeiboLog;

import java.lang.reflect.Constructor;

/**
 * 构造一个api工厂,用于多种类型的api工厂构造,由于原来的是只使用新浪微博的api,所以在Fragment中初始化是固定的.
 * 这里用反射后,添加不同的api,根据不同的登录用户类型,就可以处理了.
 *
 * @author archko
 */
public class ApiConfigFactory {

    public static synchronized AbsApiFactory getApiConfig(OauthBean auth) throws WeiboException {
        if (auth == null) {
            throw new WeiboException("no account info.");
        }

        String packageName = ApiConfigFactory.class.getPackage().getName();
        ServiceProvider sp = auth.getServiceProvider();
        //packageName+=".impl."+sp.toString().toLowerCase();
        packageName += "." + sp.toString() + "ApiFactory";
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("packageName:" + packageName);
        }

        AbsApiFactory apiFactory = null;
        try {
            Class<?> apiConfigInstanceClass = Class.forName(packageName);
            Constructor<?> constructor = apiConfigInstanceClass.getConstructor();
            apiFactory = (AbsApiFactory) constructor.newInstance();
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("construct api factory:" + apiFactory);
            }
        } catch (Exception e) {
            WeiboLog.e("ApiConfigFactory:{}" + sp + e);
        }

        return apiFactory;
    }
}
