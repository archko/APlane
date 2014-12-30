package com.me.microblog.bean;

import java.io.Serializable;

/**
 * @author root
 */
public class RelationInfo implements Serializable {

    public static final long serialVersionUID = 3894560643019408216L;

    public boolean followedBy;
    public boolean following;
    public long id;
    public boolean notificationsEnabled;
    public String screenName;

    RelationInfo() {
    }
}
