package com.gionee.secretary.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;

import java.util.Calendar;

/**
 * Created by liu on 5/30/16.
 */
public class AutoRefreshTask {
    /**
     * 执行0自动加载刷新任务
     *
     * @param context
     */
    private static final String TAG = "lml";

    public static void executeAutoRefreshTask(Context context) {
        long refreshTimeStamp = getAutoRefreshTimeStamp();
        LogUtils.d(TAG, "AutoRefreshTask.executeAutoRefreshTask, refreshTimeStamp=" + RemindUtils.time2String(refreshTimeStamp));
        Intent intent = new Intent(Constants.REFRESH_TASK_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, refreshTimeStamp, pi);
    }

    /**
     * 获取刷新任务执行时间戳
     *
     * @return
     */
    private static long getAutoRefreshTimeStamp() {
        LogUtils.d("lml", "AutoRefreshTask.getAutoRefreshTimeStamp");
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DAY_OF_YEAR, 1);
        return now.getTimeInMillis();
    }

    public static void executeRefreshDailyBroadcastTask(Context context) {
        long refreshTimeStamp = getAutoRefreshTimeStamp();
        LogUtils.d("VoiceBroadcastUtil", "executeRefreshDailyBroadcastTask, refreshTimeStamp=" + RemindUtils.time2String(refreshTimeStamp));
        Intent intent = new Intent(Constants.REFRESH_BROADCAST_TASK_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, refreshTimeStamp, pi);
    }

}
