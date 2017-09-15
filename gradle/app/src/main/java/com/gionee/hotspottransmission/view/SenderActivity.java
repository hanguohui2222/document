package com.gionee.hotspottransmission.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.app.AmigoProgressDialog;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.animation.RippleLayout;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IGoStatusCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.HotspotManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.service.GroupOwnerService;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileUtil;

import java.lang.ref.WeakReference;

/**
 * Created by luorw on 5/10/17.
 */
public class SenderActivity extends AmigoActivity implements IGoStatusCallBack {
    private TextView tv_selfHotspot;
    private TextView tv_selfDeviceName;
    private RippleLayout mRippleLayout;
    private Context mContext;
    private Intent mIntent;
    private GroupOwnerService mGoService;
    private AmigoProgressDialog mDialog;
    private SenderScanHandler mHandler;
    private boolean isGroupTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        setContentView(R.layout.activity_sender);
        parseIntent();
        initActionBar();
        initViews();
        bindGoService();
    }

    private void parseIntent() {
        final Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)
                || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
//            showReadyDialog();
            ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
                @Override
                public void run() {
                    boolean result = FileSendData.getInstance().createTransferData(
                            getApplicationContext(), intent);
                    if (!result) {
                        Message msg = mHandler.obtainMessage(Constants.FILE_CAN_NOT_SHARE);
                        mHandler.sendMessage(msg);
                    }
                    FileSendData.getInstance().calculateAllFileSize();
                    FileSendData.getInstance().setAllFileIcon(
                            getApplicationContext());
//                    if(mDialog != null && mDialog.isShowing()){
//                        Message msg = mHandler.obtainMessage(Constants.FILE_SEND_DATA_IS_READY);
//                        mHandler.sendMessage(msg);
//                    }
                }
            });
        }else if(Constants.ACTION_GROUP_TRANSFER.equals(action)){
            isGroupTransfer = true;
            FileUtil.setPasswordForHotspot(null);
        }
    }

    private void showReadyDialog(){
        mDialog = new AmigoProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.file_is_ready_transfer));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static class SenderScanHandler extends Handler {
        private WeakReference<SenderActivity> reference;

        public SenderScanHandler(SenderActivity activity) {
            reference = new WeakReference<SenderActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final SenderActivity activity = reference.get();
            if (activity != null) {
                switch (msg.what) {
                    case Constants.FILE_CAN_NOT_SHARE:
                        Toast.makeText(activity,
                                R.string.wifi_can_not_share, Toast.LENGTH_SHORT).show();
                        activity.finish();
                        break;
                    case Constants.FILE_SEND_DATA_IS_READY:
                        if(activity.mDialog != null){
                            activity.mDialog.dismiss();
                        }
                        break;
                }
            }
        }
    }

    private void bindGoService() {
        mIntent = new Intent(this, GroupOwnerService.class);
        startService(mIntent);
        bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindGoService() {
        if (mGoService != null) {
            stopService(mIntent);
        }
        unbindService(conn);
    }

    public ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGoService = ((GroupOwnerService.GoServiceBinder) service).getService();
            mGoService.setmIGoStatusCallBack(SenderActivity.this);
            mGoService.workForScan(isGroupTransfer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initViews() {
        mRippleLayout = (RippleLayout) findViewById(R.id.ripple_layout);
        tv_selfHotspot = (TextView) findViewById(R.id.create_self_hotspot);
        tv_selfDeviceName = (TextView) findViewById(R.id.self_device_name);
        tv_selfDeviceName.setText(DeviceSp.getInstance().getDeviceName(mContext));
        tv_selfHotspot.setText(getResources().getString(R.string.self_hotspot_init));
        mRippleLayout.startRippleAnimation();
    }

    private void initActionBar() {
        AmigoActionBar actionBar = getAmigoActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowCustomEnabled(true);
        if(isGroupTransfer){
            actionBar.setTitle(R.string.create_group);
        }else{
            actionBar.setTitle(R.string.sender_search_action_title);
        }
        actionBar.show();
        actionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭热点，并退出本界面
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //关闭热点，并退出本界面
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindGoService();
    }

    @Override
    public void onHotSpotEnabled(String ssid) {
        //热点成功开启
        String apStr = getResources().getString(R.string.self_hotspot) + ssid;
        tv_selfHotspot.setText(apStr);
        //等待接收端连接,开始接收GC端发来的指令
        mGoService.notifyConnectSuccess();
    }

    @Override
    public void onWifiConnected(String clientIp) {
        Intent intent = new Intent();
        if(isGroupTransfer){
            intent.setClass(this, GoMultiTransferActivity.class);
        }else{
            intent.setClass(this, GoTransferActivity.class);
            intent.setAction(Constants.ACTION_SEND_FROM_SCAN);
            intent.putExtra(Constants.CLIENT_IP,clientIp);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void online(final String name) {
        if(isGroupTransfer){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,name+mContext.getResources().getString(R.string.is_online),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onWifiDisconnected() {
        FileSendData.getInstance().setConnected(false);
        FileReceiveData.getInstance().setConnected(false);
    }

}
