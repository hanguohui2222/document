package com.gionee.secretary.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteRailwayItem;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.gionee.secretary.R;

public class AMapUtil {

    private static final String TAG = "AMapUtil";

    public static final String Kilometer = "\u516c\u91cc";// "公里";
    public static final String Meter = "\u7c73";// "米";

    public static String getFriendlyLength(int lenMeter) {
        if (lenMeter > 10000) // 10 km
        {
            int dis = lenMeter / 1000;
            return dis + Kilometer;
        }

        if (lenMeter > 1000) {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + Kilometer;
        }

        if (lenMeter > 100) {
            int dis = lenMeter / 50 * 50;
            return dis + Meter;
        }

        int dis = lenMeter / 10 * 10;
        if (dis == 0) {
            dis = 10;
        }

        return dis + Meter;
    }

    public static String getFriendlyTime(int second) {
        if (second > 3600) {
            int hour = second / 3600;
            int miniate = (second % 3600) / 60;
            return hour + "时" + miniate + "分";
        }
        if (second >= 60) {
            int miniate = second / 60;
            return miniate + "分钟";
        }
        return second + "秒";
    }

    public static String getBusPathTitle(BusPath busPath) {
        if (busPath == null) {
            return String.valueOf("");
        }
        List<BusStep> busSteps = busPath.getSteps();
        if (busSteps == null) {
            return String.valueOf("");
        }
        StringBuffer sb = new StringBuffer();
        for (BusStep busStep : busSteps) {
            StringBuffer title = new StringBuffer();
            if (busStep.getBusLines().size() > 0) {
                for (RouteBusLineItem busline : busStep.getBusLines()) {
                    if (busline == null) {
                        continue;
                    }

                    String buslineName = getSimpleBusLineName(busline.getBusLineName());
                    title.append(buslineName);
                    title.append(" / ");
                }
//					RouteBusLineItem busline = busStep.getBusLines().get(0);

                sb.append(title.substring(0, title.length() - 3));
                sb.append(" > ");
            }
            if (busStep.getRailway() != null) {
                RouteRailwayItem railway = busStep.getRailway();
                sb.append(railway.getTrip() + "(" + railway.getDeparturestop().getName()
                        + " - " + railway.getArrivalstop().getName() + ")");
                sb.append(" > ");
            }
        }
        return sb.substring(0, sb.length() - 3);
    }

    public static String getBusPathDes(BusPath busPath) {
        if (busPath == null) {
            return String.valueOf("");
        }
        long second = busPath.getDuration();
        String time = getFriendlyTime((int) second);
        float subDistance = busPath.getDistance();
        String subDis = getFriendlyLength((int) subDistance);
        float walkDistance = busPath.getWalkDistance();
        String walkDis = getFriendlyLength((int) walkDistance);
        return String.valueOf(time + "  -  " + subDis + "  -  步行" + walkDis);
    }

    public static String getSimpleBusLineName(String busLineName) {
        if (busLineName == null) {
            return String.valueOf("");
        }
        return busLineName.replaceAll("\\(.*?\\)", "");
    }

    public static SpannableStringBuilder getSpanBusPathTitle(Context context, String busPathTitle) {
        SpannableStringBuilder spanString = setSpan(context, busPathTitle, ">", R.drawable.map_transfer_arrow);
        return spanString;
    }

    public static SpannableStringBuilder getSpanBusPathDesc(Context context, String busPathDesc) {
        SpannableStringBuilder spanString = setSpan(context, busPathDesc, "-", R.drawable.map_dot_square);
        return spanString;
    }

    private static SpannableStringBuilder setSpan(Context context, String matcher, String compile, int imgRes) {
        SpannableStringBuilder builder = new SpannableStringBuilder(matcher);
        try {
            Pattern p = Pattern.compile(compile);
            Matcher m = p.matcher(matcher);
            CharacterStyle span;

            while (m.find()) {
                span = new ImageSpan(context, imgRes, DynamicDrawableSpan.ALIGN_BASELINE);
                builder.setSpan(span, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (PatternSyntaxException e) {
            LogUtils.e(TAG, "setSpan(): Failed to match pattern. ", e);
        }
        return builder;
    }
}
