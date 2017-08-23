package com.gionee.secretary.widget;

import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.utils.DensityUtils;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;

public class ScrollLayout extends FrameLayout {
    private ViewDragHelper mViewDragHelper;
    private FrameLayout monthView;
    //	private LinearLayout mainLayout;
    private int orignalY;
    private int layoutTop;
    private int lastX;
    private int mTouchSlop;
    //	private float lineHeight;
    private CalendarManager mCalendarManager;
    private ScrollLayoutCallback mScrollLayoutCallback;
    public static final int WEEK_VIEW_VISIBLE = 0;
    public static final int WEEK_VIEW_INVISIBLE = 1;
    private Context context;
    public static final int MONTH_TOP_DIP = 180;//monthHeigh*5/6
    private static int MONTH_TOP_PX;
    public static final int WEEK_HEIGHT_DIP = 38;//monthHeigh/6
    //	public static int WEEK_HEIGHT_PX;
    private MainLayout mainLayout;
    private RecyclerView scheduleView;
    private LinearLayout ll;
    private int redY;
//	private FrameLayout weekView;

    public ScrollLayout(Context context) {
        super(context);
        init(context);
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        MONTH_TOP_PX = DensityUtils.dip2px(context, MONTH_TOP_DIP);
//		WEEK_HEIGHT_PX = DensityUtils.dip2px(context, WEEK_HEIGHT_DIP);
        redY = DensityUtils.dip2px(context, 10);
        layoutTop = -MONTH_TOP_PX - redY;
        mCalendarManager = CalendarManager.getInstance();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//		initChildViewDragHelper();
        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(View arg0, int arg1) {
//				LogUtils.d("liyu", "-tryCaptureView monthView.getHeight()/6*5 = "+-monthView.getHeight()/6*5)
                return arg0 == mainLayout;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (top >= orignalY) {
                    return orignalY;
                } else {
                    if (top < MONTH_TOP_PX + redY) {
                        return Math.max(top, -MONTH_TOP_PX - redY);
                    } else {
                        return Math.max(top, -scheduleView.getMeasuredHeight());
                    }
                }
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                layoutTop = top <= -monthView.getHeight() * 5 / 6 ? -monthView.getHeight() * 5 / 6 : top;
                if (mScrollLayoutCallback == null) {
                    return;
                }
                mScrollLayoutCallback.onCalendarScrolled(dy);
                int line = mCalendarManager.getLine();
//				LogUtils.d("onViewPositionChanged", "-MONTH_TOP_PX = "+(-MONTH_TOP_PX));
                if (top > -MONTH_TOP_PX) {
                    mScrollLayoutCallback.setWeekViewClickable(false);
                } else {
                    mScrollLayoutCallback.setWeekViewClickable(true);
                }
                if (top <= -MONTH_TOP_PX / 5 * (line - 1) && dy < 0) {
                    mScrollLayoutCallback.setWeekViewVisibilty(View.VISIBLE);
                } else if (top >= -MONTH_TOP_PX / 5 * (line - 1) && dy > 0) {
                    mScrollLayoutCallback.setWeekViewVisibilty(View.INVISIBLE);
                }

            }

            Context context;

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//				LogUtils.d("onViewReleased", "releasedChild.getTop() = "+releasedChild.getTop());
//				LogUtils.d("onViewReleased", "lineHeight = "+monthView.getHeight());
                if (yvel >= 1000) {
                    mViewDragHelper.settleCapturedViewAt(0, orignalY);
                } else if (yvel <= -1000) {
                    mViewDragHelper.settleCapturedViewAt(0, -MONTH_TOP_PX - redY);
                } else {
                    if (releasedChild.getTop() > -monthView.getHeight() * 5 / 12) {//down
                        mViewDragHelper.settleCapturedViewAt(0, orignalY);
                    } else if (releasedChild.getTop() < -monthView.getHeight() * 5 / 12) {//up
                        mViewDragHelper.settleCapturedViewAt(0, -MONTH_TOP_PX - redY);
                    }
                }
                ScrollLayout.this.invalidate();
                //---------------------------------------------------
                context = ScrollLayout.this.context;
//				LogUtils.d("liyu", "monthView.getHeight()/6 = "+monthView.getHeight()/6);
//				LogUtils.d("liyu", "dp DensityUtils.px2dip(context, monthView.getHeight()/6) = "+DensityUtils.px2dip(context, monthView.getHeight()/6));
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return monthView.getHeight();
            }

        });
    }


    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            postInvalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        monthView = (FrameLayout) findViewById(R.id.month_view);
        mainLayout = (MainLayout) findViewById(R.id.main_layout);
        scheduleView = (RecyclerView) findViewById(R.id.rv);
        ll = (LinearLayout) findViewById(R.id.ll);
        orignalY = monthView.getTop();
//		new Handler().postDelayed(new Runnable(){
//
//			@Override
//			public void run() {
//				LogUtils.d("liyu", "monthView.getHeight()/6 = "+monthView.getHeight()/6);
//				LogUtils.d("liyu", "dp DensityUtils.px2dip(context, monthView.getHeight()/6) = "+DensityUtils.px2dip(context, monthView.getHeight()/6));
//			}
//			
//		}, 3000);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mainLayout.layout(0, layoutTop, mainLayout.getMeasuredWidth(), mainLayout.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((int) Math.abs(lastX - ev.getX()) >= mTouchSlop) {
                    return false;
                }
                break;
        }
        float y = ev.getY();
        if (y > mainLayout.getMeasuredHeight() + layoutTop && !b2) {
//			return childViewDragHelper.shouldInterceptTouchEvent(ev);
            return false;
        } else
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    boolean flag = true;
    boolean b;
    boolean b2;
    float diffY;

    float lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        if (y > mainLayout.getMeasuredHeight() + layoutTop && !b2) {
//			if (lastY != 0){
//				mViewDragHelper.smoothSlideViewTo(scheduleView, 0, (int)(y-lastY));
//				LogUtils.d("liyu", "(int)(y-lastY) = "+(int)(y-lastY));
//			}
//			lastY = y;
//			if (event.getAction() == MotionEvent.ACTION_UP){
//				lastY = 0;
//			}
//			ScrollLayout.this.invalidate();
//			childViewDragHelper.processTouchEvent(event);
            return false;
        }
        if (flag) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (diffY == 0) {
                    event.setAction(MotionEvent.ACTION_DOWN);
                    diffY = monthView.getBottom() - event.getY();
                } else {
                    b = true;
                }
                if (b) {
                    event.setAction(MotionEvent.ACTION_UP);
                    flag = false;
                }
                event.setLocation(event.getX(), event.getY() + diffY);
                b2 = true;

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                flag = false;
                b2 = false;
            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_MOVE) b2 = true;
            else if (event.getAction() == MotionEvent.ACTION_UP) b2 = false;
        }
        try {
            mViewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            LogUtils.e("liyu", "axiba");
        }
        return true;
    }

    //	 重写 onMeasure 支持 wrap_content
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 计算所有childview的宽高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        // 计算warp_content的时候的高度
        int wrapHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            wrapHeight += childHeight;
        }
        setMeasuredDimension(widthSize, heightMode == MeasureSpec.EXACTLY ? heightSize : wrapHeight);
    }

//	@Override
//	protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
//		ViewGroup.LayoutParams lp = child.getLayoutParams();
//
//		int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width);
//
//		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//
//		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//	}
//
//	@Override
//	protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
//		final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
//
//		final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
//		final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
//
//		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//	}


    public interface ScrollLayoutCallback {
        void setWeekViewVisibilty(int visibility);

        void setWeekViewClickable(boolean b);

        void onCalendarScrolled(int dy);
    }

    public void setScrollLayoutCallback(ScrollLayoutCallback scrollLayoutCallback) {
        this.mScrollLayoutCallback = scrollLayoutCallback;
    }

}
