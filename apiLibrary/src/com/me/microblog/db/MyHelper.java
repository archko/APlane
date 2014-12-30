package com.me.microblog.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.me.microblog.oauth.SOauth2;
import com.me.microblog.util.WeiboLog;

/**
 * User: archko Date: 12-8-21 Time: 下午1:33
 */
public class MyHelper extends SQLiteOpenHelper {

    public static MyHelper myHelper;
    private static final String TAG = "MyHelper";

    public static MyHelper getMyHelper(Context ctx) {
        if (myHelper == null) {
            myHelper = new MyHelper(ctx);
        }
        return myHelper;
    }

    MyHelper(Context context) {
        super(context, TwitterTable.DB_NAME, null, TwitterTable.VERSION);
        //super(context, databaseName, factory, v);
        WeiboLog.d(TAG, "MyHelper.databaseName:");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        WeiboLog.d(TAG, "MyHelper.onCreate:");
        updateDBToVer9(db);
        updateDBToVer10(db);
        updateDBToVer11(db);
        updateDBToVer12(db);
        updateDBToVer13(db);
        updateDBToVer14(db);
        updateDBToVer15(db);
        updateDBToVer16(db);
        updateDBToVer17(db);
        updateDBToVer18(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        WeiboLog.d(TAG, "MyHelper onUpgrade.oldVersion:" + oldVersion + " newVersion:" + newVersion);
        if (oldVersion < 4) {
            deleteDataBase(db);
            oldVersion = 4;
        }

        if (oldVersion < 9) {
            updateDBToVer9(db);
            oldVersion = 9;
        }

        if (oldVersion < 10) {
            updateDBToVer10(db);
            oldVersion = 10;
        }

        if (oldVersion < 11) {
            updateDBToVer11(db);
            oldVersion = 11;
        }

        if (oldVersion < 12) {
            updateDBToVer12(db);
            oldVersion = 12;
        }

        if (oldVersion < 13) {
            updateDBToVer13(db);
            oldVersion = 13;
        }

        if (oldVersion < 14) {
            updateDBToVer14(db);
            oldVersion = 14;
        }

        if (oldVersion < 15) {
            updateDBToVer15(db);
            oldVersion = 15;
        }

        if (oldVersion < 16) {
            updateDBToVer16(db);
            oldVersion = 16;
        }

        if (oldVersion < 17) {
            updateDBToVer17(db);
            oldVersion = 17;
        }

        if (oldVersion < 18) {
            updateDBToVer18(db);
            oldVersion = 18;
        }

        if (oldVersion != newVersion) {
            throw new IllegalStateException("error upgrading the database to version " + newVersion);
        }
    }

    void updateDBToVer9(SQLiteDatabase db) {
        try {
            db.execSQL(TwitterTable.AUTbl.CREATE_ACCOUNT);
            db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_TIME + " integer;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建at用户表，创建草稿表，创建新浪微博数据表，删除旧的微博数据表
     *
     * @param db
     */
    void updateDBToVer10(SQLiteDatabase db) {
        try {
            //创建at用户表
            db.execSQL(TwitterTable.UserTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.UserTbl.USER_SCREEN_NAME_INDEX);
            //db.execSQL(TwitterTable.UserTbl.IDX1);    //这里还没有拼音列

            //创建草稿表
            db.execSQL(TwitterTable.DraftTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.DraftTbl.DRAFT_ID_INDEX);
            db.execSQL(TwitterTable.DraftTbl.IDX1);

            //创建新浪微博数据表
            db.execSQL(TwitterTable.SStatusTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.SStatusTbl.SSTATUS_ID_INDEX);
            db.execSQL(TwitterTable.SStatusTbl.IDX1);

            //删除旧的微博数据表
            /*db.execSQL("drop index if exists "+TwitterTable.StatusTbl.STATUS_ID_INDEX);
            db.execSQL("DROP TABLE IF EXISTS "+TwitterTable.StatusTbl.TBNAME);*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加草稿，主页数据，@用户的关联当前登录用户的 id
     *
     * @param db
     */
    void updateDBToVer11(SQLiteDatabase db) {
        try {
            db.execSQL("update " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " set " + TwitterTable.AUTbl.ACCOUNT_TYPE + "=" + TwitterTable.AUTbl.WEIBO_SINA +
                " , " + TwitterTable.AUTbl.ACCOUNT_AS_DEFAULT + "=" + TwitterTable.AUTbl.ACCOUNT_IS_DEFAULT);
            db.execSQL("ALTER TABLE " + TwitterTable.SStatusTbl.TBNAME + " ADD " + TwitterTable.SStatusTbl.UID + " integer;");
            db.execSQL("ALTER TABLE " + TwitterTable.UserTbl.TBNAME + " ADD " + TwitterTable.UserTbl.UID + " integer;");
            db.execSQL("ALTER TABLE " + TwitterTable.DraftTbl.TBNAME + " ADD " + TwitterTable.DraftTbl.UID + " integer;");

            //数据库升级需要清除原来的数据，因为原来的数据没有这一列将是无效的数据。
            db.execSQL("delete FROM " + TwitterTable.SStatusTbl.TBNAME);
            db.execSQL("delete FROM " + TwitterTable.UserTbl.TBNAME);
            db.execSQL("delete FROM " + TwitterTable.DraftTbl.TBNAME);

            //创建队列表
            db.execSQL(TwitterTable.SendQueueTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.SendQueueTbl.SEND_QUEUE_ID_INDEX);
            db.execSQL(TwitterTable.SendQueueTbl.IDX1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加转发微博的id存储
     *
     * @param db
     */
    void updateDBToVer12(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TwitterTable.SStatusTbl.TBNAME + " ADD " + TwitterTable.SStatusTbl.R_STATUS_ID + " integer;");
            db.execSQL("ALTER TABLE " + TwitterTable.SStatusTbl.TBNAME + " ADD " + TwitterTable.SStatusTbl.R_STATUS_USERID + " integer;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加主页用户的拼音，类型（暂时用不到，默认值填写为0）
     *
     * @param db
     */
    void updateDBToVer13(SQLiteDatabase db) {
        try {
            db.execSQL("drop index if exists " + TwitterTable.UserTbl.USER_SCREEN_NAME_INDEX);
            db.execSQL("ALTER TABLE " + TwitterTable.UserTbl.TBNAME + " ADD " + TwitterTable.UserTbl.PINYIN + " text;");
            db.execSQL("ALTER TABLE " + TwitterTable.UserTbl.TBNAME + " ADD " + TwitterTable.UserTbl.TYPE + " integer;");
            db.execSQL("UPDATE " + TwitterTable.UserTbl.TBNAME + " SET " + TwitterTable.UserTbl.TYPE + " =0;");
            db.execSQL(TwitterTable.UserTbl.IDX1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加私信,还有要将@的用户类型修改.
     *
     * @param db
     */
    void updateDBToVer14(SQLiteDatabase db) {
        try {
            //创建私信表
            db.execSQL(TwitterTable.DirectMsgTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.DirectMsgTbl.DIRECT_MSG_ID_INDEX);
            db.execSQL("drop index if exists " + TwitterTable.DirectMsgTbl.DIRECT_MSG_TEXT_INDEX);
            db.execSQL(TwitterTable.DirectMsgTbl.IDX1);
            db.execSQL(TwitterTable.DirectMsgTbl.IDX2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加队列的发送结果类型与错误代码。
     *
     * @param db
     */
    void updateDBToVer15(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TwitterTable.SendQueueTbl.TBNAME + " ADD " + TwitterTable.SendQueueTbl.SEND_RESULT_MSG + " text;");
            db.execSQL("ALTER TABLE " + TwitterTable.SendQueueTbl.TBNAME + " ADD " + TwitterTable.SendQueueTbl.SEND_RESULT_CODE + " integer;");
            db.execSQL("UPDATE " + TwitterTable.SendQueueTbl.TBNAME + " SET " + TwitterTable.SendQueueTbl.SEND_RESULT_CODE + "=0;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加评论与@我的微博的表格,因为存储本地文件太麻烦了
     *
     * @param db
     */
    void updateDBToVer16(SQLiteDatabase db) {
        try {
            db.execSQL(TwitterTable.SStatusCommentTbl.CREATE_TABLE);
            db.execSQL("drop index if exists " + TwitterTable.SStatusCommentTbl.SSTATUS_COMMENT_ID_INDEX);
            db.execSQL(TwitterTable.SStatusCommentTbl.IDX1);
            db.execSQL("drop index if exists " + TwitterTable.SStatusCommentTbl.SSTATUS_COMMENT_UID_TYPE_INDEX);
            db.execSQL(TwitterTable.SStatusCommentTbl.IDX2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 升级微博数据库表,存储的数据需要保存多个图片
     * 评论的原微博也存储多个图片.如果有.
     *
     * @param db
     */
    void updateDBToVer17(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TwitterTable.SStatusTbl.TBNAME + " ADD " + TwitterTable.SStatusTbl.PIC_URLS + " text;");
        db.execSQL("ALTER TABLE " + TwitterTable.SStatusCommentTbl.TBNAME + " ADD " + TwitterTable.SStatusCommentTbl.PIC_URLS + " text;");
    }

    /**
     * 升级认证用户表,添加认证的类型,网页认证与密码认证.添加了自定义的key与secret
     *
     * @param db
     */
    void updateDBToVer18(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_OAUTH_TYPE + " integer;");
        db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_CUSTOM_KEY + " text;");
        db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_CUSTOM_SECRET + " text;");
        db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_CALLBACK_URL + " text;");
        db.execSQL("ALTER TABLE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " ADD " + TwitterTable.AUTbl.ACCOUNT_AUTHENTICATION_URL + " text;");

        db.execSQL("UPDATE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " SET " + TwitterTable.AUTbl.ACCOUNT_OAUTH_TYPE + "=0;");    //is a bug.serviceprovider sina=1?
        db.execSQL("UPDATE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " SET " + TwitterTable.AUTbl.ACCOUNT_CUSTOM_KEY + "='" + SOauth2.CONSUMER_KEY + "';");
        db.execSQL("UPDATE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " SET " + TwitterTable.AUTbl.ACCOUNT_CALLBACK_URL + "='" + SOauth2.CALLBACK_URL + "';");
        db.execSQL("UPDATE " + TwitterTable.AUTbl.ACCOUNT_TBNAME + " SET " + TwitterTable.AUTbl.ACCOUNT_AUTHENTICATION_URL + "='" + SOauth2.AUTHENTICATIONURL + "';");
    }

    private void deleteDataBase(SQLiteDatabase db) throws SQLException {
        //db.execSQL("DROP TABLE IF EXISTS "+TwitterTable.StatusTbl.TBNAME);
        db.execSQL("DROP TABLE IF EXISTS " + TwitterTable.SStatusTbl.TBNAME);
        //db.execSQL("DROP TRIGGER IF EXISTS rssurl_cleanup");
        db.execSQL("DROP TABLE IF EXISTS " + TwitterTable.UserTbl.TBNAME);
        db.execSQL("DROP TABLE IF EXISTS " + TwitterTable.AccountTbl.ACCOUNT_TBNAME);
    }
}
