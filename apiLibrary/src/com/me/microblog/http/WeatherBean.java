package com.me.microblog.http;

import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-1-16
 */
@Deprecated
public class WeatherBean {

    public String city;
    public String postal_code;
    public String latitude_e6;
    public String longitude_e6;
    public String forecast_date;
    public String current_date_time;
    public String unit_system;

    //current_conditions
    public String condition;
    public String temp_f;
    public String temp_c;
    public String humidity;
    public String icon;
    public String wind_condition;

    ArrayList<ForecastCondition> forecastConditions;

    public ArrayList<ForecastCondition> getForecastConditions() {
        return forecastConditions;
    }

    @Override
    public String toString() {
        return "WeatherBean{" +
            "city='" + city + '\'' +
            ", postal_code='" + postal_code + '\'' +
            ", latitude_e6='" + latitude_e6 + '\'' +
            ", longitude_e6='" + longitude_e6 + '\'' +
            ", forecast_date='" + forecast_date + '\'' +
            ", current_date_time='" + current_date_time + '\'' +
            ", unit_system='" + unit_system + '\'' +
            ", condition='" + condition + '\'' +
            ", temp_f='" + temp_f + '\'' +
            ", temp_c='" + temp_c + '\'' +
            ", humidity='" + humidity + '\'' +
            ", icon='" + icon + '\'' +
            ", wind_condition='" + wind_condition + '\'' +
            ", forecastConditions=" + forecastConditions +
            '}';
    }
}
