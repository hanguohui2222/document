package com.gionee.secretary.presenter;

import android.content.Context;

import com.gionee.secretary.SecretaryApplication;
import com.gionee.secretary.bean.AddressBean;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.ui.viewInterface.ISelfCreateScheduleView;
import com.gionee.secretary.utils.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by luorw on 11/30/16.
 */
public class SelfCreateSchedulePresenter extends BasePresenterImpl<ISelfCreateScheduleView> {
    private Context mContext;
    private ScheduleInfoDao mScheduleInfoDao;
    private double[] mEnds = new double[2];
    private String desc;

    public SelfCreateSchedulePresenter(Context context, ISelfCreateScheduleView selfCreateScheduleView) {
        this.mContext = context;
        attachView(selfCreateScheduleView);
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
    }

    public void editSchedule(int scheduleId) {
        SelfCreateSchedule schedule = (SelfCreateSchedule) mScheduleInfoDao.getInstance(mContext).getScheduleInfoById(scheduleId);
        mView.EditSchedule(schedule);
    }

    public void resetScheduleForPeriod(SelfCreateSchedule schedule, Date orign_date, boolean fromToday) {
        mScheduleInfoDao.setUpdateScheduleIdListener(new UpdateScheduleIdListener() {
            @Override
            public void updateScheduleId(int id) {
                mView.UpdateScheduleId(id);
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
        mView.saveSuccess();
    }

    /**
     * 获取历史查询
     */
    public void getHistory(String mAddressName) {
        boolean isHave = false;
        List<AddressBean> list = (List<AddressBean>) SecretaryApplication.getACache().getAsObject(Constants.HISTORY_ADDRESS);
        if (null == list) {
            list = new ArrayList<>();
        }
        if (!mAddressName.equals("")) {
            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).getName().equals(mAddressName)) {

                    isHave = true;
                    break;
                } else {

                    isHave = false;
                }
            }
            if (!isHave) {
                AddressBean addressBean = new AddressBean();
                addressBean.setName(mAddressName);
                addressBean.setmLatitude(mEnds[0]);
                addressBean.setmLongitude(mEnds[1]);
                addressBean.setDesc(desc);
                list.add(addressBean);
            }
            SecretaryApplication.getACache().put(Constants.HISTORY_ADDRESS, (Serializable) list);
        }
    }

    public interface UpdateScheduleIdListener {
        void updateScheduleId(int id);
    }
}
