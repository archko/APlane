package com.me.microblog.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import com.me.microblog.App;

/**
 * @author archko
 */
public class Util {
    public final static String TAG="Util";
    public final static boolean hasNavBar;

    /** A set of utility functions for the VLC application */

    static {
        HashSet<String> devicesWithoutNavBar=new HashSet<String>();
        devicesWithoutNavBar.add("HTC One V");
        devicesWithoutNavBar.add("HTC One S");
        devicesWithoutNavBar.add("HTC One X");
        devicesWithoutNavBar.add("HTC One XL");
        hasNavBar=isICSOrLater()&&!devicesWithoutNavBar.contains(android.os.Build.MODEL);
    }

    /**
     * Print an on-screen message to alert the user
     */
    public static void toaster(Context context, int stringId, int duration) {
        Toast.makeText(context, stringId, duration).show();
    }

    public static void toaster(Context context, int stringId) {
        toaster(context, stringId, Toast.LENGTH_SHORT);
    }

    public static File URItoFile(String URI) {
        return new File(Uri.decode(URI).replace("file://", ""));
    }

    public static String URItoFileName(String URI) {
        int sep=URI.lastIndexOf('/');
        int dot=URI.lastIndexOf('.');
        String name=dot>=0 ? URI.substring(sep+1, dot) : URI;
        return Uri.decode(name);
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

    public static boolean isGingerbreadOrLater() {
        return android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean isHoneycombOrLater() {
        return android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isICSOrLater() {
        return android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasNavBar() {
        return hasNavBar;
    }

    private static String errorMsg=null;
    private static boolean isCompatible=false;

    public static String getErrorMsg() {
        return errorMsg;
    }
}
