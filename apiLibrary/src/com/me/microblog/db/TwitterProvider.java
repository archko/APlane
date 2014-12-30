package com.me.microblog.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 微博的数据提供者
 */
public class TwitterProvider extends ContentProvider {

    private static final String TAG = "TwitterProvider";
    MyHelper dbHelper;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "ss", TwitterTable.SStatusTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "ss/#", TwitterTable.SStatusTbl.ITEM_ID);

        uriMatcher.addURI(TwitterTable.AUTHORITY, "user", TwitterTable.UserTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "user/#", TwitterTable.UserTbl.ITEM_ID);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "ac", TwitterTable.AccountTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "ac/#", TwitterTable.AccountTbl.ITEM_ID);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "au", TwitterTable.AUTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "au/#", TwitterTable.AUTbl.ITEM_ID);

        uriMatcher.addURI(TwitterTable.AUTHORITY, "dt", TwitterTable.DraftTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "dt/#", TwitterTable.DraftTbl.ITEM_ID);

        uriMatcher.addURI(TwitterTable.AUTHORITY, "sq", TwitterTable.SendQueueTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "sq/#", TwitterTable.SendQueueTbl.ITEM_ID);

        uriMatcher.addURI(TwitterTable.AUTHORITY, "dm", TwitterTable.DirectMsgTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "dm/#", TwitterTable.DirectMsgTbl.ITEM_ID);

        uriMatcher.addURI(TwitterTable.AUTHORITY, "ssc", TwitterTable.SStatusCommentTbl.ITEM);
        uriMatcher.addURI(TwitterTable.AUTHORITY, "ssc/#", TwitterTable.SStatusCommentTbl.ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = MyHelper.getMyHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TwitterTable.SStatusTbl.ITEM:
                return TwitterTable.SStatusTbl.STATUS_TYPE;

            case TwitterTable.SStatusTbl.ITEM_ID:
                return TwitterTable.SStatusTbl.STATUS_ITEM_TYPE;

            case TwitterTable.UserTbl.ITEM:
                return TwitterTable.UserTbl.USER_TYPE;

            case TwitterTable.UserTbl.ITEM_ID:
                return TwitterTable.UserTbl.USER_ITEM_TYPE;

            case TwitterTable.AccountTbl.ITEM:
                return TwitterTable.AccountTbl.USER_TYPE;

            case TwitterTable.AccountTbl.ITEM_ID:
                return TwitterTable.AccountTbl.USER_ITEM_TYPE;

            case TwitterTable.AUTbl.ITEM:
                return TwitterTable.AUTbl.USER_TYPE;

            case TwitterTable.AUTbl.ITEM_ID:
                return TwitterTable.AUTbl.USER_ITEM_TYPE;

            case TwitterTable.DraftTbl.ITEM:
                return TwitterTable.DraftTbl.DRAFT_TYPE;

            case TwitterTable.DraftTbl.ITEM_ID:
                return TwitterTable.DraftTbl.DRAFT_ITEM_TYPE;

            case TwitterTable.SendQueueTbl.ITEM:
                return TwitterTable.SendQueueTbl.SEND_QUEUE_TYPE;

            case TwitterTable.SendQueueTbl.ITEM_ID:
                return TwitterTable.SendQueueTbl.SEND_QUEUE_ITEM_TYPE;

            case TwitterTable.DirectMsgTbl.ITEM:
                return TwitterTable.DirectMsgTbl.DIRECT_MSG_TYPE;

            case TwitterTable.DirectMsgTbl.ITEM_ID:
                return TwitterTable.DirectMsgTbl.DIRECT_MSG_ITEM_TYPE;

            case TwitterTable.SStatusCommentTbl.ITEM:
                return TwitterTable.SStatusCommentTbl.STATUS_COMMENT_TYPE;

            case TwitterTable.SStatusCommentTbl.ITEM_ID:
                return TwitterTable.SStatusCommentTbl.STATUS_COMMENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknow uri:" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //WeiboLog.d(TAG, "query.uri:" + uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        String id;
        switch (uriMatcher.match(uri)) {
            case TwitterTable.SStatusTbl.ITEM:
                cursor = db.query(TwitterTable.SStatusTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.SStatusTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.SStatusTbl.TBNAME, projection,
                    TwitterTable.SStatusTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.UserTbl.ITEM:
                cursor = db.query(TwitterTable.UserTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.UserTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.UserTbl.TBNAME, projection,
                    TwitterTable.UserTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.AccountTbl.ITEM:
                cursor = db.query(TwitterTable.AccountTbl.ACCOUNT_TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.AccountTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.AccountTbl.ACCOUNT_TBNAME, projection,
                    TwitterTable.AccountTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.AUTbl.ITEM:
                cursor = db.query(TwitterTable.AUTbl.ACCOUNT_TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.AUTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.AUTbl.ACCOUNT_TBNAME, projection,
                    TwitterTable.AUTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.DraftTbl.ITEM:
                cursor = db.query(TwitterTable.DraftTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.DraftTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.DraftTbl.TBNAME, projection,
                    TwitterTable.DraftTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.SendQueueTbl.ITEM:
                cursor = db.query(TwitterTable.SendQueueTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.SendQueueTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.SendQueueTbl.TBNAME, projection,
                    TwitterTable.SendQueueTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.DirectMsgTbl.ITEM:
                cursor = db.query(TwitterTable.DirectMsgTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.DirectMsgTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.DirectMsgTbl.TBNAME, projection,
                    TwitterTable.DirectMsgTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM:
                cursor = db.query(TwitterTable.SStatusCommentTbl.TBNAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(TwitterTable.SStatusCommentTbl.TBNAME, projection,
                    TwitterTable.SStatusCommentTbl._ID + "=" + id + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        //WeiboLog.d(TAG, "insert.uri:" + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId;
        Uri newUri = null;
        int m = uriMatcher.match(uri);
        switch (m) {
            case TwitterTable.SStatusTbl.ITEM:
                rowId = db.insert(TwitterTable.SStatusTbl.TBNAME, TwitterTable.SStatusTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.SStatusTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.UserTbl.ITEM:
                rowId = db.insert(TwitterTable.UserTbl.TBNAME, TwitterTable.UserTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.UserTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.AccountTbl.ITEM:
                rowId = db.insert(TwitterTable.AccountTbl.ACCOUNT_TBNAME, TwitterTable.AccountTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.AccountTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.AUTbl.ITEM:
                rowId = db.insert(TwitterTable.AUTbl.ACCOUNT_TBNAME, TwitterTable.AUTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.AUTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.DraftTbl.ITEM:
                rowId = db.insert(TwitterTable.DraftTbl.TBNAME, TwitterTable.DraftTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.DraftTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.SendQueueTbl.ITEM:
                rowId = db.insert(TwitterTable.SendQueueTbl.TBNAME, TwitterTable.SendQueueTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.SendQueueTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.DirectMsgTbl.ITEM:
                rowId = db.insert(TwitterTable.DirectMsgTbl.TBNAME, TwitterTable.DirectMsgTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.DirectMsgTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            case TwitterTable.SStatusCommentTbl.ITEM:
                rowId = db.insert(TwitterTable.SStatusCommentTbl.TBNAME, TwitterTable.SStatusCommentTbl._ID, cv);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TwitterTable.SStatusCommentTbl.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //WeiboLog.d(TAG, "delete.uri:" + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String id;
        switch (uriMatcher.match(uri)) {
            case TwitterTable.SStatusTbl.ITEM:
                count = db.delete(TwitterTable.SStatusTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.SStatusTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.SStatusTbl.TBNAME,
                    TwitterTable.SStatusTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.UserTbl.ITEM:
                count = db.delete(TwitterTable.UserTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.UserTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.UserTbl.TBNAME,
                    TwitterTable.UserTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.AccountTbl.ITEM:
                count = db.delete(TwitterTable.AccountTbl.ACCOUNT_TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.AccountTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.AccountTbl.ACCOUNT_TBNAME,
                    TwitterTable.AccountTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.AUTbl.ITEM:
                count = db.delete(TwitterTable.AUTbl.ACCOUNT_TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.AUTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.AUTbl.ACCOUNT_TBNAME,
                    TwitterTable.AUTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.DraftTbl.ITEM:
                count = db.delete(TwitterTable.DraftTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.DraftTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.DraftTbl.TBNAME,
                    TwitterTable.DraftTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.SendQueueTbl.ITEM:
                count = db.delete(TwitterTable.SendQueueTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.SendQueueTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.SendQueueTbl.TBNAME,
                    TwitterTable.SendQueueTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.DirectMsgTbl.ITEM:
                count = db.delete(TwitterTable.DirectMsgTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.DirectMsgTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.DirectMsgTbl.TBNAME,
                    TwitterTable.DirectMsgTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM:
                count = db.delete(TwitterTable.SStatusCommentTbl.TBNAME, selection, selectionArgs);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(TwitterTable.SStatusCommentTbl.TBNAME,
                    TwitterTable.SStatusCommentTbl._ID + "=" + id
                        + (! TextUtils.isEmpty(selection) ? "and (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues cv, String selection, String[] selectionArg) {
        //WeiboLog.d(TAG, "update.uri:" + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case TwitterTable.SStatusTbl.ITEM:
                count = db.update(TwitterTable.SStatusTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.SStatusTbl.ITEM_ID:
                count = db.update(TwitterTable.SStatusTbl.TBNAME, cv,
                    TwitterTable.SStatusTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.UserTbl.ITEM:
                count = db.update(TwitterTable.UserTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.UserTbl.ITEM_ID:
                count = db.update(TwitterTable.UserTbl.TBNAME, cv,
                    TwitterTable.UserTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.AccountTbl.ITEM:
                count = db.update(TwitterTable.AccountTbl.ACCOUNT_TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.AccountTbl.ITEM_ID:
                count = db.update(TwitterTable.AccountTbl.ACCOUNT_TBNAME, cv,
                    TwitterTable.AccountTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.AUTbl.ITEM:
                count = db.update(TwitterTable.AUTbl.ACCOUNT_TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.AUTbl.ITEM_ID:
                count = db.update(TwitterTable.AUTbl.ACCOUNT_TBNAME, cv,
                    TwitterTable.AUTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.DraftTbl.ITEM:
                count = db.update(TwitterTable.DraftTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.DraftTbl.ITEM_ID:
                count = db.update(TwitterTable.DraftTbl.TBNAME, cv,
                    TwitterTable.DraftTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.SendQueueTbl.ITEM:
                count = db.update(TwitterTable.SendQueueTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.SendQueueTbl.ITEM_ID:
                count = db.update(TwitterTable.SendQueueTbl.TBNAME, cv,
                    TwitterTable.SendQueueTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.DirectMsgTbl.ITEM:
                count = db.update(TwitterTable.DirectMsgTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.DirectMsgTbl.ITEM_ID:
                count = db.update(TwitterTable.DirectMsgTbl.TBNAME, cv,
                    TwitterTable.DirectMsgTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM:
                count = db.update(TwitterTable.SStatusCommentTbl.TBNAME, cv, selection, selectionArg);
                break;

            case TwitterTable.SStatusCommentTbl.ITEM_ID:
                count = db.update(TwitterTable.SStatusCommentTbl.TBNAME, cv,
                    TwitterTable.SStatusCommentTbl._ID + "=" + uri.getPathSegments().get(1)
                        + (! TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""),
                    selectionArg);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public void closeDB() {
        dbHelper.close();
    }
}
