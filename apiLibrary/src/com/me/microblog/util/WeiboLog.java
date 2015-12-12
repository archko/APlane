package com.me.microblog.util;

import android.text.TextUtils;

/**
 * @author archko
 */
public class WeiboLog {

    public static final String TAG="WeiboLog";
    private static boolean LOG2FILE=false;

    private static int logLevel=5;

    /**
     * Change current logging level
     *
     * @param level new log level 1 <= level <= 6
     */
    public static void setLogLevel(int level) {
        logLevel=level;
    }

    /**
     * Get the current log level
     *
     * @return the log level
     */
    public static int getLogLevel() {
        return logLevel;
    }

    public boolean isDebug() {
        return logLevel>=4;
    }

    public static void d(String msg) {
        /*if (DEBUG) {
            Log.d(TAG, msg);
        }*/
        if (logLevel>=4) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        /*if (DEBUG) {
            Log.d(tag, msg);
        }*/
        if (logLevel>=4) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void i(String msg) {
        /*if (INFO) {
            Log.i(TAG, msg);
        }*/
        if (logLevel>=3) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        /*if (INFO) {
            Log.i(tag, msg);
        }*/
        if (logLevel>=3) {
            android.util.Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        /*if (INFO) {
            Log.i(tag, msg, tr);
        }*/
        if (logLevel>=3) {
            android.util.Log.i(tag, msg, tr);
        }
    }

    public static void v(String msg) {
        /*if (VERBOSE) {
            Log.v(TAG, msg);
        }*/
        if (logLevel>=5) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        /*if (VERBOSE) {
            Log.v(tag, msg);
        }*/
        if (logLevel>=5) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void e(String msg) {
        //Log.e(TAG, msg);
        if (logLevel>=1) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        //Log.e(tag, msg);
        if (logLevel>=1) {
            android.util.Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        //Log.e(tag, msg, tr);
        if (logLevel>=1) {
            android.util.Log.e(tag, msg, tr);
        }
    }

    public static void w(String msg) {
        //Log.w(TAG, msg);
        if (logLevel>=2) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        //Log.w(tag, msg);
        if (logLevel>=2) {
            android.util.Log.w(tag, msg);
        }
    }

    public static boolean isDEBUG() {
        return getLogLevel()>=4;
    }

    public static void printResult(String tempData) {
        printResult("printResult", tempData);
    }

    public static void printResult(String TAG, String tempData) {
        if (!WeiboLog.isDEBUG()) {
            return;
        }
        if (TextUtils.isEmpty(tempData)) {
            android.util.Log.d(TAG, "result is null.");
            return;
        }
        final int len=tempData.length();
        final int div=1000;
        int count=len/div;
        if (count>0) {
            for (int i=0; i<count; i++) {
                android.util.Log.d(TAG, "result:"+tempData.substring(i*div, (i+1)*div));
            }
            int mode=len%div;
            if (mode>0) {
                android.util.Log.d(TAG, "result:"+tempData.substring(div*count, len));
            }
        } else {
            android.util.Log.d(TAG, "result:"+tempData);
        }
    }

}
