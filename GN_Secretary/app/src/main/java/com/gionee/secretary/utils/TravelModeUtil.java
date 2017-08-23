package com.gionee.secretary.utils;

import android.content.Context;

import com.gionee.secretary.R;
import com.gionee.secretary.module.settings.SettingModel;

/**
 * Created by liyy on 16-6-18.
 */
public class TravelModeUtil {

    public static int none = 0;//无
    public static int walk = 1;//步行
    public static int drive = 2;//开车
    public static int taxi = 3;//打车
    public static int traffic = 4;//公共交通

    public static boolean isNeedNavigate(Context context) {
        int travelType = Integer.parseInt(SettingModel.getInstance(context).getDefaultTravelMethod());
        if (travelType == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isMapSupportMode(Context context) {
        int travelType = Integer.parseInt(SettingModel.getInstance(context).getDefaultTravelMethod());
        if (travelType == walk || travelType == traffic || travelType == drive) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMapSupportMode(Context context, String tripMode) {
        if ("步行".equals(tripMode) || "公共交通".equals(tripMode) || "开车".equals(tripMode)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getTravelType(int index) {
        String travel = "无";
        switch (index) {
            case 0:
                travel = "无";
                break;
            case 1:
                travel = "步行";
                break;
            case 2:
                travel = "开车";
                break;
            case 3:
                travel = "出租车";
                break;
            case 4:
                travel = "公共交通";
                break;
        }
        return travel;
    }
}
