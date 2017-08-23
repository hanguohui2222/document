package com.gionee.secretary.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.constants.RemindTime;

import java.util.Date;

/**
 * Created by liu on 5/13/16.
 */
public class IntelligentRemindUtils implements RouteUtils.IRouteListener {
    private static final String TAG = "lml";
    private long mAlarmTime = System.currentTimeMillis();
    private long mScheduleTime = mAlarmTime + 80 * 1000; //5*60*1000+5*1000;
    private long mGetGDDataTime;
    private double mTripTime;
    private boolean isFirstCompute = true;
    private long mTimeResvForEvent;
    private BaseSchedule mSchedule;
    private RouteUtils mRouteUtils;
    private LocationUtil mLocationUtil;
    private Context mContext;

    String[] mTravelModeAdapter;

    public IntelligentRemindUtils(Context context) {
        mContext = context;
        initSchedule();
    }

    public IntelligentRemindUtils(Context context, BaseSchedule schedule) {
        mContext = context;
        mSchedule = schedule;
        initSchedule();
    }

    private void initSchedule() {
        mGetGDDataTime = System.currentTimeMillis();
        mScheduleTime = mSchedule.getDate().getTime();
        mTravelModeAdapter = mContext.getResources().getStringArray(R.array.travel_method_entry);
        LogUtils.d(TAG, "IntelligentRemind......initSchedule......................mScheduleTime="
                + mScheduleTime + "(" + RemindUtils.time2String(mScheduleTime) + ")");
    }

//    public void setAlarmManager(Context context, long alertTime) {
//        Intent intent = new Intent(Constants.INTELLIGENT_REMIND_ACTION);
//        intent.putExtra("msg", "设置触发下一次时间估算");
//        intent.putExtra("queryGDTime", mGetGDDataTime);
//        intent.putExtra("estimateTimes", isFirstCompute);
//        intent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, mSchedule.getId());
//
//        //定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
//        //也就是发送了action 为"INTELLIGENT_ALERT"的intent
////        PendingIntent pi = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pi = PendingIntent.getBroadcast(context, mSchedule.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        //AlarmManager对象为系统级服务
//        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
//
//        am.setExact(AlarmManager.RTC_WAKEUP, alertTime, pi);
//        RemindUtils.updateScheduleRemindState(mContext, mSchedule, Constants.IS_REMIND_ACTIVE);
//    }

    public void start() {
        if (!RemindUtils.isRemindEnable(mContext) || mSchedule.getIsSmartRemind() == Constants.NOT_REMIND)
            return;
        if (!mSchedule.getIsAllDay() && (mSchedule.getType() == Constants.SELF_CREATE_TYPE && (System.currentTimeMillis() - mSchedule.getDate().getTime()) > 60 * 1000)
                || (mSchedule.getType() != Constants.SELF_CREATE_TYPE && (System.currentTimeMillis() - mSchedule.getRemindDate()) > 60 * 1000)) {
            LogUtils.d(TAG, "intelligentRemind.....start......overdue alarm dismissed");
            return;
        }
        Date date = new Date(mSchedule.getRemindDate());//mSchedule.getDate();
        LogUtils.d(TAG, "start.....date:" + date + ", remindDate: " + RemindUtils.time2String(mSchedule.getRemindDate()));
        if (RemindUtils.isSameDate(date, new Date())           // 当天
                || RemindUtils.isSameDate(date, RemindUtils.getTormorrowDate())) {      //第二天
            LogUtils.d(TAG, "mSchedule: " + mSchedule);
            RemindUtils.printScheduleTime(mSchedule);
            if (RemindUtils.isNomalRemind(mSchedule)) {
                RemindUtils.scheduleNormalAlarm(mContext, mSchedule);
            } else {
                remindEventProcess(mContext, mSchedule);
            }
        }
    }

    //added by luorw 20170108 begin
    private long getAdvanceComputeTime(int type) {
        long time = 0;
        switch (type) {
            case Constants.FLIGHT_TYPE:
                time = 5 * 60 * 60 * 1000;
                break;
            case Constants.TRAIN_TYPE:
                time = 3 * 60 * 60 * 1000;
                break;
            default:
                time = 2 * 60 * 60 * 1000;
                break;
        }
        return time;
    }

    public void remindEventProcess(Context context, BaseSchedule schedule) {
        long currentTime = System.currentTimeMillis();
        long scheduleTime = schedule.getRemindDate();
        long advanceComputeTime = getAdvanceComputeTime(schedule.getType());
        long computeTime = scheduleTime - advanceComputeTime;
        //事件发生前n个小时触发计算(飞机5h，火车3h，电影2h),如果计算时间大于当前时间，则定时在计算时间开始计算路程时间
        if (computeTime > currentTime) {
            LogUtils.i("luorw", "首次计算路程时间：computeTime = " + new Date(computeTime));
            setReadyComputeAlarm(context, computeTime, schedule);
        } else {
            computeDistanceTime(schedule);
        }
    }

    private void setReadyComputeAlarm(Context context, long computeTime, BaseSchedule schedule) {
        Intent intent = new Intent(Constants.INTELLIGENT_REMIND_ACTION);
        intent.putExtra(Constants.RemindConstans.SCHEDULEID_KEY, schedule.getId());
        PendingIntent pi = PendingIntent.getBroadcast(context, schedule.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, computeTime, pi);
        RemindUtils.updateScheduleRemindState(mContext, schedule, Constants.IS_REMIND_ACTIVE);
    }

    public void computeDistanceTime(BaseSchedule schedule) {
        String destAddress = RemindUtils.getDestAddress(schedule);
        if (!RemindUtils.isNomalRemind(schedule)) {
            getTimeFromGDMap(destAddress);
        }
    }

    private void computeAlertProcess(Context context, BaseSchedule schedule, long distanceTime) {
        long computeTime = 0;
        long scheduleTime = schedule.getRemindDate();
        long reservedTime = RemindUtils.getReservedTimeForEvent(schedule.getType());//按日程类型提前预留时间
        long expectTime = scheduleTime - (distanceTime * 1000 + reservedTime);//预期出发时间 = 日程时间 - 路程时间
        long compareTime = expectTime - System.currentTimeMillis();//当前时间与预期出发时间差值
        LogUtils.i("luorw", "computeAlertProcess , scheduleTime = " + new Date(scheduleTime) + " , distanceTime = " + ((float) distanceTime / 60 / 60) + " , expectTime = " + new Date(expectTime) + " , compareTime = " + ((float) compareTime / 1000 / 60 / 60));
        if (compareTime >= RemindTime.TIME_COMPARE_SCHEDULE_3 && compareTime <= RemindTime.TIME_COMPARE_SCHEDULE_5) {//隔1小时再计算开车路程时间
            computeTime = System.currentTimeMillis() + RemindTime.CHECK_INTERVAL_1;
            LogUtils.i("luorw", "computeAlertProcess , 间隔1小时后再计算 ， time = " + new Date(computeTime));
            setReadyComputeAlarm(context, computeTime, schedule);
        } else if (compareTime >= RemindTime.TIME_COMPARE_SCHEDULE_2 && compareTime <= RemindTime.TIME_COMPARE_SCHEDULE_3) {//隔30min再计算开车路程时间
            computeTime = System.currentTimeMillis() + RemindTime.CHECK_INTERVAL_2;
            LogUtils.i("luorw", "computeAlertProcess , 间隔半小时后再计算 ， time = " + new Date(computeTime));
            setReadyComputeAlarm(context, computeTime, schedule);
        } else if (compareTime >= RemindTime.TIME_COMPARE_SCHEDULE_1 && compareTime <= RemindTime.TIME_COMPARE_SCHEDULE_2) {//隔20min再计算开车路程时间
            computeTime = System.currentTimeMillis() + RemindTime.CHECK_INTERVAL_3;
            LogUtils.i("luorw", "computeAlertProcess , 间隔20分钟后再计算 ， time = " + new Date(computeTime));
            setReadyComputeAlarm(context, computeTime, schedule);
        } else {//马上提醒
            LogUtils.i("luorw", "computeAlertProcess , 马上提醒 , compareTime = " + ((float) compareTime / 1000 / 60 / 60));
            RemindUtils.generateAlerts(context, schedule);
        }
    }
    //added by luorw 20170108 end

//    public void eventProcess(Context context, BaseSchedule schedule, long timeQuery, boolean estimateTimes) {
//        long scheduleTime = schedule.getRemindDate();//getAlarmTime();
//        long systemTime = System.currentTimeMillis();
//        long getGDDataInterval;
//        mGetGDDataTime = timeQuery;
//        isFirstCompute = estimateTimes;
//
//        long timeResv = RemindUtils.getReservedTimeForEvent(mSchedule.getType());
//        long latestTimeToRemind = scheduleTime - timeResv;
////        setTimeResvForEvent(timeResv);
//
//        //事件发生前n个小时触发计算(飞机5h，火车3h，电影2h)
//
//        if(isFirstCompute && (systemTime+ getAdvanceComputeTime(schedule.getType())) >= latestTimeToRemind) {
//            LogUtils.d(TAG, "eventProcess..........start compute");
//            isFirstCompute = false;
//
//            scheduleAlarm();        // 返回时间单位：秒
////            alertProcess(context, scheduleTime);  // call back in getRouteTime
//        }else if (isFirstCompute && (systemTime+ getAdvanceComputeTime(schedule.getType()) < latestTimeToRemind)) {//事件发生前5个小时以上则设置在5个小时时计算
//            setAlarmManager(context, (latestTimeToRemind - RemindTime.TIME_BEFORE_SCHEDULE_5) );
//        }
//
//        getGDDataInterval = RemindUtils.getInterval(scheduleTime, timeResv);      //获取抓取高德数据的时间间隔
//        long nextEstimateTime = mGetGDDataTime + getGDDataInterval;
//        if(systemTime >= nextEstimateTime && systemTime < latestTimeToRemind) {
//            LogUtils.d(TAG, "eventProcess.................****");
//            scheduleAlarm();
////            alertProcess(context, scheduleTime);  // call back in getRouteTime
//        }
//
//        long curAlertTime = getAlarmTime();
//
//        LogUtils.d(TAG, "eventProcess......AlarmTime......................current alarm=" + RemindUtils.time2String(curAlertTime)
//                + ", system time=" + systemTime + "(" + RemindUtils.time2String(systemTime) + ")");
//    }
//
//    public void alertProcess(Context context, BaseSchedule schedule, double time) {
//        long systemTime = System.currentTimeMillis();
//        long scheduleTime = schedule.getRemindDate();
////        String destAddress = "西二旗(地铁站)";
//        long timeNeedOnTrip = (long)time;
//        LogUtils.d(TAG, "alertProcess(), timeNeedOnTrip=" + timeNeedOnTrip);
//        long rsvTime = RemindUtils.getReservedTimeForEvent(mSchedule.getType());
//        long timeNeedForSchedule = timeNeedOnTrip * 1000 + rsvTime;
//        long intervalTime = RemindUtils.getInterval(scheduleTime, rsvTime);
//
//        mGetGDDataTime = systemTime;
//        long nextTimeEstimate = systemTime + intervalTime;
//        LogUtils.d(TAG, "scheduleTime=" + scheduleTime +", timeNeedOnTrip=" + timeNeedOnTrip + ", timeNeedForSchedule="
//                + timeNeedForSchedule/1000 + ", intervalTime = " + intervalTime/1000 + ", nextTimeEstimate=" + nextTimeEstimate);
//        if(nextTimeEstimate + timeNeedForSchedule >= scheduleTime) {
//            //setAlarmManager(context, systemTime);  // 大于下一个阀值则立刻弹出提醒，不再进行估算
//            RemindUtils.generateAlerts(context, mSchedule);
//            LogUtils.d(TAG, "alertProcess******* pop alert ********** schedule time=" + RemindUtils.time2String(scheduleTime)
//                    + ", system time=" +systemTime +"(" + RemindUtils.time2String(systemTime) +")");
//        } else {
//            setAlarmManager(context, nextTimeEstimate);  // 下一次计算的时刻
//            LogUtils.d(TAG, "alertProcess******* need estimate nextTime" + ": nextTimeEstimate="+ nextTimeEstimate
//                     + "("+ RemindUtils.time2String(nextTimeEstimate) + ")" + ", system time=" + RemindUtils.time2String(systemTime));
//        }
//    }

//    private void scheduleAlarm() {
//        final String destAddress = RemindUtils.getDestAddress(mSchedule);       //        "西二旗(地铁站)";
//        if (!RemindUtils.isNomalRemind(mSchedule)) {
//            getTimeFromGDMap(destAddress);
//        } else if (mSchedule.getIsSmartRemind() != Constants.NOT_REMIND){
//            RemindUtils.scheduleNormalAlarm(mContext, mSchedule);
//        }
//    }

    private void getTimeFromGDMap(final String destAddress) {
        final int tripMode = RemindUtils.getTripMode(mSchedule, mContext);
        mLocationUtil = new LocationUtil(mContext);
        mLocationUtil.startLocation(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                LogUtils.e(TAG, "aMapLocation ----> " + aMapLocation.getErrorCode());
                if (aMapLocation.getErrorCode() == 0) {
                    mRouteUtils = new RouteUtils(mContext, IntelligentRemindUtils.this, aMapLocation);
                    mRouteUtils.getRoute(destAddress, aMapLocation.getCity(), tripMode);
                    LogUtils.e(TAG, "tripMode ----> " + tripMode);
                    LogUtils.e(TAG, "destAddress ----> " + destAddress);
                    LogUtils.e(TAG, "aMapLocation.getCity() ----> " + aMapLocation.getCity());
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.location_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void getRouteTime(long time) {
        Context context = mContext;
        LogUtils.e(TAG, "getRouteTime ----> " + time);
//        alertProcess(context, mSchedule, time);
        computeAlertProcess(context, mSchedule, time);
    }

//    private long getAlarmTime(){
//        return mAlarmTime;
//    }
//
//    private void setAlarmTime(long alarmTime) {
//        mAlarmTime = alarmTime;
//    }

}
