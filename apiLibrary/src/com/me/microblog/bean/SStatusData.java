package com.me.microblog.bean;

import java.util.ArrayList;

/**
 * User: archko Date: 12-7-23 Time: 上午11:11
 */
public class SStatusData <T> {

    /**
     * 微博列表
     */
    public ArrayList<T> mStatusData;
    public T mData;
    public int previous_cursor;
    public int next_cursor;
    public int total_number;
    public boolean hasvisible;
    public int errorCode;
    public String errorMsg;

    @Override
    public String toString() {
        return "SStatusData{" +
            "mStatusData=" + mStatusData +
            ", mData=" + mData +
            ", previous_cursor=" + previous_cursor +
            ", next_cursor=" + next_cursor +
            ", total_number=" + total_number +
            ", hasvisible=" + hasvisible +
            ", errorCode=" + errorCode +
            ", errorMsg='" + errorMsg + '\'' +
            '}';
    }
}
