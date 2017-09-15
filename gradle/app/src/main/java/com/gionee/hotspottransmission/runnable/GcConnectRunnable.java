package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.os.SystemClock;

import com.gionee.hotspottransmission.callback.IGcStatusCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.biz.DeviceBiz;
import com.gionee.hotspottransmission.manager.WifiGcManager;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by luorw on 5/25/17.
 */

public class GcConnectRunnable implements Runnable {
    private IGcStatusCallBack mIGcStatusCallBack;
    private WifiGcManager mWifiMgr;
    private String mHost;
    private Context mContext;

    public GcConnectRunnable(Context context,WifiGcManager wifiMgr,IGcStatusCallBack gcStatusCallBack) {
        mContext = context.getApplicationContext();
        mIGcStatusCallBack = gcStatusCallBack;
        mWifiMgr = wifiMgr;
    }

    @Override
    public void run() {
        if(mIGcStatusCallBack.isGroupTransfer()){
            workForMulti();
        }else{
            work();
        }
    }

    private void workForMulti() {
        try {
            getGroupOwnerIp();
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(null);
            LogUtil.i("luorw111 , Gc:  mHost= " + mHost + " clientIp = " + mWifiMgr.getLocalIp());
            socket.connect((new InetSocketAddress(mHost, Constants.CONNECT_SUCCESS_PORT)), 5000);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
            String localIp = mWifiMgr.getLocalIp();
            outWriter.println(Constants.MSG_ONLINE + "," + localIp + "," + DeviceSp.getInstance().getDeviceName(mContext) + "," + DeviceSp.getInstance().getDeviceAddress(mContext)+","+mHost);
            LogUtil.i("luorw111 , outWriter println-------------" );
            socket.close();
            DeviceSp.getInstance().saveDeviceIp(mContext,localIp);
            DeviceSp.getInstance().saveHostIp(mContext,mHost);
        } catch (IOException e) {
            LogUtil.i("luorw111 , Gc connect success , IOException = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void work() {
        try {
            getGroupOwnerIp();
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(null);
            LogUtil.i("Gc:  mHost= " + mHost + " clientIp = " + mWifiMgr.getLocalIp());
            socket.connect((new InetSocketAddress(mHost, Constants.CONNECT_SUCCESS_PORT)), 5000);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
            String localIp = mWifiMgr.getLocalIp();
            outWriter.println(Constants.MSG_ONLINE + "," + localIp + "," + DeviceSp.getInstance().getDeviceName(mContext) + "," + DeviceSp.getInstance().getDeviceAddress(mContext)+","+mHost);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = bufferedReader.readLine();
            LogUtil.i("luorw111 , GcConnectRunnable,receive online deviceInfo : " + msg);
            LogUtil.i("luorw111 , GcConnectRunnable,receive online : " + mHost);
            bufferedReader.close();
            socket.close();
            DeviceSp.getInstance().saveDeviceIp(mContext,localIp);
            DeviceSp.getInstance().saveHostIp(mContext,mHost);
            //数据库存储连接成功的设备名称
            saveDeviceInfo(msg);
        } catch (IOException e) {
            LogUtil.i("luorw111 , Gc connect success , IOException = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getGroupOwnerIp() {
        //确保WiFi连接后获取正确IP地址
        int tryCount = 0;
        mHost = mWifiMgr.getIpAddressFromHotspot();
        while (mHost.equals(Constants.DEFAULT_UNKNOW_IP) && tryCount < Constants.DEFAULT_TRY_COUNT) {
            LogUtil.i("luorw111 , getIpAddressFromHotspot ------" + mHost + " - " + tryCount);
            SystemClock.sleep(500);
            mHost = mWifiMgr.getIpAddressFromHotspot();
            tryCount++;
        }
        //是否可以ping通指定IP地址
        tryCount = 0;
        while (!mWifiMgr.pingIpAddress(mHost) && tryCount < Constants.DEFAULT_TRY_COUNT) {
            SystemClock.sleep(500);
            LogUtil.i("luorw111 , Try to ping ------" + mHost + " - " + tryCount);
            tryCount++;
        }
        LogUtil.i("luorw111 , mHost : " + mHost);
    }

    private void saveDeviceInfo(String msg) {
        DeviceBiz deviceBiz = new DeviceBiz(mContext);
        DeviceInfo deviceInfo = new DeviceInfo();
        String[] info = msg.split(",");
        deviceInfo.status = info[0];
        deviceInfo.ip = mHost;
        deviceInfo.deviceName = info[2];
        deviceInfo.deviceAddress = info[3];
        DeviceSp.getInstance().saveConnectedDeviceName(mContext,deviceInfo.deviceName);
        DeviceSp.getInstance().saveConnectedDeviceAddress(mContext,deviceInfo.deviceAddress);
        Calendar calendar = Calendar.getInstance();
        deviceInfo.connectTime = calendar.getTime();
        deviceBiz.addDevice(deviceInfo);
    }

}
