package com.me.microblog.action;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.me.microblog.util.WeiboLog;

/**
 * 加载分组的任务，
 *
 * @author : archko Date: 12-12-12 Time: 下午2:07
 */
public class AsyncActionTask extends ActionTask {

    public static final String TAG = "AsyncActionTask";
    public Handler handler = null;
    private Context mContext;

    public AsyncActionTask(Context context, Action action) {
        super(context, action);
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ActionResult doInBackground(Object... params) {
        handler = (Handler) params[ params.length - 1 ];
        return mAction.doAction(mContext, params);
    }

    @Override
    protected void onPostExecute(ActionResult result) {
        try {
            int rescode = result.resoultCode;
            WeiboLog.d(TAG, "onPostExecute.rescode:" + rescode);

            Message msg = handler.obtainMessage(rescode, result);

            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
