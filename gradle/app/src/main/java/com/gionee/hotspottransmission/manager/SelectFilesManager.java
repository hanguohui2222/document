package com.gionee.hotspottransmission.manager;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IClearFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;

import java.util.List;

/**
 * Created by luorw on 4/27/16.
 */
public class SelectFilesManager {
    private FileSendData mFileSenderImpl;

    public SelectFilesManager(){
        mFileSenderImpl = FileSendData.getInstance();
    }

    public void getFileSendList(IRefreshFileSelectedCallBack callBack){
        callBack.onRefreshCount(mFileSenderImpl.getFileSendList());
    }

    public void showFileSendList(IRefreshFileSelectedCallBack callBack){
        callBack.onShowSelected(mFileSenderImpl.getFileSendList());
    }

    public void changFileTransferList(boolean isSelected, FileInfo info, IRefreshFileSelectedCallBack callBack){
        if(isSelected){
            mFileSenderImpl.addFileTransferList(info);
        }else{
            mFileSenderImpl.removeFileTransferInfo(info);
        }
        callBack.onRefreshCount(mFileSenderImpl.getFileSendList());
        callBack.onRefreshSelected(isSelected);
    }

    public void changImgTransferList(boolean isSelected, FileInfo info, String imgDirName,IRefreshFileSelectedCallBack callBack){
        if(isSelected){
            mFileSenderImpl.addImgTransferList(info,imgDirName);
        }else{
            mFileSenderImpl.removeImgTransferInfo(info,imgDirName);
        }
        callBack.onRefreshCount(mFileSenderImpl.getFileSendList());
        callBack.onRefreshSelected(isSelected);
    }

    public void changFileTransferList(int type,boolean isSelected, List<FileInfo> infoList, IRefreshFileSelectedCallBack callBack){
        if(isSelected){
            mFileSenderImpl.addFileTransferList(type,infoList);
        }else{
            mFileSenderImpl.removeFileTransferInfo(type,infoList);
        }
        callBack.onRefreshCount(mFileSenderImpl.getFileSendList());
        callBack.onRefreshSelected(isSelected);
    }

    public void changImgTransferList(boolean isSelected, List<FileInfo> infoList, String imgDirName, IRefreshFileSelectedCallBack callBack){
        if(isSelected){
            mFileSenderImpl.addImgTransferList(infoList,imgDirName);
        }else{
            mFileSenderImpl.removeImgTransferInfo(infoList,imgDirName);
        }
        callBack.onRefreshCount(mFileSenderImpl.getFileSendList());
        callBack.onRefreshSelected(isSelected);
    }

    public void clearFileTransferList(IClearFileSelectedCallBack callback){
        mFileSenderImpl.clearAllFiles();
        callback.clearSelectedFiles();
        callback.refreshUI(mFileSenderImpl.getFileSendList());
        callback.refreshSelectAllUI();
    }

    public void clearFileTransferItem(FileInfo info,IClearFileSelectedCallBack callback){
        mFileSenderImpl.removeFileTransferInfo(info);
        callback.clearSelectedFiles();
        callback.refreshUI(mFileSenderImpl.getFileSendList());
        callback.refreshSelectAllUI();
    }
    public void clearImgTransferItem(FileInfo info, String imgDirName,IClearFileSelectedCallBack callback){
        mFileSenderImpl.removeImgTransferInfo(info,imgDirName);
        callback.clearSelectedFiles();
        callback.refreshUI(mFileSenderImpl.getFileSendList());
        callback.refreshSelectAllUI();
    }
}
