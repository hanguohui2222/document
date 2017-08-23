package com.gionee.secretary.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.service.RemindService;
import com.gionee.secretary.utils.IntelligentRemindUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.R;

/**
 * Created by liu on 5/13/16.
 */
public class RemindReceiver extends BroadcastReceiver {
    private static final String TAG = "lml";
    static PowerManager.WakeLock mStartingService;
    static final Object mStartingServiceSync = new Object();

    public RemindReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "RemindReceiver.onReceive: a=" + action + " " + intent.toString());
        if (Constants.INTELLIGENT_REMIND_ACTION.equals(action)) {
            intelligentRemind(context, intent);
        } else if (Constants.NORMAL_REMIND_ACTION.equals(action)) {
            int scheduleId = intent.getIntExtra(Constants.RemindConstans.SCHEDULEID_KEY, -1);
            if (ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId) == null) {
                LogUtils.e(TAG, "RemindReceiver......onReceive......schedule is deleted or not exist, scheduleId=" + scheduleId);
                return;
            }
            String msg = intent.getStringExtra(Constants.RemindConstans.MSG_KEY);
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, RemindService.class);
            serviceIntent.putExtras(intent);
            serviceIntent.putExtra(Constants.RemindConstans.ACTION_KEY, action);
            serviceIntent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, scheduleId);
            serviceIntent.putExtra(Constants.RemindConstans.MSG_KEY, msg);
            beginStartingService(context, serviceIntent);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Constants.REFRESH_TASK_ACTION.equals(action)) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, RemindService.class);
            serviceIntent.putExtras(intent);
            serviceIntent.putExtra("action", action);
            beginStartingService(context, serviceIntent);
        } else if (Constants.NOTE_REMIND_ACTION.equals(action)) {
            int noteId = intent.getIntExtra(Constants.RemindConstans.NOTE_KEY, -1);
            if (VoiceNoteDao.getInstance(context).getVoiceNote(noteId) == null) {
                LogUtils.e(TAG, "RemindReceiver......onReceive......note is deleted or not exist, noteId=" + noteId);
                return;
            }
            if (!RemindUtils.isRemindEnable(context)) {
                return;
            }
            String msg = intent.getStringExtra(Constants.RemindConstans.MSG_KEY);
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, RemindService.class);
            serviceIntent.putExtras(intent);
            serviceIntent.putExtra(Constants.RemindConstans.ACTION_KEY, action);
            serviceIntent.putExtra(Constants.RemindConstans.NOTE_KEY, noteId);
            serviceIntent.putExtra(Constants.RemindConstans.MSG_KEY, msg);
            beginStartingService(context, serviceIntent);
        }
        //added by luorw for  GNSPR #67676 20170220 begin
        //1分钟密码冷却时间到，更新密码冷却状态并发通知刷新界面
        else if (Constants.FREEZING_PASSWORD_ACTION.equals(action)) {
            PasswordModel.getInstance(context).updateFreezePwdState(false);
            Intent intentPwd = new Intent();
            intentPwd.setAction(Constants.UNDO_FREEZING_PASSWORD_ACTION);
            context.sendBroadcast(intentPwd);
        }
        //added by luorw for  GNSPR #67676 20170220 end
    }

    public static void intelligentRemind(Context context, Intent intent) {
        int scheduleId = intent.getIntExtra(Constants.RemindConstans.SCHEDULEID_KEY, -1);
        BaseSchedule schedule = ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId);
        if (schedule == null) {
            LogUtils.e(TAG, "RemindReceiver......scheduleIntelligentRemind......schedule is deleted or not exist, scheduleId=" + scheduleId);
            return;
        }
        if (SettingModel.getInstance(context).isRemindSchedule()) {
            IntelligentRemindUtils remind = new IntelligentRemindUtils(context, schedule);
            remind.computeDistanceTime(schedule);
        }
    }


    public static void scheduleNormalRemind(Context context, int scheduleId, String msg) {
        BaseSchedule schedule = ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId);
        if (schedule == null) {
            LogUtils.d(TAG, "RemindReceiver.....schedule not found..scheduleId=" + scheduleId);
            return;
        }

        LogUtils.d(TAG, "RemindReceiver.....................scheduleNormalRemind......................" + msg);
        String scheduleTime = RemindUtils.time2String(schedule.getRemindDate());
        long systemTime = System.currentTimeMillis();
        LogUtils.d(TAG, "RemindReceiver....................scheduleNormalRemind ........remindDate=" + scheduleTime + "(" + schedule.getRemindDate()
                + ")" + ", 当前时间：" + RemindUtils.time2String(systemTime) + "(" + systemTime + ")");

        //modify by zhengjl at 2017-1-19 for GNSPR #65237 begin
        if (SettingModel.getInstance(context).getRemindScheduleSwitch()) {
            //modify by luorw at 2017-4-10 for GNNCR #78846 begin
            RemindUtils.generateAlerts(context, schedule);
            //modify by luorw at 2017-4-10 for GNNCR #78846 end
        }
        //modify by zhengjl at 2017-1-19 for GNSPR #65237 end
    }

    public static void noteRemind(Context context, int noteId) {
        VoiceNoteBean voiceNote = VoiceNoteDao.getInstance(context).getVoiceNote(noteId);
        if (voiceNote == null) {
            LogUtils.e(TAG, "RemindReceiver......onReceive......note is deleted or not exist, noteId=" + noteId);
            return;
        }
        String remindTime = RemindUtils.time2String(voiceNote.getRemindDate());
        long systemTime = System.currentTimeMillis();
        LogUtils.d(TAG, "RemindReceiver....................scheduleNormalRemind ........remindDate=" + remindTime + "(" + voiceNote.getRemindDate()
                + ")" + ", 当前时间：" + RemindUtils.time2String(systemTime) + "(" + systemTime + ")");
        RemindUtils.generateNoteAlerts(context, voiceNote);
    }

    /**
     * Start the service to process the current event notifications, acquiring the wake lock before returning
     * to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
            LogUtils.d(TAG, "beginStartingService");
        }
    }

    /**
     * Called back by the service when it has finished processing notifications, releasing the wake lock if
     * the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                    LogUtils.d(TAG, "finishStartingService");
                }
            }
        }
    }
}
