package com.gionee.hotspottransmission.manager;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by luorw on 5/10/17.
 */
public class HotspotManager {
    private String ssid;

    public HotspotManager() {

    }
    /**
     * create WiFi Hot Spot SSID suffix
     */
    public void createWifiApSSID() {
        ssid = Constants.WIFI_HOT_SPOT_SSID_PREFIX + "-" + new Random().nextInt(1000);
    }

    public String getWifiApSSID(){
        return ssid;
    }
    /**
     * 便携热点是否开启
     * @param context 上下文
     * @return
     */
    public boolean isHotspotOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * 关闭Wi-Fi
     * @param context 上下文
     */
    public void closeWifi(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(false);
        }
    }

    /**打开wifi
     * @param context
     */
    public void openWifi(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        if (!wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(true);
        }
    }

    /**
     * 开启便携热点
     * @param context 上下文
     * @param password 便携热点密码
     * @return
     */
    public boolean openHotspot(Context context,String password) {
        createWifiApSSID();
        if(TextUtils.isEmpty(ssid)) {
            return false;
        }
        closeWifi(context);
        WifiConfiguration wifiConfiguration = getApConfig(ssid, password,context);
        try {
            WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            if(isHotspotOn(context)) {
                wifimanager.setWifiEnabled(false);
                closeHotspot(context);
            }
            //使用反射开启Wi-Fi热点
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wifiConfiguration, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("openHotspot , e = " + e.getMessage());
        }
        return false;
    }

    /**
     * 关闭便携热点
     * @param context 上下文
     */
    public void closeHotspot(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取开启便携热点后自身热点IP地址
     * @param context
     * @return
     */
    public String getHotspotLocalIpAddress(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifimanager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    /**
     * 设置有密码的热点信息
     * @param ssid 便携热点ssid
     * @param pwd 便携热点密码
     * @return
     */
    private WifiConfiguration getApConfig(String ssid, String pwd,Context context) {
        if(TextUtils.isEmpty(pwd)) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.preSharedKey = pwd + ssid.split("-")[1];
        LogUtil.i("connect , getApConfig , pwd = "+pwd);
        config.providerFriendlyName = DeviceSp.getInstance().getDeviceName(context);
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return config;
    }

}
