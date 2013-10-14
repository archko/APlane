package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @author archko
 */
public class Trend implements Serializable {

    public static final long serialVersionUID=3894560643019408207L;
    public String name;
    public String query;

    @Override
    public String toString() {
        return "Trend{"+"name="+name+", query="+query+'}';
    }

}
