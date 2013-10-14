package com.me.microblog.oauth;

public class OauthBean {

    public String accessToken="";
    public long expireTime=0L;
    public String openId="";    //qq有的，新浪中用uid
    public String openKey="";    //qq独有的。
    public String refreshToken="";    //网易独有的
    public long time;   //实际到期时间

    //下面是表格twitter_au对应的数据。
    public long id; //主键
    public String userId;   //新浪中用uid
    public String name;
    public String pass;
    public int type;//什么类型的，对应下面的五个，WEIBO_SINA...`
    public int isDefault;
    public int oauthType;//网页认证还是密码认证.
    public String customKey;
    public String customSecret;

    @Override
    public String toString() {
        return "OauthBean{"+
            "accessToken='"+accessToken+'\''+
            ", expireTime="+expireTime+
            ", openId='"+openId+'\''+
            ", openKey='"+openKey+'\''+
            ", refreshToken='"+refreshToken+'\''+
            ", time="+time+
            ", userId='"+userId+'\''+
            ", name='"+name+'\''+
            ", type="+type+
            ", isDefault="+isDefault+
            ", oauthType="+oauthType+
            '}';
    }
}
