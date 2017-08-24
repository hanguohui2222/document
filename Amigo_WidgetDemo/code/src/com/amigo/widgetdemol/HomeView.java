package com.amigo.widgetdemol;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class HomeView extends FrameLayout {
    private ImageView mUpView;
    private ImageView mIconView;
    private int mUpWidth;
    private int mStartOffset;
    private int mUpIndicatorRes;
    private Drawable mDefaultUpIndicator;
    private Drawable mUpIndicator;

    private static final long DEFAULT_TRANSITION_DURATION = 150;

    public HomeView(Context context) {
        this(context, null);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutTransition t = getLayoutTransition();
        if (t != null) {
            // Set a lower duration than the default
            t.setDuration(DEFAULT_TRANSITION_DURATION);
        }
    }

    public void setShowUp(boolean isUp) {
        mUpView.setVisibility(isUp ? VISIBLE : GONE);
    }

    public void setShowIcon(boolean showIcon) {
        mIconView.setVisibility(showIcon ? VISIBLE : GONE);
    }

    public void setIcon(Drawable icon) {
        mIconView.setImageDrawable(icon);
    }

    public void setUpIndicator(Drawable d) {
        mUpIndicator = d;
        mUpIndicatorRes = 0;
        updateUpIndicator();
    }

    public void setDefaultUpIndicator(Drawable d) {
        mDefaultUpIndicator = d;
        updateUpIndicator();
    }

    public void setUpIndicator(int resId) {
        mUpIndicatorRes = resId;
        mUpIndicator = null;
        updateUpIndicator();
    }

    private void updateUpIndicator() {
        if (mUpIndicator != null) {
            mUpView.setImageDrawable(mUpIndicator);
        } else if (mUpIndicatorRes != 0) {
            mUpView.setImageDrawable(getContext().getDrawable(mUpIndicatorRes));
        } else {
            mUpView.setImageDrawable(mDefaultUpIndicator);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mUpIndicatorRes != 0) {
            // Reload for config change
            updateUpIndicator();
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        final CharSequence cdesc = getContentDescription();
        if (!TextUtils.isEmpty(cdesc)) {
            event.getText().add(cdesc);
        }
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // Don't allow children to hover; we want this to be treated as a single component.
        return onHoverEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        mUpView = (ImageView) findViewById(R.id.amigo_up);
        mIconView = (ImageView) findViewById(R.id.amigo_home);
        mDefaultUpIndicator = mUpView.getDrawable();
    }

    public int getStartOffset() {
        return mUpView.getVisibility() == GONE ? mStartOffset : 0;
    }

    public int getUpWidth() {
        return mUpWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildWithMargins(mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        final LayoutParams upLp = (LayoutParams) mUpView.getLayoutParams();
        final int upMargins = upLp.leftMargin + upLp.rightMargin;
        mUpWidth = mUpView.getMeasuredWidth();
        mStartOffset = mUpWidth + upMargins;
        int width = mUpView.getVisibility() == GONE ? 0 : mStartOffset;
        int height = upLp.topMargin + mUpView.getMeasuredHeight() + upLp.bottomMargin;

        if (mIconView.getVisibility() != GONE) {
            measureChildWithMargins(mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
            final LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
            width += iconLp.leftMargin + mIconView.getMeasuredWidth() + iconLp.rightMargin;
            height = Math.max(height,
                    iconLp.topMargin + mIconView.getMeasuredHeight() + iconLp.bottomMargin);
        } else if (upMargins < 0) {
            // Remove the measurement effects of negative margins used for offsets
            width -= upMargins;
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                width = Math.min(width, widthSize);
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                break;
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                height = Math.min(height, heightSize);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int vCenter = (b - t) / 2;
        final boolean isLayoutRtl = false;//isLayoutRtl();
        final int width = getWidth();
        int upOffset = 0;
        if (mUpView.getVisibility() != GONE) {
            final LayoutParams upLp = (LayoutParams) mUpView.getLayoutParams();
            final int upHeight = mUpView.getMeasuredHeight();
            final int upWidth = mUpView.getMeasuredWidth();
            upOffset = upLp.leftMargin + upWidth + upLp.rightMargin;
            final int upTop = vCenter - upHeight / 2;
            final int upBottom = upTop + upHeight;
            final int upRight;
            final int upLeft;
            if (isLayoutRtl) {
                upRight = width;
                upLeft = upRight - upWidth;
                r -= upOffset;
            } else {
                upRight = upWidth;
                upLeft = 0;
                l += upOffset;
            }
            mUpView.layout(upLeft, upTop, upRight, upBottom);
        }

        final LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
        final int iconHeight = mIconView.getMeasuredHeight();
        final int iconWidth = mIconView.getMeasuredWidth();
        final int hCenter = (r - l) / 2;
        final int iconTop = Math.max(iconLp.topMargin, vCenter - iconHeight / 2);
        final int iconBottom = iconTop + iconHeight;
        final int iconLeft;
        final int iconRight;
        int marginStart = iconLp.getMarginStart();
        final int delta = Math.max(marginStart, hCenter - iconWidth / 2);
        if (isLayoutRtl) {
            iconRight = width - upOffset - delta;
            iconLeft = iconRight - iconWidth;
        } else {
            iconLeft = upOffset + delta;
            iconRight = iconLeft + iconWidth;
        }
        mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
    }
}
