package com.gionee.secretary.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;


import java.util.Calendar;

/**
 * Created by liyy on 16-3-31.
 */
public class CleanTask {

    public static void resetCleanTaskTimeStamp(Context context) {
        long cleanTimeStamp = getAutoCleanTimeStamp();
        Intent intent = new Intent("com.gionee.secretary.CLEANTASK");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cleanTimeStamp, pi);
    }

    /**
     * 执行自动清除任务
     *
     * @param context
     */
    public static void executeAutoCleanTask(Context context) {
        long cleanTimeStamp = getAutoCleanTimeStamp();
        Intent intent = new Intent("com.gionee.secretary.CLEANTASK");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cleanTimeStamp, pi);
    }

    /**
     * 获取清除任务执行时间磋
     *
     * @return
     */
    private static long getAutoCleanTimeStamp() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 1);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DAY_OF_YEAR, 1);
        return now.getTimeInMillis();
    }

    /**
     * 是否立即执行清除任务
     */
    public static boolean isUpdateNow(Context context) {
        return false;
//        long cleanTimeStamp = CleanTaskDao.obtainCleanTimeStamp(context);
//        if(cleanTimeStamp == 0) {
//            return false;
//        }
//        Calendar calendar = Calendar.getInstance();
//        long nowTimeStamp = calendar.getTimeInMillis();
//        if (nowTimeStamp > cleanTimeStamp) {
//            return true;
//        }
//        return false;
    }
}
