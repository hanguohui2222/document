package com.gionee.hotspottransmission.history.dao;

import android.content.Context;
import android.util.ArrayMap;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuboqin on 5/05/16.
 */
public class ReceivedFileDao {

    private Context mContext;
    private Map<String,List<FileInfo>> fileInfoMap = new ArrayMap<>();

    public ReceivedFileDao(Context context){
        this.mContext = context.getApplicationContext();
    }

    public Map<String,List<FileInfo>> findFileByType(int type){

        switch (type){
            case Constants.TYPE_FILE:
                fileInfoMap = FileTransferUtil.getReceiveFileByType(mContext, Constants.TYPE_FILE);
                break;
            case Constants.TYPE_APPS:
                fileInfoMap = FileTransferUtil.getReceiveFileByType(mContext, Constants.TYPE_APPS);
                break;
            case Constants.TYPE_MUSIC:
                fileInfoMap = FileTransferUtil.getReceiveFileByType(mContext, Constants.TYPE_MUSIC);
                break;
            case Constants.TYPE_IMAGE:
                fileInfoMap = FileTransferUtil.getReceiveFileByType(mContext, Constants.TYPE_IMAGE);
                break;
            case Constants.TYPE_VIDEO:
                fileInfoMap = FileTransferUtil.getReceiveFileByType(mContext, Constants.TYPE_VIDEO);
                break;
        }
        return fileInfoMap;
    }


}
