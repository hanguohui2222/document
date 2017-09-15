package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.RequestInfo;
import com.gionee.hotspottransmission.bean.ResponseInfo;
import com.gionee.hotspottransmission.callback.IDeviceCallBack;
import com.gionee.hotspottransmission.callback.ITransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by luorw on 5/26/17.
 */
public class SenderCommandRunnable {
    private Context mContext;
    private ResponseInfo mResponseInfo;
    private Handler mHandler;
    private Socket mCmdSocket;
    private boolean isCommandAlive;
    private ITransferService mTransferServiceCallBack;
    private IDeviceCallBack mDeviceCallBack;

    public SenderCommandRunnable(Context context, Handler handler, ITransferService transferService, IDeviceCallBack deviceCallBack) {
        this.mHandler = handler;
        this.mContext = context.getApplicationContext();
        this.mTransferServiceCallBack = transferService;
        this.mDeviceCallBack = deviceCallBack;
    }

    /**
     * 创建写命令线程，主要用于开始发送传输文件描述信息
     */
    public void createWriteCommand(ResponseInfo info) {
        ThreadPoolManager.getInstance().executeRunnable(new WriteCommandRunnable(info));
        FileSendData.getInstance().setSending(true);
    }

    /**
     * 创建写命令线程，主要用于用户点击取消，全部取消，文件传输全部完成
     */
    public void createWriteCommand(int command) {
        ThreadPoolManager.getInstance().executeRunnable(new WriteCommandRunnable(command));
    }

    /**
     * 创建读命令线程，主要用于读取待接收的文件描述，对方点击取消，对方点击全部取消
     */
    public void createReadCommand() {
        ThreadPoolManager.getInstance().executeRunnable(new ReadCommandRunnable());
    }

    /**
     * 写命令线程
     */
    private class WriteCommandRunnable implements Runnable {
        //全部取消 -3,取消单个 index,发送文件列表 -1
        private int command;

        private WriteCommandRunnable(ResponseInfo info) {
            this.command = info.command;
            mResponseInfo = info;
        }

        private WriteCommandRunnable(int command) {
            this.command = command;
        }

        @Override
        public void run() {
            LogUtil.i("SenderCommandRunnable,mCmdSocket = " + mCmdSocket + "command = " + command);
            if (mCmdSocket != null) {
                writeCommand();
            }
        }

        private void writeCommand() {
            try {
                OutputStream out = mCmdSocket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out);
                switch (command) {
                    case Constants.CANCEL_ALL_TAG:
                        mResponseInfo.command = Constants.CANCEL_ALL_TAG;
                        mResponseInfo.index = FileSendData.getInstance().getmCurrentSendIndex();
                        oos.writeObject(mResponseInfo);
                        LogUtil.i("SenderCommandRunnable:  全部取消发送");
                        break;
                    case Constants.SEND_DESCRIBE:
                        FileSendData.getInstance().setAllSendComplete(false);
                        FileSendData.getInstance().setCancelAllSend(false);
                        LogUtil.i("SenderCommandRunnable:  发送文件列表开始");
                        mResponseInfo.command = Constants.SEND_DESCRIBE;
                        oos.writeObject(mResponseInfo);
                        LogUtil.i("SenderCommandRunnable:  发送文件列表成功");
                        refreshUI(-1, Constants.SENDER_SEND_FILE_LIST_SUCCESS);
                        break;
                    case Constants.SEND_FAIL:
                        LogUtil.i("SenderCommandRunnable:  发送文件失败");
//                        mResponseInfo.command = Constants.SEND_FAIL;
//                        oos.writeObject(mResponseInfo);
                        break;
                    default:
                        LogUtil.i("SenderCommandRunnable:  发送取消index开始 and index=" + command);
                        ResponseInfo pInfo = new ResponseInfo();
                        pInfo.index = command;
                        pInfo.command = Constants.CANCEL_BY_INDEX;
                        oos.writeObject(pInfo);
                        LogUtil.i("SenderCommandRunnable:  发送取消index成功");
                        break;
                }
            } catch (IOException e) {
                LogUtil.i("SenderCommandRunnable,服务端:  命令socket , e = " + e.getMessage());
                e.printStackTrace();
                //added by luorw for GNSPR #44461 20160908 begin
                //命令sockect发生异常，更新状态，结束本次传输（对方已经结束本次传输而自己却没有断开）
                if (e.getMessage().contains("EPIPE")) {
                    closeCommandSocket();
                    if (!FileSendData.getInstance().isAllSendComplete()) {
                        LogUtil.i("SenderCommandRunnable,服务端： 同步与客户端的断开状态");
                        if (mDeviceCallBack != null) {
                            mDeviceCallBack.onExit();
                        }
                        //added by luorw for GNSPR #31956 20160715 begin
                        FileSendData.getInstance().setConnected(false);
                        //added by luorw for GNSPR #31956 20160715 end
                        FileSendData.getInstance().updateDisconnectAllState();
                        refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    }
                }
                //added by luorw for GNSPR #44461 20160908 end
            }
        }
    }

    /**
     * 命令线程
     */
    private class ReadCommandRunnable implements Runnable {
        public ReadCommandRunnable() {
            isCommandAlive = true;
        }

        @Override
        public void run() {
//            SystemClock.sleep(1000);
//            while (mCmdSocket == null){
//                LogUtil.i("luorw服务端:   mCmdSocket null");
            mCmdSocket = mTransferServiceCallBack.getSendCommandSocket();
//            }
            FileSendData.getInstance().setConnected(true);
            LogUtil.i("SenderCommandRunnable:   命令Socket readObject mCmdSocket = " + mCmdSocket);
            if (FileSendData.getInstance().getFileSendList().size() != 0) {
                createWriteCommand(getFileSendInfo());
            }
            readCommand();
        }

        public void readCommand() {
            while (isCommandAlive && mCmdSocket != null) {
                try {
                    LogUtil.i("SenderCommandRunnable:   命令Socket readObject");
                    InputStream is = mCmdSocket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    RequestInfo requestInfo = (RequestInfo) ois.readObject();
                    LogUtil.i("SenderCommandRunnable:   命令ServerSocket 发现请求 requestInfo :" + requestInfo.toString());
                    LogUtil.i("SenderCommandRunnable:   请求命令为  :" + requestInfo.command);
                    switch (requestInfo.command) {
                        case Constants.CANCEL_BY_INDEX:
                            refreshUI(requestInfo.index, Constants.SENDER_TRANSFER_CANCEL_BY_INDEX);
                            LogUtil.i("SenderCommandRunnable,服务端:   收到取消index and index=" + requestInfo.index);
                            break;
                        case Constants.CANCEL_ALL_TAG:
                            FileSendData.getInstance().setCancelAllSend(true);
                            refreshUI(requestInfo.index, Constants.SENDER_TRANSFER_CANCEL_ALL);
                            LogUtil.i("SenderCommandRunnable,服务端:   收到全部取消");
                            break;
                        case Constants.SEND_ALL_COMPLETE:
                            refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                            LogUtil.i("SenderCommandRunnable,服务端:   收到全部");
                            break;
                    }
                } catch (ClassNotFoundException e) {
                    LogUtil.i("SenderCommandRunnable,服务端: ClassNotFoundException 命令ServerSocket 错误 如下:" + e.getMessage());
                    e.printStackTrace();
                    //added by luorw for GNSPR #14413 20160606 begin
                    closeCommandSocket();
                    //added by luorw for GNSPR #14413 20160606 end
                    if (!FileSendData.getInstance().isAllSendComplete()) {
                        LogUtil.i("SenderCommandRunnable,服务端： 连接中断，传输失败,更新所有失败状态");
                        if (mDeviceCallBack != null) {
                            mDeviceCallBack.onExit();
                        }
                        //added by luorw for GNSPR #31956 20160715 begin
                        FileSendData.getInstance().setConnected(false);
                        //added by luorw for GNSPR #31956 20160715 end
                        FileSendData.getInstance().updateDisconnectAllState();
                        refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    }
                } catch (IOException e) {
                    LogUtil.i("SenderCommandRunnable,服务端: IOException  命令ServerSocket 错误 如下:" + e.getMessage());
                    e.printStackTrace();
                    //added by luorw for GNSPR #14413 20160606 begin
                    closeCommandSocket();
                    //added by luorw for GNSPR #14413 20160606 end
                    if (!FileSendData.getInstance().isAllSendComplete()) {
                        LogUtil.i("SenderCommandRunnable,服务端： 连接中断，传输失败,更新所有失败状态");
                        if (mDeviceCallBack != null) {
                            mDeviceCallBack.onExit();
                        }
                        //added by luorw for GNSPR #31956 20160715 begin
                        FileSendData.getInstance().setConnected(false);
                        //added by luorw for GNSPR #31956 20160715 end
                        FileSendData.getInstance().updateDisconnectAllState();
                        refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    }
                }
            }

        }
    }

    /**
     * 初始化要发送的文件清单
     */
    private ResponseInfo getFileSendInfo() {
        ResponseInfo info = FileSendData.getInstance().getResponseList();
        info.command = Constants.SEND_DESCRIBE;
        LogUtil.i("SenderCommandRunnable,服务端:  文件列表获得成功");
        return info;
    }

    private void closeCommandSocket() {
        LogUtil.i("SenderCommandRunnable,服务端:   closeAllCommandSocket , bCommandAlive = " + isCommandAlive);
        if (isCommandAlive) {
            isCommandAlive = false;
            try {
                if (mCmdSocket != null) {
                    LogUtil.i("SenderCommandRunnable,服务端:   clientCommand.shutdown");
                    mCmdSocket.shutdownInput();
                    mCmdSocket.shutdownOutput();
                }
                if (mCmdSocket != null) {
                    LogUtil.i("luorw , SenderCommandRunnable,服务端:   mCommandSocket.close()");
                    mCmdSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}
