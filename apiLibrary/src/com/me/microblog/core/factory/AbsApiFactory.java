package com.me.microblog.core.factory;

import com.me.microblog.core.abs.ICommentApi;
import com.me.microblog.core.abs.IDMApi;
import com.me.microblog.core.abs.IPlaceApi;
import com.me.microblog.core.abs.IStatusApi;
import com.me.microblog.core.abs.IUserApi;

/**
 * @description: 抽象工厂
 * @author: archko 13-10-18 :下午6:33
 */
public interface AbsApiFactory {

    /**
     * 微博api工厂
     *
     * @return
     */
    IStatusApi statusApiFactory();

    /**
     * 评价api工厂
     *
     * @return
     */
    ICommentApi commentApiFactory();

    /**
     * 用户api工厂
     *
     * @return
     */
    IUserApi userApiFactory();

    /**
     * 私信api工厂
     *
     * @return
     */
    IDMApi dmApiFactory();

    /**
     * 位置api工厂
     *
     * @return
     */
    IPlaceApi placeApiFactory();
}
