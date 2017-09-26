package com.gionee.hotspottransmission.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.runnable.multi.GoMultiConnectRunnable;
import com.gionee.hotspottransmission.runnable.multi.MultiBroadcastRunnable;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

public class GoMultiService extends MultiBaseService {
    public GoMultiService() {
    }

    private GoMultiService.GoMultiServiceBinder mBinder;

    private void initBinder() {
        mBinder = new GoMultiService.GoMultiServiceBinder();
    }


    public class GoMultiServiceBinder extends Binder {
        public GoMultiService getService() {
            return GoMultiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBinder();
    }

    /**
     * 另起线程，监听并接收其他上线的设备信息
     */
    public void watchOnline() {
        LogUtil.i("luorw , GoMultiService , watchOnline");
        GoMultiConnectRunnable runnable = new GoMultiConnectRunnable(this);
        ThreadPoolManager.getInstance().executeRunnable(runnable);
        // added by luorw begin
        String imei = DeviceSp.getInstance().getDeviceAddress(this);
        String msg = Constants.MSG_ONLINE + "," + DeviceSp.getInstance().getDeviceIp(this) + "," + DeviceSp.getInstance().getDeviceName(this) + "," + imei;
        mMultiBroadcastRunnable = new MultiBroadcastRunnable(this, multiDeviceCallBack);
        mMultiBroadcastRunnable.createWriteRunnable(msg);
        // added by luorw end
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAllCommandSocket();
        mMultiBroadcastRunnable.closeSocket();
        ThreadPoolManager.getInstance().shutdown();
    }

    @Override
    public boolean isGroupOwner() {
        return true;
    }
}
