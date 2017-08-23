package com.gionee.secretary.ui.viewInterface;

import java.util.Calendar;

public interface ICalendarView {
    void onBackPressed();

    void upDateMonthInfo(String monthInfo);

    void showSetTimeDialog();

    void updateWeekView(int position);

    void updateWeekViewByOther(int position);

    void updateSchedule(Calendar c);

    void upDateWeekScheduleInfo();

    void onScheduleDeleted();

    void updateCurrentWeekView();
}
