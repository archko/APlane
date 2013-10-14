package cn.archko.microblog.fragment.impl;

import android.content.ContentResolver;
import android.content.Intent;
import android.text.TextUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.User;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.User;
import com.me.microblog.core.SinaUserApi;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:10
 * @description: 我的评论数据获取实现。评论有多种，微博的评论列表，我发出的，收到的等。
 */
public class SinaUserImpl extends AbsStatusImpl<User> {

    public static final String TAG="SinaUserImpl";

    public SinaUserImpl() {
        mAbsApi=new SinaUserApi();
    }

    @Override
    public SStatusData<User> loadData(Object... params) throws WeiboException {
        Integer type=(Integer) params[1];
        if (type==0) {
            Intent intent=(Intent) params[0];
            String nickName=intent.getStringExtra("nickName");

            WeiboLog.d(TAG, "nickName:"+nickName);
            if (!TextUtils.isEmpty(nickName)) {
                return loadUserByName(nickName);
            } else {
                return loadUserById(intent);
            }
        } else {    //获取自己的信息.
            Long userId=(Long) params[2];
            return getMySelf(userId);
        }
    }

    /**
     * 根据用户id加载用户信息
     *
     * @param intent
     */
    private SStatusData<User> loadUserById(Intent intent) {
        long userid=intent.getLongExtra("user_id", -1);
        SStatusData<User> sStatusData=new SStatusData<User>();
        SinaUserApi sinaUserApi=(SinaUserApi) mAbsApi;
        if (-1!=userid) {
            try {
                User mUser=sinaUserApi.getUser(userid);
                WeiboLog.d(TAG, "要查询的userid为:"+userid+" ..."+mUser);
                sStatusData.mData=mUser;
                sStatusData.errorCode=0;
            } catch (Exception e) {
                sStatusData.errorCode=500;
                sStatusData.errorMsg=e.toString();
            }
        } else {
            String screeName=intent.getStringExtra("scree_name");
            if (null!=screeName&&!"".equals(screeName)) {
                try {
                    User mUser=sinaUserApi.getUser(screeName);
                    WeiboLog.d(TAG, "要查询的scree_name为:"+screeName+" ..."+mUser);
                    sStatusData.errorCode=0;
                    sStatusData.mData=mUser;
                } catch (Exception e) {
                    sStatusData.errorCode=500;
                    sStatusData.errorMsg=e.toString();
                }
            } else {
                WeiboLog.i(TAG, "screeName is null.");
            }
        }

        return sStatusData;
    }

    /**
     * 根据用户昵称加载用户信息
     *
     * @param nickName 昵称
     */
    private SStatusData<User> loadUserByName(String nickName) {
        SStatusData<User> sStatusData=new SStatusData<User>();
        try {
            SinaUserApi sinaUserApi=(SinaUserApi) mAbsApi;
            if (nickName.indexOf("：")!=-1) {
                nickName=nickName.substring(0, nickName.indexOf("："));
            }
            if (nickName.startsWith("@")) {
                nickName=nickName.substring(1);
            }
            nickName.trim();
            User mUser=sinaUserApi.getUser(nickName);
            WeiboLog.d(TAG, "要查询的nickName为:"+nickName+" ..."+mUser);
            sStatusData.errorCode=0;
            sStatusData.mData=mUser;
        } catch (Exception e) {
            sStatusData.errorCode=500;
            sStatusData.errorMsg=e.toString();
        }

        return sStatusData;
    }

    private SStatusData<User> getMySelf(Long userId) {
        SStatusData<User> sStatusData=new SStatusData<User>();
        try {
            User user=null;
            SinaUserApi sinaUserApi=(SinaUserApi) mAbsApi;
            WeiboLog.v(TAG, "getMySelf.");
            if (userId==-1) {
                user=sinaUserApi.getMyself();
                userId=user.id;
            }

            user=sinaUserApi.getUser(userId);
            sStatusData.mData=user;
        } catch (Exception e) {
            e.printStackTrace();
            sStatusData.errorMsg=e.toString();
        }
        return sStatusData;
    }

    @Override
    public Object[] queryData(Object... params) throws WeiboException {
        Long currentUserId=(Long) params[1];
        ContentResolver resolver=App.getAppContext().getContentResolver();
        //ArrayList<User> datas=SqliteWrapper.queryAtUsers(resolver, currentUserId, TwitterTable.SStatusUserTbl.TYPE_User);
        SStatusData<User> sStatusData=new SStatusData<User>();
        //sStatusData.mStatusData=datas;
        return new Object[]{sStatusData, params};
    }

    @Override
    public void saveData(SStatusData<User> statusData) {    //TODO 还需要修改，不是每个用户都存储
        /*try {   //这里是针对登录用户的
            User user=statusData.mData;
            if (null==user) {
                return;
            }
            String filename=App.getAppContext().getFilesDir().getAbsolutePath()+"/"+String.valueOf(user.id)+Constants.USER_SELF_FILE;
            FileOutputStream fos=null;
            ObjectOutputStream out=null;
            try {
                fos=new FileOutputStream(filename);
                out=new ObjectOutputStream(fos);
                out.writeObject(user);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
