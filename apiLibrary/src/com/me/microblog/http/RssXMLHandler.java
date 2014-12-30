package com.me.microblog.http;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-12-27
 */
public class RssXMLHandler extends DefaultHandler implements IXmlParser {

    WeatherBean mWeatherBean;
    ForecastCondition rb = null;
    boolean is_current_conditions = false;
    boolean is_forecast_conditions = false;
    //private StringBuilder sb=new StringBuilder();

    public WeatherBean getWeatherBean() {
        return mWeatherBean;
    }

    public RssXMLHandler() {
    }

    @Override
    public void parseRss(InputStream is) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();

            XMLReader xmlReader = parser.getXMLReader();

            xmlReader.setContentHandler(this);
            xmlReader.parse(new InputSource(is));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is = null;
            factory = null;
            parser = null;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
        if ("weather".equals(qName) || "weather".equals(localName)) {

        } else if ("city".equals(qName) || "city".equals(localName)) {
            mWeatherBean.city = attributes.getValue(0);
        } else if ("postal_code".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.postal_code = attributes.getValue(0);
        } else if ("latitude_e6".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.latitude_e6 = attributes.getValue(0);
        } else if ("longitude_e6".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.longitude_e6 = attributes.getValue(0);
        } else if ("forecast_date".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.forecast_date = attributes.getValue(0);
        } else if ("current_date_time".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.current_date_time = attributes.getValue(0);
        } else if ("unit_system".equals(qName) || "weather".equals(localName)) {
            mWeatherBean.unit_system = attributes.getValue(0);
        } else if ("current_conditions".equals(qName) || "current_conditions".equals(localName)) {
            is_current_conditions = true;
            is_forecast_conditions = false;
        } else if ("forecast_conditions".equals(qName) || "forecast_conditions".equals(localName)) {
            rb = new ForecastCondition();
            mWeatherBean.forecastConditions.add(rb);
            is_forecast_conditions = true;
            is_current_conditions = false;
        }
        /////
        else if ("condition".equals(qName) || "condition".equals(localName)) {
            if (is_current_conditions) {
                mWeatherBean.condition = attributes.getValue(0);
            } else if (is_forecast_conditions) {
                rb.condition = attributes.getValue(0);
            }
        } else if ("temp_f".equals(qName) || "temp_f".equals(localName)) {
            mWeatherBean.temp_f = attributes.getValue(0);
        } else if ("temp_c".equals(qName) || "temp_c".equals(localName)) {
            mWeatherBean.temp_c = attributes.getValue(0);
        } else if ("humidity".equals(qName) || "humidity".equals(localName)) {
            mWeatherBean.humidity = attributes.getValue(0);
        } else if ("unit_system".equals(qName) || "unit_system".equals(localName)) {
            mWeatherBean.unit_system = attributes.getValue(0);
        } else if ("wind_condition".equals(qName) || "wind_condition".equals(localName)) {
            mWeatherBean.wind_condition = attributes.getValue(0);
        }
        /////
        else if ("icon".equals(qName) || "icon".equals(localName)) {
            if (is_current_conditions) {
                mWeatherBean.icon = attributes.getValue(0);
            } else if (is_forecast_conditions) {
                rb.icon = attributes.getValue(0);
            }
        } else if ("day_of_week".equals(qName) || "day_of_week".equals(localName)) {
            rb.day_of_week = attributes.getValue(0);
        } else if ("low".equals(qName) || "low".equals(localName)) {
            rb.low = attributes.getValue(0);
        } else if ("high".equals(qName) || "high".equals(localName)) {
            rb.high = attributes.getValue(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //sb.append(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        /*if (qName.equals("item")||"item".equals(localName)) {
           //forecastConditions.add(rb);
       } else if (qName.equals("day_of_week")||"day_of_week".equals(localName)) {
           if (!is_current_conditions) {
               rb.day_of_week=(sb.toString().trim());
           }
       } else if (qName.equals("low")||"low".equals(localName)) {
           if (!is_current_conditions) {
               rb.low=(sb.toString().trim());
           }
       } else if (qName.equals("high")||"high".equals(localName)) {
           //System.out.println("link:"+sb.toString());
           if (!is_current_conditions) {
               rb.high=(sb.toString().trim());
           }
       } else if (qName.equals("icon")||"icon".equals(localName)) {
           if (!is_current_conditions) {
               rb.icon=sb.toString().trim();
           } else {
               mWeatherBean.icon=sb.toString().trim();
           }
       } else if (qName.equals("condition")||"condition".equals(localName)) {
           if (!is_current_conditions) {
               rb.condition=(sb.toString().trim());
           } else {
               mWeatherBean.condition=sb.toString().trim();
           }
       } else if (qName.equals("temp_f")||"temp_f".equals(localName)) {
           if (is_current_conditions) {
               mWeatherBean.temp_f=sb.toString().trim();
           }
       } else if (qName.equals("temp_c")||"temp_c".equals(localName)) {
           if (is_current_conditions) {
               mWeatherBean.temp_c=sb.toString().trim();
           }
       } else if (qName.equals("humidity")||"humidity".equals(localName)) {
           if (is_current_conditions) {
               mWeatherBean.humidity=sb.toString().trim();
           }
       } else if (qName.equals("wind_condition")||"wind_condition".equals(localName)) {
           if (is_current_conditions) {
               mWeatherBean.wind_condition=sb.toString().trim();
           }
       }
       sb.setLength(0);// 清除内容.*/
    }

    @Override
    public void startDocument() throws SAXException {
        this.mWeatherBean = new WeatherBean();
        mWeatherBean.forecastConditions = new ArrayList<ForecastCondition>();
    }

    @Override
    public void endDocument() throws SAXException {
        //System.out.println(mWeatherBean);
    }
}
