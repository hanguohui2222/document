package com.gionee.secretary.ui.viewInterface;

import com.gionee.secretary.bean.WeatherSchedule;

/**
 * Created by luorw on 17-1-13.
 */
public interface ISetWeatherListener {
    void showDepartureWeather(WeatherSchedule Weather);

    void showDestinationWeather(WeatherSchedule Weather);
}
