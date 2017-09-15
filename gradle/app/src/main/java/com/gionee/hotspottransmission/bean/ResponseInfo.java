package com.gionee.hotspottransmission.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by luorw on 2017/5/22
 */
public class ResponseInfo implements Serializable {
    public ArrayList<FileInfo> filesList = new ArrayList<FileInfo>();
    public String sendStatus; 
    public String deviceName;
    public String deviceAddress;
    public int index;
    public int command;

    public void setFilesList(ArrayList<FileInfo> fileInfo){
        if(filesList.size()!=0){
            filesList.clear();
        }
        filesList.addAll(fileInfo);
    }

    @Override
    public String toString() {
        return "responseSendStatus=" + sendStatus + ",deviceName=" + deviceName + ",index=" + index + ",command=" + command +
                "ResponseInfo [filesList=" + filesList + "] ";
    }

}
