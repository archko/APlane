package com.me.microblog.oauth;

/**
 * @author archko
 */
public interface Oauth1 {

    public String getAccessToken();

    public String getSecret();

    public void setAccessToken(String token);

    public void setSecret(String secret);

    public boolean oauth();
}
