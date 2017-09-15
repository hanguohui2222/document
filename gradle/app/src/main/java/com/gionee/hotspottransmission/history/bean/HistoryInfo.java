package com.gionee.hotspottransmission.history.bean;

import com.gionee.hotspottransmission.bean.FileInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhuboqin on 4/05/16.
 */
public class HistoryInfo {

    public int id;
    public String deviceName;
    public String deviceAddress;
    public int fileCount;
    public long fileSize;
    public Date date;
    public boolean isSender; //0接收者,1发送者
    public List<FileInfo> files = new ArrayList<FileInfo>();

}
