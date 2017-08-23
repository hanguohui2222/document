package com.gionee.secretary.ui.viewInterface;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.WeatherSchedule;

import java.util.List;

/**
 * Created by luorw on 4/27/16.
 */
public interface IMainView {
    void deleteCard(BaseSchedule schedule);

    void showCardDetail(BaseSchedule schedule);

    void loadNextPageCards(List<BaseSchedule> schedules);

    void loadWeather(WeatherSchedule weatherSchedule);

    void refreshCards(List<BaseSchedule> schedules);

    void updateExpressData(List<ExpressSchedule> expressSchedules);
}
