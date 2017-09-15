package com.gionee.hotspottransmission.utils;

import android.util.Log;
/**
 * Created by luorw on 17/5/9
 */
public class LogUtil {
    public static String FILTER_NAME = "HotspotTransmission";
    public static int VERBOSE_LEVEL = 4;
    public static int DEBUG_LEVEL = 3;
    public static int INFO_LEVEL = 2;
    public static int WARN_LEVEL = 1;
    public static int ERROR_LEVEL = 0;
    public static int DEFAULT_LEVEL = 5;//打印INFO_LEVEL的log


    public static void v(String msg) {
        if (DEFAULT_LEVEL > VERBOSE_LEVEL) {
            Log.i(FILTER_NAME, msg);
        }
    }
    public static void d(String msg) {
        if (DEFAULT_LEVEL > DEBUG_LEVEL) {
            Log.d(FILTER_NAME, msg);
        }
    }
    public static void i(String msg) {
        if (DEFAULT_LEVEL > INFO_LEVEL) {
            Log.i(FILTER_NAME, msg);
        }
    }
    public static void w(String msg) {
        if (DEFAULT_LEVEL > WARN_LEVEL) {
            Log.w(FILTER_NAME, msg);
        }
    }
    public static void e(String msg) {
        if (DEFAULT_LEVEL > ERROR_LEVEL) {
            Log.e(FILTER_NAME, msg);
        }
    }
}
