package cn.archko.microblog.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import cn.archko.microblog.R;
import cn.archko.microblog.service.AKWidgetService;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.service.WeiboService;
import cn.archko.microblog.ui.UserFragmentActivity;
import cn.archko.microblog.view.ColorSchemeDialog;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtils;
import com.me.microblog.util.WeiboLog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: archko Date: 12-12-28 Time: 下午3:18
 */
public class AKUtils {

    public static String stripTrailingSlash(String _s) {
        String s = _s;
        if (s.endsWith("/") && s.length() > 1)
            s = s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * Convert time to a string
     *
     * @param millis e.g.time/length from file
     * @return formated string (hh:)mm:ss
     */
    public static String millisToString(long millis) {
        boolean negative = millis < 0;
        millis = Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
        if (millis > 0) {
            time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
        } else {
            time = (negative ? "-" : "") + min + ":" + format.format(sec);
        }
        return time;
    }

    public static Bitmap scaleDownBitmap(Context context, Bitmap bitmap, int width) {
        if (bitmap != null) {
            final float densityMultiplier = context.getResources().getDisplayMetrics().density;
            int w = (int) (width * densityMultiplier);
            int h = (int) (w * bitmap.getHeight() / ((double) bitmap.getWidth()));
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    public static Bitmap cropBorders(Bitmap bitmap, int width, int height) {
        int top = 0;
        for (int i = 0; i < height / 2; i++) {
            int pixel1 = bitmap.getPixel(width / 2, i);
            int pixel2 = bitmap.getPixel(width / 2, height - i - 1);
            if ((pixel1 == 0 || pixel1 == - 16777216) &&
                (pixel2 == 0 || pixel2 == - 16777216)) {
                top = i;
            } else {
                break;
            }
        }

        int left = 0;
        for (int i = 0; i < width / 2; i++) {
            int pixel1 = bitmap.getPixel(i, height / 2);
            int pixel2 = bitmap.getPixel(width - i - 1, height / 2);
            if ((pixel1 == 0 || pixel1 == - 16777216) &&
                (pixel2 == 0 || pixel2 == - 16777216)) {
                left = i;
            } else {
                break;
            }
        }

        if (left >= width / 2 - 10 || top >= height / 2 - 10)
            return bitmap;

        // Cut off the transparency on the borders
        return Bitmap.createBitmap(bitmap, left, top,
            (width - (2 * left)), (height - (2 * top)));
    }

    //--------------------- logout ---------------------

    /**
     * 注销
     */
    public static void logout(Activity activity) {
        WeiboUtils.logout(activity);
        ((App) App.getAppContext()).logout();

        Intent intent = new Intent(activity, SendTaskService.class);
        activity.stopService(intent);

        WeiboLog.d("logout.");
        /*Intent loginIntent=new Intent(activity, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.putExtra("mode", "1");
        activity.startActivity(loginIntent);*/
        WeiboOperation.startAccountActivity(activity);
        activity.finish();
    }

    public static void exit(Activity activity) {
        Intent intent = new Intent(activity, WeiboService.class);
        activity.stopService(intent);
        intent = new Intent(activity, SendTaskService.class);
        activity.stopService(intent);
        intent = new Intent(activity, AKWidgetService.class);
        activity.stopService(intent);
        ((App) App.getAppContext()).logout();
        activity.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Shows the {@link ColorPickerView}
     *
     * @param context The {@link android.content.Context} to use.
     * @param key     sp key
     */
    public static void showColorPicker(final Context context, final String key, int currentColor) {
        final ColorSchemeDialog colorPickerView = new ColorSchemeDialog(context, currentColor);
        colorPickerView.setButton(AlertDialog.BUTTON_POSITIVE,
            context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    PreferenceUtils.getInstace(context).setDefaultFontThemeColor(key,
                        colorPickerView.getColor());
                }
            }
        );
        colorPickerView.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel),
            (DialogInterface.OnClickListener) null);
        colorPickerView.show();
    }

    //--------------------- 内容点击器 ---------------------

    public static class AtClicker extends WeiboUtils.MyClicker {

        Context mContext;

        public AtClicker(Context context) {
            this.mContext = context;
        }

        @Override
        public void updateDrawState(TextPaint textPaint) {
            try {
                if (null != mContext && null != mContext.getResources()) {
                    textPaint.setColor(mContext.getResources().getColor(R.color.holo_light_item_highliht_link));
                    textPaint.setUnderlineText(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            WeiboLog.d("AtClicker:" + name);
            if (TextUtils.isEmpty(name)) {
                WeiboLog.e("nick name is null.");
                return;
            }
            WeiboOperation.toViewStatusUser((Activity) mContext, name,
                - 1, UserFragmentActivity.TYPE_USER_INFO);
        }

    }

    public static class UrlClicker extends WeiboUtils.MyClicker {

        Context mContext;

        public UrlClicker(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void updateDrawState(TextPaint textPaint) {
            try {
                if (null != mContext && null != mContext.getResources()) {
                    textPaint.setColor(mContext.getResources().getColor(R.color.holo_light_item_highliht_link));
                    textPaint.setUnderlineText(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            WeiboLog.d("UrlClicker:" + name);
            if (TextUtils.isEmpty(name)) {
                WeiboLog.e("url is null.");
                return;
            }
            //String str1=URLEncoder.encode(this.name);
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            boolean prefWebview = mPrefs.getBoolean(PreferenceUtils.PREF_WEBVIEW, true);
            if (! prefWebview) {
                WeiboUtils.openUrlByDefaultBrowser(mContext, name);
            } else {
                /*Intent intent=new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra("url", name);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_right, R.anim.enter_left);*/
                WeiboOperation.startWebview((Activity) mContext, name);
            }
        }
    }

    public static void highlightAtClickable(Context context, Spannable spannable, Pattern pattern) {
        Matcher atMatcher = pattern.matcher(spannable);

        while (atMatcher.find()) {
            int start = atMatcher.start();
            int end = atMatcher.end();
            //WeiboLog.d("weibo", "start:"+start+" end:"+end);
            if (end - start == 2) {
            } else {
                if (end - start <= 2) {
                    break;
                }
            }

            String name = spannable.subSequence(start, end).toString();
            AtClicker clicker = new AtClicker(context);
            clicker.name = name;
            spannable.setSpan(clicker, start, end, 34);
        }
    }

    public static void highlightUrlClickable(Context context, Spannable spannable, Pattern pattern) {
        Matcher atMatcher = pattern.matcher(spannable);

        while (atMatcher.find()) {
            int start = atMatcher.start();
            int end = atMatcher.end();
            //WeiboLog.d("weibo", "start:"+start+" end:"+end);
            if (end - start == 2) {
            } else {
                if (end - start <= 2) {
                    break;
                }
            }

            String name = spannable.subSequence(start, end).toString();
            UrlClicker clicker = new UrlClicker(context);
            clicker.name = name;
            spannable.setSpan(clicker, start, end, 34);
        }
    }
}
