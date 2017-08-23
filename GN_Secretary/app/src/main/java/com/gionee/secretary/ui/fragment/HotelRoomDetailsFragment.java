package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.TravelModeUtil;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.ted.android.core.SmsEntityLoader;
import com.ted.android.data.ClickType;
import com.ted.android.data.SmsEntity;
import com.ted.android.data.bubbleAction.ActionBase;
import com.gionee.secretary.utils.NavigateUtil;

import java.util.ArrayList;
import java.util.List;

import amigoui.widget.AmigoTextView;

import android.telecom.PhoneAccount;

/**
 * Created by liu on 5/17/16.
 */
public class HotelRoomDetailsFragment extends Fragment {
    private static final String TAG = "HotelRoomDetailsFragment";
    AmigoTextView mTitle;
    AmigoTextView mHotelName;
    AmigoTextView mTimeCheckin;
    AmigoTextView mTimeCheckout;
    AmigoTextView mRoomType;
    AmigoTextView room_username;
    AmigoTextView mRoomCount;
    AmigoTextView mServiceTel;
    AmigoTextView mHotelAddress;
    AmigoTextView mNavigHotel;
    HotelSchedule schedule;
    Context mContext;

    public HotelRoomDetailsFragment() {
        // Required empty public constructor
    }

    public static HotelRoomDetailsFragment newInstance() {
        HotelRoomDetailsFragment fragment = new HotelRoomDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (HotelSchedule) activity.getSchedule();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(schedule != null){
            CardDetailsUtils.setTextViewWithoutColon(getActivity(), mNavigHotel, "导航到", schedule.getHotelName());
            initNavigate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_hotel_room_details, container, false);
        initView(root);
        getEntryActions();
        updateScheduleInfo();
        initNavigate();
        return root;
    }

    private void initView(View root) {
        mTitle = (AmigoTextView) root.findViewById(R.id.title);
        mHotelName = (AmigoTextView) root.findViewById(R.id.hotel_name);
        mTimeCheckin = (AmigoTextView) root.findViewById(R.id.time_checkin);
        mTimeCheckout = (AmigoTextView) root.findViewById(R.id.time_checkout);
        mRoomType = (AmigoTextView) root.findViewById(R.id.room_type);
        room_username = (AmigoTextView) root.findViewById(R.id.room_username);
        mRoomCount = (AmigoTextView) root.findViewById(R.id.room_count);
        mServiceTel = (AmigoTextView) root.findViewById(R.id.service_tel);
        mHotelAddress = (AmigoTextView) root.findViewById(R.id.hotel_address);
        mNavigHotel = (AmigoTextView) root.findViewById(R.id.navig_hotel);

    }

    private void updateScheduleInfo() {
        if(schedule != null) {
            CardDetailsUtils.setTextView(mTitle, "酒店订单");
            CardDetailsUtils.setTextView(mHotelName, schedule.getHotelName());
            CardDetailsUtils.setTextView(mTimeCheckin, "入住时间", DateUtils.getDate(schedule.getDate()));
            //Gionee <gn_by><zhengyt><2017-4-2> add for GNSPR76955 Begin
            CardDetailsUtils.setTextView(mTimeCheckout, "退房时间", schedule.getCheckOutDate());
            //CardDetailsUtils.setTextView(mTimeCheckout, "退房时间", DateUtils.getDate(DateUtils.str2Date2(schedule.getCheckOutDate())));
            //Gionee <gn_by><zhengyt><2017-4-2> add for GNSPR76955 End
            CardDetailsUtils.setTextView(mRoomType, "房型", schedule.getRoomStyle());
            CardDetailsUtils.setTextView(room_username, "入住人", schedule.getCheckInPeople());
            CardDetailsUtils.setTextView(mRoomCount, "间数", schedule.getRoomCounts());
            SpannableStringBuilder telString = TextUtilTools.hightLightText("客服电话：" + schedule.getServiceNum(), schedule.getServiceNum(), mContext.getResources().getColor(R.color.hotel_card_detail_service_tel_highlight_color));
            CardDetailsUtils.setTextView(mServiceTel, telString, schedule.getServiceNum());
            CardDetailsUtils.setTextViewWithoutColon(getActivity(), mHotelAddress, "", schedule.getHotelAddress());
            CardDetailsUtils.setTextViewWithoutColon(getActivity(), mNavigHotel, "导航到", schedule.getHotelName());
        }

    }

    private void initNavigate() {
        if (mNavigHotel.getVisibility() == View.VISIBLE) {

            mNavigHotel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(schedule == null)
                        return;
                    int mTravelType = Integer.parseInt(SettingModel.getInstance(mContext).getDefaultTravelMethod());
                    NavigateUtil.navigateToDes(getActivity(), schedule.getHotelName(), TravelModeUtil.getTravelType(mTravelType));
                    LogUtils.i(TAG, "TrainTick....navigstartstation......................");
                }
            });
            if (!TravelModeUtil.isMapSupportMode(getActivity())) {
                mNavigHotel.setVisibility(View.GONE);
            }

        }
    }

    private void getEntryActions() {
        if(schedule == null)
            return;
        LogUtils.i(TAG, "getEntryActions...");
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

    private void setClickLink(final ActionBase actionBase) {
        mServiceTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(schedule == null)
                    return;
                LogUtils.i(TAG, "HoteRom...setClickLink....click...action:" + actionBase.action);
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
