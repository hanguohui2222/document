package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.TravelModeUtil;
import com.gionee.secretary.ui.activity.CardDetailsActivity;

import amigoui.widget.AmigoTextView;

import android.widget.FrameLayout;

/**
 * Created by liu on 5/17/16.
 */
public class MovieTicketDetailsFragment extends Fragment {
    private static final String TAG = "MovieTicketDetailsFragment";
    AmigoTextView mTitle;
    AmigoTextView mMovieName;
    AmigoTextView mDate;
    AmigoTextView mTimeStart;
    AmigoTextView mCinema;
    AmigoTextView mSeatNo;
    AmigoTextView mTicketCertificate;
    AmigoTextView mNavigCinema;
    FrameLayout mNavigCard;
    MovieSchedule schedule;
    Context mContext;

    public MovieTicketDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieTicketDetailsFragment newInstance() {
        MovieTicketDetailsFragment fragment = new MovieTicketDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (MovieSchedule) activity.getSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movie_ticket_details, container, false);
        initView(root);
        updateScheduleInfo();
        initNavigate();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(schedule != null){
            CardDetailsUtils.setTextViewWithoutColon(getActivity(), mNavigCinema, "导航到", schedule.getCinemaName());
            initNavigate();
        }
    }

    private void initView(View root) {
        mTitle = (AmigoTextView) root.findViewById(R.id.title);
        mMovieName = (AmigoTextView) root.findViewById(R.id.movie_name);
//        mDate = (AmigoTextView)root.findViewById(R.id.tv_date);
        mTimeStart = (AmigoTextView) root.findViewById(R.id.tv_time);
        mCinema = (AmigoTextView) root.findViewById(R.id.cinema);
        mSeatNo = (AmigoTextView) root.findViewById(R.id.seat_no);
        mTicketCertificate = (AmigoTextView) root.findViewById(R.id.ticket_certificate);
        mNavigCinema = (AmigoTextView) root.findViewById(R.id.navig_cinema);
//        mNavigCard = (FrameLayout)root.findViewById(R.id.navig_card);
    }

    private void updateScheduleInfo() {
        if (schedule == null)
            return;
        LogUtils.d(TAG, "Movie Ticket deatails....updateScheduleInfo......schedule =" + schedule);
        if (!CardDetailsUtils.isEmpty(schedule.getSource())) {
            mTitle.setText(schedule.getSource() + mContext.getResources().getString(R.string.movie_order));
        }
        CardDetailsUtils.setTextView(mMovieName, schedule.getMovieName());
//        CardDetailsUtils.setTextView(mDate, DateUtils.getDate(schedule.getDate()));
        if (schedule.getPlayTime() != null && !"null".equals(schedule.getPlayTime())) {
            CardDetailsUtils.setTextView(mTimeStart, schedule.getPlayTime());
        } else {
            CardDetailsUtils.setTextView(mTimeStart, DateUtils.getDate(schedule.getDate()) + DateUtils.time2String(schedule.getDate()));
        }
        CardDetailsUtils.setTextView(mCinema, schedule.getCinemaName());
        CardDetailsUtils.setTextView(mSeatNo, schedule.getSeatDesc());
        SpannableStringBuilder certificateString = TextUtilTools.hightLightText("取票凭证:" + schedule.getTicketCertificate(), schedule.getTicketCertificate(), mContext.getResources().getColor(R.color.movie_card_detail_ticket_certificate_highlight_color));
        CardDetailsUtils.setTextView(mTicketCertificate, certificateString, schedule.getTicketCertificate());
        CardDetailsUtils.setTextViewWithoutColon(getActivity(), mNavigCinema, "导航到", schedule.getCinemaName());
        CardDetailsUtils.setShowStatus(mCinema, schedule.getCinemaName());
        CardDetailsUtils.setShowStatus(mSeatNo, schedule.getSeatDesc());
        CardDetailsUtils.setShowStatus(mTicketCertificate, schedule.getTicketCertificate());
    }

    private void initNavigate() {
//        if(mNavigCinema.getVisibility() == View.VISIBLE){
//
//            mNavigCinema.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    NavigateUtil.navigateToDes(getActivity(), schedule.getCinemaName());
//                    LogUtils.i(TAG, "TrainTick....navigstartstation......................");
//                }
//            });
//        }else{
//            mNavigCinema.setVisibility(View.GONE);
////            mNavigCard.setVisibility(View.GONE);
//        }
        if (!TravelModeUtil.isMapSupportMode(getActivity())) {
//            mNavigCard.setVisibility(View.GONE);
            mNavigCinema.setVisibility(View.GONE);
        } else {
            mNavigCinema.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(schedule == null)
                        return;
                    int mTravelType = Integer.parseInt(SettingModel.getInstance(mContext).getDefaultTravelMethod());
                    NavigateUtil.navigateToDes(getActivity(), schedule.getCinemaName(), TravelModeUtil.getTravelType(mTravelType));
                    LogUtils.i(TAG, "TrainTick....navigstartstation......................");
                }
            });
        }
    }
}
