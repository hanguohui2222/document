package com.gionee.hotspottransmission.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.gionee.hotspottransmission.callback.IGcStatusCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.receiver.WifiBroadcastReceiver;
import com.gionee.hotspottransmission.runnable.GcConnectRunnable;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.manager.WifiGcManager;

import java.util.List;

/**
 * Created by luorw on 5/10/17.
 */
public class GroupClientService extends BaseService {
    private GcServiceBinder mBinder;
    private WifiBroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private WifiGcManager mWifiMgr;
    private boolean mIsConnected;
    private IGcStatusCallBack mIGcStatusCallBack;
    private boolean isRegisterReceiver;
    private boolean mIsStartScan;
    private List<ScanResult> mScanResults;

    public GroupClientService() {

    }

    public void setmIsStartScan(boolean mIsStartScan) {
        this.mIsStartScan = mIsStartScan;
    }

    public void setmIGcStatusCallBack(IGcStatusCallBack mIGcStatusCallBack) {
        this.mIGcStatusCallBack = mIGcStatusCallBack;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBinder();
        prepare();
    }

    private void initBinder() {
        mBinder = new GcServiceBinder();
    }

    public class GcServiceBinder extends Binder {
        public GroupClientService getService() {
            return GroupClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 准备工作，声明wifi广播接收者
     */
    public void prepare() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mReceiver = new WifiBroadcastReceiver(this);
    }

    /**
     * 注册广播接收者，开始扫描热点
     */
    public void workForScan() {
        registerReceiver(mReceiver, mFilter);
        isRegisterReceiver = true;
        mWifiMgr = new WifiGcManager(this);
        if (mWifiMgr.isWifiEnabled()) {
            search(false);
        } else {
            mWifiMgr.openWifi();
        }
    }

    /**
     * 注册广播接收者，监听wifi连接状态，并启动等待接收命令线程（可启动ping线程监听wifi的连接状态）
     */
    public void workForTransfer() {
        registerReceiver(mReceiver, mFilter);
        isRegisterReceiver = true;
        createReceiveCommand();
        createSendCommand();
    }

    /**
     * 响应所有监听执行的指令
     *
     * @param what
     */
    public void action(int what) {
        switch (what) {
            case Constants.ON_SHOW_SCAN_RESULTS:
                mScanResults = mWifiMgr.getScanResults();
                if (mScanResults != null && mScanResults.size() > 0) {
                    mIGcStatusCallBack.onScanResultsAvailable(mScanResults);
                }
                break;
            case Constants.ON_CONNECTED:
                String ssid = mWifiMgr.getConnectedSSID();
                if (!mIsConnected && !TextUtils.isEmpty(ssid) && ssid.contains(Constants.WIFI_HOT_SPOT_SSID_PREFIX)) {
                    mIsConnected = true;
                    LogUtil.i("luorw111 , 连接上热点------------");
                    mIGcStatusCallBack.onWifiConnected(mHost);
                    // 告知发送端连接成功
                    doNotifyConnectSuccess();
                }
                break;
            case Constants.ON_DISCONNECTED:
                if (mIsConnected && mIGcStatusCallBack != null) {
                    mIGcStatusCallBack.onWifiDisconnected();
                }
                break;
            case Constants.ON_WIFI_DISABLE:
                if (mIsStartScan) {
                    mIGcStatusCallBack.onWifiDisabled();
                }
                break;
            default:
                break;
        }
    }

    public void search(final boolean isResearch) {
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                if (!mIsStartScan || isResearch) {
                    mWifiMgr.startScan();
                    mHandler.sendEmptyMessageDelayed(Constants.ON_SCAN_REPEAT, 3000);
//            mHandler.sendEmptyMessageDelayed(Constants.ON_RESCAN_ENABLE, 30000);
                    mIsStartScan = true;
                }
            }
        });
    }

    /**
     * 连接对方wifi
     *
     * @param ssid
     */
    public void connect(String ssid, boolean isGroupTransfer) {
        LogUtil.i("luorw , connect , ssid = " + ssid);
        boolean enableConnect = mWifiMgr.connectWifi(ssid, isGroupTransfer);
//        if (!enableConnect) {
//            LogUtil.i("GC,连接失败");
//            if (mIGcStatusCallBack != null) {
//                mIGcStatusCallBack.onExit();
//            }
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播
        unregisterWifiReceiver();
    }

    private void unregisterWifiReceiver() {
        if (mReceiver != null && isRegisterReceiver) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
            isRegisterReceiver = false;
        }
    }

    /**
     * 获取设备连接状态
     */
    public boolean getConnState() {
        return mIsConnected;
    }

    /**
     * 告知Go端连接成功
     */
    @Override
    protected void doNotifyConnectSuccess() {
        GcConnectRunnable gcConnectRunnable = new GcConnectRunnable(this, mWifiMgr, mIGcStatusCallBack);
        ThreadPoolManager.getInstance().executeRunnable(gcConnectRunnable);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.ON_RESCAN_ENABLE:
//                    if (mIsStartScan) {
//                        mIGcStatusCallBack.onRefreshEnable();
//                    }
                    break;
                case Constants.ON_SCAN_REPEAT:
                    if (mIsStartScan) {
                        mWifiMgr.startScan();
                        mHandler.sendEmptyMessageDelayed(Constants.ON_SCAN_REPEAT, 10000);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean isGroupOwner() {
        return false;
    }

}
