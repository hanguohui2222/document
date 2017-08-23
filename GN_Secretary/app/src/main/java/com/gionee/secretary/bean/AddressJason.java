package com.gionee.secretary.bean;

/**
 * 查询天气需要提供的城市和省
 * Created by luorw on 1/14/17.
 */
public class AddressJason {
    private String cityName;
    private String provinceName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
