package com.gionee.secretary.constants;

/**
 * Created by liu on 5/29/16.
 */
public class RemindTime {
    public static final long TIME_BEFORE_SCHEDULE_5 = 5 * 60 * 60 * 1000;  //50*1000;
    public static final long TIME_BEFORE_SCHEDULE_3 = 3 * 60 * 60 * 1000;  //30*1000;
    public static final long TIME_BEFORE_SCHEDULE_2 = 2 * 60 * 60 * 1000; //20*1000;
    public static final long TIME_BEFORE_SCHEDULE_1 = 60 * 60 * 1000;      //10*1000;
    public static final long TIME_RESERVED_FOR_FLIGHT = 2 * 60 * 60 * 1000;
    public static final long TIME_RESERVED_FOR_TICKET = 60 * 60 * 1000;    // 10*1000; // 火车票预留时间
    public static final long TIME_RESERVED_FOR_MOVIE = 30 * 60 * 1000;
    public static final long TIME_ON_TRIP_FROM_GD = 60 * 60 * 1000;             //40*1000;
    public static final long CHECK_INTERVAL_1 = 60 * 60 * 1000; //10*1000;
    public static final long CHECK_INTERVAL_2 = 30 * 60 * 1000; //3*1000;
    public static final long CHECK_INTERVAL_3 = 20 * 60 * 1000; //2*1000;
    public static final long CHECK_INTERVAL_4 = 10 * 60 * 1000; //1*1000;
    public static final int MAX_HOUR_PREPARE = 7;       // 5+2

    public static final long TIME_COMPARE_SCHEDULE_5 = 5 * 60 * 60 * 1000;
    public static final long TIME_COMPARE_SCHEDULE_3 = 3 * 60 * 60 * 1000;
    public static final long TIME_COMPARE_SCHEDULE_2 = 2 * 60 * 60 * 1000;
    public static final long TIME_COMPARE_SCHEDULE_1 = 1 * 60 * 60 * 1000;
}
