package com.me.microblog.http;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-12-27
 */
public class RssXMLPullHandler implements IXmlParser {

    WeatherBean mWeatherBean;
    ForecastCondition rb=null;
    boolean is_current_conditions=false;
    boolean is_forecast_conditions=false;

    public WeatherBean getWeatherBean() {
        return mWeatherBean;
    }

    public RssXMLPullHandler() {
    }

    @Override
    public void parseRss(InputStream is) {
        XmlPullParser parser=Xml.newPullParser();
        try {
            parser.setInput(is, "UTF-8");
            parse(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            is=null;
        }
    }

    void parse(XmlPullParser parser) {
        int evtType=0;
        try {
            evtType=parser.getEventType();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        while (evtType!=XmlPullParser.END_DOCUMENT) {
            switch (evtType) {
                case XmlPullParser.START_DOCUMENT:
                    this.mWeatherBean=new WeatherBean();
                    this.mWeatherBean.forecastConditions=new ArrayList<ForecastCondition>();
                    break;

                case XmlPullParser.START_TAG:
                    String tag=parser.getName();
                    if ("weather".equals(tag)) {

                    } else if ("city".equals(tag)) {
                        mWeatherBean.city=parser.getAttributeValue(0);
                    } else if ("postal_code".equals(tag)) {
                        mWeatherBean.postal_code=parser.getAttributeValue(0);
                    } else if ("latitude_e6".equals(tag)) {
                        mWeatherBean.latitude_e6=parser.getAttributeValue(0);
                    } else if ("longitude_e6".equals(tag)) {
                        mWeatherBean.longitude_e6=parser.getAttributeValue(0);
                    } else if ("forecast_date".equals(tag)) {
                        mWeatherBean.forecast_date=parser.getAttributeValue(0);
                    } else if ("current_date_time".equals(tag)) {
                        mWeatherBean.current_date_time=parser.getAttributeValue(0);
                    } else if ("unit_system".equals(tag)) {
                        mWeatherBean.unit_system=parser.getAttributeValue(0);
                    } else if ("current_conditions".equals(tag)) {
                        is_current_conditions=true;
                        is_forecast_conditions=false;
                    } else if ("forecast_conditions".equals(tag)) {
                        rb=new ForecastCondition();
                        mWeatherBean.forecastConditions.add(rb);
                        is_forecast_conditions=true;
                        is_current_conditions=false;
                    }
                    //////////
                    else if ("condition".equals(tag)) {
                        if (is_current_conditions) {
                            mWeatherBean.condition=parser.getAttributeValue(0);
                        } else if (is_forecast_conditions) {
                            rb.condition=parser.getAttributeValue(0);
                        }
                    } else if ("temp_f".equals(tag)) {
                        mWeatherBean.temp_f=parser.getAttributeValue(0);
                    } else if ("temp_c".equals(tag)) {
                        mWeatherBean.temp_c=parser.getAttributeValue(0);
                    } else if ("humidity".equals(tag)) {
                        mWeatherBean.humidity=parser.getAttributeValue(0);
                    } else if ("unit_system".equals(tag)) {
                        mWeatherBean.unit_system=parser.getAttributeValue(0);
                    } else if ("wind_condition".equals(tag)) {
                        mWeatherBean.wind_condition=parser.getAttributeValue(0);
                    }
                    ///////
                    else if ("icon".equals(tag)) {
                        if (is_current_conditions) {
                            mWeatherBean.icon=parser.getAttributeValue(0);
                        } else if (is_forecast_conditions) {
                            rb.icon=parser.getAttributeValue(0);
                        }
                    } else if ("day_of_week".equals(tag)) {
                        rb.day_of_week=parser.getAttributeValue(0);
                    } else if ("low".equals(tag)) {
                        rb.low=parser.getAttributeValue(0);
                    } else if ("high".equals(tag)) {
                        rb.high=parser.getAttributeValue(0);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    break;

                case XmlPullParser.END_DOCUMENT:
                    break;
            }

            try {
                evtType=parser.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
