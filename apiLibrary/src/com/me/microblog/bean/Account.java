package com.me.microblog.bean;

/**
 * 用户帐户
 *
 * @author archko
 */
public class Account {

    public int id;
    public String accountName;
    public String accountToken;
    public String accountSecret;
    public int accountStatus;   //帐户状态:0表示正常,-1表示错误.
    public int accountS;        //默认帐户状态:0表示是默认帐户,-1表示非默认帐户.
    public long userId;

    @Override
    public String toString() {
        return "Account{" + "accountName=" + accountName + ", accountToken=" + accountToken
            + ", accountSecret=" + accountSecret + ", accountStatus=" + accountStatus + ", accountS=" + accountS + '}';
    }

}
