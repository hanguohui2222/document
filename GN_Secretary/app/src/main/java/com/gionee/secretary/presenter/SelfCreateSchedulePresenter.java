package com.gionee.secretary.presenter;

import android.content.Context;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.ui.viewInterface.ISelfCreateScheduleView;

import java.util.Date;

/**
 * Created by luorw on 11/30/16.
 */
public class SelfCreateSchedulePresenter {
    private ISelfCreateScheduleView mSelfCreateScheduleView;
    private Context mContext;
    private ScheduleInfoDao mScheduleInfoDao;

    public SelfCreateSchedulePresenter(Context context, ISelfCreateScheduleView SelfCreateScheduleView) {
        this.mContext = context;
        this.mSelfCreateScheduleView = SelfCreateScheduleView;
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
    }

    public void editSchedule(int scheduleId) {
        SelfCreateSchedule schedule = (SelfCreateSchedule) mScheduleInfoDao.getInstance(mContext).getScheduleInfoById(scheduleId);
        mSelfCreateScheduleView.EditSchedule(schedule);
    }

    public void resetScheduleForPeriod(SelfCreateSchedule schedule, Date orign_date, boolean fromToday) {
        mScheduleInfoDao.setUpdateScheduleIdListener(new UpdateScheduleIdListener() {
            @Override
            public void updateScheduleId(int id) {
                mSelfCreateScheduleView.UpdateScheduleId(id);
            }
        });
        mScheduleInfoDao.resetScheduleForPeriod(schedule, orign_date, fromToday);
    }

    public void updateScheduleToDB(BaseSchedule schedule, boolean fromToday, boolean isForPeriod) {
        if (isForPeriod) {
            mScheduleInfoDao.updateScheduleToDBForPeriod(schedule, fromToday);
        } else {
            mScheduleInfoDao.updateScheduleToDB(schedule, null);
        }
    }

    public void saveSchedule(SelfCreateSchedule schedule) {
        mScheduleInfoDao.saveScheduleToDB(schedule);
        mSelfCreateScheduleView.saveSuccess();
    }

    public interface UpdateScheduleIdListener {
        void updateScheduleId(int id);
    }
}
