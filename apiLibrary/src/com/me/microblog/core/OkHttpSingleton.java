package com.me.microblog.core;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
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

    /**
     * 该不会开启异步线程。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public Response execute(Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallback
     */
    public void enqueue(Request request, Callback responseCallback) {
        okHttpClient.newCall(request).enqueue(responseCallback);
    }
}
