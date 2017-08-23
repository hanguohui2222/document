package com.gionee.secretary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.EditText;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Desc:
 * User: tiansj
 */
public class PasswordInputView extends EditText {
    private static final String TAG = "PasswordInput";
    private static final int defaultContMargin = 5;
    private static final int defaultSplitLineWidth = 3;

    private int passwordLength = 6;
    private int passwordColor = 0xFFCCCCCC;
    private float passwordWidth = 8;
    private float passwordRadius = 3;

    private Paint passwordPaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint emptyPasswordPaint = new Paint(ANTI_ALIAS_FLAG);
    private int textLength;

    public PasswordInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        passwordLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, passwordLength, dm);
        passwordWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, passwordWidth, dm);
        passwordRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, passwordRadius, dm);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
        passwordLength = a.getInt(R.styleable.PasswordInputView_pivPasswordLength, passwordLength);
        passwordColor = a.getColor(R.styleable.PasswordInputView_pivPasswordColor, passwordColor);
        passwordWidth = a.getDimension(R.styleable.PasswordInputView_pivPasswordWidth, passwordWidth);
        passwordRadius = a.getDimension(R.styleable.PasswordInputView_pivPasswordRadius, passwordRadius);
        a.recycle();

        passwordPaint.setStrokeWidth(passwordWidth);
        passwordPaint.setStyle(Paint.Style.FILL);
        passwordPaint.setColor(passwordColor);

        // 设置paint的外框宽度
        emptyPasswordPaint.setStrokeWidth(3);
        emptyPasswordPaint.setStyle(Paint.Style.STROKE);
        emptyPasswordPaint.setColor(getResources().getColor(R.color.emptyPassword));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        LogUtils.i(TAG, ".....onDraw....width:" + width + ", height:" + height + ",passwordLength:" + passwordLength + ",textLength:" + textLength);
        // 空心圆
        float cx0, cy0 = height / 2;
        float half0 = width / passwordLength / 2;
        for (int i = 0; i < passwordLength; i++) {
            cx0 = width * i / passwordLength + half0;
            canvas.drawCircle(cx0, cy0, passwordWidth - 3, emptyPasswordPaint);
            LogUtils.i(TAG, ".....onDraw....drawCircle...1");
        }

        // 密码
        float cx, cy = height / 2;
        float half = width / passwordLength / 2;
        for (int i = 0; i < textLength; i++) {
            cx = width * i / passwordLength + half;
            canvas.drawCircle(cx, cy, passwordWidth, passwordPaint);
            LogUtils.i(TAG, ".....onDraw....drawCircle...2");
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        this.textLength = text.toString().length();
        LogUtils.i(TAG, "onTextChanged.....textLength:" + textLength);
        invalidate();

        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        invalidate();
    }

    public int getPasswordColor() {
        return passwordColor;
    }

    public void setPasswordColor(int passwordColor) {
        this.passwordColor = passwordColor;
        passwordPaint.setColor(passwordColor);
        invalidate();
    }

    public float getPasswordWidth() {
        return passwordWidth;
    }

    public void setPasswordWidth(float passwordWidth) {
        this.passwordWidth = passwordWidth;
        passwordPaint.setStrokeWidth(passwordWidth);
        invalidate();
    }

    public float getPasswordRadius() {
        return passwordRadius;
    }

    public void setPasswordRadius(float passwordRadius) {
        this.passwordRadius = passwordRadius;
        invalidate();
    }
}
