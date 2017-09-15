package com.gionee.hotspottransmission.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.ArrayMap;

import com.gionee.hotspottransmission.bean.BaseReceiveData;
import com.gionee.hotspottransmission.bean.BaseSendData;
import com.gionee.hotspottransmission.bean.FileMultiReceiveData;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.callback.IMultiTransferService;
import com.gionee.hotspottransmission.callback.ITransferListener;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.runnable.multi.MultiBroadcastRunnable;
import com.gionee.hotspottransmission.runnable.multi.MultiCommandRunnable;
import com.gionee.hotspottransmission.runnable.multi.MultiReceiveRunnable;
import com.gionee.hotspottransmission.runnable.multi.MultiSendRunnable;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * Created by luorw on 5/26/17.
 */
public class MultiBaseService extends Service implements IMultiTransferService {
    public Map<String, ITransferListener> mSendListeners = new ArrayMap<>();
    public Map<String, ITransferListener> mReceiveListeners = new ArrayMap<>();
    public Map<String, MultiCommandRunnable> mCommandRunnables = new ArrayMap<>();
    public MultiBroadcastRunnable mMultiBroadcastRunnable;
    public IMultiDeviceCallBack multiDeviceCallBack;

    public MultiBaseService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setMultiDeviceCallBack(IMultiDeviceCallBack multiDeviceCallBack) {
        this.multiDeviceCallBack = multiDeviceCallBack;
    }

    public void registerSendListeners(String key,ITransferListener transferListener) {
        mSendListeners.put(key,transferListener);
    }

    public void registerReceiveListeners(String key,ITransferListener transferListener) {
        mReceiveListeners.put(key,transferListener);
    }

    public void unRegisterSendListener(String key) {
        mSendListeners.remove(key);
    }

    public void unRegisterReceiveListener(String key) {
        mReceiveListeners.remove(key);
    }

    @Override
    public void notifyOffline(){
        LogUtil.i("luorw1  , notifyOffline -------------  ");
        String imei = DeviceSp.getInstance().getDeviceAddress(this);
        String msg = Constants.MSG_OFFLINE + "," + DeviceSp.getInstance().getDeviceIp(this) + "," + DeviceSp.getInstance().getDeviceName(this) + "," + imei;
        mMultiBroadcastRunnable.createWriteRunnable(msg);
    }

    @Override
    public void createReadCommand(String key) {
        LogUtil.i("luorw,createReadCommand , key = " + key);
        if (!mCommandRunnables.containsKey(key)) {
            MultiCommandRunnable commandRunnable = new MultiCommandRunnable(this, mHandler);
            mCommandRunnables.put(key, commandRunnable);
            LogUtil.i("luorw,createReadCommand , mCommandRunnables.put");
            commandRunnable.createReadCommand(key);
        } else {
            LogUtil.i("luorw,createReadCommand , 当前读命令的线程已经启动过了");
        }
    }

    @Override
    public void closeAllCommandSocket() {
        LogUtil.i("luorw , closeAllCommandSocket ===="+mCommandRunnables.keySet().size());
        for(String key : mCommandRunnables.keySet()){
            LogUtil.i("luorw , closeAllCommandSocket  key===="+key);
            mCommandRunnables.get(key).closeCommandSocket();
        }
    }

    @Override
    public void createWriteCommand(String key, MultiCommandInfo info) {
        LogUtil.i("luorw,createWriteCommand , key = " + key);
        if (!mCommandRunnables.containsKey(key)) {
            MultiCommandRunnable commandRunnable = new MultiCommandRunnable(this, mHandler);
            mCommandRunnables.put(key, commandRunnable);
            LogUtil.i("luorw,createWriteCommand , mCommandRunnables.put");
        }
        mCommandRunnables.get(key).createWriteCommand(key, info);
    }

    /**
     * 启动接收文件的线程
     */
    @Override
    public void notifyReceiveWork(String key) {
        LogUtil.i("luorw , notifyReceiveWork---------key = " + key);
        MultiReceiveRunnable receiveRunnable = new MultiReceiveRunnable(this, mHandler, this, key);
        ThreadPoolManager.getInstance().executeRunnable(receiveRunnable);
    }

    /**
     * 获取接收文件的socket
     *
     * @return
     */
    @Override
    public Socket getReceiveFileSocket(String ip) {
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect((new InetSocketAddress(ip, Constants.RECEIVE_FILES_PORT)), 5000);
            return socket;
        } catch (Exception e) {
            LogUtil.i("luorw , getReceiveFileSocket: IOException = " + e.getMessage());
            e.printStackTrace();
            SystemClock.sleep(500);
            getReceiveFileSocket(ip);
            return null;
        }
    }

    /**
     * 读取接收方请求的对象，并且根据请求的下标进行发送文件
     */
    @Override
    public void notifySendWork(String key) {
        LogUtil.i("luorw , notifySendWork ,key = " + key);
        MultiSendRunnable sendRunnable = new MultiSendRunnable(this, mHandler, key);
        ThreadPoolManager.getInstance().executeRunnable(sendRunnable);
    }

    @Override
    public boolean isGroupOwner() {
        return false;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String key = msg.obj.toString();
            ITransferListener senderListener = mSendListeners.get(key);
            ITransferListener receiverListener = mReceiveListeners.get(key);
            switch (msg.what) {
                case Constants.RECEIVER_READ_FILE_LIST_SUCCESS:
                    LogUtil.i("luorw , RECEIVER_READ_FILE_LIST_SUCCESS");
                    multiDeviceCallBack.onAddReceive(key);
                    if(receiverListener != null){
                        receiverListener.onReadFileListSuccess();
                    }
                    notifyReceiveWork(key);
                    break;
                case Constants.RECEIVER_UPDATE_TRANSFER_PROGRESS:
                    if(receiverListener != null){
                        receiverListener.onUpdateTransferProgress(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_COMPLETE_BY_INDEX:
                    if(receiverListener != null){
                        receiverListener.onTransferCompleteByIndex(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_ALL_COMPLETE:
                    FileMultiReceiveData.getInstance().getFileReceiveData(key).setAllReceiveComplete(true);
                    if(receiverListener != null){
                        receiverListener.onTransferAllComplete();
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_CANCEL_BY_INDEX:
                    if(receiverListener != null){
                        receiverListener.onCancelByIndex(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_CANCEL_ALL:
                    FileMultiReceiveData.getInstance().getFileReceiveData(key).setCancelAllReceive(true);
                    if(receiverListener != null){
                        receiverListener.onCancelAll(msg.arg1);
                    }
                    break;
                case Constants.RECEIVER_TRANSFER_STORAGE_FULL:
                    FileTransferUtil.showStorespaceFullDialog(MultiBaseService.this, multiDeviceCallBack);
                    break;
                case Constants.READY_TO_EXIT:
                    BaseSendData multiSendData = FileMultiSendData.getInstance().getFileSendData(key);
                    BaseReceiveData multiReceiveData = FileMultiReceiveData.getInstance().getFileReceiveData(key);
                    if (!multiSendData.isConnected()) {
                        if (!multiSendData.isAllSendComplete()) {
                            LogUtil.i("服务端： 连接中断，传输失败,更新所有失败状态");
                            multiSendData.updateDisconnectAllState();
                            multiSendData.setAllSendComplete(true);
//                            mSendListeners.get(key).onTransferAllComplete();
                            //判断当前是否正在接收文件，若没有接收则更新接收界面底部的menu
                            if (!multiReceiveData.isReceiving()) {
                                multiDeviceCallBack.onRefreshMenu(true, true);
                            }
                        }
                        if (multiDeviceCallBack != null) {
//                            multiDeviceCallBack.offline();
                        }
                    }
                    break;
                case Constants.SENDER_SEND_FILE_LIST_SUCCESS:
                    if(senderListener != null){
                        senderListener.onReadFileListSuccess();
                    }
                    notifySendWork(key);
                    break;
                case Constants.SENDER_UPDATE_TRANSFER_PROGRESS:
                    LogUtil.i("SendFragment----SENDER_UPDATE_TRANSFER_PROGRESS----transferListener = " + mSendListeners.get(key));
                    if(senderListener != null){
                        senderListener.onUpdateTransferProgress(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_COMPLETE_BY_INDEX:
                    if(senderListener != null){
                        senderListener.onTransferCompleteByIndex(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_ALL_COMPLETE:
                    BaseSendData sendData = FileMultiSendData.getInstance().getFileSendData(key);
                    BaseReceiveData receiveData = FileMultiReceiveData.getInstance().getFileReceiveData(key);
                    sendData.setAllSendComplete(true);
                    if(senderListener != null){
                        senderListener.onTransferAllComplete();
                    }
                    //判断当前是否正在接收文件，若没有接收则更新接收界面底部的menu
//                    if (!receiveData.isReceiving()) {
//                        multiDeviceCallBack.onRefreshMenu(true, true);
//                    }
                    break;
                case Constants.SENDER_TRANSFER_CANCEL_BY_INDEX:
                    if(senderListener != null){
                        senderListener.onCancelByIndex(msg.arg1);
                    }
                    break;
                case Constants.SENDER_TRANSFER_CANCEL_ALL:
                    if(senderListener != null){
                        senderListener.onCancelAll(msg.arg1);
                    }
                    break;
            }
        }
    };

}
