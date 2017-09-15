package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.RequestInfo;
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
public class SenderTransferRunnable implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private boolean bTransferAlive;
    private ITransferService mTransferServiceCallBack;
    private FileInfo mFileInfo;
    private ServerSocket mServerSocket;

    public SenderTransferRunnable(Context context, Handler handler, ITransferService transferServiceCallBack) {
        bTransferAlive = true;
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
        this.mTransferServiceCallBack = transferServiceCallBack;
    }

    @Override
    public void run() {
        try {
            LogUtil.i("SenderTransferRunnable:   run");
            mServerSocket = new ServerSocket();
            mServerSocket.setReuseAddress(true);
            mServerSocket.bind(new InetSocketAddress(Constants.SEND_FILES_PORT));
            work();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.i("SenderTransferRunnable:   传输ServerSocket IOException : " + e.getMessage());
        }
    }

    private void work() {
        RequestInfo requestInfo = null;
        while (bTransferAlive) {
            //读取客户端请求的文件描述
            try {
                LogUtil.i("SenderTransferRunnable,服务端： 传输ServerSocket 正在运行");
                Socket socket = mServerSocket.accept();
                LogUtil.i("SenderTransferRunnable:   建立传输ServerSocket 成功");
                requestInfo = getRequestInfo(socket.getInputStream());
                if (requestInfo == null) {
                    LogUtil.i("SenderTransferRunnable:   传输请求内容:  requestInfo null");
                    continue;
                }
                LogUtil.i("SenderTransferRunnable:   传输请求内容:  " + requestInfo.toString());
                LogUtil.i("SenderTransferRunnable:   传输请求命令:    " + requestInfo.command + "");
                switch (requestInfo.command) {
                    case Constants.SEND_FILE:
                        //发送文件
                        sendFileByRequestInfo(requestInfo, socket);
                        break;
                }
            } catch (Exception e) {
                LogUtil.i("luorw , SenderTransferRunnable,Exception发送： e = " + e.getMessage());
                if (mFileInfo != null && mFileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && mFileInfo.getState() != Constants.FILE_TRANSFER_SUCCESS && !FileSendData.getInstance().isCancelAllSend()) {
                    LogUtil.i("SenderTransferRunnable,Exception服务端： 连接中断，传输失败 " + mFileInfo.getFileName() + ",e = " + e.getMessage());
                    updateInfo(Constants.FILE_TRANSFER_FAILURE);
//                    //在p2p连接没有中断前提下，发送方socket异常中断而接收方并没有中断，需要发送方发送一个中断的指令GNSPR #17174
//                    if (e.getMessage().contains("ETIMEDOUT")) {
//                        createCommandOut(Constants.SEND_FAIL);
//                    }
                }
                e.printStackTrace();
            } finally {
                if (requestInfo != null) {
                    refreshUI(requestInfo.index, Constants.SENDER_TRANSFER_COMPLETE_BY_INDEX);
                }
                //如果对方中断了传输，则更新所有传输的状态，刷新UI，最后stopservice
                LogUtil.i("SenderTransferRunnable,bConnected = " + FileSendData.getInstance().isConnected() + " , isAllComplete = " + FileSendData.getInstance().isAllSendComplete());
                if (!FileSendData.getInstance().isConnected()) {
                    if (!FileSendData.getInstance().isAllSendComplete()) {
                        LogUtil.i("SenderTransferRunnable,服务端： 连接中断，传输失败,更新所有失败状态");
                        FileSendData.getInstance().updateDisconnectAllState();
                        refreshUI(-1, Constants.SENDER_TRANSFER_ALL_COMPLETE);
                    }
                }
            }
        }
    }

    /**
     * 获取对方请求的信息
     *
     * @param inputStream
     * @return
     */
    private RequestInfo getRequestInfo(InputStream inputStream) {
        RequestInfo rInfo = null;
        try {
            LogUtil.i("SenderTransferRunnable,发送端： 读取对方请求文件的信息");
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            rInfo = (RequestInfo) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rInfo;
    }

    /**
     * 依据文件描述发送相应文件
     *
     * @param requestInfo 文件描述
     * @param socket
     */
    private void sendFileByRequestInfo(RequestInfo requestInfo, Socket socket) throws IOException, FileNotFoundException {
        LogUtil.i("SenderTransferRunnable,服务端:   开始发送文件 index=" + requestInfo.index);
        int index = requestInfo.index;
        OutputStream outputStream = socket.getOutputStream();
        mFileInfo = FileSendData.getInstance().getFileSendList().get(index);
        if (mFileInfo.getState() == Constants.FILE_TRANSFER_CANCEL) {
            return;
        }
        updateInfo(Constants.FILE_TRANSFERING);
        FileSendData.getInstance().setmCurrentSendIndex(index);
        LogUtil.i("SendFragment---refreshUI---SENDER_UPDATE_TRANSFER_PROGRESS");
        refreshUI(index, Constants.SENDER_UPDATE_TRANSFER_PROGRESS);
        LogUtil.i("SenderTransferRunnable,SEND_FILE,mFileInfo = " + mFileInfo + " , mFileInfo.getUriString() = " + mFileInfo.getUriString() + ",size = " + mFileInfo.getFileSize());
        InputStream is = mContext.getContentResolver().openInputStream(Uri.parse(mFileInfo.getUriString()));
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        int length = 2048;
        byte[] buff = new byte[length];
        int len = 0;
        long transferSize = 0;
        while ((len = bis.read(buff)) != -1) {
            if (FileSendData.getInstance().isCancelAllSend()) {
                LogUtil.i("SenderTransferRunnable,服务端:  receive: 全部取消");
                mFileInfo.setState(Constants.FILE_TRANSFER_CANCEL);
            }
            //用户取消正在发送的文件
            if (mFileInfo.getState() == Constants.FILE_TRANSFER_CANCEL) {
                LogUtil.i("SenderTransferRunnable,服务端:  receive: 取消 ");
                FileSendData.getInstance().updateAllFileSize(mFileInfo.getFileSize() - transferSize);
                break;
            }
            bos.write(buff, 0, len);
            transferSize += len;
            FileSendData.getInstance().setFileTransferSize(index, transferSize);
            FileSendData.getInstance().setTotalTransferedSize(len);
        }
        is.close();
        bos.flush();
        bos.close();
        if (mFileInfo.getState() != Constants.FILE_TRANSFER_CANCEL) {
            FileSendData.getInstance().setmCurrentSendIndex(-1);
            LogUtil.i("SenderTransferRunnable,服务端:  发送文件name=" + mFileInfo.getFileName() + "成功");
            mFileInfo.setState(Constants.FILE_TRANSFER_SUCCESS);
        }
        LogUtil.i("SenderTransferRunnable,服务端:  发送文件index=" + index + "成功");
    }

    public void updateInfo(int dis) {
        mFileInfo.setState(dis);
    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}
