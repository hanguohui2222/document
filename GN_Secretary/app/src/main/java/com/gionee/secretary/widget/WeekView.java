package com.gionee.secretary.widget;

import java.util.Calendar;

import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.adapter.WeekViewAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class WeekView extends GridView implements AdapterView.OnItemClickListener {
    private WeekViewAdapter mWeekTabAdapter;
    private int position;
    public static final String ARG_PAGE = "page";
    public static final String ARG_CALENDAR = "calendar";
    private Calendar targetCalendar;
    private CalendarManager mCalendarManager;
    private Calendar initCalendar;
    private Context context;
//	public static int HEIGHT;

    public WeekView(Context context) {
        super(context);
        this.context = context;
//		initView();
        // TODO Auto-generated constructor stub
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
//		initView();
        // TODO Auto-generated constructor stub
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
//		initView();
        // TODO Auto-generated constructor stub
    }

    public void initView(CalendarManager calendarManager) {
        mCalendarManager = calendarManager;
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(initCalendar.getTime());
        targetCalendar = mCalendarManager.getSelectWeekCalendar(initCalendar, position);
        mWeekTabAdapter = new WeekViewAdapter(context, targetCalendar,mCalendarManager);
        setAdapter(mWeekTabAdapter);
        setOnItemClickListener(this);
        // mEnentsViewPagerAdapter = new
        // EnentsViewPagerAdapter(getChildFragmentManager());
        // mViewPager.setAdapter(mEnentsViewPagerAdapter);
        // mViewPager.setOnPageChangeListener(new pageChangedListener());
//		setSelection();
    }

    public void setPositionAndCalendar(int position, Calendar initCalendar,CalendarManager calendarManager) {
        this.position = position;
        this.initCalendar = initCalendar;
        initView(calendarManager);
        invalidate();
    }

//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		LogUtils.d("liyu", "before onMeasure = "+this.getHeight());
//		int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
//		int measureheight = MeasureSpec.getSize(widthMeasureSpec);
//		setMeasuredDimension(measureWidth, (int) (measureWidth * 6F / 7F));
//		LogUtils.d("liyu", "after onMeasure = "+this.getHeight());
//	}

//	@Override
//	protected void onFinishInflate() {
//		HEIGHT = this.getMeasuredHeight();
//		LogUtils.d("liyu", "getMeasuredHeight = "+HEIGHT);
//	}

    public WeekViewAdapter getAdapter() {
        return mWeekTabAdapter;
    }

    public void setItemCliclkable(boolean b) {
        if (b) {
            setOnItemClickListener(this);
        } else {
            setOnItemClickListener(null);
        }
    }

    private float lastX;
    private float lastY;
    private boolean flag;
    private WeekViewCallback mWeekViewCallback;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float motionX = Math.abs(ev.getX() - lastX);
                float motionY = Math.abs(ev.getY() - lastY);
                if (motionY > motionX) {
                    flag = true;
                    if (mWeekViewCallback != null) {
                        mWeekViewCallback.dispatchTouch(ev);
                    }
                    return false;
                } else {
                    flag = true;
                    return super.dispatchTouchEvent(ev);
                }
            case MotionEvent.ACTION_UP:
                if (mWeekViewCallback != null && flag) {
                    mWeekViewCallback.dispatchTouch(ev);
                }
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);

    }


    public void setWeekViewCallback(WeekViewCallback weekViewCallback) {
        this.mWeekViewCallback = weekViewCallback;

    }

    public interface WeekViewCallback {
        void dispatchTouch(MotionEvent ev);

        void onItemClick(int position);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Calendar calendar = mWeekTabAdapter.getItem(position);
        // 这一段不懂 为什么要绕一圈  不然就bug
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        //------------------------------------------------
        mCalendarManager.setCalendar(cal);
        if (mWeekViewCallback != null) {
            mWeekViewCallback.onItemClick(this.position);
        }
    }
}
