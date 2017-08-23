package com.gionee.secretary.calendar;

import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Lunar {

    public static final int FLAG_SHOW_DAY = 0b1;
    public static final int FLAG_SHOW_MONTH = 0b10;
    public static final int FLAG_SHOW_YEAR = 0b100;
    public static final int FLAG_SHOW_FESTIVAL = 0b10000;

    public static final int FLAG_SHOW_DAY_FIRSTDAY_BY_MONTH = 0b100000;

    /**
     * 支持转换的最小农历年份
     */
    private static final int MIN_YEAR = 1900;
    /**
     * 支持转换的最大农历年份
     **/
    private static final int MAX_YEAR = 2099;

    /**
     * 公历每月前的天数
     */
    private static final int DAYS_BEFORE_MONTH[] = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334,
            365};

    /**
     * 用来表示1900年到2099年间农历年份的相关信息，共24位bit的16进制表示，其中： 1. 前4位表示该年闰哪个月； 2. 5-17位表示农历年份13个月的大小月分布，0表示小，1表示大； 3.
     * 最后7位表示农历年首（正月初一）对应的公历日期。
     * <p>
     * 以2014年的数据0x955ABF为例说明： 1001 0101 0101 1010 1011 1111 闰九月 农历正月初一对应公历1月31号
     */
    private static final int LUNAR_INFO[] = {0x84B6BF,/*1900*/
            0x04AE53, 0x0A5748, 0x5526BD, 0x0D2650, 0x0D9544, 0x46AAB9, 0x056A4D, 0x09AD42, 0x24AEB6, 0x04AE4A,/*1901-1910*/
            0x6A4DBE, 0x0A4D52, 0x0D2546, 0x5D52BA, 0x0B544E, 0x0D6A43, 0x296D37, 0x095B4B, 0x749BC1, 0x049754,/*1911-1920*/
            0x0A4B48, 0x5B25BC, 0x06A550, 0x06D445, 0x4ADAB8, 0x02B64D, 0x095742, 0x2497B7, 0x04974A, 0x664B3E,/*1921-1930*/
            0x0D4A51, 0x0EA546, 0x56D4BA, 0x05AD4E, 0x02B644, 0x393738, 0x092E4B, 0x7C96BF, 0x0C9553, 0x0D4A48,/*1931-1940*/
            0x6DA53B, 0x0B554F, 0x056A45, 0x4AADB9, 0x025D4D, 0x092D42, 0x2C95B6, 0x0A954A, 0x7B4ABD, 0x06CA51,/*1941-1950*/
            0x0B5546, 0x555ABB, 0x04DA4E, 0x0A5B43, 0x352BB8, 0x052B4C, 0x8A953F, 0x0E9552, 0x06AA48, 0x6AD53C,/*1951-1960*/
            0x0AB54F, 0x04B645, 0x4A5739, 0x0A574D, 0x052642, 0x3E9335, 0x0D9549, 0x75AABE, 0x056A51, 0x096D46,/*1961-1970*/
            0x54AEBB, 0x04AD4F, 0x0A4D43, 0x4D26B7, 0x0D254B, 0x8D52BF, 0x0B5452, 0x0B6A47, 0x696D3C, 0x095B50,/*1971-1980*/
            0x049B45, 0x4A4BB9, 0x0A4B4D, 0xAB25C2, 0x06A554, 0x06D449, 0x6ADA3D, 0x0AB651, 0x095746, 0x5497BB,/*1981-1990*/
            0x04974F, 0x064B44, 0x36A537, 0x0EA54A, 0x86B2BF, 0x05AC53, 0x0AB647, 0x5936BC, 0x092E50, 0x0C9645,/*1991-2000*/
            0x4D4AB8, 0x0D4A4C, 0x0DA541, 0x25AAB6, 0x056A49, 0x7AADBD, 0x025D52, 0x092D47, 0x5C95BA, 0x0A954E,/*2001-2010*/
            0x0B4A43, 0x4B5537, 0x0AD54A, 0x955ABF, 0x04BA53, 0x0A5B48, 0x652BBC, 0x052B50, 0x0A9345, 0x474AB9,/*2011-2020*/
            0x06AA4C, 0x0AD541, 0x24DAB6, 0x04B64A, 0x6a573D, 0x0A4E51, 0x0D2646, 0x5E933A, 0x0D534D, 0x05AA43,/*2021-2030*/
            0x36B537, 0x096D4B, 0xB4AEBF, 0x04AD53, 0x0A4D48, 0x6D25BC, 0x0D254F, 0x0D5244, 0x5DAA38, 0x0B5A4C,/*2031-2040*/
            0x056D41, 0x24ADB6, 0x049B4A, 0x7A4BBE, 0x0A4B51, 0x0AA546, 0x5B52BA, 0x06D24E, 0x0ADA42, 0x355B37,/*2041-2050*/
            0x09374B, 0x8497C1, 0x049753, 0x064B48, 0x66A53C, 0x0EA54F, 0x06AA44, 0x4AB638, 0x0AAE4C, 0x092E42,/*2051-2060*/
            0x3C9735, 0x0C9649, 0x7D4ABD, 0x0D4A51, 0x0DA545, 0x55AABA, 0x056A4E, 0x0A6D43, 0x452EB7, 0x052D4B,/*2061-2070*/
            0x8A95BF, 0x0A9553, 0x0B4A47, 0x6B553B, 0x0AD54F, 0x055A45, 0x4A5D38, 0x0A5B4C, 0x052B42, 0x3A93B6,/*2071-2080*/
            0x069349, 0x7729BD, 0x06AA51, 0x0AD546, 0x54DABA, 0x04B64E, 0x0A5743, 0x452738, 0x0D264A, 0x8E933E,/*2081-2090*/
            0x0D5252, 0x0DAA47, 0x66B53B, 0x056D4F, 0x04AE45, 0x4A4EB9, 0x0A4D4C, 0x0D1541, 0x2D92B5 /*2091-2099*/
    };

    private static final String NAME_YEAR = "年";
    private static final String NAME_MONTH = "月";
    private static final String NAME_LEAP = "闰";

    private static final String NAME_TEN = "初十";
    private static final String NAME_TWENTITH = "二十";
    private static final String NAME_THIRTIETH = "三十";

    private static final String FIRSTDAY_OF_MONTH = "初一";

    private static final String[] MONTH_NUMBER_ARRAY = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
            "十一", "腊"};
    private static final String[] TENS_PREFIX_ARRAY = {"初", "十", "廿", "三"};

    private final static String[] solarTerm = {"小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满",
            "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};

    private static final byte[] SSolarArray = {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23,
            9, 24, 8, 23, 7, 22, 6, 21, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23,
            8, 22, 6, 21, 5, 19, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20,
            4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 6, 21,
            5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 21, 6, 22,
            6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23,
            7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23,
            8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23,
            7, 22, 6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22, 6, 21,
            4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 19, 5, 21,
            5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21,
            6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23,
            8, 24, 8, 23, 9, 24, 8, 23, 8, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23,
            8, 23, 7, 22, 7, 21, 5, 20, 4, 19, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22,
            7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22, 6, 20,
            4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 7, 22, 6, 21, 4, 19, 5, 20,
            4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21,
            6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23,
            8, 23, 8, 23, 8, 24, 8, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23,
            9, 24, 8, 23, 7, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22,
            7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20,
            4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 6, 21,
            5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21,
            5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23,
            7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23,
            8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23,
            7, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20,
            4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21,
            5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21,
            6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22,
            7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23,
            8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22,
            7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22, 6, 20,
            4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20,
            4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 5, 21,
            6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23,
            8, 23, 8, 23, 8, 24, 8, 23, 7, 22, 6, 21, 4, 19, 5, 20, 4, 20, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22,
            8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22,
            7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20,
            4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22, 6, 20, 4, 19, 5, 20,
            4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 7, 21, 5, 20, 3, 18, 5, 20, 4, 20, 5, 21,
            5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23,
            7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23,
            8, 24, 8, 22, 7, 22, 6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22,
            6, 21, 5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20,
            4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21,
            5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 5, 20, 4, 19, 5, 20,
            5, 21, 6, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21, 5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22,
            7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23,
            8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22,
            7, 22, 6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21, 5, 20,
            3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20,
            5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 6, 21,
            6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22,
            7, 22, 7, 22, 8, 23, 7, 22, 6, 21, 5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23,
            8, 23, 7, 22, 7, 21, 5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22,
            7, 22, 5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22, 6, 20,
            4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21};

    private static final Map<String, String> LUNAR_FES = new HashMap<>();

    static {
        LUNAR_FES.put("011", "春节");
        LUNAR_FES.put("0115", "元宵节");
        LUNAR_FES.put("022", "龙抬头");
        LUNAR_FES.put("055", "端午节");
        LUNAR_FES.put("077", "七夕节");
        LUNAR_FES.put("0815", "中秋节");
        LUNAR_FES.put("099", "重阳节");
        LUNAR_FES.put("128", "腊八节");
    }

    private static final String LUNAR_FES_CHUXI = "除夕";

    private static final Map<String, String> GREGORIAN_FES = new HashMap<>();

    static {
        GREGORIAN_FES.put("011", "元旦");
        GREGORIAN_FES.put("0214", "情人节");
        GREGORIAN_FES.put("038", "妇女节");
        GREGORIAN_FES.put("0312", "植树节");
        GREGORIAN_FES.put("0315", "消权日");
        GREGORIAN_FES.put("041", "愚人节");
        GREGORIAN_FES.put("0422", "地球日");
        GREGORIAN_FES.put("051", "劳动节");
        GREGORIAN_FES.put("054", "青年节");
        GREGORIAN_FES.put("0531", "无烟日");
        GREGORIAN_FES.put("061", "儿童节");
        GREGORIAN_FES.put("071", "建党节");
        GREGORIAN_FES.put("081", "建军节");
        GREGORIAN_FES.put("093", "胜利日");
        GREGORIAN_FES.put("0910", "教师节");
        GREGORIAN_FES.put("101", "国庆节");
        GREGORIAN_FES.put("1031", "万圣节");
        GREGORIAN_FES.put("1111", "光棍节");
        GREGORIAN_FES.put("1224", "平安夜");
        GREGORIAN_FES.put("1225", "圣诞节");
    }

    private static final Map<String, String> GREGORIAN_SPECIAL_FES_SUN = new HashMap<>();

    static {
        // must set firstdayof week = monday
        GREGORIAN_SPECIAL_FES_SUN.put("521", "母亲节");
        // must set firstdayof week = monday
        GREGORIAN_SPECIAL_FES_SUN.put("631", "父亲节");
    }

    private static final Map<String, String> GREGORIAN_SPECIAL_FES_OTHER = new HashMap<>();

    static {
        // if set firstdayof week = monday, day must 22<=day<=28
        GREGORIAN_SPECIAL_FES_OTHER.put("1145", "感恩节");
        GREGORIAN_SPECIAL_FES_OTHER.put("1155", "感恩节");
    }

    private static Calendar sSolar;
    private static GregorianCalendar sUtcCal = null;

    static {
        sSolar = Calendar.getInstance();
        sSolar.setFirstDayOfWeek(Calendar.MONDAY);
        sSolar.setTimeZone(TimeZone.getTimeZone("UTC"));
        sUtcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    }

    /**
     * 将农历日期转换为公历日期
     *
     * @param year        农历年份
     * @param month       农历月
     * @param monthDay    农历日
     * @param isLeapMonth 该月是否是闰月
     * @return 返回农历日期对应的公历日期，year0, month1, day2.
     */
    public static final int[] lunarToSolar(int year, int month, int monthDay, boolean isLeapMonth) {
        int dayOffset;
        int leapMonth = (LUNAR_INFO[year - MIN_YEAR] & 0xf00000) >> 20;
        int i;

        if (year < MIN_YEAR || year > MAX_YEAR || month < 1 || month > 12 || monthDay < 1 || monthDay > 30) {
            throw new IllegalArgumentException("Illegal lunar date, must be like that:\n\t"
                    + "year : 1900~2099\n\t" + "month : 1~12\n\t" + "day : 1~30");
        }

        if (isMonthDayOverFlow(year, month, monthDay, leapMonth, isLeapMonth)) {
            return null;
        }

        dayOffset = (LUNAR_INFO[year - MIN_YEAR] & 0x001F) - 1;

        if (((LUNAR_INFO[year - MIN_YEAR] & 0x0060) >> 5) == 2)
            dayOffset += 31;

        for (i = 1; i < month; i++) {
            if ((LUNAR_INFO[year - MIN_YEAR] & (0x80000 >> (i - 1))) == 0)
                dayOffset += 29;
            else
                dayOffset += 30;
        }

        dayOffset += monthDay;

        // 这一年有闰月
        if (leapMonth != 0) {
            if (month > leapMonth || (month == leapMonth && isLeapMonth)) {
                if ((LUNAR_INFO[year - MIN_YEAR] & (0x80000 >> (month - 1))) == 0)
                    dayOffset += 29;
                else
                    dayOffset += 30;
            }
        }

        if (dayOffset > 366 || (year % 4 != 0 && dayOffset > 365)) {
            year += 1;
            if (year % 4 == 1)
                dayOffset -= 366;
            else
                dayOffset -= 365;
        }

        int[] solarInfo = new int[3];
        for (i = 1; i < 13; i++) {
            int iPos = DAYS_BEFORE_MONTH[i];
            if (year % 4 == 0 && i > 2) {
                iPos += 1;
            }

            if (year % 4 == 0 && i == 2 && iPos + 1 == dayOffset) {
                solarInfo[1] = i;
                solarInfo[2] = dayOffset - 31;
                break;
            }

            if (iPos >= dayOffset) {
                solarInfo[1] = i;
                iPos = DAYS_BEFORE_MONTH[i - 1];
                if (year % 4 == 0 && i > 2) {
                    iPos += 1;
                }
                if (dayOffset > iPos)
                    solarInfo[2] = dayOffset - iPos;
                else if (dayOffset == iPos) {
                    if (year % 4 == 0 && i == 2)
                        solarInfo[2] = DAYS_BEFORE_MONTH[i] - DAYS_BEFORE_MONTH[i - 1] + 1;
                    else
                        solarInfo[2] = DAYS_BEFORE_MONTH[i] - DAYS_BEFORE_MONTH[i - 1];

                } else
                    solarInfo[2] = dayOffset;
                break;
            }
        }
        solarInfo[0] = year;

        return solarInfo;
    }

    private static boolean isMonthDayOverFlow(int year, int month, int monthDay, int leapMonth,
                                              boolean isLeapMonth) {
        int temp = ((month > leapMonth) && leapMonth != 0) || (month == leapMonth && isLeapMonth) ? month + 1
                : month;
        if ((LUNAR_INFO[year - MIN_YEAR] & (0x80000 >> (temp - 1))) == 0) {
            if (monthDay > 29) {
                // 传入是小月，如果天数大于29，返回空，表示出入农历不合法
                return true;
            }
        }
        return false;
    }

    /**
     * 将公历日期转换为农历日期，且标识是否是闰月
     *
     * @param year
     * @param month
     * @param monthDay
     * @return 返回公历日期对应的农历日期，year0，month1，day2，leap3
     */
    public static final int[] solarToLunar(long millis) {
        int[] lunarDate = new int[4];
        int offset = (int) ((millis - UTC(1900, 0, 31, 0, 0, 0)) / 86400000L);
        if (offset <= 0) {
            return null;
        }

        // 用offset减去每农历年的天数计算当天是农历第几天
        // iYear最终结果是农历的年份, offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = MIN_YEAR; iYear <= MAX_YEAR && offset > 0; iYear++) {
            daysOfYear = daysInLunarYear(iYear);
            offset -= daysOfYear;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
        }

        // 农历年份
        lunarDate[0] = iYear;

        int leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        boolean isLeap = false;
        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth <= 13 && offset >= 0; iMonth++) {
            daysOfMonth = daysInLunarMonth(iYear, iMonth);
            offset -= daysOfMonth;
        }
        // 当前月超过闰月，要校正
        if (leapMonth != 0 && iMonth > leapMonth) {
            --iMonth;
            if (iMonth == leapMonth + 1) {
                isLeap = true;
            } else if (iMonth == leapMonth) {
                iMonth++;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
        }

        lunarDate[1] = iMonth;
        lunarDate[2] = offset + 1;
        lunarDate[3] = isLeap ? 1 : 0;

        return lunarDate;
    }

    public static final int[] solarToLunar(Date date) {
        return solarToLunar(date.getTime());
    }

    /**
     * 传回农历year年month月的总天数
     *
     * @param year  要计算的年份
     * @param month 要计算的月
     * @return 传回天数
     */
    final public static int daysInMonth(int year, int month) {
        return daysInMonth(year, month, false);
    }

    /**
     * 传回农历year年month月的总天数
     *
     * @param year  要计算的年份
     * @param month 要计算的月
     * @param leap  当月是否是闰月
     * @return 传回天数，如果闰月是错误的，返回0.
     */
    public static final int daysInMonth(int year, int month, boolean leap) {
        int leapMonth = leapMonth(year);
        int offset = 0;

        // 如果本年有闰月且month大于闰月时，需要校正
        if (leapMonth != 0 && month > leapMonth) {
            offset = 1;
        }

        // 不考虑闰月
        if (!leap) {
            return daysInLunarMonth(year, month + offset);
        } else {
            // 传入的闰月是正确的月份
            if (leapMonth != 0 && leapMonth == month) {
                return daysInLunarMonth(year, month + 1);
            }
        }

        return 0;
    }

    /**
     * 传回农历 year年的总天数
     *
     * @param year 将要计算的年份
     * @return 返回传入年份的总天数
     */
    private static int daysInLunarYear(int year) {
        int i, sum = 348;
        if (leapMonth(year) != 0) {
            sum = 377;
        }
        int monthInfo = LUNAR_INFO[year - MIN_YEAR] & 0x0FFF80;
        for (i = 0x80000; i > 0x7; i >>= 1) {
            if ((monthInfo & i) != 0)
                sum += 1;
        }
        return sum;
    }

    /**
     * 传回农历 year年month月的总天数，总共有13个月包括闰月
     *
     * @param year  将要计算的年份
     * @param month 将要计算的月份
     * @return 传回农历 year年month月的总天数
     */
    private static int daysInLunarMonth(int year, int month) {
        if ((LUNAR_INFO[year - MIN_YEAR] & (0x100000 >> month)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * 传回农历 year年闰哪个月 1-12 , 没闰传回 0
     *
     * @param year 将要计算的年份
     * @return 传回农历 year年闰哪个月1-12, 没闰传回 0
     */
    private static int leapMonth(int year) {
        return (int) ((LUNAR_INFO[year - MIN_YEAR] & 0xF00000)) >> 20;
    }

    private static boolean islittleDecember(int year) {
        int month = 12;
        int leapMonth = leapMonth(year);
        if (leapMonth != 0) {
            month = 13;
        }
        if ((LUNAR_INFO[year - MIN_YEAR] & (0x80000 >> (month - 1))) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] normalizeMonth(int year, int desMonth, int freqAmount, boolean isLeapMonth) {
        int desYear = year;
        int leapMonth = leapMonth(desYear);
        if (leapMonth > 0) {
            if ((isLeapMonth && desMonth == leapMonth) || desMonth > leapMonth) {
                desMonth++;
            }
        }
        desMonth += freqAmount;
        while (desMonth > 0) {
            desMonth -= monthInYear(desYear);
            desYear++;
        }
        desYear--;
        desMonth += monthInYear(desYear);

        leapMonth = leapMonth(desYear);

        int isCurLeapMonth = 0;
        if (leapMonth > 0 && desMonth > leapMonth) {
            desMonth--;
            if (desMonth == leapMonth) {
                isCurLeapMonth = 1;
            }
        }

        return new int[]{desYear, desMonth, isCurLeapMonth};
    }

    private static int monthInYear(int year) {
        int leapMonth = leapMonth(year);
        return leapMonth == 0 ? 12 : 13;
    }

    public static String[] getLunarString(int year, int month, int day, int showFlag) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            return null;
        }
        return getLunarString(UTC(year, month, day, 0, 0, 0), showFlag);
    }

    private static String[] getLunarString(long timeMills, int showFlag) {
        boolean isShowFes = (showFlag & FLAG_SHOW_FESTIVAL) != 0;

        String result[] = new String[2];
        int[] lunar = null;
        synchronized (sSolar) {
            sSolar.setTimeInMillis(timeMills);
        }
        lunar = solarToLunar(timeMills);

        String lunarDate = getLunarDateString(lunar[0], lunar[1], lunar[2], lunar[3] != 0, showFlag);
        if (!TextUtils.isEmpty(lunarDate)) {
            result[0] = lunarDate;
        }
        if (isShowFes) {
            result[1] = getFestival(lunar, sSolar);
        }
        return result;
    }

    private static String getFestival(int[] lunar, Calendar solar) {
        String result = null;
        // get lunar festival
        if (lunar[3] == 0) {
            result = getLunarFesChuXi(lunar);
            if (result == null) {
                String key = "" + (lunar[1] < 10 ? "0" + lunar[1] : lunar[1]) + lunar[2];
                result = LUNAR_FES.get(key);
            }
        }
        // get Twenty-four solar terms
        if (result == null) {
            result = getTermString(solar.get(Calendar.YEAR), solar.get(Calendar.MONTH) + 1,
                    solar.get(Calendar.DAY_OF_MONTH));
        }

        // get gregorian festival
        if (result == null) {
            int month = solar.get(Calendar.MONTH) + 1;
            String key = "" + (month < 10 ? "0" + month : month) + solar.get(Calendar.DAY_OF_MONTH);
            result = GREGORIAN_FES.get(key);
        }

        // get special gregorain restival
        if (result == null) {
            result = getGregorianSpecialFes(solar);
        }
        return result;
    }

    private static String getGregorianSpecialFes(Calendar solar) {
        String result = null;
        int month = solar.get(Calendar.MONTH) + 1;
        int monthDay = solar.get(Calendar.DAY_OF_MONTH);
        String key = "" + month + solar.get(Calendar.WEEK_OF_MONTH) + solar.get(Calendar.DAY_OF_WEEK);
        if (month == 11 && (22 <= monthDay && monthDay <= 28)) {
            result = GREGORIAN_SPECIAL_FES_OTHER.get(key);
        } else {
            result = GREGORIAN_SPECIAL_FES_SUN.get(key);
        }
        return result;
    }

    private static String getLunarFesChuXi(int[] lunar) {
        if (lunar[1] == 12 && lunar[2] >= 29) {
            if (islittleDecember(lunar[0]) && lunar[2] == 29) {
                return LUNAR_FES_CHUXI;
            } else if (lunar[2] == 30) {
                return LUNAR_FES_CHUXI;
            }
        }
        return null;
    }

    /**
     * The really function produce lunar date string.
     *
     * @param lunarYear
     * @param lunarMonth
     * @param lunarDay
     * @return the lunar date string like:xx年[闰]xx月初xx
     */
    private static String getLunarDateString(int lunarYear, int lunarMonth, int lunarDay,
                                             boolean isLeapMonth, int showFlag) {
        boolean isShowDay = (showFlag & FLAG_SHOW_DAY) != 0;
        boolean isShowMonth = (showFlag & FLAG_SHOW_MONTH) != 0;
        boolean isShowYear = (showFlag & FLAG_SHOW_YEAR) != 0;
        boolean isReplaceDayByMonth = (showFlag & FLAG_SHOW_DAY_FIRSTDAY_BY_MONTH) != 0;
        try {
            String year = isShowYear ? lunarYear + NAME_YEAR : "";
            String month = isShowMonth ? ((isLeapMonth ? NAME_LEAP : "") + MONTH_NUMBER_ARRAY[lunarMonth - 1] + NAME_MONTH)
                    : "";
            String day = isShowDay ? getLunarDayString(lunarDay) : "";
            if (isReplaceDayByMonth) {
                if (FIRSTDAY_OF_MONTH.equals(day)) {
                    day = (isLeapMonth ? NAME_LEAP : "") + MONTH_NUMBER_ARRAY[lunarMonth - 1] + NAME_MONTH;
                }
            }
            return year + month + day;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get a lunar day's chnese String.
     *
     * @param lunarDay the number of which day
     * @return the chnese string that the luanr day corresponded. like:初二,初二三.
     */
    private static String getLunarDayString(int lunarDay) {
        int n = lunarDay % 10 == 0 ? 9 : lunarDay % 10 - 1;
        if (lunarDay < 0 || lunarDay > 30) {
            return "";
        }

        String ret;
        switch (lunarDay) {
            case 10:
                ret = NAME_TEN;
                break;
            case 20:
                ret = NAME_TWENTITH;
                break;
            case 30:
                ret = NAME_THIRTIETH;
                break;
            default:
                ret = TENS_PREFIX_ARRAY[lunarDay / 10] + MONTH_NUMBER_ARRAY[n];
                break;
        }
        return ret;
    }

    /**
     * 返回公历日期的节气字符串
     *
     * @return 二十四节气字符串, 若不是节气日, 返回空串(例:冬至)
     */
    public static String getTermString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int days[] = getAMonthSolarTermDays(gregorianYear, gregorianMonth);
        if ((gregorianDay != days[0]) && (gregorianDay != days[1])) {
            return null;
        } else if (gregorianDay == days[0]) {
            return getSolarTermNameByIndex(gregorianMonth * 2 - 1);
        } else if (gregorianDay == days[1]) {
            return getSolarTermNameByIndex(gregorianMonth * 2);
        }
        return null;
    }

    private static String getSolarTermNameByIndex(int index) {
        if (index < 1 || index > solarTerm.length) {
            return null;
        }
        return solarTerm[index - 1];
    }

    private static int[] getAMonthSolarTermDays(int gregorianYear, int gregorianMonth) {
        int firstSolarTermIndex = (gregorianMonth - 1) * 2;
        int days[] = {0, 0};
        if (gregorianYear > 1969 && gregorianYear < 2037) {
            int firstSolarTermDay = SSolarArray[(gregorianYear - 1970) * 24 + firstSolarTermIndex];
            int secondSolarTermDay = SSolarArray[(gregorianYear - 1970) * 24 + firstSolarTermIndex + 1];
            days[0] = firstSolarTermDay;
            days[1] = secondSolarTermDay;
        }
        return days;
    }

    private static long UTC(int y, int m, int d, int h, int min, int sec) {
        synchronized (sUtcCal) {
            sUtcCal.clear();
            sUtcCal.set(y, m, d, h, min, sec);
        }
        return sUtcCal.getTimeInMillis();
    }
}
