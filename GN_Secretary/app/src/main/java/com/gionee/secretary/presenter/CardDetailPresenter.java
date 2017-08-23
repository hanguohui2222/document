package com.gionee.secretary.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.ui.viewInterface.ICardDetailView;


/**
 * Created by luorw on 11/30/16.
 */
public class CardDetailPresenter {
    private ICardDetailView mCardDetailView;
    private Context mContext;
    private ScheduleInfoDao mScheduleInfoDao;
    private static final int DELETE_SUCCESS = 0;

    public CardDetailPresenter(Context context, ICardDetailView cardDetailView) {
        this.mContext = context;
        this.mCardDetailView = cardDetailView;
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
    }

    public BaseSchedule getScheduleById(int id) {
        return mScheduleInfoDao.getScheduleInfoById(id);
    }

    public void deleteScheduleRepeatAll(BaseSchedule schedule, boolean fromToday) {
        mScheduleInfoDao.deleteScheduleRepeatPeriodAll(schedule, fromToday);
        mContext.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
        Message message = mHandle.obtainMessage(DELETE_SUCCESS);
        message.obj = true;
        mHandle.sendMessage(message);
    }

    public void deleteScheduleById(BaseSchedule schedule) {
        RemindUtils.alarmCancel(mContext, schedule);
        ScheduleInfoDao.getInstance(mContext).deleteScheduleById(schedule);
        mContext.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
        Message message = mHandle.obtainMessage(DELETE_SUCCESS);
        message.obj = false;
        mHandle.sendMessage(message);
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELETE_SUCCESS:
                    mCardDetailView.deleteSuccess((boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
}
