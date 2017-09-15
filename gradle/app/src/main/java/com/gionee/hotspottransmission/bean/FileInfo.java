package com.gionee.hotspottransmission.bean;

import android.content.Context;
import android.net.Uri;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.Serializable;

/**
 * Created by luorw on 2016/4/23 0025.
 */
public class FileInfo implements Serializable {
    private static final long serialVersionUID=7981560250804078637l;
    public String dis; //文件描述
    public int state = Constants.DEFAULT_FILE_TRANSFER_STATE; //文件传送状态
    private int id;//id
    private String fileName; //文件名
    private String filePath;//文件路径
    private long fileSize;  //文件大小
    private long modifiedDate;//文件修改的时间
    private boolean isSelected;//文件是否已经选择
    private String fileDir;//用于图片一级目录名称
    private int fileType;//文件类型：FILE，APPS，IMAGE，MUSIC，VIDEO
    private byte[] fileIcon;//文件图片，用于传输的时候现是
    private String uriString;//(Uri不能序列化,传输的时候无法传输，所以采用string)
    private long transferingSize;//正在传输的文件大小
    private int loaderId;//查询id:TYPE_DOCUMENT/TYPE_COMPRESS/TYPE_EBOOK/TYPE_APK/TYPE_APPS/TYPE_IMAGE/TYPE_VIDEO/TYPE_MUSIC

    public FileInfo(long size, String dis, String name, int state) {
        super();
        this.fileSize = size;
        this.dis = dis;
        this.fileName = name;
        this.state = state;
    }

    public FileInfo(Uri uri, String fileName, long fileSize) {
        this.uriString = uri.toString();
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileInfo(Context context, Uri uri, String fileName, long fileSize, String filePath) {
        this.uriString = uri.toString();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.fileType = FileTransferUtil.getFileTypeFromFileName(filePath);
    }

    public FileInfo() {

    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public String getUriString() {
        return uriString;
    }

    public long getTransferingSize() {
        return transferingSize;
    }

    public void setTransferingSize(long transferingSize) {
        this.transferingSize = transferingSize;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if(state == Constants.FILE_TRANSFER_FAILURE){
            LogUtil.i("luorw , 设置文件传输失败状态--------------------");
        }
        this.state = state;
    }

    public int getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(int loaderId) {
        this.loaderId = loaderId;
    }

    public void setFileIcon(byte[] fileIcon) {
        this.fileIcon = fileIcon;
    }

    public byte[] getFileIcon() {
        return fileIcon;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setIsSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getFileType() {
        return fileType;
    }

    @Override
    public String toString() {
        return "FileInfo [size=" + fileSize + ", dis=" + dis + ", name=" + fileName + ", state=" + state + "]";
    }
}
