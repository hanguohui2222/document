package com.gionee.secretary.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import amigoui.app.AmigoActionBar;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gionee.secretary.adapter.SelectAddressAdapter;
import com.gionee.secretary.bean.AddressBean;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LocationUtil;

import java.util.ArrayList;
import java.util.List;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;


public class SelectAddressActivity extends PasswordBaseActivity implements AMapLocationListener {

    private final String LOG_TAG = SelectAddressActivity.class.getSimpleName();

    private ListView mAddressListView;
    private FrameLayout mResultEmptyView;
    private FrameLayout mLoadingEmptyView;
    private SelectAddressAdapter mAddressAdapter;
    private List<AddressBean> mAddressResult = new ArrayList<>();
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;

    //AMap Location field
    LocationUtil mLocationUtil;
    double[] departCoord = {0, 0};
    String mDestPoiName = "";
    String mCurrentCity = "";
    String mTravelType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        handleIntentData();
        initViews();
        initDatas();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread() {

            @Override
            public void run() {
                mLocationUtil.mLocationClient.onDestroy();
            }

        }.start();
    }

    private void initViews() {
        //Initialize General View
        mAddressListView = (ListView) findViewById(R.id.lv_address);
        mResultEmptyView = (FrameLayout) findViewById(R.id.empty_address_result);
        mLoadingEmptyView = (FrameLayout) findViewById(R.id.empty_address_loading);

        //Init Actionbar
        AmigoActionBar actionBar = getAmigoActionBar();
        LinearLayout actionbarLayout = (LinearLayout) View.inflate(this, R.layout.actionbar_address_remark, null);
        ImageView btnBack = (ImageView) actionbarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionbarLayout);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Set EmptyView
        mAddressListView.setEmptyView(mLoadingEmptyView);

        //Set Adapter
        mAddressAdapter = new SelectAddressAdapter(this, null);
        mAddressListView.setAdapter(mAddressAdapter);
    }

    private void initDatas() {
        mLocationUtil = new LocationUtil(this);
        mLocationUtil.setEnableOnceLocation(false);
        mLocationUtil.setLocationInterval(5000);
//        getAddressList(this,mDestPoiName,mCurrentCity);
    }


    private void handleIntentData() {
        Intent intent = getIntent();
        mDestPoiName = intent.getStringExtra("DEST_POI_NAME");
        mTravelType = intent.getStringExtra("TRAVEL_TYPE");
        LogUtils.d(LOG_TAG, "handleIntentData(), DEST_POI_NAME=" + mDestPoiName);
    }


    private void setListeners() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }else{
            mLocationUtil.startLocation(this);
        }

        mAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(SelectAddressActivity.this, SelectRouteActivity.class);
                if (mAddressResult != null && NavigateUtil.checkLocationService(SelectAddressActivity.this)) {
                    AddressBean address = (AddressBean) mAddressAdapter.getItem(position);
                    double destLat = address.getmLatitude();
                    double destLong = address.getmLongitude();
                    intent.putExtra("DEPT_CITY", mCurrentCity);
                    intent.putExtra("DEPT_COORD_LAT", departCoord[0]);
                    intent.putExtra("DEPT_COORD_LONG", departCoord[1]);
                    intent.putExtra("DEST_COORD_LAT", destLat);
                    intent.putExtra("DEST_COORD_LONG", destLong);
                    intent.putExtra("TRAVEL_TYPE", mTravelType);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//授权成功
                        mLocationUtil.startLocation(this);
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {//点击拒绝授权
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void getAddressList(Context context, String destPoiName, String cityName) {
        LogUtils.d(LOG_TAG, "getAddressList(): Dest=" + destPoiName + "; CurrentCity" + cityName);
        PoiSearch.Query query = new PoiSearch.Query(destPoiName.trim(), "", cityName.trim());
        query.setPageSize(10);
        query.setPageNum(0);

        PoiSearch poiSearch = new PoiSearch(context, query);
        poiSearch.searchPOIAsyn();
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int retCode) {

                if (retCode == 1000) {
                    if (poiResult != null && poiResult.getQuery() != null) {
                        LogUtils.d(LOG_TAG, "onPoiSearched() Success. Size:" + poiResult.getPois().size());
                        // Gionee sunyang 2019-01-20 modify for GNSPR #65599 begin
                        mAddressResult.clear();
                        // Gionee sunyang 2019-01-20 modify for GNSPR #65599 end
                        List<PoiItem> poiList = poiResult.getPois();
                        if (poiList.size() == 0) {
                            mLoadingEmptyView.setVisibility(View.GONE);
                            mAddressListView.setEmptyView(mResultEmptyView);
                            return;
                        }
                        for (PoiItem poiItem : poiList) {
                            AddressBean address = new AddressBean();
                            address.setName(poiItem.getTitle());
                            address.setDesc(poiItem.getSnippet());
                            address.setmLatitude(poiItem.getLatLonPoint().getLatitude());
                            address.setmLongitude(poiItem.getLatLonPoint().getLongitude());
                            LogUtils.d(LOG_TAG, "onPoiSearched(); name=" + poiItem.getTitle());
                            mAddressResult.add(address);
                        }
                        if (mAddressResult != null) {
                            mAddressAdapter.setDatasource(mAddressResult);
                            mAddressAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(SelectAddressActivity.this, "无结果", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mLoadingEmptyView.setVisibility(View.GONE);
                    mAddressListView.setEmptyView(mResultEmptyView);
                    Toast.makeText(SelectAddressActivity.this, "搜索兴趣点出错。错误码：" + retCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        departCoord[0] = aMapLocation.getLatitude();
        departCoord[1] = aMapLocation.getLongitude();
        if (mCurrentCity.equals("") && !aMapLocation.getCity().equals(mCurrentCity)) {
            mCurrentCity = aMapLocation.getCity();
            getAddressList(SelectAddressActivity.this, mDestPoiName, mCurrentCity);
        }
    }
}
