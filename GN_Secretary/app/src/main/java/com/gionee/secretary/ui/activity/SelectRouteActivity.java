package com.gionee.secretary.ui.activity;

import android.content.Intent;
//import android.support.design.widget.BottomSheetBehavior;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.gionee.secretary.adapter.RouteResultAdapter;
import com.gionee.secretary.utils.AMapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;

import amigoui.app.AmigoActionBar;


public class SelectRouteActivity extends PasswordBaseActivity implements TabLayout.OnTabSelectedListener, RadioGroup.OnCheckedChangeListener, AMapNaviListener, View.OnClickListener, RouteSearch.OnRouteSearchListener {

    private final String LOG_TAG = SelectRouteActivity.class.getSimpleName();

    //Views
    RadioGroup mTabMode;
    Button mBtnStartNavigation;
    MapView mMapView;
    RelativeLayout mBusResultLayout;
    LinearLayout mBriefBar;


    //AMap Constant
    private final int MODE_DRIVE = 2;
    private final int MODE_TRANSFER = 4;
    private final int MODE_WALK = 1;
    private final int STATUS_LOADING = 0;
    private final int STATUS_OK = 1;
    private final int STATUS_FAILED = 2;

    //AMap Field
    AMap mAmap;
    AMapNavi mAmapNavi;

    private double[] mDeptCoord = {0, 0};
    private double[] mDestCoord = {0, 0};
    private int mMode = MODE_DRIVE;
    private String mTravelType = "";
    private int mSelectedRoute = 0;
    private boolean mStartNavigate = false;
    private Object mSelectedRoutePath;
    private RouteSearch mBusRouteSearch;
    private List<BusPath> busPaths = new ArrayList<>();
    private int mRouteZindex = 1;
    private boolean mIsCalculateRoute = false;
    private String mCurrentCity = "北京市";


    //Map Overlay Field
    private SparseArray<RouteOverLay> mRouteOverlays = new SparseArray<>();
    private SparseArray<BusRouteOverlay> mBusRouteOverlays;


    /* Activity General Lifecycle Callback */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);
        handleIntentData();
        initViews();
        initAMapNavi();
        restoreInstanceState(savedInstanceState);
        initMapView(savedInstanceState);
        LogUtils.d(LOG_TAG, "Activity lifecycle callback: onCreate()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        LogUtils.d(LOG_TAG, "Activity lifecycle callback: onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        NavigateUtil.checkLocationService(this);
        LogUtils.d(LOG_TAG, "Activity lifecycle callback: onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if(mAmapNavi != null){
            mAmapNavi.removeAMapNaviListener(this);
            if (!mStartNavigate) mAmapNavi.destroy();
            mAmapNavi = null;
        }

        LogUtils.d(LOG_TAG, "Activity lifecycle callback: onDestroy()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("SELECT_ROUTE", mSelectedRoute);
        outState.putInt("SELECT_TAB", mTabMode.getCheckedRadioButtonId());
        mMapView.onSaveInstanceState(outState);
    }

    private void initViews() {
        //init ActionBar
        AmigoActionBar actionBar = getAmigoActionBar();
        RelativeLayout actionbarLayout = (RelativeLayout) View.inflate(this, R.layout.actionbar_navigation, null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
//        if(DisplayUtils.isFullScreen()){
//            actionBar.setHomeAsUpIndicator(R.drawable.back_icon_full);
//        }else {
//            actionBar.setHomeAsUpIndicator(R.drawable.back_icon);
//        }
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_color)));
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(actionbarLayout, param);

        //Init Actionbar Tab
        mTabMode = (RadioGroup) actionbarLayout.findViewById(R.id.select_route_tab_mode);
        mTabMode.setVisibility(View.VISIBLE);
        mTabMode.check(getTravelMode(mTravelType));
        mTabMode.setOnCheckedChangeListener(this);

        //Init MapView
        mMapView = (MapView) findViewById(R.id.select_route_map);
        mBtnStartNavigation = (Button) findViewById(R.id.select_route_btn_start_navigation);

        //Init BusResultView
        mBusResultLayout = (RelativeLayout) findViewById(R.id.layout_bus_route_result);

        //Bottom Tab Setup
        mBriefBar = (LinearLayout) findViewById(R.id.select_route_layout_brief);

        //Set OnClickListener
        mBtnStartNavigation.setOnClickListener(this);
    }

    private int getTravelMode(String travelType) {
        int id = R.id.select_route_mode_drive;
        if ("步行".equals(travelType)) {
            id = R.id.select_route_mode_walk;
        } else if ("开车".equals(travelType)) {
            id = R.id.select_route_mode_drive;
        } else if ("公共交通".equals(travelType)) {
            id = R.id.select_route_mode_transfer;
        } else {
            id = R.id.select_route_mode_drive;
        }
        return id;
    }

    private void initMapView(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        mMapView = (MapView) findViewById(R.id.select_route_map);
        mAmap = mMapView.getMap();
    }

    private void initAMapNavi() {
        //Setup AMapNavi
        clearOverlays();
        displayBriefStatus(STATUS_LOADING);
        mAmapNavi = AMapNavi.getInstance(getApplicationContext());
        mAmapNavi.addAMapNaviListener(this);
        mIsCalculateRoute = false;
        LogUtils.d(LOG_TAG, "initAmapNavi()");
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        mCurrentCity = intent.getStringExtra("DEPT_CITY");
        mDeptCoord[0] = intent.getDoubleExtra("DEPT_COORD_LAT", 0);
        mDeptCoord[1] = intent.getDoubleExtra("DEPT_COORD_LONG", 0);
        mDestCoord[0] = intent.getDoubleExtra("DEST_COORD_LAT", 0);
        mDestCoord[1] = intent.getDoubleExtra("DEST_COORD_LONG", 0);
        mTravelType = intent.getStringExtra("TRAVEL_TYPE");
        mMode = getMode(mTravelType);
        LogUtils.d(LOG_TAG, "handleIntentData(); " + "DEPT_COORD_LAT=" + mDeptCoord[0] + ";LONG=" + mDeptCoord[1] + "; DEST_COORD_LAT=" + mDestCoord[0] + ";LONG=" + mDestCoord[1] + "DEPT_CITY=" + mCurrentCity);
    }

    private int getMode(String travelType) {
        int mode = MODE_WALK;
        if ("步行".equals(travelType)) {
            mode = MODE_WALK;
        } else if ("开车".equals(travelType)) {
            mode = MODE_DRIVE;
        } else if ("公共交通".equals(travelType)) {
            mode = MODE_TRANSFER;
        }
        return mode;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedRoute = savedInstanceState.getInt("SELECT_ROUTE", 0);
            int selectTab = savedInstanceState.getInt("SELECT_TAB", 0);
            mTabMode.check(selectTab);
        }
    }


    /*  Route Select Method  */

    /**
     * 计算路径
     *
     * @param mode      选取的出行方式
     * @param deptCoord 出发地坐标
     * @param destCoord 目的地坐标
     */
    private void calculateRoute(int mode, double[] deptCoord, double[] destCoord) {
        mIsCalculateRoute = false;
        mBtnStartNavigation.setEnabled(false);
        mSelectedRoute = 0;
//        mScrollDetail.setVisibility(View.GONE);
        //mBottomSheetDetail.setHideable(true);
        //mBottomSheetDetail.setState(BottomSheetBehavior.STATE_HIDDEN);

        NaviLatLng deptCoordLatLng = new NaviLatLng(deptCoord[0], deptCoord[1]);
        NaviLatLng destCoordLatLng = new NaviLatLng(destCoord[0], destCoord[1]);
        List<NaviLatLng> deptCoordLatLngList = new ArrayList<>();
        deptCoordLatLngList.add(deptCoordLatLng);
        List<NaviLatLng> destCoordLatLngList = new ArrayList<>();
        destCoordLatLngList.add(destCoordLatLng);

        LogUtils.d(LOG_TAG, "AMapNavi callback: calculateRoute(); Mode=" + mMode + "; CurrentCity=" + mCurrentCity);
        if(mAmapNavi == null)
            return;

        switch (mode) {
            case MODE_DRIVE: {
                int strategy = 0;
                try {
                    strategy = mAmapNavi.strategyConvert(true, false, false, true, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAmapNavi.calculateDriveRoute(deptCoordLatLngList, destCoordLatLngList, null, strategy);
                break;
            }
            case MODE_TRANSFER: {
                if (mBusRouteSearch == null) {
                    mBusRouteSearch = new RouteSearch(this);
                }
                RouteSearch.FromAndTo busFromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(deptCoord[0], deptCoord[1]), new LatLonPoint(destCoord[0], destCoord[1]));
                RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(busFromAndTo, RouteSearch.BusLeaseWalk, mCurrentCity, 0);
                LogUtils.d(LOG_TAG, "AMapNavi callback: calculateRoute(); Calculating BusRoute, CurrentCity=" + mCurrentCity);
                mBusRouteSearch.calculateBusRouteAsyn(query);
                mBusRouteSearch.setRouteSearchListener(this);
                break;
            }
            case MODE_WALK: {
                mAmapNavi.calculateWalkRoute(deptCoordLatLng, destCoordLatLng);
                break;
            }
        }
    }

    /**
     * 在地图上绘制路线
     *
     * @param mode          出行方式
     * @param routeId       路线ID
     * @param path          路线信息
     * @param isTransparent 是否设置透明
     */
    private void drawRoute(int mode, int routeId, Object path, boolean isTransparent) {
        if (path != null) {

            if (path instanceof AMapNaviPath) {
                if ((mode == MODE_DRIVE) || (mode == MODE_WALK)) {
                    RouteOverLay routeOverLay = new RouteOverLay(mAmap, (AMapNaviPath) path, this);
                    if (mode == MODE_DRIVE) {
                        routeOverLay.setTrafficLine(true);
                    }
                    if (isTransparent) {
                        routeOverLay.setTransparency(0.25f);
                    } else {
                        routeOverLay.setTransparency(1.0f);
                    }
                    routeOverLay.addToMap();
                    mRouteOverlays.put(routeId, routeOverLay);
                }
            } else if (path instanceof BusPath) {
                BusRouteOverlay busOverlay = new BusRouteOverlay(this, mAmap, (BusPath) path, new LatLonPoint(mDeptCoord[0], mDeptCoord[1]), new LatLonPoint(mDestCoord[0], mDestCoord[1]));
                mBusRouteOverlays.put(routeId, busOverlay);
            }
        }
    }

    /**
     * 高亮显示某条路径
     *
     * @param routeID
     */
    private void highlightRoute(int routeID) {
        if (mMode == MODE_DRIVE || mMode == MODE_WALK) {
            //将所有路线置为透明
            for (int i = 0; i < mRouteOverlays.size(); i++) {
                int key = mRouteOverlays.keyAt(i);
                RouteOverLay overlay = mRouteOverlays.get(key);
                overlay.setTransparency(0.25f);
            }
            RouteOverLay overlay = mRouteOverlays.get(routeID);
            overlay.setTransparency(1.0f);
            overlay.zoomToSpan();
            overlay.setZindex(mRouteZindex++);
        } else if (mMode == MODE_TRANSFER) {
            for (int i = 0; i < mBusRouteOverlays.size(); i++) {
                BusRouteOverlay busOverlay = mBusRouteOverlays.get(i);
                busOverlay.removeFromMap();
            }
            BusRouteOverlay busOverlay = mBusRouteOverlays.get(routeID);
            busOverlay.addToMap();
            busOverlay.zoomToSpan();
        }
    }

    /**
     * 清除地图上的路线显示
     */
    private void clearOverlays() {
        //Clear Drive and Walk Overlay
        if (mRouteOverlays != null && mRouteOverlays.size() != 0) {
            for (int i = 0; i < mRouteOverlays.size(); i++) {
                int key = mRouteOverlays.keyAt(i);
                RouteOverLay overlay = mRouteOverlays.get(key);
                overlay.removeFromMap();
            }
            mRouteOverlays.clear();
        }

        //Clear Bus Overlay
        if (mBusRouteOverlays != null && mBusRouteOverlays.size() != 0) {
            for (int i = 0; i < mBusRouteOverlays.size(); i++) {
//                int key = mRouteOverlays.keyAt(i);
                BusRouteOverlay overlay = mBusRouteOverlays.get(i);
                overlay.removeFromMap();
            }
            mBusRouteOverlays.clear();
        }
    }

    /**
     * 更换路线
     */
    private void onChangeRoute() {
        if (mIsCalculateRoute) {
//            mSelectedRoute++;
            if ((mMode == MODE_DRIVE) || (mMode == MODE_WALK)) {
                if (mSelectedRoute >= mRouteOverlays.size()) {
                    mSelectedRoute = 0;
                }
                int key = mRouteOverlays.keyAt(mSelectedRoute);
                //added by luorw for s10c #88276 2017-03-23 begin
                if (mRouteOverlays.get(key) != null) {
                    showRouteDetail(mRouteOverlays.get(key).getAMapNaviPath());
                }
                //added by luorw for s10c #88276 2017-03-23 end
                highlightRoute(key);
                if(mAmapNavi == null)
                    return;
                mAmapNavi.selectRouteId(key);
            } else if (mMode == MODE_TRANSFER) {
                if (mSelectedRoute >= mBusRouteOverlays.size()) {
                    mSelectedRoute = 0;
                }
                showRouteDetail(busPaths.get(mSelectedRoute));
                highlightRoute(mSelectedRoute);
            }
        } else {
            mSelectedRoute = 0;
        }
    }

    /**
     * 显示路线的详细信息
     *
     * @param path 规划好的路线信息（AMapNaviPath与BusPath)
     */
    private void showRouteDetail(Object path) {
        mSelectedRoutePath = path;
        if ((path != null) && (path instanceof AMapNaviPath)) {
            AMapNaviPath naviPath = (AMapNaviPath) path;
            //Show Brief information
            String str_length = AMapUtil.getFriendlyLength(naviPath.getAllLength());
            String str_eta = AMapUtil.getFriendlyTime(naviPath.getAllTime());
//            Toast.makeText(getApplicationContext(),"路线长度:"+str_length+"; 估计时间:"+str_eta,Toast.LENGTH_SHORT).show();
            //TODO:Show Detail Information for Drive and Walk

        } else if (path instanceof BusPath) {
            BusPath busPath = (BusPath) path;
            //Show Brief Information
            String str_eta = AMapUtil.getFriendlyTime((int) busPath.getDuration());
            //TODO:Show Detail Information for Transfer
        }
    }

    private void showRouteBriefInfo() {
        refreshBriefBar();
        if (mIsCalculateRoute) {
//            mTabBriefInfo.removeAllTabs();
            switch (mMode) {
                case MODE_DRIVE: {
                    LinearLayout driveBriefBarLayout = (LinearLayout) View.inflate(this, R.layout.navi_briefbar_drive, null);
                    TabLayout briefTab = (TabLayout) driveBriefBarLayout.findViewById(R.id.navi_briefbar_tablayout);
                    briefTab.removeAllTabs();
                    mBriefBar.addView(driveBriefBarLayout, 0, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    for (int i = 0; i < mRouteOverlays.size(); i++) {
                        int key = mRouteOverlays.keyAt(i);
                        RouteOverLay overlay = mRouteOverlays.get(key);
                        AMapNaviPath path = overlay.getAMapNaviPath();
                        String str_time = AMapUtil.getFriendlyTime(path.getAllTime());
                        String str_distance = AMapUtil.getFriendlyLength(path.getAllLength());
                        addBriefInfoTab(briefTab, "BRIEF_DRIVE", str_time, str_distance);
                    }
                    briefTab.setOnTabSelectedListener(this);
                    briefTab.setSelectedTabIndicatorColor(Color.argb(255, 79, 152, 237));
                    break;
                }
                case MODE_WALK: {
                    for (int i = 0; i < mRouteOverlays.size(); i++) {
                        int key = i;
                        RouteOverLay overlay = mRouteOverlays.get(key);
                        AMapNaviPath path = overlay.getAMapNaviPath();
                        String str_time = AMapUtil.getFriendlyTime(path.getAllTime());
                        String str_length = AMapUtil.getFriendlyLength(path.getAllLength());
                        RelativeLayout walkBriefBarLayout = (RelativeLayout) View.inflate(this, R.layout.navi_briefbar_walk, null);
                        TextView walkTime = (TextView) walkBriefBarLayout.findViewById(R.id.tv_brief_time);
                        TextView walkDistance = (TextView) walkBriefBarLayout.findViewById(R.id.tv_brief_distance);
                        TextView walkDescription = (TextView) walkBriefBarLayout.findViewById(R.id.tv_brief_description);
                        mBriefBar.addView(walkBriefBarLayout, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        walkTime.setText(str_time);
                        walkDistance.setText(str_length);
                    }
                    break;
                }
                case MODE_TRANSFER: {
                    //Non-implement
                    break;
                }
            }
        }
    }

    private void showBusRouteResultList(List<BusPath> busResult) {
        mBusResultLayout.setVisibility(View.VISIBLE);
        RecyclerView busResultListView = (RecyclerView) mBusResultLayout.findViewById(R.id.rv_bus_route_list);
        busResultListView.setLayoutManager(new LinearLayoutManager(this));
        RouteResultAdapter resultAdapter = new RouteResultAdapter(this, null);
        resultAdapter.setOnItemClickListener(new RouteResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mSelectedRoute = position;
                onChangeRoute();
                Intent intent = new Intent(SelectRouteActivity.this, SelectRouteDetailActivity.class);
                if (mSelectedRoutePath != null) {
                    intent.putExtra("ROUTE_DETAIL", (BusPath) mSelectedRoutePath);
                    startActivity(intent);
                }
            }
        });
        busResultListView.setAdapter(resultAdapter);

        //Add BusPath to List
        List resultList = new ArrayList(0);
        for (BusPath path : busResult) {
            resultList.add(path);
        }
        resultAdapter.updateDatasource(resultList);
    }

    private void hideBusRouteResultList() {
        if (mBusResultLayout != null) {
            mBusResultLayout.setVisibility(View.GONE);
        }
    }

    private void addBriefInfoTab(TabLayout tabLayout, String tag, String str_time, String str_distance) {
        TabLayout.Tab tab = tabLayout.newTab();
        LinearLayout llBriefInfoTab = (LinearLayout) View.inflate(this, R.layout.item_tab_route_brief_info, null);
        TextView tvBriefTime = (TextView) llBriefInfoTab.findViewById(R.id.tv_brief_time);
        TextView tvBriefDistance = (TextView) llBriefInfoTab.findViewById(R.id.tv_brief_distance);
        tvBriefTime.setText(str_time);
        tvBriefDistance.setText(str_distance);
        tab.setCustomView(llBriefInfoTab);
        tab.setTag(tag);
        tabLayout.addTab(tab);
    }

    private void refreshBriefBar() {
        mBriefBar.removeAllViews();
    }

    private void displayBriefStatus(int status) {
        refreshBriefBar();
        switch (status) {
            case STATUS_LOADING: {
                ProgressBar pd = new ProgressBar(this);
                mBriefBar.addView(pd, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
            }
            case STATUS_FAILED: {
                TextView tvFailed = new TextView(this);
                tvFailed.setText("获取路线出错");
                tvFailed.setTextSize(24);
                mBriefBar.addView(tvFailed);
            }
        }
    }


    /*  AMapNavi Callback  */
    @Override
    public void onInitNaviFailure() {
        LogUtils.d(LOG_TAG, "AMapNavi callback: onInitNaviFailure()");
    }

    @Override
    public void onInitNaviSuccess() {
        LogUtils.d(LOG_TAG, "AMapNavi callback: onInitNaviSuccess()");
        calculateRoute(mMode, mDeptCoord, mDestCoord);
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {

    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {

    }

    @Override
    public void onCalculateRouteSuccess() {
        LogUtils.d(LOG_TAG, "AMapNavi callback: onCalculateRouteSuccess()");

        if (!(mMode == MODE_DRIVE || mMode == MODE_WALK) || mAmapNavi == null) {
            return;
        }

        clearOverlays();
        AMapNaviPath path = mAmapNavi.getNaviPath();
        mIsCalculateRoute = true;

        mBtnStartNavigation.setEnabled(true);
        mBtnStartNavigation.setText("开始导航");

        showRouteDetail(path);
        drawRoute(mMode, 0, path, false);
        highlightRoute(0);
        showRouteBriefInfo();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        LogUtils.d(LOG_TAG, "AMapNavi callback: onCalculateRouteFailure(),errCode=" + i);
        Toast.makeText(this, "计算路线失败。", Toast.LENGTH_SHORT).show();
        mIsCalculateRoute = false;
        displayBriefStatus(STATUS_FAILED);
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
        LogUtils.d(LOG_TAG, "AMapNavi callback: onCalculateMultipleRoutesSuccess()");

        if (!(mMode == MODE_DRIVE || mMode == MODE_WALK) || mAmapNavi == null) {
            return;
        }

        mIsCalculateRoute = true;
        mBtnStartNavigation.setEnabled(true);
        mBtnStartNavigation.setText("开始导航");
//        mScrollDetail.setVisibility(View.VISIBLE);
        //mBottomSheetDetail.setHideable(false);
        //mBottomSheetDetail.setState(BottomSheetBehavior.STATE_COLLAPSED);

        clearOverlays();
        HashMap<Integer, AMapNaviPath> paths = mAmapNavi.getNaviPaths();
        for (int i = 0; i < paths.size(); i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if (path != null) {
                drawRoute(mMode, ints[i], path, true);
            }
        }

        showRouteBriefInfo();
        showRouteDetail(paths.get(ints[0]));
        highlightRoute(ints[0]);
        mAmapNavi.selectRouteId(ints[0]);
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

    /*  Bus Route Search Callback  */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int retCode) {

        LogUtils.d(LOG_TAG, "AMap callback: onBusRouteSearched(); retCode=" + retCode);

        //成功搜索公共交通路线
        if (retCode == 1000 && mMode == MODE_TRANSFER) {
            if (mBusRouteOverlays == null) {
                mBusRouteOverlays = new SparseArray<>();
            }
            mBtnStartNavigation.setEnabled(true);
            mBtnStartNavigation.setText("显示详情");
            clearOverlays();

//            mScrollDetail.setVisibility(View.VISIBLE);
            //mBottomSheetDetail.setHideable(false);
            //mBottomSheetDetail.setState(BottomSheetBehavior.STATE_COLLAPSED);

            mIsCalculateRoute = true;
            List<BusPath> paths = busRouteResult.getPaths();
            busPaths = paths;
            showBusRouteResultList(paths);
            for (int i = 0; i < paths.size(); i++) {
                BusPath path = paths.get(i);
                drawRoute(MODE_TRANSFER, i, path, false);
            }
            showRouteDetail(paths.get(0));
            highlightRoute(0);
        }
        //搜索公共交通路线失败
        else {
            mIsCalculateRoute = false;
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /*  General View Callback  */

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(mAmapNavi == null)
            return;
        displayBriefStatus(STATUS_LOADING);
        hideBusRouteResultList();
        mBtnStartNavigation.setText(getText(R.string.start_navigation));
        switch (group.getId()) {
            case R.id.select_route_tab_mode: {
                switch (checkedId) {
                    case R.id.select_route_mode_drive: {
                        mMode = MODE_DRIVE;
                        mAmapNavi.destroy();
                        initAMapNavi();
                        break;
                    }
                    case R.id.select_route_mode_transfer: {
                        mMode = MODE_TRANSFER;
                        mAmapNavi.destroy();
                        calculateRoute(mMode, mDeptCoord, mDestCoord);
                        break;
                    }
                    case R.id.select_route_mode_walk: {
                        mMode = MODE_WALK;
                        mAmapNavi.destroy();
                        initAMapNavi();
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(mAmapNavi == null)
            return;
        if (tab.getTag().equals("TRAVEL_MODE")) {
            int selectTabPosition = tab.getPosition();
            switch (selectTabPosition) {
                case 0: {
                    mMode = MODE_DRIVE;
                    mAmapNavi.destroy();
                    initAMapNavi();
                    break;
                }
                case 1: {
                    mMode = MODE_TRANSFER;
                    mAmapNavi.destroy();
//                    mTabBriefInfo.removeAllTabs();
                    calculateRoute(mMode, mDeptCoord, mDestCoord);
                    break;
                }
                case 2: {
                    mMode = MODE_WALK;
                    mAmapNavi.destroy();
                    initAMapNavi();
                    break;
                }
            }
        } else if (tab.getTag().equals("BRIEF_DRIVE")) {
            int position = tab.getPosition();
            mSelectedRoute = position;
            onChangeRoute();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_route_btn_start_navigation: {
                if (mMode == MODE_DRIVE || mMode == MODE_WALK) {
                    Intent intent = new Intent(SelectRouteActivity.this, NavigationActivity.class);
                    startActivity(intent);
                    mStartNavigate = true;
                    this.finish();
                } else if (mMode == MODE_TRANSFER) {
                    Intent intent = new Intent(SelectRouteActivity.this, SelectRouteDetailActivity.class);
                    if (mSelectedRoutePath != null) {
                        intent.putExtra("ROUTE_DETAIL", (BusPath) mSelectedRoutePath);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.btn_back: {
                finish();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return true;
    }
}
