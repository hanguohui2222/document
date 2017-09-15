package com.gionee.hotspottransmission.callback;

import android.net.wifi.ScanResult;
import java.util.List;

/**
 * Created by luorw on 5/16/17.
 */
public interface IGcStatusCallBack {
    void onWifiDisabled();
    void onScanResultsAvailable(List<ScanResult> scanResults);
    void onWifiConnected(String hostIp);
    void online(String name);
    void onWifiDisconnected();
    void onExit();
    void onRefreshEnable();
    boolean isGroupTransfer();
}
