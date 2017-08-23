package com.gionee.secretary.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;

/**
 * Created by liu on 6/9/16.
 */
public class SnoozeAlarmsService extends IntentService {
    private static final String TAG = "lml";

    public SnoozeAlarmsService() {
        super("SnoozeAlarmsService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        LogUtils.d(TAG, "enter SnoozeAlarmsService.onHandleIntent");
        String action = intent.getStringExtra(Constants.RemindConstans.ACTION_KEY);
        if (Constants.NORMAL_REMIND_ACTION.equals(action)) {
            int scheduleId = intent.getIntExtra(Constants.RemindConstans.SCHEDULEID_KEY, -1);
            int notificationId = scheduleId;
            if (notificationId != Constants.RemindConstans.INVALID_NOTIFICATION_ID) {
                NotificationManager nm =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(Constants.RemindConstans.SCHEDULE_NOTIFICATION, notificationId);
                long alarmTime = System.currentTimeMillis() + Constants.RemindConstans.SNOOZE_DELAY;
                RemindUtils.scheduleSnoozeAlarm(SnoozeAlarmsService.this, scheduleId, alarmTime);
                LogUtils.d(TAG, "cancel nm notificationId = " + notificationId);
            }
        } else if (Constants.NOTE_REMIND_ACTION.equals(action)) {
            int noteId = intent.getIntExtra(Constants.RemindConstans.NOTE_KEY, -1);
            if (noteId != Constants.RemindConstans.INVALID_NOTIFICATION_ID) {
                NotificationManager nm =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(Constants.RemindConstans.NOTE_NOTIFICATION, noteId);
                long alarmTime = System.currentTimeMillis() + Constants.RemindConstans.SNOOZE_DELAY;
                RemindUtils.noteSnoozeAlarm(SnoozeAlarmsService.this, noteId, alarmTime);
                LogUtils.d(TAG, "cancel nm notificationId = " + noteId);
            }
        }
        stopSelf();
    }
}
