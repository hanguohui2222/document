package com.gionee.hotspottransmission.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.service.GroupOwnerService;
import com.gionee.hotspottransmission.utils.LogUtil;

/**
 * Created by luorw on 5/10/17.
 */
public class HotspotBroadcastReceiver extends BroadcastReceiver {
    private GroupOwnerService mGOService;

    public HotspotBroadcastReceiver(GroupOwnerService goService) {
        this.mGOService = goService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i("onReceive , action = "+action);
        if (action.equals(Constants.ACTION_HOTSPOT_STATE_CHANGED)) {
            //便携热点状态监听
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                //便携热点可用
                mGOService.action(Constants.ON_HOTSPOT_ENABLE);
            }
        }
    }
}
