package com.me.microblog.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author archko
 */
public class WeiboLog {

    public static final String TAG = "WeiboLog";
    public static boolean DEBUG = true;
    public static boolean INFO = true;
    private static boolean LOG2FILE = false;
    private static final String LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.microblog/microblog.log";

    private static int logLevel = 5;

    /**
     * Change current logging level
     *
     * @param level new log level 1 <= level <= 6
     */
    public static void setLogLevel(int level) {
        logLevel = level;
    }

    /**
     * Get the current log level
     *
     * @return the log level
     */
    public static int getLogLevel() {
        return logLevel;
    }

    public static void d(String msg) {
        /*if (DEBUG) {
            Log.d(TAG, msg);
        }*/
        if (logLevel >= 4) {
            android.util.Log.d(TAG, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void d(String tag, String msg) {
        /*if (DEBUG) {
            Log.d(tag, msg);
        }*/
        if (logLevel >= 4) {
            android.util.Log.d(tag, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void i(String msg) {
        /*if (INFO) {
            Log.i(TAG, msg);
        }*/
        if (logLevel >= 3) {
            android.util.Log.i(TAG, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void i(String tag, String msg) {
        /*if (INFO) {
            Log.i(tag, msg);
        }*/
        if (logLevel >= 3) {
            android.util.Log.i(tag, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        /*if (INFO) {
            Log.i(tag, msg, tr);
        }*/
        if (logLevel >= 3) {
            android.util.Log.i(tag, msg, tr);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void v(String msg) {
        /*if (VERBOSE) {
            Log.v(TAG, msg);
        }*/
        if (logLevel >= 5) {
            android.util.Log.v(TAG, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void v(String tag, String msg) {
        /*if (VERBOSE) {
            Log.v(tag, msg);
        }*/
        if (logLevel >= 5) {
            android.util.Log.v(tag, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void e(String msg) {
        //Log.e(TAG, msg);
        if (logLevel >= 1) {
            android.util.Log.e(TAG, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void e(String tag, String msg) {
        //Log.e(tag, msg);
        if (logLevel >= 1) {
            android.util.Log.e(tag, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        //Log.e(tag, msg, tr);
        if (logLevel >= 1) {
            android.util.Log.e(tag, msg, tr);
        }
        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void w(String msg) {
        //Log.w(TAG, msg);
        if (logLevel >= 2) {
            android.util.Log.w(TAG, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static void w(String tag, String msg) {
        //Log.w(tag, msg);
        if (logLevel >= 2) {
            android.util.Log.w(tag, msg);
        }

        if (LOG2FILE) {
            FileHelper.WriteStringToFile(msg + "\r\n", LOG_PATH, true);
        }
    }

    public static boolean isDEBUG() {
        return DEBUG;
    }

    public static void setDEBUG(boolean debug) {
        DEBUG = debug;
    }

    public static boolean isLOG2FILE() {
        return LOG2FILE;
    }

    public static void setLOG2FILE(boolean log2File) {
        LOG2FILE = log2File;
    }

    /**
     * 此方法能输出异常详细信息
     * 用于bug定位
     */
    public static String getStackTrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer, true));

        return writer.toString();
    }

    public static void printResult(String tempData) {
        printResult("printResult", tempData);
    }

    public static void printResult(String TAG, String tempData) {
        if (TextUtils.isEmpty(tempData)) {
            WeiboLog.d(TAG, "result is null.");
            return;
        }
        if (logLevel >= 4) {
            final int len = tempData.length();
            final int div = 1000;
            int count = len / div;
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    WeiboLog.d(TAG, "result:" + tempData.substring(i * div, (i + 1) * div));
                }
                int mode = len % div;
                if (mode > 0) {
                    WeiboLog.d(TAG, "result:" + tempData.substring(div * count, len));
                }
            } else {
                WeiboLog.d(TAG, "result:" + tempData);
            }
        }
    }

}
