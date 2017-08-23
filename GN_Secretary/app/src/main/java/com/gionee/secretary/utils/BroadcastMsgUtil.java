package com.gionee.secretary.utils;

import android.content.Context;

import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luorw on 3/24/17.
 */
public class BroadcastMsgUtil {
    private static final String NO_SCHEDULE = "您今天没有行程！！";
    private static final String HAS_SCHEDULE = "今日行程！！";
    private static final String OTHER_SCHEDULES = "个其他行程！";
    private static final String BY_AIR = "乘坐飞机！";
    private static final String BY_TRAIN = "乘坐火车！";
    private static final String BY_HOTEL = "中午12点，入住酒店！";
    private static final String WATCH_MOVIE = "观看电影！";
    public static final String NETWORK_UNAVAILABLE = "没有网络连接，我查询不到天气情况！！";

    /**
     * 随机获得问候语
     *
     * @param context
     * @return
     */
    public static String getMorningTips(Context context) {
        String[] helloArrays = null;
        int hour = new Date().getHours();
        if (hour >= 0 && hour <= 12) {
            helloArrays = context.getResources().getStringArray(R.array.morning_tips);
        } else if (hour > 12 && hour <= 18) {
            helloArrays = context.getResources().getStringArray(R.array.afternoon_tips);
        } else {
            helloArrays = context.getResources().getStringArray(R.array.evening_tips);
        }
        int id = (int) (Math.random() * (helloArrays.length - 1));//随机产生一个index索引
        return helloArrays[id];
    }

    /**
     * 获取日程播报内容
     *
     * @param scheduleList
     * @return
     */
    public static String getBroadcastMsg(List<BaseSchedule> scheduleList, String weatherMsg, Context context) {
        int selfCreateNum = 0;
        int notSelfCreateNum = 0;
        String morningTips = getMorningTips(context);
        String msg = morningTips + weatherMsg;
        //没有行程的播报内容
        if (scheduleList == null || scheduleList.size() == 0) {
            return msg + NO_SCHEDULE;
        } else {
            msg = msg + HAS_SCHEDULE;
        }
        Map<Integer, Integer> expressMap = new HashMap<>();
        //有行程的播报内容
        for (int i = 0; i < scheduleList.size(); i++) {
            BaseSchedule schedule = scheduleList.get(i);
            int type = schedule.getType();
            if (type != Constants.SELF_CREATE_TYPE) {
                notSelfCreateNum++;
                switch (type) {
                    case Constants.BANK_TYPE:
                        msg += getBankState((BankSchedule) schedule);
                        break;
                    case Constants.EXPRESS_TYPE:
                        getExpressState((ExpressSchedule) schedule, expressMap);
                        break;
                    case Constants.FLIGHT_TYPE:
                        msg += getSubMsg(schedule, BY_AIR);
                        break;
                    case Constants.HOTEL_TYPE:
                        msg += BY_HOTEL;
                        break;
                    case Constants.MOVIE_TYPE:
                        msg += getSubMsg(schedule, WATCH_MOVIE);
                        break;
                    case Constants.TRAIN_TYPE:
                        msg += getSubMsg(schedule, BY_TRAIN);
                        break;
                }
            } else {
                selfCreateNum++;
            }
        }
        //加上快递的信息
        if (expressMap.size() != 0) {
            msg += getExpressStr(expressMap);
        }
        //最后是其他日程信息
        if (selfCreateNum != 0) {
            if (notSelfCreateNum == 0) {
                msg += "您有" + selfCreateNum + OTHER_SCHEDULES;
            } else {
                msg += "您还有" + selfCreateNum + OTHER_SCHEDULES;
            }
        }
        return msg;
    }

    /**
     * 东亚银行贷款账单/建设银行信用卡还款
     *
     * @param schedule
     * @return
     */
    private static String getBankState(BankSchedule schedule) {
        String stateStr = schedule.getBankName() + schedule.getTitle() + "！！";
        return stateStr;
    }

    private static void getExpressState(ExpressSchedule schedule, Map<Integer, Integer> expressMap) {
        int state = schedule.getState();
        //把所有重复状态的快递计数
        if (expressMap.containsKey(state)) {
            int repeatStateNum = expressMap.get(state);
            expressMap.put(state, repeatStateNum + 1);
        } else {
            expressMap.put(state, 1);
        }
    }

    private static String getExpressStr(Map<Integer, Integer> expressMap) {
        String stateStr = "您有";
        for (Integer state : expressMap.keySet()) {
            int repeatStateNum = expressMap.get(state);
            switch (state) {
                case 3:
                    stateStr += repeatStateNum + "个快递已签收！！";
                    break;
                case 2:
                case 4:
                    stateStr += repeatStateNum + "个快递运输中！！";
                    break;
                default:
                    stateStr += repeatStateNum + "个快递未发货！！";
                    break;
            }
        }
        return stateStr;
    }

    private static String getSubMsg(BaseSchedule schedule, String event) {
        String subMsg;
        String date;
        int hour = schedule.getDate().getHours();
        int minute = schedule.getDate().getMinutes();
        if (hour < 12) {
            date = "上午" + hour + "点";
        } else if (hour == 12) {
            date = "中午" + hour + "点";
        } else if (hour > 12 && hour < 18) {
            date = "下午" + (hour - 12) + "点";
        } else {
            date = "晚上" + (hour - 12) + "点";
        }
        if (minute > 0 && minute < 10) {
            date += "0" + minute + "分";
        } else if (minute >= 10) {
            date += minute + "分";
        }
        subMsg = date + "，" + event;
        return subMsg;
    }

    /**
     * @param list
     * @return
     */
    public static String getWeatherMsg(List<WeatherSchedule> list) {
        String weatherMsg;
        WeatherSchedule weatherSchedule = list.get(0);
        weatherMsg = weatherSchedule.getAddress() + "：" + weatherSchedule.getWeather() + "！" + weatherSchedule.getTemp() + "℃！！";
        return weatherMsg;
    }

}
