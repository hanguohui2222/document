package com.gionee.secretary.bean;

import java.io.Serializable;

/**
 * Created by rongdd on 16-5-11.
 */
public class AddressBean implements Serializable {
    private String name;
    private double mLongitude;
    private double mLatitude;
    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
