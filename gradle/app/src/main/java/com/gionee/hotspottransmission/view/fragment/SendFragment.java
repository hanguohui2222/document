package com.gionee.hotspottransmission.view.fragment;

import android.app.Fragment;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.hotspottransmission.adapter.FileTransferListAdapter;
import com.gionee.hotspottransmission.animation.NumberProgressBar;
import com.gionee.hotspottransmission.animation.OnProgressBarListener;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.DeviceCallBackAdapter;
import com.gionee.hotspottransmission.callback.IDeviceCallBack;
import com.gionee.hotspottransmission.callback.ITransferListener;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.history.biz.HistoryBiz;
import com.gionee.hotspottransmission.manager.NotificationManager;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.view.BaseTransferActivity;
import com.gionee.hotspottransmission.view.GoTransferActivity;
import com.gionee.hotspottransmission.view.SelectFilesActivity;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoProgressBar;

/**
 * Created by luorw on 5/23/17.
 */
public class SendFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private ListView mSendingListView;
    private NumberProgressBar mNumberProgressBar;
    private FileTransferListAdapter mListAdapter;
    private TextView mtvTotalProgress;
    private TextView mtvReceiveDeviceName;
    private FileSendData mFileSendData;
    private ArrayList<FileInfo> mFileDatas;
    private Handler mHandler = new Handler();
    private int mIndex;
    private Notification mNotification;
    private RelativeLayout mLayoutBlankPage;
    private LinearLayout mLayoutTransfer;
    private BaseTransferActivity mActivity;
    private int MAX_PROCESS = 100;
    private boolean isStartNotification;
    //    private boolean isTransferBreak = false;
    private boolean isStartAmimation;

    private RelativeLayout mLayoutSendFiles;
    private RelativeLayout mLayoutCancelAll;
    private ImageView mIvSendFiles;
    private TextView mTvSendFiles;
    private ImageView mIvCancelAll;
    private TextView mTvCancelAll;
    private ProgressBar mPbLoading;

    public static SendFragment newInstance() {
        SendFragment pageFragment = new SendFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("SendFragment-----onCreate");
        mContext = getActivity().getApplicationContext();
        mActivity = (BaseTransferActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);
        return view;
    }

    private void initView() {
        mLayoutTransfer = (LinearLayout) getView().findViewById(R.id.layout_transfer);
        mLayoutBlankPage = (RelativeLayout) getView().findViewById(R.id.blank_page_layout);
        mSendingListView = (ListView) getView().findViewById(R.id.lv_file_transfering);
        mNumberProgressBar = (NumberProgressBar) getView().findViewById(R.id.number_progressbar);
        mNumberProgressBar.setMax(MAX_PROCESS);
        mtvTotalProgress = (TextView) getView().findViewById(R.id.tv_total_progress);
        mtvReceiveDeviceName = (TextView) getView().findViewById(R.id.receive_device_name);
        mLayoutSendFiles = (RelativeLayout) getView().findViewById(R.id.rl_send);
        mLayoutCancelAll = (RelativeLayout) getView().findViewById(R.id.rl_clear);
        mIvSendFiles = (ImageView) getView().findViewById(R.id.iv_send);
        mIvCancelAll = (ImageView) getView().findViewById(R.id.iv_clear);
        mTvSendFiles = (TextView) getView().findViewById(R.id.tv_send);
        mTvCancelAll = (TextView) getView().findViewById(R.id.tv_clear);
        mPbLoading = (ProgressBar)getView().findViewById(R.id.loading_progressbar);
        mLayoutSendFiles.setOnClickListener(this);
        mLayoutCancelAll.setOnClickListener(this);
        setSendFilesEnable(false,true);
        if(getActivity() instanceof GoTransferActivity){
            mLayoutBlankPage.setVisibility(View.GONE);
        }else{
            mPbLoading.setVisibility(View.GONE);
        }
    }

    private void initData() {
        mFileSendData = FileSendData.getInstance();
        mFileDatas = mFileSendData.getFileSendList();
        mListAdapter = new FileTransferListAdapter(mContext, mFileDatas, true, this);
        mSendingListView.setAdapter(mListAdapter);
    }


    public void setSendFilesEnable(boolean isEnable,boolean isVisible){
        if(isVisible){
            mLayoutSendFiles.setVisibility(View.VISIBLE);
        }else{
            mLayoutSendFiles.setVisibility(View.GONE);
        }
        mLayoutSendFiles.setClickable(isEnable);
        mIvSendFiles.setEnabled(isEnable);
        if(isEnable){
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        }else{
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_disable_text_color));
        }
    }

    private void setCancelAllEnable(boolean isEnable,boolean isVisible){
        if(isVisible){
            mLayoutCancelAll.setVisibility(View.VISIBLE);
        }else{
            mLayoutCancelAll.setVisibility(View.GONE);
        }
        mLayoutCancelAll.setClickable(isEnable);
        mIvCancelAll.setEnabled(isEnable);
        if(isEnable){
            mTvCancelAll.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        }else{
            mTvCancelAll.setTextColor(getResources().getColor(R.color.menu_disable_text_color));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_transfer_cancel:
                cancelByIndex(v);
                break;
            case R.id.rl_send:
                reSend();
                break;
            case R.id.rl_clear:
                makeAllCancelDialog();
                break;
            default:
                break;
        }
    }

    private void cancelByIndex(View v) {
        int index = (Integer) v.getTag();
        mActivity.getService().senderWriteCommand(index);
        mSendListener.onCancelByIndex(index);
    }

    private void reSend() {
        Intent intent = new Intent();
        intent.setClass(mContext, SelectFilesActivity.class);
        if (FileReceiveData.getInstance().isConnected() || mActivity.getService().isGroupOwner()) {
            intent.setAction(Constants.ACTION_RESEND_FILES);
        }
        intent.putExtra(Constants.IS_GROUP_OWNER, mActivity.getService().isGroupOwner());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        refreshForResend();
    }

    /**
     * 全部取消提示
     */
    public void makeAllCancelDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(mContext, R.style.AmigoDialogTheme);
        builder.setMessage(this.getResources().getString(R.string.cancel_all_transfer));
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.i("SendFragment,makeAllCancelDialog,确定取消---------");
                cancelAll();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void cancelAll() {
        mActivity.getService().senderWriteCommand(Constants.CANCEL_ALL_TAG);
        mSendListener.onCancelAll(FileSendData.getInstance().getmCurrentSendIndex());
    }


    @Override
    public void onResume() {
        super.onResume();
        isStartNotification = false;
        cancelNoti();
        if (!FileSendData.getInstance().isAllSendComplete() && isStartAmimation) {
            mHandler.post(mFileSendRunnable);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isStartNotification = true;
        startNoti();
        mHandler.removeCallbacks(mFileSendRunnable);
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

    public ITransferListener mSendListener = new ITransferListener() {
        @Override
        public void onCancelByIndex(int index) {
            FileInfo info = FileSendData.getInstance().getFileSendList().get(index);
            if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE) {
                FileSendData.getInstance().updateAllFileSize(info.getFileSize());
            } else {
                FileSendData.getInstance().updateAllFileSize(info.getFileSize() - info.getTransferingSize());
            }
            info.setState(Constants.FILE_TRANSFER_CANCEL);
            mListAdapter.notifyDataSetChanged();
            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, info.getState(),mActivity.getService().isGroupOwner());
            startNoti();
        }

        @Override
        public void onCancelAll(int currentTransferIndex) {
            FileSendData.getInstance().setCancelAllSend(true);
            FileSendData.getInstance().updateCancelAllState(currentTransferIndex);
            mListAdapter.notifyDataSetChanged();
            LogUtil.i("SendFragment,isAllComplete = " + FileSendData.getInstance().isAllSendComplete());
            if (!FileSendData.getInstance().isAllSendComplete()) {
                LogUtil.i("SendFragment,onCancelAll------isAllComplete---------");
                onTransferAllComplete();
            }
        }

        @Override
        public void onTransferCompleteByIndex(int index) {
            mListAdapter.notifyDataSetChanged();
            //modified by luorw for GNSPR #42744 begin
            int size = FileSendData.getInstance().getFileSendList().size();
            if (size > 0 && index < size) {
                //modified by luorw for GNSPR #42744 end
                FileInfo info = FileSendData.getInstance().getFileSendList().get(index);
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, info.getState(),mActivity.getService().isGroupOwner());
                LogUtil.i("SendFragment,已完成 第  " + index + "  传输 需要更新列表 ");
                startNoti();
            }
        }

        @Override
        public void onTransferAllComplete() {
            updateAllCompleteProgress();
            mDeviceCallBack.onRefreshMenu(true,true);
            mHandler.removeCallbacks(mFileSendRunnable);
//            if (!isTransferBreak) {
            saveTransferHistory();
//            }
            if (FileSendData.getInstance().isConnected()) {
                LogUtil.i("SendFragment,mAllCompleteFlag = true;---------");
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_ALL_COMPLETE,mActivity.getService().isGroupOwner());
                startNoti();
                FileSendData.getInstance().setAllSendComplete(true);
            } else {
                LogUtil.i("SendFragment,异常中断的传输,onTransferAllComplete---------");
                //异常中断的传输，需要等状态全部更新完才能断开服务
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_BREAK,mActivity.getService().isGroupOwner());
                startNoti();
                mActivity.exit(false);
            }
            mListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onReadFileListSuccess() {
            FileSendData.getInstance().setAllSendComplete(false);
            isStartAmimation = false;
            mPbLoading.setVisibility(View.GONE);
            mLayoutBlankPage.setVisibility(View.GONE);
            mLayoutTransfer.setVisibility(View.VISIBLE);
            mDeviceCallBack.onRefreshMenu(false,false);
            mtvReceiveDeviceName.setText(DeviceSp.getInstance().getConnectedDeviceName(mContext));
            mFileDatas = FileSendData.getInstance().getFileSendList();
            mListAdapter.setFileDatas(mFileDatas);
            mListAdapter.notifyDataSetChanged();
            LogUtil.i("SendFragment----startTransfer 生成列表");
            mNotification = NotificationManager.generNotification(mContext, NotificationManager.NOTIFY_ID_FILE_SENDER, Constants.FILE_TRANSFER_START,mActivity.getService().isGroupOwner());
            startNoti();
        }

        @Override
        public void onUpdateTransferProgress(int index) {
            LogUtil.i("SendFragment----onUpdateTransferProgress index = "+index);
            if (!isStartAmimation) {
                mDeviceCallBack.onRefreshMenu(false,false);
                isStartAmimation = true;
            }
            mIndex = index;
            mHandler.post(mFileSendRunnable);
        }
    };

    Runnable mFileSendRunnable = new Runnable() {

        @Override
        public void run() {
            updateTransferProgressByIndex(mIndex);
            updateTotalTransferProgress();
            mHandler.postDelayed(this, 100);
        }
    };

    private void updateTransferProgressByIndex(int index) {
        LogUtil.i("SendFragment----updateTransferProgressByIndex");
        if (mFileDatas.size() > 0) {
            FileInfo fileData = mFileDatas.get(index);
            int index1 = mSendingListView.getFirstVisiblePosition();
            LogUtil.i("SendFragment----updateTransferProgressByIndex , index1 = "+index1);
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
        String progress = FileUtil.convertStorage(mFileSendData.getTotalTransferedSize());
        int percent = FileUtil.getTransferPercent(mFileSendData.getTotalTransferedSize(), mFileSendData.getAllFileSize());
        mtvTotalProgress.setText(getResources().getString(R.string.file_aready_transfer) + progress + getResources().getString(R.string.files));
        mNumberProgressBar.setProgress(percent);
        if ("NaN%".equals(percent)) {
            mNumberProgressBar.setProgress(0);
        }
    }

    private void updateAllCompleteProgress() {
        mtvTotalProgress.setText(this.getResources().getString(R.string.process_all_complete));
        mNumberProgressBar.setProgress(MAX_PROCESS);
    }

    private void saveTransferHistory() {
        ArrayList<FileInfo> Files = FileSendData.getInstance().getFileSendList();
        HistoryInfo historyInfo = new HistoryInfo();
        historyInfo.files.clear();
        historyInfo.files.addAll(Files);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFileSendRunnable);
    }

    public void refreshForResend() {
        mtvTotalProgress.setText(getResources().getString(R.string.file_aready_transfer) + "0B" + getResources().getString(R.string.files));
        mNumberProgressBar.setProgress(0);
        FileSendData.getInstance().clearAllFiles();
        mListAdapter.notifyDataSetChanged();
        FileSendData.getInstance().setCancelAllSend(false);
        isStartAmimation = false;
    }

    public IDeviceCallBack mDeviceCallBack = new DeviceCallBackAdapter() {
        @Override
        public void onRefreshMenu(boolean isTransferable,boolean isVisible) {
            setSendFilesEnable(isTransferable,isVisible);
            setCancelAllEnable(!isTransferable,!isVisible);
        }

        @Override
        public void onWifiUnAvailable() {
            Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
            mActivity.exit(false);
        }

        @Override
        public void onExit() {
            //added by luorw for GNSPR #41659 begin
//            SharedPreferences sharedPreferences = getSharedPreferences(Constants.CONNECT_STATUS, Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(Constants.IS_CONNECTED, false);
//            editor.commit();
            //added by luorw for GNSPR #41659 end
            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    if (clientService != null && clientService.isAllComplete) {
                        LogUtil.i("SendFragment,客户端传输已经中断---------");
                        //toast放在内部，避免多次弹出toast
                        Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
                        if(mActivity != null){
                            mActivity.exit(false);
                        }
//                    }
                }
            });
        }
    };


}
