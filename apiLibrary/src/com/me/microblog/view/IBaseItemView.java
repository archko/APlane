package com.me.microblog.view;

import com.me.microblog.bean.Status;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-12-17
 */
public interface IBaseItemView {

    void update(final Status bean, boolean updateFlag, boolean cache);
}
