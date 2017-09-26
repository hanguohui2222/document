package com.gionee.hotspottransmission.runnable.multi;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.gionee.hotspottransmission.bean.BaseReceiveData;
import com.gionee.hotspottransmission.bean.BaseSendData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiReceiveData;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.callback.IMultiTransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ReceivedImageSourceManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by luorw on 5/25/17.
 */
public class MultiCommandRunnable {
    private static final String TAG = "MultiCommandRunnable";
    private Handler mHandler;
    private Context mContext;
    private ReadCommandRunnable mReadCmdRunnable;
    private Socket mCommandSocket;
    private boolean isReadCmdAlive;
    private String mKey;

    public MultiCommandRunnable(Context context, Handler handler) {
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
    }

    /**
     * 读命令线程
     */
    public class ReadCommandRunnable implements Runnable {


        public ReadCommandRunnable(String key) {
            mKey = key;
            isReadCmdAlive = true;
            mCommandSocket = SocketChannel.getInstance().mCommendSockets.get(key);
        }

        @Override
        public void run() {
            LogUtil.i(TAG + "luorw  命令Socket readObject mCommandSocket = " + mCommandSocket);
//            FileReceiveData.getInstance().setConnected(true);
            if (mCommandSocket != null) {
                readCommand();
            }
        }

        public void readCommand() {
            while (isReadCmdAlive) {
                try {
                    LogUtil.i("luorw,MultiCommandRunnable:   命令Socket readObject");
                    InputStream is = mCommandSocket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    MultiCommandInfo multiCommandInfo = (MultiCommandInfo) ois.readObject();
                    LogUtil.i("luorw,MultiCommandRunnable:   命令Socket 发现请求 responseInfo :" + multiCommandInfo.toString());
                    String key = multiCommandInfo.responseDeviceImei;
                    if (filterSelf(key)) {
                        LogUtil.i("luorw,createReadCommand , filterSelf------------");
                        continue;
                    }
                    switch (multiCommandInfo.command) {
                        case Constants.SENDER_SEND_DESCRIBE:
                            LogUtil.i("luorw,MultiCommandRunnable:   SENDER_SEND_DESCRIBE");
                            readFileListSuccess(multiCommandInfo);
                            FileMultiReceiveData.getInstance().getFileReceiveData(key).setReceiving(true);
                            FileMultiReceiveData.getInstance().getFileReceiveData(key).setConnected(true);
                            refreshUI(-1, Constants.RECEIVER_READ_FILE_LIST_SUCCESS, key);
                            break;
                        case Constants.SENDER_CANCEL_BY_INDEX:
                            refreshUI(multiCommandInfo.responseIndex, Constants.RECEIVER_TRANSFER_CANCEL_BY_INDEX, key);
                            LogUtil.i("luorw,MultiCommandRunnable:   收到SENDER_CANCEL_BY_INDEX index=" + multiCommandInfo.responseIndex);
                            break;
                        case Constants.SENDER_CANCEL_ALL:
                            FileMultiReceiveData.getInstance().getFileReceiveData(key).setCancelAllReceive(true);
                            refreshUI(multiCommandInfo.responseIndex, Constants.RECEIVER_TRANSFER_CANCEL_ALL, key);
                            LogUtil.i("luorw,MultiCommandRunnable:   收到SENDER_CANCEL_ALL");
                            break;
                        case Constants.RECEIVER_CANCEL_BY_INDEX:
                            refreshUI(multiCommandInfo.requestIndex, Constants.SENDER_TRANSFER_CANCEL_BY_INDEX, key);
                            LogUtil.i("luorw,MultiCommandRunnable:   收到RECEIVER_CANCEL_BY_INDEX index=" + multiCommandInfo.requestIndex);
                            break;
                        case Constants.RECEIVER_CANCEL_ALL:
                            FileMultiSendData.getInstance().getFileSendData(key).setCancelAllSend(true);
                            refreshUI(multiCommandInfo.requestIndex, Constants.SENDER_TRANSFER_CANCEL_ALL, key);
                            LogUtil.i("luorw,MultiCommandRunnable:   收到RECEIVER_CANCEL_ALL");
                            break;
//                        case Constants.RECEIVER_RECEIVE_OVER:
//                            refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE, key);
//                            LogUtil.i("luorw,MultiCommandRunnable:   收到RECEIVER_RECEIVE_OVER");
//                            break;
                    }
                } catch (ClassNotFoundException e) {
                    LogUtil.i("luorw,MultiCommandRunnable:  ClassNotFoundException命令Socket 错误 " + e.getMessage());
                    closeCommandSocket();
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.i("luorw,MultiCommandRunnable:  IOException命令Socket 错误 " + e.getMessage());
                    closeCommandSocket();
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeCommandSocket() {
        isReadCmdAlive = false;
        try {
            mCommandSocket.shutdownInput();
            mCommandSocket.shutdownOutput();
            mCommandSocket.close();
            LogUtil.i("luorw,MultiCommandRunnable:  closeCommandSocket ----------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshUI(int index, int what, String key) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        msg.obj = key;
        mHandler.sendMessage(msg);
    }


    /**
     * 取得文件列表成功
     *
     * @param info
     */
    private void readFileListSuccess(MultiCommandInfo info) {
        LogUtil.i("luorw,MultiCommandRunnable,获取信息");
        ReceivedImageSourceManager.getInstance(mContext).clearAllReceivedImagePath();
        ReceivedImageSourceManager.getInstance(mContext).clearAllReceivedImageTitle();
        BaseReceiveData fileReceiveData = FileMultiReceiveData.getInstance().getFileReceiveData(info.responseDeviceImei);
        if (fileReceiveData != null) {
            LogUtil.i("luorw,MultiCommandRunnable,再次接收同一设备的文件----new BaseReceiveData()");
            FileMultiReceiveData.getInstance().removeFileReceiveData(info.responseDeviceImei);
        }
        fileReceiveData = new BaseReceiveData();
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        fileInfos.addAll(info.responseFilesList);
        fileReceiveData.setFileReceiveList(fileInfos);
        fileReceiveData.calculateAllFileSize();
        FileMultiReceiveData.getInstance().setFileReceiveData(info.responseDeviceImei, fileReceiveData);
        LogUtil.i("luorw,MultiCommandRunnable,客户端:   读取文件列表成功  key = " + info.responseDeviceImei);
    }

    /**
     * 创建写命令线程，主要用于开始发送传输文件描述信息,请求文件
     */
    public void createWriteCommand(String key, MultiCommandInfo info) {
        LogUtil.i("luorw , MultiCommandRunnable , createWriteCommand");
        ThreadPoolManager.getInstance().executeRunnable(new WriteCommandRunnable(key, info));
        if (info.command == Constants.SENDER_SEND_DESCRIBE) {
            BaseSendData baseSendData = FileMultiSendData.getInstance().getFileSendData(key);
            baseSendData.setSending(true);
            baseSendData.setConnected(true);
        }
    }

    /**
     * 创建读命令线程，主要用于读取待接收的文件描述，对方点击取消，对方点击全部取消
     */
    public void createReadCommand(String key) {
        if (mReadCmdRunnable != null) {
            LogUtil.i("luorw , MultiCommandRunnable , createReadCommand , mReadCmdRunnable != null");
            mReadCmdRunnable.readCommand();
        } else {
            LogUtil.i("luorw , MultiCommandRunnable , createReadCommand , mReadCmdRunnable == null");
            mReadCmdRunnable = new ReadCommandRunnable(key);
            ThreadPoolManager.getInstance().executeRunnable(mReadCmdRunnable);
        }
    }

    /**
     * 写命令线程
     */
    public class WriteCommandRunnable implements Runnable {
        private MultiCommandInfo mCommandInfo;
        private Socket mCommandSocket;
        private String key;//将命令写给对方，key为对方的imei号

        private WriteCommandRunnable(String key, MultiCommandInfo commandInfo) {
            this.mCommandInfo = commandInfo;
            this.mCommandSocket = SocketChannel.getInstance().mCommendSockets.get(key);
            this.key = key;
        }

        @Override
        public void run() {
            LogUtil.i("luorw , MultiCommandRunnable:  mCommandSocket = " + mCommandSocket + "command = " + mCommandInfo.command);
            if (mCommandSocket == null) {
                return;
            }
            try {
                switch (mCommandInfo.command) {
                    case Constants.RECEIVER_CANCEL_ALL:
                        mCommandInfo.requestIndex = FileMultiReceiveData.getInstance().getFileReceiveData(key).getmCurrentReceiveIndex();
                        LogUtil.i("luorw , MultiCommandRunnable:  RECEIVER_CANCEL_ALL");
                        break;
                    case Constants.RECEIVER_REQUEST_FILE:
                        FileMultiReceiveData.getInstance().getFileReceiveData(key).setCancelAllReceive(false);
                        FileMultiReceiveData.getInstance().getFileReceiveData(key).setAllReceiveComplete(false);
                        LogUtil.i("luorw , MultiCommandRunnable:  RECEIVER_REQUEST_FILE , index = " + mCommandInfo.requestIndex);
                        break;
//                    case Constants.RECEIVER_RECEIVE_OVER:
//                        LogUtil.i("luorw , MultiCommandRunnable:  RECEIVER_RECEIVE_OVER");
//                        break;
                    case Constants.SENDER_CANCEL_ALL:
                        mCommandInfo.responseIndex = FileMultiSendData.getInstance().getFileSendData(key).getmCurrentSendIndex();
                        LogUtil.i("luorw , MultiCommandRunnable:  SENDER_CANCEL_ALL");
                        break;
                    case Constants.SENDER_SEND_DESCRIBE:
                        FileMultiSendData.getInstance().getFileSendData(key).setAllSendComplete(false);
                        FileMultiSendData.getInstance().getFileSendData(key).setCancelAllSend(false);
                        LogUtil.i("luorw , MultiCommandRunnable:  发送文件列表开始");
                        refreshUI(-1, Constants.SENDER_SEND_FILE_LIST_SUCCESS, key);
                        break;
                    default:
                        if (mCommandInfo.option == Constants.OP_SEND) {
                            mCommandInfo.responseIndex = mCommandInfo.command;
                            mCommandInfo.command = Constants.SENDER_CANCEL_BY_INDEX;
                            LogUtil.i("luorw , MultiCommandRunnable:  SENDER取消第" + mCommandInfo.command + "个文件");
                        } else if (mCommandInfo.option == Constants.OP_RECEIVE) {
                            mCommandInfo.requestIndex = mCommandInfo.command;
                            mCommandInfo.command = Constants.RECEIVER_CANCEL_BY_INDEX;
                            LogUtil.i("luorw , MultiCommandRunnable:  RECEIVER取消第" + mCommandInfo.command + "个文件");
                        }
                        break;
                }
                //此处注意：写命令时必须将自己的imei号作为key写过去，这样对方读的时候才能对应
                mCommandInfo.responseDeviceImei = DeviceSp.getInstance().getDeviceAddress(mContext);
                OutputStream out = mCommandSocket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(mCommandInfo);
                LogUtil.i("luorw , MultiCommandRunnable:  write Object : " + mCommandInfo.toString());
            } catch (IOException e) {
                LogUtil.i("luorw , MultiCommandRunnable:  writeObject e = " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private boolean filterSelf(String imei) {
        return TextUtils.equals(imei, DeviceSp.getInstance().getDeviceAddress(mContext));
    }
}
