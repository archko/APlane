package com.me.microblog.bean;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-2-21
 */
public class UpdateInfo {

    public String updateMode;
    public String updateMsg;
    public String updateUrl;
    public String hasNewVer;
    public String newVer;

    public UpdateInfo() {
        hasNewVer="-1";
        updateUrl="";
        updateMode="0";
        updateMsg="";
        newVer="574";
    }

    @Override
    public String toString() {
        return "UpdateInfo{"+
            "updateMode='"+updateMode+'\''+
            ", updateMsg='"+updateMsg+'\''+
            ", updateUrl='"+updateUrl+'\''+
            '}';
    }
}
