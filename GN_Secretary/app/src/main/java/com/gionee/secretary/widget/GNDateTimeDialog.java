package com.gionee.secretary.widget;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoDatePicker;
import amigoui.widget.AmigoTimePicker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
//import amigo.widget.AmigoDatePicker;
//import amigo.widget.AmigoDatePicker.OnDateChangedListener;

import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
//import amigo.widget.AmigoTimePicker;
//import amigo.widget.AmigoTimePicker.OnTimeChangedListener;

import android.widget.TimePicker;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.GNCalendarUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.TimeUtils;


public class GNDateTimeDialog extends AmigoAlertDialog implements OnClickListener {

    private static final String TAG = "GNDateTimeDialogDebug";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";
    private static final String TIME_FORMAT_1 = "yyyy/MM/dd";
    private static final String TIME_FORMAT_2 = "HH:mm";

    private Context mContext;
    private TextView mDateView;
    private TextView mLunarDateView;
    private LinearLayout mDateViewLayout;
    private TextView mTimeView;
    private AmigoDatePicker mDatePicker;
    private AmigoTimePicker mTimePicker;
    private final OnDateTimeSetListener mDateTimeCallback;
    private Calendar mCalendar;
    private Date mTextDate;

    private int mYear;
    private int mMonthOfYear;
    private int mDayOfMonth;
    private int mHourOfDay;
    private int mMinute;
    //    private boolean mIs24HourView;
    private long mTime = -1;

    public interface OnDateTimeSetListener {
        /*
         * view The view associated with this listener. year The year that was
         * set monthOfYear The month that was set (0-11) for compatibility
         * dayOfMonth The day of the month that was set. hourOfDay The hour that
         * was set. minute The minute that was set.
         */
        void onDateTimeSet(Calendar calendar);
    }

    public GNDateTimeDialog(Context context, OnDateTimeSetListener datetimecallBack, Calendar calendar,
                            boolean isShowTime) {
        this(context, AmigoAlertDialog.THEME_HOLO_DARK, datetimecallBack, calendar, 0, isShowTime);
    }

    public GNDateTimeDialog(Context context, int theme, OnDateTimeSetListener datetimecallBack,
                            Calendar calendar, long minTime, boolean isShowTime) {
        super(context);
        mContext = context;
        mDateTimeCallback = datetimecallBack;
        if (calendar == null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 10 * 60 * 1000);
        }
        mYear = calendar.get(Calendar.YEAR);
        mMonthOfYear = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        mCalendar = calendar;

        setButton(BUTTON_POSITIVE, getContext().getText(R.string.time_picker_setting), this);
        setButton(BUTTON_NEGATIVE, getContext().getText(R.string.time_picker_cancel), this);
        setIcon(0);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_gn_date_time_picker, null);

        initTextView(view, minTime);
        initVisible(view, isShowTime);
        setView(view);
    }

    private void initVisible(View view, boolean isShowTime) {
        if (!isShowTime) {
            view.findViewById(R.id.picker_title).setVisibility(View.GONE);
            view.findViewById(R.id.gn_time_picker).setVisibility(View.GONE);
            view.findViewById(R.id.gn_date_picker).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.picker_title).setVisibility(View.VISIBLE);
            view.findViewById(R.id.gn_time_picker).setVisibility(View.VISIBLE);
            view.findViewById(R.id.gn_date_picker).setVisibility(View.GONE);
        }
    }

    private void updateDate() {
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
        LogUtils.i(TAG, "updateDate time=" + mCalendar.getTimeInMillis());
        mTextDate = mCalendar.getTime();
    }

    private void initTextView(View view, long minTime) {
        mTextDate = mCalendar.getTime();
        mDateView = (TextView) view.findViewById(R.id.picker_date_text);
        mLunarDateView = (TextView) view.findViewById(R.id.picker_date_lunar_text);
        mDateViewLayout = (LinearLayout) view.findViewById(R.id.picker_date_layout);
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_1);
        dateFormat.setTimeZone(mCalendar.getTimeZone());
        mDateView.setText(dateFormat.format(mTextDate));
        mLunarDateView.setText(TimeUtils.getLunarDate(mContext, mTextDate.getTime()));
        mDateViewLayout.setOnClickListener(mClickListener);
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_2);
        timeFormat.setTimeZone(mCalendar.getTimeZone());
        mTimeView = (TextView) view.findViewById(R.id.picker_time_text);
        mTimeView.setText(timeFormat.format(mTextDate));
        mTimeView.setOnClickListener(mClickListener);

        mDatePicker = (AmigoDatePicker) view.findViewById(R.id.gn_date_picker);
        setDatePickerRange();
        mDatePicker.init(mYear, mMonthOfYear, mDayOfMonth, new AmigoDatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(AmigoDatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                updateDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_1);
                dateFormat.setTimeZone(mCalendar.getTimeZone());
                mDateView.setText(dateFormat.format(mTextDate));
                mLunarDateView.setText(TimeUtils.getLunarDate(mContext, mTextDate.getTime()));
            }
        });
        mCalendar.clear();
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
        mTimePicker = (AmigoTimePicker) view.findViewById(R.id.gn_time_picker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setCurrentHour(mHourOfDay);
        mTimePicker.setCurrentMinute(mMinute);
        mTimePicker.setOnTimeChangedListener(new AmigoTimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(AmigoTimePicker view, int hourOfDay, int minute) {
                mHourOfDay = hourOfDay;
                mMinute = minute;
                updateDate();
                SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_2);
                timeFormat.setTimeZone(mCalendar.getTimeZone());
                mTimeView.setText(timeFormat.format(mTextDate));
            }
        });

    }

    private void setDatePickerRange() {
        Time minTime = new Time();
        minTime.set(0, 0, 0, 1, 0, 1970);
        Time maxTime = new Time();
        maxTime.set(59, 59, 23, 31, 11, 2036);// 2037/12/31
        long maxDate = GNCalendarUtils.toMillis(maxTime, true);
        maxDate = maxDate + 999;// in millsec
        long minDate = GNCalendarUtils.toMillis(minTime, true);
        mDatePicker.setMinDate(minDate);
        mDatePicker.setMaxDate(maxDate);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.picker_date_layout:
                    handleDatePicker();
                    break;
                case R.id.picker_time_text:
                    handleTimePicker();
                    break;

                default:
                    break;
            }
        }

        private void handleTimePicker() {
            mDatePicker.setVisibility(View.GONE);
            mTimePicker.setVisibility(View.VISIBLE);
            mDateViewLayout.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.time_picker_light_bg));
            mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.time_picker_selected_bg));
            mDateView.setTextColor(mContext.getResources().getColor(R.color.time_picker_unselected_color));
            mLunarDateView.setTextColor(mContext.getResources().getColor(R.color.time_picker_unselected_color));
            mTimeView.setTextColor(mContext.getResources().getColor(R.color.time_picker_selected_color));
        }

        private void handleDatePicker() {
            mDatePicker.setVisibility(View.VISIBLE);
            mTimePicker.setVisibility(View.GONE);
            mDateViewLayout.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.time_picker_selected_bg));
            mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.time_picker_light_bg));
            mDateView.setTextColor(mContext.getResources().getColor(R.color.time_picker_selected_color));
            mLunarDateView.setTextColor(mContext.getResources().getColor(R.color.time_picker_selected_color));
            mTimeView.setTextColor(mContext.getResources().getColor(R.color.time_picker_unselected_color));
        }
    };

    private class DateChangedListener implements AmigoDatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(AmigoDatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonthOfYear = monthOfYear;
            mDayOfMonth = dayOfMonth;
            updateDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_1);
            dateFormat.setTimeZone(mCalendar.getTimeZone());
            mDateView.setText(dateFormat.format(mTextDate));
            mLunarDateView.setText(TimeUtils.getLunarDate(mContext, mTextDate.getTime()));
        }

//        @Override
//        public void onDateChanged(AmigoDatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            mYear = year;
//            mMonthOfYear = monthOfYear;
//            mDayOfMonth = dayOfMonth;
//            updateDate();
//            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_1);
//            dateFormat.setTimeZone(mCalendar.getTimeZone());
//            mDateView.setText(dateFormat.format(mTextDate));
//        }
    }

//    @Override
//    public void onTimeChanged(AmigoTimePicker view, int hourOfDay, int minute) {
//
//        mHourOfDay = hourOfDay;
//        mMinute = minute;
//        updateDate();
//        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_2);
//        timeFormat.setTimeZone(mCalendar.getTimeZone());
//        mTimeView.setText(timeFormat.format(mTextDate));
//    }

    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case BUTTON_POSITIVE:
                handlePositiveBtn();

                break;
            case BUTTON_NEGATIVE:
                if (mTime != -1) {
                    mCalendar.setTimeInMillis(mTime);
                }
                break;

            default:
                break;
        }
        dismiss();
    }

    private void handlePositiveBtn() {
        LogUtils.i(TAG, "click button positive!");

        long currentTime = System.currentTimeMillis();
        LogUtils.d(TAG, "mCalendar.getTimeInMillis: " + mCalendar.getTimeInMillis()
                + ", System.currentTimeMillis: " + currentTime);

        long timeToSetMinute = mCalendar.getTimeInMillis() / 60000;
        long timeCurrentMinute = currentTime / 60000;
        LogUtils.d(TAG, "mCalendar.getTimeInMillis/60000: " + timeToSetMinute
                + ", System.currentTimeMillis/60000: " + timeCurrentMinute);

        // begin
        if (mDatePicker.getVisibility() == View.VISIBLE) {
            mDatePicker.clearFocus();
        } else if (mTimePicker.getVisibility() == View.VISIBLE) {
            mTimePicker.clearFocus();
        }

        if (mDateTimeCallback != null) {
            mDateTimeCallback.onDateTimeSet(mCalendar);
            mTime = mCalendar.getTimeInMillis();
        }
    }

    public void updateDate(Calendar calendar) {
        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minutOfHour);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());

        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, new DateChangedListener());

        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }

    private void setDialogNotShow() {
        try {
            Field field = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
        super.onBackPressed();
    }

    public void hideDayPicker() {
        Class<? extends AmigoDatePicker> c = mDatePicker.getClass();
        try {
            Field fd = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                fd = c.getDeclaredField("mDaySpinner");
            } else {
                fd = c.getDeclaredField("mDayPicker");
            }
            fd.setAccessible(true);
            View vd = (View) fd.get(mDatePicker);
            vd.setVisibility(View.GONE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
