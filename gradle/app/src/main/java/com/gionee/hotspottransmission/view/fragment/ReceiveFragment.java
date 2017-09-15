package com.gionee.hotspottransmission.view.fragment;

import android.app.Fragment;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.FileTransferListAdapter;
import com.gionee.hotspottransmission.animation.NumberProgressBar;
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
import com.gionee.hotspottransmission.view.BaseTransferActivity;
import com.gionee.hotspottransmission.view.GcTransferActivity;
import com.gionee.hotspottransmission.view.SelectFilesActivity;
import java.util.ArrayList;
import java.util.Calendar;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoProgressBar;

/**
 * Created by luorw on 5/23/17.
 */
public class ReceiveFragment extends Fragment implements View.OnClickListener{
    private Context mContext;
    private ListView mReceivingListView;
    private FileTransferListAdapter mListAdapter;
    private TextView mtvTotalProgress;
    private NumberProgressBar mNumberProgressBar;
    private TextView mSendDeviceName;
    private FileReceiveData mFileReceiveData;
    private ArrayList<FileInfo> mFileDatas;
    private Handler mHandler = new Handler();
    private int mIndex;
    private Notification mNotification;
    private RelativeLayout mLayoutBlankPage;
    private LinearLayout mLayoutTransfer;
    private BaseTransferActivity mActivity;
    private int MAX_PROGRESS = 100;
    private boolean isStartNotification;
    private boolean isStartAmimation;
//    private boolean isTransferBreak = false;

    private RelativeLayout mLayoutSendFiles;
    private RelativeLayout mLayoutCancelAll;
    private ImageView mIvSendFiles;
    private TextView mTvSendFiles;
    private ImageView mIvCancelAll;
    private TextView mTvCancelAll;
    private ProgressBar mPbLoading;

    public static ReceiveFragment newInstance() {
        ReceiveFragment pageFragment = new ReceiveFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("ReceiveFragment-----onCreate");
        mContext = getActivity().getApplicationContext();
        mActivity = (BaseTransferActivity)getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);
        return view;
    }

    private void initView() {
        mLayoutTransfer = (LinearLayout)getView().findViewById(R.id.layout_transfer);
        mLayoutBlankPage = (RelativeLayout)getView().findViewById(R.id.blank_page_layout);
        mReceivingListView = (ListView) getView().findViewById(R.id.lv_file_transfering);
        mtvTotalProgress = (TextView) getView().findViewById(R.id.tv_total_progress);
        mNumberProgressBar = (NumberProgressBar) getView().findViewById(R.id.number_progressbar);
        mNumberProgressBar.setMax(MAX_PROGRESS);
        mSendDeviceName = (TextView) getView().findViewById(R.id.send_device_name);
        mLayoutSendFiles = (RelativeLayout)getView().findViewById(R.id.rl_send);
        mLayoutCancelAll = (RelativeLayout)getView().findViewById(R.id.rl_clear);
        mIvSendFiles = (ImageView)getView().findViewById(R.id.iv_send);
        mIvCancelAll = (ImageView)getView().findViewById(R.id.iv_clear);
        mTvSendFiles = (TextView)getView().findViewById(R.id.tv_send);
        mTvCancelAll = (TextView)getView().findViewById(R.id.tv_clear);
        mPbLoading = (ProgressBar)getView().findViewById(R.id.loading_progressbar);
        mLayoutSendFiles.setOnClickListener(this);
        mLayoutCancelAll.setOnClickListener(this);
        setSendFilesEnable(false,true);
        if(getActivity() instanceof GcTransferActivity){
            mLayoutBlankPage.setVisibility(View.GONE);
        }else{
            mPbLoading.setVisibility(View.GONE);
        }
    }

    private void initData(){
        mFileReceiveData = FileReceiveData.getInstance();
        mFileDatas = mFileReceiveData.getFileReceiveList();
        mListAdapter = new FileTransferListAdapter(mContext, mFileDatas, false, this);
        mReceivingListView.setAdapter(mListAdapter);
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

    private void cancelByIndex(View v){
        int index = (Integer) v.getTag();
        mActivity.getService().receiverWriteCommand(index);
        mReceiveListener.onCancelByIndex(index);
    }

    private void reSend() {
        Intent intent = new Intent();
        intent.setClass(mContext, SelectFilesActivity.class);
        if(FileReceiveData.getInstance().isConnected()){
            intent.setAction(Constants.ACTION_RESEND_FILES);
        }
        intent.putExtra(Constants.IS_GROUP_OWNER,mActivity.getService().isGroupOwner());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
                LogUtil.i("ReceiveFragment , makeAllCancelDialog,确定取消---------");
                cancelAll();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void cancelAll() {
        mActivity.getService().receiverWriteCommand(Constants.CANCEL_ALL_TAG);
        mReceiveListener.onCancelAll(FileReceiveData.getInstance().getmCurrentReceiveIndex());
    }

    public ITransferListener mReceiveListener = new ITransferListener() {
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
            mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, info.getState(),mActivity.getService().isGroupOwner());
            startNoti();
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
            ArrayList<FileInfo> list = mFileReceiveData.getFileReceiveList();
            if(list.size() > 0){
                FileInfo info = list.get(index);
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, info.getState(),mActivity.getService().isGroupOwner());
                LogUtil.i("ReceiveFragment已完成 第  " + index + "  传输 需要更新列表 ");
                startNoti();
            }
        }

        @Override
        public void onTransferAllComplete() {
            LogUtil.i("ReceiveFragment,onTransferAllComplete---------");
            updateAllCompleteProgress();
            if(FileSendData.getInstance().isSending()){
                mDeviceCallBack.onRefreshMenu(false,true);
            }else{
                mDeviceCallBack.onRefreshMenu(true,true);
            }
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
            if (FileReceiveData.getInstance().isConnected()) {
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_ALL_COMPLETE,mActivity.getService().isGroupOwner());
                startNoti();
                mFileReceiveData.setAllReceiveComplete(true);
            } else {
                //异常中断的传输，需要等状态全部更新完才能断开服务
                LogUtil.i("ReceiveFragment,异常中断的传输,onTransferAllComplete---------");
                mNotification = NotificationManager.updateNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_BREAK,mActivity.getService().isGroupOwner());
                startNoti();
                mActivity.exit(false);
            }
        }

        @Override
        public void onReadFileListSuccess() {
            mFileReceiveData.setAllReceiveComplete(false);
            isStartAmimation = false;
            mPbLoading.setVisibility(View.GONE);
            mLayoutBlankPage.setVisibility(View.GONE);
            mLayoutTransfer.setVisibility(View.VISIBLE);
            mDeviceCallBack.onRefreshMenu(false,false);
            mSendDeviceName.setText(DeviceSp.getInstance().getConnectedDeviceName(mContext));
            mFileDatas = mFileReceiveData.getFileReceiveList();
            mListAdapter.setFileDatas(mFileDatas);
            mListAdapter.notifyDataSetChanged();
            LogUtil.i("ReceiveFragment,client----startTransfer 生成列表");
            mNotification = NotificationManager.generNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, Constants.FILE_TRANSFER_START,mActivity.getService().isGroupOwner());
            startNoti();
        }

        @Override
        public void onUpdateTransferProgress(int index) {
            if (!isStartAmimation) {
                mDeviceCallBack.onRefreshMenu(false,false);
                isStartAmimation = true;
            }
            mIndex = index;
            mHandler.post(mFileSendRunnable);
        }
    };

    private void saveTransferHistory() {
        ArrayList<FileInfo> Files = FileReceiveData.getInstance().getFileReceiveList();
        HistoryInfo historyInfo = new HistoryInfo();
        historyInfo.files.clear();
        historyInfo.files.addAll(Files);
        historyInfo.isSender = false;
        Calendar calendar = Calendar.getInstance();
        historyInfo.date = calendar.getTime();
        historyInfo.fileCount = historyInfo.files.size();
        historyInfo.fileSize = FileReceiveData.getInstance().getTotalTransferedSize();
        historyInfo.deviceAddress = DeviceSp.getInstance().getConnectedDeviceAddress(mContext);
        historyInfo.deviceName = DeviceSp.getInstance().getConnectedDeviceName(mContext);
        HistoryBiz historyBiz = new HistoryBiz(mContext);
        historyBiz.addHistoryRecord(historyInfo);
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
        mtvTotalProgress.setText(getResources().getString(R.string.file_aready_transfer) + progress + getResources().getString(R.string.files));
        mNumberProgressBar.setProgress(percent);
        if("NaN%".equals(percent)){
            mNumberProgressBar.setProgress(0);
        }
    }

    private void updateAllCompleteProgress() {
        mtvTotalProgress.setText(mContext.getResources().getString(R.string.process_all_complete));
        mNumberProgressBar.setProgress(MAX_PROGRESS);
    }


    private void cancelNoti() {
        if (mNotification != null && !isStartNotification) {
            NotificationManager.cancelNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER);
        }
    }

    private void startNoti() {
        if (mNotification != null && isStartNotification && !mFileReceiveData.isAllReceiveComplete()) {
            NotificationManager.startNotification(mContext, NotificationManager.NOTIFY_ID_FILE_RECEIVER, mNotification);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isStartNotification = false;
        cancelNoti();
        if (!mFileReceiveData.isAllReceiveComplete() && isStartAmimation) {
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFileSendRunnable);
    }

    public IDeviceCallBack mDeviceCallBack = new DeviceCallBackAdapter() {
        @Override
        public void onRefreshMenu(boolean isTransferable,boolean isVisible) {
            setSendFilesEnable(isTransferable,isVisible);
            setCancelAllEnable(!isTransferable,!isVisible);
        }

        @Override
        public void onFullStorage() {
            //更新所有未传输的文件为传输失败的状态，并保存到历史纪录
            LogUtil.i("ReceiveFragment,onFullStorage---------------");
            FileReceiveData.getInstance().updateDisconnectAllState();
            mListAdapter.notifyDataSetChanged();
            mReceiveListener.onTransferAllComplete();
            mActivity.exit(true);
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
                    Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
//                    if (serverService != null && serverService.isAllComplete) {
//                        stopService(intent);
//                    }
                    mActivity.exit(false);
                }
            });
        }
    };
}
