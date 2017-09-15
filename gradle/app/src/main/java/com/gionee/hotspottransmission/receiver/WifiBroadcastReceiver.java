package com.gionee.hotspottransmission.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.service.GcMultiService;
import com.gionee.hotspottransmission.service.GroupClientService;

/**
 * Created by luorw on 2017/5/12.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {
    private GroupClientService mGcService;

    public WifiBroadcastReceiver(GroupClientService gcService) {
        super();
        this.mGcService = gcService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                //WiFi已开启
                mGcService.search(false);
            } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                //WiFi已关闭
                mGcService.action(Constants.ON_WIFI_DISABLE);
            }
        } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            mGcService.action(Constants.ON_SHOW_SCAN_RESULTS);
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info != null && info.getState().equals(NetworkInfo.State.CONNECTED)) {
                //WiFi已连接
                mGcService.action(Constants.ON_CONNECTED);
            }
            if (info != null && info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                //WiFi已断开连接
                mGcService.action(Constants.ON_DISCONNECTED);
            }
        }
    }
}
