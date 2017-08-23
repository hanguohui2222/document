package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.TravelModeUtil;
import com.gionee.secretary.utils.WeatherIconUtil;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.gionee.secretary.ui.viewInterface.ISetWeatherListener;
import com.ted.android.core.SmsEntityLoader;
import com.ted.android.data.ClickType;
import com.ted.android.data.SmsEntity;
import com.ted.android.data.bubbleAction.ActionBase;
import com.gionee.secretary.utils.NavigateUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import amigoui.widget.AmigoTextView;

/**
 * Created by liu on 5/17/16.
 */
public class AirTicketDetailsFragment extends Fragment implements ISetWeatherListener {
    private static final String TAG = "CreditCardDetailsFragment";
    AmigoTextView mStartStation;
    AmigoTextView mEndStation;
    AmigoTextView mDate;
    AmigoTextView mDateStart;
    AmigoTextView mDateEnd;
    AmigoTextView mTimeTakeoff;
    AmigoTextView mTimeArrive;
    AmigoTextView mFlightNo;
    AmigoTextView mPassenger;
    AmigoTextView mTicketNo;
    AmigoTextView mTicketSource;
    AmigoTextView mSeatNo;
    AmigoTextView mServiceTel;
    AmigoTextView mServiceWebsite;
    FlightSchedule schedule;
    Context mContext;
    AmigoTextView mNavigStartStation;
    AmigoTextView mStartCity;
    AmigoTextView mEndCity;
    AmigoTextView mStartCityTemp;
    AmigoTextView mEndCityTemp;

    ImageView mStartingWeather;
    ImageView mEndingWeather;
    AmigoTextView mStartingWeatherTxt;
    AmigoTextView mEndingWeatherTxt;
    RelativeLayout mStartCityLayout;
    RelativeLayout mEndCityLayout;
    private final MyHandler mHandler = new MyHandler(this);

    public AirTicketDetailsFragment() {
        // Required empty public constructor
    }

    public static AirTicketDetailsFragment newInstance() {
        AirTicketDetailsFragment fragment = new AirTicketDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (FlightSchedule) activity.getSchedule();
        setWeatherInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_air_ticket_details, container, false);
        initView(root);
        getEntryActions();
        updateScheduleInfo();
        initNavigate();
        return root;
    }

    /*
    获取天气情况
     */
    private void setWeatherInfo() {
        if(schedule == null)
            return;
        if (schedule.getStartAddress() != null && !"null".equalsIgnoreCase(schedule.getStartAddress())) {
            NavigateUtil.getAddressCity(mContext, schedule.getStartAddress(), this, true);
        }
        if (schedule.getDestination() != null && !"null".equalsIgnoreCase(schedule.getDestination())) {
            NavigateUtil.getAddressCity(mContext, schedule.getDestination(), this, false);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        initNavigate();
    }

    private void initView(View root) {
        mStartStation = (AmigoTextView) root.findViewById(R.id.start_station);
        mEndStation = (AmigoTextView) root.findViewById(R.id.end_station);
        mTimeTakeoff = (AmigoTextView) root.findViewById(R.id.time_detail_start);
        mTimeArrive = (AmigoTextView) root.findViewById(R.id.time_detail_arrive);
        mDateStart = (AmigoTextView) root.findViewById(R.id.date_start);
        mDateEnd = (AmigoTextView) root.findViewById(R.id.date_end);
        mFlightNo = (AmigoTextView) root.findViewById(R.id.flight_no);
        mPassenger = (AmigoTextView) root.findViewById(R.id.passenger);
        mTicketNo = (AmigoTextView) root.findViewById(R.id.order_no);
        mTicketSource = (AmigoTextView) root.findViewById(R.id.ticket_source);
        mServiceTel = (AmigoTextView) root.findViewById(R.id.service_tel);
        mServiceWebsite = (AmigoTextView) root.findViewById(R.id.service_website);
        mNavigStartStation = (AmigoTextView) root.findViewById(R.id.navig_start_station);


        mSeatNo = (AmigoTextView) root.findViewById(R.id.seat_no);
        mStartCity = (AmigoTextView) root.findViewById(R.id.starting_city);
        mEndCity = (AmigoTextView) root.findViewById(R.id.ending_city);
        mStartCityTemp = (AmigoTextView) root.findViewById(R.id.starting_temp);
        mEndCityTemp = (AmigoTextView) root.findViewById(R.id.ending_temp);
        mStartingWeather = (ImageView) root.findViewById(R.id.starting_city_weather_img);
        mEndingWeather = (ImageView) root.findViewById(R.id.ending_city_weather_img);
        mStartingWeatherTxt = (AmigoTextView) root.findViewById(R.id.starting_city_weather_txt);
        mEndingWeatherTxt = (AmigoTextView) root.findViewById(R.id.ending_city_weather_txt);
        mStartCityLayout = (RelativeLayout) root.findViewById(R.id.start_city_weather_layout);
        mEndCityLayout = (RelativeLayout) root.findViewById(R.id.end_city_weather_layout);
        mStartCityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                enterWeatherApp();
            }
        });
        mEndCityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                enterWeatherApp();
            }
        });
    }

    private void updateScheduleInfo() {
        if(schedule == null)
            return;
        CardDetailsUtils.setTextView(mDateStart, DateUtils.getDate(schedule.getDate()), true);
        CardDetailsUtils.setTextView(mStartStation, schedule.getStartAddress(), true);
        CardDetailsUtils.setTextView(mEndStation, schedule.getDestination(), true);
        CardDetailsUtils.setTextView(mFlightNo, schedule.getFlightNum(), true);
        CardDetailsUtils.setTextView(mTimeTakeoff, DateUtils.time2String(schedule.getDate()), true);
        CardDetailsUtils.setTextView(mPassenger, "乘客", schedule.getPassenger(), true);
        CardDetailsUtils.setTextView(mSeatNo, "座位号", null);
        CardDetailsUtils.setTextView(mTicketNo, "订单号", schedule.getTicketNum(), false);
        SpannableStringBuilder serviceTelString = TextUtilTools.hightLightText("客服电话：" + schedule.getServiceNum(), schedule.getServiceNum(), mContext.getResources().getColor(R.color.flight_card_detail_service_tel_highlight_color));
        CardDetailsUtils.setTextView(mServiceTel, serviceTelString, schedule.getServiceNum());
        CardDetailsUtils.setTextView(mTicketSource, "机票来源", schedule.getAirlineSource(), false);
        CardDetailsUtils.setShowStatus(mServiceTel, schedule.getServiceNum());
        CardDetailsUtils.setShowStatus(mServiceWebsite, null);
        CardDetailsUtils.setShowStatus(mSeatNo, null);
        //added by luorw for GNSPR #72122 2017-03-15 begin
        setArrivalDateAndTime(schedule.getArrivalTime());
        //added by luorw for GNSPR #72122 2017-03-15 end
        mNavigStartStation.setText(mContext.getString(R.string.navigate_to) + schedule.getStartAddress());
    }

    //added by luorw for GNSPR #72122 2017-03-15 begin
    private void setArrivalDateAndTime(String arrivalTime) {
        if (!TextUtils.isEmpty(arrivalTime) && !arrivalTime.equals("null") && (!arrivalTime.trim().equals("")) && arrivalTime != null) {
            if (arrivalTime.contains("日")) {
                String subTime = arrivalTime.substring(arrivalTime.indexOf("日") + 1, arrivalTime.length());
                mTimeArrive.setText(subTime);
                String subDate = arrivalTime.substring(0, arrivalTime.indexOf("日") + 1);
                LogUtils.e("zhubq", subDate + "subDate");
                if (subDate.contains("月") && subDate.contains("日")) {
                    mDateEnd.setText(subDate);
                }
            } else {
                mTimeArrive.setText(arrivalTime);
                //added by luorw for GNSPR #79108 2017-04-12 begin
                CardDetailsUtils.setTextView(mDateEnd, getArriveDate(arrivalTime), true);
                //added by luorw for GNSPR #79108 2017-04-12 end
            }
        } else {
            mTimeArrive.setText("--");
            mDateEnd.setText("--");
        }
    }

    //added by luorw for GNSPR #72122 2017-03-15 end
    //added by luorw for GNSPR #79108 2017-04-12 begin
    private String getArriveDate(String arrivalTime) {
        if(schedule == null)
            return "";
        String arriveDate = null;
        Date date = schedule.getDate();
        int takeOffHour = date.getHours();
        int arriveHour = 100;
        if (arrivalTime.contains(":")) {
            String str = arrivalTime.split(":")[0];
            arriveHour = Integer.parseInt(str);
        }
        //出发小时大于到达小时说明是第二天到达
        if (takeOffHour > arriveHour) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date arrive = new Date(calendar.getTimeInMillis());
            arriveDate = DateUtils.getDate(arrive);
        } else {
            arriveDate = DateUtils.getDate(date);
        }
        return arriveDate;
    }

    //added by luorw for GNSPR #79108 2017-04-12 end
    private void initNavigate() {
        if (NavigateUtil.canNavigate(mContext)) {
            mNavigStartStation.setVisibility(View.VISIBLE);
            mNavigStartStation.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(schedule == null)
                        return;
                    int mTravelType = Integer.parseInt(SettingModel.getInstance(mContext).getDefaultTravelMethod());
                    NavigateUtil.navigateToDes(getActivity(), schedule.getStartAddress(), TravelModeUtil.getTravelType(mTravelType));
                    LogUtils.i(TAG, "airTick....navigstartstation......................");
                }
            });
        } else {
            mNavigStartStation.setVisibility(View.GONE);
        }

        if (!TravelModeUtil.isMapSupportMode(getActivity())) {
            mNavigStartStation.setVisibility(View.GONE);
        }
    }

    private void getEntryActions() {
        if(schedule == null)
            return;
        LogUtils.i(TAG, "getEntryActions...getSmsContent:" + schedule.getSmsContent() + "  getSmsSender:" + schedule.getSmsSender());
        long key = System.currentTimeMillis();
        SmsEntityLoader entityLoader = SmsEntityLoader.getInstance(mContext.getApplicationContext());

        SmsEntity entity = entityLoader.loadSmsEntity(key, schedule.getSmsContent(), schedule.getSmsSender(),
                System.currentTimeMillis(),
                new SmsEntityLoader.SmsEntityLoaderCallback() {
                    @Override
                    public void onSmsEntityLoaded(Long aLong,
                                                  SmsEntity smsEntity) {
                        List<ActionBase> showActions = filterAction(smsEntity);
                        if (null != showActions && showActions.size() >= 0) {
                            LogUtils.i(TAG, "getEntryActions...showActions.size:" + showActions.size());
                            for (ActionBase ab : showActions) {
                                if (ab.action == ClickType.CLICKTYPE_PHONE_NUMBER) {
                                    setClickLink(ab);
                                }
                                LogUtils.i(TAG, "getEntryActions....!entity.hasBusinessADAction.......actionBase:" + ab.toString());
                            }
                        }
                    }
                });
        if (entity != null) {
            LogUtils.e(TAG, entity.toString());
        }
    }

    private List<ActionBase> filterAction(SmsEntity entity) {
        List<ActionBase> showActions = new ArrayList<ActionBase>();
        List<ActionBase> actionBases = entity.getAllActions();
        LogUtils.i(TAG, "filterAction...entity:" + entity.toString());
        if (entity != null) {
            if (entity.hasBusinessADAction()) {
                for (ActionBase actionBase : actionBases) {
                    if (actionBase.businessType == ActionBase.BUSINESS_TYPE_AD) {
                        LogUtils.i(TAG, "filterAction....businessType == ActionBase.BUSINESS_TYPE_AD.......actionBase:" + actionBase.toString());
                        showActions.add(actionBase);
                    } else {
                        LogUtils.i(TAG, "filterAction....businessType != ActionBase.BUSINESS_TYPE_AD.......actionBase:" + actionBase.toString());
                        showActions.add(actionBase);
                    }
                }
            } else {
                LogUtils.i(TAG, "filterAction....!entity.hasBusinessADAction.......actionBase");
                showActions = actionBases;
            }
        }
        return showActions;
    }

    @Override
    public void showDepartureWeather(WeatherSchedule Weather) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = Weather;
        mHandler.sendMessage(msg);
    }

    @Override
    public void showDestinationWeather(WeatherSchedule Weather) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = Weather;
        mHandler.sendMessage(msg);
    }

    private static class MyHandler extends Handler{
        private final WeakReference<AirTicketDetailsFragment> mFragment;
        public MyHandler(AirTicketDetailsFragment fragment){
            mFragment = new WeakReference<AirTicketDetailsFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final AirTicketDetailsFragment airTicketDetailsFragment = mFragment.get();
            if(airTicketDetailsFragment != null){
                switch (msg.what) {
                    case 0:
                        airTicketDetailsFragment.loadDepartureWeather((WeatherSchedule) msg.obj);
                        break;
                    case 1:
                        airTicketDetailsFragment.loadDestinationWeather((WeatherSchedule) msg.obj);
                        break;
                    default:
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }


    private void loadDepartureWeather(WeatherSchedule startWeather) {
        if (startWeather == null) {
            return;
        }
        if (!WeatherIconUtil.isEmpty(startWeather.getAddress())) {
            mStartCity.setText(startWeather.getAddress());
        } else {
            mStartCity.setText(getResources().getString(R.string.null_value));
        }
        if (!WeatherIconUtil.isEmpty(startWeather.getTemp())) {
            mStartCityTemp.setText(startWeather.getTemp() + "℃");
        } else {
            mStartCityTemp.setText(getResources().getString(R.string.null_value));
        }
        if (startWeather.getWeather() != null && !WeatherIconUtil.isEmpty(startWeather.getWeather().trim())) {
            WeatherIconUtil.setWeather(startWeather, mStartingWeather, mStartingWeatherTxt);
        } else {
            mStartingWeather.setImageResource(R.drawable.unknown);
            mStartingWeatherTxt.setText(R.string.null_value);
        }
    }


    private void loadDestinationWeather(WeatherSchedule endWeather) {
        if (endWeather == null) {
            return;
        }
        if (!WeatherIconUtil.isEmpty(endWeather.getAddress())) {
            mEndCity.setText(endWeather.getAddress());
        } else {
            mEndCity.setText(getResources().getString(R.string.null_value));
        }
        if (!WeatherIconUtil.isEmpty(endWeather.getTemp())) {
            mEndCityTemp.setText(endWeather.getTemp() + "℃");
        } else {
            mEndCityTemp.setText(getResources().getString(R.string.null_value));
        }
        if (endWeather.getWeather() != null && !WeatherIconUtil.isEmpty(endWeather.getWeather().trim())) {
            WeatherIconUtil.setWeather(endWeather, mEndingWeather, mEndingWeatherTxt);
        } else {
            mEndingWeather.setImageResource(R.drawable.unknown);
            mEndingWeatherTxt.setText(R.string.null_value);
        }
    }

    private void enterWeatherApp() {
        PackageManager packageManager = getActivity().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(Constants.PACKAGE_WEATHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivity(intent);
    }

    //add by zhengjl for GNSPR #69233 end
    private void setClickLink(final ActionBase actionBase) {
        mServiceTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i(TAG, "AirTicket...setClickLink....click...action:" + actionBase.action);
                if(schedule == null)
                    return;
                if (schedule.getSmsSender() == null) {
                    return;
                }
                Uri callUri = Uri.fromParts("tel", schedule.getServiceNum(), null);
                final Intent intent = new Intent("android.intent.action.CALL", callUri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                mContext.startActivity(intent);
                //actionBase.doAction(mContext);
            }
        });
    }
}
