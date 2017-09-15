package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.service.MultiBaseService;

/**
 * Created by luorw on 7/12/17.
 */

public interface IMultiDeviceCallBack {
    void online(String deviceName);
    void offline(String msg);
    MultiBaseService getMultiService();
    void onFullStorage();
    void onRefreshMenu(boolean isTransferable,boolean isVisible);
    void onReadCommand(String key);
    void onAddReceive(String key);
}
