package cn.archko.microblog.view;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-4-13
 */
public interface CommentListener {

    /**
     * 取消发送
     */
    void cancel();

    /**
     * 发送成功
     *
     * @param receiver 接收方
     * @param content  发送内容
     */
    void finish(Object receiver, String content);
}
