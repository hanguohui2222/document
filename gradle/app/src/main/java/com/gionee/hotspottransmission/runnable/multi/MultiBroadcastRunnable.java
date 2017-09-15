package com.gionee.hotspottransmission.runnable.multi;

import android.content.Context;
import android.text.TextUtils;

import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.biz.DeviceBiz;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.manager.WifiGcManager;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Calendar;

/**
 * Created by luorw on 7/7/17.
 */
public class MultiBroadcastRunnable {
    private Context mContext;
    private InetAddress mBroadcastAddress;
    private IMultiDeviceCallBack mMultiDeviceCallBack;
    private DatagramSocket mDatagramSocket;
    private WriteOnlineRunnable mWriteOnlineRunnable;
    private ReadOnlineRunnable mReadOnlineRunnable;

    public MultiBroadcastRunnable(Context context, IMultiDeviceCallBack listener) {
        mContext = context.getApplicationContext();
        mMultiDeviceCallBack = listener;
    }

    public class ReadOnlineRunnable implements Runnable {
        private ServerSocket mCommandServerSocket;

        public ReadOnlineRunnable() {
            mCommandServerSocket = getServerSocket(Constants.SEND_COMMAND_PORT);
        }

        @Override
        public void run() {
            try {
                read();
            } catch (Exception e) {
                LogUtil.e("luorw , MultiBroadcastRunnable , online,ReadOnlineRunnable , e = " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void read() throws Exception {
            byte[] receiveData = new byte[Constants.DATA_LEN];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (mDatagramSocket != null) {
                LogUtil.i("luorw , MultiBroadcastRunnable , ReadOnlineRunnable , mDatagramSocket != null");
                mDatagramSocket.receive(receivePacket);
                String msg = new String(receiveData, 0, receivePacket.getLength());
                String[] info = msg.split(",");
                LogUtil.i("luorw , MultiBroadcastRunnable , ReadOnlineRunnable , receive = " + msg);
                LogUtil.i("luorw , MultiBroadcastRunnable,receive : online = " + msg.split(",")[2]);
                if (filterSelf(msg)) {
                    continue;
                }
                if (isResponse(msg)) {
                    android.util.Log.d("luorw", "MultiBroadcastRunnable read response -------" + msg);
                    SocketChannel.getInstance().createCommandSocket(info, mMultiDeviceCallBack);
                    msg = msg.replace(Constants.MSG_RESPONSE, Constants.MSG_ONLINE);
                    saveDeviceInfo(msg);
                    continue;
                }
                if (isOffline(msg)) {
                    android.util.Log.d("luorw", "MultiBroadcastRunnable read Offline -------" + msg);
                    mMultiDeviceCallBack.offline(msg);
                    continue;
                }
                if (mMultiDeviceCallBack != null) {
                    android.util.Log.d("luorw111", "MultiBroadcastRunnable read online -------" + msg);
                    SocketChannel.getInstance().createServerCommandSocket(mCommandServerSocket, info, mMultiDeviceCallBack);
                    mMultiDeviceCallBack.online(info[2]);
                }
                String response = Constants.MSG_RESPONSE + "," + DeviceSp.getInstance().getDeviceIp(mContext) + "," + DeviceSp.getInstance().getDeviceName(mContext) + "," + DeviceSp.getInstance().getDeviceAddress(mContext);
                mWriteOnlineRunnable.write(response);
                saveDeviceInfo(msg);
            }
        }

        private boolean filterSelf(String msg) {
            LogUtil.i("luorw , MultiBroadcastRunnable , ReadOnlineRunnable ,filterSelf = " + DeviceSp.getInstance().getDeviceAddress(mContext) + " , " + msg.split(",")[3]);
            return TextUtils.equals(DeviceSp.getInstance().getDeviceAddress(mContext), msg.split(",")[3]);
        }

        private boolean isResponse(String msg) {
            return TextUtils.equals(Constants.MSG_RESPONSE, msg.split(",")[0]);
        }

        private boolean isOffline(String msg) {
            return TextUtils.equals(Constants.MSG_OFFLINE, msg.split(",")[0]);
        }
    }

    public class WriteOnlineRunnable implements Runnable {
        String msg;

        public WriteOnlineRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (Constants.MSG_ONLINE.equals(msg.split(",")[0])) {
                createReadRunnable();
            }
            write(msg);
        }

        public void write(String msg) {
            LogUtil.e("luorw1 , MultiBroadcastRunnable , WriteOnlineRunnable , write---luorw----" + msg);
            byte[] sendData = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mBroadcastAddress, Constants.BROADCAST_PORT);
            try {
                mDatagramSocket.send(sendPacket);
            } catch (IOException e) {
                LogUtil.e("luorw1 , MultiBroadcastRunnable ,write, WriteOnlineRunnable , e = " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (msg.contains(Constants.MSG_OFFLINE)) {
                    LogUtil.e("luorw1 , MultiBroadcastRunnable ,write, 本机下线 closeSocket   ");
                    closeSocket();
                }
            }
        }
    }

    private ServerSocket getServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverSocket;
    }

    public void init() {
        try {
            if (mDatagramSocket == null) {
                //解决：java.net.BindException: bind failed: EADDRINUSE (Address already in use)
                mDatagramSocket = new DatagramSocket(null);
                mDatagramSocket.setReuseAddress(true);
                mDatagramSocket.bind(new InetSocketAddress(Constants.BROADCAST_PORT));
//                mBroadcastAddress = new WifiGcManager(mContext).getBroadcastAddress();
                String hostIp = DeviceSp.getInstance().getHostIp(mContext);
                while (hostIp == null){
                    LogUtil.e("luorw111 , MultiBroadcastRunnable , init , ------online----hostIp == null----");
                    hostIp = DeviceSp.getInstance().getHostIp(mContext);
                }
                String address = hostIp.substring(0, hostIp.length() - 1) + "255";
                mBroadcastAddress = InetAddress.getByName(address);
                LogUtil.e("luorw111 , MultiBroadcastRunnable , init , ------online--------" + mBroadcastAddress.toString()/* + " , address = "+address*/);
            }
        } catch (Exception e) {
            LogUtil.e("luorw , MultiBroadcastRunnable ,online, init , e = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createWriteRunnable(String msg) {
        android.util.Log.d("luorw1", "MultiBroadcastRunnable createWriteRunnable-----msg = ");
        mWriteOnlineRunnable = new WriteOnlineRunnable(msg);
        init();
        ThreadPoolManager.getInstance().executeRunnable(mWriteOnlineRunnable);
    }

    public void createReadRunnable() {
        mReadOnlineRunnable = new ReadOnlineRunnable();
        init();
        ThreadPoolManager.getInstance().executeRunnable(mReadOnlineRunnable);
    }

    public void closeSocket() {
        android.util.Log.d("luorw1", "MultiBroadcastRunnable closeSocket-----");
        if (mDatagramSocket != null) {
            mDatagramSocket.close();
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
        Calendar calendar = Calendar.getInstance();
        deviceInfo.connectTime = calendar.getTime();
        deviceBiz.addDevice(deviceInfo);
    }

}
