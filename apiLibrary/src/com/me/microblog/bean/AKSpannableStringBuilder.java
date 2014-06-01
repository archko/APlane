package com.me.microblog.bean;

import android.text.SpannableString;

import java.io.Serializable;

/**
 * @author: archko 2014/6/1 :13:24
 */
public class AKSpannableStringBuilder extends SpannableString implements Serializable {

    public static final long serialVersionUID=3894560643019408230L;

    public AKSpannableStringBuilder(CharSequence source) {
        super(source);
    }
}
