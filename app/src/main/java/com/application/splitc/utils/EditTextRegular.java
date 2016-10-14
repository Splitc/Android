package com.application.splitc.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class EditTextRegular extends EditText {
    public EditTextRegular(Context context) {
        super(context);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
    }

    public EditTextRegular(Context context, AttributeSet attr) {
        super(context,attr);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
    }

    public EditTextRegular(Context context, AttributeSet attr, int i) {
        super(context,attr,i);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
    }
}