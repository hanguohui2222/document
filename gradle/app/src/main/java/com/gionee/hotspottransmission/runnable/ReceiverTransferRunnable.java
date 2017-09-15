package com.gionee.hotspottransmission.runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.RequestInfo;
import com.gionee.hotspottransmission.callback.IDeviceCallBack;
import com.gionee.hotspottransmission.callback.ITransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ReceivedImageSourceManager;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by luorw on 5/25/17.
 */
public class ReceiverTransferRunnable implements Runnable {
    private Handler mHandler;
    private Context mContext;
    private ArrayList<FileInfo> fileInfos;
    private boolean bDownLoad;
    private boolean bFullStorage;
    private ITransferService mTransferServiceCallBack;
    private IDeviceCallBack mDeviceCallBack;

    public ReceiverTransferRunnable(Context context, Handler handler, ITransferService transferServiceCallBack, IDeviceCallBack deviceCallBack) {
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
        this.mTransferServiceCallBack = transferServiceCallBack;
        this.mDeviceCallBack = deviceCallBack;
    }

    @Override
    public void run() {
        work();
    }

    private void work() {
        bDownLoad = true;
        fileInfos = FileReceiveData.getInstance().getFileReceiveList();
        LogUtil.i("ReceiverTransferRunnable:   开始下载文件");
        if (fileInfos == null || fileInfos.size() == 0) {
            return;
        }
        for (int i = 0; i < fileInfos.size(); i++) {
            if (!bDownLoad) {
                break;
            }
            FileInfo fileInfo = fileInfos.get(i);
            if (!isAvailableStorage(fileInfo)) {
                break;
            }
            if (fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || FileReceiveData.getInstance().isCancelAllReceive()) {
                continue;
            }
            fileInfo.setState(Constants.FILE_TRANSFERING);
            refreshUI(i, Constants.RECEIVER_UPDATE_TRANSFER_PROGRESS);
            Socket socket = mTransferServiceCallBack.getReceiveFileSocket();
            LogUtil.i("ReceiverTransferRunnable,接收文件socket连接成功,socket = " + socket);
            try {
                receive(fileInfo, i, socket);
            } catch (Exception e) {
                LogUtil.i("ReceiverTransferRunnable,接收: e = " + e.getMessage());
                if (fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && !FileReceiveData.getInstance().isCancelAllReceive()) {
                    LogUtil.i("ReceiverTransferRunnable,客户端:   接受文件失败" + i + ",e = " + e.getMessage());
                    fileInfo.setState(Constants.FILE_TRANSFER_FAILURE);
                    //传输失败的文件要删除掉
                    FileUtil.removeFileByPath(fileInfo.getFilePath());
                }
                LogUtil.i("ReceiverTransferRunnable,客户端:   接受文件成功异常" + e.getMessage());
            } finally {
                refreshUI(i, Constants.RECEIVER_TRANSFER_COMPLETE_BY_INDEX);
                if (!FileReceiveData.getInstance().isConnected()) {
                    LogUtil.i("ReceiverTransferRunnable,客户端:  连接中断" + i);
                    break;
                }
            }
        }
        LogUtil.i("ReceiverTransferRunnable,客户端： 所有文件传输完毕");
        if (bDownLoad && !bFullStorage) {
            if (!FileReceiveData.getInstance().isConnected()) {
                if (!FileReceiveData.getInstance().isAllReceiveComplete()) {
                    LogUtil.i("ReceiverTransferRunnable,客户端:  连接中断，更新所有文件状态");
                    FileReceiveData.getInstance().updateDisconnectAllState();
                    refreshUI(-1, Constants.RECEIVER_TRANSFER_ALL_COMPLETE);
                }
                if (mDeviceCallBack != null) {
                    mDeviceCallBack.onExit();
                }
            } else {
                if (!FileReceiveData.getInstance().isCancelAllReceive()) {
                    over();
                    bDownLoad = false;
                }
            }
        }
    }

    private boolean isAvailableStorage(FileInfo fileInfo) {
        boolean isAvailable = FileTransferUtil.getAvailableStorage(mContext, fileInfo.getFileSize());
        if (!isAvailable) {
            //added by luorw for GNSPR28062 20160705 begin
            bFullStorage = true;
            mHandler.sendEmptyMessage(Constants.RECEIVER_TRANSFER_STORAGE_FULL);
            //added by luorw for GNSPR28062 20160705 end
        }
        return isAvailable;
    }

    private void receive(FileInfo fileInfo, int index, Socket socket) throws Exception {
        FileReceiveData.getInstance().setmCurrentReceiveIndex(index);
        LogUtil.i("ReceiverTransferRunnable,客户端：  开始接收文件 and index= " + index);
        //请求文件
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.command = Constants.SEND_FILE;
        requestInfo.index = index;
        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(requestInfo);
        //接收文件
        String filePath = FileTransferUtil.getReceiveFilePath(mContext, fileInfo);
        fileInfo.setFilePath(filePath);
        LogUtil.i("ReceiverTransferRunnable,客户端:   保存文件路径为: " + filePath);
        File file = new File(filePath);
        File dirs = new File(file.getParent());
        if (!dirs.exists())
            dirs.mkdirs();
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        InputStream is = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        int buffSize = 2048;
        byte[] buff = new byte[buffSize];
        int len = 0;
        long mTransferSize = 0;
        while ((len = bis.read(buff)) != -1) {
            if (FileReceiveData.getInstance().isCancelAllReceive()) {
                LogUtil.i("ReceiverTransferRunnable,客户端:  receive: 全部取消");
                fileInfo.setState(Constants.FILE_TRANSFER_CANCEL);
            }
            if (fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL/* || isCurrentReceiveFail*/) {
                LogUtil.i("ReceiverTransferRunnable,客户端:  receive: 取消 ， isCurrentReceiveFail = " /*+ isCurrentReceiveFail*/);
                FileReceiveData.getInstance().updateAllFileSize(fileInfo.getFileSize() - mTransferSize);
                //传输失败的文件要删除掉
                FileUtil.removeFileByPath(fileInfo.getFilePath());
//                isCurrentReceiveFail = false;
                break;
            }
            bos.write(buff, 0, len);
            mTransferSize += len;
            FileReceiveData.getInstance().setFileTransferSize(index, mTransferSize);
            FileReceiveData.getInstance().setTotalTransferedSize(len);
        }
        bos.flush();
        bos.close();
        bis.close();
        socket.close();
        LogUtil.i("ReceiverTransferRunnable,客户端:   接受文件成功 and TransferLen= " + mTransferSize);
        if (fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && mTransferSize == fileInfo.getFileSize()) {
            FileReceiveData.getInstance().setmCurrentReceiveIndex(-1);
            fileInfo.setState(Constants.FILE_TRANSFER_SUCCESS);
            if (fileInfo.getFileType() == Constants.TYPE_IMAGE) {
                ReceivedImageSourceManager.getInstance(mContext).setReceivedImageName(fileInfo.getFileName());
                ReceivedImageSourceManager.getInstance(mContext).setReceivedImagePath(filePath);
            }
        } else {
            LogUtil.i("ReceiverTransferRunnable,客户端:   接受文件失败 and State = " + fileInfo.getState());
            //added by luorw for GNSPR #35768 GNSPR #36592 20160725 begin
            if (!FileReceiveData.getInstance().isCancelAllReceive() && fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL) {
                LogUtil.i("ReceiverTransferRunnable,客户端:   接受文件失败,index = " + index);
                //added by luorw for  GNSPR #28950 begin
                fileInfo.setState(Constants.FILE_TRANSFER_FAILURE);
                //added by luorw for  GNSPR #28950 end
            }
            //added by luorw for GNSPR #35768 GNSPR #36592 20160725 end
        }
        //added by luorw for  GNSPR #18822 begin
        if (mTransferSize != fileInfo.getFileSize()) {
            //传输失败的文件要删除掉
            FileUtil.removeFileByPath(fileInfo.getFilePath());
        }
        //added by luorw for  GNSPR #18822 end
    }

    public void over() {
        LogUtil.i("ReceiverTransferRunnable,receive over");
        mTransferServiceCallBack.receiverWriteCommand(Constants.SEND_ALL_COMPLETE);
        refreshUI(-1, Constants.RECEIVER_TRANSFER_ALL_COMPLETE);
    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}