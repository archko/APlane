package cn.archko.microblog.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.archko.microblog.R;
import com.me.microblog.util.WeiboLog;

/**
 * @author archko
 */
public final class SeekBarPref extends Preference implements OnSeekBarChangeListener {

    public static final String TAG="SeekBarPref";

    private int defaultValue;
    private int maxValue;
    private int minValue;
    private int currentValue;

    private SeekBar seekBar;
    private TextView textLabel;
    private TextView text;
    /**
     * 是否改变当前的字体大小,如果是字体配置,可以显示,如果不是,就不用
     */
    boolean changeVal=true;

    public SeekBarPref(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        minValue=12;
        maxValue=26;
        defaultValue=16;

        //下面这段得不到值，只能取为最小值。可能这也是自定义的一个bug？
        TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, 0, 0);
        int dv=a.getInt(R.styleable.SeekBarPreference_defaultValue, minValue);
        currentValue=dv;
        a.recycle();
    }

    /*@Override
    protected void onSetInitialValue(final boolean restoreValue, final Object defaultVal) {
        try {
            WeiboLog.v("defaultValue:"+defaultVal);
            if (null!=defaultVal) {
                currentValue=Integer.parseInt(defaultVal.toString());
                SeekBarPref.this.defaultValue=currentValue;
            } else {
                currentValue=restoreValue ? getPersistedInt(minValue) : minValue;
            }
        } catch (NumberFormatException ex) {
            currentValue=minValue;
        }
    }*/

    /**
     * init default value
     *
     * @param defaultVal
     */
    public void setInitialValue(final int defaultVal) {
        WeiboLog.v(TAG, "setInitialValue:"+defaultVal);
        currentValue=defaultVal;
    }

    /**
     * init default value
     *
     * @param defaultVal
     * @param changeVal
     */
    public void setInitialValue(final int defaultVal, boolean changeVal, final int minVal, final int maxVal) {
        WeiboLog.v(TAG, "setInitialValue:"+defaultVal);
        currentValue=defaultVal;
        this.changeVal=changeVal;
        minValue=minVal;
        maxValue=maxVal;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        seekBar=(SeekBar) view.findViewById(R.id.pref_seek_bar);
        seekBar.setMax(maxValue-minValue);
        seekBar.setProgress(currentValue-minValue);
        seekBar.setKeyProgressIncrement(1);
        seekBar.setOnSeekBarChangeListener(this);

        textLabel=(TextView) view.findViewById(R.id.label);
        text=(TextView) view.findViewById(R.id.pref_seek_current_value);

        setFontSize();
        //text.setTextSize(currentValue);
        text.setText(Integer.toString(currentValue));
    }

    @Override
    public CharSequence getSummary() {
        final String summary=super.getSummary().toString();
        int value=minValue;
        try {
            value=(getPersistedInt((defaultValue)));
        } catch (NumberFormatException ex) {
        }
        //Log.d(TAG, "setSummary:"+value);
        return String.format(summary, value);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        text.setText(String.valueOf(summary));
        //Log.d(TAG, "setSummary:"+currentValue);
    }

    @Override
    public void onProgressChanged(final SeekBar seek, final int value, final boolean fromTouch) {
        //Log.d(TAG, "onProgressChanged.currentValue:"+currentValue+" val:"+value);
        currentValue=value+minValue;
        setFontSize();
        text.setText(Integer.toString(currentValue));
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
        //Log.d(TAG, "onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        //Log.d(TAG, "onStopTrackingTouch.currentValue:"+currentValue);
        persistInt(currentValue);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    void setFontSize() {
        if (changeVal) {
            textLabel.setTextSize(currentValue);
        }
    }
}
