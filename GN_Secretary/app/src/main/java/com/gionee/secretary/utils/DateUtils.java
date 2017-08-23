package com.gionee.secretary.utils;

import android.text.TextUtils;

import com.gionee.secretary.constants.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by luorw on 5/12/16.
 */
public class DateUtils {
    private static final String TAG = "DateUtils";

    /**
     * @param date 如2016-05-12 08:12:00
     * @return
     */
    public static Date formatDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime = null;
        try {
            if (!TextUtils.isEmpty(date)) {
                dateTime = format.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static boolean inSameDay(Date date1, Date Date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTime(Date2);
        int year2 = calendar.get(Calendar.YEAR);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);

        if ((year1 == year2) && (day1 == day2)) {
            return true;
        }
        return false;
    }


    public static Date formatDate2(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = null;
        try {
            if (!TextUtils.isEmpty(date)) {
                dateTime = format.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static Date formatDate3(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dateTime = null;
        try {
            if (!TextUtils.isEmpty(date)) {
                dateTime = format.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    /**
     * @param date yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDate2String(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date yyyy-MM-dd HH:mm
     * @return
     */
    public static String formatDate2String2(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String str = "";
        if (date != null) {
            str = format.format(date);
        }
        return str;
    }


    /**
     * @param date yyyy-MM-dd
     * @return
     */
    public static String formatDate2DateString(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date yyyyMMdd
     * @return
     */
    public static String date2String(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String str = format.format(date);
        return str;
    }

    public static String date2String4(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date yyyyMMdd
     * @return
     */
    public static String date2String2(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date HH:mm
     * @return
     */
    public static String time2String(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date HH:mm:ss
     * @return
     */
    public static String date2String3(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    public static Date str2Date(String str_date) {
        if (str_date == null || "null".equals(str_date)) {
            LogUtils.e(TAG, "date = null");
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        Date date = new Date();
        try {
            date = format.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

    public static Date str2DateFormat(String str_date) {
        if (str_date == null || "null".equals(str_date)) {
            LogUtils.e(TAG, "date = null");
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try {
            date = (Date) format.parse(str_date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }


    public static String getDate(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        return new SimpleDateFormat("M月d日").format(date);
    }

    public static String getDateIncludeYear(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        return new SimpleDateFormat("yyyy年M月d日").format(date);
    }

    public static String formatDate2StringByMonth(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String str = format.format(date);
        return str;
    }

    /**
     * @param date 如2016-05
     * @return
     */
    public static Date formatString2DateByMonth(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date dateTime = null;
        try {
            if (!TextUtils.isEmpty(date)) {
                dateTime = format.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static String formatDate2StringByDay(Date date) {
        if (date == null) {
            LogUtils.e(TAG, "date = null");
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(date);
        return str;
    }

    public static Calendar convertHotelDate(Calendar hotelCalendar, String value, String dateFormatStr) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatStr);
            String year = new SimpleDateFormat("yyyy").format(hotelCalendar.getTime());
            Date checkInDate = simpleDateFormat.parse(value);
            hotelCalendar.setTime(checkInDate);
            hotelCalendar.set(Calendar.HOUR_OF_DAY, 0);
            hotelCalendar.set(Calendar.MINUTE, 0);
            hotelCalendar.set(Calendar.SECOND, 0);
            hotelCalendar.set(Calendar.MILLISECOND, 0);
            hotelCalendar.set(Calendar.YEAR, Integer.valueOf(year));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hotelCalendar;
    }

    public static Date formatDateForBroadcast(String date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date dateTime = null;
        try {
            if (!TextUtils.isEmpty(date)) {
                dateTime = format.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    //Gionee <gn_by><zhengyt><2017-4-2> add for GNSPR76955 Begin
    public static Date str2Date2(String str_date) {
        if (str_date == null || "null".equals(str_date)) {
            LogUtils.e(TAG, "date = null");
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        if (str_date.length() > 5) {
            format = new SimpleDateFormat("yyyy/MM/dd");
        }
        Date date = new Date();
        try {
            date = format.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }
    //Gionee <gn_by><zhengyt><2017-4-2> add for GNSPR76955 End
}
