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
import com.gionee.hotspottransmission.manager.HotspotManager;
import com.gionee.hotspottransmission.service.GoMultiService;
import com.gionee.hotspottransmission.service.MultiBaseService;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.fragment.ReceiveMultiFragment;
import com.gionee.hotspottransmission.view.fragment.SendMultiFragment;
import com.gionee.hotspottransmission.constants.Constants;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by luorw on 5/26/17.
 */
public class GoMultiTransferActivity extends BaseTransferActivity implements IMultiDeviceCallBack {
    private Intent mIntent;
    private ArrayList<Fragment> mFragmentPages;
    private SendMultiFragment mSendMultiFragment;
    private ReceiveMultiFragment mReceiveMultiFragment;
    private GoMultiService mGoMultiService;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        bindGoService();
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
    public void refreshFragmentMenu(int position) {
        //当切换到接收or发送界面时，需要按照当前的接收or发送状态进行刷新menu
        if (position == 0) {
            if (!FileSendData.getInstance().isSending()) {
                mSendMultiFragment.setSendFilesEnable(true, true);
            } else {
                mSendMultiFragment.setSendFilesEnable(false, false);
            }
        } else if (position == 1) {
            if (!FileReceiveData.getInstance().isReceiving()) {
                if (FileSendData.getInstance().isSending()) {
                    mReceiveMultiFragment.setSendFilesEnable(false, true);
                } else {
                    mReceiveMultiFragment.setSendFilesEnable(true, true);
                }
            }
        }
    }

    private void bindGoService() {
        mIntent = new Intent(this, GoMultiService.class);
        startService(mIntent);
        bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindGoService() {
        if (mGoMultiService != null) {
            stopService(mIntent);
        }
        unbindService(conn);
    }

    public ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("luorw , GoMultiTransferActivity,onServiceConnected-----------------");
            mGoMultiService = ((GoMultiService.GoMultiServiceBinder) service).getService();
            mGoMultiService.setMultiDeviceCallBack(GoMultiTransferActivity.this);
            mGoMultiService.watchOnline();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("luorw , GoMultiTransferActivity,onNewIntent-----------------");
        super.onNewIntent(intent);
        setIntent(intent);
        send();
//        if(Constants.ACTION_ENTER_FROM_NOTIFICATION.equals(action)){
//            int tabForNotification = intent.getIntExtra(Constants.CURRENT_TAB,0);
//            mViewPager.setCurrentItem(tabForNotification);
//        }
    }

    private void send() {
        Map<String, Socket> socketMap = SocketChannel.getInstance().mCommendSockets;
        ArrayList<FileInfo> fileSendList = FileSendData.getInstance().getFileSendList();
        long allFileSize = FileSendData.getInstance().getAllFileSize();
        for (String key : socketMap.keySet()) {
            BaseSendData baseSendData = new BaseSendData();
            baseSendData.setFileSendList((ArrayList<FileInfo>) fileSendList.clone());
            baseSendData.setAllFileSize(allFileSize);
            FileMultiSendData.getInstance().setAllMultiFileData(key, baseSendData);
            MultiCommandInfo multiCommandInfo = new MultiCommandInfo();
            multiCommandInfo.command = Constants.SENDER_SEND_DESCRIBE;
            multiCommandInfo.responseFilesList = baseSendData.getFileSendList();
            mGoMultiService.createWriteCommand(key, multiCommandInfo);
            mGoMultiService.registerSendListeners(key,mSendMultiFragment.addSendLayout(key));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoMultiService != null) {
            unBindGoService();
//            mGoMultiService.unRegisterReceiveListener(mReceiveFragment.mReceiveListener);
//            mGoMultiService.unRegisterSendListener(mSendFragment.mSendListener);
        }
    }


    @Override
    public void exit(boolean isFinishActivity) {
        if (isFinishActivity) {
            new HotspotManager().closeHotspot(this);
            mGoMultiService.notifyOffline();
            finish();
        }
    }

    @Override
    public void online(final String deviceName) {
        LogUtil.i("GO, online  = " + deviceName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, deviceName + getResources().getString(R.string.is_online), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void offline(final String msg) {
        LogUtil.i("luorw , GO 读到 GC   , offline  ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] info = msg.split(",");
                Toast.makeText(mContext, info[2] + getResources().getString(R.string.is_offline), Toast.LENGTH_SHORT).show();
                removeAssociation(info[3]);
            }
        });
    }

    @Override
    public MultiBaseService getMultiService() {
        return mGoMultiService;
    }

    @Override
    public void onFullStorage() {

    }

    @Override
    public void onRefreshMenu(boolean isTransferable, boolean isVisible) {

    }

    @Override
    public void onReadCommand(String key) {
        LogUtil.i("luorw , GoMultiTransferActivity , onReadCommand");
        mGoMultiService.createReadCommand(key);
    }

    @Override
    public void onAddReceive(String key) {
        mGoMultiService.registerReceiveListeners(key,mReceiveMultiFragment.addReceiveLayout(key));
    }

}
