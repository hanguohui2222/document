package com.gionee.secretary.module.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;

/**
 * Created by liu on 5/12/16.
 */
public class SettingModel {
    private SharedPreferences mSharedPreferences;
    private String mDefault_travelMethod;
    private Context mContext;
    private static SettingModel mSettingsModel = null;
    private String password;
    private String mDefaultRingtoneTitle;
    private String mDefaultRingtoneUri;

    public static SettingModel getInstance(Context context) {
        if (mSettingsModel == null) {
            synchronized (SettingModel.class) {
                if (mSettingsModel == null) {
                    mSettingsModel = new SettingModel(context.getApplicationContext());
                }
            }
        }
        return mSettingsModel;
    }

    private SettingModel(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mDefault_travelMethod = mContext.getResources().getString(R.string.travel_default_method_value);
    }

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getDefaultTravelMethod() {
        String travelMethod = mSharedPreferences.getString(Constants.TRAVEL_MODE_PREFERENCE_KEY, mDefault_travelMethod);
        return travelMethod;
    }

    public boolean isShowStatusOfExpress() {
        return mSharedPreferences.getBoolean(Constants.EXPRESS_SWITCH_PREFERENCE_KEY, true);
    }

    public boolean isShowWidget() {
        return mSharedPreferences.getBoolean(Constants.WIDGET_SWITCH_PREFERENCE_KEY, true);
    }

    public boolean isRemindSchedule() {
        return mSharedPreferences.getBoolean(Constants.REMIND_SWITCH_PREFERENCE_KEY, true);
    }

    public void setRemindScheduleSwitch(boolean isRemind) {
        editSharePreferences(Constants.REMIND_SWITCH_PREFERENCE_KEY, isRemind);
    }

    public void setBroadcastScheduleSwitch(boolean isBroadcast) {
        editSharePreferences(Constants.BROADCAST_SWITCH_PREFERENCE_KEY, isBroadcast);
    }

    public boolean getBroadcastScheduleSwitch() {
        return mSharedPreferences.getBoolean(Constants.BROADCAST_SWITCH_PREFERENCE_KEY, true);
    }

    public void setBroadcastTime(String time) {
        editSharePreferences(Constants.BROADCAST_TIME_PREFERENCE_KEY, time);
    }

    public String getBroadcastTime() {
        return mSharedPreferences.getString(Constants.BROADCAST_TIME_PREFERENCE_KEY, null);
    }

    //add by zhengjl at 2017-1-19 for GNSPR #65237 not end
    public boolean getRemindScheduleSwitch() {
        return mSharedPreferences.getBoolean(Constants.REMIND_SWITCH_PREFERENCE_KEY, true);
    }

    public void setExpressStatusSwitch(boolean isShow) {
        editSharePreferences(Constants.EXPRESS_SWITCH_PREFERENCE_KEY, isShow);
    }

    public void setShowWidget(boolean showWidget) {
        editSharePreferences(Constants.WIDGET_SWITCH_PREFERENCE_KEY, showWidget);
    }

    public void setDefaultTravelMethod(String method) {
        editSharePreferences(Constants.TRAVEL_MODE_PREFERENCE_KEY, method);
    }

    public void saveSelfScheduleTravel(int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("travel_selfschedule", value).apply();
    }

   /* public int getSelfScheduleTravel(){
        return mSharedPreferences.getInt("travel_selfschedule",0);
    }*/

    private void editSharePreferences(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value).apply();
    }

    private void editSharePreferences(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value).apply();
    }

    public void setDefaultRingtoneTitle(String title) {
        editSharePreferences(Constants.NOTIFICATION_RING_PREFERENCE_KEY, title);
    }

    public String getDefaultRingtoneTitle() {
        //通过sharedPreference获取铃声名
        return mSharedPreferences.getString(Constants.NOTIFICATION_RING_PREFERENCE_KEY, null);
    }

    public void setDefaultRingtoneUri(String ringtoneUri) {
        editSharePreferences(Constants.NOTIFICATION_RING_URI_KEY, ringtoneUri);
    }

    public String getDefaultRingtoneUri() {
        return mSharedPreferences.getString(Constants.NOTIFICATION_RING_URI_KEY, null);
    }
}
