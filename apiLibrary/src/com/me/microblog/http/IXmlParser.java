package com.me.microblog.http;

import java.io.InputStream;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-2-7
 */
public interface IXmlParser {

    public void parseRss(InputStream is);

    public WeatherBean getWeatherBean();
}
