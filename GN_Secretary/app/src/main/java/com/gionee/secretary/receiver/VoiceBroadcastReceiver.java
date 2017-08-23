package com.gionee.secretary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.service.VoiceBroadcastService;
import com.gionee.secretary.utils.AutoRefreshTask;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.VoiceBroadcastUtil;

/**
 * Created by luorw on 3/29/17.
 */
public class VoiceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "VoiceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "onReceive: action=" + action + " " + intent.toString());
        if (Constants.START_BROADCAST_TASK_ACTION.equals(action)) {
            if (!VoiceBroadcastUtil.getVoiceBroadcastEnable(context)) return;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setClass(context, VoiceBroadcastService.class);
            context.startService(broadcastIntent);
        } else if (Constants.REFRESH_BROADCAST_TASK_ACTION.equals(action)) {
            VoiceBroadcastUtil.executeBroadcastTask(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //将每天0点刷新的alarm重新建
            AutoRefreshTask.executeRefreshDailyBroadcastTask(context);
            //重启之后，如果设置的播报时间比现在的时间早则不播报，否则建立播报alarm
            if (VoiceBroadcastUtil.getBroadcastTime(context) >= System.currentTimeMillis()) {
                VoiceBroadcastUtil.executeBroadcastTask(context);
            }
        }
    }
}
