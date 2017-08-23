package com.gionee.secretary.module.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyy on 16-11-24.
 */
public class PasswordModel {
    private static String TAG = "PasswordModel";
    private static PasswordModel mPasswordModel;
    private Context mContext;

    private PasswordModel(Context context) {
        mContext = context;
    }

    public static PasswordModel getInstance(Context context) {
        synchronized (PasswordModel.class) {
            if (mPasswordModel == null) {
                synchronized (PasswordModel.class) {
                    if (mPasswordModel == null) {
                        mPasswordModel = new PasswordModel(context.getApplicationContext());
                    }
                }
            }
        }
        return mPasswordModel;
    }

    //modified by lurow for GNSPR #64915 20170118 begin
    public boolean getLockSwitch() {
        LogUtils.i(TAG, "getLockSwitch:" + ScreenLock.getInstance(mContext).isLockSwitch());
        return ScreenLock.getInstance(mContext).isLockSwitch();
    }

    public String getPassword() {
        LogUtils.i(TAG, "getPassword:" + ScreenLock.getInstance(mContext).getPassword());
        return ScreenLock.getInstance(mContext).getPassword();
    }


    public boolean getLockState() {
        LogUtils.i(TAG, "getLockState:" + ScreenLock.getInstance(mContext).isLockState());
        return ScreenLock.getInstance(mContext).isLockState();
    }

    public void updateLockState(boolean lockState) {
        LogUtils.i(TAG, "updateLockState:" + lockState);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        ScreenLock.getInstance(mContext).setLockState(lockState);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.LOCK_STATE, lockState).commit();
    }

    //added by luorw for  GNSPR #67676 20170220 begin
    public boolean getFreezePwdState() {
        LogUtils.i(TAG, "getFreezePwdState:" + ScreenLock.getInstance(mContext).isFreezePwdState());
        return ScreenLock.getInstance(mContext).isFreezePwdState();
    }

    public void updateFreezePwdState(boolean freezePwdState) {
        LogUtils.i(TAG, "updateFreezePwdState:" + freezePwdState);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        ScreenLock.getInstance(mContext).setFreezePwdState(freezePwdState);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.FREEZING_PASSWORD_STATE, freezePwdState).commit();
    }

    //added by luorw for  GNSPR #67676 20170220 end
    public void updateLockSwitch(boolean lockSwitch) {
        LogUtils.i(TAG, "updateLockSwitch:" + lockSwitch);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        ScreenLock.getInstance(mContext).setLockSwitch(lockSwitch);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.LOCK_SWITCH, lockSwitch).commit();
    }

    public void updatePassword(String password) {
        LogUtils.i(TAG, "updatePassword:" + password);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        ScreenLock.getInstance(mContext).setPassword(password);
    }

    /**
     * added by luorw for GNSPR #64915 20170118
     *
     * @param lockState
     * @param lockSwitch
     * @param password
     */
    public void updateLockAllStatus(boolean lockState,boolean lockSwitch,String password){
        LogUtils.i(TAG, "updateLockAllStatus: lockState = " + lockState + " , lockSwitch = "+lockSwitch);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        ScreenLock.getInstance(mContext).setLockState(lockState);
        ScreenLock.getInstance(mContext).setLockSwitch(lockSwitch);
        ScreenLock.getInstance(mContext).setPassword(password);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.LOCK_SWITCH, lockSwitch)
                .putBoolean(Constants.LOCK_STATE, lockState).commit();
    }
    //modified by lurow for GNSPR #64915 20170118 end
    //modified by luorw for GNSPR #79347 2017-04-13 begin

    /**
     * 此方法只能用于baseActivity判断是否需要弹出密码界面
     *
     * @return
     */
    public boolean isShowLockScreen() {
        //直接获取锁的状态值会影响ami换机数据获取，改成直接从sharepreference读取
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        boolean lock = mSharedPreferences.getBoolean(Constants.LOCK_SWITCH, false);
        ScreenLock.getInstance(mContext).setLockSwitch(lock);
        boolean lockState = mSharedPreferences.getBoolean(Constants.LOCK_STATE, false);
        ScreenLock.getInstance(mContext).setLockState(lockState);
        String password = mSharedPreferences.getString(Constants.EXTRA_PASSWORD, "");
        ScreenLock.getInstance(mContext).setPassword(password);
        return lock && lockState;
    }

    //modified by luorw for GNSPR #79347 2017-04-13 end
    private boolean mIsFromAmigoAccount = false;

    public void setInAmigoLog(boolean isInAmigo) {
        mIsFromAmigoAccount = isInAmigo;
    }

    public boolean getIsFromAmigAccount() {
        return mIsFromAmigoAccount;
    }
}
