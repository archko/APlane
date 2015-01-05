package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @author: archko 2015/1/4 :9:36
 */
public class AKLocation implements Serializable {

    public double longitude = 0.0;
    public double latitude = 0.0;
    public int range = 10000;
    /**
     * 定位的时间.如果地图定位没有自动更新,就需要手动更新.
     */
    public long mLocationTimestamp = 0;
    public String addr;

    public AKLocation() {
    }

    public AKLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "AKLocation{" +
            "longitude=" + longitude +
            ", latitude=" + latitude +
            ", range=" + range +
            ", mLocationTimestamp=" + mLocationTimestamp +
            '}';
    }
}
