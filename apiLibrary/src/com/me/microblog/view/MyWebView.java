package com.me.microblog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * @author archko
 */
public class MyWebView extends WebView {

    private int a=-1;
    private int b=-1;

    public MyWebView(Context paramContext) {
        super(paramContext);
    }

    public MyWebView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public MyWebView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    @Override
    protected void onMeasure(int paramInt1, int paramInt2) {
        super.onMeasure(paramInt1, paramInt2);
        if (this.a<=0)
            return;
        if (this.b<=0)
            return;
        int i=this.a;
        int j=this.b;
        setMeasuredDimension(i, j);
    }

    public void setMeasureSpec(int paramInt1, int paramInt2) {
        this.a=paramInt1;
        this.b=paramInt2;
    }

}
