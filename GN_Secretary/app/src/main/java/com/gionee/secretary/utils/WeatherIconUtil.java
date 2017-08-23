package com.gionee.secretary.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.R;

import amigoui.widget.AmigoTextView;

/**
 * Created by zhuboqin on 15/06/16.
 */
public class WeatherIconUtil {
    public static void setWeather(WeatherSchedule weatherSchedule, ImageView iv_weather_icon, AmigoTextView tv_weather) {
        String weather = weatherSchedule.getWeather();
        if (weather != null && !isEmpty(weather.trim()) && !weather.equals("N/A")) {
            iv_weather_icon.setVisibility(View.VISIBLE);
            if (weather.contains("晴")) {
                setWeatherIcon(iv_weather_icon, R.drawable.sunny);
            }
            if (weather.contains("阴")) {
                setWeatherIcon(iv_weather_icon, R.drawable.cloudy);
            }
            if (weather.contains("阴") && weather.contains("多云")) {
                setWeatherIcon(iv_weather_icon, R.drawable.sunny_cloudy);
            }
            if (weather.contains("多云")) {
                setWeatherIcon(iv_weather_icon, R.drawable.sunny_cloudy);
            }
            if (weather.contains("雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.rain);
            }
            if (weather.contains("小雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.rain_small);
            }
            if (weather.contains("大雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.rain_big);
            }
            if (weather.contains("中雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.rain_middle);
            }
            if (weather.contains("冻雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.freezing_rain);
            }
            if (weather.contains("大暴雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.heavy_rain);
            }
            if (weather.contains("特大暴雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.heavy_rain_so_much);
            }
            if (weather.contains("阵雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.shower);
            }
            if (weather.contains("雷阵雨")) {
                setWeatherIcon(iv_weather_icon, R.drawable.thunder_shower);
            }
            if (weather.contains("小雪")) {
                setWeatherIcon(iv_weather_icon, R.drawable.snow);
            }
            if (weather.contains("中雪")) {
                setWeatherIcon(iv_weather_icon, R.drawable.snow_middle);
            }
            if (weather.contains("大雪")) {
                setWeatherIcon(iv_weather_icon, R.drawable.snow_big);
            }
            if (weather.contains("暴雪")) {
                setWeatherIcon(iv_weather_icon, R.drawable.blizzard);
            }
            if (weather.contains("雨夹雪")) {
                setWeatherIcon(iv_weather_icon, R.drawable.snow_rain);
            }
            if (weather.contains("沙尘暴") || weather.contains("浮尘")) {
                setWeatherIcon(iv_weather_icon, R.drawable.sand_storm);
            }
            if (weather.contains("霜冻")) {
                setWeatherIcon(iv_weather_icon, R.drawable.frost);
            }
            if (weather.contains("雾") || weather.contains("霾")) {
                setWeatherIcon(iv_weather_icon, R.drawable.fog);
            }
            if (weather.contains("冰雹")) {
                setWeatherIcon(iv_weather_icon, R.drawable.hail);
            }
            tv_weather.setText(weather);
        } else {
            iv_weather_icon.setImageResource(R.drawable.unknown);
            tv_weather.setText(R.string.null_value);
        }
    }

    private static void setWeatherIcon(ImageView iv, int resId) {
        iv.setImageResource(resId);
    }


    public static boolean isEmpty(String str) {
        if (!TextUtils.isEmpty(str) && !str.equals("无数据") && !str.contains("null")) {
            return false;
        } else {
            return true;
        }
    }

}
