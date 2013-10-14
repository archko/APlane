package com.me.microblog.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.me.microblog.R;

/**
 * @author: root Date: 13-4-8 Time: 下午1:41
 * @description:
 */
public class TextProgressBar extends ProgressBar {

    private String text;
    private Paint mPaint;

    Rect rect=new Rect();

    public TextProgressBar(Context context) {
        super(context);
        initText(context, null);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText(context, attrs);
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText(context, attrs);
    }

    @Override
    public void setProgress(int progress) {
        setText(progress);
        super.setProgress(progress);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
        int x=(getWidth()/2)-rect.centerX();
        int y=(getHeight()/2)-rect.centerY();
        canvas.drawText(this.text, x, y, this.mPaint);
    }

    // 初始化，画笔
    private void initText(Context context, AttributeSet attrs) {
        this.mPaint=new Paint();
        this.mPaint.setAntiAlias(true);
        int color=Color.WHITE;
        int textSize=15;
        if (null!=attrs) {
            TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.TextProgressBar);
            if (null!=a) {
                color=a.getColor(R.styleable.TextProgressBar_progress_txt_color, Color.WHITE);
                textSize=a.getDimensionPixelSize(R.styleable.TextProgressBar_progress_txt_size, 15);
                a.recycle();
            }
        }
        this.mPaint.setColor(color);
        this.mPaint.setTextSize(textSize);
    }

    // 设置文字内容
    private void setText(int progress) {
        int i=(int) ((progress*1.0f/this.getMax())*100);
        this.text=String.valueOf(i)+"%";
    }

    public void setText(String progress) {
        this.text=progress;
    }

}
