package com.gionee.hotspottransmission.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.runnable.PingRunnable;
import com.gionee.hotspottransmission.runnable.multi.MultiBroadcastRunnable;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

public class GcMultiService extends MultiBaseService {
    private GcMultiService.GcMultiServiceBinder mBinder;

    public GcMultiService() {

    }

    private void initBinder() {
        mBinder = new GcMultiService.GcMultiServiceBinder();
    }

    public class GcMultiServiceBinder extends Binder {
        public GcMultiService getService() {
            return GcMultiService.this;
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
     *另起线程，监听并接收其他上线的设备信息
     */
    public void watchOnline() {
        LogUtil.i("luorw , GcMultiService , watchOnline");
        String imei = DeviceSp.getInstance().getDeviceAddress(this);
        String msg = Constants.MSG_ONLINE + "," + DeviceSp.getInstance().getDeviceIp(this) + "," + DeviceSp.getInstance().getDeviceName(this) + "," + imei;
        mMultiBroadcastRunnable = new MultiBroadcastRunnable(this, multiDeviceCallBack);
        mMultiBroadcastRunnable.createWriteRunnable(msg);
    }

    public void pingHost(){
        PingRunnable pingRunnable = new PingRunnable(this,multiDeviceCallBack);
        ThreadPoolManager.getInstance().executeRunnable(pingRunnable);
    }

    @Override
    public void onDestroy() {
        LogUtil.i("luorw1 , GcMultiService , onDestroy-----mMultiBroadcastRunnable---closeSocket--");
        mMultiBroadcastRunnable.closeSocket();
        ThreadPoolManager.getInstance().shutdown();
    }

    @Override
    public boolean isGroupOwner() {
        return false;
    }

}
