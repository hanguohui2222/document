package com.gionee.secretary.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.calendar.LunarUtil;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;
import com.gionee.secretary.utils.TravelModeUtil;

import amigoui.widget.AmigoSwitch;
import amigoui.widget.AmigoTextView;

/**
 * Created by hangh on 5/11/16.
 */
public class SelfCreateScheduleDetailFragment extends Fragment {
    private static final String TAG = "SelfScheduleDetail";
    AmigoTextView mTitle;
    AmigoTextView mRemind;
    AmigoTextView mRemindPeriod;
    LinearLayout mAddressLayout;
    AmigoTextView mAddress;
    ImageView mNavigDivider;
    LinearLayout mTravelModeLayout;
    AmigoTextView mTripMode;
    AmigoTextView mDescription;
    AmigoTextView mStartDate;
    AmigoTextView mStartTime;
    AmigoTextView mEndDate;
    AmigoTextView mEndTime;
    AmigoSwitch mWholeDay;
    AmigoTextView mNavigSelf;
    AmigoTextView mStartDateLunar;
    AmigoTextView mEndDateLunar;
    SelfCreateSchedule schedule;
    int scheduleId = -1;
    Context mContext;
    private int mEventId;
    private ScrollView mScrollView;

    public SelfCreateScheduleDetailFragment() {
        // Required empty public constructor
    }

    public ScrollView getmScrollView() {
        return mScrollView;
    }

    public static SelfCreateScheduleDetailFragment newInstance() {
        SelfCreateScheduleDetailFragment fragment = new SelfCreateScheduleDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SelfCreateScheduleDetailFragment newInstanceForUpdate(BaseSchedule schedule) {
        SelfCreateScheduleDetailFragment fragment = new SelfCreateScheduleDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("ss", schedule);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mContext = getActivity();
        //此处涉及语音打开详情，编辑后打开详情
        schedule = (SelfCreateSchedule) activity.getIntent().getSerializableExtra("schedule");

        if (schedule == null) {
            mEventId = activity.getIntent().getIntExtra("eventid", 0);
            schedule = (SelfCreateSchedule) ScheduleInfoDao.getInstance(activity).getScheduleInfoById(mEventId);
        } else {
            scheduleId = schedule.getId();
            schedule = (SelfCreateSchedule) ScheduleInfoDao.getInstance(activity).getScheduleInfoById(scheduleId);
            // Gionee sunyang 2016-01-14 modify for GNSPR #60653 begin
//            if (schedule == null) {
//                activity.finish();
//            }
            // Gionee sunyang 2016-01-14 modify for GNSPR #60653 end
        }
        SelfCreateSchedule ss = (SelfCreateSchedule) getArguments().getSerializable("ss");
        if (ss != null) {
            schedule = ss;
        }
        /*if (schedule == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mEventId = sharedPreferences.getInt(Constants.LAST_EVENT_ID_KEY, 0);
            schedule = (SelfCreateSchedule) ScheduleInfoDao.getInstance(activity).getScheduleInfoById(mEventId);
        }*/

        //是否是从语音进入
        boolean isVoice = activity.getIntent().getBooleanExtra("voice_flag",false);
        if(isVoice){
            mEventId = activity.getIntent().getIntExtra("eventid",0);
            schedule = (SelfCreateSchedule) ScheduleInfoDao.getInstance(activity).getScheduleInfoById(mEventId);
        }
        //是否是从智能助手进入
        boolean isTYAssistant = activity.getIntent().getBooleanExtra("assistant",false);
        if(isTYAssistant){
            mEventId = activity.getIntent().getIntExtra("scheduleId",0);
            schedule = (SelfCreateSchedule) ScheduleInfoDao.getInstance(activity).getScheduleInfoById(mEventId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_schedule_detail_main, container, false);
        initView(root);
        updateScheduleInfo();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(schedule != null){
            CardDetailsUtils.setTextViewWithoutColon(getActivity(), mNavigSelf, "导航到", schedule.getAddress());
        }
//        initNavigate();
    }

    private void initView(View root) {
        mTitle = (AmigoTextView) root.findViewById(R.id.title);
        mRemind = (AmigoTextView) root.findViewById(R.id.remind);
        mRemindPeriod = (AmigoTextView) root.findViewById(R.id.remindperiod);
        mAddressLayout = (LinearLayout) root.findViewById(R.id.address_layout);
        mAddress = (AmigoTextView) root.findViewById(R.id.address);
        mTravelModeLayout = (LinearLayout) root.findViewById(R.id.travel_mode_layout);
        mTripMode = (AmigoTextView) root.findViewById(R.id.tripmode);
        mDescription = (AmigoTextView) root.findViewById(R.id.description);
        mStartDate = (AmigoTextView) root.findViewById(R.id.detail_startdate);
        mStartTime = (AmigoTextView) root.findViewById(R.id.detail_starttime);
        mEndDate = (AmigoTextView) root.findViewById(R.id.detail_enddate);
        mEndTime = (AmigoTextView) root.findViewById(R.id.detail_endtime);
        mWholeDay = (AmigoSwitch) root.findViewById(R.id.wholeday);
        mNavigSelf = (AmigoTextView) root.findViewById(R.id.navig_self);
        mNavigDivider = (ImageView) root.findViewById(R.id.divider_img);
        mScrollView = (ScrollView) root.findViewById(R.id.scrollView);
    }

    private void updateScheduleInfo() {
        if (schedule == null) {
            return;
        }
        mTitle.setText(schedule.getTitle());
        mRemind.setText(schedule.getRemindType());
        mRemindPeriod.setText(schedule.getRemindPeriod());
        boolean isAllDay = schedule.getIsAllDay();
        // Gionee sunyang modify for GNSPR #65721 at 2017-01-20 begin
//        mWholeDay.setChecked(isAllDay);
        mWholeDay.setVisibility(isAllDay ? View.VISIBLE : View.GONE);
        // Gionee sunyang modify for GNSPR #65721 at 2017-01-20 end
        LogUtils.i(TAG, "selfDetail....schedule.getAddress():" + schedule.getAddress());
        if (schedule.getAddress() != null && TextUtils.isEmpty(schedule.getAddress().trim())) {
            mAddressLayout.setVisibility(View.GONE);
            mTravelModeLayout.setVisibility(View.GONE);
            mNavigDivider.setVisibility(View.INVISIBLE);
        } else {
            //用于判断是否显示"导航到"按钮。  Fix bug GNSPR #69015 by liyh
            boolean showNaviAction = TravelModeUtil.isMapSupportMode(getActivity(), schedule.getTripMode());
            mAddressLayout.setVisibility(View.VISIBLE);
            mTravelModeLayout.setVisibility(View.VISIBLE);
            mNavigSelf.setVisibility(showNaviAction ? View.VISIBLE : View.GONE);
            mNavigDivider.setVisibility(showNaviAction ? View.VISIBLE : View.INVISIBLE);
            mAddress.setText(schedule.getAddress());
            mTripMode.setText(schedule.getTripMode());
            mNavigSelf.setText("导航到" + schedule.getAddress());
            initNavigate();
        }
        if (schedule.getDescription() == null || (schedule.getDescription() != null && TextUtils.isEmpty(schedule.getDescription().trim()))) {
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText("描述:" + schedule.getDescription());
        }
        mStartDate.setText(DateUtils.date2String2(schedule.getDate()));
        if (isAllDay) {
            mStartTime.setText(DateUtils.date2String2(schedule.getDate()));
        } else {
            mStartTime.setText(DateUtils.date2String2(schedule.getDate()) + " " + DateUtils.time2String(schedule.getDate()));
        }
        long millis = schedule.getDate().getTime();
        Time time = new Time();
        time.set(millis);
        time.normalize(true);
        String lunarDate = LunarUtil.getLunarDateWithOutYear(mContext, time.year,
                time.month, time.monthDay, true);
        if (schedule.getEndtime() != null) {
            mEndDate.setText(DateUtils.date2String2(schedule.getEndtime()));
            if (isAllDay) {
                mEndTime.setText(DateUtils.date2String2(schedule.getEndtime()));
            } else {
                mEndTime.setText(DateUtils.date2String2(schedule.getEndtime()) + " " + DateUtils.time2String(schedule.getEndtime()));
            }
            long endmillis = schedule.getEndtime().getTime();
            Time time2 = new Time();
            time2.set(endmillis);
            time2.normalize(true);
            String endLunarDate = LunarUtil.getLunarDateWithOutYear(mContext, time2.year,
                    time2.month, time2.monthDay, true);
        }
    }


    private void initNavigate() {
        if (true) {
            mAddressLayout.setVisibility(View.VISIBLE);
            mNavigSelf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int traveltype = SettingModel.getInstance(getActivity()).getSelfScheduleTravel();
                    if(schedule == null)
                        return;
                    NavigateUtil.navigateToDes(getActivity(), schedule.getAddress(), schedule.getTripMode());
                    LogUtils.i(TAG, "SelfCreateScheduleDetailFragement....navigstartstation......................");
                }
            });
        } else {
            mAddressLayout.setVisibility(View.GONE);
        }
        if (schedule != null && !TravelModeUtil.isMapSupportMode(getActivity(), schedule.getTripMode())) {
//            mAddressLayout.setVisibility(View.GONE);
        }
    }

}
