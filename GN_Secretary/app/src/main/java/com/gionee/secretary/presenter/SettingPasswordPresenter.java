package com.gionee.secretary.presenter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.viewInterface.IPasswordVerifyView;
import com.gionee.secretary.ui.viewInterface.ISettingPasswordView;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyy on 16-12-3.
 */
public class SettingPasswordPresenter {
    private static final String TAG = "SettingPasswordPresenter";
    private Context mContext;
    private ISettingPasswordView mSettingPasswordView;

    private IPasswordVerifyView mPasswordVerifyView;

    /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
    private PasswordModel mPasswordModel;

    public SettingPasswordPresenter(Context context, ISettingPasswordView settingPasswordView) {
        this.mContext = context;
        this.mSettingPasswordView = settingPasswordView;
        //modify by zhengjl at 2017-2-13 for GNSPR #67031 not end
        mPasswordModel = PasswordModel.getInstance(context);
    }

    public SettingPasswordPresenter(Context context, IPasswordVerifyView passwordVerifyView) {
        this.mContext = context;
        this.mPasswordVerifyView = passwordVerifyView;
        /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
        mPasswordModel = PasswordModel.getInstance(context);
    }

    public void SettingPassword(Editable s, final String secondPass, String first_pass) {
        if (s.length() == Constants.PASSWORD_LENGTH) {
            if (secondPass.equals(first_pass)) {
                //modify by zhengjl at 2017-2-13 for GNSPR #67031 begin
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPasswordModel.updateLockAllStatus(false, true, secondPass);
                    }
                }).start();
                //modify by zhengjl at 2017-2-13 for GNSPR #67031 not end
                Intent intent = new Intent();
                LogUtils.i(TAG, "goto....SettingPassword:" + secondPass);
                intent.putExtra(Constants.EXTRA_PASSWORD, secondPass);
                ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                ((Activity) mContext).finish();
            } else {
                mSettingPasswordView.inputAgain();
            }
        }
    }

    //added by luorw for  GNSPR #69388 20170227 begin
    private void cancelFreezePwdAlam() {
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        Intent intent = new Intent(Constants.FREEZING_PASSWORD_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, Constants.FREEZING_PASSWORD_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(pi);
    }

    private boolean isFreezePwd() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFreezePwd = sharedPreferences.getBoolean(Constants.FREEZING_PASSWORD_STATE, false);
        return isFreezePwd;
    }

    //added by luorw for  GNSPR #69388 20170227 end
    public void SettingPassword(Editable s, String secondPass, String first_pass, boolean isReset, int isFromStart) {
        LogUtils.e(TAG, "zhengyt-----SettingPassword  -----isreset---");
        if (s.length() == Constants.PASSWORD_LENGTH && isReset) {
            if (secondPass.equals(first_pass)) {
                LogUtils.e(TAG, "zhengyt-----isreset---psw = " + secondPass);
                //added by luorw for GNSPR #69388 20170227 begin
                if (isFreezePwd()) {
                    cancelFreezePwdAlam();
                    mPasswordModel.updateFreezePwdState(false);
                }
                //added by luorw for GNSPR #69388 20170227 end
                Intent intent = new Intent();
                LogUtils.i(TAG, "goto....SettingPassword:" + secondPass);
                intent.putExtra(Constants.EXTRA_PASSWORD, secondPass);
                if (isFromStart != 1) {
                    ((Activity) mContext).setResult(-2, intent);
                } else {
                    /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
                    mPasswordModel.updateLockState(false);
                    mPasswordModel.updatePassword(secondPass);
                }
                ((Activity) mContext).finish();
            } else {
//                mSettingPasswordView.inputAgain();
                mPasswordVerifyView.inputAgain();
                /*modify by zhengjl at 2017-2-9 for GNSPR #66730 end*/
            }
        }
    }
}
