package com.me.microblog.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.me.microblog.App;
import com.me.microblog.R;

/**
 * 通知或信息工具类。比如toast通知，服务发出的Nofitycation通知，弹出对话框通知。
 *
 * @author: archko 2014/8/25 :15:38
 */
public class NotifyUtil {

    public final static View makeToastView(String txt) {
        View overlay=((LayoutInflater) App.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
            inflate(R.layout.ak_toast_view, null);
        TextView textView=(TextView) overlay.findViewById(R.id.txt_toast_text);
        textView.setText(txt);

        return overlay;
    }

    public static final Toast makeToast(String txt) {
        View overlay=makeToastView(txt);
        Toast toast=new Toast(App.getAppContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(overlay);
        return toast;
    }

    public static final Toast makeToast(String txt, int delay) {
        View overlay=makeToastView(txt);
        Toast toast=new Toast(App.getAppContext());
        toast.setDuration(delay);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(overlay);
        return toast;
    }

    public static final Toast makeToast(int resId) {
        View overlay=makeToastView(App.getAppContext().getString(resId));
        Toast toast=new Toast(App.getAppContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(overlay);
        return toast;
    }

    public static final Toast makeToast(int resId, int delay) {
        View overlay=makeToastView(App.getAppContext().getString(resId));
        Toast toast=new Toast(App.getAppContext());
        toast.setDuration(delay);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(overlay);
        return toast;
    }

    public static final void showToast(String txt) {
        Toast toast=makeToast(txt);
        toast.show();
    }

    public static final void showToast(int resId) {
        Toast toast=makeToast(resId);
        toast.show();
    }

    public static final void showToast(final String message, final int delay) {
        Toast toast=makeToast(message, delay);
        toast.show();
    }

    public static final void showToast(final int resId, final int delay) {
        Toast toast=makeToast(resId, delay);
        toast.show();
    }

    //------------------------------

    public static void showCheatSheet(final View view) {

        final int[] screenPos=new int[2]; // origin is device display
        final Rect displayFrame=new Rect(); // includes decorations (e.g.
        // status bar)
        view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);

        final Context context=view.getContext();
        final int viewWidth=view.getWidth();
        final int viewHeight=view.getHeight();
        final int viewCenterX=screenPos[0]+viewWidth/2;
        final int screenWidth=context.getResources().getDisplayMetrics().widthPixels;
        final int estimatedToastHeight=(int) (48*context.getResources().getDisplayMetrics().density);

        final Toast cheatSheet=Toast.makeText(context, view.getContentDescription(),
            Toast.LENGTH_SHORT);
        final boolean showBelow=screenPos[1]<estimatedToastHeight;
        if (showBelow) {
            // Show below
            // Offsets are after decorations (e.g. status bar) are factored in
            cheatSheet.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, viewCenterX
                -screenWidth/2, screenPos[1]-displayFrame.top+viewHeight);
        } else {
            // Show above
            // Offsets are after decorations (e.g. status bar) are factored in
            cheatSheet.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, viewCenterX
                -screenWidth/2, displayFrame.bottom-screenPos[1]);
        }
        cheatSheet.show();
    }

    //------------------------------
}
