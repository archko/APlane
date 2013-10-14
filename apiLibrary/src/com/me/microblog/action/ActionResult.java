package com.me.microblog.action;

/**
 * @author archko
 */
public class ActionResult {

    public final static int ACTION_FALL=0;
    public final static int ACTION_SUCESS=1;

    public ActionResult() {
        resoultCode=ACTION_FALL;
    }

    /**
     * 错误代码
     */
    public int resoultCode;
    /**
     * 错误信息
     */
    public String reslutMsg;
    /**
     * 结果集
     */
    public Object obj;
    /**
     * 结果集数组
     */
    public Object[] results;
}
