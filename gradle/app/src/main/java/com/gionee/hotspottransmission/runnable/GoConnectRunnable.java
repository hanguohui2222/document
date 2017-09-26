package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.text.TextUtils;

import com.gionee.hotspottransmission.callback.IGoStatusCallBack;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.biz.DeviceBiz;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by luorw on 5/25/17.
 */
public class GoConnectRunnable implements Runnable {
    private IGoStatusCallBack mIGoStatusCallBack;
    private Context mContext;
    public boolean isGroupTransfer;

    public GoConnectRunnable(Context context) {
        mContext = context.getApplicationContext();
    }

    public GoConnectRunnable(Context context, IGoStatusCallBack IGoStatusCallBack) {
        mContext = context.getApplicationContext();
        mIGoStatusCallBack = IGoStatusCallBack;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Constants.CONNECT_SUCCESS_PORT));
            if (isGroupTransfer) {
                while (true) {
                    workForMulti(serverSocket);
                }
            } else {
                work(serverSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void work(ServerSocket serverSocket) {
        LogUtil.i("GoConnectRunnable,work online-------------");
        try {
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = bufferedReader.readLine();
            LogUtil.i("GoConnectRunnable,receive online: " + msg);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
            String hostIp = msg.split(",")[4];
            String outStr = Constants.MSG_ONLINE + "," + hostIp + "," + DeviceSp.getInstance().getDeviceName(mContext) + "," + DeviceSp.getInstance().getDeviceAddress(mContext);
            outWriter.println(outStr);
            bufferedReader.close();
            socket.close();
            DeviceSp.getInstance().saveDeviceIp(mContext, hostIp);
            DeviceSp.getInstance().saveHostIp(mContext,hostIp);
            if (mIGoStatusCallBack != null) {
                mIGoStatusCallBack.onWifiConnected(msg.split(",")[1]);
                mIGoStatusCallBack.online(msg.split(",")[2]);
            }
            saveDeviceInfo(msg);
        } catch (IOException e) {
            LogUtil.i("GoConnectRunnable ,online, IOException = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void workForMulti(ServerSocket serverSocket) {
        LogUtil.i("luorw111 , GoConnectRunnable,work online-------------");
        try {
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = bufferedReader.readLine();
            LogUtil.i("luorw111 , GoConnectRunnable,receive online: " + msg);
            bufferedReader.close();
            socket.close();
            if (!TextUtils.isEmpty(msg)) {
                String[] info = msg.split(",");
                //连接并跳转到传输界面时提示上线
                if (mIGoStatusCallBack != null) {
                    mIGoStatusCallBack.onWifiConnected(info[1]);
                    LogUtil.i("luorw111 , GoConnectRunnable,onWifiConnected = " + info[1]);
                }
                LogUtil.i("luorw111 , GoConnectRunnable,receive : online = " + info[2]);
                DeviceSp.getInstance().saveDeviceIp(mContext, info[4]);
                DeviceSp.getInstance().saveHostIp(mContext,info[4]);
            }
        } catch (IOException e) {
            LogUtil.i("luorw111 , GoConnectRunnable ,online, IOException = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveDeviceInfo(String msg) {
        DeviceBiz deviceBiz = new DeviceBiz(mContext);
        DeviceInfo deviceInfo = new DeviceInfo();
        String[] info = msg.split(",");
        deviceInfo.status = info[0];
        deviceInfo.ip = info[1];
        deviceInfo.deviceName = info[2];
        deviceInfo.deviceAddress = info[3];
        DeviceSp.getInstance().saveConnectedDeviceName(mContext, deviceInfo.deviceName);
        DeviceSp.getInstance().saveConnectedDeviceAddress(mContext, deviceInfo.deviceAddress);
        Calendar calendar = Calendar.getInstance();
        deviceInfo.connectTime = calendar.getTime();
        deviceBiz.addDevice(deviceInfo);
    }
}
