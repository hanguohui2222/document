package com.gionee.secretary.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gionee.secretary.adapter.MonthViewAdapter;
import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.utils.GregorianUtil;
import com.gionee.secretary.utils.SolarTermsUtil;
import com.gionee.secretary.utils.StringUtil;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.gionee.secretary.R;
import com.gionee.secretary.ui.fragment.MonthFragment;
import com.gionee.secretary.ui.fragment.MonthPageFragment;
import com.gionee.secretary.ui.fragment.VoiceNoteListFragment;
import com.gionee.secretary.ui.viewInterface.ICalendarView;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class CalendarManager {
    private Calendar selectedCalendar;// 目标calendar 表示点击之后的对象
    private ICalendarView mCalendarView;
    private FragmentManager mFragmentManager;
    private static final int PAGE_MONTH = 0;
    private static final int PAGE_WEEK = 1;
    private int mPageType;
    private MonthFragment mMonthFragment;
    private VoiceNoteListFragment mVoiceNoteListFragment;
    private int mMonthScrollType;
    private int mWeekScrollType;
    public static final int SCROLL_FREE = 0;
    public static final int SCROLL_LEFT_DISAGBLE = 1;
    public static final int SCROLL_RIGHT_DISAGBLE = 2;
    public Calendar initCalendar;
    private ScheduleInfoDao mDao;
    private int todayLine;
    private Map<Integer, MonthViewAdapter> monthViewAdapters = new HashMap();

    public CalendarManager(ICalendarView calendarView, FragmentManager fragmentManager, Context context) {
        selectedCalendar = Calendar.getInstance();
        initCalendar = Calendar.getInstance();
        mCalendarView = calendarView;
        mFragmentManager = fragmentManager;
        mDao = ScheduleInfoDao.getInstance(context.getApplicationContext());
    }

    public void onDateChanged() {
        MonthViewAdapter monthAdapter = getMonthViewAdapter(mMonthFragment.getCurPosition());
        if (monthAdapter != null) {
            monthAdapter.notifyDataSetChanged();
            mCalendarView.updateCurrentWeekView();
        }
    }

    public void setTodayLine(int todayLine) {
        this.todayLine = todayLine;
    }

    public int getLine() {
        return selectedCalendar.get(Calendar.WEEK_OF_MONTH);
    }

    public void onBackPressed() {
        switch (mPageType) {
            case PAGE_MONTH:
                mCalendarView.onBackPressed();
                break;
            case PAGE_WEEK:
                showCalendarView();
                break;
            default:
                mCalendarView.onBackPressed();
        }
    }

    public void showCalendarView() {
        mMonthFragment = new MonthFragment();
        mFragmentManager.beginTransaction().replace(R.id.month_view, mMonthFragment).commit();
    }

    public void showNoteFragment() {
        mVoiceNoteListFragment = new VoiceNoteListFragment();
        mFragmentManager.beginTransaction().replace(R.id.fl_content, mVoiceNoteListFragment).commit();
    }

    public void toToday() {
        selectedCalendar.setTime(Calendar.getInstance().getTime());
        mCalendarView.updateSchedule(selectedCalendar);
        mMonthFragment.toToday();
        mCalendarView.updateWeekViewByOther(getWeekCalendarPosition(Calendar.getInstance()));
    }

    public void updateWeekViewByOther() {
        mCalendarView.updateWeekViewByOther(getWeekCalendarPosition(selectedCalendar));
    }

    public void updateWeekView() {
        mCalendarView.updateWeekView(getWeekCalendarPosition(selectedCalendar));
    }

    private int getWeekCalendarPosition(Calendar cal) {
        Calendar c2 = Calendar.getInstance();
        c2.setTime(cal.getTime());
        c2.set(Calendar.DAY_OF_WEEK, 0);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(initCalendar.getTime());
        c1.set(Calendar.DAY_OF_WEEK, 0);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        return (int) ((c2.getTime().getTime() - c1.getTime().getTime()) / (1000 * 60 * 60 * 24) / 7 + INDEX_INIT);
    }

    public String initMonthInfoByMonth(int position) {
        Calendar calendar = getSelectCalendar(initCalendar, position);
        return upDateMonthInfo(calendar);
    }

    public String upDateMonthInfoByMonth(int position) {
        Calendar calendar = getSelectCalendar(initCalendar, position);
        // if (!checkISMonthSelected(position)) updateWeekView(calendar);
        return upDateMonthInfo(calendar);
    }

    public String upDateMonthInfoByWeek(int position) {
        Calendar calendar = getSelectWeekCalendar(initCalendar, position);
        return upDateMonthInfo(calendar);
    }

    private String upDateMonthInfo(Calendar calendar) {
        String monthInfo = calendar.get(Calendar.YEAR) + "." + LeftPad_Tow_Zero(calendar.get(Calendar.MONTH) + 1);
        mCalendarView.upDateMonthInfo(monthInfo);
        checkMonthScrollble(monthInfo);
        return monthInfo;
    }

    public void setCalendar(Calendar calendar) {
        selectedCalendar = calendar;
        mCalendarView.updateSchedule(calendar);
    }

    public Calendar getCalendar() {
        return selectedCalendar;
    }

    public void nextMonth() {
        selectedCalendar.add(Calendar.MONTH, 1);
        mCalendarView.updateSchedule(selectedCalendar);
    }

    public void preMonth() {
        selectedCalendar.add(Calendar.MONTH, -1);
        mCalendarView.updateSchedule(selectedCalendar);
    }

    public void nextWeek() {
        selectedCalendar.add(Calendar.DAY_OF_WEEK, 7);
        mCalendarView.updateSchedule(selectedCalendar);
    }

    public void preWeek() {
        selectedCalendar.add(Calendar.DAY_OF_WEEK, -7);
        mCalendarView.updateSchedule(selectedCalendar);
    }

    public void updateMonthPage(Calendar calendar) {
        int index = getAssignedIndexByMonth(calendar);
        mMonthFragment.updatePage(index);
    }

    private void checkMonthScrollble(String monthInfo) {
        float f = Float.parseFloat(monthInfo);
        if (f >= 2036.11) {
            mMonthScrollType = SCROLL_LEFT_DISAGBLE;
        } else if (f <= 1970.02) {
            mMonthScrollType = SCROLL_RIGHT_DISAGBLE;
        } else {
            mMonthScrollType = SCROLL_FREE;
        }
    }

    private void checkWeekScrollble() {
        if (selectedCalendar.get(Calendar.YEAR) > 2036) {
            mWeekScrollType = SCROLL_LEFT_DISAGBLE;
        } else if (selectedCalendar.get(Calendar.YEAR) < 1970) {
            mWeekScrollType = SCROLL_RIGHT_DISAGBLE;
        } else {
            mWeekScrollType = SCROLL_FREE;
        }
    }

    public int getmMonthScrollType() {
        return mMonthScrollType;
    }

    public int getmWeekScrollType() {
        return mWeekScrollType;
    }

    public void clickData() {
        switch (mPageType) {
            case PAGE_MONTH:
                mCalendarView.showSetTimeDialog();
                break;
            case PAGE_WEEK:
                showCalendarView();
                break;
            default:

        }
    }

    public Fragment getMonthPageFragment(int position) {
        return MonthPageFragment.create(position);
    }

    public Calendar getTargetMonthPageCalendar(int position) {
        return getSelectCalendar(initCalendar, position);
    }

    public List<BaseSchedule> queryScheduleByDate(Date date) {
        String str = DateUtils.formatDate2StringByDay(date);
        return mDao.queryScheduleByDate(str);
    }

    private Map<String, List<Integer>> scheduleFlagsMap = new HashMap();

    /**
     * 在更新schedule flag
     *
     * @param targetCalendar
     * @return
     */
    public void updateScheduleFlags() {
        new Thread() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedCalendar.getTime());
                Date date = calendar.getTime();
                String key = DateUtils.formatDate2StringByMonth(date);
                List<Integer> list = mDao.queryScheduleByMonth(key);
                if (scheduleFlagsMap.get(key) != null && (!list.containsAll(scheduleFlagsMap.get(key)) || !scheduleFlagsMap.get(key).containsAll(list))
                        || scheduleFlagsMap.get(key) == null
                        && list.size() != 0) {// 避免多刷新一次adapter
                    scheduleFlagsMap.put(key, list);
                    ((Activity) mCalendarView).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mCalendarView.upDateWeekScheduleInfo();
                            MonthViewAdapter adapter = getMonthViewAdapter(mMonthFragment.getCurPosition());
                            if (adapter != null) adapter.notifyDataSetChanged();
                        }

                    });

                }
            }
        }.start();
    }

    boolean hasSchedule;

    public boolean hasSchedule(String date, int day) {
        List<Integer> list = scheduleFlagsMap.get(date);
        if (list != null) {
            return list.contains(day);
        }
        return false;
    }

    public List<ExpressSchedule> queryExpressList(List<BaseSchedule> list,Calendar cal) {
        for (BaseSchedule schedule : list){
            if(schedule instanceof ExpressSchedule){
                list.remove(schedule);
            }
        }
        List<ExpressSchedule> expressSchedules = mDao.queryAllExpressSchedule();
        Calendar today = Calendar.getInstance();
        List<ExpressSchedule> schedule = new ArrayList<>();
        schedule.clear();
        for (ExpressSchedule e : expressSchedules) {
//            if (e.getState() == 3 && !DateUtils.date2String(e.getTrace_date()).equals(DateUtils.date2String(today))) {
//                continue;
//            } else {
            Date traceDate = e.getTrace_date();
            Date date = cal.getTime();
            boolean isSameDay = DateUtils.inSameDay(traceDate,date);
            if(cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
                //今天的快递
                if(e.getState() != 3){
                    schedule.add(e);
                } else if(e.getState() == 3 && isSameDay){
                    schedule.add(e);
                }
            } else {
                if(e.getState() == 3 && isSameDay){
                    schedule.add(e);
                }
            }
        }
        if (null != schedule && schedule.size() > 0) {
            list.add(0, schedule.get(0));
        }
        return schedule;
    }

    public void startScheduleDetailsActivity(Context context, BaseSchedule schedule) {

        int type = schedule.getType();
        Intent intent = new Intent();
        switch (type) {
            case Constants.SELF_CREATE_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                SelfCreateSchedule selfCreateSchedule = (SelfCreateSchedule) schedule;
                intent.putExtra("schedule", selfCreateSchedule);
                break;
            case Constants.BANK_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                BankSchedule bankSchedule = (BankSchedule) schedule;
                intent.putExtra("schedule", bankSchedule);
                break;
            case Constants.TRAIN_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                TrainSchedule trainSchedule = (TrainSchedule) schedule;
                intent.putExtra("schedule", trainSchedule);
                break;
            case Constants.FLIGHT_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                FlightSchedule flightSchedule = (FlightSchedule) schedule;
                intent.putExtra("schedule", flightSchedule);
                break;
            case Constants.MOVIE_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                MovieSchedule movieSchedule = (MovieSchedule) schedule;
                intent.putExtra("schedule", movieSchedule);
                break;
            case Constants.HOTEL_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                HotelSchedule hotelSchedule = (HotelSchedule) schedule;
                intent.putExtra("schedule", hotelSchedule);
                break;
            case Constants.EXPRESS_TYPE:
                intent.setClass(context, CardDetailsActivity.class);
                ExpressSchedule expressSchedule = (ExpressSchedule) schedule;
                intent.putExtra("schedule", expressSchedule);
                break;

        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(intent);
    }

    public void putMonthAdapter(int key, MonthViewAdapter adapter) {
        monthViewAdapters.put(key, adapter);
    }

    public void removeMonthAdapter(int position) {
        monthViewAdapters.remove(position);
    }

    public MonthViewAdapter getMonthViewAdapter(int position) {
        return monthViewAdapters.get(position);
    }

    Handler handler = new Handler();

    public void postDelayed(Runnable r) {
        handler.postDelayed(r, 350);
    }

    public void postDelayed(Runnable r, int time) {
        handler.postDelayed(r, time);
    }

    // -------------------------------------------------------------------------------

    public static final int INDEX_INIT = 1000;
    public static final int INDEX_TOTLE = 2000;

    private final static String CHINESE_NUMBER[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊"};

    private final static String WEEK_NUMBER[] = {"日", "一", "二", "三", "四", "五", "六"};

    private final static long[] LUNAR_INFO = new long[]{0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255,
            0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0,
            0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0,
            0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4,
            0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50,
            0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
            0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
            0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    /**
     * 转换为2012年11月22日格式
     */
    private static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    /**
     * 转换为2012-11-22格式
     */
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 计算得到农历的年份
     */
    private int mLuchYear;
    /**
     * 计算得到农历的月份
     */
    private int mLuchMonth;

    /**
     * 计算得到农历的日期
     */
    private int mLuchDay;

    /**
     * 用于标识是事为闰年
     */
    private boolean isLoap;

    /**
     * 用于记录当前处理的时间
     */
    private Calendar mCurrenCalendar;

    /**
     * 传回农历 year年的总天数
     *
     * @param year 将要计算的年份
     * @return 返回传入年份的总天数
     */
    private static int yearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(year));
    }

    /**
     * 传回农历 year年闰月的天数
     *
     * @param year 将要计算的年份
     * @return 返回 农历 year年闰月的天数
     */
    private static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((LUNAR_INFO[year - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    /**
     * 传回农历 year年闰哪个月 1-12 , 没闰传回 0
     *
     * @param year 将要计算的年份
     * @return 传回农历 year年闰哪个月 1-12 , 没闰传回 0
     */
    private static int leapMonth(int year) {
        return (int) (LUNAR_INFO[year - 1900] & 0xf);
    }

    /**
     * 传回农历 year年month月的总天数
     *
     * @param year  将要计算的年份
     * @param month 将要计算的月份
     * @return 传回农历 year年month月的总天数
     */
    private static int monthDays(int year, int month) {
        if ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * 传回农历 y年的生肖
     *
     * @return 传回农历 y年的生肖
     */
    public String animalsYear() {
        final String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        return Animals[(mLuchYear - 4) % 12];
    }

    // ====== 传入 月日的offset 传回干支, 0=甲子
    private static String cyclicalm(int num) {
        final String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        final String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        return (Gan[num % 10] + Zhi[num % 12]);
    }

    // ====== 传入 offset 传回干支, 0=甲子
    public String cyclical() {
        int num = mLuchYear - 1900 + 36;
        return (cyclicalm(num));
    }

    /**
     * 传出y年m月d日对应的农历. yearCyl3:农历年与1864的相差数 ? monCyl4:从1900年1月31日以来,闰月数
     * dayCyl5:与1900年1月31日相差的天数,再加40 ?
     *
     * @param cal
     * @return
     */

    public void initChineseCalendar(Calendar cal) {
        int yearCyl, monCyl, dayCyl;
        mCurrenCalendar = cal;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            e.printStackTrace(); // To change body of catch statement use
            // Options | File Templates.
        }

        // 求出和1900年1月31日相差的天数
        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;

        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        // 农历年份
        mLuchYear = iYear;

        yearCyl = iYear - 1864;
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        isLoap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !isLoap) {
                --iMonth;
                isLoap = true;
                daysOfMonth = leapDays(mLuchYear);
            } else
                daysOfMonth = monthDays(mLuchYear, iMonth);

            offset -= daysOfMonth;
            // 解除闰月
            if (isLoap && iMonth == (leapMonth + 1))
                isLoap = false;
            if (!isLoap)
                monCyl++;
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (isLoap) {
                isLoap = false;
            } else {
                isLoap = true;
                --iMonth;
                --monCyl;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;

        }
        mLuchMonth = iMonth;
        mLuchDay = offset + 1;
    }

    /**
     * 返化成中文格式
     *
     * @param day
     * @return
     */
    public static String getChinaDayString(int day) {
        String chineseTen[] = {"初", "十", "廿", "三"};
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30)
            return "";
        if (day == 10)
            return "初十";
        else
            return chineseTen[day / 10] + CHINESE_NUMBER[n];
    }

    /**
     * 用于显示农历的初几这种格式
     *
     * @return 农历的日期
     */
    public String getChineseDay(Calendar cal) {
        int yearCyl, monCyl, dayCyl;
        // mCurrenCalendar = cal;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            e.printStackTrace(); // To change body of catch statement use
            // Options | File Templates.
        }

        // 求出和1900年1月31日相差的天数
        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;

        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        // 农历年份
        mLuchYear = iYear;

        yearCyl = iYear - 1864;
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        isLoap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !isLoap) {
                --iMonth;
                isLoap = true;
                daysOfMonth = leapDays(mLuchYear);
            } else
                daysOfMonth = monthDays(mLuchYear, iMonth);

            offset -= daysOfMonth;
            // 解除闰月
            if (isLoap && iMonth == (leapMonth + 1))
                isLoap = false;
            if (!isLoap)
                monCyl++;
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (isLoap) {
                isLoap = false;
            } else {
                isLoap = true;
                --iMonth;
                --monCyl;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;

        }
        mLuchMonth = iMonth;
        mLuchDay = offset + 1;

        String message = "";
        int n = mLuchDay % 10 == 0 ? 9 : mLuchDay % 10 - 1;
        message = getChinaCalendarMsg(mLuchYear, mLuchMonth, mLuchDay);
        if (StringUtil.isNullOrEmpty(message)) {
            String solarMsg = new SolarTermsUtil(cal).getSolartermsMsg();
            // 判断当前日期是否为节气
            if (!StringUtil.isNullOrEmpty(solarMsg)) {
                message = solarMsg;
            } else {
                /**
                 * 判断当前日期是否为公历节日
                 */
                String gremessage = new GregorianUtil(cal).getGremessage();
                if (!StringUtil.isNullOrEmpty(gremessage)) {
                    message = gremessage;
                } else if (mLuchDay == 1) {
                    message = CHINESE_NUMBER[mLuchMonth - 1] + "月";
                } else {
                    message = getChinaDayString(mLuchDay);
                }

            }
        }
        return message;
    }

    /**
     * 返回农历的年月日
     *
     * @return 农历的年月日格式
     */
    public String getDay() {
        return (isLoap ? "闰" : "") + CHINESE_NUMBER[mLuchMonth - 1] + "月" + getChinaDayString(mLuchDay);
    }

    /**
     * 返回农历的月日
     *
     * @return 农历的年月日格式
     */
    public String getLunarMonthDay(Calendar cal) {
        initChineseCalendar(cal);
        return getDay();
    }

    /**
     * 把calendar转化为当前年月日
     *
     * @param calendar Calendar
     * @return 返回成转换好的 年月日格式
     */
    public static String getDay(Calendar calendar) {
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 用于比对二个日期的大小
     *
     * @param compareDate 将要比对的时间
     * @param currentDate 当前时间
     * @return true 表示大于当前时间 false 表示小于当前时间
     */
    public static boolean compare(Date compareDate, Date currentDate) {
        return chineseDateFormat.format(compareDate).compareTo(chineseDateFormat.format(currentDate)) >= 0;
    }

    /**
     * 获取当前周几
     *
     * @param calendar
     * @return
     */
    public static String getWeek(Calendar calendar) {
        return "周" + WEEK_NUMBER[calendar.get(Calendar.DAY_OF_WEEK) - 1] + "";
    }

    /**
     * 将当前时间转换成要展示的形式
     *
     * @param calendar
     * @return
     */
    // public static String getCurrentDay(Calendar calendar) {
    // initCalendar(calendar);
    // return getDay(calendar) + " 农历" + new CalendarUtils(calendar).getDay() +
    // " " + getWeek(calendar);
    // }

    /**
     * 用于获取中国的传统节日
     *
     * @param month 农历的月
     * @param day   农历日
     * @return 中国传统节日
     */
    private String getChinaCalendarMsg(int year, int month, int day) {
        String message = "";
        if (((month) == 1) && day == 1) {
            message = "春节";
        } else if (((month) == 1) && day == 15) {
            message = "元宵";
        } else if (((month) == 5) && day == 5) {
            message = "端午";
        } else if ((month == 7) && day == 7) {
            message = "七夕";
        } else if (((month) == 8) && day == 15) {
            message = "中秋";
        } else if ((month == 9) && day == 9) {
            message = "重阳";
        } else if ((month == 12) && day == 8) {
            message = "腊八";
        } else {
            if (month == 12) {
                if ((((monthDays(year, month) == 29) && day == 29)) || ((((monthDays(year, month) == 30) && day == 30)))) {
                    message = "除夕";
                }
            }
        }
        return message;
    }

    public String LeftPad_Tow_Zero(int str) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("00");
        return format.format(str);
    }

    public Calendar getSelectCalendar(Calendar cal, int mPageNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cal.getTime());
        if (mPageNumber > INDEX_INIT) {
            for (int i = 0; i < mPageNumber - INDEX_INIT; i++) {
                calendar = setNextViewItem(calendar);
            }
        } else if (mPageNumber < INDEX_INIT) {
            for (int i = 0; i < INDEX_INIT - mPageNumber; i++) {
                calendar = setPrevViewItem(calendar);
            }
        } else {

        }
        return calendar;
    }

    private Calendar setPrevViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth--;// 当前选择月--

        if (iMonthViewCurrentMonth == -1) {
            iMonthViewCurrentMonth = 11;
            iMonthViewCurrentYear--;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置日为当月1日
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth); // 设置月
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear); // 设置年
        return calendar;
    }

    private Calendar setNextViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth++;
        if (iMonthViewCurrentMonth == 12) {
            iMonthViewCurrentMonth = 0;
            iMonthViewCurrentYear++;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth);
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear);
        return calendar;
    }

    public Calendar getSelectWeekCalendar(Calendar cal, int mPageNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cal.getTime());
        if (mPageNumber > INDEX_INIT) {
            for (int i = 0; i < mPageNumber - INDEX_INIT; i++) {
                calendar = setNextWeekItem(calendar);
            }
        } else if (mPageNumber < INDEX_INIT) {
            for (int i = 0; i < INDEX_INIT - mPageNumber; i++) {
                calendar = setPrevWeekItem(calendar);
            }
        }
        return calendar;
    }

    private Calendar setPrevWeekItem(Calendar calendar) {
        calendar.add(Calendar.WEEK_OF_MONTH, -1);
        return calendar;
    }

    private Calendar setNextWeekItem(Calendar calendar) {
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        return calendar;
    }

    public int getTodayIndexByMonth() {
        Calendar calendar = Calendar.getInstance();
        return getAssignedIndexByMonth(calendar);
    }

    // TODO 日历设置偏差很大有bug
    public int getTodayIndexByWeek(Calendar cal) {
        Calendar calendar = Calendar.getInstance();
        int curWeek = cal.get(Calendar.WEEK_OF_YEAR);
        int curYear = cal.get(Calendar.YEAR);
        int todayWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        int todayYear = calendar.get(Calendar.YEAR);
        if (todayYear < curYear) {
            return INDEX_INIT - 1 + ((todayYear - curYear) * 52 + todayWeek - curWeek);
        }
        return INDEX_INIT + ((todayYear - curYear) * 52 + todayWeek - curWeek);
    }

    public int getAssignedIndexByMonth(Calendar targetCalendar) {
        int todayMonth = initCalendar.get(Calendar.MONTH);
        int todayYear = initCalendar.get(Calendar.YEAR);
        int targetMonth = targetCalendar.get(Calendar.MONTH);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return INDEX_INIT + ((targetYear - todayYear) * 12 + targetMonth - todayMonth);
    }

    public static List<Date> getRemindPeriodDates(Context context, Date date, String remindPeriod) {
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String[] array = context.getResources().getStringArray(R.array.remind_period);
        if (array[0].equals(remindPeriod) || remindPeriod == null) {
            // 一次
            dateList.add(date);
        } else if (array[1].equals(remindPeriod)) {
            // 每日
            for (int i = 1; i <= 365 * 2; i++) {
                dateList.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else if (array[2].equals(remindPeriod)) {
            // 每周
            for (int i = 1; i <= 52 * 2; i++) {
                dateList.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
        } else if (array[3].equals(remindPeriod)) {
            // 每月
            for (int i = 1; i <= 24; i++) {
                // Gionee sunyang modify for GNSPR #65021 at 2017-01-19 begin
                int days = getDaysByYearMonth(calendar);
                if (days >= date.getDate()) {
                    calendar.set(Calendar.DAY_OF_MONTH, date.getDate());
                    dateList.add(calendar.getTime());
                }
                // Gionee sunyang modify for GNSPR #65021 at 2017-01-19 end
                calendar.add(Calendar.MONTH, 1);
            }
        } else if (array[4].equals(remindPeriod)) {
            boolean flag = calendar.get(Calendar.YEAR) % 4 == 0 && calendar.get(Calendar.MONTH) == 1 && calendar.get(Calendar.DAY_OF_MONTH) == 29;//润年二月二十九号特殊处理;
            // 每年
            for (int i = 1; i <= 10; i++) {
                if (!flag) {
                    LogUtils.d("liyu", "getRemindPeriodDates calendar YEAR = " + calendar.get(Calendar.YEAR));
                    dateList.add(calendar.getTime());
                } else if (flag && calendar.get(Calendar.YEAR) % 4 == 0) {
                    calendar.set(Calendar.DAY_OF_MONTH, 29);
                    LogUtils.d("liyu", "getRemindPeriodDates calendar YEAR = " + calendar.get(Calendar.YEAR));
                    dateList.add(calendar.getTime());
                }
                calendar.add(Calendar.YEAR, 1);
            }
        }
        return dateList;
    }

    public static List<Date> getRemindPeriodRemindDates(Context context, Date remindDate, String remindPeriod) {
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(remindDate);
        String[] array = context.getResources().getStringArray(R.array.remind_period);
        if (array[0].equals(remindPeriod) || remindPeriod == null) {
            // 一次
            dateList.add(remindDate);
        } else if (array[1].equals(remindPeriod)) {
            // 每日
            for (int i = 1; i <= 365 * 2; i++) {
                dateList.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else if (array[2].equals(remindPeriod)) {
            // 每周
            for (int i = 1; i <= 52 * 2; i++) {
                dateList.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
        } else if (array[3].equals(remindPeriod)) {
            // 每月
            for (int i = 1; i <= 24; i++) {
                // Gionee sunyang modify for GNSPR #65021 at 2017-01-19 begin
                int days = getDaysByYearMonth(calendar);
                if (days >= remindDate.getDate()) {
                    calendar.set(Calendar.DAY_OF_MONTH, remindDate.getDate());
                    dateList.add(calendar.getTime());
                }
                // Gionee sunyang modify for GNSPR #65021 at 2017-01-19 end
                calendar.add(Calendar.MONTH, 1);
            }
        } else if (array[4].equals(remindPeriod)) {
            boolean flag = calendar.get(Calendar.YEAR) % 4 == 0 && calendar.get(Calendar.MONTH) == 1 && calendar.get(Calendar.DAY_OF_MONTH) == 29;//润年二月二十九号特殊处理;
            // 每年
            for (int i = 1; i <= 10; i++) {
                if (!flag) {
                    LogUtils.d("liyu", "getRemindPeriodDates calendar YEAR = " + calendar.get(Calendar.YEAR));
                    dateList.add(calendar.getTime());
                } else if (flag && calendar.get(Calendar.YEAR) % 4 == 0) {
                    calendar.set(Calendar.DAY_OF_MONTH, 29);
                    LogUtils.d("liyu", "getRemindPeriodDates calendar YEAR = " + calendar.get(Calendar.YEAR));
                    dateList.add(calendar.getTime());
                }
                calendar.add(Calendar.YEAR, 1);
            }
        }
        return dateList;
    }

    /**
     * 根据年 月 获取对应的月份 天数
     */
    public static int getDaysByYearMonth(Calendar calendar) {
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);
        int maxDate = calendar.get(Calendar.DATE);
        return maxDate;
    }
}
