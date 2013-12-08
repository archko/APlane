package com.me.microblog.oauth;

import java.io.Serializable;

/**
 * 认证的结果,包含了运行时需要用的key,与数据库对应的数据.
 *
 * @author archko
 */
public class OauthBean implements Serializable{

    /**
     * 认证后的token
     */
    public String accessToken="";
    /**
     * 认证过期的时间戳,在认证过程中,获取到的值,需要经过转换为毫秒,然后保存于数据库中.
     */
    public long expireTime=0L;
    public String openId="";    //qq有的，新浪中用uid
    public String openKey="";    //qq独有的。
    public String refreshToken="";    //网易独有的
    public long time;   //实际到期时间,最终是与expireTime一样的.

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

    ServiceProvider serviceProvider;

    public OauthBean() {
        serviceProvider=ServiceProvider.Sina;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider=serviceProvider;
    }

    public void setServiceProvider(int spNo) {
        serviceProvider=ServiceProvider.getServiceProvider(spNo);
    }

    @Override
    public String toString() {
        return "OauthBean{"+
            "accessToken='"+accessToken+'\''+
            ", expireTime="+expireTime+
            ", openId='"+openId+'\''+
            ", openKey='"+openKey+'\''+
            ", refreshToken='"+refreshToken+'\''+
            ", time="+time+
            ", id="+id+
            ", userId='"+userId+'\''+
            ", name='"+name+'\''+
            ", pass='"+pass+'\''+
            ", type="+type+
            ", isDefault="+isDefault+
            ", oauthType="+oauthType+
            ", customKey='"+customKey+'\''+
            ", customSecret='"+customSecret+'\''+
            ", serviceProvider="+serviceProvider+
            '}';
    }
}
