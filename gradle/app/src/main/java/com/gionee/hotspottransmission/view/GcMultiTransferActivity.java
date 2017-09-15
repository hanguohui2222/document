package com.gionee.hotspottransmission.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Toast;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.TransferPagerAdapter;
import com.gionee.hotspottransmission.bean.BaseSendData;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.bean.MultiCommandInfo;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.WifiGcManager;
import com.gionee.hotspottransmission.service.GcMultiService;
import com.gionee.hotspottransmission.service.MultiBaseService;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.fragment.ReceiveMultiFragment;
import com.gionee.hotspottransmission.view.fragment.SendMultiFragment;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by luorw on 5/26/17.
 */
public class GcMultiTransferActivity extends BaseTransferActivity implements IMultiDeviceCallBack {
    private Intent mIntent;
    private ArrayList<Fragment> mFragmentPages;
    private SendMultiFragment mSendMultiFragment;
    private ReceiveMultiFragment mReceiveMultiFragment;
    private GcMultiService mGcMultiService;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        bindGcService();
    }


    @Override
    public void addFragments() {
        mViewPager = (ViewPager) findViewById(R.id.transfer_file_viewpager);
        mFragmentPages = new ArrayList<Fragment>();
        mSendMultiFragment = SendMultiFragment.newInstance();
        mReceiveMultiFragment = ReceiveMultiFragment.newInstance();
        mFragmentPages.clear();
        mFragmentPages.add(mSendMultiFragment);
        mFragmentPages.add(mReceiveMultiFragment);
        FragmentManager manager = getFragmentManager();
        mViewPagerAdapter = new TransferPagerAdapter(getApplicationContext(), manager, mFragmentPages);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setOnTouchListener(mTouchListener);
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void refreshFragmentMenu(int position){
        //当切换到接收/发送界面时，需要按照当前的接收/发送状态进行刷新menu
        if(position == 0){
            if(!FileSendData.getInstance().isSending()){
                mSendMultiFragment.setSendFilesEnable(true,true);
            }else{
                mSendMultiFragment.setSendFilesEnable(false,false);
            }
        }else if(position == 1){
            if(!FileReceiveData.getInstance().isReceiving()){
                if(FileSendData.getInstance().isSending()){
                    mReceiveMultiFragment.setSendFilesEnable(false,true);
                }else{
                    mReceiveMultiFragment.setSendFilesEnable(true,true);
                }
            }
        }
    }

    private void bindGcService() {
        mIntent = new Intent(this, GcMultiService.class);
//        startService(mIntent);
        bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindGcService() {
//        if (mGcMultiService != null) {
//            stopService(mIntent);
//        }
        unbindService(conn);
    }

    public ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("luorw , GcMultiTransferActivity,onServiceConnected-----------------");
            mGcMultiService = ((GcMultiService.GcMultiServiceBinder) service).getService();
            mGcMultiService.setMultiDeviceCallBack(GcMultiTransferActivity.this);
            mGcMultiService.watchOnline();
//            mGcMultiService.pingHost();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("luorw , GcMultiTransferActivity,onNewIntent-----------------");
        super.onNewIntent(intent);
        setIntent(intent);
        if (mGcMultiService != null) {
            send();
        }
//        if(Constants.ACTION_ENTER_FROM_NOTIFICATION.equals(action)){
//            int tabForNotification = intent.getIntExtra(Constants.CURRENT_TAB,0);
//            mViewPager.setCurrentItem(tabForNotification);
//        }
    }

    private void send(){
        Map<String,Socket> socketMap = SocketChannel.getInstance().mCommendSockets;
        ArrayList<FileInfo> fileSendList = FileSendData.getInstance().getFileSendList();
        long allFileSize = FileSendData.getInstance().getAllFileSize();
        for(String key : socketMap.keySet()){
            BaseSendData baseSendData = new BaseSendData();
            baseSendData.setFileSendList((ArrayList<FileInfo>) fileSendList.clone());
            baseSendData.setAllFileSize(allFileSize);
            FileMultiSendData.getInstance().setAllMultiFileData(key,baseSendData);
            MultiCommandInfo multiCommandInfo = new MultiCommandInfo();
            multiCommandInfo.command = Constants.SENDER_SEND_DESCRIBE;
            multiCommandInfo.responseFilesList = baseSendData.getFileSendList();
            mGcMultiService.createWriteCommand(key,multiCommandInfo);
            mGcMultiService.registerSendListeners(key,mSendMultiFragment.addSendLayout(key));
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("luorw1 , GcMultiTransferActivity , onDestroy   ");
        if (mGcMultiService != null) {
            LogUtil.i("luorw1 , GcMultiTransferActivity , unBindGcService   ");
            unBindGcService();
        }
    }


    @Override
    public void exit(boolean isFinishActivity) {
        LogUtil.i("luorw1 , GcMultiTransferActivity , exit   ");
        if(isFinishActivity){
            mGcMultiService.notifyOffline();
            finish();
        }
    }

    @Override
    public void online(final String deviceName) {
        LogUtil.i("GC , online  = "+deviceName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext,deviceName + getResources().getString(R.string.is_online),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void offline(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] info = msg.split(",");
                String str = "";
                boolean isHostOffline = TextUtils.equals(info[1],DeviceSp.getInstance().getHostIp(mContext));
                if(isHostOffline){
                    LogUtil.i("luorw , GC 读到 GO offline  ");
                    str = getResources().getString(R.string.connected_interrupt);
                }else{
                    LogUtil.i("luorw , GC 读到 GC offline  ");
                    str = info[2] + getResources().getString(R.string.is_offline);
                }
                Toast.makeText(mContext, str , Toast.LENGTH_SHORT).show();
                removeAssociation(info[3]);
                if(isHostOffline){
                    LogUtil.i("luorw , GC 读到 GO offline 刷新UI");
                }
            }
        });
    }

    @Override
    public MultiBaseService getMultiService() {
        return mGcMultiService;
    }

    @Override
    public void onFullStorage() {

    }

    @Override
    public void onRefreshMenu(boolean isTransferable, boolean isVisible) {

    }

    @Override
    public void onReadCommand(String key) {
        mGcMultiService.createReadCommand(key);
    }

    @Override
    public void onAddReceive(String key) {
        mGcMultiService.registerReceiveListeners(key,mReceiveMultiFragment.addReceiveLayout(key));
    }

}
