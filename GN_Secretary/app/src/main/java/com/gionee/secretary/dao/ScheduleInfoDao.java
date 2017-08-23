package com.gionee.secretary.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.SubBaseSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.db.SecretaryDBMetaData;
import com.gionee.secretary.db.SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData;
import com.gionee.secretary.db.SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData;
import com.gionee.secretary.db.SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData;
import com.gionee.secretary.db.SecretaryDBOpenHelper;
import com.gionee.secretary.db.SecretarySQLite;
import com.gionee.secretary.presenter.SelfCreateSchedulePresenter;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by luorw on 5/11/16.
 */
public class ScheduleInfoDao {
    private SecretaryDBOpenHelper mDBHelper;
    private static ScheduleInfoDao scheduleInfoDao = null;
    private Context mContext;
    private int mPeriodId;
    private SelfCreateSchedulePresenter.UpdateScheduleIdListener mUpdateScheduleIdListener;

    public static ScheduleInfoDao getInstance(Context context) {
        if (scheduleInfoDao == null) {
            synchronized (ScheduleInfoDao.class) {
                if (scheduleInfoDao == null) {
                    scheduleInfoDao = new ScheduleInfoDao(context.getApplicationContext());
                }
            }
        }
        return scheduleInfoDao;
    }

    private ScheduleInfoDao(Context context) {
        mContext = context;
        mDBHelper = SecretarySQLite.getDBHelper(context);
    }

    public synchronized List<BaseSchedule> getSchedulesForWidget() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<BaseSchedule> allInfos = new ArrayList<>();
        //先查询排除快递外的所有日程
        String day = DateUtils.formatDate2StringByDay(new Date());
        String sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where "
                + T_BASE_SCHEDULE_MeteData.IS_ALL_DAY + "=1 and strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") = '" + day + "'";
        allInfos = query(db, sql);
        db = mDBHelper.getWritableDatabase();
        Iterator<BaseSchedule> it = allInfos.iterator();
        while (it.hasNext()) {
            BaseSchedule s = it.next();
            if (s.getType() == 106) {
                it.remove();
            }
        }
        //查询所以快递取其中有用的第一个插到集合首位
        boolean isShowStatus = SettingModel.getInstance(mContext).isShowStatusOfExpress();
        if (isShowStatus) {
            List<ExpressSchedule> allExpressSchedules = this.queryAllExpressSchedule();
            for (ExpressSchedule e : allExpressSchedules) {
                if ((e.getState() == 3 && !DateUtils.date2String(e.getTrace_date()).equals(DateUtils.date2String(Calendar.getInstance().getTime())))
                        || TextUtils.isEmpty(e.getTitle())) {
                    Log.d("liyu", "continue DateUtils.date2String(e.getTrace_date()) = " + DateUtils.date2String(e.getTrace_date())
                            + "  DateUtils.date2String(Calendar.getInstance().getTime() = " + DateUtils.date2String(Calendar.getInstance().getTime()));
                    continue;
                } else {
                    String sqlStr = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + e.id;
                    allInfos.add(0, query(db, sqlStr).get(0));
                    Log.d("liyu", "allInfos.size() = " + allInfos.size());
                    break;
                }
            }
        }
        //如果快递和全天日程不足两个在查询当天其他日程
        if (allInfos.size() < 2) {
            String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.DATE + " asc";
            sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " +
                    T_BASE_SCHEDULE_MeteData.IS_ALL_DAY + "=0 and " + T_BASE_SCHEDULE_MeteData.DATE
                    + ">datetime('now','localtime') and strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") = '" + day + "'" + orderBy;
            allInfos.addAll(query(db, sql));
        }
        return allInfos;
    }

    /**
     * 保存语音自建日程
     *
     * @param title
     * @param datetime
     */
    public synchronized int saveVoiceSelfScheduleToDB(String title, String datetime) {

        int id = 0;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        LogUtils.e("hangh", "saveVoiceSelfScheduleToDB sql db.isOpen = " + db.isOpen());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(datetime);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Timestamp timestamp = new Timestamp(date.getTime());
        int isAllDay = 0;
        String baseSql = "insert into " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + "("
                + T_BASE_SCHEDULE_MeteData.TYPE + ","
                + T_BASE_SCHEDULE_MeteData.TITLE + ","
                + T_BASE_SCHEDULE_MeteData.DATE + ","
                + T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE + ","
                + T_BASE_SCHEDULE_MeteData.REMIND_DATE + ","
                + T_BASE_SCHEDULE_MeteData.IS_ALL_DAY + ")"
                + " values("
                + Constants.SELF_CREATE_TYPE + ", '"
                + title + "', '"
                + timestamp + "', '"
                + timestamp + "', "
                + date.getTime() + ", "
                + isAllDay + "); ";
        if (db.isOpen()) {
            LogUtils.e("hangh", "saveVoiceSelfScheduleToDB sql = " + baseSql);
            db.execSQL(baseSql);
            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            //db.close();
        }
        Timestamp endtime = new Timestamp(date.getTime() + 1 * 60 * 60 * 1000);
        SQLiteDatabase db2 = mDBHelper.getWritableDatabase();
        String sql = "insert into " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + "("
                + T_SELF_CREATE_SCHEDULE_MeteData._ID + ","
                + T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS + ","
                + T_SELF_CREATE_SCHEDULE_MeteData.END_TIME + ","
                + T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION + ")"
                + " values("
                + id + ", '', '"
                + endtime + "','"
                + title + "'); ";
        if (db2.isOpen()) {
            db2.execSQL(sql);
            //db2.close();
        }
        return id;
    }

    /**
     * 保存日程信息（包括周期重复）
     *
     * @param schedule
     */
    public synchronized boolean saveScheduleToDB(BaseSchedule schedule) {
        SQLiteDatabase db = null;
        mPeriodId = -1;
        try {
            db = mDBHelper.getWritableDatabase();
            List<Date> dateList = CalendarManager.getRemindPeriodDates(mContext, schedule.getDate(), schedule.getRemindPeriod());
            List<Date> remindDateList = CalendarManager.getRemindPeriodRemindDates(mContext, new Date(schedule.getRemindDate()), schedule.getRemindPeriod());
            db.beginTransaction();
            int size = dateList.size();
            for (int i = 0; i < size; i++) {
                schedule.setDate(dateList.get(i));
                schedule.setRemindDate(remindDateList.get(i).getTime());
                saveScheduleToDBOnce(schedule, db, i == 0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != db) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 保存周期为一次日程信息
     *
     * @param schedule
     */
    private synchronized void saveScheduleToDBOnce(BaseSchedule schedule, SQLiteDatabase db, boolean isFirstSchedule) {
        Timestamp timestamp = new Timestamp(schedule.getDate().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_BASE_SCHEDULE_MeteData.TYPE, schedule.getType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.TITLE, schedule.getTitle());
        //modified by luorw for widget 20160706 begin
        if (schedule.getType() == Constants.EXPRESS_TYPE) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(schedule.getDate());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Timestamp timestamp1 = new Timestamp(calendar.getTime().getTime());
            contentValues.put(T_BASE_SCHEDULE_MeteData.DATE, timestamp1.toString());
            contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, timestamp1.toString());
        } else {
            contentValues.put(T_BASE_SCHEDULE_MeteData.DATE, timestamp.toString());
            if (schedule.getType() == Constants.HOTEL_TYPE) {
                String broadCastSortDate = getHotelBroadcastSortDate(schedule);
                contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, broadCastSortDate);
            } else {
                contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, timestamp.toString());
            }
        }
        //modified by luorw for widget 20160706 end
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_ALL_DAY, (schedule.getIsAllDay() ? 1 : 0));
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_TYPE, schedule.getRemindType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_PERIOD, schedule.getRemindPeriod());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_DATE, schedule.getRemindDate());
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND, schedule.getIsSmartRemind());
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE, schedule.getIsRemindActive());
        contentValues.put(T_BASE_SCHEDULE_MeteData.SENDER, schedule.getSmsSender());
        contentValues.put(T_BASE_SCHEDULE_MeteData.Content, schedule.getSmsContent());
        contentValues.put(T_BASE_SCHEDULE_MeteData.Source, schedule.getSource());
        contentValues.put(T_BASE_SCHEDULE_MeteData.PERIOD_ID, schedule.getPeriodID());

        if (db.isOpen()) {
            // Gionee sunyang modify for GNSPR #60214  at 2017-01-19 begin
            long id = db.insert(T_BASE_SCHEDULE_MeteData.TABLE_NAME, null, contentValues);
            schedule.setId((int) id);
            // Gionee sunyang modify for GNSPR #60214  at 2017-01-19 end
        }
        //获取重复日程（每日，每周，每月，每年）创建的第一条记录的id，或者周期为一次自建日程，或者短信解析的日程
        if (isFirstSchedule) {
            mPeriodId = schedule.getId();
        }
        //设置周期为一次自建日程和短信解析日程，设置周期id与自身id一致
        //设置重复日程（每日，每周，每月，每年）的周期id，与上面第一条记录的id，周期id保持一致
        schedule.setPeriodID(mPeriodId);
        //update该日程的周期ID
        if (mUpdateScheduleIdListener != null) {
            mUpdateScheduleIdListener.updateScheduleId(mPeriodId);
        }
        //更新db中基类表该记录的周期id
        updateSchedulePeriodID(schedule, db);
        //保存相应子表数据
        saveSubScheduleToDB(schedule, db);
    }

    public void setUpdateScheduleIdListener(SelfCreateSchedulePresenter.UpdateScheduleIdListener listener) {
        mUpdateScheduleIdListener = listener;
    }

    /**
     * 保存子日程信息
     *
     * @param schedule
     */
    private synchronized void saveSubScheduleToDB(BaseSchedule schedule, SQLiteDatabase db) {
        switch (schedule.getType()) {
            case Constants.SELF_CREATE_TYPE:
                saveSelfCreateSchedule((SelfCreateSchedule) schedule, db);
                break;
            case Constants.TRAIN_TYPE:
                saveTrainSchedule((TrainSchedule) schedule, db);
                break;
            case Constants.FLIGHT_TYPE:
                saveFlightSchedule((FlightSchedule) schedule, db);
                break;
            case Constants.BANK_TYPE:
                saveBankSchedule((BankSchedule) schedule, db);
                break;
            case Constants.EXPRESS_TYPE:
                saveExpressSchedule((ExpressSchedule) schedule, db);
                break;
            case Constants.HOTEL_TYPE:
                saveHotelSchedule((HotelSchedule) schedule, db);
                break;
            case Constants.MOVIE_TYPE:
                saveMovieSchedule((MovieSchedule) schedule, db);
                break;
            default:
                break;
        }
    }

    /**
     * 保存自定义创建的日程信息
     *
     * @param schedule
     */
    public synchronized void saveSelfCreateSchedule(SelfCreateSchedule schedule, SQLiteDatabase db) {
        long end = schedule.getDate().getTime() + schedule.getPeriod();
        schedule.setEndtime(new Date(end));
        Timestamp endtimestamp = new Timestamp(schedule.getEndtime().getTime());
        Timestamp timestamp = new Timestamp(schedule.getDate().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData._ID, schedule.getId());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS, schedule.getAddress());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION, schedule.getDescription());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE, schedule.getTripMode());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.END_TIME, endtimestamp.toString());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE, timestamp.toString());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID, schedule.getPeriodID());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.PERIOD, schedule.getPeriod());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK, schedule.getAddressRemark());
        if (db.isOpen()) {
            db.insert(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME, null, contentValues);
        }
    }

    /**
     * 保存短信解析的火车票日程信息
     *
     * @param schedule
     */
    public synchronized void saveTrainSchedule(TrainSchedule schedule, SQLiteDatabase db) {
        String sql = "insert into " + T_TRAIN_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + schedule.getStarttime() + "', '"
                + schedule.getArrivaltime() + "', '"
                + schedule.getDeparture() + "', '"
                + schedule.getDestination() + "', '"
                + schedule.getTrainnumber() + "', '"
                + schedule.getSeatnumber() + "', '"
                + schedule.getOrdernumber() + "', '"
                + schedule.getOrderperson() + "'); ";

        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }


    /**
     * 保存短信解析的电影日程信息
     *
     * @param schedule
     */
    public synchronized void saveMovieSchedule(MovieSchedule schedule, SQLiteDatabase db) {
        String sql = "insert into " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + schedule.getCinemaName() + "', '"
                + schedule.getMovieName() + "', '"
                + schedule.getSeatDesc() + "', '"
                + schedule.getTicketCertificate() + "', '"
                + schedule.getPlayTime() + "'); ";
        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }


    /**
     * 保存短信解析的酒店日程信息
     *
     * @param schedule
     */
    public synchronized void saveHotelSchedule(HotelSchedule schedule, SQLiteDatabase db) {
        String sql = "insert into " + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + schedule.getCheckInPeople() + "', '"
                + schedule.getCheckOutDate() + "', '"
                + schedule.getHotelAddress() + "', '"
                + schedule.getHotelName() + "', '"
                + schedule.getRoomCounts() + "', '"
                + schedule.getServiceNum() + "', '"
                + schedule.getRoomStyle() + "'); ";

        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 保存短信解析的快递日程信息
     *
     * @param schedule
     */
    public synchronized void saveExpressSchedule(ExpressSchedule schedule, SQLiteDatabase db) {
        Timestamp timestamp = new Timestamp(schedule.getDate().getTime());
        String sql = "insert into " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + timestamp.toString() + "', '"
                + schedule.getExpressCompany() + "', '"
                + schedule.getExpressNum() + "', '"
                + schedule.getExpressState() + "', '"
                + schedule.getExpressProgress() + "', '"
                + schedule.getExpressCode() + "', "
                + schedule.getState() + " );";
        if (db.isOpen()) {
            db.execSQL(sql);
        }
        LogUtils.d("liyu", "saveExpressSchedule id = " + schedule.getId());
    }

    /**
     * 保存短信解析的银行日程信息
     *
     * @param schedule
     */
    public synchronized void saveBankSchedule(BankSchedule schedule, SQLiteDatabase db) {
        String sql = "insert into " + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + schedule.getBankName() + "', '"
                + schedule.getCardNum() + "', '"
                + schedule.getRepaymentAmount() + "', '"
                + schedule.getRepaymentMonth() + "', '"
                + schedule.getBillMonth() + "', '"
                + schedule.getAlertDesc() + "'); ";
        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 保存短信解析的机票日程信息
     *
     * @param schedule
     */
    public synchronized void saveFlightSchedule(FlightSchedule schedule, SQLiteDatabase db) {
        String sql = "insert into " + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME + " values("
                + schedule.getId() + ", '"
                + schedule.getAirlineSource() + "', '"
                + schedule.getArrivalTime() + "', '"
                + schedule.getFlightNum() + "', '"
                + schedule.getDestination() + "', '"
                + schedule.getPassenger() + "', '"
                + schedule.getServiceNum() + "', '"
                + schedule.getStartAddress() + "', '"
                + schedule.getTicketNum() + "', '"
                + schedule.getAlertDesc() + "'); ";
        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 更新日程周期id
     *
     * @param schedule
     */
    public synchronized void updateSchedulePeriodID(BaseSchedule schedule, SQLiteDatabase db) {
        if (db == null) {
            db = SecretarySQLite.getDBHelper(mContext).getWritableDatabase();
        }
        Timestamp timestamp = new Timestamp(schedule.getDate().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_BASE_SCHEDULE_MeteData.TYPE, schedule.getType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.TITLE, schedule.getTitle());
        contentValues.put(T_BASE_SCHEDULE_MeteData.DATE, timestamp.toString());
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_ALL_DAY, (schedule.getIsAllDay() ? 1 : 0));
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE, schedule.getIsRemindActive());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_TYPE, schedule.getRemindType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_PERIOD, schedule.getRemindPeriod());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_DATE, schedule.getRemindDate());
        contentValues.put(T_BASE_SCHEDULE_MeteData.PERIOD_ID, schedule.getPeriodID());
        if (schedule.getType() == Constants.HOTEL_TYPE) {
            String broadCastSortDate = getHotelBroadcastSortDate(schedule);
            contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, broadCastSortDate);
        } else {
            contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, timestamp.toString());
        }
        String where = T_BASE_SCHEDULE_MeteData._ID + " = " + schedule.getId();
        if (db.isOpen()) {
            db.update(T_BASE_SCHEDULE_MeteData.TABLE_NAME, contentValues, where, null);
        }
    }


    /**
     * 更新日程信息周期为一次
     *
     * @param schedule
     */
    public synchronized void updateScheduleToDB(BaseSchedule schedule, SQLiteDatabase db) {
        if (db == null) {
            db = SecretarySQLite.getDBHelper(mContext).getWritableDatabase();
        }
        Timestamp timestamp = new Timestamp(schedule.getDate().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_BASE_SCHEDULE_MeteData.TYPE, schedule.getType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.TITLE, schedule.getTitle());
        contentValues.put(T_BASE_SCHEDULE_MeteData.DATE, timestamp.toString());
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_ALL_DAY, (schedule.getIsAllDay() ? 1 : 0));
        contentValues.put(T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE, schedule.getIsRemindActive());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_TYPE, schedule.getRemindType());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_PERIOD, schedule.getRemindPeriod());
        contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_DATE, schedule.getRemindDate());
        contentValues.put(T_BASE_SCHEDULE_MeteData.PERIOD_ID, schedule.getPeriodID());
        if (schedule.getType() == Constants.HOTEL_TYPE) {
            String broadCastSortDate = getHotelBroadcastSortDate(schedule);
            contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, broadCastSortDate);
        } else {
            contentValues.put(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, timestamp.toString());
        }
        String where = T_BASE_SCHEDULE_MeteData._ID + " = " + schedule.getId();

        if (db.isOpen()) {
            db.update(T_BASE_SCHEDULE_MeteData.TABLE_NAME, contentValues, where, null);
        }
        updateSubScheduleInfo(schedule, db);
    }

    private String getHotelBroadcastSortDate(BaseSchedule schedule) {
        Calendar calendar = Calendar.getInstance();
        Date date = schedule.getDate();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return timestamp.toString();
    }

    /**
     * 涉及到更新日程信息中的时间或者周期(周期重复)，则重置日程
     *
     * @param schedule
     * @param fromToday
     */
    public synchronized void resetScheduleForPeriod(SelfCreateSchedule schedule, Date oldScheduleDate, boolean fromToday) {
        //先删除该周期重复的相应日程。再创建新的周期重复日程
        deleteScheduleRepeatPeriodAll(schedule, oldScheduleDate, fromToday);
        schedule.setIsActive(Constants.REMIND_NOT_ACTIVE);
        saveScheduleToDB(schedule);
    }

    /**
     * 不涉及到更新日程信息中的时间或者周期，则更新日程信息(周期重复)
     *
     * @param schedule
     */
    public synchronized boolean updateScheduleToDBForPeriod(BaseSchedule schedule, boolean fromToday) {
        String whereStr = "";
        SQLiteDatabase db = null;
        if (fromToday) {
            String date = DateUtils.formatDate2DateString(schedule.getDate());
            whereStr = " datetime(" + T_BASE_SCHEDULE_MeteData.DATE + ") >= datetime('" + date + "') and "
                    + T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID() + ";";
            mPeriodId = schedule.getPeriodID();
        } else {
            whereStr = T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID() + ";";
            mPeriodId = schedule.getPeriodID();
        }
        schedule.setPeriodID(mPeriodId);
        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(T_BASE_SCHEDULE_MeteData.TYPE, schedule.getType());
            contentValues.put(T_BASE_SCHEDULE_MeteData.TITLE, schedule.getTitle());
            contentValues.put(T_BASE_SCHEDULE_MeteData.IS_ALL_DAY, (schedule.getIsAllDay() ? 1 : 0));
            contentValues.put(T_BASE_SCHEDULE_MeteData.REMIND_TYPE, schedule.getRemindType());
            contentValues.put(T_BASE_SCHEDULE_MeteData.PERIOD_ID, schedule.getPeriodID());
            contentValues.put(T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE, schedule.getIsRemindActive());
            db.update(T_BASE_SCHEDULE_MeteData.TABLE_NAME, contentValues, whereStr, null);
            updateSubScheduleToDBForPeriod((SelfCreateSchedule) schedule, fromToday);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != db) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 更新子日程信息(周期重复)
     *
     * @param schedule
     */
    private synchronized void updateSubScheduleToDBForPeriod(SelfCreateSchedule schedule, boolean fromToday) {
        String whereStr;
        if (fromToday) {
            String date = DateUtils.formatDate2DateString(schedule.getSubPeriodDate());
            whereStr = " where strftime('%Y-%m-%d'," + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE + ") >= '" + date + "' and " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getSubPeriodId() + ";";
        } else {
            whereStr = " where " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getSubPeriodId() + ";";
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Timestamp endtimestamp = null;
        if (schedule.getEndtime() != null) {
            endtimestamp = new Timestamp(schedule.getEndtime().getTime());
        }
        String sql = "update " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " set "
                + T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS + "='" + schedule.getAddress() + "', "
                + T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION + "='" + schedule.getDescription() + "', "
                + T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE + "='" + schedule.getTripMode() + "', "
                + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getPeriodID()
                + whereStr;


        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 更新子日程信息
     *
     * @param schedule
     */
    private synchronized void updateSubScheduleInfo(BaseSchedule schedule, SQLiteDatabase db) {
        switch (schedule.getType()) {
            case Constants.SELF_CREATE_TYPE:
                updateSelfCreateSchedule((SelfCreateSchedule) schedule, db);
                break;
            default:
                break;
        }
    }

    /**
     * 更新自定义创建的日程信息
     *
     * @param schedule
     */
    private synchronized void updateSelfCreateSchedule(SelfCreateSchedule schedule, SQLiteDatabase db) {
        Timestamp endtimestamp = null;
        if (schedule.getEndtime() != null) {
            endtimestamp = new Timestamp(schedule.getEndtime().getTime());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS, schedule.getAddress());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK, schedule.getAddressRemark());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION, schedule.getDescription());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE, schedule.getTripMode());
        contentValues.put(T_SELF_CREATE_SCHEDULE_MeteData.END_TIME, endtimestamp.toString());
        String where = T_SELF_CREATE_SCHEDULE_MeteData._ID + "=" + schedule.getId();
        if (db.isOpen()) {
            db.update(T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME, contentValues, where, null);
        }
    }

    public synchronized void updateExpressSchedule(String time, String info, int state, int id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long timeLong = DateUtils.formatDate(time).getTime();
        Timestamp timestamp = new Timestamp(timeLong);
        //TRACE_DATE为long
        String sql = "update " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + " set "
                + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE + "='" + timestamp.toString() + "', "
                + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS + "='" + info + "', "
                + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE + "='" + state + "'"
                + " where " + T_SELF_CREATE_SCHEDULE_MeteData._ID + "=" + id + ";";
        if (db.isOpen()) {
            db.execSQL(sql);
        }
        LogUtils.d("liyu", "updateExpressSchedule id = " + id);
    }

    public synchronized void updateExpressInfo(String code, String name, int id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        //TRACE_DATE为long
        String sql = "update " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + " set "
                + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE + "='" + code + "',"
                + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY + "='" + name + "'"
                + " where " + T_SELF_CREATE_SCHEDULE_MeteData._ID + "=" + id + ";";

        String sql2 = "update " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " set "
                + T_BASE_SCHEDULE_MeteData.TITLE + "='" + name + "'"
                + " where " + T_SELF_CREATE_SCHEDULE_MeteData._ID + "=" + id + ";";

        if (db.isOpen()) {
            db.execSQL(sql);
            db.execSQL(sql2);
        }
        LogUtils.d("liyu", "updateExpressInfo id = " + id);
    }

    /**
     * 通过id和type获取日程
     *
     * @param id
     * @return
     */
    public synchronized BaseSchedule getScheduleInfoById(int id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (!db.isOpen()) {
            return null;
        }
        Cursor cursor = db.rawQuery(sql, null);
        BaseSchedule schedule = null;
        while (cursor.moveToNext()) {
            BaseSchedule info = setBaseScheduleInfoValue(cursor);
            switch (info.getType()) {
                case Constants.SELF_CREATE_TYPE:
                    schedule = querySelfCreateSchedule(db, info);
                    break;
                case Constants.TRAIN_TYPE:
                    schedule = queryTrainSchedule(db, info);
                    break;
                case Constants.FLIGHT_TYPE:
                    schedule = queryFlightSchedule(db, info);
                    break;
                case Constants.HOTEL_TYPE:
                    schedule = queryHotelSchedule(db, info);
                    break;
                case Constants.BANK_TYPE:
                    schedule = queryBankSchedule(db, info);
                    break;
                case Constants.EXPRESS_TYPE:
                    schedule = queryExpressSchedule(db, info);
                    break;
                case Constants.MOVIE_TYPE:
                    schedule = queryMovieSchedule(db, info);
                    break;
                default:
                    break;
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return schedule;
    }

    /**
     * 根据id删除相应日程
     */
    public synchronized void deleteScheduleById(BaseSchedule schedule) {
        int id = schedule.getId();
        int type = schedule.getType();
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "delete from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_BASE_SCHEDULE_MeteData._ID + "=" + id;
        if (db.isOpen()) {
            db.execSQL(sql);
        }
        deleteSubScheduleById(id, type);
    }

    /**
     * 删除所有已签收的快递
     * @return
     */

    public synchronized boolean deleteAllReceiveExpressSchedule() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = null;
        try {
            cursor = db.query("t_express_schedule_info", null, "state = 3", null, null, null, null);
            while (cursor.moveToNext()) {
                int index = cursor.getInt(cursor.getColumnIndex("_id"));
                db.delete("t_express_schedule_info", "_id = ? ", new String[]{"" + index});
                db.delete("t_base_schedule_info", "_id = ? ", new String[]{"" + index});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            db.endTransaction();
        }
        return true;
    }

    private synchronized void deleteScheduleRepeatPeriodAll(BaseSchedule schedule, Date oldSchedule_date, boolean fromToday) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql;
        LogUtils.e("hangh", "oldSchedule = " + oldSchedule_date.toString());
        if (fromToday) {
            //删除表内所有日程时间大于当前日程时间并且与其周期ID相等的日程
            String date = DateUtils.formatDate2DateString(oldSchedule_date);
            sql = "delete from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") >= '" + date + "' and " + T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID();
        } else {
            //删除表内所有与其周期ID相等的日程
            sql = "delete from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID();
        }
        if (db.isOpen()) {
            LogUtils.e("hangh", "sql = " + sql);
            db.execSQL(sql);
        }
        deleteSubScheduleByPeriodId(schedule, oldSchedule_date, fromToday);
    }

    /**
     * 删除周期重复的今天以后的或者所有日程
     *
     * @param schedule
     */
    public synchronized void deleteScheduleRepeatPeriodAll(BaseSchedule schedule, boolean fromToday) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql;
        if (fromToday) {
            //删除表内所有日程时间大于当前日程时间并且与其周期ID相等的日程
            String date = DateUtils.formatDate2DateString(schedule.getDate());
            sql = "delete from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") >= '" + date + "' and " + T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID();
        } else {
            //删除表内所有与其周期ID相等的日程
            sql = "delete from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_BASE_SCHEDULE_MeteData.PERIOD_ID + "=" + schedule.getPeriodID();
        }
        if (db.isOpen()) {
            db.execSQL(sql);
            //db.close();
        }
        deleteSubScheduleByPeriodId(schedule, fromToday);
    }

    /**
     * 删除周期重复的今天以后的或者所有子日程
     *
     * @param schedule
     * @param fromToday
     */
    private synchronized void deleteSubScheduleByPeriodId(BaseSchedule schedule, Date oldDate, boolean fromToday) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql;
        if (fromToday) {
            //删除表内所有日程时间大于当前日程时间并且与其周期ID相等的日程
            String date = DateUtils.formatDate2DateString(oldDate);
            sql = "delete from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE + ") >= '" + date + "' and " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getPeriodID();
        } else {
            //删除表内所有与其周期ID相等的日程
            sql = "delete from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getPeriodID();
        }
        if (db.isOpen()) {
            LogUtils.e("hangh", "sql2 = " + sql);
            db.execSQL(sql);
        }
    }

    private synchronized void deleteSubScheduleByPeriodId(BaseSchedule schedule, boolean fromToday) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql;
        if (fromToday) {
            //删除表内所有日程时间大于当前日程时间并且与其周期ID相等的日程
            String date = DateUtils.formatDate2DateString(schedule.getDate());
            sql = "delete from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE + ") >= '" + date + "' and " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getPeriodID();
        } else {
            //删除表内所有与其周期ID相等的日程
            sql = "delete from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + "=" + schedule.getPeriodID();
        }
        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 根据id删除相应子日程
     *
     * @param id
     * @param type
     */
    private synchronized void deleteSubScheduleById(int id, int type) {
        String tableName = null;
        switch (type) {
            case Constants.SELF_CREATE_TYPE:
                tableName = T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.TRAIN_TYPE:
                tableName = T_TRAIN_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.FLIGHT_TYPE:
                tableName = SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.BANK_TYPE:
                tableName = SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.EXPRESS_TYPE:
                tableName = SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.HOTEL_TYPE:
                tableName = SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case Constants.MOVIE_TYPE:
                tableName = SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME;
                break;
            default:
                break;
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "delete from " + tableName + " where " + T_BASE_SCHEDULE_MeteData._ID + "=" + id;
        if (db.isOpen()) {
            db.execSQL(sql);
        }
    }

    /**
     * 查询符合某月日程信息
     *
     * @param date 日期   yyyy-mm
     * @return
     */
    public synchronized List<Integer> queryScheduleByMonth(String date) {
        List<BaseSchedule> list;
        List<Integer> dateList = new ArrayList<Integer>();
        SQLiteDatabase db = SecretarySQLite.getDBHelper(mContext).getWritableDatabase();
        String sql = null;
        sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m'," + T_BASE_SCHEDULE_MeteData.DATE + ") = '" + date + "'";
        list = query(db, sql);
        boolean isShowStatus = SettingModel.getInstance(mContext).isShowStatusOfExpress();
        //boolean hasExpress = false;
        for (BaseSchedule info : list) {
            if (info == null) {
                LogUtils.d("liyu", "info == null");
                continue;
            } else if (info.getType() == 106) {
                if (!isShowStatus) {
                    continue;
                } else {
                    ExpressSchedule e = queryExpressSchedule(db, info);
                    if (e.getState() == 3 && !DateUtils.date2String(e.getTrace_date()).equals(DateUtils.date2String(Calendar.getInstance().getTime()))) {
                        LogUtils.d("liyu", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                        //continue;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(e.getTrace_date());
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);

                        Date date2 = DateUtils.formatString2DateByMonth(date);
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.setTime(date2);
                        int month2 = calendar2.get(Calendar.MONTH);
                        int year2 = calendar2.get(Calendar.YEAR);


                        if(!dateList.contains(day) && (month == month2) && (year == year2)){
                            dateList.add(day);
                        }

                    } else {
                        LogUtils.d("liyu", ">>>>>>>>>>>>>>>>>>" + DateUtils.date2String(e.getTrace_date()));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(calendar.getTime());
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        if (!dateList.contains(day)) {
                            dateList.add(day);
                        }
                    }
                    continue;
                }
            }
            Date date1 = info.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (!dateList.contains(day)) {
                dateList.add(day);
            }
        }

        String expressSql = "select * from " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME
                + " where strftime('%Y-%m'," + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE + ") = '" + date + "'";
        List<ExpressSchedule> expressSchedules = queryexpress(db,expressSql);
        for (ExpressSchedule e : expressSchedules){
            if (!isShowStatus) {
                continue;
            } else {
                if (e.getState() == 3 && !DateUtils.date2String(e.getTrace_date()).equals(DateUtils.date2String(Calendar.getInstance().getTime()))) {
                    LogUtils.d("liyu", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    //continue;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(e.getTrace_date());
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);

                    Date date2 = DateUtils.formatString2DateByMonth(date);
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime(date2);
                    int month2 = calendar2.get(Calendar.MONTH);

                    if(!dateList.contains(day) && (month == month2)){
                        dateList.add(day);
                    }

                } else {
                    LogUtils.d("liyu", ">>>>>>>>>>>>>>>>>>" + DateUtils.date2String(e.getTrace_date()));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(calendar.getTime());
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    if (!dateList.contains(day)) {
                        dateList.add(day);
                    }
                }
                continue;
            }
        }
        return dateList;
    }


    private synchronized List<ExpressSchedule> queryexpress(SQLiteDatabase db, String sql) {
        List<ExpressSchedule> infos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            infos.clear();
            while (cursor.moveToNext()) {
                ExpressSchedule info = new ExpressSchedule();
                info.setExpressCompany(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY)));
                info.setExpressNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM)));
                info.setTrace_date(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE))));
                info.setExpressState(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE)));
                info.setExpressProgress(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS)));
                info.setExpressCode(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE)));
                info.setState(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE)));
                info.setId(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData._ID)));
                infos.add(info);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;
    }



    public synchronized List<BaseSchedule> queryScheduleByDate(String date) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = null;
        String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.DATE + " asc";
        sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") = '" + date + "'" + orderBy;
        LogUtils.d("lml", "******queryScheduleByDate, date=" + date + ", sql=" + sql);
        return queryExceptExpress(db, sql);
    }

    public synchronized List<BaseSchedule> queryScheduleByRemindDate(Long timeStart, Long timeEnd) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = null;
        String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.REMIND_DATE + " asc";
        sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_BASE_SCHEDULE_MeteData.REMIND_DATE + ">= '" + timeStart + "'" + " and " + T_BASE_SCHEDULE_MeteData.REMIND_DATE + "< '" + timeEnd + "'" + orderBy;
        LogUtils.d("lml", "******queryScheduleByRemindDate" + ", sql=" + sql);
        return query(db, sql);
    }

    /**
     * 查询今天及以后的所有日程,分页
     *
     * @return
     */
    public synchronized List<BaseSchedule> queryScheduleFromToday2Main(int page) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int limit = page * Constants.PAGE_CONTENT_COUNT;
        String limitStr = " limit " + limit + "," + Constants.PAGE_CONTENT_COUNT;
        String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.DATE + " asc";
        String sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where " + T_BASE_SCHEDULE_MeteData.DATE + ">=datetime('now','start of day')" + orderBy + limitStr;
        return queryBaseScheduleFromToday(db, sql, page);
    }

    public synchronized List<ExpressSchedule> queryAllExpressSchedule() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<ExpressSchedule> expressSchedules = new ArrayList<>();
        String orderBy = " order by " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE + " desc";
        String sql = "select * from " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + orderBy;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            Cursor baseCursor = null;
            expressSchedules.clear();
            while (cursor.moveToNext()) {
                ExpressSchedule info = new ExpressSchedule();
                info.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData._ID))));
                info.setTrace_date(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE))));
                info.setExpressCompany(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY)));
                info.setExpressNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM)));
                info.setExpressState(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE)));
                info.setExpressProgress(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS)));
                info.setExpressCode(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE)));
                info.setState(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE)));
                String baseSql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME
                        + " where " + T_BASE_SCHEDULE_MeteData._ID + " = " + info.getId();
                baseCursor = db.rawQuery(baseSql, null);
                if (baseCursor.moveToNext()) {
                    BaseSchedule base = setBaseScheduleInfoValue(baseCursor);
                    info.setDate(base.getDate());
                    info.setTitle(base.getTitle());
                    info.setType(base.getType());
                    info.setRemindPeriod(base.getRemindPeriod());
                    info.setAllDay(base.getIsAllDay());
                    info.setRemindType(base.getRemindType());
                    info.setRemindDate(base.getRemindDate());
                    info.setIsSmartRemind(base.getIsSmartRemind());
                }
                expressSchedules.add(info);
                if (baseCursor != null && !baseCursor.isClosed()) {
                    baseCursor.close();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return expressSchedules;
        }
        return expressSchedules;
    }
    
    
    /*Gionee zhengyt 2016-12-20 add for search not Begin*/
    /*
     * 查询备忘，搜索关键字，包括title和备忘内容搜索
     * */

//    public synchronized List<BaseNoteSchedule> queryNoteByKeyWord(String keyWord, int page) {
//    	SQLiteDatabase db = mDBHelper.getWritableDatabase();
//        int limit = page * Constants.PAGE_CONTENT_COUNT;
//        String table = "t_voice_note";
//        
//        String selection  = "title like ? or content like ?";
//        String[] selectionArgs = new String[]{"%"+keyWord+"%","%"+keyWord+"%",};
//        
//        List<BaseNoteSchedule> infos = new ArrayList<>();
//        
//        Cursor cursor =  db.query(table, null, selection, selectionArgs, null, null, null, null);
//        LogUtils.i("liyy", "cousor....querynotebyword.....count:" + cursor.getCount());
//        infos.clear();
//        
//        while (cursor.moveToNext()) {
//        	BaseNoteSchedule info = (BaseNoteSchedule)setBaseNoteScheduleInfoValue(cursor);
//        	infos.add(info);
//        }
//        
//        if (!cursor.isClosed()) {
//            cursor.close();
//        }
//        
//        return infos;
//    }

//    private BaseNoteSchedule setBaseNoteScheduleInfoValue(Cursor cursor) {
//    	
//    	int aaid = cursor.getInt(cursor.getColumnIndex("_id"));
//    	String bbTitle = cursor.getString(cursor.getColumnIndex("title"));
//    	String ccContent = cursor.getString(cursor.getColumnIndex("content"));
//    	
//    	BaseNoteSchedule info = new BaseNoteSchedule(aaid,bbTitle,ccContent);
//    	
////    	info.setNoteTitle(cursor.getString(cursor.getColumnIndex("title")));
////    	info.setNoteTitle(cursor.getString(cursor.getColumnIndex("content")));
//    	
//    	
//    	
//    	info.setNoteId(cursor.getInt(cursor.getColumnIndex("_id")));
//    	info.setTitle(cursor.getString(cursor.getColumnIndex("title")));
//    	info.setNoteContext(cursor.getString(cursor.getColumnIndex("content")));
//    	
//    	return info;
//    }
//
//    /*Gionee zhengyt 2016-12-20 add for search not End*/

    /**
     * 查询title和description包含搜索关键字的所有日程
     * 重复日程只显示一个
     *
     * @param keyWord
     * @param page
     * @return
     */
    @SuppressWarnings("deprecation")
    public synchronized List<BaseSchedule> queryByKeyWord(String keyWord, int page, boolean isIncludeExpress) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int limit = page * Constants.PAGE_CONTENT_COUNT;
        
        /*Gionee zhengyt 2017-2-6 add for 65723 begin*/
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String temp = String.format("%tY-%tm-%td-%tH-%tM-%tS", c, c, c, c, c, c);
        String compareTime = temp.toString().substring(0, 10);
//        compareTime += " 00:00:00.0";

        
        /*Gionee zhengyt 2017-2-6 add for 65723 end*/
        
        



        /*modify by zhengjl at 2017-2-7 for search
          使用LEFT JOIN进行表连接，t_base_schedule_info为主表，保存其中的所有内容
        t*/
        //INNER JOIN
        String table = T_BASE_SCHEDULE_MeteData.TABLE_NAME + " LEFT JOIN " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " ON " +


                T_BASE_SCHEDULE_MeteData.TABLE_NAME + "." + T_BASE_SCHEDULE_MeteData._ID + "=" + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + "." + T_SELF_CREATE_SCHEDULE_MeteData._ID;
        String selection = "datetime('now','start of day') and (" + T_BASE_SCHEDULE_MeteData.TITLE + "  like ?" + "  OR " + T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION + "  like ? )" + " and datetime(t_base_schedule_info.date) > " + "'" + compareTime + "'";
        if (!isIncludeExpress) {
            selection += " and " + T_BASE_SCHEDULE_MeteData.TYPE + " != " + Constants.EXPRESS_TYPE;
        }
        selection += " group by period_id";
        String[] selectionArgs = new String[]{"%" + keyWord + "%", "%" + keyWord + "%"};
        String orderBy = " " + T_BASE_SCHEDULE_MeteData.DATE + " ASC";
        String limitQuery = limit + "," + Constants.PAGE_CONTENT_COUNT;
        List<BaseSchedule> infos = new ArrayList<>();

        String columns = "t_base_schedule_info._id as _id," +
                T_BASE_SCHEDULE_MeteData.TYPE + "," +
                T_BASE_SCHEDULE_MeteData.TITLE + "," +
                "min(date) as date," +
                T_BASE_SCHEDULE_MeteData.IS_ALL_DAY + "," +
                T_BASE_SCHEDULE_MeteData.REMIND_PERIOD + "," +
                T_BASE_SCHEDULE_MeteData.REMIND_TYPE + "," +
                T_BASE_SCHEDULE_MeteData.REMIND_DATE + "," +
                T_BASE_SCHEDULE_MeteData.PERIOD_ID + "," +
                T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND + "," +
                T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE + "," +
                T_BASE_SCHEDULE_MeteData.SENDER + "," +
                T_BASE_SCHEDULE_MeteData.Content + "," +
                T_BASE_SCHEDULE_MeteData.Source + "," +
                T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE;


        if (db.isOpen()) {
            //Cursor cursor = db.query(table, columns, selection, selectionArgs, null, null, orderBy, limitQuery);
            String sql = "select " + columns + " from  " + table +
                    " where " + selection + " order by " + orderBy +
                    " limit " + limitQuery;
            LogUtils.d("hangh", "sql = " + sql);
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            LogUtils.e("liyy", "size:" + cursor.getCount());
            infos.clear();
            while (cursor.moveToNext()) {
                BaseSchedule info = setBaseScheduleInfoValue(cursor);
                switch (info.getType()) {
                    case Constants.SELF_CREATE_TYPE:
                        SelfCreateSchedule selfCreateSchedule = querySelfCreateSchedule(db, info);
                        infos.add(selfCreateSchedule);
                        break;
                    case Constants.TRAIN_TYPE:
                        TrainSchedule trainSchedule = queryTrainSchedule(db, info);
                        infos.add(trainSchedule);
                        break;
                    case Constants.FLIGHT_TYPE:
                        FlightSchedule flightSchedule = queryFlightSchedule(db, info);
                        infos.add(flightSchedule);
                        break;
                    case Constants.BANK_TYPE:
                        BankSchedule bankSchedule = queryBankSchedule(db, info);
                        infos.add(bankSchedule);
                        break;
                    case Constants.EXPRESS_TYPE:
                        ExpressSchedule expressSchedule = queryExpressSchedule(db, info);
                        infos.add(expressSchedule);
                        break;
                    case Constants.HOTEL_TYPE:
                        HotelSchedule hotelSchedule = queryHotelSchedule(db, info);
                        infos.add(hotelSchedule);
                        break;
                    case Constants.MOVIE_TYPE:
                        MovieSchedule movieSchedule = queryMovieSchedule(db, info);
                        infos.add(movieSchedule);
                        break;
                    default:
                        break;
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;

    }

    /**
     * 查询title包含搜索关键字的所有日程
     *
     * @param keyWord
     * @param page
     * @return
     */
    public synchronized List<BaseSchedule> queryScheduleByKeyWord(String keyWord, int page, boolean isIncludeExpress) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int limit = page * Constants.PAGE_CONTENT_COUNT;
        String table = T_BASE_SCHEDULE_MeteData.TABLE_NAME;
        String selection = "datetime('now','start of day') and " + T_BASE_SCHEDULE_MeteData.TITLE + "  like ?";
        if (!isIncludeExpress) {
            selection += " and " + T_BASE_SCHEDULE_MeteData.TYPE + " != " + Constants.EXPRESS_TYPE;
        }
        String[] selectionArgs = new String[]{"%" + keyWord + "%"};
        String orderBy = T_BASE_SCHEDULE_MeteData.DATE + " ASC";
        String limitQuery = limit + "," + Constants.PAGE_CONTENT_COUNT;
        List<BaseSchedule> infos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, orderBy, limitQuery);
            LogUtils.i("liyy", "cousor....querybyword.....count:" + cursor.getCount());
            infos.clear();
            while (cursor.moveToNext()) {
                BaseSchedule info = setBaseScheduleInfoValue(cursor);
                switch (info.getType()) {
                    case Constants.SELF_CREATE_TYPE:
                        SelfCreateSchedule selfCreateSchedule = querySelfCreateSchedule(db, info);
                        infos.add(selfCreateSchedule);
                        break;
                    case Constants.TRAIN_TYPE:
                        TrainSchedule trainSchedule = queryTrainSchedule(db, info);
                        infos.add(trainSchedule);
                        break;
                    case Constants.FLIGHT_TYPE:
                        FlightSchedule flightSchedule = queryFlightSchedule(db, info);
                        infos.add(flightSchedule);
                        break;
                    case Constants.BANK_TYPE:
                        BankSchedule bankSchedule = queryBankSchedule(db, info);
                        infos.add(bankSchedule);
                        break;
                    case Constants.EXPRESS_TYPE:
                        ExpressSchedule expressSchedule = queryExpressSchedule(db, info);
                        infos.add(expressSchedule);
                        break;
                    case Constants.HOTEL_TYPE:
                        HotelSchedule hotelSchedule = queryHotelSchedule(db, info);
                        infos.add(hotelSchedule);
                        break;
                    case Constants.MOVIE_TYPE:
                        MovieSchedule movieSchedule = queryMovieSchedule(db, info);
                        infos.add(movieSchedule);
                        break;
                    default:
                        break;
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;

    }

    /**
     * 查询当天所有日程
     *
     * @param datetime
     * @return
     */

    public synchronized List<SubBaseSchedule> queryAllScheduleCurrent(String datetime) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String startdate = datetime;
        String startdateAddOne = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date mStartdate = null;
        try {
            mStartdate = sdf.parse(startdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartdate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        startdateAddOne = sdf.format(calendar.getTime());

        String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.DATE + " asc";
        String sql = "select " + T_BASE_SCHEDULE_MeteData._ID + ", "
                + T_BASE_SCHEDULE_MeteData.DATE + ", "
                + T_BASE_SCHEDULE_MeteData.TITLE + ", "
                + T_BASE_SCHEDULE_MeteData.TYPE
                + " from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME
                + " where datetime(" + T_BASE_SCHEDULE_MeteData.DATE + ")>=datetime('" + startdate + "') and "
                + " datetime(" + T_BASE_SCHEDULE_MeteData.DATE + ")<datetime('" + startdateAddOne + "')" + orderBy;
        LogUtils.e("hangh", "queryAllScheduleCurrent sql = " + sql);
        List<SubBaseSchedule> infos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            infos.clear();
            while (cursor.moveToNext()) {
                SubBaseSchedule info = new SubBaseSchedule();
                info.setId(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData._ID)));
                info.setDate(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.DATE)));
                info.setTitle(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.TITLE)));
                int type = cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.TYPE));
                if (type == Constants.SELF_CREATE_TYPE) {
                    //自定义类型,结束时间。
                    String subsql = "select " + T_SELF_CREATE_SCHEDULE_MeteData.END_TIME
                            + " from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME
                            + " where " + T_SELF_CREATE_SCHEDULE_MeteData._ID + " = " + info.getId() + ";";
                    Cursor subcursor = db.rawQuery(subsql, null);
                    if (subcursor.moveToNext()) {
                        info.setEndtime(subcursor.getString(subcursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.END_TIME)));
                    }
                } else {
                    info.setEndtime("notime");
                }
                infos.add(info);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;
    }


    private synchronized List<BaseSchedule> queryBaseScheduleFromToday(SQLiteDatabase db, String sql, int page) {
        List<BaseSchedule> infos = new ArrayList<>();
        List<ExpressSchedule> expressScheduleList = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            infos.clear();
            expressScheduleList.clear();
            while (cursor.moveToNext()) {
                BaseSchedule info = setBaseScheduleInfoValue(cursor);
                switch (info.getType()) {
                    case Constants.SELF_CREATE_TYPE:
                        SelfCreateSchedule selfCreateSchedule = querySelfCreateSchedule(db, info);
                        infos.add(selfCreateSchedule);
                        break;
                    case Constants.TRAIN_TYPE:
                        TrainSchedule trainSchedule = queryTrainSchedule(db, info);
                        infos.add(trainSchedule);
                        break;
                    case Constants.FLIGHT_TYPE:
                        FlightSchedule flightSchedule = queryFlightSchedule(db, info);
                        infos.add(flightSchedule);
                        break;
                    case Constants.BANK_TYPE:
                        BankSchedule bankSchedule = queryBankSchedule(db, info);
                        infos.add(bankSchedule);
                        break;
                    case Constants.HOTEL_TYPE:
                        HotelSchedule hotelSchedule = queryHotelSchedule(db, info);
                        infos.add(hotelSchedule);
                        break;
                    case Constants.MOVIE_TYPE:
                        MovieSchedule movieSchedule = queryMovieSchedule(db, info);
                        infos.add(movieSchedule);
                        break;
                    default:
                        break;
                }
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;
    }

    /**
     * 根据不同的sql语句查询日程信息
     *
     * @param db
     * @param sql
     * @return
     */
    private synchronized List<BaseSchedule> query(SQLiteDatabase db, String sql) {
        List<BaseSchedule> infos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            infos.clear();
            while (cursor.moveToNext()) {
                BaseSchedule info = setBaseScheduleInfoValue(cursor);
                switch (info.getType()) {
                    case Constants.SELF_CREATE_TYPE:
                        SelfCreateSchedule selfCreateSchedule = querySelfCreateSchedule(db, info);
                        infos.add(selfCreateSchedule);
                        break;
                    case Constants.TRAIN_TYPE:
                        TrainSchedule trainSchedule = queryTrainSchedule(db, info);
                        infos.add(trainSchedule);
                        break;
                    case Constants.FLIGHT_TYPE:
                        FlightSchedule flightSchedule = queryFlightSchedule(db, info);
                        infos.add(flightSchedule);
                        break;
                    case Constants.BANK_TYPE:
                        BankSchedule bankSchedule = queryBankSchedule(db, info);
                        infos.add(bankSchedule);
                        break;
                    case Constants.EXPRESS_TYPE:
                        ExpressSchedule expressSchedule = queryExpressSchedule(db, info);
                        infos.add(expressSchedule);
                        break;
                    case Constants.HOTEL_TYPE:
                        HotelSchedule hotelSchedule = queryHotelSchedule(db, info);
                        infos.add(hotelSchedule);
                        break;
                    case Constants.MOVIE_TYPE:
                        MovieSchedule movieSchedule = queryMovieSchedule(db, info);
                        infos.add(movieSchedule);
                        break;
                    default:
                        break;
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;
    }

    private synchronized List<BaseSchedule> queryExceptExpress(SQLiteDatabase db, String sql) {
        List<BaseSchedule> infos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            infos.clear();
            while (cursor.moveToNext()) {
                BaseSchedule info = setBaseScheduleInfoValue(cursor);
                switch (info.getType()) {
                    case Constants.SELF_CREATE_TYPE:
                        SelfCreateSchedule selfCreateSchedule = querySelfCreateSchedule(db, info);
                        infos.add(selfCreateSchedule);
                        break;
                    case Constants.TRAIN_TYPE:
                        TrainSchedule trainSchedule = queryTrainSchedule(db, info);
                        infos.add(trainSchedule);
                        break;
                    case Constants.FLIGHT_TYPE:
                        FlightSchedule flightSchedule = queryFlightSchedule(db, info);
                        infos.add(flightSchedule);
                        break;
                    case Constants.BANK_TYPE:
                        BankSchedule bankSchedule = queryBankSchedule(db, info);
                        infos.add(bankSchedule);
                        break;
                    case Constants.HOTEL_TYPE:
                        HotelSchedule hotelSchedule = queryHotelSchedule(db, info);
                        infos.add(hotelSchedule);
                        break;
                    case Constants.MOVIE_TYPE:
                        MovieSchedule movieSchedule = queryMovieSchedule(db, info);
                        infos.add(movieSchedule);
                        break;
                    default:
                        break;
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return infos;
    }

    public synchronized MovieSchedule queryMovieSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        MovieSchedule info = new MovieSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent(), baseInfo.getSource());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setCinemaName(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.CINEMA_NAME)));
                info.setMovieName(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.MOVIE_NAME)));
                info.setSeatDesc(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.SEAT_DESC)));
                info.setTicketCertificate(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TICKET_CERTIFICATE)));
                info.setPlayTime(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.PLAY_TIME)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    public synchronized HotelSchedule queryHotelSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        HotelSchedule info = new HotelSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setCheckInPeople(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKIN_PEOPLE)));
                info.setCheckOutDate(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKOUT_DATE)));
                info.setHotelAddress(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_ADDRESS)));
                info.setHotelName(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_NAME)));
                info.setRoomCounts(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_COUNTS)));
                info.setRoomStyle(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_STYLE)));
                info.setServiceNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.SERVICE_NUM)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    public synchronized ExpressSchedule queryExpressSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        ExpressSchedule info = new ExpressSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setExpressCompany(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY)));
                info.setExpressNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM)));
                info.setTrace_date(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE))));
                info.setExpressState(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE)));
                info.setExpressProgress(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS)));
                info.setExpressCode(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE)));
                info.setState(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    public synchronized BankSchedule queryBankSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        BankSchedule info = new BankSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent(), baseInfo.getSource());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setBankName(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BANK_NAME)));
                info.setAlertDesc(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.ALERT_DESC)));
                info.setCardNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.CARD_NUM)));
                info.setRepaymentAmount(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_AMOUNT)));
                info.setRepaymentMonth(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_MONTH)));
                info.setBillMonth(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BILL_MONTH)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    public synchronized FlightSchedule queryFlightSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        FlightSchedule info = new FlightSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setAirlineSource(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.AIRLINE_SOURCE)));
                info.setAlertDesc(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ALERT_DESC)));
                info.setArrivalTime(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ARRIVAL_TIME)));
                info.setDestination(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.DESTINATION)));
                info.setFlightNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.FLIGHT_NUM)));
                info.setPassenger(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.PASSENGER)));
                info.setStartAddress(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.START_ADDRESS)));
                info.setServiceNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.SERVICE_NUM)));
                info.setTicketNum(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TICKET_NUM)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    /**
     * 将base日程表中的数据设置到实体
     *
     * @param cursor
     * @return
     */
    private synchronized BaseSchedule setBaseScheduleInfoValue(Cursor cursor) {
        BaseSchedule info = new BaseSchedule();

        info.setAllDay(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.IS_ALL_DAY)) == 1);
        /*modify by zhengjl at 2017-2-7 for search
        查询除自定义日程以外的日程时，查询出的id不对，因为表连接之后，有两个id,取了后面的id，应该取第一个id
         */
//        info.setId(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData._ID)));
        info.setId(cursor.getInt(0));
        info.setTitle(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.TITLE)));
        info.setType(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.TYPE)));
        info.setDate(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.DATE))));
        info.setRemindPeriod(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.REMIND_PERIOD)));
        info.setRemindType(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.REMIND_TYPE)));
        info.setRemindDate(cursor.getLong(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.REMIND_DATE)));
        info.setPeriodID(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.PERIOD_ID)));
        info.setIsSmartRemind(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND)));
        info.setIsActive(cursor.getInt(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE)));
        info.setSmsSender(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.SENDER)));
        info.setSmsContent(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.Content)));
        info.setSource(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.Source)));
        info.setBroadcastSortDate(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE))));
        return info;
    }

    /**
     * 将自定义日程表中的数据设置到实体
     *
     * @param db
     * @param baseInfo
     * @return
     */
    public synchronized SelfCreateSchedule querySelfCreateSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        SelfCreateSchedule info = new SelfCreateSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(),
                baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getPeriodID());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setAddress(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS)));
                info.setDescription(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION)));
                info.setTripMode(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE)));
                info.setEndtime(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.END_TIME))));
                info.setPeriod(cursor.getLong(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.PERIOD)));
                info.setSubPeriodDate(DateUtils.formatDate(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE))));
                info.setSubPeriodId(cursor.getInt(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID)));
                info.setAddressRemark(cursor.getString(cursor.getColumnIndex(T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    /**
     * 将火车日程表中的数据设置到实体
     *
     * @param db
     * @param baseInfo
     * @return
     */
    public synchronized TrainSchedule queryTrainSchedule(SQLiteDatabase db, BaseSchedule baseInfo) {
        TrainSchedule info = new TrainSchedule(baseInfo.getDate(), baseInfo.getType(), baseInfo.getIsAllDay(), baseInfo.getTitle(), baseInfo.getRemindType(), baseInfo.getRemindPeriod(), baseInfo.getRemindDate(), baseInfo.getIsSmartRemind(), baseInfo.getIsRemindActive(), baseInfo.getSmsSender(), baseInfo.getSmsContent());
        int id = baseInfo.getId();
        info.setId(id);
        String sql = "select * from " + T_TRAIN_SCHEDULE_MeteData.TABLE_NAME + " where _id=" + id;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                info.setSeatnumber(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.SEATNUMBER)));
                info.setDeparture(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.DEPARTURE)));
                info.setDestination(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.DESTINATION)));
                info.setTrainnumber(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.TRAINNUMBER)));
                info.setOrdernumber(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.ORDERNUMBER)));
                info.setOrderperson(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.ORDERPERSON)));
                info.setStarttime(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.START_TIME)));
                info.setArrivaltime(cursor.getString(cursor.getColumnIndex(T_TRAIN_SCHEDULE_MeteData.ARRIVAL_TIME)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return info;
        }
        return null;
    }

    /**
     * 获取今天的所有日程提供给早上语音播报
     *
     * @return
     */
    public synchronized List<BaseSchedule> getTodaySchedulesForBroadcast() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<BaseSchedule> allInfos = new ArrayList<>();
        String day = DateUtils.formatDate2StringByDay(new Date());
        String orderBy = " order by " + T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE + " asc";
        String sql = "select * from " + T_BASE_SCHEDULE_MeteData.TABLE_NAME + " where strftime('%Y-%m-%d'," + T_BASE_SCHEDULE_MeteData.DATE + ") = '" + day + "'" + orderBy;
        allInfos = query(db, sql);
        return allInfos;
    }

}
