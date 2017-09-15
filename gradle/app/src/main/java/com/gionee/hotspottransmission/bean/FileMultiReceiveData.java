package com.gionee.hotspottransmission.bean;

import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luorw on 7/13/17.
 */
public class FileMultiReceiveData {

    private static FileMultiReceiveData mFileMultiReceiveData;
    private Map<String, BaseReceiveData> mAllMultiFileData;

    public synchronized static FileMultiReceiveData getInstance() {
        if (mFileMultiReceiveData == null) {
            mFileMultiReceiveData = new FileMultiReceiveData();
        }
        return mFileMultiReceiveData;
    }

    private FileMultiReceiveData() {
        mAllMultiFileData = new ArrayMap<>();
    }

    public Map<String, BaseReceiveData> getAllMultiFileData() {
        return mAllMultiFileData;
    }

    public BaseReceiveData getFileReceiveData(String key) {
        return mAllMultiFileData.get(key);
    }

    public void setFileReceiveData(String key, BaseReceiveData baseReceiveData) {
        if (mAllMultiFileData.containsKey(key)) {
            mAllMultiFileData.remove(key);
        }
        mAllMultiFileData.put(key, baseReceiveData);
    }

    public void removeFileReceiveData(String key) {
        mAllMultiFileData.remove(key);
    }

}
