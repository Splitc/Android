package in.splitc.share.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by neo on 29/11/16.
 */
public class IconViewV extends TextView {

    public IconViewV(Context context) {
        super(context);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsV));
    }

    public IconViewV(Context context, AttributeSet attr) {
        super(context,attr);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsV));
    }

    public IconViewV(Context context, AttributeSet attr, int i) {
        super(context,attr,i);
        setTypeface(CommonLib.getTypeface(context, CommonLib.IconsV));
    }
}