package com.gionee.hotspottransmission.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.callback.IGoStatusCallBack;
import com.gionee.hotspottransmission.manager.HotspotManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.receiver.HotspotBroadcastReceiver;
import com.gionee.hotspottransmission.runnable.GoConnectRunnable;
import com.gionee.hotspottransmission.utils.FileUtil;

/**
 * Created by luorw on 5/10/17.
 */
public class GroupOwnerService extends BaseService {
    private GoServiceBinder mBinder;
    private HotspotBroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private boolean isRegisterReceiver;
    private IGoStatusCallBack mIGoStatusCallBack;
    private HotspotManager mHotspotMgr;
    private GoConnectRunnable mGoConnectRunnable;

    public GroupOwnerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBinder();
        prepare();
    }

    private void initBinder() {
        mBinder = new GoServiceBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class GoServiceBinder extends Binder {
        public GroupOwnerService getService() {
            return GroupOwnerService.this;
        }
    }

    public void setmIGoStatusCallBack(IGoStatusCallBack mIGoStatusCallBack) {
        this.mIGoStatusCallBack = mIGoStatusCallBack;
    }

    /**
     * 准备工作，声明广播接收者
     */
    public void prepare() {
        mFilter = new IntentFilter(Constants.ACTION_HOTSPOT_STATE_CHANGED);
        mReceiver = new HotspotBroadcastReceiver(this);
    }

    /**
     * 注册热点的状态监听，并启动热点
     */
    public void workForScan(boolean isGroupTransfer) {
        registerReceiver(mReceiver, mFilter);
        isRegisterReceiver = true;
        mHotspotMgr = new HotspotManager();
        mHotspotMgr.openHotspot(this, FileUtil.getPasswordForHotspot(isGroupTransfer));
    }

    /**
     * 注册热点状态的监听，也就是在监听连接的状态，并启动等待接收命令的线程
     */
    public void workForTransfer() {
        registerReceiver(mReceiver, mFilter);
        isRegisterReceiver = true;
        createSendCommand();
        createReceiveCommand();
    }

//    private static final int REQUEST_CODE_ASK_WRITE_SETTINGS = 1;
//    private void getPermissionAndOpenAp(){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.System.canWrite(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
//                        Uri.parse("package:" + getPackageName()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
//            } else {
//                HotspotManager.openHotspot(this,"11111111");
//            }
//        }
//    }

    /**
     * 响应所有监听执行的指令
     *
     * @param what
     */
    public void action(int what) {
        switch (what) {
            case Constants.ON_HOTSPOT_ENABLE:
                mIGoStatusCallBack.onHotSpotEnabled(mHotspotMgr.getWifiApSSID());
                break;
            default:
                break;
        }
    }

    private void unregisterHotSpotReceiver() {
        if (mReceiver != null && isRegisterReceiver) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
            isRegisterReceiver = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterHotSpotReceiver();
    }

    /**
     * 当GC端连接上本热点，会发消息通知本端（GO），此方法等待GC端发来连接成功的指令
     */
    @Override
    protected void doNotifyConnectSuccess() {
        mGoConnectRunnable = new GoConnectRunnable(this, mIGoStatusCallBack);
        ThreadPoolManager.getInstance().executeRunnable(mGoConnectRunnable);
    }

    @Override
    public boolean isGroupOwner() {
        return true;
    }
}
