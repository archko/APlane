package com.me.microblog.bean;

import java.io.Serializable;

/**
 * User: archko Date: 12-8-13 Time: 下午2:36
 */
public class SAnnotation implements Serializable {

    public static final long serialVersionUID=3894560643019408214L;

    public SPlace place;

    @Override
    public String toString() {
        return "SAnnotation{"+
            "place="+place+
            '}';
    }
}
