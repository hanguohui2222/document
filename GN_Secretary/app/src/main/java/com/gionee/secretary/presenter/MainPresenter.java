package com.gionee.secretary.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.ui.viewInterface.ICalendarView;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.dao.WeatherDao;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.ui.viewInterface.IMainView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by luorw on 11/30/16.
 */
public class MainPresenter {
    private IMainView mMainView;
    private ICalendarView mCalendarView;
    private Context mContext;
    private ScheduleInfoDao mScheduleInfoDao;
    private static final int LOAD_WEATHER = 1;
    private static final int DELETE_REPEAT_CARDS = 2;

    public MainPresenter(Context context, IMainView mainView) {
        this.mContext = context;
        this.mMainView = mainView;
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
    }

    public MainPresenter(Context context, ICalendarView calendarView) {
        this.mContext = context;
        mScheduleInfoDao = ScheduleInfoDao.getInstance(context);
        mCalendarView = calendarView;
    }

    public void loadWeather() {
        Thread weatherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                WeatherDao weatherDao = new WeatherDao(mContext);
                WeatherSchedule weather = weatherDao.getWeatherSchedule();
                Message msg = new Message();
                msg.what = LOAD_WEATHER;
                msg.obj = weather;
                mHandle.sendMessage(msg);
            }
        });
        weatherThread.start();
    }

    public void refreshCards(int page) {
        if (page == 0) {
            List<BaseSchedule> list = new ArrayList<>();
            List<BaseSchedule> schedulesList = new ArrayList<>();
            schedulesList = mScheduleInfoDao.queryScheduleFromToday2Main(page);
            for (int i = 0; i < 2; i++) {
                if (schedulesList.size() == 0) {
                    page++;
                    schedulesList = mScheduleInfoDao.queryScheduleFromToday2Main(page);
                } else {
                    break;
                }
            }
            list.addAll(schedulesList);
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            boolean isShowExpress = sharedPreferences.getBoolean(Constants.EXPRESS_SWITCH_PREFERENCE_KEY, true);
            if (isShowExpress) {
                getExpressList(list);
            }
            mMainView.refreshCards(list);
        } else {
            List<BaseSchedule> schedulesList = new ArrayList<>();
            schedulesList = mScheduleInfoDao.queryScheduleFromToday2Main(page);
            for (int i = 0; i < 2; i++) {
                if (schedulesList.size() == 0) {
                    page++;
                    schedulesList = mScheduleInfoDao.queryScheduleFromToday2Main(page);
                } else {
                    break;
                }
            }
            mMainView.loadNextPageCards(schedulesList);
        }
    }

    private void getExpressList(List<BaseSchedule> list) {
        List<ExpressSchedule> expressSchedules = mScheduleInfoDao.queryAllExpressSchedule();
        Date today = Calendar.getInstance().getTime();
        List<ExpressSchedule> schedule = new ArrayList<>();
        for (ExpressSchedule e : expressSchedules) {
            if (e.getState() == 3 && !DateUtils.date2String(e.getTrace_date()).equals(DateUtils.date2String(today))) {
                continue;
            } else {
                e.setDate(today);
                schedule.add(e);
            }
        }
        if (null != schedule && schedule.size() > 0) {
            list.add(0, schedule.get(0));
        }
        mMainView.updateExpressData(schedule);
    }

    public void deleteRepeatSchedule(final BaseSchedule baseSchedule, final boolean fromToday) {
        Thread deleteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RemindUtils.deletePeriodScheduleAlarm(mContext, baseSchedule, fromToday);
                mScheduleInfoDao.deleteScheduleRepeatPeriodAll(baseSchedule, fromToday);
                Message msg = new Message();
                msg.what = DELETE_REPEAT_CARDS;
                //msg.arg1 = 0;
                mHandle.sendMessage(msg);
            }
        });
        deleteThread.start();
    }

    /*
     * sunyang modify for GNSPR #65023 at 2017-01-14
     */
    public void deleteRepeatSchedule(final BaseSchedule baseSchedule, final List<BaseSchedule> eventList,
                                     final List<ExpressSchedule> expressSchedules, final boolean fromToday) {
        Thread deleteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (baseSchedule instanceof ExpressSchedule) {
                    expressSchedules.remove(baseSchedule);
                    eventList.remove(baseSchedule);
                    if (expressSchedules.size() > 0) {
                        if (eventList.size() == 0 || !(eventList.get(0) instanceof ExpressSchedule)) {
                            eventList.add(0, expressSchedules.get(0));
                        }
                    }
                } else {
                    eventList.remove(baseSchedule);
                }
                RemindUtils.deletePeriodScheduleAlarm(mContext, baseSchedule, fromToday);
                mScheduleInfoDao.deleteScheduleRepeatPeriodAll(baseSchedule, fromToday);
                Message msg = new Message();
                msg.what = DELETE_REPEAT_CARDS;
                //msg.arg1 = 0;
                mHandle.sendMessage(msg);
            }
        });
        deleteThread.start();
    }

    public void deleteSingleSchedule(BaseSchedule event, List<BaseSchedule> eventList, List<ExpressSchedule> expressSchedules) {
        if (event instanceof ExpressSchedule) {
            expressSchedules.remove(event);
            eventList.remove(event);
            if (expressSchedules.size() > 0) {
                if (eventList.size() == 0 || !(eventList.get(0) instanceof ExpressSchedule)) {
                    eventList.add(0, expressSchedules.get(0));
                }
            }
        } else {
            eventList.remove(event);
        }
        synchronized (event) {
            RemindUtils.alarmCancel(mContext, event);
            mScheduleInfoDao.deleteScheduleById(event);
        }
        Message msg = new Message();
        msg.what = DELETE_REPEAT_CARDS;
        //msg.arg1 = 0;
        mHandle.sendMessage(msg);
    }

    public boolean isRepeatEvent(BaseSchedule schedule) {
        boolean isRepeateEvent;
        if (schedule.getType() == Constants.SELF_CREATE_TYPE) {
            if (!"一次".equals(((SelfCreateSchedule) schedule).getRemindPeriod())) {
                isRepeateEvent = true;//自建日程 非一次性事件
            } else {
                isRepeateEvent = false;//自建日程 一次性事件
            }
        } else {
            isRepeateEvent = false;//短信解析日程
        }
        return isRepeateEvent;
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_WEATHER:
                    WeatherSchedule weather = (WeatherSchedule) msg.obj;
                    mMainView.loadWeather(weather);
                    break;
                case DELETE_REPEAT_CARDS:
                    mCalendarView.onScheduleDeleted();
                default:
                    break;
            }
        }
    };

}
