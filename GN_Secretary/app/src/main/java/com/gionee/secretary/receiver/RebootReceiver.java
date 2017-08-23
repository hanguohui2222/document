package com.gionee.secretary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gionee.secretary.module.settings.PasswordModel;

/**
 * Created by hangh on 7/4/17.
 */

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction()) || Intent.ACTION_REBOOT.equals(intent.getAction()) ) {
            PasswordModel model = PasswordModel.getInstance(context);
            model.updateFreezePwdState(false);
        }
    }
}
