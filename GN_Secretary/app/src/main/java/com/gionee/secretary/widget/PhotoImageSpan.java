package com.gionee.secretary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.TypedValue;

public class PhotoImageSpan extends ImageSpan {
    private Context mContext;

    public PhotoImageSpan(Context context, Bitmap b) {
        super(context, b);
        mContext = context;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();
        if (fm != null) {
            FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;
            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;

            fm.ascent = -bottom;
            fm.top = -bottom;
            fm.bottom = top;
            fm.descent = top;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int disp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics());
        top -= disp;
        bottom -= disp;
        Drawable b = getDrawable();
        canvas.save();
        int transY = 0;
        transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
        canvas.translate(x, transY);
        if (b instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) b;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (!bitmap.isRecycled()) {
                b.draw(canvas);
            }
        }
        canvas.restore();
    }

    public void recycle() {
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmapDrawable.getBitmap().recycle();
        }
    }

}
