package com.gionee.secretary.calendar;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.GNCalendarUtils;


public class LunarUtil {
    public static String getLunarMonthDayOrFes(Time time) {
        Time t = new Time(time);
        GNCalendarUtils.normalizeTime(t, true);
        int flag = Lunar.FLAG_SHOW_DAY | Lunar.FLAG_SHOW_FESTIVAL | Lunar.FLAG_SHOW_DAY_FIRSTDAY_BY_MONTH;
        return getLunarMonthDayOrFes(t.year, t.month, t.monthDay, flag);
    }

    public static String getLunarMonthDayOrFes(int year, int month, int day) {
        int flag = Lunar.FLAG_SHOW_DAY | Lunar.FLAG_SHOW_FESTIVAL | Lunar.FLAG_SHOW_DAY_FIRSTDAY_BY_MONTH;
        return getLunarMonthDayOrFes(year, month, day, flag);
    }

    private static String getLunarMonthDayOrFes(int year, int month, int day, int flag) {
        String result = null;
        try {
            String lunar[] = Lunar.getLunarString(year, month, day, flag);
            if (lunar != null) {
                if (!TextUtils.isEmpty(lunar[1])) {
                    result = lunar[1];
                } else if (!TextUtils.isEmpty(lunar[0])) {
                    result = lunar[0];
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    private static String getLunarByType(Context context, int year, int month, int day, boolean withType,
                                         int flag) {
        String result = null;
        String lunar[] = Lunar.getLunarString(year, month, day, flag);
        if (lunar != null && !TextUtils.isEmpty(lunar[0])) {
            result = (withType ? context.getString(R.string.gn_day_lunar) : "") + lunar[0];
        }
        return result;
    }

    public static String getLunarDateWithOutYear(Context context, int year, int month, int day,
                                                 boolean withType) {
        int flag = Lunar.FLAG_SHOW_DAY | Lunar.FLAG_SHOW_MONTH;
        return getLunarByType(context, year, month, day, withType, flag);
    }

    public static String getLunarMonthDayWithOutYear(Context context, int year, int month, int day,
                                                     boolean withType) {
        int flag = Lunar.FLAG_SHOW_DAY;
        return getLunarByType(context, year, month, day, withType, flag);
    }

    public static String getLunarStringForDayView(Context context, Time time) {
        Time t = new Time(time);
        GNCalendarUtils.normalizeTime(t, true);
        String result = context.getString(R.string.gn_day_lunar);
        int flag = Lunar.FLAG_SHOW_DAY | Lunar.FLAG_SHOW_MONTH | Lunar.FLAG_SHOW_FESTIVAL;
        String lunar[] = Lunar.getLunarString(t.year, t.month, t.monthDay, flag);
        if (lunar != null) {
            if (!TextUtils.isEmpty(lunar[0])) {
                result += lunar[0];
            }
            if (!TextUtils.isEmpty(lunar[1])) {
                result += " " + lunar[1];
            }
        }
        return result;
    }
}
