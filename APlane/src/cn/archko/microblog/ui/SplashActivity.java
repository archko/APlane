package cn.archko.microblog.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.utils.WeiboOperation;
import com.me.microblog.App;
import com.me.microblog.bean.UpdateInfo;
import com.me.microblog.core.ImageManager;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.util.Constants;
import com.me.microblog.util.StreamUtils;
import com.me.microblog.util.WeiboLog;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 闪屏，因为Fragment的问题也不去解决为什么ActionBar没有办法获取了， 所以只有能原来的FragmentTabActivity中
 * 的checkUpdate()方法放在这里了，先启动闪屏，如果没有更新再启动FragmentTabActivity
 *
 * @author archko date:2012-7-1
 */
public class SplashActivity extends NavModeActivity {

    public static final String TAG="SplashActivity";
    //-------------- update -------------------
    private final String mUpdateUrl="http://archko.t8go.com/update.json";
    Handler mHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (App.isLogined) {
            startIntent();
            return;
        }

        setContentView(R.layout.splash);

        createShortCut();

        //MobclickAgent.onError(this);
        init();
        //checkUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }

    public void createShortCut() {
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        int hasShortcuts=preferences.getInt(Constants.SHORTCUTS, -1);
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d(TAG, "hasShortcuts:"+hasShortcuts);
        }
        if (hasShortcuts>0) {
            return;
        }

        preferences.edit().putInt(Constants.SHORTCUTS, 1).commit();

        // 创建快捷方式的Intent
        Intent shortcutintent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        // 快捷图片
        Parcelable icon=Intent.ShortcutIconResource.fromContext(
            getApplicationContext(), R.drawable.logo);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
            getApplicationContext(), SplashActivity.class));
        // 发送广播。OK
        sendBroadcast(shortcutintent);
    }

    /**
     * 检查更新，这里一天检查一次
     */
    private void checkUpdate() {
        if (WeiboLog.isDEBUG()) {
            WeiboLog.d("checkUpdate");
        }

        if (!App.hasInternetConnection(SplashActivity.this)) {
            WeiboLog.w(TAG, "没有网络，不检查更新。");
            Toast.makeText(SplashActivity.this, getResources().getString(R.string.network_error),
                Toast.LENGTH_LONG).show();
            init();
            return;
        }

        SharedPreferences options=PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        boolean autoChkUpdate=options.getBoolean(PrefsActivity.PREF_AUTO_CHK_UPDATE, true);
        if (!autoChkUpdate) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d("不自动检查更新。");
            }
            init();
            return;
        }

        PackageManager manager=SplashActivity.this.getPackageManager();
        int currVersionCode=574;
        try {
            PackageInfo info=manager.getPackageInfo(SplashActivity.this.getPackageName(), 0);
            String packageName=info.packageName;
            currVersionCode=info.versionCode;
            //currVersionName=info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        final int cvd=currVersionCode;
        //mPrefs.getString(UPDTE_MODE,"0");

        long time=mPreferences.getLong(Constants.UPDATE_TIMESTAMP, -1);
        long now=System.currentTimeMillis();
        long delta=now-time-Constants.UPDATE_DELTA;
        WeiboLog.i("update.time:"+time+" now:"+now+" currVersionCode:"+currVersionCode);

        if (delta<0&&time!=-1) {
            if (WeiboLog.isDEBUG()) {
                WeiboLog.d(TAG, "不需要检查更新，近一天刚检查过，delta:"+delta+" time:"+time);
            }
            init();
            return;
        }

        SharedPreferences.Editor editor=mPreferences.edit();
        editor.putLong(Constants.UPDATE_TIMESTAMP, now);
        editor.commit();

        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updateFlag=false;

                String xml=null;
                try {
                    InputStream is=ImageManager.getImageStream(mUpdateUrl);
                    xml=StreamUtils.parseInputStream(is);
                    WeiboLog.v(TAG, "xml:"+xml);

                    final UpdateInfo updateInfo;
                    updateInfo=WeiboParser.parseUpdateInfo(xml);
                    String m="";
                    if ("-1".equals(updateInfo.hasNewVer)) {
                        if (WeiboLog.isDEBUG()) {
                            WeiboLog.d("没有新版本，或者检查更新出错，直接进入。");
                        }
                        init();
                    } else {
                        if (Integer.valueOf(updateInfo.newVer)>cvd) {
                            if (WeiboLog.isDEBUG()) {
                                WeiboLog.d("有新版本.");
                            }
                            updateFlag=true;
                        }

                        if (WeiboLog.isDEBUG()) {
                            WeiboLog.d(TAG, "updateInfo:"+updateInfo+" updateFlag:"+updateFlag);
                        }

                        if (updateFlag) {   //show update dialog
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        showUpdateDialog(updateInfo);
                                    }
                                });
                            } else {
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(SplashActivity.this, getString(R.string.update_force_NoSdcard),
                                            Toast.LENGTH_SHORT).show();
                                        init();
                                    }
                                });
                            }
                        } else {    //
                            init();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    init();
                }
            }
        }).start();
    }

    private void init() {
        /*BaseApi microBlog=App.initWeiboApi(SplashActivity.this);
        if (microBlog==null||App.oauth2_timestampe==0) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    Intent loginIntent=new Intent(SplashActivity.this, LoginActivity.class);
                    loginIntent.putExtra("mode", "1");
                    startActivity(loginIntent);
                    WeiboLog.d(TAG, "not logined.");
                    finish();
                }
            });
            return;
        }
        App.getAdvancedWeiboApi(SplashActivity.this);*/
        App app=(App) App.getAppContext();
        app.initOauth2(false);
        if (null==app.getOauthBean()||TextUtils.isEmpty(app.getOauthBean().accessToken)) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    /*Intent loginIntent=new Intent(SplashActivity.this, LoginActivity.class);
                    loginIntent.putExtra("mode", "1");
                    startActivity(loginIntent);*/
                    WeiboOperation.startAccountActivity(SplashActivity.this);
                    if (WeiboLog.isDEBUG()) {
                        WeiboLog.d(TAG, "not logined.");
                    }
                    finish();
                }
            });
            return;
        } else {

        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startIntent();
            }
        });
    }

    //-------------------------

    /**
     * 显示更新对话框
     *
     * @param updateInfo 更新信息实体
     */
    void showUpdateDialog(final UpdateInfo updateInfo) {
        LayoutInflater inflater=LayoutInflater.from(SplashActivity.this);
        View view=inflater.inflate(R.layout.home_dialog_view, null);
        Button cancelButton=(Button) view.findViewById(R.id.cancel);
        Button updateButton=(Button) view.findViewById(R.id.ok);
        TextView msgView=(TextView) view.findViewById(R.id.update_msg);

        msgView.setText(Html.fromHtml(updateInfo.updateMsg));
        AlertDialog.Builder builder=new AlertDialog.Builder(SplashActivity.this)
            .setTitle(R.string.update_title)
            .setView(view);

        final AlertDialog dialog=builder.create();
        dialog.show();

        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                WeiboLog.i(TAG, "cancel:"+updateInfo);
                /*if ("2".equals(updateInfo.updateMode)) {
                    //FragmentTabActivity.this.finish();
                } else if ("1".equals(updateInfo.updateMode)) {
                }*/
                init();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                WeiboLog.i(TAG, "udpate:"+updateInfo);
                //downloadUpdate(updateInfo);
                try {
                    Intent intent;
                    Uri uri=Uri.parse(updateInfo.updateUrl);
                    intent=new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashActivity.this.startActivity(intent);

                    SplashActivity.this.finish();
                    //android.os.Process.killProcess(android.os.Process.myPid());
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(SplashActivity.this, R.string.update_no_browser, Toast.LENGTH_LONG).show();
                    init();
                }
            }
        });
    }

    public static final String SOURCE="Fri Feb 14 20:23:51 +0800 2014";

    public static void test() {
       /*  Fri Feb 14 20:23:51 GMT+08:00 2014
         Fri Feb 14 20:23:51 +0800 2014
         2014-02-14 20:23:51
         2014年02月14日 20时23分51秒
         自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象经过的毫秒数为：1392380631000毫秒*/

        SimpleDateFormat sdf=new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("CHINA"));

        Date myDate=null;
        try {
            myDate=sdf.parse(SOURCE);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(myDate);

        sdf.applyPattern("EEE MMM dd HH:mm:ss Z yyyy");
        System.out.println(sdf.format(myDate));

        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("CHINESE", "CHINA"));
        System.out.println(sdf2.format(myDate));

        sdf2.applyPattern("yyyy年MM月dd日 HH时mm分ss秒");
        System.out.println(sdf2.format(myDate));

        long miliSeconds=myDate.getTime();
        System.out.println("自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象经过的毫秒数为："+miliSeconds+"毫秒");
    }
}
