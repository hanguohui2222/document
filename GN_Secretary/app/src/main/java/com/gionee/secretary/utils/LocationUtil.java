package com.gionee.secretary.utils;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.gionee.secretary.bean.AddressJason;

/**
 * 高德定位
 * Created by rongdd.
 */
public class LocationUtil {
    private Context context;
    // 声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    // 声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    //     声明定位回调监听器
    //    public AMapLocationListener mLocationListener;
    // 是否开启一次定位
    private boolean mEnableOnceLocation = true;
    // 位置更新时间间隔
    private long mUpdateInterval = 2000;

    public LocationUtil(Context context) {
        this.context = context;
    }

    /**
     * 设置使用单次定位功能。若单次定位打开，查询位置时会造成UI线程阻塞问题。
     *
     * @param b 是否使用单次定位
     */
    public void setEnableOnceLocation(boolean b) {
        mEnableOnceLocation = b;
    }

    /**
     * 设置位置更新时间间隔
     *
     * @param interval 位置更新时间间隔
     */
    public void setLocationInterval(long interval) {
        mUpdateInterval = interval;
    }

    public void startLocation(AMapLocationListener mLocationListener) {
        // 初始化定位
        mLocationClient = new AMapLocationClient(context);
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(mEnableOnceLocation);
        // 设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(mUpdateInterval);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    public AddressJason setAddressJason(String cityName, String provinceName) {
        AddressJason addressJason = new AddressJason();
        /*if (provinceName.contains("黑龙江") || provinceName.contains("内蒙古")) {
            provinceName = provinceName.substring(0, 3);
        } else {
            provinceName = provinceName.substring(0, 2);
        }
        if (cityName.contains("沙市") || cityName.contains("黄山市")
                || cityName.contains("津市")) {
            addressJason.setCityName(cityName);
        } else {
            addressJason.setCityName(cityName.replace("市", ""));
        }*/
        addressJason.setCityName(cityName);
        addressJason.setProvinceName(provinceName);
        return addressJason;
    }
}
