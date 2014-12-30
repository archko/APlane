package com.me.microblog.action;

import android.content.Context;
import android.os.AsyncTask;

/**
 * @author archko
 */
public class ActionTask extends AsyncTask<Object, Integer, ActionResult> {

    protected Context mContext;
    protected Action mAction;

    public ActionTask(Context context, Action action) {
        super();
        this.mContext = context;
        this.mAction = action;
    }

    @Override
    protected ActionResult doInBackground(Object... params) {
        return mAction.doAction(mContext, params);
    }
}
