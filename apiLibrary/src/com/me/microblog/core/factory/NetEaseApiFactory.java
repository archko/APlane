package com.me.microblog.core.factory;

import com.me.microblog.core.SinaCommentApi;
import com.me.microblog.core.SinaDMApi;
import com.me.microblog.core.SinaPlaceApi;
import com.me.microblog.core.SinaStatusApi;
import com.me.microblog.core.SinaUserApi;
import com.me.microblog.core.abs.ICommentApi;
import com.me.microblog.core.abs.IDMApi;
import com.me.microblog.core.abs.IPlaceApi;
import com.me.microblog.core.abs.IStatusApi;
import com.me.microblog.core.abs.IUserApi;

/**
 * @description: 网易api工厂
 * @author: archko 13-10-18 :下午6:35
 */
public class NetEaseApiFactory implements AbsApiFactory {

    @Override
    public IStatusApi statusApiFactory() {
        return new SinaStatusApi();
    }

    @Override
    public ICommentApi commentApiFactory() {
        return new SinaCommentApi();
    }

    @Override
    public IUserApi userApiFactory() {
        return new SinaUserApi();
    }

    @Override
    public IDMApi dmApiFactory() {
        return new SinaDMApi();
    }

    @Override
    public IPlaceApi placeApiFactory() {
        return new SinaPlaceApi();
    }
}
