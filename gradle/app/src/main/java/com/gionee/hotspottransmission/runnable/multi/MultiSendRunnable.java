package com.gionee.hotspottransmission.runnable.multi;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.gionee.hotspottransmission.bean.BaseSendData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.bean.RequestInfo;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.IMultiTransferService;
import com.gionee.hotspottransmission.callback.ITransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by luorw on 5/26/17.
 */
public class MultiSendRunnable implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private FileInfo mFileInfo;
    private String mKey;
    private BaseSendData mSendData;
    private ServerSocket mServerSocket;
    private boolean isTransferAlive;
    private InputStream inputStream;
    private OutputStream outputStream;

    public MultiSendRunnable(Context context, Handler handler, String key) {
        isTransferAlive = true;
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
        this.mKey = key;
        mServerSocket = SocketChannel.getInstance().getmServerSocket();
        mSendData = FileMultiSendData.getInstance().getFileSendData(mKey);
    }

    @Override
    public void run() {
        LogUtil.i("luorw , MultiSendRunnable:   开始发送文件 , mKey = " + mKey + " , mServerSocket = "+mServerSocket);
        work();
    }

    private void work() {
        MultiCommandInfo requestInfo = null;
        Socket socket = null;
        try {
            LogUtil.i("luorw , MultiSendRunnable,服务端： 传输ServerSocket 正在运行");
            socket = mServerSocket.accept();
            LogUtil.i("luorw , MultiSendRunnable:   建立传输ServerSocket 成功");
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (Exception e) {
            LogUtil.i("luorw , MultiSendRunnable,服务端： Exception = "+e.getMessage());
            e.printStackTrace();
        }
        while (isTransferAlive) {
            //读取客户端请求的文件描述
            try {
                requestInfo = getRequestInfo();
                if (requestInfo == null) {
                    LogUtil.i("luorw , MultiSendRunnable:   传输请求内容:  requestInfo null");
                    continue;
                }
                LogUtil.i("luorw , MultiSendRunnable:   传输请求内容:  " + requestInfo.toString());
                if(Constants.RECEIVER_REQUEST_FILE == requestInfo.command){
                    mSendData = FileMultiSendData.getInstance().getFileSendData(mKey);
                    sendFile(requestInfo.requestIndex, socket);
                }
                if(Constants.RECEIVER_RECEIVE_OVER == requestInfo.command){
                    LogUtil.i("luorw , MultiSendRunnable:   收到接收方接收完毕的命令，退出发送循环  ");
                    isTransferAlive = false;
                    refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    break;
                }
            } catch (Exception e) {
                LogUtil.i("luorw , MultiSendRunnable,Exception发送： e = " + e.getMessage());
                if (mFileInfo != null && mFileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && mFileInfo.getState() != Constants.FILE_TRANSFER_SUCCESS && !mSendData.isCancelAllSend()) {
                    LogUtil.i("luorw , MultiSendRunnable,Exception服务端： 连接中断，传输失败 " + mFileInfo.getFileName() + ",e = " + e.getMessage());
                    updateInfo(Constants.FILE_TRANSFER_FAILURE);
                }
                e.printStackTrace();
            } finally {
                if (requestInfo != null) {
                    refreshUI(requestInfo.requestIndex, Constants.SENDER_TRANSFER_COMPLETE_BY_INDEX);
                }
                //如果对方中断了传输，则更新所有传输的状态，刷新UI，最后stopservice
                LogUtil.i("luorw , MultiSendRunnable,bConnected = " + mSendData.isConnected() + " , isAllComplete = " + mSendData.isAllSendComplete());
                if (!mSendData.isConnected()) {
                    if (!mSendData.isAllSendComplete()) {
                        LogUtil.i("SenderTransferRunnable,服务端： 连接中断，传输失败,更新所有失败状态");
                        mSendData.updateDisconnectAllState();
                        refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    }
                }
            }
        }
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对方请求的信息
     *
     * @param
     * @return
     */
    private MultiCommandInfo getRequestInfo() {
        MultiCommandInfo rInfo = null;
        try {
            LogUtil.i("luorw , MultiSendRunnable,发送端： 读取对方请求文件的信息");
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            rInfo = (MultiCommandInfo) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rInfo;
    }

    private void sendFile(int index ,Socket socket) throws IOException {
        LogUtil.i("luorw , MultiSendRunnable,服务端:   开始发送文件 index=" + index);
        long begin = System.currentTimeMillis();
        LogUtil.i("luorwTime,MultiSendRunnable , 发送文件 , " + index + " begin = " + begin + " , socket = "+socket);
        mFileInfo = mSendData.getFileSendList().get(index);
        if (mFileInfo.getState() == Constants.FILE_TRANSFER_CANCEL) {
            return;
        }
        updateInfo(Constants.FILE_TRANSFERING);
        mSendData.setmCurrentSendIndex(index);
        LogUtil.i("luorw , SendFragment---refreshUI---SENDER_UPDATE_TRANSFER_PROGRESS");
        refreshUI(index, Constants.SENDER_UPDATE_TRANSFER_PROGRESS);
        LogUtil.i("luorw , MultiSendRunnable,SEND_FILE,mFileInfo = " + mFileInfo + " , .getUriString() = " + mFileInfo.getUriString() + ",size = " + mFileInfo.
                getFileSize());
        InputStream is = mContext.getContentResolver().openInputStream(Uri.parse(mFileInfo.getUriString()));
        int length = 2048;
        byte[] buff = new byte[length];
        int len = 0;
        long transferSize = 0;
        while ((len = is.read(buff)) != -1) {
            if (mSendData.isCancelAllSend()) {
                LogUtil.i("luorw , MultiSendRunnable,服务端:  receive: 全部取消");
                mFileInfo.setState(Constants.FILE_TRANSFER_CANCEL);
            }
            //用户取消正在发送的文件
            if (mFileInfo.getState() == Constants.FILE_TRANSFER_CANCEL) {
                LogUtil.i("luorw , MultiSendRunnable,服务端:  receive: 取消 ");
                mSendData.updateAllFileSize(mFileInfo.getFileSize() - transferSize);
                break;
            }
            outputStream.write(buff, 0, len);
            outputStream.flush();
            transferSize += len;
            mSendData.setFileTransferSize(index, transferSize);
            mSendData.setTotalTransferedSize(len);
        }
        is.close();
        if (mFileInfo.getState() != Constants.FILE_TRANSFER_CANCEL) {
            mSendData.setmCurrentSendIndex(-1);
            LogUtil.i("luorw , MultiSendRunnable 服务端:  发送文件name=" + mFileInfo.getFileName() + "成功");
            mFileInfo.setState(Constants.FILE_TRANSFER_SUCCESS);
        }
        LogUtil.i("luorw , MultiSendRunnable,服务端:  发送文件index=" + index + "成功");
        long end = System.currentTimeMillis();
        LogUtil.i("luorwTime,MultiSendRunnable,发送文件 , " + index + " end = " + end);
        LogUtil.i("luorwTime,MultiSendRunnable,发送文件 , " + index + " 耗时 = " + (end - begin));
    }

    public void updateInfo(int dis) {
        LogUtil.i("luorw , updateInfo ---------------");
        mFileInfo.setState(dis);
    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.obj = mKey;
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}
