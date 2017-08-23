package com.gionee.secretary.bean;

/**
 * 天气日程
 * Created by zhuboqin on 11/05/16.
 */
public class WeatherSchedule extends BaseSchedule {

    private String address;
    private String weather;
    private String temp;//温度
    private String dressing;//穿衣指数
    private String umbrella;//是否需要携带雨伞出门


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getDressing() {
        return dressing;
    }

    public void setDressing(String dressing) {
        this.dressing = dressing;
    }

    public String getUmbrella() {
        return umbrella;
    }

    public void setUmbrella(String umbrella) {
        this.umbrella = umbrella;
    }
}
