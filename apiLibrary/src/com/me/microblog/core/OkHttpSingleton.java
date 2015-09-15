package com.me.microblog.core;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * @author: archko 2015/9/15 :9:11
 */
public class OkHttpSingleton {

    private static OkHttpSingleton ourInstance=new OkHttpSingleton();
    private OkHttpClient okHttpClient;

    public static OkHttpSingleton getInstance() {
        return ourInstance;
    }

    private OkHttpSingleton() {
        okHttpClient=new OkHttpClient();
        okHttpClient.setConnectTimeout(AbsApiImpl.CONNECT_TIMEOUT, TimeUnit.SECONDS); // connect timeout
        okHttpClient.setReadTimeout(AbsApiImpl.READ_TIMEOUT, TimeUnit.SECONDS);    // socket timeout
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
