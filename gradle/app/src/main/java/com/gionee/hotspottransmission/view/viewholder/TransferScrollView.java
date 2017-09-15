package com.gionee.hotspottransmission.view.viewholder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by luorw on 8/7/17.
 */

public class TransferScrollView extends ScrollView {
    public TransferScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //不拦截，继续分发下去
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
