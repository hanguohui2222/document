package com.gionee.hotspottransmission.callback;

/**
 * Created by luorw on 5/16/17.
 */
public interface IGoStatusCallBack {
    void onHotSpotEnabled(String ssid);
    void onWifiConnected(String clientIp);
    void online(String name);
    void onWifiDisconnected();
}
