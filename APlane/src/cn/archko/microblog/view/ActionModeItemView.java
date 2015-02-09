package cn.archko.microblog.view;

import android.content.Context;
import android.widget.Checkable;
import cn.archko.microblog.R;

/**
 * 主要用于ActionMode,可以悠选中的背景色.
 *
 * @author: archko 11-12-13
 */
public class ActionModeItemView extends ThreadBeanItemView implements Checkable {

    private static final String TAG="ActionModeItemView";

    private boolean checked=false;

    public ActionModeItemView(Context context, String cacheDir, boolean updateFlag,
        boolean cache, boolean showLargeBitmap, boolean showBitmap) {
        super(context, cacheDir, updateFlag, cache, showLargeBitmap, showBitmap);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean aChecked) {
        if (checked==aChecked) {
            return;
        }
        checked=aChecked;
        setBackgroundResource(checked ? R.drawable.abs__list_longpressed_holo : android.R.color.transparent);
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
}