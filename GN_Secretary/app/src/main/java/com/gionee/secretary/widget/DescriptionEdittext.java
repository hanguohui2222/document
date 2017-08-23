package com.gionee.secretary.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import amigoui.widget.AmigoEditText;

/**
 * Created by liyy on 16-6-20.
 */
public class DescriptionEdittext extends AmigoEditText {


    public DescriptionEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DescriptionEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DescriptionEdittext(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }
}
