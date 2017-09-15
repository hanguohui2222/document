package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.HotspotManager;
import com.gionee.hotspottransmission.manager.WifiGcManager;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

/**
 * Created by luorw on 17/5/9.
 */
public class GroupTransferActivity extends AmigoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        initActionBar();
        setContentView(R.layout.activity_group_transfer);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setStatusBarColor() {
        Window window = this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(this.getResources().getColor(R.color.home_top_layout_color));
        ViewGroup mContentView =
                (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View .预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
    }

    private void initActionBar() {
        AmigoActionBar actionBar = getAmigoActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void createGroup(View view) {
        Intent intent = new Intent(this, SenderActivity.class);
        intent.setAction(Constants.ACTION_GROUP_TRANSFER);
        startActivity(intent);
    }

    public void joinGroup(View view) {
        Intent intent = new Intent(this, ReceiverActivity.class);
        intent.setAction(Constants.ACTION_GROUP_TRANSFER);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new HotspotManager().closeHotspot(this);
        WifiGcManager wifiGcManager = new WifiGcManager(this);
        wifiGcManager.disconnectWifi(wifiGcManager.getConnectedSSID());
        SocketChannel.getInstance().mCommendSockets.clear();
        SocketChannel.getInstance().mName.clear();
        SocketChannel.getInstance().mAddresses.clear();
    }
}
