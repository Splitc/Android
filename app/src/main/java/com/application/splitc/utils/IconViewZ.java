package com.application.splitc.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by neo on 16/10/16.
 */
public class IconViewZ extends TextView {

    public IconViewZ(Context context) {
        super(context);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsZ));
    }

    public IconViewZ(Context context, AttributeSet attr) {
        super(context,attr);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsZ));
    }

    public IconViewZ(Context context, AttributeSet attr, int i) {
        super(context,attr,i);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsZ));
    }
}