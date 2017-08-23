package com.gionee.secretary.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.AddressJason;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.activity.SelectAddressActivity;
import com.gionee.secretary.ui.viewInterface.ISetWeatherListener;

import java.util.List;

import amigoui.app.AmigoAlertDialog;

/**
 * Created by liyy on 16-6-15.
 */
public class NavigateUtil {
    private static final String TAG = "NavigateUtil";
    static String startCityCode;//当前城市地区编码
    static String startAddress;//当前地址
    static double startLatitude;//当前维度
    static double startLongitude;//当前经度

    static String endCityCode;//目的城市城市地区编码
    static String endAddress;//目的地址
    static double endLatitude;//目的维度
    static double endLongitude;//目的经度
    private static List<String> cityAddressJson;

    public static void navigateToDes(Context context, String des, String traveltype) {
        if (TextUtils.isEmpty(des.trim())) {
            return;
        }
//        try {
//            String station = "androidamap://keywordNavi?sourceApplication=secretary&keyword=" +des + " &style=2";
//            Intent intent = new Intent("android.intent.action.VIEW",android.net.Uri.parse(station));
//            intent.setPackage("com.autonavi.minimap");
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//            context.startActivity(intent);
//            LogUtils.i(TAG, "TrainTick....navigstartstation......................");
//        }catch (ActivityNotFoundException e){
////            Toast.makeText(context, context.getResources().getString(R.string.not_found_map), Toast.LENGTH_LONG).show();
//            showDialogForDownLoad(context);
//        }
        //int mTravelType = Integer.parseInt(SettingModel.getInstance(context).getDefaultTravelMethod());
        Intent intent = new Intent(context, SelectAddressActivity.class);
        intent.putExtra("DEST_POI_NAME", des);
        intent.putExtra("TRAVEL_TYPE", traveltype);
        context.startActivity(intent);
    }


    public static boolean canNavigate(Context context) {
        boolean map_available = appAvailable(context, Constants.APP_MAP_PACKAGENAME, Constants.APP_MAP_CLASSNAME);
        boolean market_available = appAvailable(context, Constants.APP_MARKET_PACKAGENAME, Constants.APP_MARKET_CLASSNAME);
        if (!map_available && !market_available) {
            return false;
        } else {
            return true;
        }
    }


    private static boolean appAvailable(Context context, String pkgName, String className) {
        List resolveInfo = null;
        if (context != null) {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent();
            intent.setClassName(pkgName, className);
            resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);

        }

        if (resolveInfo != null && resolveInfo.size() != 0) {
            return true;
        } else {
            return false;
        }
    }


    private static void showDialogForDownLoad(final Context context) {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.not_found_app_message_title));
        builder.setMessage(context.getString(R.string.not_found_map));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(context.getString(R.string.download), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mainIntent = new Intent(Constants.MARKET_SEARCH_ACTION, null);
                mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY1, Constants.MARKET_EXTRA_MAP_VALUE);
                mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY2, Constants.MARKET_EXTRA_MAP_VALUE);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                context.startActivity(mainIntent);
            }
        });
        builder.show();
    }

    public static void navigateToDesOTA(final Context context, String desAddress) {
        cleanDatas();
        if (TextUtils.isEmpty(desAddress.trim())) {
            return;
        }
        //定位当前位置名称，获取经纬度 定位1
        getCurrentLocation(context);
        //定位目标位置名称，获取经纬度 定位2
        getAddressList(context, desAddress, "");
        // 调起用高德导航界面
        startAMap(context);
    }

    public static void startAMap(Context context) {
        try {
            String station = "androidamap://route?sourceApplication=secretary&slat=0.0" + "&slon=0.0" + "&sname=我的位置" + "&dlat=0.0" + "&dlon=0.0" + "&dname=" + "北京西站" + "&dev=0&m=0&t=2";
            Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse(station));
            intent.setPackage("com.autonavi.minimap");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            context.startActivity(intent);
            LogUtils.i(TAG, "TrainTick....navigstartstation......................");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getResources().getString(R.string.not_found_map), Toast.LENGTH_LONG).show();
        }
    }

    public static void getCurrentLocation(final Context context) {

        final LocationUtil mLocationUtil = new LocationUtil(context);
        mLocationUtil.startLocation(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {

                    LogUtils.e(TAG, "-------errdr code : " + aMapLocation.getErrorCode());
                    startCityCode = aMapLocation.getAdCode();//城市地区编码
                    startAddress = aMapLocation.getAddress();//地址
                    startLatitude = aMapLocation.getLatitude();//获取纬度
                    startLongitude = aMapLocation.getLongitude();////获取经度
                    LogUtils.e(TAG, "---first..location....current.....----citycode : " + startCityCode +
                            "  ,startAddress:" + startAddress +
                            "  ,startLatitude:" + startLatitude +
                            "  ,startLongitude:" + startLongitude);
                } else {
                    Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();

                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    LogUtils.e(TAG, "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                    //每次点击导航都会重启导航定位所以定位错误时候应该停止并销毁客户端
                    mLocationUtil.mLocationClient.stopLocation();//停止定位
                    mLocationUtil.mLocationClient.onDestroy();//销毁定位客户端

                }
            }
        });
    }

    public static void getAddressList(Context context, String address, String cityName) {
        endAddress = address;
        PoiSearch.Query poiSearch = new PoiSearch.Query(address, "", startCityCode);
        poiSearch.setPageSize(20);
        poiSearch.setPageNum(0);
        poiSearch.setCityLimit(true);
        PoiSearch search = new PoiSearch(context, poiSearch);
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    if (null != poiResult) {
                        List<PoiItem> listPOI = poiResult.getPois();
                        if (null != listPOI && listPOI.size() > 0) {

                            endCityCode = listPOI.get(0).getAdCode();
                            endLatitude = listPOI.get(4).getLatLonPoint().getLatitude();
                            endLongitude = listPOI.get(2).getLatLonPoint().getLongitude();
//                            List<AddressBean> list = new ArrayList<AddressBean>();
                            for (int l = 0; l < listPOI.size(); l++) {
                                LogUtils.i(TAG, "title:" + listPOI.get(l).getTitle() + "  ,snippet:" + listPOI.get(l).getSnippet()
                                        + "  ,latitue:" + listPOI.get(l).getLatLonPoint().getLatitude() + "    ,longitude:" + listPOI.get(l).getLatLonPoint().getLongitude());
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

    public static void getAddressCity(final Context context, final String address, final ISetWeatherListener listener, final boolean isDeparture) {
        PoiSearch.Query poiSearch = new PoiSearch.Query(address, "", startCityCode);
        poiSearch.setPageSize(10);
        poiSearch.setPageNum(0);
        PoiSearch search = new PoiSearch(context, poiSearch);
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    if (poiResult == null) {
                        return;
                    }
                    List<PoiItem> listPOI = poiResult.getPois();
                    if (null != listPOI && listPOI.size() > 0) {
                        AddressJason address = setAddressJason(listPOI.get(0));
                        //如：{"cityName":"深圳","provinceName":"广东"}{"cityName":"海口","provinceName":"海南"}
                        LogUtils.i("luorw", address.toString());
                        WeatherQueryUtil.getWeatherLive(context, listener, address, isDeparture);
                    }
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        search.searchPOIAsyn();
    }

    private static AddressJason setAddressJason(PoiItem poiItem) {
        AddressJason addressJason = new AddressJason();
        String cityName = poiItem.getCityName();
        String provinceName = poiItem.getProvinceName();
       /* if (provinceName.contains("黑龙江") || provinceName.contains("内蒙古")) {
            provinceName = provinceName.substring(0, 3);
        } else {
            provinceName = provinceName.substring(0, 2);
        }
        if (cityName.contains("沙市") || cityName.contains("黄山市") || cityName.contains("津市")) {
            addressJason.setCityName(cityName);
        } else {
            addressJason.setCityName(cityName.replace("市", ""));
        }*/
        addressJason.setCityName(cityName);
        addressJason.setProvinceName(provinceName);
        return addressJason;
    }

    public static void cleanDatas() {
        startCityCode = null;//当前城市
        startAddress = null;//当前地址
        startLatitude = 0.0D;//当前维度
        startLongitude = 0.0D;//当前经度

        endCityCode = null;//目的城市
        endAddress = null;//目的地址
        endLatitude = 0.0D;//目的维度
        endLongitude = 0.0D;
        //目的经度
    }

    /**
     * 检查位置信息服务开启情况。若未开启，则弹出对话框让用户开启
     *
     * @param context context
     * @return 位置信息是否开启。true为已开启，false为未开启。
     */
    public static boolean checkLocationService(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(context);
            builder.setTitle("位置信息未开启");
            builder.setMessage("为了使用导航功能，请确定位置信息已开启");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
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
            return false;
        } else {
            return true;
        }
    }

}
