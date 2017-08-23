package com.gionee.secretary.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.gionee.secretary.constants.Constants;

/**
 * Created by luorw on 12/17/16.
 */
public class TipsPointSP {

    public static void setTipsShow(Context context, boolean isShow, int page) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getTipsSpTag(page), isShow);
        editor.commit();
    }

    public static void setTipsPointShow(Context context, boolean isShow, int page) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getTipsPointSpTag(page), isShow);
        editor.commit();
    }

    public static void setLandscapeGestureShow(Context context, boolean isShow) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHOW_HOME_PAGE_GESTURE_TIPS_LANDSCAPE, isShow);
        editor.commit();
    }

    public static void setVerticallyGestureShow(Context context, boolean isShow) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHOW_HOME_PAGE_GESTURE_TIPS_VERTICALLY, isShow);
        editor.commit();
    }

    private static String getTipsSpTag(int page) {
        String tag = "";
        switch (page) {
            case Constants.TIPS_FOR_HOME_PAGE:
                tag = Constants.SHOW_HOME_PAGE_TIPS;
                break;
            case Constants.TIPS_FOR_NEW_SCHEDULE:
                tag = Constants.SHOW_NEW_SCHEDULE_TIPS;
                break;
            case Constants.TIPS_FOR_NEW_NOTE:
                tag = Constants.SHOW_NEW_NOTE_TIPS;
                break;
        }
        return tag;
    }

    private static String getTipsPointSpTag(int page) {
        String tag = "";
        switch (page) {
            case Constants.TIPS_FOR_HOME_PAGE:
                tag = Constants.SHOW_HOME_PAGE_TIPS_POINT;
                break;
            case Constants.TIPS_FOR_NEW_SCHEDULE:
                tag = Constants.SHOW_NEW_SCHEDULE_TIPS_POINT;
                break;
            case Constants.TIPS_FOR_NEW_NOTE:
                tag = Constants.SHOW_NEW_NOTE_TIPS_POINT;
                break;
        }
        return tag;
    }

    public static boolean isVerticallyGestureShow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SHOW_HOME_PAGE_GESTURE_TIPS_VERTICALLY, false);
    }

    public static boolean isLandscapeGestureShow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SHOW_HOME_PAGE_GESTURE_TIPS_LANDSCAPE, false);
    }

    public static boolean isTipsPointShow(Context context, int page) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(getTipsPointSpTag(page), false);
    }

    public static boolean isTipsShow(Context context, int page) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIPS_POINT_SP, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(getTipsSpTag(page), false);
    }
}
