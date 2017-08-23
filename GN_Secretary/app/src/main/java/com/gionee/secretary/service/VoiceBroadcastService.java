package com.gionee.secretary.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.gionee.secretary.bean.AddressJason;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.LocationUtil;
import com.gionee.secretary.utils.NetWorkUtil;
import com.gionee.secretary.utils.WeatherQueryUtil;
import com.gionee.secretary.utils.BroadcastMsgUtil;

import java.util.List;

/**
 * Created by luorw on 3/24/17.
 */
public class VoiceBroadcastService extends IntentService implements AMapLocationListener {
    private static final String TAG = "VoiceBroadcastService";
    private static final String APPID = "secretaryappid";
    private List<BaseSchedule> mScheduleList;
    private String mBroadcastMsg;
    private Context mContext;
    private LocationUtil mLocationUtil;

    public VoiceBroadcastService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
//        startVoiceBroadCast();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private void startVoiceBroadCast() {
        mContext = this;
        mScheduleList = ScheduleInfoDao.getInstance(mContext).getTodaySchedulesForBroadcast();
        if (!NetWorkUtil.isNetworkAvailable(mContext)) {
            mBroadcastMsg = BroadcastMsgUtil.getBroadcastMsg(mScheduleList, BroadcastMsgUtil.NETWORK_UNAVAILABLE, mContext);
            startVoice(mBroadcastMsg);
        } else {
            getLocationWeather();
        }
    }

    private void getLocationWeather() {
        mLocationUtil = new LocationUtil(this);
        mLocationUtil.setEnableOnceLocation(true);
        mLocationUtil.startLocation(this);
    }

    private void startVoice(String message) {
        Intent intent = new Intent();
        intent.setClassName("gn.com.voice", "gn.com.voice.TTSService");
        intent.putExtra("appid", APPID);
        intent.putExtra("type", "speak_info");
        intent.putExtra("operation", "read_msg");
        intent.putExtra("message", message);
        this.startService(intent);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        AddressJason address = mLocationUtil.setAddressJason(aMapLocation.getCity(), aMapLocation.getProvince());
        WeatherQueryUtil.getWeatherForBroadCast(mContext, new IWeatherBroadcastListener() {
            @Override
            public void getWeather(List<WeatherSchedule> list) {
                if (list == null || list.size() == 0) {
                    mBroadcastMsg = BroadcastMsgUtil.getBroadcastMsg(mScheduleList, BroadcastMsgUtil.NETWORK_UNAVAILABLE, mContext);
                } else {
                    String weatherMsg = BroadcastMsgUtil.getWeatherMsg(list);
                    mBroadcastMsg = BroadcastMsgUtil.getBroadcastMsg(mScheduleList, weatherMsg, mContext);
                }
                startVoice(mBroadcastMsg);
            }
        }, address);
    }

    public interface IWeatherBroadcastListener {
        void getWeather(List<WeatherSchedule> list);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
