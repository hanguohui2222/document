package com.gionee.secretary.utils;

import java.util.Calendar;
import java.util.Locale;

import amigoui.app.AmigoDatePickerDialog;
import amigoui.widget.AmigoDatePicker;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.Toast;


public class GNCalendarUtils {
    private static final String LOG_TAG = "GNCalendarUtils";

    public static final String GN_PREF_NAME = "com.gionee.calendar.pref";

    public static final int MIN_YEAR_NUM = 1970;
    public static final int MAX_YEAR_NUM = 2036;
    public static final String MIN_DISPLAY_TIME = "19700101";
    public static final String MAX_DISPLAY_TIME = "20361231";

    public static boolean isYearInRange(Time t) {
        if (t == null)
            return false;

        return (t.year >= MIN_YEAR_NUM && t.year <= MAX_YEAR_NUM);
    }

    public static void correctInvalidTime(Time t) {
        if (t == null)
            return;

        if (t.year >= GNCalendarUtils.MAX_YEAR_NUM) {
            t.year = GNCalendarUtils.MAX_YEAR_NUM;
            t.month = 11;
            t.monthDay = 31;
        } else {
            t.year = GNCalendarUtils.MIN_YEAR_NUM;
            t.month = 0;
            t.monthDay = 1;
        }
        normalizeTime(t, true);
    }

    public static final class MonthName {
        private static final int MONTH_BASE = 0;

        public static final int JANURAY = MONTH_BASE;
        public static final int FEBRURAY = MONTH_BASE + 1;
    }

    // compare two Time object, if these two object indicate
    // the same date, then return true
    public static boolean isIdenticalDate(Time date1, Time date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        if (date1.year == date2.year && date1.yearDay == date2.yearDay) {
            return true;
        }

        return false;
    }

    public static boolean isIdenticalDate(long date1, long date2) {
        if (Math.abs(date1 - date2) < DateUtils.DAY_IN_MILLIS) {
            return true;
        }
        return false;
    }

    public static int compareDate(Time date1, Time date2) {
        if (date1 == null || date2 == null) {
            throw new RuntimeException("invalid null params");
        }

        int diffYear = date1.year - date2.year;
        if (diffYear != 0)
            return diffYear;

        int diffMonth = date1.month - date2.month;
        if (diffMonth != 0)
            return diffMonth;

        int diffMonthDay = date1.monthDay - date2.monthDay;
        return diffMonthDay;
    }

    public static boolean isIdenticalMonth(Time date1, Time date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        if (date1.year == date2.year && date1.month == date2.month) {
            return true;
        }

        return false;
    }

    public static int compareMonth(Time date1, Time date2) {
        if (date1 == null || date2 == null) {
            throw new RuntimeException("invalid null params");
        }

        int diffYear = date1.year - date2.year;
        if (diffYear != 0)
            return diffYear;

        int diffMonth = date1.month - date2.month;
        return diffMonth;
    }

    // compute week num of first day of specific month, 20
    public static int getWeekNumOfFirstMonthDay(Time time, int firstDayOfWeek) {
        Time temp = new Time(time);
        temp.monthDay = 1;
        long millis = GNCalendarUtils.normalizeTime(temp, true);

        int julianDay = Time.getJulianDay(millis, temp.gmtoff);
        //int weekNum = Utils.getWeeksSinceEpochFromJulianDay(julianDay, firstDayOfWeek);

        return 0;
    }

    public static Time getJulianMondayTimeFromWeekNum(int weekNum) {
        //int julianDay = Utils.getJulianMondayFromWeeksSinceEpoch(weekNum);
        Time time = new Time();
        //Utils.setJulianDayInGeneral(time, julianDay);

        return time;
    }

    public static String printDate(Time time) {
        StringBuilder builder = new StringBuilder(100);
        builder.append(time.year).append("-").append(time.month + 1).append("-").append(time.monthDay);

        return builder.toString();
    }

    public static String printTime(Time time) {
        StringBuilder builder = new StringBuilder(100);
        builder.append(time.year).append("-").append(time.month + 1).append("-").append(time.monthDay);
        builder.append(" ").append(time.hour).append(":").append(time.minute).append(":").append(time.second);

        return builder.toString();
    }

    public static String printDate(int julianDay) {
        Time time = new Time();
        time.setJulianDay(julianDay);
        time.normalize(true);

        return printDate(time);
    }

    public static int getLastMonthDayJulianDay(Time currTime) {
        //Logger.printLog(LOG_TAG, "invoke getLastMonthDayJulianDay() begin");
        //Logger.printLog(LOG_TAG, "current date is: " + printDate(currTime));
        Time temp = new Time(currTime);
        temp.month += 1;
        temp.monthDay = 1;
        temp.monthDay -= 1;
        temp.normalize(true);
        //Logger.printLog(LOG_TAG, "the last month day is: " + printDate(temp));
        //Logger.printLog(LOG_TAG, "invoke getLastMonthDayJulianDay() end");

        return Time.getJulianDay(GNCalendarUtils.normalizeTime(temp, true), temp.gmtoff);
    }

    public static boolean isChineseEnv() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.CHINA.getLanguage()) || lang.equals(Locale.CHINESE.getLanguage())
                || lang.equals(Locale.TAIWAN.getLanguage())
                || lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage())
                || lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage())) {
            // Chinese env
            return true;
        }

        return false;
    }

    public static long checkTimeRange(long millis) {
        Time t = new Time();
        t.set(millis);

        if (t.year < GNCalendarUtils.MIN_YEAR_NUM) {
            t.year = GNCalendarUtils.MIN_YEAR_NUM;
            t.month = 0;
            t.monthDay = 1;

            return GNCalendarUtils.normalizeTime(t, true);
        } else if (t.year > GNCalendarUtils.MAX_YEAR_NUM) {
            t.year = GNCalendarUtils.MAX_YEAR_NUM;
            t.month = 11;
            t.monthDay = 31;

            return GNCalendarUtils.normalizeTime(t, true);
        }

        return millis;
    }

    private static Toast sToast = null;

    public static void showToast(Context context, String text, int duration) {
        if (sToast == null) {
            sToast = Toast.makeText(context, text, duration);
        }

        sToast.setText(text);
        sToast.setDuration(duration);
        sToast.show();
    }

    public static int getTotalDisplayMonthNum() {
        Time timeBegin = new Time();
        timeBegin.parse(MIN_DISPLAY_TIME);
        timeBegin.normalize(true);
        Time timeEnd = new Time();
        timeEnd.parse(MAX_DISPLAY_TIME);
        timeEnd.normalize(true);
        int month = (timeEnd.year - timeBegin.year) * 12 + (timeEnd.month - timeBegin.month) + 1;
        //Logger.printLog("LOG_TAG", "getTotalDisplayMonthNum:" + month);
        return month;
    }

    public static int getDisplayMonthNum(Time targetTime) {
        Time timeBegin = new Time(Time.getCurrentTimezone());
        timeBegin.parse(MIN_DISPLAY_TIME);
        int month = (targetTime.year - timeBegin.year) * 12 + (targetTime.month - timeBegin.month);
        //Logger.printLog("LOG_TAG", "getDisplayMonthNum:" + month);
        return month;
    }

    public static Time getFirstDayByMonthNum(int monthNum) {
        int yearNum = monthNum / 12;
        int month = monthNum % 12;
        Time time = new Time(Time.getCurrentTimezone());
        time.set(1, month, MIN_YEAR_NUM + yearNum);
        normalizeTime(time, true);
        return time;
    }

    public static int getTotalDisplayDayNum() {
        Time timeBegin = new Time();
        timeBegin.parse(MIN_DISPLAY_TIME);
        int firstDay = Time.getJulianDay(GNCalendarUtils.normalizeTime(timeBegin, true), timeBegin.gmtoff);
        Time timeEnd = new Time();
        timeEnd.parse(MAX_DISPLAY_TIME);
        int lastDay = Time.getJulianDay(GNCalendarUtils.normalizeTime(timeEnd, true), timeEnd.gmtoff);
        int days = lastDay - firstDay + 1;
        //Logger.printLog("LOG_TAG", "getTotalDisplayDayNum:" + days);
        return days;
    }

    public static Time getTimeByDayNum(int dayNum) {
        Time time = new Time();
        time.parse(MIN_DISPLAY_TIME);
        time.monthDay += dayNum;
        time.allDay = false;
        normalizeTime(time, true);
        return time;
    }

    public static int getDisplayDayNum(Time targetTime) {
        Time timeBegin = new Time();
        timeBegin.parse(MIN_DISPLAY_TIME);
        int days = Time.getJulianDay(GNCalendarUtils.toMillis(targetTime, true), targetTime.gmtoff)
                - Time.getJulianDay(GNCalendarUtils.toMillis(timeBegin, true), timeBegin.gmtoff);
        //Logger.printLog("LOG_TAG", "getDisplayDayNum:" + days);
        return days;
    }

    public static void setDateRange(AmigoDatePickerDialog dialog) {
        if (dialog != null) {
            setDatePickerRange(dialog.getDatePicker());
        }
    }

    public static void setDatePickerRange(AmigoDatePicker picker) {
        Time minTime = new Time();
        Time maxTime = new Time();
        minTime.set(0, 0, 0, 1, 0, 1970);// 1970/1/1
        maxTime.set(59, 59, 23, 31, 11, 2036);// 2037/12/31
        long maxDate = GNCalendarUtils.toMillis(maxTime, true);
        maxDate = maxDate + 999;// in millsec
        long minDate = GNCalendarUtils.toMillis(minTime, true);
        picker.setMinDate(minDate);
        picker.setMaxDate(maxDate);
    }

    /**
     * Get first day of week as android.text.format.Time constant.
     *
     * @return the first day of week in android.text.format.Time
     */
    public static int getFirstDayOfWeek(Context context) {
//        SettingModel setting = new SettingModel(context);
//        String pref = setting.getWeekStart();
//        int startDay;
//        if (SettingModel.WEEK_START_DEFAULT.equals(pref)) {
//            startDay = Calendar.getInstance().getFirstDayOfWeek();
//        } else {
//            startDay = Integer.parseInt(pref);
//        }
        int startDay = Calendar.getInstance().getFirstDayOfWeek();

        if (startDay == Calendar.SATURDAY) {
            return Time.SATURDAY;
        } else if (startDay == Calendar.MONDAY) {
            return Time.MONDAY;
        } else {
            return Time.SUNDAY;
        }
    }

    public static Time getCurrentTime() {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        normalizeTime(time, true);
        return time;
    }

    public static long getCurrentTimeMillis() {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        return normalizeTime(time, true);
    }

    public static int getCurrentJulianDay() {
        Time t = getCurrentTime();
        return Time.getJulianDay(normalizeTime(t, true), t.gmtoff);
    }

    public static boolean isToday(Time curTime) {
        Time nowTime = GNCalendarUtils.getCurrentTime();
        if (GNCalendarUtils.isIdenticalDate(nowTime, curTime)) {
            return true;
        }
        return false;
    }

    public static void launchAlmanac(Context context, long millis) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setClass(context, GNAlmanacActivity.class);
//        intent.putExtra(GNAlmanacActivity.ALMANAC_EXTRA, millis);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(intent);
    }

    public static void launchHoroscope(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setClass(context, GNHoroscopeActivity.class);
//        intent.putExtra(GNHoroscopeActivity.HOROSCOPE_ACTIVITY_EXTRANAME, System.currentTimeMillis());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(intent);
    }

    public static void launchPeriod(Context context) {
        // GNPeriodMainActivity.startPeriodMainActivity(context);
    }

    public static long normalizeTime(Time time, boolean ignoreDst) {
        long timeMills = -1;
        if (time != null) {
            timeMills = time.normalize(ignoreDst);
            if (timeMills < 0) {
                time.isDst = 0;
                timeMills = time.normalize(false);
                time.allDay = false;
            }
        }
        return timeMills;
    }

    public static long toMillis(Time time, boolean ignoreDst) {
        long timeMills = -1;
        if (time != null) {
            timeMills = time.toMillis(ignoreDst);
            if (timeMills < 0) {
                time.isDst = 0;
                timeMills = time.toMillis(false);
            }
        }
        return timeMills;
    }

}
