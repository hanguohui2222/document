package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.RequestInfo;
import com.gionee.hotspottransmission.bean.ResponseInfo;
import com.gionee.hotspottransmission.callback.ITransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ReceivedImageSourceManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
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
public class ReceiverCommandRunnable {
    private boolean isReadCmdAlive;
    private Socket mCommandSocket;
    private Handler mHandler;
    private Context mContext;
    private ITransferService mTransferServiceCallBack;

    public ReceiverCommandRunnable(Context context, Handler handler, ITransferService transferService) {
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
        this.mTransferServiceCallBack = transferService;
    }

    /**
     * 读命令线程
     */
    public class ReadCommandRunnable implements Runnable {

        public ReadCommandRunnable() {
            isReadCmdAlive = true;
        }

        @Override
        public void run() {
            SystemClock.sleep(1000);
            while (mCommandSocket == null){
                LogUtil.i("ReceiverCommandRunnable:   mCommandSocket null");
                mCommandSocket = mTransferServiceCallBack.getReceiveCommandSocket();
            }
            LogUtil.i("ReceiverCommandRunnable:   命令Socket readObject mCommandSocket = "+mCommandSocket);
            FileReceiveData.getInstance().setConnected(true);
            if(mCommandSocket != null){
                readCommand();
            }
        }

        public void readCommand() {
            while (isReadCmdAlive) {
                try {
                    LogUtil.i("ReceiverCommandRunnable:   命令Socket readObject");
                    InputStream is = mCommandSocket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    ResponseInfo responseInfo = (ResponseInfo) ois.readObject();
                    LogUtil.i("ReceiverCommandRunnable:   命令Socket 发现请求 responseInfo :" + responseInfo.toString());
                    LogUtil.i("ReceiverCommandRunnable:   请求命令为  :" + responseInfo.command);
                    switch (responseInfo.command) {
                        case Constants.SEND_DESCRIBE:
                            FileReceiveData.getInstance().setReceiving(true);
                            readFileListSuccess(responseInfo);
                            mHandler.sendEmptyMessageDelayed(Constants.NOTIFY_RECEIVE_WORK, 300);
                            break;
                        case Constants.CANCEL_BY_INDEX:
                            refreshUI(responseInfo.index, Constants.RECEIVER_TRANSFER_CANCEL_BY_INDEX);
                            LogUtil.i("ReceiverCommandRunnable:   收到取消index and index=" + responseInfo.index);
                            break;
                        case Constants.CANCEL_ALL_TAG:
                            FileReceiveData.getInstance().setCancelAllReceive(true);
                            refreshUI(responseInfo.index, Constants.RECEIVER_TRANSFER_CANCEL_ALL);
                            LogUtil.i("ReceiverCommandRunnable:   收到全部取消");
                            break;
                        case Constants.SEND_FAIL:
//                            isCurrentReceiveFail = true;
                            LogUtil.i("ReceiverCommandRunnable:   发送失败");
                            break;
                    }
                } catch (ClassNotFoundException e) {
                    LogUtil.i("ReceiverCommandRunnable:  ClassNotFoundException命令Socket 错误 " + e.getMessage());
                    closeCommandSocket();
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.i("ReceiverCommandRunnable:  IOException命令Socket 错误 " + e.getMessage());
                    closeCommandSocket();
                    e.printStackTrace();
                }
            }
        }
    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private void closeCommandSocket() {
        isReadCmdAlive = false;
        try {
            mCommandSocket.shutdownInput();
            mCommandSocket.shutdownOutput();
            mCommandSocket.close();
            LogUtil.i("luorw , ReceiverCommandRunnable,closeAllCommandSocket----------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得文件列表成功
     *
     * @param responseInfo
     */
    private void readFileListSuccess(ResponseInfo responseInfo) {
        LogUtil.i("ReceiverCommandRunnable,获取信息");
        ReceivedImageSourceManager.getInstance(mContext).clearAllReceivedImagePath();
        ReceivedImageSourceManager.getInstance(mContext).clearAllReceivedImageTitle();
        FileReceiveData.getInstance().clearAllFiles();
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        fileInfos.addAll(responseInfo.filesList);
        FileReceiveData.getInstance().setFileReceiveList(fileInfos);
        FileReceiveData.getInstance().calculateAllFileSize();
        LogUtil.i("ReceiverCommandRunnable,客户端:   读取文件列表成功");
        mHandler.sendEmptyMessage(Constants.RECEIVER_READ_FILE_LIST_SUCCESS);
    }

    /**
     * 创建写命令线程，主要用于用户点击取消，全部取消，文件传输全部完成
     *
     * @param command
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
    public class WriteCommandRunnable implements Runnable {
        //全部取消 -3,取消单个 index;全部接收完成 -6
        private int command;

        private WriteCommandRunnable(int command) {
            this.command = command;
        }

        @Override
        public void run() {
            LogUtil.i("ReceiverCommandRunnable:  mCommandSocket = "+mCommandSocket + "command = "+command);
            if(mCommandSocket == null){
                return;
            }
            try {
                RequestInfo requestInfo = new RequestInfo();
                requestInfo.command = command;
                switch (command) {
                    case Constants.CANCEL_ALL_TAG:
                        requestInfo.index = FileReceiveData.getInstance().getmCurrentReceiveIndex();
                        LogUtil.i("ReceiverCommandRunnable:  全部取消");
                        break;
                    case Constants.SEND_DESCRIBE:
                        FileReceiveData.getInstance().setCancelAllReceive(false);
                        FileReceiveData.getInstance().setAllReceiveComplete(false);
                        LogUtil.i("ReceiverCommandRunnable:  请求获取文件列表");
                        break;
                    case Constants.SEND_ALL_COMPLETE:
                        LogUtil.i("ReceiverCommandRunnable,客户端:  全部传输完成");
                        break;
                    default:
                        LogUtil.i("ReceiverCommandRunnable:  取消第" + command + "个文件");
                        requestInfo.index = command;
                        requestInfo.command = Constants.CANCEL_BY_INDEX;
                        break;
                }
                OutputStream out = mCommandSocket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(requestInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
