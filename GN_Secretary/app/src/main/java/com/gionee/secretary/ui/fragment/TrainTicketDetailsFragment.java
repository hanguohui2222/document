package com.gionee.secretary.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;
import com.gionee.secretary.utils.TravelModeUtil;
import com.gionee.secretary.utils.WeatherIconUtil;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.gionee.secretary.ui.viewInterface.ISetWeatherListener;
import com.ted.android.core.SmsEntityLoader;
import com.ted.android.data.ClickType;
import com.ted.android.data.SmsEntity;
import com.ted.android.data.bubbleAction.ActionBase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import amigoui.widget.AmigoProgressBar;
import amigoui.widget.AmigoTextView;

public class TrainTicketDetailsFragment extends Fragment implements ISetWeatherListener {
    private static final String TAG = "TrainTicketDetails";
    AmigoTextView mTitle;
    AmigoTextView mStartStation;
    AmigoTextView mTrainNo;
    AmigoTextView mStartTime;
    AmigoTextView mArriveTime;
    AmigoTextView mPassengerName;
    AmigoTextView mSeatNo;
    AmigoTextView mServiceTel;
    AmigoTextView mOrderNo;
    AmigoTextView mDestination;
    AmigoTextView mPrice;
    AmigoTextView mNavigStartStation;
    AmigoTextView mTimeTable;
    FrameLayout mFLTimeTable;
    TrainSchedule schedule;
    Context mContext;
    WebView mWebView;
    FrameLayout mTrainTicketLayout;
    FrameLayout mWebViewLayout;
    AmigoProgressBar mProgressBar;
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

    public TrainTicketDetailsFragment() {
        // Required empty public constructor
    }

    public static TrainTicketDetailsFragment newInstance() {
        TrainTicketDetailsFragment fragment = new TrainTicketDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (TrainSchedule) activity.getSchedule();
        setHasOptionsMenu(true);
        setWeatherInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_train_ticket_details, container, false);
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
        String departure = schedule.getDeparture();
        String destination = schedule.getDestination();
        if (departure != null && !"null".equalsIgnoreCase(departure)) {
            if (!departure.contains("站")) {
                departure = departure + "站";
            }
            NavigateUtil.getAddressCity(mContext, departure, this, true);
        }
        if (destination != null && !"null".equalsIgnoreCase(destination)) {
            if (!destination.contains("站")) {
                destination = destination + "站";
            }
            NavigateUtil.getAddressCity(mContext, destination, this, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(schedule != null){
            CardDetailsUtils.setTextViewWithPreSuffix(getActivity(), mNavigStartStation, "导航到", schedule.getDeparture(), "站");
            initNavigate();
        }
    }

    private void initView(View root) {
        mTitle = (AmigoTextView) root.findViewById(R.id.title);
        mStartStation = (AmigoTextView) root.findViewById(R.id.start_station);
        mTrainNo = (AmigoTextView) root.findViewById(R.id.train_number);
        mStartTime = (AmigoTextView) root.findViewById(R.id.time_start);
        mArriveTime = (AmigoTextView) root.findViewById(R.id.time_arrive);
        mSeatNo = (AmigoTextView) root.findViewById(R.id.seat_no);
        mServiceTel = (AmigoTextView) root.findViewById(R.id.service_tel);
        mOrderNo = (AmigoTextView) root.findViewById(R.id.order_no);
        mDestination = (AmigoTextView) root.findViewById(R.id.end_station);
        mPrice = (AmigoTextView) root.findViewById(R.id.price);
        mNavigStartStation = (AmigoTextView) root.findViewById(R.id.navig_start_station);
        mTimeTable = (AmigoTextView) root.findViewById(R.id.time_table);
        mPassengerName = (AmigoTextView) root.findViewById(R.id.passenger);
        //mFLTimeTable = (FrameLayout) root.findViewById(R.id.fl_time_table);
        //mFLTimeTable.setVisibility(View.GONE);
        mTrainTicketLayout = (FrameLayout) root.findViewById(R.id.train_details_layout);
        mWebViewLayout = (FrameLayout) root.findViewById(R.id.web_view_layout);
        mWebView = (WebView) root.findViewById(R.id.web_view);
        mProgressBar = (AmigoProgressBar) root.findViewById(R.id.progress_bar);

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
//        CardDetailsUtils.setTextView(mTitle, "火车票");
        CardDetailsUtils.setTextView(mStartStation, schedule.getDeparture());
        CardDetailsUtils.setTextView(mTrainNo, schedule.getTrainnumber());
        LogUtils.i(TAG, "trainTickDetailsFragement.....schedule.getDestination():" + schedule.getDestination() + " ,arriveTime:" + schedule.getArrivaltime());
        if ("".equals(schedule.getDestination()) || "null".equals(schedule.getDestination())) {
            LogUtils.i(TAG, "trainTickDetailsFragement.....1");
            CardDetailsUtils.setTextView(mDestination, " -- ");
        } else {
            LogUtils.i(TAG, "trainTickDetailsFragement.....2");
            CardDetailsUtils.setTextView(mDestination, schedule.getDestination());
        }

        CardDetailsUtils.setTextView(mStartTime, schedule.getStarttime());
        CardDetailsUtils.setTextView(mArriveTime, schedule.getArrivaltime());
        CardDetailsUtils.setTextView(mSeatNo, "座席", schedule.getSeatnumber());
        CardDetailsUtils.setTextView(mOrderNo, "订单号", schedule.getOrdernumber());
        CardDetailsUtils.setTextView(mPassengerName, "乘客", schedule.getOrderperson());
        CardDetailsUtils.setTextViewWithPreSuffix(getActivity(), mNavigStartStation, "导航到", schedule.getDeparture(), "站");
        CardDetailsUtils.setTextViewWithTimeTable(mTimeTable, schedule.getTrainnumber(), "时刻表");
        CardDetailsUtils.setShowStatus(mServiceTel, null);
        CardDetailsUtils.setShowStatus(mPrice, null);
    }

    private void initNavigate() {
        if (mNavigStartStation.getVisibility() == View.VISIBLE) {
            mNavigStartStation.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(schedule == null)
                        return;
                    int mTravelType = Integer.parseInt(SettingModel.getInstance(mContext).getDefaultTravelMethod());
                    NavigateUtil.navigateToDes(getActivity(), schedule.getDeparture(), TravelModeUtil.getTravelType(mTravelType));
                }
            });
            if (!TravelModeUtil.isMapSupportMode(getActivity())) {
                mNavigStartStation.setVisibility(View.GONE);
            }
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
                                LogUtils.i(TAG, "getEntryActions....!entity.hasBusinessADAction.......actionBase:" + ab.toString());
                                if (ab.action == ClickType.CLICKTYPE_GOTOLINK && ab.buttonText.contains("时刻表")) {
                                    setClickLink(ab);
                                }

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

    private void setClickLink(final ActionBase actionBase) {
        //mFLTimeTable.setVisibility(View.VISIBLE);
        mTimeTable.setVisibility(View.VISIBLE);
        mTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i(TAG, "train...setClickLink....click...action:" + actionBase.buttonText + actionBase.action);
//                Intent intent = new Intent();
//                intent.setClassName("com.android.browser", "com.android.browser.GNBrowserActivity");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//                String urlString = actionBase.url;
//                if (getActivity() != null && urlString != null) {
//                    Uri url = Uri.parse(actionBase.url);
//                    intent.setData(url);
//                    getActivity().startActivity(intent);
//                }
                loadTrainTimeTable(actionBase);
            }
        });
    }

    private void loadTrainTimeTable(ActionBase actionBase) {
        mTrainTicketLayout.setVisibility(View.GONE);
        mWebViewLayout.setVisibility(View.VISIBLE);
        setHasOptionsMenu(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(actionBase.url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            //网页加载开始时调用，显示加载提示旋转进度条
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(android.view.View.VISIBLE);
            }

            //网页加载完成时调用，隐藏加载提示旋转进度条
            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }

            //网页加载失败时调用，隐藏加载提示旋转进度条
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
                mProgressBar.setVisibility(android.view.View.GONE);
            }
        });
    }

    /**
     * Hide Train's Timetable  隐藏时刻表
     */
    public void hideTrainTimeTable() {
        mWebViewLayout.setVisibility(View.GONE);
        mTrainTicketLayout.setVisibility(View.VISIBLE);
        setHasOptionsMenu(true);
    }

    /**
     * Is Timetable displayed  时刻表是否已显示
     *
     * @return 时刻表显示状态
     */
    public boolean isTrainTimeTableLoad() {
        switch (mWebViewLayout.getVisibility()) {
            case View.VISIBLE: {
                return true;
            }
            case View.GONE: {
                return false;
            }
        }
        return false;
    }

    public WebView getmWebView() {
        return mWebView;
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
        private final WeakReference<TrainTicketDetailsFragment> mFragment;
        public MyHandler(TrainTicketDetailsFragment fragment){
            mFragment = new WeakReference<TrainTicketDetailsFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            TrainTicketDetailsFragment trainTicketDetailsFragment = mFragment.get();
            if(trainTicketDetailsFragment != null){
                switch (msg.what) {
                    case 0:
                        trainTicketDetailsFragment.loadDepartureWeather((WeatherSchedule) msg.obj);
                        break;
                    case 1:
                        trainTicketDetailsFragment.loadDestinationWeather((WeatherSchedule) msg.obj);
                        break;
                    default:
                        break;
                }
            }
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
}
