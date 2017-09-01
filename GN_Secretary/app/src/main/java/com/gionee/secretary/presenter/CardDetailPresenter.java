package com.gionee.secretary.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.gionee.secretary.SecretaryApplication;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.ui.viewInterface.ICardDetailView;


/**
 * Created by luorw on 11/30/16.
 */
public class CardDetailPresenter extends BasePresenterImpl<ICardDetailView>{
    private Context mContext;
    private ScheduleInfoDao mScheduleInfoDao;

    public CardDetailPresenter(Context context, ICardDetailView cardDetailView) {
        this.mContext = context;
        attachView(cardDetailView);
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
    }

    public BaseSchedule getScheduleById(int id) {
        return mScheduleInfoDao.getScheduleInfoById(id);
    }

    public void deleteScheduleRepeatAll(BaseSchedule schedule, boolean fromToday) {
        mScheduleInfoDao.deleteScheduleRepeatPeriodAll(schedule, fromToday);
        mContext.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
        SecretaryApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mView.deleteSuccess(true);
            }
        });
    }

    public void deleteScheduleById(BaseSchedule schedule) {
        RemindUtils.alarmCancel(mContext, schedule);
        ScheduleInfoDao.getInstance(mContext).deleteScheduleById(schedule);
        mContext.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
        SecretaryApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mView.deleteSuccess(false);
            }
        });
    }

}
