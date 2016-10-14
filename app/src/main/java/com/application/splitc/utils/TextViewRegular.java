package com.application.splitc.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class TextViewRegular extends TextView {

    public TextViewRegular(Context context) {
        super(context);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
    }

    public TextViewRegular(Context context, AttributeSet attr) {
        super(context,attr);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
    }

    public TextViewRegular(Context context, AttributeSet attr, int i) {
        super(context,attr,i);
        setTypeface(CommonLib.getTypeface(context, CommonLib.FONT_REGULAR));
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
    }
}