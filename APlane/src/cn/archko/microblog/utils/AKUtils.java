package cn.archko.microblog.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import cn.archko.microblog.R;
import cn.archko.microblog.service.AKWidgetService;
import cn.archko.microblog.service.SendTaskService;
import cn.archko.microblog.service.WeiboService;
import cn.archko.microblog.ui.LoginActivity;
import cn.archko.microblog.view.ColorSchemeDialog;
import com.andrew.apollo.utils.PreferenceUtils;
import com.me.microblog.App;
import com.me.microblog.WeiboUtil;
import com.me.microblog.util.WeiboLog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * User: archko Date: 12-12-28 Time: 下午3:18
 */
public class AKUtils {

    public static void showToast(final String message) {
        Toast.makeText(App.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(final int resId) {
        Toast.makeText(App.getAppContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(final String message, final int delay) {
        Toast.makeText(App.getAppContext(), message, delay).show();
    }

    public static void showToast(final int resId, final int delay) {
        Toast.makeText(App.getAppContext(), resId, delay).show();
    }

    public static String stripTrailingSlash(String _s) {
        String s=_s;
        if (s.endsWith("/")&&s.length()>1)
            s=s.substring(0, s.length()-1);
        return s;
    }

    /**
     * Convert time to a string
     *
     * @param millis e.g.time/length from file
     * @return formated string (hh:)mm:ss
     */
    public static String millisToString(long millis) {
        boolean negative=millis<0;
        millis=Math.abs(millis);

        millis/=1000;
        int sec=(int) (millis%60);
        millis/=60;
        int min=(int) (millis%60);
        millis/=60;
        int hours=(int) millis;

        String time;
        DecimalFormat format=(DecimalFormat) NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
        if (millis>0) {
            time=(negative ? "-" : "")+hours+":"+format.format(min)+":"+format.format(sec);
        } else {
            time=(negative ? "-" : "")+min+":"+format.format(sec);
        }
        return time;
    }

    public static Bitmap scaleDownBitmap(Context context, Bitmap bitmap, int width) {
        if (bitmap!=null) {
            final float densityMultiplier=context.getResources().getDisplayMetrics().density;
            int w=(int) (width*densityMultiplier);
            int h=(int) (w*bitmap.getHeight()/((double) bitmap.getWidth()));
            bitmap=Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    public static Bitmap cropBorders(Bitmap bitmap, int width, int height) {
        int top=0;
        for (int i=0; i<height/2; i++) {
            int pixel1=bitmap.getPixel(width/2, i);
            int pixel2=bitmap.getPixel(width/2, height-i-1);
            if ((pixel1==0||pixel1==-16777216)&&
                (pixel2==0||pixel2==-16777216)) {
                top=i;
            } else {
                break;
            }
        }

        int left=0;
        for (int i=0; i<width/2; i++) {
            int pixel1=bitmap.getPixel(i, height/2);
            int pixel2=bitmap.getPixel(width-i-1, height/2);
            if ((pixel1==0||pixel1==-16777216)&&
                (pixel2==0||pixel2==-16777216)) {
                left=i;
            } else {
                break;
            }
        }

        if (left>=width/2-10||top>=height/2-10)
            return bitmap;

        // Cut off the transparency on the borders
        return Bitmap.createBitmap(bitmap, left, top,
            (width-(2*left)), (height-(2*top)));
    }

    public static int convertPxToDp(int px) {
        WindowManager wm=(WindowManager) App.getAppContext().
            getSystemService(Context.WINDOW_SERVICE);
        Display display=wm.getDefaultDisplay();
        DisplayMetrics metrics=new DisplayMetrics();
        display.getMetrics(metrics);
        float logicalDensity=metrics.density;
        int dp=Math.round(px/logicalDensity);
        return dp;
    }

    public static int convertDpToPx(int dp) {
        return Math.round(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                App.getAppContext().getResources().getDisplayMetrics())
        );
    }

    //--------------------- logout ---------------------

    /**
     * 注销
     */
    public static void logout(Activity activity) {
        WeiboUtil.logout(activity);
        ((App) App.getAppContext()).logout();

        Intent intent=new Intent(activity, SendTaskService.class);
        activity.stopService(intent);

        WeiboLog.d("logout.");
        Intent loginIntent=new Intent(activity, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.putExtra("mode", "1");
        activity.startActivity(loginIntent);
        activity.finish();
    }

    public static void exit(Activity activity) {
        Intent intent=new Intent(activity, WeiboService.class);
        activity.stopService(intent);
        intent=new Intent(activity, SendTaskService.class);
        activity.stopService(intent);
        intent=new Intent(activity, AKWidgetService.class);
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
        final ColorSchemeDialog colorPickerView=new ColorSchemeDialog(context, currentColor);
        colorPickerView.setButton(AlertDialog.BUTTON_POSITIVE,
            context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                PreferenceUtils.getInstace(context).setDefaultFontThemeColor(key,
                    colorPickerView.getColor());
            }
        });
        colorPickerView.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel),
            (DialogInterface.OnClickListener) null);
        colorPickerView.show();
    }
}
