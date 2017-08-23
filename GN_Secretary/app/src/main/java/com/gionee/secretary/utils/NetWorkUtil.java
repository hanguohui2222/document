package com.gionee.secretary.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by zhuboqin on 22/06/16.
 */
public class NetWorkUtil {

    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */

    public static boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            //added by luorw for S10 Bug #90235 2017/03/30 begin
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
            boolean mobile = false;
            boolean wifi = false;
            if (activeInfo == null) {
                return false;
            } else {
                if (activeInfo.getTypeName().equalsIgnoreCase("mobile")) {
                    mobile = mobileInfo.isConnected();
                }
                if (activeInfo.getTypeName().equalsIgnoreCase("wifi")) {
                    wifi = wifiInfo.isConnected();
                }
            }
            return mobile || wifi;
        }
        //added by luorw for S10 Bug #90235 2017/03/30 end
    }

}
