package com.gionee.secretary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;

import java.util.Date;

/**
 * 监听闹钟关闭的广播，上午5点到10点之间视为起床闹钟
 * Created by luorw on 3/27/17.
 */
public class DeskClockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        LogUtils.i("DeskClockReceiver","action = "+action);
//        if (Constants.DESK_CLOCK_FULL_SCREEN_DISMISS_ACTION.equals(action) || Constants.DESK_CLOCK_HEAD_UP_DISMISS_ACTION.equals(action)) {
//            Date date = new Date();
//            int hour = date.getHours();
//            if (hour >= 5 && hour <= 10) {
//                Intent intent1 = new Intent();
//                intent1.setClass(context, VoiceBroadcastService.class);
//                context.startService(intent1);
//            }
//        }
    }
}
