package com.gionee.hotspottransmission.sharepreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.gionee.hotspottransmission.constants.Constants;

/**
 * Created by luorw on 5/9/17.
 */

public class DeviceSp {
    private static DeviceSp mDeviceSp;

    private DeviceSp() {

    }

    public static DeviceSp getInstance() {
        synchronized (DeviceSp.class) {
            if (mDeviceSp == null) {
                synchronized (DeviceSp.class){
                    if (mDeviceSp == null) {
                        mDeviceSp = new DeviceSp();
                    }
                }
            }
        }
        return mDeviceSp;
    }

    public String getDeviceName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.DEVICE_NAME,null);
        if(name == null || "".equals(name)){
            name = android.os.Build.MODEL;
            DeviceSp.getInstance().saveDeviceName(context,name);
        }
        return name;
    }

    public void saveDeviceName(Context context, String deviceName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.DEVICE_NAME, deviceName).commit();
    }

    public void saveDeviceAddress(Context context, String deviceAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.DEVICE_ADDRESS_KEY, deviceAddress);
        editor.commit();
    }

    public String getDeviceAddress(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.DEVICE_ADDRESS_KEY,null);
        if(name == null){
            name = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        return name;
    }

    public void saveDeviceIp(Context context, String ip) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.DEVICE_IP_KEY, ip);
        editor.commit();
    }

    public String getDeviceIp(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.DEVICE_IP_KEY,null);
        return name;
    }

    public void saveHostIp(Context context, String ip) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.HOST_IP_KEY, ip);
        editor.commit();
    }

    public String getHostIp(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.HOST_IP_KEY,null);
        return name;
    }

    public void saveConnectedDeviceName(Context context, String deviceName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CONNECTED_DEVICE_NAME_KEY, deviceName);
        editor.commit();
    }

    public String getConnectedDeviceName(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.CONNECTED_DEVICE_NAME_KEY,null);
        return name;
    }

    public void saveConnectedDeviceAddress(Context context, String deviceAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CONNECTED_DEVICE_ADDRESS_KEY, deviceAddress);
        editor.commit();
    }

    public String getConnectedDeviceAddress(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(Constants.PREFERENCE_DEVICE_INFO,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.CONNECTED_DEVICE_ADDRESS_KEY,null);
        return name;
    }
}
