package com.gionee.secretary.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.ui.activity.NoteDetailActivity;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.constants.RemindTime;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.receiver.RemindReceiver;
import com.gionee.secretary.service.SnoozeAlarmsService;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.ui.activity.CardDetailsActivity;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liu on 5/29/16.
 */
public class RemindUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "lml";


    public static void scheduleRecentAlarm(Context context) {
        LogUtils.d("lml", "scheduleRecentAlarm......system time:" + RemindUtils.time2String(System.currentTimeMillis()));
        if (!isRemindEnable(context)) return;
        List<BaseSchedule> schedules = RemindUtils.getRecentTwoDaySchedule(context);
        Iterator iterator = schedules.iterator();
        while (iterator.hasNext()) {
            BaseSchedule sch = (BaseSchedule) iterator.next();
            if (DEBUG) {
                LogUtils.d(TAG, "sch = " + sch);
            }
            int hour = RemindUtils.getHourOfDate(sch.getDate());
            LogUtils.d(TAG, "schedule id = " + sch.getId() + ", title = " + sch.getTitle() + ", Date = " + DateUtils.date2String3(sch.getDate()) + ", hour = " + hour);
            if (sch.getIsSmartRemind() == Constants.NOT_REMIND || sch.getIsRemindActive() == Constants.REMIND_DONE) {
//                    || getYearOfTime(sch.getRemindDate()) == 1970)  {
                continue;
            }
//            if (sch.getIsSmartRemind() != -1 && hour > RemindTime.MAX_HOUR_PREPARE) {
            LogUtils.d(TAG, "schedule id = " + sch.getId());
            createScheduleRemind(context, sch);
        }
    }

    //added by luorw for GNSPR #70065 20170302 begin
    public static void noteRecentAlarm(Context context) {
        LogUtils.d("lml", "noteRecentAlarm......system time:" + RemindUtils.time2String(System.currentTimeMillis()));
        if (!isRemindEnable(context)) return;
        List<VoiceNoteBean> notes = VoiceNoteDao.getInstance(context).getVoiceNoteListByDate();
        Iterator iterator = notes.iterator();
        while (iterator.hasNext()) {
            VoiceNoteBean voiceNoteBean = (VoiceNoteBean) iterator.next();
            if (voiceNoteBean.getRemindDate() == 0 || voiceNoteBean.getIsRemindActive() == Constants.REMIND_DONE) {
                continue;
            }
            noteAlarm(context, voiceNoteBean);
        }
    }

    public static void refreshNoteAlarm(Context context) {
        LogUtils.d("lml", "noteRecentAlarm......system time:" + RemindUtils.time2String(System.currentTimeMillis()));
        if (!isRemindEnable(context)) return;
        List<VoiceNoteBean> notes = VoiceNoteDao.getInstance(context).getVoiceNoteListByDate();
        Iterator iterator = notes.iterator();
        while (iterator.hasNext()) {
            VoiceNoteBean voiceNoteBean = (VoiceNoteBean) iterator.next();
            if (voiceNoteBean.getRemindDate() == 0 || voiceNoteBean.getIsRemindActive() != Constants.REMIND_NOT_ACTIVE) {
                continue;
            }
            noteAlarm(context, voiceNoteBean);
        }
    }
    //added by luorw for GNSPR #70065 end

    public static void refreshScheduleAlarm(Context context) {
        LogUtils.d(TAG, "refreshScheduleAlarm......system time:" + RemindUtils.time2String(System.currentTimeMillis()));
        if (!isRemindEnable(context)) return;
        List<BaseSchedule> schedules = RemindUtils.getRecentTwoDaySchedule(context);
        Iterator iterator = schedules.iterator();
        while (iterator.hasNext()) {
            BaseSchedule sch = (BaseSchedule) iterator.next();
            if (DEBUG) {
                LogUtils.d(TAG, "sch = " + sch);
            }
            if (sch.getIsSmartRemind() == Constants.NOT_REMIND || sch.getIsRemindActive() != Constants.REMIND_NOT_ACTIVE) {
                continue;
            }
            LogUtils.d(TAG, "schedule id = " + sch.getId() + sch);
            createScheduleRemind(context, sch);
        }
    }

    public static void cancelScheduleAlarm(Context context, BaseSchedule schedule) {
        LogUtils.d(TAG, "cancelScheduleAlarm...schedule:" + schedule);
        List<BaseSchedule> schedules = RemindUtils.getRecentTwoDaySchedule(context);
        Iterator iterator = schedules.iterator();
        int periodId = schedule.getPeriodID();
        while (iterator.hasNext()) {
            BaseSchedule sch = (BaseSchedule) iterator.next();
            if (DEBUG) {
                LogUtils.d(TAG, "sch = " + sch);
            }
            if (sch.getPeriodID() == periodId) {
                LogUtils.d(TAG, "cancelScheduleAlarm.....schedule id = " + sch.getId());
                alarmCancel(context, sch);
            }
        }
    }

    public static void deletePeriodScheduleAlarm(Context context, BaseSchedule schedule, boolean fromCurrent) {
        if (!fromCurrent) {
            cancelScheduleAlarm(context, schedule);
        } else {
            long scheduleTime = schedule.getDate().getTime();
            if (scheduleTime >= getDateTimeStamp(2)) {
                return;
            } else if (scheduleTime >= getDateTimeStamp(1)) {
                alarmCancel(context, schedule);
            } else {
                cancelScheduleAlarm(context, schedule);
            }
        }
    }

    public static void cancelCurrentScheduleAlarm(Context context) {
        LogUtils.d(TAG, "enter cancelScheduleAlarm......");
        List<BaseSchedule> schedules = RemindUtils.getRecentTwoDaySchedule(context);
        Iterator iterator = schedules.iterator();
        while (iterator.hasNext()) {
            BaseSchedule sch = (BaseSchedule) iterator.next();
            if (DEBUG) {
                LogUtils.d(TAG, "sch = " + sch);
            }
            LogUtils.d(TAG, "cancelScheduleAlarm.....schedule id = " + sch.getId());
            alarmCancel(context, sch);
        }
    }

    public static void scheduleNormalAlarm(Context context, BaseSchedule schedule) {
        if (schedule.getIsSmartRemind() == Constants.NOT_REMIND) return;
        Intent intent = new Intent(Constants.NORMAL_REMIND_ACTION);
        intent.setClass(context, RemindReceiver.class);
        intent.putExtra("msg", "普通提醒");
        intent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, schedule.getId());
        PendingIntent pi = PendingIntent.getBroadcast(context, schedule.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
//        long alertTime = getAlertTime(schedule);
//        long alertTime = schedule.getDate().getTime();
        long alertTime = schedule.getRemindDate();
        LogUtils.d(TAG, "scheduleNormalAlarm.......alertTime=" + RemindUtils.time2String(alertTime) + schedule);
        am.setExact(AlarmManager.RTC_WAKEUP, alertTime, pi);
        RemindUtils.updateScheduleRemindState(context, schedule, Constants.IS_REMIND_ACTIVE);
    }

    public static void scheduleSnoozeAlarm(Context context, int scheduleId, long alertTime) {
        BaseSchedule schedule = ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId);
        if (schedule == null || schedule.getIsSmartRemind() == Constants.NOT_REMIND) return;
        Intent intent = new Intent(Constants.NORMAL_REMIND_ACTION);
        intent.putExtra("msg", "普通提醒");
        intent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, scheduleId);
        PendingIntent pi = PendingIntent.getBroadcast(context, scheduleId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, alertTime, pi);
        updateScheduleRemindState(context, schedule, Constants.IS_REMIND_ACTIVE);
    }

    public static void updateScheduleRemindState(Context context, BaseSchedule schedule, int newState) {
        if (schedule.getIsRemindActive() != newState) {
            LogUtils.d(TAG, "updateScheduleRemindState, newState=" + newState);
            schedule.setIsActive(newState);
            new UpdateScheduleThread(context, schedule).start();
        }
    }


    public static void scheduleRemind(Context context, BaseSchedule schedule) {
        if (!isRemindEnable(context)) return;
        if ("一次".equals(schedule.getRemindPeriod())) {
            createScheduleRemind(context, schedule);
            return;
        }
        addScheduleAlarm(context, schedule.getPeriodID());
    }

    public static void addScheduleAlarm(Context context, int periodId) {
        LogUtils.d(TAG, "addScheduleAlarm");
        if (!isRemindEnable(context)) return;
        List<BaseSchedule> schedules = RemindUtils.getRecentTwoDaySchedule(context);
        Iterator iterator = schedules.iterator();
        while (iterator.hasNext()) {
            BaseSchedule sch = (BaseSchedule) iterator.next();
            if (DEBUG) {
                LogUtils.d(TAG, "sch = " + sch);
            }
            if (sch.getIsSmartRemind() != Constants.NOT_REMIND && sch.getPeriodID() == periodId) {
                LogUtils.d(TAG, "addScheduleAlarm:" + sch);
                createScheduleRemind(context, sch);
            }
        }
    }

    public static void startScheduleRemind(Context context, BaseSchedule schedule, int remindType) {
        if (isRemindEnable(context)) {
            //智能提醒;
            schedule.setIsSmartRemind(remindType);
            createScheduleRemind(context, schedule);
        }
    }

    public static void createScheduleRemind(Context context, BaseSchedule schedule) {
        LogUtils.d(TAG, "createScheduleRemind......schedule=" + schedule);
        long systemTime = System.currentTimeMillis();
        long remindTime = schedule.getRemindDate();
        if ((schedule.getType() == Constants.SELF_CREATE_TYPE && !schedule.getIsAllDay() && (systemTime - schedule.getDate().getTime()) > Constants.RemindConstans.DISMISS_OVERDUE_TIME)
                || (schedule.getType() != Constants.SELF_CREATE_TYPE && (systemTime - remindTime) > Constants.RemindConstans.DISMISS_OVERDUE_TIME)
                || (schedule.getIsAllDay() && (systemTime - remindTime) > Constants.RemindConstans.DISMISS_OVERDUE_TIME) && (schedule.isSmartRemind != 1)) {
            LogUtils.d(TAG, "createScheduleRemind......overdue alarm dismissed");
            return;
        }
        Date date = new Date(remindTime);//schedule.getDate();
        LogUtils.d(TAG, "createScheduleRemind.....date:" + date + ", remindDate: " + RemindUtils.time2String(schedule.getRemindDate()));
        if (RemindUtils.isSameDate(date, new Date())           // 当天
                || RemindUtils.isSameDate(date, RemindUtils.getTormorrowDate())) {      //第二天
            if (RemindUtils.isNomalRemind(schedule)) {
                scheduleNormalAlarm(context, schedule);
            } else {
                //智能提醒;
                IntelligentRemindUtils remindUtils = new IntelligentRemindUtils(context, schedule);
                remindUtils.start();
            }
        }
    }

    public static boolean isRemindEnable(Context context) {
        SettingModel sharedPref = SettingModel.getInstance(context);
        return sharedPref.isRemindSchedule() ? true : false;
    }

    public static long getReservedTimeForEvent(int type) {
        long reserveTime;
        switch (type) {
            case Constants.TRAIN_TYPE:
                reserveTime = RemindTime.TIME_RESERVED_FOR_TICKET;
                break;
            case Constants.FLIGHT_TYPE:
                reserveTime = RemindTime.TIME_RESERVED_FOR_FLIGHT;
                break;
            case Constants.MOVIE_TYPE:
                reserveTime = RemindTime.TIME_RESERVED_FOR_MOVIE;
                break;
            default:
                reserveTime = 0;
                break;
        }
        LogUtils.d(TAG, "reserveTime=" + (reserveTime / 60 / 1000));
        return reserveTime;
    }

    public static String getDestAddress(BaseSchedule schedule) {
        String address = null;
        int type = schedule.getType();
        switch (type) {
            case Constants.TRAIN_TYPE:
                TrainSchedule trainSchedule = (TrainSchedule) schedule;
                address = trainSchedule.getDeparture();
                break;
            case Constants.FLIGHT_TYPE:
                FlightSchedule flightSchedule = (FlightSchedule) schedule;
                address = flightSchedule.getStartAddress();
                break;
            case Constants.MOVIE_TYPE:
                MovieSchedule movieSchedule = (MovieSchedule) schedule;
                address = movieSchedule.getCinemaName();
                break;
            case Constants.HOTEL_TYPE:
                HotelSchedule hotelSchedule = (HotelSchedule) schedule;
                address = hotelSchedule.getHotelName();
                break;
            case Constants.SELF_CREATE_TYPE:
                SelfCreateSchedule selfCreateSchedule = (SelfCreateSchedule) schedule;
                address = selfCreateSchedule.getAddress();
            default:
                break;
        }
        return address;
    }

    public static void alarmEdit(Context context, BaseSchedule schedule) {
        LogUtils.d(TAG, "alarmEdit.............scheduleId=" + schedule.getId() + schedule);
        alarmCancel(context, schedule);
        if (!isRemindEnable(context)) return;
        createScheduleRemind(context, schedule);
    }

    public static void alarmCancel(Context context, BaseSchedule sch) {
        LogUtils.d(TAG, "alarmCancel.............schedule:" + sch);
        int scheduleId = sch.getId();
        BaseSchedule schedule = ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId);
        if (schedule == null) {
            LogUtils.e(TAG, "alarmCancel......schedule not exist, scheduleId = " + scheduleId);
            return;
        }
        if (schedule.getIsSmartRemind() != Constants.NOT_REMIND && schedule.getIsRemindActive() == Constants.REMIND_DONE) {
            notificationCancel(context, schedule);
            //modified by luorw for GNSPR #65279 20170117 begin
            //RemindUtils.updateScheduleRemindState(context, schedule, Constants.REMIND_NOT_ACTIVE);
            //modified by luorw for GNSPR #65279 20170117 end
            return;
        }
        if (schedule.getIsSmartRemind() == Constants.NOT_REMIND || schedule.getIsRemindActive() != Constants.IS_REMIND_ACTIVE)
            return;
        Intent intent;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        if (isNomalRemind(schedule)) {
            LogUtils.d(TAG, "alarmCancel........NORMAL_REMIND");
            intent = new Intent(Constants.NORMAL_REMIND_ACTION);
        } else {
            intent = new Intent(Constants.INTELLIGENT_REMIND_ACTION);
        }
        LogUtils.d(TAG, "alarmCancel............schedule.getId = " + schedule.getId());
        PendingIntent pi = PendingIntent.getBroadcast(context, schedule.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 与上面的intent匹配（filterEquals(intent)）的闹钟会被取消
        alarmMgr.cancel(pi);
        RemindUtils.updateScheduleRemindState(context, schedule, Constants.REMIND_NOT_ACTIVE);
    }

    public static void notificationCancel(Context context, BaseSchedule schedule) {
        LogUtils.d(TAG, "notificationCancel.............");
        if (schedule.getIsSmartRemind() == Constants.NOT_REMIND || schedule.getIsRemindActive() != Constants.REMIND_DONE)
            return;
        int notificationId = schedule.getId();
        LogUtils.d(TAG, "notificationCancel.............notificationId = " + notificationId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.RemindConstans.SCHEDULE_NOTIFICATION, notificationId);
    }

    public static boolean isNomalRemind(BaseSchedule schedule) {
        boolean remindType;
        String address = getDestAddress(schedule);
        LogUtils.d(TAG, "enter isNomalRemind()......address = " + address);
        if (address == null || address.equals("null") || "".equals(address)
                || (schedule.getType() == Constants.SELF_CREATE_TYPE && !"智能提醒".equals(schedule.getRemindType()))) {
            remindType = true;
        } else {
            if (schedule.getType() == Constants.HOTEL_TYPE) {
                remindType = true;
            } else {
                remindType = false;
            }
        }
        return remindType;
    }

    public static void generateAlerts(Context context, BaseSchedule schedule) {
        LogUtils.d(TAG, "generateAlerts......schedule: " + schedule);
        int scheduleId = schedule.getId();
        if (ScheduleInfoDao.getInstance(context).getScheduleInfoById(scheduleId) == null) {
            LogUtils.e(TAG, "generateAlerts......schedule not exist, scheduleId = " + scheduleId);
            return;
        }
        printScheduleTime(schedule);
        if (schedule.getRemindDate() == Constants.RemindConstans.INVALID_REMIND_TIME) {
            LogUtils.e(TAG, "generateAlerts......INVALID_REMIND_TIME");
            return;
        } else if (System.currentTimeMillis() - schedule.getRemindDate() > Constants.RemindConstans.TWO_YEAR_BEFORE) {
            LogUtils.e(TAG, "generateAlerts......TWO_YEAR_BEFORE");
            return;
        }
        postNotification(context, schedule);
        updateScheduleRemindState(context, schedule, Constants.REMIND_DONE);
    }

    public static void generateNoteAlerts(Context context, VoiceNoteBean note) {
        int noteId = note.getId();
        if (VoiceNoteDao.getInstance(context).getVoiceNote(noteId) == null) {
            return;
        }
        if (note.getRemindDate() == Constants.RemindConstans.INVALID_REMIND_TIME) {
            LogUtils.e(TAG, "generateAlerts......INVALID_REMIND_TIME");
            return;
        } else if (System.currentTimeMillis() - note.getRemindDate() > Constants.RemindConstans.TWO_YEAR_BEFORE) {
            LogUtils.e(TAG, "generateAlerts......TWO_YEAR_BEFORE");
            return;
        }
        postNoteNotification(context, note);
        updateNoteRemindState(context, note, Constants.REMIND_DONE);
    }

    public static synchronized void postNotification(Context context, BaseSchedule schedule) {
        int notificationId = schedule.getId();
        LogUtils.d(TAG, "postNotification.............notificationId = " + notificationId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.icon_notification);
        builder.setContentTitle(getNotificationTitle(context, schedule));
        builder.setContentText(getNotificationContent(context, schedule));
        PendingIntent pIntent = createAlertActivityIntent(context, schedule);
        PendingIntent snoozeIntent = createSnoozeIntent(context, schedule);
        builder.setContentIntent(pIntent);
        builder.setShowWhen(false);
        builder.setUsesChronometer(true);
        builder.addAction(R.drawable.ic_alarm_holo_dark, context.getString(R.string.remind_later), snoozeIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        SettingModel settinModel = SettingModel.getInstance(context);
        String ringtoneUri = settinModel.getDefaultRingtoneUri();
        //added by luorw for GNSPR #71067 20170307 begin
        if (ringtoneUri != null && !"".equals(ringtoneUri)) {
            Uri uri = Uri.parse(ringtoneUri);
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor actualimagecursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (ringtoneUri != null && actualimagecursor.getCount() != 0) {
                builder.setSound(uri);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL);
            }
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        //added by luorw for GNSPR #71067 20170307 end
        notificationManager.notify(Constants.RemindConstans.SCHEDULE_NOTIFICATION, notificationId, builder.build());
        context.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
    }

    public static synchronized void postNoteNotification(Context context, VoiceNoteBean note) {
        int notificationId = note.getId();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.icon_notification);
        builder.setContentTitle("备忘");
        //modify by zhengjl at 2017-1-22 for GNSPR #65750 begin
//        builder.setContentText(note.getContent());
        builder.setContentText(note.getTitle());
        //modify by zhengjl at 2017-1-22 for GNSPR #65750 end
        PendingIntent pIntent = createNoteAlertActivityIntent(context, note);
        PendingIntent snoozeIntent = createNoteSnoozeIntent(context, note);
        builder.setContentIntent(pIntent);
        builder.setShowWhen(false);
        builder.setUsesChronometer(true);
        builder.addAction(R.drawable.ic_alarm_holo_dark, context.getString(R.string.remind_later), snoozeIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        //added by luorw for GNSPR #71067 20170307 begin
        SettingModel settinModel = SettingModel.getInstance(context);
        String ringtoneUri = settinModel.getDefaultRingtoneUri();
        if (ringtoneUri != null && !"".equals(ringtoneUri)) {
            Uri uri = Uri.parse(ringtoneUri);
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor actualimagecursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (ringtoneUri != null && actualimagecursor.getCount() != 0) {
                builder.setSound(uri);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL);
            }
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        //added by luorw for GNSPR #71067 20170307 end
        notificationManager.notify(Constants.RemindConstans.NOTE_NOTIFICATION, notificationId, builder.build());
    }

    //added by luorw for GNSPR #70074 20170307 begin
    public static void cancelNoteNotification(Context context, int notifyId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.RemindConstans.NOTE_NOTIFICATION, notifyId);
    }
    //added by luorw for GNSPR #70074 20170307 end

    /**
     * 日程稍后提醒
     *
     * @param context
     * @param schedule
     * @return
     */
    private static PendingIntent createSnoozeIntent(Context context, BaseSchedule schedule) {
        Intent intent = new Intent();
        intent.setClass(context, SnoozeAlarmsService.class);
        intent.putExtra(Constants.RemindConstans.ACTION_KEY, Constants.NORMAL_REMIND_ACTION);
        intent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, schedule.getId());
        return PendingIntent.getService(context, schedule.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 备忘稍后提醒
     *
     * @param context
     * @param noteBean
     * @return
     */
    private static PendingIntent createNoteSnoozeIntent(Context context, VoiceNoteBean noteBean) {
        Intent intent = new Intent();
        intent.setClass(context, SnoozeAlarmsService.class);
        intent.putExtra(Constants.RemindConstans.ACTION_KEY, Constants.NOTE_REMIND_ACTION);
        intent.putExtra(Constants.RemindConstans.NOTE_KEY, noteBean.getId());
        return PendingIntent.getService(context, noteBean.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createAlertActivityIntent(Context context, BaseSchedule schedule) {
        Intent clickIntent = new Intent();
        clickIntent.putExtra(Constants.RemindConstans.SCHEDULE_KEY, schedule);
        clickIntent.putExtra(Constants.NOTIFACATION_FLAG, true);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        clickIntent.setClass(context, CardDetailsActivity.class);

        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, schedule.getId(), clickIntent, PendingIntent.FLAG_ONE_SHOT
                | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createNoteAlertActivityIntent(Context context, VoiceNoteBean note) {
        Intent clickIntent = new Intent();
        clickIntent.putExtra(Constants.RemindConstans.NOTE_KEY, note.getId());
        clickIntent.putExtra(Constants.NOTIFACATION_FLAG, true);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        // Gionee sunyang 2017-01-18 modify for GNSPR #65546 begin
        clickIntent.setClass(context, NoteDetailActivity.class);
        // Gionee sunyang 2017-01-18 modify for GNSPR #65546 end

        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, note.getId(), clickIntent, PendingIntent.FLAG_ONE_SHOT
                | PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static String getNotificationContent(Context context, BaseSchedule schedule) {
        String content;
        int type = schedule.getType();
        if (type == Constants.SELF_CREATE_TYPE) {
            SelfCreateSchedule selfCreateSchedule = (SelfCreateSchedule) schedule;
            Date startTime = selfCreateSchedule.getDate();
            Date endTime = selfCreateSchedule.getEndtime();
            if (endTime == null) {   // created by voice
                LogUtils.d(TAG, "startTime or endTime is invalid");
                return DateUtils.time2String(schedule.getDate());
            }
            String timeDivide = context.getString(R.string.time_between);
            if (schedule.getIsAllDay()) {
                content = "今天";
            } else if (RemindUtils.isSameDate(startTime, endTime)) {
                content = DateUtils.time2String(startTime) + timeDivide + DateUtils.time2String(endTime);
                //                    + ", remindDate:" +schedule.getRemindDate();
            } else {
                content = DateUtils.getDate(startTime) + " " + DateUtils.time2String(startTime)
                        + timeDivide + DateUtils.getDate(endTime) + " " + DateUtils.time2String(endTime);
            }

        } else if (type == Constants.BANK_TYPE) {
            content = context.getString(R.string.time_to_repay);
        } else if (type == Constants.HOTEL_TYPE) {
            content = "今天";
        } else {
            long remindtime = schedule.getRemindDate();
            Date remindDate = new Date(remindtime);
            content = DateUtils.time2String(remindDate);
        }
        return content;
    }

    public static String getNotificationTitle(Context context, BaseSchedule schedule) {
        String title = schedule.getTitle();
        switch (schedule.getType()) {
            case Constants.FLIGHT_TYPE:
                title = context.getString(R.string.remind_title_flight);
                break;
            case Constants.HOTEL_TYPE:
                //modified by luorw for GNSPR #71382 20170310 begin
                String hotelName = ((HotelSchedule) schedule).getHotelName();
                if (TextUtils.isEmpty(hotelName) || "null".equals(hotelName)) {
                    title = "酒店";
                } else {
                    title = hotelName;
                }
                //modified by luorw for GNSPR #71382 20170310 end
                break;
            case Constants.MOVIE_TYPE:
                title = ((MovieSchedule) schedule).getMovieName();
                break;
            case Constants.TRAIN_TYPE:
                title = context.getString(R.string.remind_title_train);
                break;
        }
        return title;
    }

    public static int getTripMode(BaseSchedule schedule, Context context) {
        int tripMode;
        if (schedule.getType() == Constants.SELF_CREATE_TYPE) {
            tripMode = getManualCardTransportMethod(context, schedule);
        } else {
            tripMode = getDefaultTransportMethod(context);
        }

        LogUtils.d(TAG, "getTripMode......tripMode=" + tripMode);
        return tripMode;
    }

    public static int getDefaultTransportMethod(Context context) {
        int tripMode;
        String tripModeString = SettingModel.getInstance(context).getDefaultTravelMethod();
        String[] travelModeAdapter = context.getResources().getStringArray(R.array.travel_method_value);
        LogUtils.d(TAG, "getDefaultTransportMethod......tripModeString=" + tripModeString);
        if (tripModeString.equals(travelModeAdapter[1])) {
            tripMode = Constants.TYPE_WALK;
        } else if (tripModeString.equals(travelModeAdapter[2])) {
            tripMode = Constants.TYPE_DRIVE;
        } else if (tripModeString.equals(travelModeAdapter[4])) {
            tripMode = Constants.TYPE_BUS;
        } else {
            tripMode = Constants.TYPE_DRIVE;
        }
        LogUtils.d(TAG, "getDefaultTransportMethod......tripMode=" + tripMode);
        return tripMode;
    }

    public static int getManualCardTransportMethod(Context context, BaseSchedule schedule) {
        int tripMode;
        String tripModeString = ((SelfCreateSchedule) schedule).getTripMode();
        LogUtils.d(TAG, "getManualCardTransportMethod......tripModeString=" + tripModeString);
        String[] travelModeAdapter = context.getResources().getStringArray(R.array.travel_method_entry);
        if (tripModeString.equals(travelModeAdapter[1])) {
            tripMode = Constants.TYPE_WALK;
        } else if (tripModeString.equals(travelModeAdapter[2])) {
            tripMode = Constants.TYPE_DRIVE;
        } else if (tripModeString.equals(travelModeAdapter[4])) {
            tripMode = Constants.TYPE_BUS;
        } else {
            tripMode = Constants.TYPE_DRIVE;
        }
        LogUtils.d(TAG, "getDefaultTransportMethod......tripMode=" + tripMode);
        return tripMode;
    }

    public static void printScheduleTime(BaseSchedule schedule) {
        LogUtils.d(TAG, "schedule info: date = " + DateUtils.formatDate2String2(schedule.getDate()) + ", remindDate = " + RemindUtils.time2String(schedule.getRemindDate())
                + ", System time：" + RemindUtils.time2String(System.currentTimeMillis()));
    }

    public static String time2String(long time) {
        Date date = new Date(time);
//        date.setTime(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    public static int getHourOfDate(Date date) {
        int hourCnt;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        hourCnt = calendar.get(Calendar.HOUR_OF_DAY);
        return hourCnt;
    }

    public static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    public static Date getTormorrowDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTimeInMillis());
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);//1001 天加1
        Date tmw = calendar.getTime();

        return tmw;
    }

    public static long getDateTimeStamp(int afterToday) {
        LogUtils.d(TAG, "getDateTimeStamp......afterToday count:" + afterToday);
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DAY_OF_YEAR, afterToday);
        return now.getTimeInMillis();
    }

    public static List<BaseSchedule> getRecentTwoDaySchedule(Context context) {
        ScheduleInfoDao scheduleInfoDao = ScheduleInfoDao.getInstance(context);
        Long timeStart = getDateTimeStamp(0);
        Long timeEnd = getDateTimeStamp(2);
        List<BaseSchedule> schedules = scheduleInfoDao.queryScheduleByRemindDate(timeStart, timeEnd);
        return schedules;
    }

    public static void noteAlarmEdit(Context context, VoiceNoteBean note) {
        LogUtils.d(TAG, "alarmEdit.............noteId=" + note.getId() + note);
        if (note.getIsRemindActive() == Constants.REMIND_DONE) {
            return;
        }
        noteAlarmCancel(context, note);
        if (note.getRemindDate() != 0) {
            noteAlarm(context, note);
        }
    }

    public static void noteAlarmCancel(Context context, VoiceNoteBean note) {
        LogUtils.d(TAG, "alarmCancel.............note:" + note.toString());
        int noteId = note.getId();
        VoiceNoteBean voiceNoteBean = VoiceNoteDao.getInstance(context).getVoiceNote(noteId);
        if (voiceNoteBean == null) {
            LogUtils.e(TAG, "alarmCancel......voiceNoteBean not exist, noteId = " + noteId);
            return;
        }
        //added by luorw for GNSPR #70074 20170307 begin
        cancelNoteNotification(context, noteId);
        //added by luorw for GNSPR #70074 20170307 end
        if (voiceNoteBean.getIsRemindActive() != Constants.IS_REMIND_ACTIVE) return;
        Intent intent;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        intent = new Intent(Constants.NOTE_REMIND_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 与上面的intent匹配（filterEquals(intent)）的闹钟会被取消
        alarmMgr.cancel(pi);
        RemindUtils.updateNoteRemindState(context, note, Constants.REMIND_NOT_ACTIVE);
    }

    public static void noteAlarm(Context context, VoiceNoteBean note) {
        Intent intent = new Intent(Constants.NOTE_REMIND_ACTION);
        intent.setClass(context, RemindReceiver.class);
        intent.putExtra("msg", "普通提醒");
        intent.putExtra(Constants.RemindConstans.NOTE_KEY, note.getId());
        PendingIntent pi = PendingIntent.getBroadcast(context, note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        long alertTime = note.getRemindDate();
        LogUtils.d(TAG, "noteAlarm.......alertTime=" + RemindUtils.time2String(alertTime) + note);
        am.setExact(AlarmManager.RTC_WAKEUP, alertTime, pi);
        updateNoteRemindState(context, note, Constants.IS_REMIND_ACTIVE);
    }

    public static void noteSnoozeAlarm(Context context, int noteId, long alertTime) {
        VoiceNoteBean voiceNoteBean = VoiceNoteDao.getInstance(context).getVoiceNote(noteId);
        if (voiceNoteBean == null) return;
        Intent intent = new Intent(Constants.NOTE_REMIND_ACTION);
        intent.putExtra("msg", "普通提醒");
        intent.putExtra(Constants.RemindConstans.NOTE_KEY, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, alertTime, pi);
        updateNoteRemindState(context, voiceNoteBean, Constants.IS_REMIND_ACTIVE);
    }

    public static void updateNoteRemindState(Context context, VoiceNoteBean note, int newState) {
        if (note.getIsRemindActive() != newState) {
            LogUtils.d(TAG, "updateNoteRemindState, newState=" + newState);
            note.setIsActive(newState);
            new UpdateNoteThread(context, note).start();
        }
    }

    private static class UpdateNoteThread extends Thread {
        private WeakReference<Context> mContext;
        private VoiceNoteBean mNote;

        public UpdateNoteThread(Context context, VoiceNoteBean note) {
            mContext = new WeakReference<Context>(context);
            mNote = note;
        }

        @Override
        public void run() {
            final Context context = mContext.get();
            if(context != null){
                VoiceNoteDao.getInstance(context).updateVoiceToDB(mNote);
            }
        }
    }

    private static class UpdateScheduleThread extends Thread {
        private WeakReference<Context> mContext;
        private BaseSchedule mSchedule;

        public UpdateScheduleThread(Context context, BaseSchedule schedule) {
            mContext = new WeakReference<Context>(context);
            mSchedule = schedule;
        }

        @Override
        public void run() {
            final Context context = mContext.get();
            if(context != null){
                ScheduleInfoDao.getInstance(context).updateScheduleToDB(mSchedule, null);
            }
        }

    }

}
