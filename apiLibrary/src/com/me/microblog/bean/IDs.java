package com.me.microblog.bean;

import java.io.Serializable;

public class IDs implements Serializable {

    public static final long serialVersionUID = 3894560643019408224L;

    public long ids[];
    public long nextCursor;
    public long previousCursor;

    public IDs() {
    }
}
