package com.gionee.hotspottransmission.history.bean;

import com.gionee.hotspottransmission.bean.FileInfo;

import java.util.Date;

/**
 * Created by zhuboqin on 4/05/16.
 */
public class HistoryFileInfo {

    public int id;
    public String deviceName;
    public String deviceAddress;
    public int fileCount;
    public long fileSize;
    public Date date;
    public boolean isSender; //0接收者,1发送者
    public FileInfo file;

}
