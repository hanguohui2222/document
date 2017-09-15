package com.gionee.hotspottransmission.history.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.bean.HistoryFileInfo;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.history.biz.DeviceBiz;
import com.gionee.hotspottransmission.history.biz.HistoryBiz;
import com.gionee.hotspottransmission.history.biz.ReceivedFileBiz;
import com.gionee.hotspottransmission.history.view.IFileRecordView;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuboqin on 3/05/16.
 */
public class FileRecordPresenter  {

    private DeviceBiz mDeviceBiz;
    private HistoryBiz mHistoyBiz;
    private ReceivedFileBiz mFileBiz;

    private List<DeviceInfo> mDeviceInfos;
    private List<HistoryInfo> mHistoryList;
    private IFileRecordView mIFileRecordView;

    private static final int RESPONSE_DEVICE = 11;
    private static final int RESPONSE_HISTORY_RECORD = 12;
    private static final int RESPONSE_FILE = 13;

    public FileRecordPresenter(Context context,IFileRecordView view){
        mDeviceBiz = new DeviceBiz(context);
        mHistoyBiz = new HistoryBiz(context);
        mFileBiz = new ReceivedFileBiz(context);
        mIFileRecordView = view;
    }

    public void initData(final Handler handler){
        LogUtil.i("设备bug Presenter initData " );
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run(){
                mDeviceInfos = mDeviceBiz.findDevices();
                Message msg = Message.obtain();
                msg.what = RESPONSE_DEVICE;
                msg.obj = mDeviceInfos;
                handler.sendMessage(msg);
                LogUtil.i("设备bug Presenter send msg.obj " + msg.obj + " 设备个数" + mDeviceInfos.size());

                mHistoryList = mHistoyBiz.getHistory();
                Message msg2 = Message.obtain();
                msg2.what = RESPONSE_HISTORY_RECORD;
                List<HistoryFileInfo> historyFileInfoList = new ArrayList<>();

                    for (HistoryInfo historyInfo : mHistoryList) {

                        if (historyInfo.files != null && historyInfo.files.size() != 0) {

                            for (FileInfo fileInfo : historyInfo.files) {
                                HistoryFileInfo historyFileInfo = new HistoryFileInfo();
                                historyFileInfo.file = fileInfo;
                                historyFileInfo.isSender = historyInfo.isSender;
                                historyFileInfo.date = historyInfo.date;
                                historyFileInfo.fileSize = historyInfo.fileSize;
                                historyFileInfo.fileCount = historyInfo.fileCount;
                                historyFileInfo.deviceAddress = historyInfo.deviceAddress;
                                historyFileInfo.deviceName = historyInfo.deviceName;
                                historyFileInfo.id = historyInfo.id;
//                                LogUtil.i("FileRecordPresenter msg2.obj" + historyFileInfo.id +"   " +historyFileInfo.fileSize + "\n");
                                historyFileInfoList.add(historyFileInfo);
                            }

                        }

                    }
                msg2.obj = historyFileInfoList;
                LogUtil.i("FileRecordPresenter msg2.obj" + historyFileInfoList.size() + "\n" +historyFileInfoList.toString());
                handler.sendMessage(msg2);

                List<Map<String,List<FileInfo>>> list = new ArrayList<>();
                Map<String,List<FileInfo>> fileInfoMap1 = mFileBiz.findFileByType(Constants.TYPE_FILE);
                Map<String,List<FileInfo>> fileInfoMap2 = mFileBiz.findFileByType(Constants.TYPE_APPS);
                Map<String,List<FileInfo>> fileInfoMap3 = mFileBiz.findFileByType(Constants.TYPE_IMAGE);
                Map<String,List<FileInfo>> fileInfoMap4 = mFileBiz.findFileByType(Constants.TYPE_MUSIC);
                Map<String,List<FileInfo>> fileInfoMap5 = mFileBiz.findFileByType(Constants.TYPE_VIDEO);
                list.add(fileInfoMap1);
                list.add(fileInfoMap2);
                list.add(fileInfoMap3);
                list.add(fileInfoMap4);
                list.add(fileInfoMap5);

                Message msg3 = Message.obtain();
                msg3.what = RESPONSE_FILE;
                msg3.obj = list;
                handler.sendMessage(msg3);

            }
        });
    }

    public void clearDevice(){
        if(mDeviceInfos != null){
            mDeviceInfos.clear();

            boolean result = mDeviceBiz.deleteAllDevice();
            if(result){
                mIFileRecordView.deleteSucc();
            }else{
                mIFileRecordView.deleteFaile();
            }
        }
    }

    public void clearHistory(){
        if(mHistoryList != null){
            mHistoryList.clear();

            boolean result = mHistoyBiz.deleteHistory();
            if(result)
                mIFileRecordView.deleteSucc();
            else
                mIFileRecordView.deleteFaile();

        }
    }

}
