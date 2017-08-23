package com.gionee.secretary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import com.gionee.secretary.utils.LogUtils;

public class BillImageSpan extends ImageSpan {

    private int distance = 0;

    public BillImageSpan(Context context, Bitmap b) {
        super(context, b);
        // TODO Auto-generated constructor stub
    }

//	public NoteImageSpan(Context context, int resourceId) {
//		super(context, resourceId);
//	}

	 /*public int getSize(Paint paint, CharSequence text, int start, int end,
             FontMetricsInt fm) {  
         Drawable d = getDrawable();  
         Rect rect = d.getBounds();  
         if (fm != null) {  
             FontMetricsInt fmPaint=paint.getFontMetricsInt();  
             int fontHeight = fmPaint.bottom - fmPaint.top;  
             int drHeight=rect.bottom-rect.top;  
               
             int top= drHeight/2 - fontHeight/4;  
             int bottom=drHeight/2 + fontHeight/4;  
               
             fm.ascent=-bottom;  
             fm.top=-bottom;  
             fm.bottom=top;  
             fm.descent=top;  
         }  
         return rect.right;  
     }  */

    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Rect localRect = getDrawable().getBounds();
        Paint.FontMetricsInt localFontMetricsInt = new Paint.FontMetricsInt();
        if (fm != null) {
            fm.ascent = localFontMetricsInt.ascent;
            fm.descent = localFontMetricsInt.descent;
            fm.top = localFontMetricsInt.top;
            fm.bottom = localFontMetricsInt.bottom;
            LogUtils.d("NoteImageSpan", "localFontMetricsInt.top = " + localFontMetricsInt.top);
            LogUtils.d("NoteImageSpan", "localFontMetricsInt.bottom = " + localFontMetricsInt.bottom);
        }
        return localRect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();
        int transY = 0;
//         if(bottom > top) {
//             distance = bottom - top;
//         }
         /*
         if(top == bottom){
             bottom = top + 65;
         }*/
        if (top == y) {
            y = top + 51;
        }
        int height = b.getBounds().bottom - b.getBounds().top;
        //transY = ((bottom-top) - b.getBounds().bottom)/2+top-40;
        LogUtils.d("NoteImageSpan", "bottom = " + bottom);
        LogUtils.d("NoteImageSpan", "top = " + top);
        LogUtils.d("NoteImageSpan", "b.getBounds() = " + b.getBounds());
        LogUtils.d("NoteImageSpan", "b.getIntrinsicHeight() = " + b.getIntrinsicHeight());
        //transY = (bottom + top - height)/2;
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        transY = (y + fm.descent + y + fm.ascent) / 2
                - b.getBounds().bottom / 2;
        LogUtils.d("NoteImageSpan", "transY = " + transY);
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

}
