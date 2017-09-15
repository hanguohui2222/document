package com.gionee.hotspottransmission.view.viewholder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.gionee.hotspottransmission.utils.LogUtil;

/**
 * Created by luorw on 8/8/17.
 */

public class TransferScrollContent extends LinearLayout {

    public TransferScrollContent(Context context) {
        super(context);
    }

    public TransferScrollContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransferScrollContent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TransferScrollContent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            height += this.getChildAt(i).getHeight();
        }
        super.onMeasure(widthMeasureSpec, height);
    }
}
