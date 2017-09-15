package com.gionee.hotspottransmission.bean;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.utils.LogUtil;

/**
 * Created by luorw on 7/21/17.
 */

public class SocketChannel {
    private static SocketChannel mSocketChannel;
    public Map<String, String> mAddresses;
    public Map<String, Socket> mCommendSockets;
    public Map<String, String> mName;
    private ServerSocket mServerSocket;

    private SocketChannel() {
        mName = new ArrayMap<>();
        mAddresses = new ArrayMap<>();
        mCommendSockets = new ArrayMap<>();
    }

    public synchronized static SocketChannel getInstance() {
        if (mSocketChannel == null) {
            mSocketChannel = new SocketChannel();
        }
        return mSocketChannel;
    }

    public ServerSocket getmServerSocket() {
        if(mServerSocket == null){
            try {
                mServerSocket = new ServerSocket();
                mServerSocket.setReuseAddress(true);
                mServerSocket.bind(new InetSocketAddress(Constants.SEND_FILES_PORT));
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.i("luorw , SocketChannel:   传输ServerSocket IOException : " + e.getMessage());
            }
        }
        return mServerSocket;
    }

    public void addAddress(String key, String ip) {
        Log.i("luorw", "addAddress = " + ip);
        mAddresses.put(key, ip);
    }

    public void removeAddress(String key) {
        mAddresses.remove(key);
    }

    public void addName(String key, String name) {
        Log.i("luorw", "addName = " + name);
        mName.put(key, name);
    }

    public void removeName(String key) {
        mName.remove(key);
    }

    public void addCommendSocket(String key, Socket socket) {
        Log.i("luorw", "addReceiveSocket = " + socket.toString());
        mCommendSockets.put(key, socket);
    }

    public void removeCommendSocket(String key) {
        mCommendSockets.remove(key);
    }

    private void addServerSocketChannel(ServerSocket serverSocket, String[] info, IMultiDeviceCallBack multiDeviceCallBack) {
        android.util.Log.d("luorw", "SocketcChannel addServerSocketChannel ------- imei = " + info[3] + " , ip = " + info[1] + " ,serverSocket = " + serverSocket);
        try {
            Socket socket = serverSocket.accept();
            addCommendSocket(info[3], socket);
            addAddress(info[3],info[1]);
            addName(info[3],info[2]);
            LogUtil.i("luorw , multiDeviceCallBack.onReadCommand--------------");
            multiDeviceCallBack.onReadCommand(info[3]);
        } catch (IOException e) {
            android.util.Log.d("luorw", "SocketcChannel addServerSocketChannel catch e = " + e.toString());
            e.printStackTrace();
        }
    }

    private void addSocketChannel(String[] info, IMultiDeviceCallBack multiDeviceCallBack) {
        android.util.Log.d("luorw", "SocketcChannel addSocketChannel imei = " + info[3] + ",ip = " + info[1]);
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect((new InetSocketAddress(info[1], Constants.RECEIVE_COMMAND_PORT)), 5000);
            addCommendSocket(info[3], socket);
            addAddress(info[3],info[1]);
            addName(info[3],info[2]);
            LogUtil.i("luorw , multiDeviceCallBack.onReadCommand--------------");
            multiDeviceCallBack.onReadCommand(info[3]);
        } catch (IOException e) {
            android.util.Log.d("luorw", "SocketcChannel addSocketChannel catch e = " + e.toString());
            e.printStackTrace();
            SystemClock.sleep(500);
            addSocketChannel(info, multiDeviceCallBack);
        }
    }

    public void createServerCommandSocket(final ServerSocket serverSocket, final String[] info, final IMultiDeviceCallBack multiDeviceCallBack) {
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                if (!mCommendSockets.containsKey(info[3])) {
                    android.util.Log.d("luorw", "SocketcChannel createServerCommandSocket -------SEND_COMMAND_PORT");
                    addServerSocketChannel(serverSocket, info, multiDeviceCallBack);
                }
            }
        });
    }

    public void createCommandSocket(final String[] info, final IMultiDeviceCallBack multiDeviceCallBack) {
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                if (!mCommendSockets.containsKey(info[3])) {
                    android.util.Log.d("luorw", "SocketcChannel createCommendSocket -------RECEIVE_COMMAND_PORT");
                    addSocketChannel(info, multiDeviceCallBack);
                }
            }
        });
    }
}
