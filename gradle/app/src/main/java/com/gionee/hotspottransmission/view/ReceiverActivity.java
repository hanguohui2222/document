package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.hotspottransmission.animation.RadarScanView;
import com.gionee.hotspottransmission.animation.RandomTextView;
import com.gionee.hotspottransmission.animation.RippleView;
import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IGcStatusCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.service.GroupClientService;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.name;

/**
 * Created by luorw on 5/12/17.
 */
public class ReceiverActivity extends AmigoActivity implements IGcStatusCallBack,View.OnClickListener {
    private RandomTextView randomTextView;
    private RelativeLayout scanRelative;
    private RelativeLayout scanRocket;
    private RadarScanView mRadarScanView;
    private ImageView btn_refresh;
    private GroupClientService mGcService;
    private Intent mIntent;
    private List<String> mDeviceList;
    private static final int RANDOM_MAX_DEVICE = 5;
    private PopupWindow mPopupWindow;
    private boolean isGroupTransfer;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        parseIntent();
        initActionBar();
        initViews();
        bindGcService();
    }

    private void parseIntent() {
        mContext = this;
        final Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if(Constants.ACTION_GROUP_TRANSFER.equals(action)){
            isGroupTransfer = true;
            FileUtil.setPasswordForHotspot(null);
        }
    }

    private void initActionBar() {
        AmigoActionBar  mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        View actionbar = getLayoutInflater().inflate(
                R.layout.actionbar_receiver, null);
        btn_refresh = (ImageView) actionbar.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(this);
        btn_refresh.setVisibility(View.GONE);
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.WRAP_CONTENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
        mActionBar.setCustomView(actionbar, param);
        if(isGroupTransfer){
            mActionBar.setTitle(R.string.join_group);
        }else{
            mActionBar.setTitle(R.string.receiver_search_action_title);
        }
        mActionBar.show();
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止搜索，并退出本界面
                finish();
            }
        });
    }

    private void initViews() {
        TextView radar_scan_name = (TextView) findViewById(R.id.activity_radar_scan_name);
        radar_scan_name.setText(DeviceSp.getInstance().getDeviceName(this));
        scanRelative = (RelativeLayout) findViewById(R.id.activity_radar_scan_relative);
        scanRelative.setVisibility(View.VISIBLE);
        scanRocket = (RelativeLayout) findViewById(R.id.activity_radar_rocket_layout);
        scanRocket.setVisibility(View.GONE);
        mRadarScanView = (RadarScanView)findViewById(R.id.activity_radar_scan_view);
        randomTextView = (RandomTextView) findViewById(R.id.activity_radar_rand_textview);
        randomTextView.setMode(RippleView.MODE_OUT);
        randomTextView.setOnRippleViewClickListener(new RandomTextView.OnRippleViewClickListener() {
            @Override
            public void onRippleViewClicked(String ssid) {
                scanRelative.setVisibility(View.GONE);
                scanRocket.setVisibility(View.VISIBLE);
                if (mGcService != null) {
                    mGcService.connect(ssid,isGroupTransfer);
                    mGcService.setmIsStartScan(false);
                }
            }
        });
    }

    private void bindGcService() {
        mIntent = new Intent(this, GroupClientService.class);
        startService(mIntent);
        bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindGcService() {
        if (mGcService != null && mGcService.getConnState() == false) {
            stopService(mIntent);
        }
        unbindService(conn);
    }

    public ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGcService = ((GroupClientService.GcServiceBinder) service).getService();
            mGcService.setmIGcStatusCallBack(ReceiverActivity.this);
            mGcService.workForScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindGcService();
    }

    @Override
    public void onWifiDisabled() {
        Toast.makeText(this, getResources().getString(R.string.wifi_disable), Toast.LENGTH_SHORT).show();
        stopService(mIntent);
//        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onScanResultsAvailable(List<ScanResult> scanResults) {
        randomTextView.getKeyWords().clear();
        int size = scanResults.size();
        if(size > RANDOM_MAX_DEVICE){
            showMoreDevicePop(scanResults);
        }else{
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult scanResult = scanResults.get(i);
                randomTextView.addKeyWord(scanResult.SSID);
            }
            if(mPopupWindow != null && mPopupWindow.isShowing()){
                mPopupWindow.dismiss();
            }
        }
        randomTextView.show();
    }

    private void showMoreDevicePop(List<ScanResult> scanResults){
        if(mDeviceList == null){
            mDeviceList = new ArrayList<>();
        }
        mDeviceList.clear();
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            mDeviceList.add(scanResult.SSID);
        }
        View contentView= LayoutInflater.from(this).inflate(R.layout.pop_more_device,null);
        ListView lv= (ListView) contentView.findViewById(R.id.more_device_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.item_more_device,mDeviceList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPopupWindow.dismiss();
                if (mGcService != null) {
                    mGcService.connect(mDeviceList.get(position),isGroupTransfer);
                }
                scanRelative.setVisibility(View.GONE);
                scanRocket.setVisibility(View.VISIBLE);
            }
        });
        if(mPopupWindow == null){
            mPopupWindow=new PopupWindow(contentView, 700,700);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setFocusable(false);
        }
        if(mPopupWindow.isShowing()){
            adapter.notifyDataSetChanged();
        }else{
            mPopupWindow.showAtLocation(scanRelative, Gravity.CENTER,0,100);
        }
    }

    @Override
    public void onWifiConnected( String hostIp) {
        Log.i("luorw111","onWifiConnected-----------");
        Intent intent = new Intent();
        if(isGroupTransfer){
            intent.setClass(this, GcMultiTransferActivity.class);
        }else{
            intent.setClass(this, GcTransferActivity.class);
            intent.putExtra(Constants.HOST_IP,hostIp);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
//        Toast.makeText(this, getResources().getString(R.string.wifi_disconnected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExit() {
        stopService(mIntent);
        finish();
    }

    @Override
    public void onRefreshEnable() {
        mRadarScanView.stopAnim();
        btn_refresh.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isGroupTransfer() {
        return isGroupTransfer;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_refresh:
                LogUtil.i("btn_refresh------------");
                mGcService.search(true);
                mRadarScanView.startAnim();
                btn_refresh.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
