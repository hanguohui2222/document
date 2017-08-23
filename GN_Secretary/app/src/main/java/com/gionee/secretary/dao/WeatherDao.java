package com.gionee.secretary.dao;

import android.content.Context;
import android.database.Cursor;

//import com.gionee.amiweather.library.QueryConstant;
//import com.gionee.amiweather.library.WeatherData;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.constants.Constants;

import java.util.Date;

/**
 * Created by rongdd on 16-5-13.
 */
public class WeatherDao {

    private Context mContext;

    public WeatherDao(Context context) {
        mContext = context;
    }

    public WeatherSchedule getWeatherSchedule() {
        WeatherSchedule weatherSchedule = new WeatherSchedule();
        /*WeatherData weatherData = queryWeatherInfo();
        if (null != weatherData) {
            weatherSchedule.setDate(new Date());
            weatherSchedule.setType(Constants.WEATHER_TYPE);
            weatherSchedule.setAllDay(true);
            weatherSchedule.setAddress(weatherData.getCityName());
            weatherSchedule.setWeather(weatherData.getForecastState());
            weatherSchedule.setTemp(weatherData.getForecastTemperature() + weatherData.getTemperatureUnit());
            weatherSchedule.setDressing(weatherData.getDressing() + ", ");
            weatherSchedule.setUmbrella(weatherData.shouldTakeUmbrella() + "带雨伞");
        } else {
            weatherSchedule.setDate(new Date());
            weatherSchedule.setType(Constants.WEATHER_TYPE);
            weatherSchedule.setAllDay(true);
            weatherSchedule.setAddress("无数据");
            weatherSchedule.setWeather("N/A");
            weatherSchedule.setTemp("无数据");
            weatherSchedule.setDressing("无数据,");
            weatherSchedule.setUmbrella("无数据");
        }*/
        return weatherSchedule;
    }

    /*private WeatherData queryWeatherInfo() {
        WeatherData situation = null;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(QueryConstant.QUERY_WEATHER_CASE_LANGUAGE, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                situation = WeatherData.obtain(cursor);
                cursor.close();
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return situation;
    }*/
}
