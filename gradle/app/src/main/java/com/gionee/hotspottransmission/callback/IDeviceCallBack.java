package com.gionee.hotspottransmission.callback;

/**
 * Created by luorw on 6/13/17.
 */
public interface IDeviceCallBack {
    void onWifiUnAvailable();
    void onExit();
    void onRefreshMenu(boolean isTransferable,boolean isVisible);
    void onFullStorage();
}
