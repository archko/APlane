package com.me.microblog.bean;

import java.util.Date;

/**
 * @author archko
 */
public class Trends {

    public Trend[] trends;
    public String asOf;
    public Date trendAt;

    @Override
    public String toString() {
        return "Trends{" + "trends=" + trends + ", asOf=" + asOf + ", trendAt=" + trendAt + '}';
    }

}
