package com.gionee.hotspottransmission.runnable.multi;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.gionee.hotspottransmission.bean.BaseReceiveData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiReceiveData;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.bean.RequestInfo;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.IMultiTransferService;
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
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by luorw on 5/25/17.
 */
public class MultiReceiveRunnable implements Runnable {
    private Handler mHandler;
    private Context mContext;
    private ArrayList<FileInfo> fileInfos;
    private boolean isDownLoad;
    private boolean isFullStorage;
    private IMultiTransferService mTransferServiceCallBack;
    private String mKey;
    private String mIp;
    private BaseReceiveData mReceiveData;

    public MultiReceiveRunnable(Context context, Handler handler, IMultiTransferService transferServiceCallBack, String key) {
        this.mContext = context.getApplicationContext();
        this.mHandler = handler;
        this.mTransferServiceCallBack = transferServiceCallBack;
        this.mKey = key;
        mReceiveData = FileMultiReceiveData.getInstance().getFileReceiveData(mKey);
        mIp = SocketChannel.getInstance().mAddresses.get(mKey);
        LogUtil.i("luorw,MultiReceiveRunnable:   开始下载文件 , mKey = "+mKey + " , ip = "+mIp);
    }

    @Override
    public void run() {
        work();
    }

    private void work() {
        isDownLoad = true;
        fileInfos = mReceiveData.getFileReceiveList();
        LogUtil.i("luorw,MultiReceiveRunnable:   开始下载文件 , fileInfos = "+fileInfos);
        if (fileInfos == null || fileInfos.size() == 0) {
            return;
        }
        for (int i = 0; i < fileInfos.size(); i++) {
            if (!isDownLoad) {
                break;
            }
            FileInfo fileInfo = fileInfos.get(i);
            if (!isAvailableStorage(fileInfo)) {
                LogUtil.i("luorw,MultiReceiveRunnable:   isAvailableStorage = false");
                break;
            }
            if (fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || mReceiveData.isCancelAllReceive()) {
                LogUtil.i("luorw,MultiReceiveRunnable:   cancel | cancelAll");
                continue;
            }
            fileInfo.setState(Constants.FILE_TRANSFERING);
            refreshUI(i, Constants.RECEIVER_UPDATE_TRANSFER_PROGRESS);
            Socket socket = mTransferServiceCallBack.getReceiveFileSocket(mIp);
            LogUtil.i("luorw,MultiReceiveRunnable,接收文件socket连接成功,socket = " + socket);
            try {
                requestFile(socket , i);
                receive(fileInfo, i , socket);
            } catch (Exception e) {
                LogUtil.i("luorw , MultiReceiveRunnable,接收: e = " + e.getMessage());
                if (fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && !mReceiveData.isCancelAllReceive()) {
                    LogUtil.i("luorw , MultiReceiveRunnable,客户端:   接受文件失败" + i + ",e = " + e.getMessage());
                    fileInfo.setState(Constants.FILE_TRANSFER_FAILURE);
                    //传输失败的文件要删除掉
                    FileUtil.removeFileByPath(fileInfo.getFilePath());
                }
                LogUtil.i("luorw , MultiReceiveRunnable,客户端:   接受文件成功异常" + e.getMessage());
            } finally {
                refreshUI(i, Constants.RECEIVER_TRANSFER_COMPLETE_BY_INDEX);
                if (!mReceiveData.isConnected()) {
                    LogUtil.i("luorw ,MultiReceiveRunnable,客户端:  连接中断" + i);
                    break;
                }
            }
        }
        LogUtil.i("luorw , MultiReceiveRunnable,客户端： 所有文件传输完毕");
        if (isDownLoad && !isFullStorage) {
            if (!mReceiveData.isConnected() || mReceiveData.isCancelAllReceive()) {
                if (!mReceiveData.isAllReceiveComplete()) {
                    LogUtil.i("luorw , MultiReceiveRunnable,客户端:  连接中断，更新所有文件状态");
                    mReceiveData.updateDisconnectAllState();
                    refreshUI(-1, Constants.RECEIVER_TRANSFER_ALL_COMPLETE);
                    isDownLoad = false;
                }
//                if (mDeviceCallBack != null) {
//                    mDeviceCallBack.onExit();
//                }
            } else {
                if (!mReceiveData.isCancelAllReceive()) {
                    over();
                    isDownLoad = false;
                }
            }
        }
    }

    private boolean isAvailableStorage(FileInfo fileInfo) {
        boolean isAvailable = FileTransferUtil.getAvailableStorage(mContext, fileInfo.getFileSize());
        if (!isAvailable) {
            //added by luorw for GNSPR28062 20160705 begin
            isFullStorage = true;
            mHandler.sendEmptyMessage(Constants.RECEIVER_TRANSFER_STORAGE_FULL);
            //added by luorw for GNSPR28062 20160705 end
        }
        return isAvailable;
    }

    private void receive(FileInfo fileInfo, int index , Socket socket) throws Exception {
        mReceiveData.setmCurrentReceiveIndex(index);
        LogUtil.i("luorw , MultiReceiveRunnable,客户端：  开始接收文件 and index= " + index);
        //接收文件
        String filePath = FileTransferUtil.getReceiveFilePath(mContext, fileInfo);
        fileInfo.setFilePath(filePath);
        LogUtil.i("luorw ,MultiReceiveRunnable,客户端:   保存文件路径为: " + filePath);
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
                LogUtil.i("luorw , MultiReceiveRunnable,客户端:  receive: 全部取消");
                fileInfo.setState(Constants.FILE_TRANSFER_CANCEL);
            }
            if (fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL/* || isCurrentReceiveFail*/) {
                LogUtil.i("luorw , 客户端:  receive: 取消 ， isCurrentReceiveFail = " /*+ isCurrentReceiveFail*/);
                mReceiveData.updateAllFileSize(fileInfo.getFileSize() - mTransferSize);
                //传输失败的文件要删除掉
                FileUtil.removeFileByPath(fileInfo.getFilePath());
//                isCurrentReceiveFail = false;
                break;
            }
            bos.write(buff, 0, len);
            mTransferSize += len;
            mReceiveData.setFileTransferSize(index, mTransferSize);
            mReceiveData.setTotalTransferedSize(len);
        }
        bos.flush();
        bos.close();
        bis.close();
        socket.close();
        LogUtil.i("luorw , MultiReceiveRunnable,客户端:   接受文件成功 and TransferLen= " + mTransferSize);
        if (fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL && mTransferSize == fileInfo.getFileSize()) {
            mReceiveData.setmCurrentReceiveIndex(-1);
            fileInfo.setState(Constants.FILE_TRANSFER_SUCCESS);
            if (fileInfo.getFileType() == Constants.TYPE_IMAGE) {
                ReceivedImageSourceManager.getInstance(mContext).setReceivedImageName(fileInfo.getFileName());
                ReceivedImageSourceManager.getInstance(mContext).setReceivedImagePath(filePath);
            }
        } else {
            LogUtil.i("luorw , MultiReceiveRunnable,客户端:   接受文件失败 and State = " + fileInfo.getState());
            //added by luorw for GNSPR #35768 GNSPR #36592 20160725 begin
            if (!mReceiveData.isCancelAllReceive() && fileInfo.getState() != Constants.FILE_TRANSFER_CANCEL) {
                LogUtil.i("luorw , MultiReceiveRunnable,客户端:   接受文件失败,index = " + index);
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
        LogUtil.i("luorw , MultiReceiveRunnable,receive over");
        mTransferServiceCallBack.createWriteCommand(mKey,new MultiCommandInfo(Constants.RECEIVER_RECEIVE_OVER));
        refreshUI(-1, Constants.RECEIVER_TRANSFER_ALL_COMPLETE);
    }

    private void requestFile(Socket socket , int index){
        LogUtil.i("luorw , MultiReceiveRunnable,requestFile----------------");
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo();
        multiCommandInfo.command = Constants.RECEIVER_REQUEST_FILE;
        multiCommandInfo.requestIndex = index;
        try {
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(multiCommandInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void refreshUI(int index, int what) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = index;
        msg.what = what;
        msg.obj = mKey;
        mHandler.sendMessage(msg);
    }
}