package cn.archko.microblog.controller;

import android.app.Activity;
import com.me.microblog.util.NotifyUtils;
import com.me.microblog.util.WeiboLog;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * @author: archko 2015/1/4 :14:43
 */
public class UpdateHelper {

    private Activity mActivity;

    public UpdateHelper(Activity activity) {
        this.mActivity = activity;
    }

    public void checkUpdate() {
        UmengUpdateAgent.setUpdateOnlyWifi(false); // 目前我们默认在Wi-Fi接入情况下才进行自动提醒。如需要在其他网络环境下进行更新自动提醒，则请添加该行代码
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(mUpdateListener);

        /*UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

            @Override
            public void OnDownloadEnd(int result) {
                WeiboLog.i(TAG, "download result : "+result);
                showToast("download result : "+result);
            }

        });*/

        UmengUpdateAgent.update(mActivity);
    }

    UmengUpdateListener mUpdateListener = new UmengUpdateListener() {
        @Override
        public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
            if (mActivity.isFinishing()) {
                return;
            }

            switch (updateStatus) {
                case 0: // has update
                    WeiboLog.i("callback result");
                    UmengUpdateAgent.showUpdateDialog(mActivity, updateInfo);
                    break;
                case 1: // has no update
                    NotifyUtils.showToast("没有更新");
                    break;
                case 2: // none wifi
                    NotifyUtils.showToast("没有wifi连接， 只在wifi下更新");
                    break;
                case 3: // time out
                    NotifyUtils.showToast("超时");
                    break;
            }

        }
    };
}
