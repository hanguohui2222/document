package com.gionee.secretary.utils;

import android.content.Context;
import android.content.Intent;

public class WidgetUtils {

    public static void updateWidget(Context context) {
        context.sendBroadcast(new Intent("com.gionee.secretary.UPDATEDATA"));
    }

    public static void isHideWidgetSchedule(Context context, boolean hide) {
        Intent i = new Intent("com.gionee.secretary.HIDESCHEDLE");
        i.putExtra("hide", hide);
        context.sendBroadcast(i);
    }
}
