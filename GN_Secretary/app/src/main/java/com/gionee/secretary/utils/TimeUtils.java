package com.gionee.secretary.utils;

import android.content.Context;
import android.text.format.Time;

import com.gionee.secretary.calendar.LunarUtil;

/**
 * Created by hangh on 5/17/16.
 */
public class TimeUtils {
    /**
     * When we aren't given an explicit start time, we default to the next upcoming half hour. So, for
     * example, 5:01 -> 5:30, 5:30 -> 6:00, etc.
     *
     * @return a UTC time in milliseconds representing the next upcoming half hour
     */
    public static long constructDefaultStartTime(long now) {
        Time defaultStart = new Time();
        defaultStart.set(now);
        defaultStart.second = 0;
        defaultStart.minute = 30;
        long defaultStartMillis = GNCalendarUtils.toMillis(defaultStart, true);
        if (now < defaultStartMillis) {
            return defaultStartMillis;
        } else {
            return defaultStartMillis + 30 * 60 * 1000;
        }
    }

    /**
     * When we aren't given an explicit end time, we default to an hour after the start time.
     *
     * @param startTime the start time
     * @return a default end time
     */
    public static long constructDefaultEndTime(long startTime) {
        return startTime + 60 * 60 * 1000;
    }


    public static String getLunarDate(Context context, long millis) {
        Time time = new Time();
        time.set(millis);
        time.normalize(true);
        String lunarDate = LunarUtil.getLunarDateWithOutYear(context, time.year,
                time.month, time.monthDay, true);
        return lunarDate;
    }
}
