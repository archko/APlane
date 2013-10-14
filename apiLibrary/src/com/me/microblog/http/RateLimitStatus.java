package com.me.microblog.http;

import java.io.Serializable;
import java.util.Date;

public class RateLimitStatus implements Serializable {

    public int hourlyLimit;
    public int remainingHits;
    public Date resetTime;
    public int resetTimeInSeconds;

    public RateLimitStatus() {
    }
}
