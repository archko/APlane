package com.me.microblog.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: archko Date: 12-8-21 Time: 下午4:14
 */
public class DateUtils {
    public static final String FULL_DATE_STRING="yyyy-MM-dd HH:mm:ss";
    public static final String SHORT_DATE_STRING="MM-dd HH:mm:ss";

    ///////////////////////////////////
    public static Date longTimeToDate(long time) {
        return new Date(time);
    }

    public static String formatDate(Date date, String pattern) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(pattern);
            return sdf.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getFullDateString(Date date) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(FULL_DATE_STRING);
            return sdf.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getDateString(Date date) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(SHORT_DATE_STRING);
            return sdf.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getShortDateString(long time) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(SHORT_DATE_STRING);
            return sdf.format(new Date(time));
        } catch (Exception e) {
        }
        return "";
    }

    //格式化GMT时间.
    public static String gmtToString(Date date) {
        String pattern="EEE MMM dd HH:mm:ss z yyyy";
        return formatDate(date, pattern);
    }

    public static String fullDateTimeString(Date date) {
        String pattern="yyyy-mm-dd hh:MM:ss";
        return formatDate(date, pattern);
    }

    public static String shortDateTimeString(Date date) {
        String pattern="mm-dd hh:MM:ss";
        return formatDate(date, pattern);
    }

    public static String fullDateString(Date date) {
        String pattern="yyyy-mm-dd";
        return formatDate(date, pattern);
    }

    public static String longToDateString(long time) {
        String pattern="yyyy-mm-dd";
        return formatDate(new Date(time), pattern);
    }

    public static String longToDateTimeString(long time) {
        //String pattern="yyyy-mm-dd hh:MM:ss";
        return formatDate(new Date(time), SHORT_DATE_STRING);
    }

    public static Date parseDateString(String date, String pattern) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(pattern);
            return sdf.parse(date);
        } catch (Exception e) {
        }
        return null;
    }
}
