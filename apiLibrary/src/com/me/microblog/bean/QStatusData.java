package com.me.microblog.bean;

import java.util.ArrayList;

/**
 * @author archko
 */
public class QStatusData {

    public static final long serialVersionUID = 3894560643019408218L;

    /**
     * 返回值，0-成功，非0-失败
     */
    public int ret;
    /**
     * 错误信息
     */
    public String msg;
    /**
     * 返回错误码
     */
    public String errcode;
    /**
     * 可选，所有记录的总数,主页时间线没有，话题时间线有
     */
    public int totalnum;
    /**
     * 服务器时间戳，不能用于翻页，主页时间线有，话题时间线没有
     */
    public long timestamp;
    /**
     * 0-表示还有微博可拉取，1-已拉取完毕
     */
    public int hasnext;
    /**
     * 包含的微博信息
     */
    public ArrayList<QStatus> qStatuses;

    @Override
    public String toString() {
        return "QStatusData{" + "ret=" + ret + ", msg=" + msg + ", errcode=" + errcode + ", totalnum="
            + totalnum + ", timestamp=" + timestamp + ", hasnext=" + hasnext
            + ", qStatuses=" + qStatusToString() + '}';
    }

    String qStatusToString() {
        if (qStatuses == null || qStatuses.size() < 1) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        for (QStatus qStatus : qStatuses) {
            sb.append(qStatus.toString()).append("\n");
        }
        return sb.toString();
    }
}
