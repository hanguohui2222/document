package com.gionee.secretary.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
//import com.autonavi.v2.protocol.BaseRequest;
//import com.autonavi.v2.protocol.ReqCallback;
//import com.autonavi.v2.protocol.aos.ReqKeywordSearch;
//import com.autonavi.v2.protocol.its.lineinfo.ReqETSLineInfo;
//import com.autonavi.v2.protocol.model.Account;
//import com.autonavi.v2.protocol.model.POI;
//import com.autonavi.v2.protocol.model.Point;
//import com.autonavi.v2.protocol.model.aos.SearchResult;
//import com.autonavi.v2.protocol.model.ets.Route;
import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by rongdd on 16/5/13.
 */
public class RouteUtils implements RouteSearch.OnRouteSearchListener {

    private static final String TAG = "RouteUtils";
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private IRouteListener mListener;
    boolean isSuccess = false;
    private Thread mThread;
    private LatLonPoint endPoint;
    private LatLonPoint mEndPoint;
    private int index = 0;
    private AMapLocation aMapLocation;

    public RouteUtils(Context mContext, IRouteListener mListener, AMapLocation aMapLocation) {
        this.mContext = mContext;
        this.mListener = mListener;
        this.aMapLocation = aMapLocation;
        mRouteSearch = new RouteSearch(mContext);
        mRouteSearch.setRouteSearchListener(this);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!isSuccess && endPoint != null && index < 2) {
                LogUtils.e(TAG, "index  :  " + index);
                getWalkAndBusResult(mEndPoint, Constants.ROUTE_TYPE_DRIVE);
            }

        }
    };

    /**
     * drive 开车
     */
    public void getDriveResult(LatLonPoint mEndPoint) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    //
                } else {
                    try {
                        Thread.sleep(10000);
                        mHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThread.start();
        if (aMapLocation.getErrorCode() == 0) {
            index++;
            try {
                Date date = Calendar.getInstance().getTime();
                date.setHours(date.getHours() - 1);

//                Account account = new Account(User.ID, User.PWD);
//                List<Point> pointStartList = new ArrayList<>();
                double latitude = aMapLocation.getLatitude();
                double longitude = aMapLocation.getLongitude();
                LatLonPoint mStartPoint = new LatLonPoint(latitude, longitude);
                if (mStartPoint == null) {
                    Toast.makeText(mContext, "定位中，稍后再试...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mEndPoint == null) {
                    Toast.makeText(mContext, "终点未设置", Toast.LENGTH_SHORT).show();
                }
                RouteSearch driveRouteSearch = new RouteSearch(mContext);
                driveRouteSearch.setRouteSearchListener(this);
                RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
                RouteSearch.DriveRouteQuery driveQuery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingAvoidCongestion, null, null, null);
                driveRouteSearch.calculateDriveRouteAsyn(driveQuery);
//                Point point = new Point(longitude, latitude);
//                pointStartList.add(point);
//                ReqETSLineInfo req = new ReqETSLineInfo(account, Route.CALC_TYPE_COST_PRIORITY, date,
//                        pointStartList, null, endPoint);
//                req.doRequest(new ReqCallback() {
//                    @Override
//                    public void onStart(BaseRequest baseRequest) {
//                        LogUtils.e(TAG,"onStart  :  " + baseRequest.getMsg());
//                    }
//
//                    @Override
//                    public void onSuccess(BaseRequest baseRequest) {
//                        LogUtils.e(TAG,"onSuccess");
//                        LogUtils.e(TAG,"ReqETSLineInfo  =  " + baseRequest.getType());//ReqETSLineInfo
//                        if (ReqETSLineInfo.TYPE.equals(baseRequest.getType())) {
//                            double length = ((ReqETSLineInfo) baseRequest).getETSInfo().getRouteList().get(0).getRouteLength();
//                            double time = ((ReqETSLineInfo) baseRequest).getETSInfo().getRouteList().get(0).getRouteTime();
//                            //TODO
//                            mListener.getRouteTime(time);
//                            isSuccess = true;
//                            LogUtils.e(TAG, "drive onSuccess time : " + time + " 秒");
//                        }
//                    }
//
//                    @Override
//                    public void onFail(BaseRequest baseRequest) {
//                        LogUtils.e(TAG,"onFail   :   " + baseRequest.getMsg());
//                    }
//
//                    @Override
//                    public void onNetError(BaseRequest baseRequest) {
//                        LogUtils.e(TAG,"onNetError  :   " + baseRequest.getMsg());
//                    }
//                },true);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } else {
            //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
            LogUtils.e(TAG, "location Error, ErrCode:"
                    + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
        }
    }

    /**
     * 步行，公交
     */
    public void getWalkAndBusResult(LatLonPoint mEndPoint, int routeType) {
        LogUtils.e(TAG, "code : " + aMapLocation.getErrorCode());
        if (aMapLocation.getErrorCode() == 0) {
            String mCurrentCityName = aMapLocation.getCity();
            double latitude = aMapLocation.getLatitude();
            double longitude = aMapLocation.getLongitude();
            LatLonPoint mStartPoint = new LatLonPoint(latitude, longitude);
            if (mStartPoint == null) {
                Toast.makeText(mContext, "定位中，稍后再试...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mEndPoint == null) {
                Toast.makeText(mContext, "终点未设置", Toast.LENGTH_SHORT).show();
            }
            final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
            if (routeType == Constants.ROUTE_TYPE_BUS) {// 公交路径规划
                RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusDefault,
                        mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
                mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
            } else if (routeType == Constants.ROUTE_TYPE_DRIVE) {// 驾车路径规划
                RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null,
                        null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
                mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
            } else if (routeType == Constants.ROUTE_TYPE_WALK) {// 步行路径规划
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
            }
        } else {
            //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
            LogUtils.e(TAG, "location Error, ErrCode:"
                    + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    mBusRouteResult = busRouteResult;
                    final BusPath busPath = mBusRouteResult.getPaths().get(0);
                    int dur = (int) busPath.getDuration();
                    mListener.getRouteTime(dur);
                    LogUtils.e(TAG, "bus time : " + dur + " 秒");
                } else if (busRouteResult != null && busRouteResult.getPaths() == null) {
                    Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, errorCode + "", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    int dur = (int) drivePath.getDuration();
                    mListener.getRouteTime(dur);
                    isSuccess = true;
                    LogUtils.e(TAG, "drive time : " + dur + " 秒");
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, errorCode + "", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
                if (walkRouteResult.getPaths().size() > 0) {
                    mWalkRouteResult = walkRouteResult;
                    final WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
                    LogUtils.e(TAG, "sizze : " + mWalkRouteResult.getPaths().size());
                    int dur = (int) walkPath.getDuration();
                    mListener.getRouteTime(dur);
                    LogUtils.e(TAG, "walk time : " + dur + " 秒");
                } else if (walkRouteResult != null && walkRouteResult.getPaths() == null) {
                    Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, errorCode + "", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * @param address  当前定位地点
     * @param cityName 所在城市
     * @param type     出行方式
     */
    public void getRoute(String address, String cityName, final int type) {
        PoiSearch.Query poiSearch = new PoiSearch.Query(address, "", cityName);
        poiSearch.setPageSize(20);
        poiSearch.setPageNum(0);
        poiSearch.setCityLimit(true);
        PoiSearch search = new PoiSearch(mContext, poiSearch);
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    if (null != poiResult) {
                        List<PoiItem> listPOI = poiResult.getPois();
                        PoiItem item = listPOI.get(0);
                        // ------ item --->
                        LatLonPoint point = item.getLatLonPoint();
                        double longitude = point.getLongitude();
                        double latitude = point.getLatitude();
                        LogUtils.e(TAG, "longitude = " + longitude);
                        LogUtils.e(TAG, "latitude = " + latitude);
                        endPoint = new LatLonPoint(latitude, longitude);
                        mEndPoint = new LatLonPoint(latitude, longitude);
                        if (type == Constants.TYPE_DRIVE) {
                            getDriveResult(endPoint);
                        } else if (type == Constants.TYPE_BUS) {
                            getWalkAndBusResult(mEndPoint, Constants.ROUTE_TYPE_BUS);
                        } else if (type == Constants.TYPE_WALK) {
                            getWalkAndBusResult(mEndPoint, Constants.ROUTE_TYPE_WALK);
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

    public interface IRouteListener {
        void getRouteTime(long time);
    }
}
