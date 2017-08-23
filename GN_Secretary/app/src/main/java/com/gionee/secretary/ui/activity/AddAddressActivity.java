package com.gionee.secretary.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gionee.secretary.R;
import com.gionee.secretary.adapter.MyAddressAdapter;
import com.gionee.secretary.bean.AddressBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.ACache;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LocationUtil;
import com.gionee.secretary.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.widget.AmigoButton;


/**
 * Created by rongdd on 16-5-11.
 */
public class AddAddressActivity extends PasswordBaseActivity implements TextWatcher, AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "AddAddressActivity";
    private EditText addressEdit;
    private ImageButton mClearEditTextButton;
    private ListView addressLV;
    private AmigoButton addressClean;
    private String citycode;
    // private List<PoiItem> listPOI;
    private ACache mACache;
    private MyAddressAdapter mAdapter;
    private boolean etIsEmpty = false;
    private LocationUtil mLocationUtil;
    private AMapLocation aMapLocation;
    private List<AddressBean> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        initDatas();
        initActionBar();
        initViews();
        setListeners();
        // if (null != listPOI) {
        // listPOI.clear();
        // } else {
        // listPOI = new ArrayList<PoiItem>();
        // }

        list = (List<AddressBean>) mACache.getAsObject(Constants.HISTORY_ADDRESS);
        if (null != list && list.size() > 0) {
            addressLV.setVisibility(View.VISIBLE);
            // for (int i = 0; i < list.size(); i++) {
            // LatLonPoint latLonPoint = new LatLonPoint(
            // Double.parseDouble(list.get(i).getmLatitude()),
            // Double.parseDouble(list.get(i).getmLongitude()));
            // PoiItem poi = new PoiItem(null, latLonPoint,
            // list.get(i).getName(), null);
            // poi.setAdName(list.get(i).getName());
            // listPOI.add(poi);
            // }
            mAdapter = new MyAddressAdapter(AddAddressActivity.this, list);
            addressLV.setAdapter(mAdapter);
            addressClean.setVisibility(View.VISIBLE);
        } else {
            addressClean.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // ((SecretaryApplication)getApplication()).isLocked = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if(((SecretaryApplication)getApplication()).isLocked) {
        // Intent intent = new Intent(this, SplashActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        // startActivity(intent);
        // }
    }

    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View view = getLayoutInflater().inflate(R.layout.actionbar_search_activity, null);
        ImageView btn_back = (ImageView) view.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addressEdit = (EditText) view.findViewById(R.id.search_edittext);
        mClearEditTextButton = (ImageButton) view.findViewById(R.id.clearButton);
        mClearEditTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressEdit.setText("");
            }
        });

        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.WRAP_CONTENT, AmigoActionBar.LayoutParams.WRAP_CONTENT);
        mActionBar.setCustomView(view, param);
        mActionBar.show();

    }

    private void initDatas() {
        mACache = ACache.get(this);
        mLocationUtil = new LocationUtil(this);
        mLocationUtil.setEnableOnceLocation(false);
        mLocationUtil.setLocationInterval(60000);
    }

    private void initViews() {
        addressLV = (ListView) findViewById(R.id.address_listview);
        addressClean = (AmigoButton) findViewById(R.id.address_clean);
        addressEdit.addTextChangedListener(AddAddressActivity.this);

    }

    private void setListeners() {
        addressLV.setOnItemClickListener(this);
        addressClean.setOnClickListener(this);
        // --- 定位
        mLocationUtil.startLocation(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                AddAddressActivity.this.aMapLocation = aMapLocation;
                if (aMapLocation.getErrorCode() == 0) {

                    LogUtils.e(TAG, "-------errdr code : " + aMapLocation.getErrorCode());
                    citycode = aMapLocation.getAdCode();
                    LogUtils.e(TAG, "-------code : " + citycode);

                    if (!TextUtils.isEmpty(addressEdit.getText())) {
                        String address = addressEdit.getText().toString().trim();
                        if (!TextUtils.isEmpty(address)) {
                            getAddressList(address, aMapLocation.getCity());
                        }
                    }
                } else {
                    Toast.makeText(AddAddressActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                    // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    LogUtils.e(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
                }
            }
        });
    }

    /**
     * 获取地址列表
     *
     * @param address
     * @param cityName
     */
    public void getAddressList(String address, String cityName) {
        PoiSearch.Query poiSearch = new PoiSearch.Query(address, "", cityName);
        poiSearch.setPageSize(20);
        poiSearch.setPageNum(0);
        poiSearch.setCityLimit(true);
        PoiSearch search = new PoiSearch(AddAddressActivity.this, poiSearch);
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    if (null != poiResult) {
                        List<PoiItem> listPOI = poiResult.getPois();
                        if (null != listPOI && listPOI.size() > 0) {
                            list = new ArrayList<AddressBean>();
                            for (int l = 0; l < listPOI.size(); l++) {
                                AddressBean addressBean = new AddressBean();
                                addressBean.setmLatitude(listPOI.get(l).getLatLonPoint().getLatitude());
                                addressBean.setmLongitude(listPOI.get(l).getLatLonPoint().getLongitude());
                                addressBean.setName(listPOI.get(l).getTitle());
                                addressBean.setDesc(listPOI.get(l).getSnippet());
                                list.add(addressBean);
                            }
                            // TODO
                            addressLV.setVisibility(View.VISIBLE);
                            addressClean.setVisibility(View.GONE);
                            if (etIsEmpty || !addressEdit.getText().toString().equals(poiResult.getQuery().getQueryString())) {
                                return;
                            }
                            if (mAdapter == null) {
                                mAdapter = new MyAddressAdapter(AddAddressActivity.this, list);
                                addressLV.setAdapter(mAdapter);
                            } else {
                                addressLV.setAdapter(mAdapter);
                                mAdapter.setNewDate(list);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        search.searchPOIAsyn();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Constants.ADDRESS, list.get(position).getName());
        intent.putExtra(Constants.LONGITUDE, list.get(position).getmLongitude());
        intent.putExtra(Constants.LATITUDE, list.get(position).getmLatitude());
        intent.putExtra(Constants.DESC, list.get(position).getDesc());
        LogUtils.e(TAG, "getTitle --- > " + list.get(position).getName());
        LogUtils.e(TAG, "getLongitude --- > " + list.get(position).getmLongitude());
        LogUtils.e(TAG, "getLatitude --- > " + list.get(position).getmLatitude());
        LogUtils.e(TAG, "getDesc --- > " + list.get(position).getDesc());
        intent.setClass(this, AddressRemarkActivity.class);
        startActivity(intent);
        AddAddressActivity.this.finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        etIsEmpty = TextUtils.isEmpty(s);
        mClearEditTextButton.setVisibility(etIsEmpty ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(s) && AddAddressActivity.this.aMapLocation != null) {
            getAddressList(s.toString(), aMapLocation.getCity());
        } else if (mAdapter != null) {
            // Fix GNSPR #68877。判断此时EditText是否为空。当为空时就不再接受地址列表回调结果。
            list = (List<AddressBean>) mACache.getAsObject(Constants.HISTORY_ADDRESS);
            if (list != null) {
                mAdapter.setNewDate(list);
                addressClean.setVisibility(View.VISIBLE);
            } else {
                mAdapter.clearData();
                addressClean.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.address_clean:
                mACache.clear();
                addressLV.setAdapter(null);
                addressClean.setVisibility(View.GONE);
                break;
        }
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

}
