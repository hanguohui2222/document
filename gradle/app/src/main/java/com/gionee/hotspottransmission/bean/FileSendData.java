package com.gionee.hotspottransmission.bean;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gionee.hotspottransmission.R;

/**
 * Created by luorw on 4/27/16.
 */
public class FileSendData extends BaseSendData{
    private static FileSendData mFileSendData;

    private FileSendData() {
        super();
    }

    public synchronized static FileSendData getInstance() {
        if (mFileSendData == null) {
            mFileSendData = new FileSendData();
        }
        return mFileSendData;
    }

}
