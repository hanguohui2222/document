package com.gionee.hotspottransmission.view.viewholder;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.FileTransferListAdapter;
import com.gionee.hotspottransmission.animation.NumberProgressBar;
import com.gionee.hotspottransmission.bean.BaseSendData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
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

public class MultiSendViewHolder implements ITransferListener {
    private Context mContext;
    private ListView mSendingListView;
    private NumberProgressBar numberProgressBar;
    private LinearLayout mLayoutTransfer;
    private FileTransferListAdapter mListAdapter;
    private TextView receiveDeviceName;
    private ArrayList<FileInfo> mFileDatas;
    private Handler mHandler = new Handler();
    private int mIndex;
    private Notification mNotification;
    private boolean isStartNotification;
    private boolean isStartAmimation;
    private BaseSendData mFileSendData;
    private String mKey;
    private BaseTransferActivity mActivity;
    private ImageView mCancelAll;

    public MultiSendViewHolder(Context context, String key,BaseTransferActivity activity) {
        this.mContext = context.getApplicationContext();
        this.mKey = key;
        this.mActivity = activity;
    }

    public View createSendLayout() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_multi_transfer_list, null);
        initSendView(view);
        return view;
    }

    private void initSendView(View view) {
        mLayoutTransfer = (LinearLayout) view.findViewById(R.id.layout_transfer);
        mSendingListView = (ListView) view.findViewById(R.id.lv_file_transfering);
        numberProgressBar = (NumberProgressBar) view.findViewById(R.id.number_progressbar);
        receiveDeviceName = (TextView)view.findViewById(R.id.receive_device_name);
        mCancelAll = (ImageView) view.findViewById(R.id.cancel_transfer_multi);
        initData();
    }

    private void initData(){
        numberProgressBar.setMax(Constants.MAX_PROCESS);
        receiveDeviceName.setText(SocketChannel.getInstance().mName.get(mKey));
        mLayoutTransfer.setVisibility(View.VISIBLE);
        mFileSendData = FileMultiSendData.getInstance().getFileSendData(mKey);
        mFileDatas = (ArrayList<FileInfo>) mFileSendData.getFileSendList().clone();
        LogUtil.i("luorw , initData--------------mFileDatas = "+mFileDatas.size());
        mListAdapter = new FileTransferListAdapter(mContext, mFileDatas, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelByIndex(v);
            }
        });
        mSendingListView.setAdapter(mListAdapter);
        mCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAllCancelDialog();
            }
        });
    }

    private void cancelByIndex(View v){
        int index = (Integer) v.getTag();
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo(Constants.SENDER_CANCEL_BY_INDEX);
        multiCommandInfo.responseDeviceImei = mKey;
        multiCommandInfo.responseIndex = index;
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
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo(Constants.SENDER_CANCEL_ALL);
        multiCommandInfo.responseDeviceImei = mKey;
        multiCommandInfo.responseIndex = mFileSendData.getmCurrentSendIndex();
        mActivity.getMultiService().createWriteCommand(mKey,multiCommandInfo);
        onCancelAll(multiCommandInfo.responseIndex);
    }

    private void cancelNoti() {
        if (mNotification != null && !isStartNotification) {
            NotificationManager.cancelNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER);
        }
    }

    private void startNoti() {
        if (mNotification != null && isStartNotification && !FileSendData.getInstance().isAllSendComplete()) {
            NotificationManager.startNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, mNotification);
        }
    }

    @Override
    public void onCancelByIndex(int index) {
        FileInfo info = mFileDatas.get(index);
        if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE) {
            mFileSendData.updateAllFileSize(info.getFileSize());
        } else {
            mFileSendData.updateAllFileSize(info.getFileSize() - info.getTransferingSize());
        }
        info.setState(Constants.FILE_TRANSFER_CANCEL);
        mListAdapter.notifyDataSetChanged();
//        mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, info.getState(),mActivity.getService().isGroupOwner());
//        startNoti();
    }

    @Override
    public void onCancelAll(int currentTransferIndex) {
        mFileSendData.setCancelAllSend(true);
        mFileSendData.updateCancelAllState(currentTransferIndex);
        mListAdapter.notifyDataSetChanged();
        LogUtil.i("SendFragment,isAllComplete = " + mFileSendData.isAllSendComplete());
        if (!mFileSendData.isAllSendComplete()) {
            LogUtil.i("SendFragment,onCancelAll------isAllComplete---------");
            onTransferAllComplete();
        }
    }

    @Override
    public void onTransferCompleteByIndex(int index) {
        mListAdapter.notifyDataSetChanged();
        //modified by luorw for GNSPR #42744 begin
        int size = mFileDatas.size();
        if (size > 0 && index < size) {
            //modified by luorw for GNSPR #42744 end
            FileInfo info = mFileDatas.get(index);
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, info.getState(),mActivity.getService().isGroupOwner());
            LogUtil.i("luorw , multiSendFragment,已完成 第  " + index + "  传输 需要更新列表 ");
//            startNoti();
        }
    }

    @Override
    public void onTransferAllComplete() {
        LogUtil.i("luorw , multiSendFragment , onTransferAllComplete---------");
        updateAllCompleteProgress();
//        mDeviceCallBack.onRefreshMenu(true,true);
        mHandler.removeCallbacks(mFileSendRunnable);
//            if (!isTransferBreak) {
        saveTransferHistory();
//            }
        if (mFileSendData.isConnected()) {
            LogUtil.i("luorw , multiSendFragment,mAllCompleteFlag = true;---------");
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_ALL_COMPLETE,mActivity.getService().isGroupOwner());
//            startNoti();
            mFileSendData.setAllSendComplete(true);
        } else {
            LogUtil.i("luorw , multiSendFragment,异常中断的传输,onTransferAllComplete---------");
            //异常中断的传输，需要等状态全部更新完才能断开服务
//            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_BREAK,mActivity.getService().isGroupOwner());
//            startNoti();
//            mActivity.exit(false);
        }
        mListAdapter.notifyDataSetChanged();
        mActivity.getMultiService().unRegisterSendListener(mKey);
    }

    @Override
    public void onReadFileListSuccess() {
        mFileSendData.setAllSendComplete(false);
        isStartAmimation = false;
        mLayoutTransfer.setVisibility(View.VISIBLE);
//        mDeviceCallBack.onRefreshMenu(false,false);
        mListAdapter.notifyDataSetChanged();
        LogUtil.i("luorw , multiSendFragment----startTransfer 生成列表");
//        mNotification = NotificationManager.generNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_START,mActivity.getService().isGroupOwner());
//        startNoti();
    }

    @Override
    public void onUpdateTransferProgress(int index) {
        LogUtil.i("luorw , multiSendFragment----onUpdateTransferProgress index = "+index);
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
            int index1 = mSendingListView.getFirstVisiblePosition();
            if (index - index1 >= 0) {
                View view = mSendingListView.getChildAt(index - index1);
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
//        String progress = FileUtil.convertStorage(mFileSendData.getTotalTransferedSize());
        int percent = FileUtil.getTransferPercent(mFileSendData.getTotalTransferedSize(), mFileSendData.getAllFileSize());
//        tvTotalProgress.setText(mContext.getResources().getString(R.string.file_aready_transfer) + progress + mContext.getResources().getString(R.string.files));
        numberProgressBar.setProgress(percent);
        if ("NaN%".equals(percent)) {
            numberProgressBar.setProgress(0);
        }
    }

    private void updateAllCompleteProgress() {
//        tvTotalProgress.setText(mContext.getResources().getString(R.string.process_all_complete));
        numberProgressBar.setProgress(Constants.MAX_PROCESS);
    }

    private void saveTransferHistory() {
        HistoryInfo historyInfo = new HistoryInfo();
        historyInfo.files.clear();
        historyInfo.files.addAll(mFileDatas);
        historyInfo.isSender = true;
        Calendar calendar = Calendar.getInstance();
        historyInfo.date = calendar.getTime();
        historyInfo.fileCount = historyInfo.files.size();
        historyInfo.fileSize = FileSendData.getInstance().getTotalTransferedSize();
        historyInfo.deviceAddress = DeviceSp.getInstance().getConnectedDeviceAddress(mContext);
        historyInfo.deviceName = DeviceSp.getInstance().getConnectedDeviceName(mContext);
        HistoryBiz historyBiz = new HistoryBiz(mContext);
        historyBiz.addHistoryRecord(historyInfo);
    }
}
