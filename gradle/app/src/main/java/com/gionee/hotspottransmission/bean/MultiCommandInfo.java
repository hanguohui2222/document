package com.gionee.hotspottransmission.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by luorw on 7/25/17.
 */

public class MultiCommandInfo implements Serializable{
    public ArrayList<FileInfo> responseFilesList = new ArrayList<FileInfo>();
    public String responseSendStatus;
    public String responseDeviceName;
    public String responseDeviceImei;
    public int responseIndex;
    public int command;
    public String requestDescribe;
    public String requestDeviceName;
    public String requestSendStatus;
    public int requestIndex;
    public int option;//发送OP_SEND,接收OP_RECEIVE

    public MultiCommandInfo(int command){
        this.command = command;
    }

    public MultiCommandInfo(){

    }

    public void setFilesList(ArrayList<FileInfo> fileInfo){
        if(responseFilesList.size()!=0){
            responseFilesList.clear();
        }
        responseFilesList.addAll(fileInfo);
    }

    @Override
    public String toString() {
        return "MultiCommandInfo { responseSendStatus = " + responseSendStatus + ",responseDeviceName = " + responseDeviceName + ",responseIndex = " + responseIndex + ",Command = " + command +
                "ResponseInfo [filesList=" + responseFilesList + "] }"
                + "RequestInfo{" +
                ", requestDescribe = " + requestDescribe + '\'' +
                ", requestDeviceName='" + requestDeviceName + '\'' +
                ", requestSendStatus='" + requestSendStatus + '\'' +
                ", requestIndex=" + requestIndex +
                '}';
    }
}
