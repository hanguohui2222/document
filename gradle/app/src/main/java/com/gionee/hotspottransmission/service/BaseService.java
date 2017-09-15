package com.gionee.hotspottransmission.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.ResponseInfo;
import com.gionee.hotspottransmission.callback.IDeviceCallBack;
import com.gionee.hotspottransmission.callback.ITransferListener;
import com.gionee.hotspottransmission.callback.ITransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.runnable.multi.MultiBroadcastRunnable;
import com.gionee.hotspottransmission.runnable.ReceiverCommandRunnable;
import com.gionee.hotspottransmission.runnable.ReceiverTransferRunnable;
import com.gionee.hotspottransmission.runnable.SenderCommandRunnable;
import com.gionee.hotspottransmission.runnable.SenderTransferRunnable;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luorw on 5/26/17.
 */
public class BaseService extends Service implements ITransferService {
    public List<ITransferListener> mSendListeners = new ArrayList<>();
    public List<ITransferListener> mReceiveListeners = new ArrayList<>();
    public ReceiverCommandRunnable mReceiverCmdRunnable;
    public SenderCommandRunnable mSenderCmdRunnable;
    public SenderTransferRunnable mSenderFileRunnable;
    public String mHost;
    public String mClient;
    public IDeviceCallBack mSendDeviceCallBack;
    public IDeviceCallBack mReceiveDeviceCallBack;

    public BaseService() {

    }

    public void setHost(String mHost) {
        this.mHost = mHost;
    }

    public void setClient(String mClient) {
        this.mClient = mClient;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void registerSendListeners(ITransferListener transferListener) {
        mSendListeners.add(transferListener);
    }

    public void registerReceiveListeners(ITransferListener transferListener) {
        mReceiveListeners.add(transferListener);
    }

    public void unRegisterSendListener(ITransferListener transferListener) {
        mSendListeners.remove(transferListener);
    }

    public void unRegisterReceiveListener(ITransferListener transferListener) {
        mReceiveListeners.remove(transferListener);
    }

    public void setSendDeviceCallBack(IDeviceCallBack deviceCallBack) {
        this.mSendDeviceCallBack = deviceCallBack;
    }

    public void setReceiveDeviceCallBack(IDeviceCallBack deviceCallBack) {
        this.mReceiveDeviceCallBack = deviceCallBack;
    }

    /**
     * 告知对端连接成功
     */
    @Override
    public void notifyConnectSuccess() {
        doNotifyConnectSuccess();
    }

    protected void doNotifyConnectSuccess() {

    }

    /**
     * 等待对方发来的接收指令，先启动读命令线程，再启动写命令线程
     */
    @Override
    public void createReceiveCommand() {
        mReceiverCmdRunnable = new ReceiverCommandRunnable(this, mReceiveHandler, this);
        mReceiverCmdRunnable.createReadCommand();
    }

    @Override
    public void receiverWriteCommand(int command) {
        if (mReceiverCmdRunnable != null) {
            mReceiverCmdRunnable.createWriteCommand(command);
        }
    }

    /**
     * 获取接收命令的socket
     *
     * @return
     */
    @Override
    public Socket getReceiveCommandSocket() {
        try {
            Socket commandSocket = new Socket();
            LogUtil.i("getReceiveCommandSocket:   建立命令Socket");
            commandSocket.setReuseAddress(true);
            commandSocket.bind(null);
            if (isGroupOwner()) {
                LogUtil.i("getReceiveCommandSocket:  mClient= " + mClient);
                commandSocket.connect((new InetSocketAddress(mClient, Constants.RECEIVE_COMMAND_PORT)), 5000);
            } else {
                mHost = DeviceSp.getInstance().getHostIp(this);
                LogUtil.i("getReceiveCommandSocket:  mHost= " + mHost);
                commandSocket.connect((new InetSocketAddress(mHost, Constants.RECEIVE_COMMAND_PORT)), 5000);
            }
            LogUtil.i("getReceiveCommandSocket:   命令socket连接成功");
            return commandSocket;
        } catch (IOException e) {
            LogUtil.i("getReceiveCommandSocket:   建立命令IOException = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 启动接收文件的线程
     */
    @Override
    public void notifyReceiveWork() {
        ReceiverTransferRunnable receiveFileRunnable = new ReceiverTransferRunnable(this, mReceiveHandler, this ,mReceiveDeviceCallBack);
        ThreadPoolManager.getInstance().executeRunnable(receiveFileRunnable);
    }

    /**
     * 获取接收文件的socket
     *
     * @return
     */
    @Override
    public Socket getReceiveFileSocket() {
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(null);
            if (isGroupOwner()) {
                socket.connect((new InetSocketAddress(mClient, Constants.RECEIVE_FILES_PORT)), 5000);
            } else {
                socket.connect((new InetSocketAddress(mHost, Constants.RECEIVE_FILES_PORT)), 5000);
            }
            return socket;
        } catch (Exception e) {
            LogUtil.i("getReceiveFileSocket: IOException = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 选择好需要发送的文件后，执行发送，先把需要发送的文件描述以命令的形式发送给对方
     */
    @Override
    public void createSendCommand() {
        mSenderCmdRunnable = new SenderCommandRunnable(this, mSenderHandler, this ,mSendDeviceCallBack);
        mSenderCmdRunnable.createReadCommand();
    }

    @Override
    public void senderWriteCommand(int command) {
        if (mSenderCmdRunnable != null) {
            mSenderCmdRunnable.createWriteCommand(command);
        }
    }

    /**
     * 获取发送命令的socket
     *
     * @return
     */
    @Override
    public Socket getSendCommandSocket() {
        try {
            LogUtil.i("getSendCommandSocket:   建立命令ServerSocket");
            ServerSocket commandSocket = new ServerSocket();
            commandSocket.setReuseAddress(true);
            commandSocket.bind(new InetSocketAddress(Constants.SEND_COMMAND_PORT));
            Socket socket = commandSocket.accept();
            LogUtil.i("getSendCommandSocket:   建立命令ServerSocket 成功");
            return socket;
        } catch (IOException e) {
            LogUtil.i("getSendCommandSocket:   建立命令IOException = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 启动发送文件的线程
     */
    @Override
    public void notifySendWork() {
        mSenderFileRunnable = new SenderTransferRunnable(this, mSenderHandler, this);
        ThreadPoolManager.getInstance().executeRunnable(mSenderFileRunnable);
    }

    @Override
    public boolean isGroupOwner() {
        return false;
    }

    Handler mReceiveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.NOTIFY_RECEIVE_WORK:
                    notifyReceiveWork();
                    break;
                case Constants.RECEIVER_READ_FILE_LIST_SUCCESS:
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onReadFileListSuccess();
                    }
                    break;
                case Constants.RECEIVER_UPDATE_TRANSFER_PROGRESS:
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onUpdateTransferProgress(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_COMPLETE_BY_INDEX:
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onTransferCompleteByIndex(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_ALL_COMPLETE:
                    FileReceiveData.getInstance().setAllReceiveComplete(true);
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onTransferAllComplete();
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_CANCEL_BY_INDEX:
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onCancelByIndex(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_CANCEL_ALL:
                    FileReceiveData.getInstance().setCancelAllReceive(true);
                    for (ITransferListener transferListener : mReceiveListeners) {
                        transferListener.onCancelAll(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_STORAGE_FULL:
                    FileTransferUtil.showStorespaceFullDialog(BaseService.this.getApplicationContext(), mReceiveDeviceCallBack);
                    break;
            }
        }
    };

    Handler mSenderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.READY_TO_EXIT:
                    if (!FileSendData.getInstance().isConnected()) {
                        if (!FileSendData.getInstance().isAllSendComplete()) {
                            LogUtil.i("服务端： 连接中断，传输失败,更新所有失败状态");
                            FileSendData.getInstance().updateDisconnectAllState();
                            FileSendData.getInstance().setAllSendComplete(true);
                            for (ITransferListener transferListener : mSendListeners) {
                                transferListener.onTransferAllComplete();
                            }
                            //判断当前是否正在接收文件，若没有接收则更新接收界面底部的menu
                            if(!FileReceiveData.getInstance().isReceiving()){
                                mReceiveDeviceCallBack.onRefreshMenu(true,true);
                            }
                        }
                        if (mSendDeviceCallBack != null) {
                            mSendDeviceCallBack.onExit();
                        }
                    }
                    break;
                case Constants.SENDER_SEND_FILE_LIST_SUCCESS:
                    notifySendWork();
                    for (ITransferListener transferListener : mSendListeners) {
                        transferListener.onReadFileListSuccess();
                    }
                    break;
                case Constants.SENDER_UPDATE_TRANSFER_PROGRESS:
                    LogUtil.i("SendFragment----SENDER_UPDATE_TRANSFER_PROGRESS");
                    for (ITransferListener transferListener : mSendListeners) {
                        LogUtil.i("SendFragment----SENDER_UPDATE_TRANSFER_PROGRESS----transferListener = "+transferListener);
                        transferListener.onUpdateTransferProgress(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_COMPLETE_BY_INDEX:
                    for (ITransferListener transferListener : mSendListeners) {
                        transferListener.onTransferCompleteByIndex(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_ALL_COMPLETE:
                    FileSendData.getInstance().setAllSendComplete(true);
                    for (ITransferListener transferListener : mSendListeners) {
                        transferListener.onTransferAllComplete();
                    }
                    //判断当前是否正在接收文件，若没有接收则更新接收界面底部的menu
                    if(!FileReceiveData.getInstance().isReceiving()){
                        mReceiveDeviceCallBack.onRefreshMenu(true,true);
                    }
                    break;
                case Constants.SENDER_TRANSFER_CANCEL_BY_INDEX:
                    for (ITransferListener transferListener : mSendListeners) {
                        transferListener.onCancelByIndex(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_CANCEL_ALL:
                    for (ITransferListener transferListener : mSendListeners) {
                        transferListener.onCancelAll(msg.arg1);
                    }
                    break;
            }
        }
    };

    public void reSend() {
        LogUtil.i("reSend");
        mSenderCmdRunnable.createWriteCommand(resendFileList());
        FileReceiveData.getInstance().setResend(true);
    }

    /**
     * 初始化要再发送的文件清单,及自己当前的设备信息
     */
    private ResponseInfo resendFileList() {
        ResponseInfo info = FileSendData.getInstance().getResponseList();
        info.command = Constants.SEND_DESCRIBE;
        LogUtil.i("resendFileList:  文件列表获得成功");
        return info;
    }

}
