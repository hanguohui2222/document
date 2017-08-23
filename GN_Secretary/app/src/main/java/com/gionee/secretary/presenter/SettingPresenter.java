package com.gionee.secretary.presenter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.module.settings.LoginModel;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.ui.viewInterface.ISettingView;

import com.gionee.secretary.ui.activity.SettingActivity;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.WidgetUtils;
import com.gionee.secretary.ui.activity.SettingPasswordActivity;
import com.gionee.secretary.ui.activity.VerifyPassWordActivity;
import com.gionee.secretary.utils.VoiceBroadcastUtil;
import com.youju.statistics.YouJuAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoTimePickerDialog;
import amigoui.preference.AmigoPreference;
import amigoui.widget.AmigoTimePicker;

import com.gionee.secretary.R;

/**
 * Created by liyy on 16-11-30.
 */
public class SettingPresenter {
    private static final String TAG = "secretary";
    public Context mContext;
    private ISettingView mSettingView;
    private PasswordModel mPasswordModel;
    private SettingModel mSettingModel;

    private LoginModel mLoginModel;

    private static final int SET_PASSWORD_AND_LOCK_SWITCH = 0;
    private static final int CLOSE_LOCK_SWITCH = 1;
    private static final int SET_NOTIFICATION_RING = 2;

    private String mCustomRingtone;

    public SettingPresenter(Context context, ISettingView settingView) {
        this.mContext = context;
        this.mSettingView = settingView;
        mSettingModel = SettingModel.getInstance(context);
        mPasswordModel = PasswordModel.getInstance(mContext);

        mLoginModel = new LoginModel(mContext);
    }


//    public void needPassword() {
//        if (PasswordModel.getInstance(mContext).isShowLockScreen()) {
//            Intent intent = new Intent(mContext, VerifyPassWordActivity.class);
//            mContext.startActivity(intent);
//        }
//    }

    public void checkoutPasswordSwitch() {
        boolean lockSwitch = mPasswordModel.getLockSwitch();
        if (lockSwitch) {
            mSettingView.openPasswordSwitch();
        } else {
            mSettingView.closePasswordSwitch();
        }
    }


    public boolean onPreferenceChange(AmigoPreference preference, Object newValue) {
        String preKey = preference.getKey();
        if (preKey.equals(Constants.TRAVEL_MODE_PREFERENCE_KEY)) {

            setTravelMode((String) newValue);
            updateTravelModePreferenceSummery();
            accountTravelMode();
            invalidateMainUI();

        } else if (preKey.equals(Constants.REMIND_SWITCH_PREFERENCE_KEY)) {

            setRemindSwitch((boolean) newValue);
            updateAlarm((boolean) newValue);

        } else if (preKey.equals(Constants.EXPRESS_SWITCH_PREFERENCE_KEY)) {

            setExpressSwitch((boolean) newValue);
            invalidateMainUI();
            accountExpressSwitch((boolean) newValue);

        } else if (preKey.equals(Constants.PASSWORD_SWITCH_PREFERENCE_KEY)) {

            setPassword((boolean) newValue);

        } else if (preKey.equals(Constants.WIDGET_SWITCH_PREFERENCE_KEY)) {
            setWidgetSwitch((boolean) newValue);
            WidgetUtils.isHideWidgetSchedule(mContext, !(boolean) newValue);
            accountWidgetSwitch((boolean) newValue);

        } else if (preKey.equals(Constants.BROADCAST_SWITCH_PREFERENCE_KEY)) {
            setBroadcastSwitch((boolean) newValue);
            updateBroadcast((boolean) newValue);
        } else {
            return false;
        }
        return true;
    }

    public boolean onPreferenceClick(AmigoPreference preference) {
        String preKey = preference.getKey();
        if (preKey.equals((Constants.NOTIFICATION_RING_PREFERENCE_KEY))) {
            setNotification();
        } else {
            return false;
        }
        return true;
    }

    public void setBroadcastTime(final Context context, final SettingActivity.IsetBroadcastTimeListener listener) {
        int hour = 8;
        int minute = 0;
        String broadcastTime = SettingModel.getInstance(context).getBroadcastTime();
        if (broadcastTime != null) {
            hour = Integer.parseInt(broadcastTime.split(":")[0]);
            minute = Integer.parseInt(broadcastTime.split(":")[1]);
        }
        AmigoTimePickerDialog dialog = new AmigoTimePickerDialog(context, new AmigoTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(AmigoTimePicker amigoTimePicker, int hour, int minute) {
                String hourStr = "" + hour;
                String minuteStr = "" + minute;
                if (hour < 10) {
                    hourStr = "0" + hour;
                }
                if (minute < 10) {
                    minuteStr = "0" + minute;
                }
                String time = hourStr + ":" + minuteStr;
                listener.showBroadcastTime(time);
                mSettingModel.setBroadcastTime(time);
                VoiceBroadcastUtil.broadcastAlarmEdit(context);
                youJuAccountBroadcastTime(time);
            }
        }, hour, minute, false);
        dialog.show();
    }

    private void youJuAccountBroadcastTime(String time) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.ACCOUNT_BROADCAST_TIME_YOUJU, time);
        YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_BROADCAST_TIME", map);
    }

    private void setWidgetSwitch(boolean value) {
        mSettingModel.setShowWidget(value);
    }

    private void setPassword(boolean value) {
        if (value) {
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, Constants.YOUJU_PASSWORD);
            Intent intent = new Intent(mContext, SettingPasswordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            ((Activity) mSettingView).startActivityForResult(intent, SET_PASSWORD_AND_LOCK_SWITCH);


        } else {
            //输入密码关闭
            Intent intent = new Intent(mContext, VerifyPassWordActivity.class);
            intent.putExtra(Constants.EXTRA_CLOSE_PASSWORD_SWITCH, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            ((Activity) mSettingView).startActivityForResult(intent, CLOSE_LOCK_SWITCH);
        }
    }

    private void accountWidgetSwitch(boolean newValue) {
        if (!newValue) {
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_CLOSE_SHOWWIDGET");
        }
    }

    private void accountExpressSwitch(boolean newValue) {
        if (!newValue) {
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_CLOSE_EXPRESS");
        }
    }

    private void setTravelMode(String travelMode) {
        mSettingModel.setDefaultTravelMethod(travelMode);
    }

    private void accountTravelMode() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.ACCOUNT_TRAVEL_MODE_YOUJU, getDefaultTravelMode());
        YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_TRAVEL_MODE", map);
    }

    private void invalidateMainUI() {
        //刷新主页面广播
        mContext.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
    }

    private String getDefaultTravelMode() {
        String travelMethod = mSettingModel.getDefaultTravelMethod();
        String[] travelModeArray = mContext.getResources().getStringArray(R.array.travel_method_entry);
        return travelModeArray[Integer.parseInt(travelMethod)];
    }

    private void setRemindSwitch(boolean remindWwitch) {
        mSettingModel.setRemindScheduleSwitch(remindWwitch);
    }

    private void setBroadcastSwitch(boolean broadcastSwitch) {
        mSettingModel.setBroadcastScheduleSwitch(broadcastSwitch);
    }

    public void setNotification() {
        Log.e("zjl", "调用model进行设置 SelectNotificationRingActivity");
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        // Allow the user to pick a silent ringtone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        final Uri ringtoneUri;
        if ((mCustomRingtone = mSettingModel.getDefaultRingtoneUri()) != null) {
            ringtoneUri = Uri.parse(mCustomRingtone);
        } else {
            // Otherwise pick default ringtone Uri so that something is selected.
//            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            /*
            modify by zhengjl at 2017-1-18
            默认铃声定位出错的bug
             */
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                    RingtoneManager.TYPE_NOTIFICATION);
        }

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        //将当前选中的音频名字带过去 intent.putExtra(Constants.EXTRA_CLOSE_PASSWORD_SWITCH, true);
        ((Activity) mSettingView).startActivityForResult(intent, SET_NOTIFICATION_RING);
    }

    public void updateTravelModePreferenceSummery() {
        mSettingView.updateTravelModePreferenceSummery(getDefaultTravelMode());
    }

    private void updateAlarm(boolean remindSwitch) {
        if (remindSwitch) {
            RemindUtils.refreshScheduleAlarm(mContext);
        } else {
            RemindUtils.cancelCurrentScheduleAlarm(mContext);
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_CLOSE_SCHEDULE_REMIND");
        }
    }

    private void updateBroadcast(boolean broadcastSwitch) {
        if (broadcastSwitch) {
            VoiceBroadcastUtil.broadcastAlarmEdit(mContext);
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_OPEN_SCHEDULE_BROADCAST");
        } else {
            VoiceBroadcastUtil.broadcastAlarmCancel(mContext);
            YouJuAgent.onEvent(mContext, Constants.EVENT_ID, "GN_CLOSE_SCHEDULE_BROADCAST");
        }
    }

    private void setExpressSwitch(boolean expressSwitch) {
        mSettingModel.setExpressStatusSwitch(expressSwitch);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SET_PASSWORD_AND_LOCK_SWITCH) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String pwd = data.getStringExtra(Constants.EXTRA_PASSWORD);
                LogUtils.i(TAG, "setting....PWD:" + pwd);
                //modify by zhengjl at 2017-2-13 for GNSPR #67031 not begin
                //modified by luorw for GNSPR #64915 20170118 begin
                //mPasswordModel.updateLockAllStatus(false,true,pwd);//在返回settingActivity之前先保存状态和密码
                //modified by luorw for GNSPR #64915 20170118 end
                //modify by zhengjl at 2017-2-13 for GNSPR #67031 end
                if (!isNoSwitch()) {
                    RequestPermission();
                }
                mLoginModel.amigoAccountLogin();
                mLoginModel.setAmigoAccountLoginLinster(new LoginModel.AmigoAccountLoginLinster() {
                    @Override
                    public void loginOK() {

                    }

                    @Override
                    public void loginNo() {
                        loginAmigoAccountDialog();

                    }
                });

            } else {
                LogUtils.i(TAG, "settingPWD....error");
                mSettingView.closePasswordSwitch();
                mSettingView.showToast(R.string.fail_pw);
            }

        } else if (requestCode == CLOSE_LOCK_SWITCH) {

            if (resultCode == Activity.RESULT_OK) {
                LogUtils.i(TAG, "closePWD....LockSwitch...false");
                mPasswordModel.updateLockSwitch(false);
                mPasswordModel.updateLockState(false);
                mSettingView.closePasswordSwitch();
                mSettingView.showToast(R.string.close_pw);
            } else if (resultCode == -2) {
                LogUtils.i(TAG, "closePWD....-2");
                String pwd = data.getStringExtra(Constants.EXTRA_PASSWORD);
                LogUtils.i(TAG, "setting....PWD....-2:" + pwd);
                //modified by luorw for GNSPR #64915 20170118 begin
                mPasswordModel.updateLockAllStatus(false, false, pwd);
                //modified by luorw for GNSPR #64915 20170118 end
                if (!isNoSwitch()) {
                    RequestPermission();
                }
                mSettingView.closePasswordSwitch();
            } else {
                LogUtils.i(TAG, "closePWD....error");
                mSettingView.openPasswordSwitch();
                //modified by luorw for 60611 20170207 begin
                if (data == null || !data.getBooleanExtra("isClosePw", false)) {
                    mSettingView.showToast(R.string.close_pw_error);
                }
                //modified by luorw for 60611 20170207 end
            }
        } else if (requestCode == SET_NOTIFICATION_RING) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                YouJuAgent.onEvent(mContext, mContext.getResources().getString(R.string.click_change_ring));
                final Uri pickedUri = data.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                String ringtoneTitle = getRingtoneTitleFromUri(pickedUri);
                mSettingModel.setDefaultRingtoneTitle(ringtoneTitle);
                mSettingModel.setDefaultRingtoneUri(pickedUri.toString());

                mSettingView.updateNotifyRingPreferenceSummery(ringtoneTitle);
            } else {
//                mSettingView.showToast(R.string.fail_set_ringtone);
            }
        }
    }

    public boolean isFileDeleted() {
        String ringtoneUri = mSettingModel.getDefaultRingtoneUri();
        Uri uri = Uri.parse(ringtoneUri);
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor actualimagecursor = mContext.getContentResolver().query(uri, proj, null, null, null);
        return actualimagecursor.getCount() == 0;
    }

    /*
    通过uri获取铃声title
     */
    public String getRingtoneTitleFromUri(Uri uri) {
        if (uri == null) return null;
        String fileTitle = null;
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[]{MediaStore.MediaColumns.TITLE,}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fileTitle = cursor.getString(0);
            }
        }
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        //LogUtils.e("zzz", fileTitle);
        return fileTitle;
    }

    public void RequestPermission() {
        new AmigoAlertDialog.Builder(mContext).
                setTitle(R.string.settings).
                setMessage(R.string.setting_password_validate_tips)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        mContext.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtils.i(TAG, "closePWD....LockSwitch...false");
                        mPasswordModel.updateLockSwitch(false);
                        mSettingView.closePasswordSwitch();
                        mSettingView.showToast(R.string.close_pw);
                    }
                })
                .show();
    }


    public void loginAmigoAccountDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(mContext);
        builder.setTitle(R.string.settings);
        builder.setMessage(R.string.set_password_login_amigo_account_tip);
        builder.setPositiveButton(R.string.bind_amigo_account, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mLoginModel != null) {
                    LogUtils.e(TAG, "zhengyt-----onGetBack  -----");
                    mLoginModel.login();
                }
            }
        });
        builder.setNegativeButton(R.string.never_forget_password, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isNoSwitch() {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        List queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
        return !(queryUsageStats == null || queryUsageStats.isEmpty());
    }


}
