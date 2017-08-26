package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ScrollView;

import com.gionee.secretary.R;
import com.gionee.secretary.adapter.MonthViewAdapter;
import com.gionee.secretary.adapter.RouteResultAdapter;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.ui.activity.CalendarActivity;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by liyu on 16/5/11.
 */
public class MonthPageFragment extends Fragment{
    private GridView mGridView;
    private MonthViewAdapter mMonthViewAdapter;
    private ScheduleInfoDao scheduleInfoDao;
    private int position;
    public static final String ARG_PAGE = "page";
    public static final String ARG_CALENDAR = "calendar";
    private Calendar targetCalendar;
    private CalendarManager mCalendarManager;
    private MyItemClickListener mListener;

    public static MonthPageFragment create(int position) {
        MonthPageFragment fragment = new MonthPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month_page, null);
        mGridView = (GridView) view.findViewById(R.id.gv);
        position = getArguments().getInt(ARG_PAGE);
        mCalendarManager = ((CalendarActivity)getActivity()).mCalendarManager;
        targetCalendar = mCalendarManager.getTargetMonthPageCalendar(position);
        mMonthViewAdapter = new MonthViewAdapter(getActivity(), targetCalendar , mCalendarManager);
        mGridView.setAdapter(mMonthViewAdapter);
        mListener = new MyItemClickListener(this);
        mGridView.setOnItemClickListener(mListener);
        mCalendarManager.putMonthAdapter(position, mMonthViewAdapter);
        ScrollView sc;
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGridView.setOnItemClickListener(null);
        mListener = null;
        mGridView = null;
        mCalendarManager.removeMonthAdapter(position);
    }

    private static class MyItemClickListener implements AdapterView.OnItemClickListener{
        private final WeakReference<MonthPageFragment> mFragment;
        public MyItemClickListener(MonthPageFragment fragment){
            mFragment = new WeakReference<MonthPageFragment>(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Calendar calendar = mFragment.get().mMonthViewAdapter.getItem(position);
            // 这一段不懂 为什么要绕一圈  不然就bug
            Calendar cal = Calendar.getInstance();
            cal.setTime(calendar.getTime());
            //------------------------------------------------
            mFragment.get().mCalendarManager.setCalendar(cal);
            mFragment.get().mCalendarManager.updateWeekView();
            mFragment.get(). mMonthViewAdapter.notifyDataSetChangedWithAnimation();
        }
    }
    
}

