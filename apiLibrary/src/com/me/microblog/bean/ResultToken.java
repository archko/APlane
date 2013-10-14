package com.me.microblog.bean;

/**
 * @version 1.00.00
 * @description: 最后的Token，如果是oauth1的有两个值，如果是oauth2的也有两个值，但一个是过期时间
 * @author: archko 12-6-17
 */
public class ResultToken {

    public String token;
    /**
     * oauth1有，oauth2没有
     */
    public String secret;
    /**
     * 过期时间 oauth1没有，oauth2有
     */
    public String expires_in;
}
