package com.gionee.hotspottransmission.view.viewholder;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.gionee.hotspottransmission.R;
import android.widget.TextView;

import com.gionee.hotspottransmission.adapter.FileTransferListAdapter;
import com.gionee.hotspottransmission.animation.NumberProgressBar;
import com.gionee.hotspottransmission.bean.BaseReceiveData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.ITransferListener;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.history.biz.HistoryBiz;
import com.gionee.hotspottransmission.manager.NotificationManager;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.BaseTransferActivity;

import java.util.ArrayList;
import java.util.Calendar;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoProgressBar;

/**
 * Created by luorw on 8/7/17.
 */

public class MultiReceiveViewHolder implements ITransferListener {
    private Context mContext;
    private ListView mReceivingListView;
    private NumberProgressBar numberProgressBar;
    private LinearLayout mLayoutTransfer;
    private FileTransferListAdapter mListAdapter;
    private TextView sendDeviceName;
    private ArrayList<FileInfo> mFileDatas;
    private Handler mHandler = new Handler();
    private int mIndex;
    private Notification mNotification;
    private boolean isStartNotification;
    private boolean isStartAmimation;
    private BaseReceiveData mFileReceiveData;
    private String mKey;
    private BaseTransferActivity mActivity;
    private ImageView mCancelAll;

    public MultiReceiveViewHolder(Context context, String key,BaseTransferActivity activity) {
        this.mContext = context.getApplicationContext();
        mKey = key;
        this.mActivity = activity;
    }

    public View createReceiveLayout() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_multi_transfer_list, null);
        initReceiveView(view);
        return view;
    }

    private void initReceiveView(View view) {
        mLayoutTransfer = (LinearLayout) view.findViewById(R.id.layout_transfer);
        mReceivingListView = (ListView) view.findViewById(R.id.lv_file_transfering);
        numberProgressBar = (NumberProgressBar) view.findViewById(R.id.number_progressbar);
        sendDeviceName = (TextView) view.findViewById(R.id.send_device_name);
        mCancelAll = (ImageView)view.findViewById(R.id.cancel_transfer_multi);
        initData();
    }

    private void initData(){
        numberProgressBar.setMax(Constants.MAX_PROCESS);
        sendDeviceName.setText(SocketChannel.getInstance().mName.get(mKey));
        mLayoutTransfer.setVisibility(View.VISIBLE);
        mFileReceiveData = FileMultiReceiveData.getInstance().getFileReceiveData(mKey);
        mFileDatas = (ArrayList<FileInfo>) mFileReceiveData.getFileReceiveList().clone();
        mListAdapter = new FileTransferListAdapter(mContext, mFileDatas, false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelByIndex(v);
            }
        });
        mReceivingListView.setAdapter(mListAdapter);
        mCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAllCancelDialog();
            }
        });
    }

    private void cancelByIndex(View v){
        int index = (Integer) v.getTag();
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo(Constants.RECEIVER_CANCEL_BY_INDEX);
        multiCommandInfo.responseDeviceImei = mKey;
        multiCommandInfo.requestIndex = index;
        mActivity.getMultiService().createWriteCommand(mKey,multiCommandInfo);
        onCancelByIndex(index);
    }

    /**
     * 全部取消提示
     */
    public void makeAllCancelDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(mContext, R.style.AmigoDialogTheme);
        builder.setMessage(mContext.getResources().getString(R.string.cancel_all_transfer));
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.i("ReceiveFragment , makeAllCancelDialog,确定取消---------");
                cancelAll();
            }
        });
        builder.setCancelable(false);
        AmigoAlertDialog dialog = builder.create();
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private void cancelAll() {
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo(Constants.RECEIVER_CANCEL_ALL);
        multiCommandInfo.responseDeviceImei = mKey;
        multiCommandInfo.requestIndex = mFileReceiveData.getmCurrentReceiveIndex();
        mActivity.getMultiService().createWriteCommand(mKey,multiCommandInfo);
        onCancelAll(multiCommandInfo.requestIndex);
    }

    private void cancelNoti() {
        if (mNotification != null && !isStartNotification) {
            NotificationManager.cancelNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER);
        }
    }

    private void startNoti() {
        if (mNotification != null && isStartNotification && !FileSendData.getInstance().isAllSendComplete()) {
            NotificationManager.startNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, mNotification);
        }
    }

    @Override
    public void onCancelByIndex(int index) {
        LogUtil.i("ReceiveFragment, onCancelByIndex , index = "+index);
        //added by luorw for GNSPR #47037 20161011 begin
        if(mFileDatas.size() == 0){
            return;
        }
        //added by luorw for GNSPR #47037 20161011 end
        FileInfo info = mFileDatas.get(index);
        LogUtil.i("ReceiveFragment , onCancelByIndex , State = "+info.getState());
        if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE) {
            mFileReceiveData.updateAllFileSize(info.getFileSize());
        } else {
            mFileReceiveData.updateAllFileSize(info.getFileSize() - info.getTransferingSize());
        }
        info.setState(Constants.FILE_TRANSFER_CANCEL);
        mListAdapter.notifyDataSetChanged();
//        mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, info.getState(),mActivity.getService().isGroupOwner());
//        startNoti();
    }

    @Override
    public void onCancelAll(int currentTransferIndex) {
        LogUtil.i("ReceiveFragment,onCancelAll----------");
        mFileReceiveData.setCancelAllReceive(true);
        mFileReceiveData.updateCancelAllState(currentTransferIndex);
        mListAdapter.notifyDataSetChanged();
        if (!mFileReceiveData.isAllReceiveComplete()) {
            LogUtil.i("ReceiveFragment,onCancelAll------!isAllComplete---------");
            onTransferAllComplete();
        }
    }

    @Override
    public void onTransferCompleteByIndex(int index) {
        mListAdapter.notifyDataSetChanged();
        if(mFileDatas.size() > 0){
            FileInfo info = mFileDatas.get(index);
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, info.getState(),mActivity.getService().isGroupOwner());
            LogUtil.i("ReceiveFragment已完成 第  " + index + "  传输 需要更新列表 ");
//            startNoti();
        }
    }

    @Override
    public void onTransferAllComplete() {
        LogUtil.i("ReceiveFragment,onTransferAllComplete---------");
        updateAllCompleteProgress();
//        if(FileSendData.getInstance().isSending()){
//            mDeviceCallBack.onRefreshMenu(false,true);
//        }else{
//            mDeviceCallBack.onRefreshMenu(true,true);
//        }
        mHandler.removeCallbacks(mFileSendRunnable);
//            if (!isTransferBreak) {
        saveTransferHistory();
//            }
        //added by luorw for GNSPR #20731 begin
        //更新媒体库
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        String dirPath = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.dir_name) + "/";
        LogUtil.i("ReceiveFragment,更新媒体库----dirPath = " + dirPath);
        if (null != dirPath) {
            mediaScanIntent.setData(Uri.parse("file://" + dirPath));
            mContext.sendBroadcast(mediaScanIntent);
        }
        //added by luorw for GNSPR #20731 end
        if (mFileReceiveData.isConnected()) {
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_ALL_COMPLETE,mActivity.getService().isGroupOwner());
//            startNoti();
            mFileReceiveData.setAllReceiveComplete(true);
        } else {
            //异常中断的传输，需要等状态全部更新完才能断开服务
            LogUtil.i("ReceiveFragment,异常中断的传输,onTransferAllComplete---------");
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_BREAK,mActivity.getService().isGroupOwner());
//            startNoti();
//            mActivity.exit(false);
        }
        mActivity.getMultiService().unRegisterSendListener(mKey);
    }

    private void saveTransferHistory() {
        HistoryInfo historyInfo = new HistoryInfo();
        historyInfo.files.clear();
        historyInfo.files.addAll(mFileDatas);
        historyInfo.isSender = false;
        Calendar calendar = Calendar.getInstance();
        historyInfo.date = calendar.getTime();
        historyInfo.fileCount = historyInfo.files.size();
        historyInfo.fileSize = FileSendData.getInstance().getTotalTransferedSize();
        historyInfo.deviceAddress = DeviceSp.getInstance().getConnectedDeviceAddress(mContext);
        historyInfo.deviceName = DeviceSp.getInstance().getConnectedDeviceName(mContext);
        HistoryBiz historyBiz = new HistoryBiz(mContext);
        historyBiz.addHistoryRecord(historyInfo);
    }

    @Override
    public void onReadFileListSuccess() {
        mFileReceiveData.setAllReceiveComplete(false);
        isStartAmimation = false;
//        mPbLoading.setVisibility(View.GONE);
//        mLayoutBlankPage.setVisibility(View.GONE);
//        mLayoutTransfer.setVisibility(View.VISIBLE);
//        mDeviceCallBack.onRefreshMenu(false,false);
//        mFileDatas = mFileReceiveData.getFileReceiveList();
//        mListAdapter.setFileDatas(mFileDatas);
        mListAdapter.notifyDataSetChanged();
        LogUtil.i("ReceiveFragment,client----startTransfer 生成列表");
//        mNotification = NotificationManager.generNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_START,mActivity.getService().isGroupOwner());
//        startNoti();
    }

    @Override
    public void onUpdateTransferProgress(int index) {
        if (!isStartAmimation) {
//            mDeviceCallBack.onRefreshMenu(false,false);
            isStartAmimation = true;
        }
        mIndex = index;
        mHandler.post(mFileSendRunnable);
    }

    Runnable mFileSendRunnable = new Runnable() {

        @Override
        public void run() {
            updateTransferProgressByIndex(mIndex);
            updateTotalTransferProgress();
            mHandler.postDelayed(this, 100);
        }
    };

    private void updateTransferProgressByIndex(int index) {
        if (mFileDatas.size() > 0) {
            FileInfo fileData = mFileDatas.get(index);
            int index1 = mReceivingListView.getFirstVisiblePosition();
            if (index - index1 >= 0) {
                View view = mReceivingListView.getChildAt(index - index1);
                if (null != view && view.getTag() instanceof FileTransferListAdapter.ViewHolder && fileData.getState() == Constants.FILE_TRANSFERING) {
                    FileTransferListAdapter.ViewHolder viewHolder = (FileTransferListAdapter.ViewHolder) view.getTag();
                    viewHolder.fileTransferProgressBar = (AmigoProgressBar) view.findViewById(R.id.file_transfer_progressbar);
                    viewHolder.fileTransferProgressBar.setVisibility(View.VISIBLE);
                    long maxProgress = fileData.getFileSize();
                    long progress = fileData.getTransferingSize();
                    if (Integer.MAX_VALUE < maxProgress) {
                        maxProgress = maxProgress / 10;
                        progress = progress / 10;
                    }
                    viewHolder.fileTransferProgressBar.setMax((int) maxProgress);
                    viewHolder.fileTransferProgressBar.setProgress((int) progress);
                }
            }
        }
    }

    private void updateTotalTransferProgress() {
        String progress = FileUtil.convertStorage(mFileReceiveData.getTotalTransferedSize());
        int percent = FileUtil.getTransferPercent(mFileReceiveData.getTotalTransferedSize(), mFileReceiveData.getAllFileSize());
//        tvTotalProgress.setText(mContext.getResources().getString(R.string.file_aready_transfer) + progress + mContext.getResources().getString(R.string.files));
        numberProgressBar.setProgress(percent);
        if("NaN%".equals(percent)){
            numberProgressBar.setProgress(0);
        }
    }

    private void updateAllCompleteProgress() {
//        tvTotalProgress.setText(mContext.getResources().getString(R.string.process_all_complete));
        numberProgressBar.setProgress(Constants.MAX_PROCESS);
    }
}
