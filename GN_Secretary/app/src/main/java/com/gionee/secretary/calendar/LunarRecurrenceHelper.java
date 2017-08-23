package com.gionee.secretary.calendar;

import android.text.format.Time;

import com.gionee.secretary.utils.GNCalendarUtils;

import java.util.ArrayList;
import java.util.List;

public final class LunarRecurrenceHelper {

    public static List<Long> getAftertimeRecurrenceDateByLunarYear(Long milliseconds, String timeZong) {
        Time time = new Time(timeZong);
        time.set(milliseconds);
        long startMills = GNCalendarUtils.normalizeTime(time, true);
        if (time.year >= 2036) {
            return null;
        }
        int[] nowLunarTime = Lunar.solarToLunar(startMills);
        int startYear = nowLunarTime[0] + 1;
        int month = nowLunarTime[1];
        int day = nowLunarTime[2];
        List<Long> results = new ArrayList<Long>();
        for (; startYear <= 2036; startYear++) {
            int[] temp = Lunar.lunarToSolar(startYear, month, day, false);
            if (temp == null)
                continue;
            time.year = temp[0];
            time.month = temp[1] - 1;
            time.monthDay = temp[2];
            results.add(GNCalendarUtils.normalizeTime(time, true));
        }
        return results;
    }

    public static List<Long> getPastRecurrenceDateByLunarYear(Long milliseconds, int recurrenceCount,
                                                              String timeZong) {
        Time time = new Time(timeZong);
        time.set(milliseconds);
        long startMills = time.normalize(true);
        if (time.year <= 1970) {
            return null;
        }
        int[] nowLunarTime = Lunar.solarToLunar(startMills);
        int startYear = nowLunarTime[0] + 1;
        int month = nowLunarTime[1];
        int day = nowLunarTime[2];
        List<Long> results = new ArrayList<Long>();
        for (int count = 1; startYear >= 70 && count <= recurrenceCount; startYear--, count++) {
            int[] temp = Lunar.lunarToSolar(startYear, month, day, false);
            if (temp == null)
                continue;
            time.year = temp[0];
            time.month = temp[1] - 1;
            time.monthDay = temp[2];
            results.add(GNCalendarUtils.normalizeTime(time, true));
        }
        return results;
    }
}
