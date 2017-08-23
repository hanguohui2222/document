package com.gionee.secretary.service;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.SubBaseSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.JsonUtil;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.WidgetUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.List;

/**
 * Created by hangh on 5/26/16.
 */
public class VoiceService extends Service {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        LogUtils.e("hangh", "VoiceService onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.e("hangh", "VoiceService onStartCommand");
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.e("hangh", "VoiceService onBind");
        return mBinder;
    }

    IBinder mBinder = new IVoiceService.Stub() {
        @Override
        public void saveVoiceSelfSchedule(String title, String datetime) {
            try {
                int scheduleId = ScheduleInfoDao.getInstance(VoiceService.this).saveVoiceSelfScheduleToDB(title, datetime);
                BaseSchedule schedule = ScheduleInfoDao.getInstance(VoiceService.this).getScheduleInfoById(scheduleId);
                LogUtils.d("lml", "saveVoiceSelfSchedule.... createScheduleRemind...schedule =" + schedule);
                RemindUtils.createScheduleRemind(VoiceService.this, schedule);
                WidgetUtils.updateWidget(VoiceService.this);
                sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("hangh", e.toString());
            }
        }

        @Override
        public String getAllScheduleCurrentDay(String datetime) {
            try {
                List<SubBaseSchedule> subBaseSchedules = ScheduleInfoDao.getInstance(VoiceService.this).queryAllScheduleCurrent(datetime);
                String jsonStr = JsonUtil.toJson(subBaseSchedules);
                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("hangh", e.toString());
            }
            return "";
        }

    };
}
