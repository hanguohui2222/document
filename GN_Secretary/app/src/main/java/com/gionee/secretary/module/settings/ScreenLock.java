package com.gionee.secretary.module.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.secretary.constants.Constants;

/**
 * Created by liyy on 16-11-24.
 */
public class ScreenLock {
    private boolean mLockSwitch;
    private boolean mLockState;
    private boolean mFreezePwdState;
    private static ScreenLock mScreenLock;
    private SharedPreferences mSharedPreferences;
    //modified by lurow for GNSPR #64915 20170118 begin
    private ScreenLock(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static ScreenLock getInstance(Context context) {
        synchronized (ScreenLock.class) {
            if (mScreenLock == null) {
                synchronized (ScreenLock.class){
                    if (mScreenLock == null) {
                        mScreenLock = new ScreenLock(context.getApplicationContext());
                    }
                }
            }
        }
        return mScreenLock;
    }

    //modified by lurow for GNSPR #64915 20170118 end
    public String getPassword() {
        if(mSharedPreferences != null){
            String password = mSharedPreferences.getString(Constants.EXTRA_PASSWORD,"");
            if("".equals(password)){
                password = mSharedPreferences.getString("lock_password","");
            }
            return password;
        }
        return "";
    }

    public void setPassword(String password) {
        android.util.Log.d("ScreenLock", "------setPassowrd ---------- = " + password);
        if(mSharedPreferences != null && !TextUtils.isEmpty(password)){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.EXTRA_PASSWORD,password);
            editor.commit();
        }
    }

    public boolean isLockSwitch() {
        return mLockSwitch;
    }

    public void setLockSwitch(boolean lockSwitch) {
        this.mLockSwitch = lockSwitch;
    }

    public boolean isLockState() {
        return mLockState;
    }

    public void setLockState(boolean lockState) {
        this.mLockState = lockState;
    }

    //added by luorw for  GNSPR #67676 20170220 begin
    public boolean isFreezePwdState() {
        return mFreezePwdState;
    }

    public void setFreezePwdState(boolean freezePwdState) {
        this.mFreezePwdState = freezePwdState;
    }
    //added by luorw for  GNSPR #67676 20170220 end
}
