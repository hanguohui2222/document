package com.gionee.secretary.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.module.settings.SettingModel;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by luorw on 3/29/17.
 */
public class VoiceBroadcastUtil {
    private static final String TAG = "VoiceBroadcastUtil";

    /**
     * @param context
     */
    public static void executeBroadcastTask(Context context) {
        if (!getVoiceBroadcastEnable(context)) {
            return;
        }
        long broadcastTimeStamp = getBroadcastTime(context);
        LogUtils.d("VoiceBroadcastUtil", "executeBroadcastTask, broadcastTimeStamp=" + RemindUtils.time2String(broadcastTimeStamp));
        Intent intent = new Intent(Constants.START_BROADCAST_TASK_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, broadcastTimeStamp, pi);
    }

    public static boolean getVoiceBroadcastEnable(Context context) {
        SettingModel settingModel = SettingModel.getInstance(context);
        return settingModel.getBroadcastScheduleSwitch();
    }

    public static long getBroadcastTime(Context context) {
        LogUtils.d("VoiceBroadcastUtil", "      getBroadcastTime");
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String broadcastTime = SettingModel.getInstance(context).getBroadcastTime();
        if (!"".equals(broadcastTime) && broadcastTime != null) {
            Date broadcastDate = DateUtils.formatDateForBroadcast(broadcastTime);
            calendar.set(Calendar.HOUR_OF_DAY, broadcastDate.getHours());
            calendar.set(Calendar.MINUTE, broadcastDate.getMinutes());
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
        }
        return calendar.getTimeInMillis();
    }

    public static void broadcastAlarmCancel(Context context) {
        LogUtils.d(TAG, "broadcastAlarmCancel.............");
        if (!getVoiceBroadcastEnable(context)) return;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(Constants.START_BROADCAST_TASK_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 与上面的intent匹配（filterEquals(intent)）的闹钟会被取消
        alarmMgr.cancel(pi);
    }

    /**
     * 先取消掉原有的Alarm，再根据新设置的时间启动新的
     *
     * @param context
     */
    public static void broadcastAlarmEdit(Context context) {
        if (!getVoiceBroadcastEnable(context)) return;
        LogUtils.d(TAG, "broadcastAlarmEdit.............");
        broadcastAlarmCancel(context);
        //设置播报时间，如果设置的播报时间比现在的时间早则不刷新播报，否则刷新播报
        if (VoiceBroadcastUtil.getBroadcastTime(context) >= System.currentTimeMillis()) {
            executeBroadcastTask(context);
        }
    }
}
