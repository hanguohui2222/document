package com.gionee.secretary.utils;

import android.util.Log;

/**
 *
 */
public class LogUtils {
    public static final int level = 0;
    private static final String TAG = "secretary";

    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARNNING = 3;
    public static final int ERROR = 4;


    public static void v(String tag, String message) {
        if (level <= VERBOSE) {
            Log.v(TAG, tag + " , " + message);
        }
    }

    public static void d(String tag, String message) {
        if (level <= DEBUG) {
            Log.d(TAG, tag + " , " + message);
        }
    }

    public static void i(String tag, String message) {
        if (level <= INFO) {
            Log.i(TAG, tag + " , " + message);
        }
    }

    public static void w(String tag, String message) {
        if (level <= WARNNING) {
            Log.w(TAG, tag + " , " + message);
        }
    }

    public static void e(String tag, String message) {
        if (level <= ERROR) {
            Log.e(TAG, tag + " , " + message);
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (level <= ERROR) {
            Log.e(TAG, tag + " , " + message, e);
        }
    }
}
