package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.TransferPagerAdapter;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.service.BaseService;
import com.gionee.hotspottransmission.service.GroupOwnerService;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.fragment.ReceiveFragment;
import com.gionee.hotspottransmission.view.fragment.SendFragment;
import com.gionee.hotspottransmission.view.fragment.SendMultiFragment;

import java.util.ArrayList;

import amigoui.app.AmigoActionBar;

/**
 * Created by luorw on 5/26/17.
 */
public class GoTransferActivity extends BaseTransferActivity {
    private Intent mIntent;
    private ArrayList<Fragment> mFragmentPages;
    private SendFragment mSendFragment;
    private ReceiveFragment mReceiveFragment;
    private GroupOwnerService mGoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindGoService();
    }


    @Override
    public void addFragments() {
        mViewPager = (ViewPager) findViewById(R.id.transfer_file_viewpager);
        mFragmentPages = new ArrayList<Fragment>();
        mSendFragment = SendFragment.newInstance();
        mReceiveFragment = ReceiveFragment.newInstance();
        mFragmentPages.clear();
        mFragmentPages.add(mSendFragment);
        mFragmentPages.add(mReceiveFragment);
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
        //当切换到接收/发送界面时，需要按照当前的接收/发送状态进行刷新menu
        if (position == 0) {
            if (!FileSendData.getInstance().isSending()) {
                mSendFragment.setSendFilesEnable(true, true);
            } else {
                mSendFragment.setSendFilesEnable(false, false);
            }
        } else if (position == 1) {
            if (!FileReceiveData.getInstance().isReceiving()) {
                if (FileSendData.getInstance().isSending()) {
                    mReceiveFragment.setSendFilesEnable(false, true);
                } else {
                    mReceiveFragment.setSendFilesEnable(true, true);
                }
            }
        }
    }


    @Override
    public BaseService getService() {
        return mGoService;
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
            mGoService.setSendDeviceCallBack(mSendFragment.mDeviceCallBack);
            mGoService.setReceiveDeviceCallBack(mReceiveFragment.mDeviceCallBack);
            mGoService.registerReceiveListeners(mReceiveFragment.mReceiveListener);
            mGoService.registerSendListeners(mSendFragment.mSendListener);
            mGoService.setClient(getIntent().getStringExtra(Constants.CLIENT_IP));
            mGoService.workForTransfer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("GOActivity,onNewIntent-----------------");
        super.onNewIntent(intent);
        setIntent(intent);
        String action = intent.getAction();
        if (Constants.ACTION_RESEND_FILES.equals(action)) {
            LogUtil.i("GOActivity,ACTION_RESEND_FILES-----------------");
            mViewPager.setCurrentItem(0);
            if (mGoService != null) {
                mGoService.reSend();
            }
        }
        if (Constants.ACTION_ENTER_FROM_NOTIFICATION.equals(action)) {
            int tabForNotification = intent.getIntExtra(Constants.CURRENT_TAB, 0);
            mViewPager.setCurrentItem(tabForNotification);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoService != null) {
            unBindGoService();
            mGoService.unRegisterReceiveListener(mReceiveFragment.mReceiveListener);
            mGoService.unRegisterSendListener(mSendFragment.mSendListener);
        }
    }


    @Override
    public void exit(boolean isFinishActivity) {
        stopService(mIntent);
        if (isFinishActivity) {
            finish();
        }
    }

}
