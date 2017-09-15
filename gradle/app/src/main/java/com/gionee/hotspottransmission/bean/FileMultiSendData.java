package com.gionee.hotspottransmission.bean;

import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luorw on 7/13/17.
 */
public class FileMultiSendData {
    private static FileMultiSendData mFileMultiSendData;
    private Map<String,BaseSendData> mAllMultiFileData;

    public synchronized static FileMultiSendData getInstance() {
        if (mFileMultiSendData == null) {
            mFileMultiSendData = new FileMultiSendData();
        }
        return mFileMultiSendData;
    }

    private FileMultiSendData() {
        mAllMultiFileData = new ArrayMap<>();
    }

    public Map<String, BaseSendData> getAllMultiFileData() {
        return mAllMultiFileData;
    }

    public void setAllMultiFileData(String key,BaseSendData baseSendData) {
        if(mAllMultiFileData.containsKey(key)){
            mAllMultiFileData.remove(key);
        }
        mAllMultiFileData.put(key,baseSendData);
    }

    public BaseSendData getFileSendData(String key){
        return mAllMultiFileData.get(key);
    }

    public void clearAllMultiSendData(){
        for (String key : mAllMultiFileData.keySet()) {
            mAllMultiFileData.remove(key);
        }
    }

}
