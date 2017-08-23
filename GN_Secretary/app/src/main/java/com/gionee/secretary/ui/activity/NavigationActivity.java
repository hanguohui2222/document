package com.gionee.secretary.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoAlertDialog;

public class NavigationActivity extends PasswordBaseActivity implements AMapNaviListener, AMapNaviViewListener, TextToSpeech.OnInitListener, View.OnClickListener {

    private static final String LOG_TAG = NavigationActivity.class.getSimpleName();

    //AMap Navi Field
    AMapNaviView mAmapNaviView;
    AMapNavi mAMapNavi;

    //TTS Service
    private TextToSpeech mGeneralTTS;
    private Boolean mIsTTSInit = false;
    private Intent mGioneeTTSIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        initViews(savedInstanceState);
        initTTS();
        initNavi();
        LogUtils.d(LOG_TAG, "Activity Lifecycle Callback: onCreate()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAmapNaviView.onPause();
        LogUtils.d(LOG_TAG, "Activity Lifecycle Callback: onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmapNaviView.onResume();
        NavigateUtil.checkLocationService(this);
        LogUtils.d(LOG_TAG, "Activity Lifecycle Callback: onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAmapNaviView.onDestroy();
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        LogUtils.d(LOG_TAG, "Activity Lifecycle Callback: onDestroy()");
    }

    private void initViews(Bundle savedInstanceState) {
        //ActionBar
        AmigoActionBar actionBar = getAmigoActionBar();
        actionBar.hide();

        mAmapNaviView = (AMapNaviView) findViewById(R.id.navigation_naviview);
        mAmapNaviView.onCreate(savedInstanceState);
        mAmapNaviView.setAMapNaviViewListener(this);
    }

    private void initNavi() {
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        boolean gpsEnabled = mAMapNavi.startGPS();
        boolean gpsReady = mAMapNavi.isGpsReady();
        mAMapNavi.startNavi(NaviType.GPS);
        LogUtils.d(LOG_TAG, "AMapNavi: initNavi()" + "GPS Status=" + gpsEnabled + "; GPS Ready = " + gpsReady);
    }

    private void initTTS() {
        if (Build.MANUFACTURER.equals("GIONEE")) {
            //金立手机调用自己的语音引擎
            if (mGioneeTTSIntent == null) {
                mGioneeTTSIntent = new Intent();
                mGioneeTTSIntent.setClassName("gn.com.voice", "gn.com.voice.TTSService");
            }
        } else {
            //其他手机调用系统通用语音服务
            if (mGeneralTTS == null) {
                mGeneralTTS = new TextToSpeech(this, this);
            }
        }
    }

    private void showStopNavigationDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(NavigationActivity.this);
        builder.setTitle("提示");
        builder.setMessage("确定退出导航?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavigationActivity.this.stopService(mGioneeTTSIntent);
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AmigoAlertDialog dialog = builder.create();
        dialog.show();
    }

    /*  AMap Navi Callback  */
    @Override
    public void onInitNaviFailure() {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onInitNaviFailure()");
    }

    @Override
    public void onInitNaviSuccess() {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onInitNaviSuccess()");
    }

    @Override
    public void onStartNavi(int i) {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onStartNavi()");
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {
        if (Build.MANUFACTURER.equals("GIONEE")) {
            mGioneeTTSIntent.putExtra("appid", "SECRETARY");
            mGioneeTTSIntent.putExtra("type", "speak_info");
            mGioneeTTSIntent.putExtra("operation", "read_msg");
            mGioneeTTSIntent.putExtra("message", s);
            startService(mGioneeTTSIntent);
        } else {
            if (mIsTTSInit) {
                mGeneralTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onEndEmulatorNavi() {
        finish();
    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onArriveDestination()");
    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onArriveDestination()");
    }

    @Override
    public void onCalculateRouteSuccess() {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onCalculateRouteSuccess()");
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onCalculateRouteFailure()");
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(LOG_TAG, "AMapNavi Callback: onCalculateMultipleRoutesSuccess()");
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    /*  TTS Callback  */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mIsTTSInit = true;
        } else {
            mIsTTSInit = false;
        }
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {
        finish();
    }

    @Override
    public boolean onNaviBackClick() {
        showStopNavigationDialog();
        return true;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onBackPressed() {
        showStopNavigationDialog();
    }
}
