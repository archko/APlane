package cn.archko.microblog.fragment.abs;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-1-2
 */
public interface OnRefreshListener {

    public void onRefreshStarted();

    public void onRefreshFinished();

    public void onRefreshFailed();
}
