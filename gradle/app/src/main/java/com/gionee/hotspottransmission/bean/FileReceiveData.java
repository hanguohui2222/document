package com.gionee.hotspottransmission.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by luorw on 4/27/16.
 */
public class FileReceiveData extends BaseReceiveData{

    private FileReceiveData() {
    	super();
    }

    public synchronized static FileReceiveData getInstance() {
        if (mFileTransferData == null) {
            mFileTransferData = new FileReceiveData();
        }
        return mFileTransferData;
    }

}
