package com.me.microblog.bean;

import java.io.Serializable;

public class Relationship implements Serializable {

    public static final long serialVersionUID = 3894560643019408215L;

    public RelationInfo source;
    public RelationInfo target;

    public Relationship() {
        source = new RelationInfo();
        target = new RelationInfo();
    }
}
