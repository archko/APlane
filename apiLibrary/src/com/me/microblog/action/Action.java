package com.me.microblog.action;

import android.content.Context;

/**
 * @author archko
 */
public interface Action {

    public ActionResult doAction(Context context, Object... params);
}
