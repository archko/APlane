package cn.archko.microblog.fragment.abs;

import com.me.microblog.bean.AtUser;

/**
 * at用户查询的监听器
 *
 * @author : archko Date: 12-12-6 Time: 下午7:58
 */
public interface AtUserListener {

    /**
     * 获取一个用户用于@。
     *
     * @return
     */
    public void getAtUser(AtUser atUser);
}
