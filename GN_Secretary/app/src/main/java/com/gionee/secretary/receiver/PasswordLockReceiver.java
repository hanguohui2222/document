package com.gionee.secretary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyy on 16-12-2.
 */
public class PasswordLockReceiver extends BroadcastReceiver {
    private String TAG = "PasswordLockReceiver";
    final String SYS_KEY = "reason";
    final String SYS_HOME_KEY = "homekey";
    final String SYS_MENU_KEY = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.e(TAG, action);
        boolean switchLock = PasswordModel.getInstance(context).getLockSwitch();
        if (!switchLock) {
            return;
        }
        if (action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_SCREEN_ON)) {
            LogUtils.i(TAG, "setPWD....LockState....true");
            PasswordModel.getInstance(context).updateLockState(true);
        } else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYS_KEY);
            LogUtils.e(TAG, reason);
            if (reason != null && (reason.equals(SYS_HOME_KEY) || reason.equals(SYS_MENU_KEY))) {
                LogUtils.i(TAG, "setPWD....LockState....true");
                PasswordModel.getInstance(context).updateLockState(true);
            }
        }
    }
}
