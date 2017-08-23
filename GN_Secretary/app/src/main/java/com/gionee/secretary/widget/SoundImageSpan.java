package com.gionee.secretary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;

import com.gionee.secretary.R;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.TextUtilTools;

/**
 * Created by hangh on 1/11/17.
 */
public class SoundImageSpan extends ImageSpan {
    private Bitmap mDot;
    private Bitmap mPlay;
    private final String mPath;
    private final Context mContext;
    /*
     * add by zhengjl at 2017-1-16 小圆点的top值和left值
     */
    private static final int DOT_LEFT_VALUE = 8;
    private static final int DOT_TOP_VALUE = 15;
    private static final int TIME_TEXT_LEFT = 22;
    private static final int PLAT_ICON_TEXT_LEFT = 291;
    private MediaPlayer mp;

    public SoundImageSpan(final Context context, Bitmap bitmap, String path) {
        super(context, bitmap);
        mPath = path;
        mContext = context;
        mp = MediaPlayer.create(context, Uri.parse(mPath));
        mPlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_icon);
        mDot = BitmapFactory.decodeResource(context.getResources(), R.drawable.dot);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int disp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics());
        top -= disp;
        bottom -= disp;
        LogUtils.d("hangh", "text = " + text);
        LogUtils.d("hangh", "start = " + start);
        LogUtils.d("hangh", "end = " + end);
        LogUtils.d("hangh", "top = " + top);
        LogUtils.d("hangh", "x = " + x);
        LogUtils.d("hangh", "y = " + y);
        LogUtils.d("hangh", "bottom = " + bottom);
        LogUtils.d("hangh", "mPath = " + mPath);
        int time = 0;
        if (mp != null) {
            time = mp.getDuration() / 1000;
        } else {
            time = VoiceNoteDao.getInstance(mContext).getRecordBean(mPath).getTime();
        }
        LogUtils.d("hangh", "time = " + time);
        String timestr = TextUtilTools.formatTime(time, ":");
        canvas.save();
        paint.setColor(Color.parseColor("#f2f2f2"));
        float bg_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        float bg_right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 332, mContext.getResources().getDisplayMetrics());
        float padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, mContext.getResources().getDisplayMetrics());
        canvas.drawRect(bg_left, top + padding, bg_right, bottom - padding, paint);
        paint.setColor(0xdd000000);
        Rect bounds = new Rect();
        paint.getTextBounds(timestr, 0, timestr.length(), bounds);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (bottom - top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        int text_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TIME_TEXT_LEFT, mContext.getResources().getDisplayMetrics());
        canvas.drawText(timestr, text_left, baseline + top, paint);
        int height = mPlay.getHeight();
        int dotHeight = mDot.getHeight();
        LogUtils.d("hangh", "getIntrinsicWidth = " + getDrawable().getIntrinsicWidth());
        float dot_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DOT_LEFT_VALUE, mContext.getResources().getDisplayMetrics());
        canvas.drawBitmap(mDot, dot_left, (bottom + top - dotHeight) / 2, paint);
        float play_icon_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PLAT_ICON_TEXT_LEFT, mContext.getResources().getDisplayMetrics());
        canvas.drawBitmap(mPlay, play_icon_left, (bottom + top - height) / 2, paint);
        canvas.restore();
    }

    public void recycle() {
        if (mDot != null && !mDot.isRecycled()) {
            mDot.recycle();
            mDot = null;
        }
        if (mPlay != null && !mPlay.isRecycled()) {
            mPlay.recycle();
            mPlay = null;
        }
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmapDrawable.getBitmap().recycle();
        }
    }
}
