package com.gionee.secretary.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;

import java.util.Date;
import java.util.List;

import amigoui.widget.AmigoEditText;
import amigoui.widget.AmigoNumberPicker;

/**
 * Created by hangh on 9/1/17.
 */

public class SecretaryUtils {
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidContext(Activity activity) {
        Activity a = activity;
        if (a.isDestroyed() || a.isFinishing()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 当前系统语言是否为简体中文
     *
     * @return
     */
    public static boolean isChinese(Context context) {
        String locale = context.getApplicationContext().getResources().getConfiguration().locale.getCountry();
        return "CN".equals(locale);
    }

    public static long getRemindTimeLong(Context context, Date startTime, String remindType,
                                         AmigoNumberPicker mDayPicker,AmigoNumberPicker mHourPicker,AmigoNumberPicker mMinutePicker) {
        long event = startTime.getTime();
        long remindtime = -1;
        if (context.getResources().getString(R.string.not_remind).equals(remindType)) {
            remindtime = -1;
        } else if (remindType.contains(context.getResources().getString(R.string.on_time))) {
            remindtime = event;
        } else if (context.getResources().getString(R.string.ten_min_ago).equals(remindType) || context.getResources().getString(R.string.smart_remind).equals(remindType)) {
            long porid = 10 * 60 * 1000;
            remindtime = event - porid;
        } else if (context.getResources().getString(R.string.half_hour_ago).equals(remindType)) {
            long porid = 30 * 60 * 1000;
            remindtime = event - porid;
        } else if (context.getResources().getString(R.string.one_hour_ago).equals(remindType)) {
            long porid = 1 * 60 * 60 * 1000;
            remindtime = event - porid;
        } else if (context.getResources().getString(R.string.one_day_ago).equals(remindType)) {
            long porid = 1 * 24 * 60 * 60 * 1000;
            remindtime = event - porid;
        } else {
            //自定义提醒
            int day = 0;
            int hour = 0;
            int minute = 0;
            if (mDayPicker == null && !"".equals(remindType)) {
                String remind = remindType;
                remind = remind.replace(context.getResources().getString(R.string.defined_day), ":").
                        replace(context.getResources().getString(R.string.defined_hour), ":").
                        replace(context.getResources().getString(R.string.defined_minute), "");
                if (remind.split(":").length != 0) {
                    day = Integer.parseInt(remind.split(":")[0]);
                    hour = Integer.parseInt(remind.split(":")[1]);
                    minute = Integer.parseInt(remind.split(":")[2]);
                }
            } else {
                day = mDayPicker.getValue();
                hour = mHourPicker.getValue();
                minute = mMinutePicker.getValue();
            }
            long porid = day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000 + minute * 60 * 1000;
            remindtime = event - porid;
        }
        return remindtime;
    }

    public static int getIsSmart(Context context,String remindType) {
        if (context.getResources().getString(R.string.not_remind).equals(remindType)) {
            return Constants.NOT_REMIND;
        } else if (context.getResources().getString(R.string.smart_remind).equals(remindType)) {
            return Constants.SMART_REMIND;
        } else {
            return Constants.GENERAL_REMIND;
        }
    }

    public static boolean isRepeateEvent(Context context, String mRecyleName) {
        boolean mIsRepeateEvent = false;
        if (!context.getResources().getString(R.string.once).equals(mRecyleName)) {
            mIsRepeateEvent = true;//自建日程 非一次性事件
        } else {
            mIsRepeateEvent = false;//自建日程 一次性事件
        }
        return mIsRepeateEvent;
    }


    /**
     * Hide Soft Input  隐藏软键盘
     */
    public static void hideSoftInput(Context context, AmigoEditText mEtTitle,AmigoEditText mDescription) {
        InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        LogUtils.d("liyh", "SelfCreateScheduleActivity. hideSoftInput()." + "immActive=" + imm.isActive());
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mEtTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            imm.hideSoftInputFromInputMethod(mDescription.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
