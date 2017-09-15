package com.gionee.hotspottransmission.bean;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

public class BaseReceiveData {

	public ArrayList<FileInfo> mAllFileDatas;
    public static FileReceiveData mFileTransferData;
    public long mAllFileSize;
    public long mTotalTransferedSize = 0;
    public boolean isCancelAllReceive;
    public boolean isAllReceiveComplete;
    public int mCurrentReceiveIndex;
    public boolean isConnected;
    public boolean isResend;
    public boolean isReceiving;

    public BaseReceiveData() {
        mAllFileDatas = new ArrayList<FileInfo>();
    }

   
    public boolean isReceiving() {
        return isReceiving;
    }

    public void setReceiving(boolean receiving) {
        isReceiving = receiving;
    }

    public boolean isResend() {
        return isResend;
    }

    public void setResend(boolean resend) {
        isResend = resend;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isCancelAllReceive() {
        return isCancelAllReceive;
    }

    public void setCancelAllReceive(boolean cancelAllReceive) {
        isCancelAllReceive = cancelAllReceive;
    }

    public boolean isAllReceiveComplete() {
        return isAllReceiveComplete;
    }

    public void setAllReceiveComplete(boolean allReceiveComplete) {
        isAllReceiveComplete = allReceiveComplete;
        if(allReceiveComplete){
            setReceiving(false);
        }
    }

    public int getmCurrentReceiveIndex() {
        return mCurrentReceiveIndex;
    }

    public void setmCurrentReceiveIndex(int mCurrentReceiveIndex) {
        this.mCurrentReceiveIndex = mCurrentReceiveIndex;
    }

    public ArrayList<FileInfo> getFileReceiveList() {
        return mAllFileDatas;
    }

    public void setFileReceiveList(ArrayList<FileInfo> list) {
        mAllFileDatas = list;
    }

    public ResponseInfo getResponseList() {
        ResponseInfo responseList = new ResponseInfo();
        responseList.setFilesList(mAllFileDatas);
        return responseList;
    }

    public void clearAllFiles() {
        mAllFileDatas.clear();
        mAllFileSize = 0;
        mTotalTransferedSize = 0;
    }

    public void setFileTransferSize(int index, long fileSize) {
        if (mAllFileDatas == null || index >= mAllFileDatas.size()) {
            return;
        }
        FileInfo fileTransferData = mAllFileDatas.get(index);
        fileTransferData.setTransferingSize(fileSize);
    }

    public void calculateAllFileSize() {
        for (FileInfo info : mAllFileDatas) {
            mAllFileSize += info.getFileSize();
        }
        LogUtil.i("calculateAllFileSize = " + mAllFileSize);
    }

    public long getAllFileSize() {
        return mAllFileSize;
    }

    public long updateAllFileSize(long cancelSize) {
        return mAllFileSize - cancelSize;
    }

    public void setTotalTransferedSize(long size) {
        mTotalTransferedSize += size;
    }

    public long getTotalTransferedSize() {
        return mTotalTransferedSize;
    }

    public void updateCancelAllState(int currentTransferIndex) {
        for (int i = 0; i < mAllFileDatas.size(); i++) {
            FileInfo info = mAllFileDatas.get(i);
            if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE || info.getState() == Constants.FILE_TRANSFERING || i == currentTransferIndex) {
                info.setState(Constants.FILE_TRANSFER_CANCEL);
            }
        }
    }

    public void setAllFileIcon(Context context) {
        CacheUtil cacheUtil = new CacheUtil(null, context);
        Bitmap bitmap = null;
        Bitmap fileBitmape = BitmapFactory.decodeResource(context.getResources(), R.drawable.file_icon_folder);
        Bitmap musicBitmape = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio_icon);
        //modified by luorw for GNSPR #24579 begin
        //modified by luorw for GNSPR #26748 20160706 begin
        Iterator<FileInfo> iterator = mAllFileDatas.iterator();
        LogUtil.i("setAllFileIcon,mAllFileDatas.size = " + mAllFileDatas.size());
//        int docCount = 0, appsCount = 0, imageCount = 0, musicCount = 0, videoCount = 0;
        while (iterator.hasNext()) {
            try {
                FileInfo fileInfo = iterator.next();
                //modified by luorw for GNSPR #26748 20160706 end
                switch (fileInfo.getFileType()) {
                    case Constants.TYPE_FILE:
//                        docCount++;
                        fileInfo.setFileIcon(FileUtil.getBytes(fileBitmape));
                        break;
                    case Constants.TYPE_APPS:
//                        appsCount++;
                        bitmap = cacheUtil.getAPKThumbBitmap(context, fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    case Constants.TYPE_IMAGE:
//                        imageCount++;
                        bitmap = cacheUtil.getImageBitmap(fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    case Constants.TYPE_MUSIC:
//                        musicCount++;
                        fileInfo.setFileIcon(FileUtil.getBytes(musicBitmape));
                        break;
                    case Constants.TYPE_VIDEO:
//                        videoCount++;
                        bitmap = cacheUtil.getVideoThumbnail(fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i("setAllFileIcon : " + e.toString());
                break;
            }
        }
        fileBitmape.recycle();
        musicBitmape.recycle();
        fileBitmape = null;
        musicBitmape = null;
        bitmap = null;
        //modified by luorw for GNSPR #24579 end
//        //added by luorw for GNNCR #51917 20161031 begin
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put(Constants.TRANSFER_LIST_COUNT, mAllFileDatas.size());
//        map.put(Constants.TRANSFER_DOC_COUNT, docCount);
//        map.put(Constants.TRANSFER_APPS_COUNT, appsCount);
//        map.put(Constants.TRANSFER_IMAGE_COUNT, imageCount);
//        map.put(Constants.TRANSFER_MUSIC_COUNT, musicCount);
//        map.put(Constants.TRANSFER_VIDEO_COUNT, videoCount);
//        //YouJuAgent.onEvent (context, Constants.EVENT_ID, Constants.TRANSFER_LIST_EVENT, map);
//        YouJuAgent.onEvent(context,"传输文件列表","传输文件类型",map);
//        //added by luorw for GNNCR #51917 20161031 end
    }

    public void updateDisconnectAllState() {
        for (FileInfo info : mAllFileDatas) {
            if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE || info.getState() == Constants.FILE_TRANSFERING) {
                LogUtil.i("updateDisconnectAllState=" + info.getFileName());
                info.setState(Constants.FILE_TRANSFER_FAILURE);
            }
        }
    }
}
