package com.gionee.secretary.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.AutoRefreshTask;
import com.gionee.secretary.utils.RemindUtils;

public class InitAlarmsService extends IntentService {
    private static final String TAG = "lml";
    private static final long DELAY_MS = 10000;

    public InitAlarmsService() {
        super("InitAlarmsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SystemClock.sleep(DELAY_MS);
        Log.d(TAG, "InitAlarmsService......Clearing and rescheduling alarms.");
        String action = intent.getStringExtra(Constants.RemindConstans.ACTION_KEY);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            RemindUtils.scheduleRecentAlarm(InitAlarmsService.this);
            //added by luorw for GNSPR #70065 20170302 begin
            RemindUtils.noteRecentAlarm(InitAlarmsService.this);
            //added by luorw for GNSPR #70065 20170302 end
            AutoRefreshTask.executeAutoRefreshTask(this);
        } else if (action.equals(Constants.REFRESH_TASK_ACTION)) {
            RemindUtils.refreshScheduleAlarm(InitAlarmsService.this);
            //added by luorw for GNSPR #70065 20170302 begin
            RemindUtils.refreshNoteAlarm(InitAlarmsService.this);
            //added by luorw for GNSPR #70065 20170302 end
            AutoRefreshTask.executeAutoRefreshTask(this);
        }
    }
}
