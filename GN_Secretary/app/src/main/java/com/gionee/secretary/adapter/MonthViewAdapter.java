package com.gionee.secretary.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.secretary.R;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.widget.RelativeLayout;

/**
 * Created by liyu on 16/5/12.
 */
public class MonthViewAdapter extends BaseAdapter {
    private Calendar calStartDate;// 当前显示的日历
    private int iMonthViewCurrentMonth = 0; // 当前视图月
    private CalendarManager mCalendarManager;
    private ScaleAnimation sa;
    private boolean hasAnimation;

    // 根据改变的日期更新日历
    // 填充日历控件用
    private void UpdateStartDateForMonth() {
        calStartDate.set(Calendar.DATE, 1); // 设置成当月第一天
        iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);// 得到当前日历显示的月

        // 星期一是2 星期天是1 填充剩余天数
        int iDay = 0;
        int iFirstDayOfWeek = Calendar.MONDAY;
        int iStartDay = iFirstDayOfWeek;
        if (iStartDay == Calendar.MONDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if (iDay < 0)
                iDay = 6;
        }
        if (iStartDay == Calendar.SUNDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            if (iDay < 0)
                iDay = 6;
        }
        calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);

        calStartDate.add(Calendar.DAY_OF_MONTH, -1);// 周日第一位

    }


    ArrayList<Calendar> dates;

    private ArrayList<Calendar> getDates() {

        UpdateStartDateForMonth();

        ArrayList<Calendar> alArrayList = new ArrayList<>();

        for (int i = 1; i <= 42; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(calStartDate.getTime());
            alArrayList.add(calendar);
            calStartDate.add(Calendar.DAY_OF_MONTH, 1);
        }


        Calendar calendar = alArrayList.get(6);
        if (calendar.get(Calendar.MONTH) != iMonthViewCurrentMonth) {
            for (int i = 0; i < 7; i++) {
                alArrayList.remove(0);
            }
        }
        return alArrayList;
    }

    private Activity mActivity;

    // construct
    public MonthViewAdapter(Activity a, Calendar cal) {
        mCalendarManager = CalendarManager.getInstance();
        calStartDate = cal;
        mActivity = a;
        dates = getDates();
        sa = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(300);
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Calendar getItem(int position) {
        return dates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position / 7 + 1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mActivity, R.layout.item_calendar, null);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
//        TextView tv_remarks = (TextView)view.findViewById(R.id.tv_remarks);
        ImageView iv = (ImageView) view.findViewById(R.id.iv);
        Calendar calCalendar = getItem(position);

        final int iMonth = calCalendar.get(Calendar.MONTH);
        final int iDay = calCalendar.get(Calendar.DAY_OF_WEEK);

        if (iDay == 7 || iDay == 1) {
            tv_number.setTextColor(mActivity.getResources().getColor(R.color.calender_item_number_first_or_end));
//            tv_remarks.setTextColor(mActivity.getResources().getColor(R.color.calender_item_number_remarks_first_or_end));
        }
//        tv_remarks.setText(mCalendarManager.getChineseDay(calCalendar));
        if (iMonth == iMonthViewCurrentMonth) {
        } else {
            view.setVisibility(View.INVISIBLE);
//            tv_number.setTextColor(Color.parseColor("#55000000"));
        }

        int dayOfMonth = calCalendar.get(Calendar.DAY_OF_MONTH);
        tv_number.setText("" + dayOfMonth);

        if (mCalendarManager.hasSchedule(DateUtils.formatDate2StringByMonth(calCalendar.getTime()), dayOfMonth)) {
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }

        Calendar cal = Calendar.getInstance();
        if (mCalendarManager.getCalendar().get(Calendar.YEAR) == calCalendar.get(Calendar.YEAR)
                && mCalendarManager.getCalendar().get(Calendar.DAY_OF_YEAR) == calCalendar.get(Calendar.DAY_OF_YEAR)) {
            ImageView bg = (ImageView) view.findViewById(R.id.bg);
            bg.setBackgroundResource(R.drawable.bg_calendar_selected);
            if (hasAnimation) {
                bg.startAnimation(sa);
                hasAnimation = false;
            }

        } else if (cal.get(Calendar.YEAR) == calCalendar.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == calCalendar.get(Calendar.DAY_OF_YEAR)) {
            ImageView bg = (ImageView) view.findViewById(R.id.bg);
            bg.setBackgroundResource(R.drawable.bg_calendar_today);
            tv_number.setTextColor(mActivity.getResources().getColor(R.color.month_item_number_selected));
//            tv_remarks.setTextColor(mActivity.getResources().getColor(R.color.month_item_remarks_selected));
            mCalendarManager.setTodayLine(position / 7 + 1);
        }
        return view;
    }

    public void notifyDataSetChangedWithAnimation() {
        hasAnimation = true;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

}
