package com.me.microblog.http;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-1-16
 */
@Deprecated
public class ForecastCondition {

    //forecast_conditions
    public String day_of_week;
    public String low;
    public String high;
    public String icon;
    public String condition;

    @Override
    public String toString() {
        return "ForecastCondition{" +
            "day_of_week='" + day_of_week + '\'' +
            ", low='" + low + '\'' +
            ", high='" + high + '\'' +
            ", icon='" + icon + '\'' +
            ", condition='" + condition + '\'' +
            '}';
    }
}
