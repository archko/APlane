package com.me.microblog.util;

import java.util.ArrayList;
import java.util.Date;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.me.microblog.App;
import com.me.microblog.bean.AtUser;
import com.me.microblog.bean.Comment;
import com.me.microblog.bean.DirectMessage;
import com.me.microblog.bean.Draft;
import com.me.microblog.bean.SendTask;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.db.TwitterTable;
import com.me.microblog.oauth.OauthBean;

/**
 * 数据库处理帮助类
 *
 * @author archko
 */
public final class SqliteWrapper {

    private static final String TAG="SqliteWrapper";

    private SqliteWrapper() {
        // Forbidden being instantiated.
    }

    /**
     * 查询数据库中的微博数据.
     *
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public static ArrayList<Status> queryStatuses(ContentResolver resolver, long currentUserId) {
        WeiboLog.d(TAG, TAG+" queryStatuses."+resolver);
        ArrayList<Status> list=null;

        Cursor cursor=null;
        try {
            cursor=resolver.query(TwitterTable.SStatusTbl.CONTENT_URI, null, TwitterTable.SStatusTbl.UID+"='"+currentUserId+"'",
                null, TwitterTable.SStatusTbl.CREATED_AT+" desc");
            if (null==cursor||cursor.getCount()<1) {
                WeiboLog.w(TAG, "查询数据为空.");
                return null;
            }

            list=new ArrayList<Status>(36);
            Status status;
            Status r_status=null;
            User user;
            cursor.moveToFirst();
            do {
                long _id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl._ID));
                long sid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl.STATUS_ID));
                long user_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl.USER_ID));
                String screen_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.USER_SCREEN_NAME));
                String portrait=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PORTRAIT));
                long created_at=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl.CREATED_AT));
                String text=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.TEXT));
                String source=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.SOURCE));
                String pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PIC_THUMB));
                String pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PIC_MID));
                String pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PIC_ORIG));
                int r_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_NUM));
                int c_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusTbl.C_NUM));

                String r_status_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_STATUS_NAME));
                String r_status_content=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_STATUS));
                if (!TextUtils.isEmpty(r_status_name)&&!TextUtils.isEmpty(r_status_content)) {
                    r_status=new Status();
                    r_status.user=new User(r_status_name);
                    r_status.text=r_status_content;

                    long r_status_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_STATUS_ID));
                    long r_status_userid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_STATUS_USERID));
                    String r_pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_PIC_THUMB));
                    String r_pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_PIC_MID));
                    String r_pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.R_PIC_ORIG));
                    r_status.id=r_status_id;
                    r_status.user.id=r_status_userid;
                    r_status.thumbnailPic=r_pic_thumb;
                    r_status.bmiddlePic=r_pic_mid;
                    r_status.originalPic=r_pic_orig;
                } else {
                    r_status=null;
                }
                user=new User(screen_name);
                user.id=user_id;
                user.profileImageUrl=portrait;
                status=new Status(_id, sid, new Date(created_at), text, source, pic_thumb, pic_mid, pic_orig, r_status, user, r_num, c_num);

                String pic_urls=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PIC_URLS));
                if (!TextUtils.isEmpty(pic_urls)) {
                    String[] picUrls=pic_urls.split(",");
                    status.thumbs=picUrls;
                }

                list.add(status);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            WeiboLog.e(TAG, "查询出错:"+e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * 查询@的用户。废除,因为用户已经不只一种类型
     *
     * @param context
     * @param currentUserId 当前登录用户的id，不同的用户有不同的@数据。
     * @return
     */
    @Deprecated
    public static ArrayList<String> queryAtNames(Context context, long currentUserId) {
        Cursor cursor=null;
        ArrayList<String> usernames=new ArrayList<String>();
        try {
            //cursor=db.rawQuery(sql, null);
            cursor=context.getContentResolver().query(TwitterTable.UserTbl.CONTENT_URI, null,
                TwitterTable.UserTbl.UID+"='"+currentUserId+"'", null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    String displayName=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.USER_SCREEN_NAME));
                    usernames.add(displayName);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return usernames;
    }

    //--------------------- 帐户查询操作 ---------------------

    /**
     * 查询帐户，当前登录的默认帐户与高级帐户查询，类型与是否是默认的状态一定要有，userId可以无。
     * 通常只有在第一次全程时才没有userId参数。
     *
     * @param context
     * @param account_type 类型，有区分新浪，网易等，还有新浪高级key
     * @param au_default   是否是默认的帐户，如果是高级key就不是。
     * @param userId       用户的id，如果为-1，表示只查询当前的帐户类型与默认的状态
     * @return
     */
    public static OauthBean queryAccount(Context context, int account_type, int au_default, long userId) {
        Cursor cursor=null;
        try {
            StringBuilder sb=new StringBuilder();
            sb.append(TwitterTable.AUTbl.ACCOUNT_TYPE).append("=").append(account_type);
            if (au_default!=0) {    //当不是0时，表示不过滤默认帐户类型。
                sb.append(" and ").append(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT).append("=").append(au_default);
            }
            if (userId>-1) {
                sb.append(" and ").append(TwitterTable.AUTbl.ACCOUNT_USERID).append("=").append(userId);
            }

            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null, sb.toString(), null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID));
                String name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_NAME));
                String pass=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_PASS));

                String accessToken=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TOKEN));
                long time=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TIME));
                String openId=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_USERID));
                int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TYPE));
                int isDefault=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT));

                OauthBean bean=new OauthBean();
                bean.id=id;
                bean.accessToken=accessToken;
                bean.expireTime=time;
                bean.time=time;
                bean.openId=bean.userId=openId;
                bean.name=name;
                bean.pass=pass;
                bean.type=type;
                bean.isDefault=isDefault;
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return null;
    }

    @SuppressWarnings("temp")
    public static OauthBean queryAccount(Context context, int account_type, String uname) {
        Cursor cursor=null;
        try {
            StringBuilder sb=new StringBuilder();
            sb.append(TwitterTable.AUTbl.ACCOUNT_TYPE).append("=").append(account_type);
            String username=RC4.RunRC4(uname, App.KEY);
            sb.append(" and ").append(TwitterTable.AUTbl.ACCOUNT_NAME).append("='").append(username).append("'");

            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null, sb.toString(), null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID));
                //String name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_NAME));
                String pass=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_PASS));

                String accessToken=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TOKEN));
                long time=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TIME));
                String openId=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_USERID));
                int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TYPE));
                int isDefault=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT));

                OauthBean bean=new OauthBean();
                bean.id=id;
                bean.accessToken=accessToken;
                bean.expireTime=time;
                bean.time=time;
                bean.openId=bean.userId=openId;
                bean.name=uname;
                bean.pass=pass;
                bean.type=type;
                bean.isDefault=isDefault;
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * 查询帐户，当前登录的默认帐户与高级帐户查询，主要用于在帐户切换时查询，不能查询出桌面key的帐户。
     * 名字是解码后的。
     *
     * @param context
     * @param account_type 类型，有区分新浪，网易等，还有新浪高级key
     * @return
     */
    public static ArrayList<OauthBean> queryAccounts(Context context, int account_type) {
        ArrayList<OauthBean> oauthBeanList=new ArrayList<OauthBean>();
        OauthBean tmp;
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null,
                TwitterTable.AUTbl.ACCOUNT_TYPE+"='"+account_type+"'", null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID));
                    String name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_NAME));
                    String pass=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_PASS));

                    String accessToken=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TOKEN));
                    long time=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TIME));
                    String openId=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_USERID));
                    int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TYPE));
                    int isDefault=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT));

                    tmp=new OauthBean();
                    tmp.id=id;
                    tmp.accessToken=accessToken;
                    tmp.expireTime=time;
                    tmp.time=time;
                    tmp.openId=tmp.userId=openId;
                    if (!TextUtils.isEmpty(name)) {
                        tmp.name=RC4.RunRC4(name, App.KEY);
                    }
                    tmp.pass=pass;
                    tmp.type=type;
                    tmp.isDefault=isDefault;
                    oauthBeanList.add(tmp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return oauthBeanList;
    }

    /**
     * 查询所有的用户
     *
     * @param context
     * @return
     */
    public static ArrayList<OauthBean> queryAllAccount(Context context) {
        ArrayList<OauthBean> oauthBeanList=new ArrayList<OauthBean>();
        OauthBean tmp;
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.AUTbl.CONTENT_URI, null, null, null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl._ID));
                    String name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_NAME));
                    String pass=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_PASS));

                    String accessToken=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TOKEN));
                    long time=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TIME));
                    String openId=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_USERID));
                    int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_TYPE));
                    int isDefault=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT));

                    tmp=new OauthBean();
                    tmp.id=id;
                    tmp.accessToken=accessToken;
                    tmp.expireTime=time;
                    tmp.time=time;
                    tmp.openId=tmp.userId=openId;
                    tmp.name=RC4.RunRC4(name, App.KEY);
                    tmp.pass=pass;
                    tmp.type=type;
                    tmp.isDefault=isDefault;
                    oauthBeanList.add(tmp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return oauthBeanList;
    }

    /**
     * 删除帐户
     *
     * @param context
     * @param account_type 类型，有区分新浪，网易等，还有新浪高级key
     * @param au_default   是否是默认的帐户，如果是高级key就不是。
     * @param userId       用户的id，如果为-1，表示只查询当前的帐户类型与默认的状态
     * @return
     */
    public static int deleteAccount(Context context, int account_type, int au_default, long userId) {
        try {
            ContentResolver resolver=context.getContentResolver();
            StringBuilder sb=new StringBuilder();
            sb.append(TwitterTable.AUTbl.ACCOUNT_TYPE).append("=").append(account_type);
            sb.append(" and ").append(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT).append("=").append(au_default);
            if (userId>-1) {
                sb.append(" and ").append(TwitterTable.AUTbl.ACCOUNT_USERID).append("=").append(userId);
            }
            return resolver.delete(TwitterTable.AUTbl.CONTENT_URI, sb.toString(), null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 删除帐户
     *
     * @param context
     * @param id      帐户主键
     * @return
     */
    public static int deleteAccount(Context context, String userId) {
        try {
            ContentResolver resolver=context.getContentResolver();
            return resolver.delete(TwitterTable.AUTbl.CONTENT_URI, TwitterTable.AUTbl.ACCOUNT_USERID+" ='"+userId+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 添加帐户
     *
     * @param context
     * @param oauthBean 认证结果
     * @param username  用户名
     * @param password  密码
     * @return
     */
    public static Uri addAccount(Context context, OauthBean bean, String username, String password,
        int type, String sType) {
        try {
            bean.time=bean.expireTime*1000+System.currentTimeMillis()-100l; //re caculate expireTime
            ContentResolver resolver=context.getContentResolver();
            ContentValues cv=new ContentValues();
            if (!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)) {
                cv.put(TwitterTable.AUTbl.ACCOUNT_NAME, RC4.RunRC4(username, App.KEY));
                cv.put(TwitterTable.AUTbl.ACCOUNT_PASS, RC4.RunRC4(password, App.KEY));
            }
            cv.put(TwitterTable.AUTbl.ACCOUNT_TOKEN, bean.accessToken);
            cv.put(TwitterTable.AUTbl.ACCOUNT_TIME, bean.time);
            //cv.put(TwitterTable.AUTbl.ACCOUNT_TYPE, TwitterTable.AUTbl.WEIBO_SINA);
            cv.put(TwitterTable.AUTbl.ACCOUNT_TYPE, type);
            cv.put(TwitterTable.AUTbl.ACCOUNT_USERID, bean.openId);
            //cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, -1);
            cv.put(TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT, sType);

            return resolver.insert(TwitterTable.AUTbl.CONTENT_URI, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return null;
    }

    /**
     * 查询所有的用户
     *
     * @param context
     * @return
     */
    public static ArrayList<Draft> queryAllDraft(Context context, String currentUserId) {
        ArrayList<Draft> draftList=new ArrayList<Draft>();
        Draft tmp;
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.DraftTbl.CONTENT_URI, null,
                TwitterTable.DraftTbl.UID+"="+currentUserId, null, null);
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl._ID));
                    long userId=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.USER_ID));
                    String content=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.CONTENT));
                    String imgUrl=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.IMG_URL));
                    long createdAt=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.CREATED_AT));
                    String text=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.TEXT));
                    String source=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.SOURCE));
                    String data=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.DATA));
                    long uid=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.UID));
                    int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.DraftTbl.TYPE));

                    tmp=new Draft();
                    tmp.id=id;
                    tmp.userId=userId;
                    tmp.content=content;
                    tmp.imgUrl=imgUrl;
                    tmp.createdAt=createdAt;
                    tmp.text=text;
                    tmp.source=source;
                    tmp.data=data;
                    tmp.uid=uid;
                    tmp.type=type;
                    draftList.add(tmp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return draftList;
    }

    /**
     * 删除草稿
     *
     * @param context
     * @param draft
     * @return
     */
    public static int deleteDraft(Context context, Draft draft) {
        try {
            ContentResolver resolver=context.getContentResolver();
            return resolver.delete(Uri.withAppendedPath(TwitterTable.DraftTbl.CONTENT_URI, String.valueOf(draft.id)), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 查询所有的未执行的任务
     *
     * @param context
     * @return
     */
    public static ArrayList<SendTask> queryAllTasks(Context context, String currentUserId, int rc) {
        ArrayList<SendTask> sendTasks=new ArrayList<SendTask>();
        SendTask tmp;
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();
            String where=TwitterTable.SendQueueTbl.USER_ID+"="+currentUserId;
            if (rc>-1) {
                where=where+" and "+TwitterTable.SendQueueTbl.SEND_RESULT_CODE+"="+rc;
            }
            //WeiboLog.v(TAG, "sql:"+where);
            cursor=resolver.query(TwitterTable.SendQueueTbl.CONTENT_URI, null,
                where, null, TwitterTable.SendQueueTbl.CREATED_AT+" desc");
            if (null!=cursor&&cursor.getCount()>0) {
                cursor.moveToFirst();

                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl._ID));
                    long userId=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.USER_ID));
                    String content=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.CONTENT));
                    String imgUrl=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.IMG_URL));
                    long createdAt=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.CREATED_AT));
                    String text=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.TEXT));
                    String source=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.SOURCE));
                    String data=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.DATA));
                    //long uid=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.UID));
                    int sendType=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.TYPE));
                    int resultCode=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.SEND_RESULT_CODE));
                    String resultMsg=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.SendQueueTbl.SEND_RESULT_MSG));

                    tmp=new SendTask();
                    tmp.id=id;
                    tmp.userId=userId;
                    tmp.content=content;
                    tmp.imgUrl=imgUrl;
                    tmp.createAt=createdAt;
                    tmp.text=text;
                    tmp.source=source;
                    tmp.data=data;
                    //tmp.uid=uid;
                    tmp.type=sendType;
                    tmp.resultCode=resultCode;
                    tmp.resultMsg=resultMsg;
                    sendTasks.add(tmp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return sendTasks;
    }

    /**
     * 更新发送任务，通常是发送失败的。
     *
     * @param context
     * @param code    返回的错误代码
     * @param msg     返回的错误信息
     * @param task    任务
     * @return
     */
    public static int updateSendTask(Context context, int code, String msg, SendTask task) {
        try {
            ContentResolver resolver=context.getContentResolver();
            ContentValues cv=new ContentValues();
            cv.put(TwitterTable.SendQueueTbl.SEND_RESULT_MSG, msg);
            cv.put(TwitterTable.SendQueueTbl.SEND_RESULT_CODE, code);
            return resolver.update(TwitterTable.SendQueueTbl.CONTENT_URI, cv, " _id='"+task.id+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 删除队列任务
     *
     * @param context
     * @param task    任务
     * @return
     */
    public static int deleteSendTask(Context context, SendTask task) {
        try {
            ContentResolver resolver=context.getContentResolver();
            return resolver.delete(TwitterTable.SendQueueTbl.CONTENT_URI, " _id='"+task.id+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 删除所有队列任务
     *
     * @param context
     * @param currentUserId 登录用户的id
     * @return
     */
    public static int deleteAllSendTask(Context context, long currentUserId) {
        try {
            ContentResolver resolver=context.getContentResolver();
            return resolver.delete(TwitterTable.SendQueueTbl.CONTENT_URI,
                TwitterTable.SendQueueTbl.USER_ID+"='"+currentUserId+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    //--------------------- @用户,有插入新的用户信息,有查询用户操作 ---------------------

    /**
     * 保存用户,用于@
     *
     * @param context
     * @param atUser        保存的用户实体,只有用户名与id两个属性.拼音根据用户名而获取的.
     * @param currentUserId 当前登录的用户
     * @param type          类型.具体参考TwitterTable.UserTbl
     */
    public static void saveAtUser(Context context, AtUser user, long currentUserId, int type) {
        Cursor cursor=null;
        ContentValues cv;
        String name;
        ContentResolver resolver=context.getContentResolver();
        try {
            cursor=resolver.query(TwitterTable.UserTbl.CONTENT_URI, null,
                TwitterTable.UserTbl.USER_ID+"="+user.userId+" and "+TwitterTable.UserTbl.UID+"="+currentUserId, null, null);

            if (null==cursor||cursor.getCount()<1) {
                cv=new ContentValues();
                cv.put(TwitterTable.UserTbl.USER_ID, user.userId);
                cv.put(TwitterTable.UserTbl.USER_SCREEN_NAME, user.name);
                cv.put(TwitterTable.UserTbl.UID, currentUserId);
                cv.put(TwitterTable.UserTbl.PINYIN, user.pinyin);
                cv.put(TwitterTable.UserTbl.TYPE, type);

                resolver.insert(TwitterTable.UserTbl.CONTENT_URI, cv);
            } else {
                cursor.moveToFirst();
                name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.USER_SCREEN_NAME));
                if (!name.equals(user.name)) {
                    cv=new ContentValues();
                    cv.put(TwitterTable.UserTbl.USER_SCREEN_NAME, user.name);
                    cv.put(TwitterTable.UserTbl.PINYIN, user.pinyin);
                    cv.put(TwitterTable.UserTbl.UID, currentUserId);
                    cv.put(TwitterTable.UserTbl.TYPE, type);

                    int count=resolver.update(Uri.withAppendedPath(TwitterTable.UserTbl.CONTENT_URI, String.valueOf(user.userId)), cv, null, null);
                    WeiboLog.d(TAG, "更新的用户："+user+" count:"+count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }
    }

    /**
     * 插入主页的微博列表中的用户信息，如果不存在。
     *
     * @param context
     * @param list          微博列表
     * @param currentUserId 当前登录用户的id
     * @param type          类型,0表示主页的关注用户.1为选定
     */
    public static void saveFriendUser(Context context, ArrayList<Status> list, long currentUserId, int type) {
        ArrayList<AtUser> atUsers=new ArrayList<AtUser>();
        User tmp;
        AtUser atUser;
        for (Status status : list) {
            tmp=status.user;
            atUser=new AtUser();
            atUser.userId=tmp.id;
            atUser.name=tmp.screenName;
            if (!atUsers.contains(atUser)) {
                atUsers.add(atUser);
            }
        }
        WeiboLog.d(TAG, "待处理的用户数为："+atUsers.size());

        if (atUsers.size()>0) {
            saveFriendUsers(context, atUsers, currentUserId, type);
        }
    }

    /**
     * 插入主页的微博列表中的用户信息，如果不存在。
     *
     * @param context
     * @param list          用户列表
     * @param currentUserId 当前登录用户的id
     * @param type          类型,0表示主页的关注用户.1为选定
     */
    public static void saveFriendUsers(Context context, ArrayList<AtUser> atUsers, long currentUserId, int type) {
        ArrayList<AtUser> newUsers=new ArrayList<AtUser>();
        Cursor cursor=null;
        ContentValues cv;
        String name;
        ContentResolver resolver=context.getContentResolver();
        for (AtUser user : atUsers) {
            try {
                cursor=resolver.query(TwitterTable.UserTbl.CONTENT_URI, null,
                    TwitterTable.UserTbl.USER_ID+"="+user.userId+
                        " and "+TwitterTable.UserTbl.UID+"="+currentUserId+
                        " and "+TwitterTable.UserTbl.TYPE+"="+type, null, null);

                if (null==cursor||cursor.getCount()<1) {
                    newUsers.add(user);
                } else {
                    cursor.moveToFirst();
                    name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.USER_SCREEN_NAME));
                    if (!name.equals(user.name)) {
                        cv=new ContentValues();
                        cv.put(TwitterTable.UserTbl.USER_SCREEN_NAME, user.name);
                        String pinyin=PinYin.getPinYin(user.name);
                        cv.put(TwitterTable.UserTbl.PINYIN, pinyin);
                        //cv.put(TwitterTable.UserTbl.UID, currentUserId);

                        int count=resolver.update(Uri.withAppendedPath(TwitterTable.UserTbl.CONTENT_URI, String.valueOf(user.userId)), cv, null, null);
                        WeiboLog.d(TAG, "更新的用户："+user+" count:"+count);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null!=cursor) {
                    cursor.close();
                }
            }
        }

        int len=newUsers.size();
        WeiboLog.d(TAG, "需要新插入的用户："+len);
        int i=0;

        if (len>0) {
            ContentValues[] contentValueses=new ContentValues[len];
            for (AtUser user : newUsers) {
                cv=new ContentValues();
                cv.put(TwitterTable.UserTbl.USER_ID, user.userId);
                cv.put(TwitterTable.UserTbl.USER_SCREEN_NAME, user.name);
                cv.put(TwitterTable.UserTbl.UID, currentUserId);
                cv.put(TwitterTable.UserTbl.PINYIN, user.pinyin);
                cv.put(TwitterTable.UserTbl.TYPE, type);
                contentValueses[i++]=cv;
            }
            len=resolver.bulkInsert(TwitterTable.UserTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存用户记录:"+len);
        }
    }

    /**
     * 查询用户，用于@，可查询最近查看的用户，或是登录用户关注的用户。或是最近@的。
     * 关键词可以是汉字，也可以是拼音。
     *
     * @param context
     * @param currentUserId 当前登录用户的id
     * @param type          用户的 类型，可能是@,可能是关注的。
     * @param where         查询的模糊匹配条件。
     * @return AtUser用户列表。
     */
    public static ArrayList<AtUser> queryAtUsers(Context context, long currentUserId, int type,
        String where) {
        ArrayList<AtUser> atUsers=new ArrayList<AtUser>();
        AtUser tmp;
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();

            StringBuilder sb=new StringBuilder();
            sb.append(TwitterTable.UserTbl.UID).append("=").append(currentUserId);
            sb.append(" and ").append(TwitterTable.UserTbl.TYPE).append("=").append(type);
            if (!TextUtils.isEmpty(where)) {
                sb.append(" and (").append(TwitterTable.UserTbl.USER_SCREEN_NAME).append(" like '%").append(where).append("%'");
                sb.append(" or ").append(TwitterTable.UserTbl.PINYIN).append(" like '%").append(where).append("%')");
            }

            WeiboLog.d(TAG, "at user.:"+sb.toString());
            cursor=resolver.query(TwitterTable.UserTbl.CONTENT_URI, null, sb.toString(), null, null);
            WeiboLog.d(TAG, "cursor:"+cursor);
            if (null!=cursor&&cursor.getCount()>0) {
                WeiboLog.d(TAG, "cursor:"+cursor.getCount());
                cursor.moveToFirst();

                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl._ID));
                    long userId=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.USER_ID));
                    String name=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.USER_SCREEN_NAME));
                    String pinyin=cursor.getString(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.PINYIN));
                    //int type=cursor.getInt(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.TYPE));
                    long uid=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.UserTbl.UID));

                    tmp=new AtUser();
                    tmp.id=id;
                    tmp.userId=userId;
                    tmp.name=name;
                    tmp.pinyin=pinyin;
                    tmp.type=type;
                    tmp.uid=uid;
                    atUsers.add(tmp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }

        return atUsers;
    }

    //--------------------- @与评论操作 ---------------------

    /**
     * 查询数据库中的@me微博数据.
     *
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public static ArrayList<Status> queryAtStatuses(ContentResolver resolver, long currentUserId) {
        WeiboLog.d(TAG, TAG+" queryAtStatuses.");
        ArrayList<Status> list=null;

        Cursor cursor=null;
        try {
            cursor=resolver.query(TwitterTable.SStatusCommentTbl.CONTENT_URI, null,
                TwitterTable.SStatusCommentTbl.UID+"='"+currentUserId+"' and "+
                    TwitterTable.SStatusCommentTbl.TYPE+"="+TwitterTable.SStatusCommentTbl.TYPE_STATUT,
                null, TwitterTable.SStatusCommentTbl.CREATED_AT+" desc");
            if (null==cursor||cursor.getCount()<1) {
                WeiboLog.w(TAG, "查询数据为空.");
                return null;
            }

            list=new ArrayList<Status>(36);
            Status status;
            Status r_status=null;
            User user;
            cursor.moveToFirst();
            do {
                long _id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl._ID));
                long sid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.STATUS_ID));
                long user_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.USER_ID));
                String screen_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.USER_SCREEN_NAME));
                String portrait=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PORTRAIT));
                long created_at=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.CREATED_AT));
                String text=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.TEXT));
                String source=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.SOURCE));
                String pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_THUMB));
                String pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_MID));
                String pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_ORIG));
                int r_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_NUM));
                int c_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.C_NUM));

                String r_status_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_NAME));
                String r_status_content=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS));
                if (!TextUtils.isEmpty(r_status_name)&&!TextUtils.isEmpty(r_status_content)) {
                    r_status=new Status();
                    r_status.user=new User(r_status_name);
                    r_status.text=r_status_content;

                    long r_status_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_ID));
                    long r_status_userid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_USERID));
                    String r_pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_THUMB));
                    String r_pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_MID));
                    String r_pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_ORIG));
                    r_status.id=r_status_id;
                    r_status.user.id=r_status_userid;
                    r_status.thumbnailPic=r_pic_thumb;
                    r_status.bmiddlePic=r_pic_mid;
                    r_status.originalPic=r_pic_orig;
                } else {
                    r_status=null;
                }
                user=new User(screen_name);
                user.id=user_id;
                user.profileImageUrl=portrait;
                status=new Status(_id, sid, new Date(created_at), text, source, pic_thumb, pic_mid, pic_orig, r_status, user, r_num, c_num);

                String pic_urls=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusTbl.PIC_URLS));
                if (!TextUtils.isEmpty(pic_urls)) {
                    String[] picUrls=pic_urls.split(",");
                    status.thumbs=picUrls;
                }

                list.add(status);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            WeiboLog.e(TAG, "查询出错:"+e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * 插入@我的微博, 与主面一样,放在Fragment中
     *
     * @param context
     * @param list          微博列表
     * @param currentUserId 当前登录用户的id
     * @param type          类型,0表示主页的关注用户.1为选定
     */
    @Deprecated
    public static void saveAtStatus(Context context, ArrayList<Status> list, long currentUserId) {
        Cursor cursor=null;
        ContentResolver resolver=context.getContentResolver();
        ArrayList<Status> dataList=new ArrayList<Status>();
        for (Status status : list) {
            try {
                StringBuilder sb=new StringBuilder();
                sb.append(TwitterTable.SStatusCommentTbl.UID).append("='").append(currentUserId).append("'");
                sb.append(" and ").append(TwitterTable.SStatusCommentTbl.TYPE).append("=").append(TwitterTable.SStatusCommentTbl.TYPE_STATUT);
                sb.append(" and ").append(TwitterTable.SStatusCommentTbl.STATUS_ID).append("='").append(status.id).append("'");

                cursor=resolver.query(TwitterTable.SStatusCommentTbl.CONTENT_URI, null,
                    sb.toString(), null, null);
                if (null!=cursor&&cursor.getCount()>0) {
                } else {
                    dataList.add(status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null!=cursor) {
                    cursor.close();
                }
            }
        }

        int len=dataList.size();
        if (len>0) {
            ContentValues[] contentValueses=new ContentValues[len];
            ContentValues cv;
            Status status;
            Status retStatus;
            for (int i=len-1; i>=0; i--) {
                status=dataList.get(i);
                cv=new ContentValues();
                cv.put(TwitterTable.SStatusTbl.STATUS_ID, status.id);
                cv.put(TwitterTable.SStatusTbl.CREATED_AT, status.createdAt.getTime());
                cv.put(TwitterTable.SStatusTbl.TEXT, status.text);
                cv.put(TwitterTable.SStatusTbl.SOURCE, status.source);
                if (!TextUtils.isEmpty(status.thumbnailPic)) {
                    cv.put(TwitterTable.SStatusTbl.PIC_THUMB, status.thumbnailPic);
                    cv.put(TwitterTable.SStatusTbl.PIC_MID, status.bmiddlePic);
                    cv.put(TwitterTable.SStatusTbl.PIC_ORIG, status.originalPic);
                }
                cv.put(TwitterTable.SStatusTbl.R_NUM, status.r_num);
                cv.put(TwitterTable.SStatusTbl.C_NUM, status.c_num);
                cv.put(TwitterTable.SStatusTbl.UID, currentUserId);
                cv.put(TwitterTable.SStatusCommentTbl.TYPE, TwitterTable.SStatusCommentTbl.TYPE_STATUT);

                retStatus=status.retweetedStatus;
                if (retStatus!=null) {
                    cv.put(TwitterTable.SStatusTbl.R_STATUS_ID, retStatus.id);
                    User user=retStatus.user;
                    if (null!=user) {
                        try {
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_USERID, retStatus.user.id);
                            cv.put(TwitterTable.SStatusTbl.R_STATUS_NAME, retStatus.user.screenName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cv.put(TwitterTable.SStatusTbl.R_STATUS, retStatus.text);
                    if (!TextUtils.isEmpty(retStatus.thumbnailPic)) {
                        cv.put(TwitterTable.SStatusTbl.R_PIC_THUMB, retStatus.thumbnailPic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_MID, retStatus.bmiddlePic);
                        cv.put(TwitterTable.SStatusTbl.R_PIC_ORIG, retStatus.originalPic);
                    }
                }
                cv.put(TwitterTable.SStatusTbl.USER_ID, status.user.id);
                cv.put(TwitterTable.SStatusTbl.USER_SCREEN_NAME, status.user.screenName);
                cv.put(TwitterTable.SStatusTbl.PORTRAIT, status.user.profileImageUrl);

                contentValueses[i]=cv;
            }

            len=resolver.bulkInsert(TwitterTable.SStatusTbl.CONTENT_URI, contentValueses);
            WeiboLog.d(TAG, "insert at status."+len);
        }
    }

    /**
     * 查询数据库中的@me评论数据.
     *
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public static ArrayList<Comment> queryAtComments(ContentResolver resolver, long currentUserId, int type) {
        WeiboLog.d(TAG, TAG+" queryAtComments.");
        ArrayList<Comment> list=null;

        Cursor cursor=null;
        try {
            cursor=resolver.query(TwitterTable.SStatusCommentTbl.CONTENT_URI, null,
                TwitterTable.SStatusCommentTbl.UID+"='"+currentUserId+"' and "+
                    TwitterTable.SStatusCommentTbl.TYPE+"="+type,
                null, TwitterTable.SStatusCommentTbl.CREATED_AT+" desc");
            if (null==cursor||cursor.getCount()<1) {
                WeiboLog.w(TAG, "查询数据为空.");
                return null;
            }

            list=new ArrayList<Comment>(36);
            Comment comment;
            Comment replyComment;
            Status status=null;
            User user;
            cursor.moveToFirst();
            do {
                //long _id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl._ID));
                long sid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.STATUS_ID));
                long user_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.USER_ID));
                String screen_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.USER_SCREEN_NAME));
                String portrait=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PORTRAIT));
                long created_at=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.CREATED_AT));
                String text=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.TEXT));
                String source=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.SOURCE));

                user=new User(screen_name);
                user.id=user_id;
                user.profileImageUrl=portrait;
                comment=new Comment();
                comment.id=sid;
                comment.createdAt=new Date(created_at);
                comment.text=text;
                comment.source=source;
                comment.user=user;

                long r_status_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_ID));
                if (r_status_id>0) {
                    status=new Status();

                    String pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_THUMB));
                    String pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_MID));
                    String pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_ORIG));
                    int r_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_NUM));
                    int c_num=cursor.getInt(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.C_NUM));

                    String r_status_content=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS));
                    status.text=r_status_content;
                    status.id=r_status_id;
                    status.thumbnailPic=pic_thumb;
                    status.bmiddlePic=pic_mid;
                    status.originalPic=pic_orig;
                    status.r_num=r_num;
                    status.c_num=c_num;

                    long r_status_userid=cursor.getLong(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_USERID));
                    if (r_status_userid>0) {
                        String r_status_name=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_STATUS_NAME));

                        status.user=new User(r_status_name);
                        status.user.id=r_status_userid;
                    }

                    String pic_urls=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.PIC_URLS));
                    if (!TextUtils.isEmpty(pic_urls)) {
                        String[] picUrls=pic_urls.split(",");
                        status.thumbs=picUrls;
                    }

                    comment.status=status;
                }

                String r_pic_thumb=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_THUMB));
                if (!TextUtils.isEmpty(r_pic_thumb)) {    //replyComment
                    try {
                        String r_pic_mid=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_MID));
                        String r_pic_orig=cursor.getString(cursor.getColumnIndex(TwitterTable.SStatusCommentTbl.R_PIC_ORIG));
                        replyComment=new Comment();
                        comment.replyComment=replyComment;
                        replyComment.text=r_pic_thumb;
                        user=new User(r_pic_orig);
                        user.id=Long.valueOf(r_pic_mid);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                list.add(comment);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            WeiboLog.e(TAG, "查询出错:"+e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }

        return list;
    }

    //--------------------- 私信操作 ---------------------

    /**
     * 查询数据库中的私信数据.根据接收者与发送者查询
     *
     * @param senderId   发送者id
     * @param receiverId 接收者id
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public static ArrayList<DirectMessage> queryDirectMsgsByRecipient(ContentResolver resolver,
        long senderId, long receiverId) {
        WeiboLog.d(TAG, TAG+" queryDirectMsgsByRecipient.");
        ArrayList<DirectMessage> list=null;

        Cursor cursor=null;
        try {
            cursor=resolver.query(TwitterTable.DirectMsgTbl.CONTENT_URI, null, TwitterTable.DirectMsgTbl.SENDER_ID+"='"+senderId+"'"+
                " and "+TwitterTable.DirectMsgTbl.RECIPIENT_ID+"='"+receiverId+"'",
                null, TwitterTable.DirectMsgTbl.CREATED_AT+" desc");
            if (null==cursor||cursor.getCount()<1) {
                WeiboLog.w(TAG, "查询数据为空.");
                return null;
            }

            list=prepareDirectMsgs(cursor);
        } catch (Exception e) {
            WeiboLog.e(TAG, "查询出错:"+e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * 查询数据库中的私信数据.不分接收与发送,查询当前的登录用户相关的私信
     *
     * @param currentUserId 登录用户的id
     * @return
     * @throws com.me.microblog.WeiboException
     *
     */
    public static ArrayList<DirectMessage> queryDirectMsgs(ContentResolver resolver, long currentUserId) {
        WeiboLog.d(TAG, TAG+" queryDirectMsgsByRecipient.");
        ArrayList<DirectMessage> list=null;

        Cursor cursor=null;
        try {
            cursor=resolver.query(TwitterTable.DirectMsgTbl.CONTENT_URI, null,
                TwitterTable.DirectMsgTbl.UID+"='"+currentUserId+"'",
                null, TwitterTable.DirectMsgTbl.CREATED_AT+" desc");
            if (null==cursor||cursor.getCount()<1) {
                WeiboLog.w(TAG, "查询数据为空.");
                return null;
            }

            list=prepareDirectMsgs(cursor);
        } catch (Exception e) {
            WeiboLog.e(TAG, "查询出错:"+e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * 组装查询结果.
     *
     * @param cursor
     * @return
     */
    static ArrayList<DirectMessage> prepareDirectMsgs(Cursor cursor) {
        ArrayList<DirectMessage> list=new ArrayList<DirectMessage>(36);
        DirectMessage directMessage;
        User recipient;
        User sender;
        cursor.moveToFirst();
        do {
            long _id=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl._ID));
            long dmId=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.DM_ID));
            String idstr=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.IDSTR));
            long recipient_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.RECIPIENT_ID));
            String recipient_screenname=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.RECIPIENT_SCREENNAME));
            String recipient_profile_url=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.RECIPIENT_PROFILE_URL));
            long created_at=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.CREATED_AT));
            long sender_id=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.SENDER_ID));
            String sender_screenname=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.SENDER_SCREENNAME));
            String sender_profile_url=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.SENDER_PROFILE_URL));
            String text=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.TEXT));
            String source=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.SOURCE));
            String data=cursor.getString(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.DATA));
            long uid=cursor.getLong(cursor.getColumnIndex(TwitterTable.DirectMsgTbl.UID));

            recipient=new User(recipient_screenname);
            recipient.id=recipient_id;
            recipient.profileImageUrl=recipient_profile_url;

            sender=new User(sender_screenname);
            sender.id=sender_id;
            sender.profileImageUrl=sender_profile_url;

            directMessage=new DirectMessage();
            directMessage.id=dmId;
            directMessage._id=_id;
            directMessage.idstr=idstr;
            directMessage.recipient=recipient;
            directMessage.sender=sender;
            directMessage.createdAt=new Date(created_at);
            directMessage.text=text;
            directMessage.source=source;
            directMessage.data=data;
            directMessage.uid=uid;

            list.add(directMessage);
        } while (cursor.moveToNext());

        return list;
    }

    /**
     * 删除私信
     *
     * @param context
     * @param directMessage 私信
     * @return
     */
    public static int deleteDirectMsg(Context context, DirectMessage directMessage) {
        return deleteDirectMsg(context, directMessage.id);
    }

    /**
     * 删除私信
     *
     * @param context
     * @param dmId    私信id
     * @return
     */
    public static int deleteDirectMsg(Context context, long dmId) {
        try {
            ContentResolver resolver=context.getContentResolver();
            return resolver.delete(TwitterTable.DirectMsgTbl.CONTENT_URI, TwitterTable.DirectMsgTbl.DM_ID+"='"+dmId+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return 0;
    }

    /**
     * 保存新的私信,根据下载的新内容存储,对于发送的也可以保存.
     *
     * @param context
     * @param list          私信列表
     * @param currentUserId 当前登录用户的id
     */
    public static void saveDirectMsgs(Context context, ArrayList<DirectMessage> list, long currentUserId) {
        ArrayList<DirectMessage> newDirectMsgs=new ArrayList<DirectMessage>();

        Cursor cursor=null;
        ContentResolver resolver=context.getContentResolver();
        for (DirectMessage message : list) {
            try {
                cursor=resolver.query(TwitterTable.DirectMsgTbl.CONTENT_URI, null,
                    TwitterTable.DirectMsgTbl.DM_ID+"="+message.id+
                        " and "+TwitterTable.DirectMsgTbl.UID+"="+currentUserId, null, null);

                if (null==cursor||cursor.getCount()<1) {
                    newDirectMsgs.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null!=cursor) {
                    cursor.close();
                }
            }
        }

        int len=newDirectMsgs.size();
        WeiboLog.d(TAG, "需要新插入的私信："+len);
        int i=0;

        if (len>0) {
            ContentValues[] contentValueses=new ContentValues[len];
            ContentValues cv;
            for (DirectMessage message : newDirectMsgs) {
                cv=new ContentValues();
                cv.put(TwitterTable.DirectMsgTbl.DM_ID, message.id);
                cv.put(TwitterTable.DirectMsgTbl.IDSTR, message.idstr);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_ID, message.recipientId);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_SCREENNAME, message.recipientScreenName);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_PROFILE_URL, message.recipient.profileImageUrl);
                cv.put(TwitterTable.DirectMsgTbl.CREATED_AT, message.createdAt.getTime());
                cv.put(TwitterTable.DirectMsgTbl.SENDER_ID, message.senderId);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_SCREENNAME, message.senderScreenName);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_PROFILE_URL, message.sender.profileImageUrl);
                cv.put(TwitterTable.DirectMsgTbl.TEXT, message.text);
                cv.put(TwitterTable.DirectMsgTbl.SOURCE, message.source);
                cv.put(TwitterTable.DirectMsgTbl.UID, currentUserId);
                contentValueses[i++]=cv;
            }
            len=resolver.bulkInsert(TwitterTable.DirectMsgTbl.CONTENT_URI, contentValueses);
            WeiboLog.i(TAG, "保存用户记录:"+len);
        }
    }

    /**
     * 保存新的私信,根据下载的新内容存储,对于发送的也可以保存.
     *
     * @param context
     * @param message       私信
     * @param currentUserId 当前登录用户的id
     */
    public static void saveDirectMsg(Context context, DirectMessage message, long currentUserId) {
        Cursor cursor=null;
        try {
            ContentResolver resolver=context.getContentResolver();
            cursor=resolver.query(TwitterTable.DirectMsgTbl.CONTENT_URI, null,
                TwitterTable.DirectMsgTbl.DM_ID+"="+message.id+
                    " and "+TwitterTable.DirectMsgTbl.UID+"="+currentUserId, null, null);

            if (null==cursor||cursor.getCount()<1) {
                ContentValues cv=new ContentValues();
                cv.put(TwitterTable.DirectMsgTbl.DM_ID, message.id);
                cv.put(TwitterTable.DirectMsgTbl.IDSTR, message.idstr);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_ID, message.recipientId);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_SCREENNAME, message.recipientScreenName);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_PROFILE_URL, message.recipient.profileImageUrl);
                cv.put(TwitterTable.DirectMsgTbl.CREATED_AT, message.createdAt.getTime());
                cv.put(TwitterTable.DirectMsgTbl.SENDER_ID, message.senderId);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_SCREENNAME, message.senderScreenName);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_PROFILE_URL, message.sender.profileImageUrl);
                cv.put(TwitterTable.DirectMsgTbl.TEXT, message.text);
                cv.put(TwitterTable.DirectMsgTbl.SOURCE, message.source);
                cv.put(TwitterTable.DirectMsgTbl.UID, currentUserId);
                Uri uri=resolver.insert(TwitterTable.DirectMsgTbl.CONTENT_URI, cv);
                WeiboLog.d(TAG, "保存一条私信:"+uri);
            } else {
                cursor.moveToFirst();
                long _id=cursor.getLong(cursor.getColumnIndexOrThrow(TwitterTable.DirectMsgTbl._ID));
                ContentValues cv=new ContentValues();
                cv.put(TwitterTable.DirectMsgTbl.DM_ID, message.id);
                cv.put(TwitterTable.DirectMsgTbl.IDSTR, message.idstr);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_SCREENNAME, message.recipientScreenName);
                cv.put(TwitterTable.DirectMsgTbl.RECIPIENT_PROFILE_URL, message.recipient.profileImageUrl);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_SCREENNAME, message.senderScreenName);
                cv.put(TwitterTable.DirectMsgTbl.SENDER_PROFILE_URL, message.sender.profileImageUrl);
                int res=resolver.update(Uri.withAppendedPath(TwitterTable.DirectMsgTbl.CONTENT_URI, String.valueOf(_id)), cv, null, null);
                WeiboLog.i(TAG, "新的私信已经存在了."+res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!=cursor) {
                cursor.close();
            }
        }
    }
}
