package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;

import com.gionee.secretary.R;
import com.gionee.secretary.adapter.MonthViewAdapter;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.ui.activity.CalendarActivity;
import com.gionee.secretary.widget.CanotSlidingViewpager;

/**
 * Created by liyu on 16/5/12.
 */
public class MonthFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private CanotSlidingViewpager mViewPager;
    //    private RecyclerView mRv;
//    private View line;
    private CalendarManager mCalendarManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, null);
        mCalendarManager = ((CalendarActivity)getActivity()).mCalendarManager;
        mViewPager = (CanotSlidingViewpager) view.findViewById(R.id.viewpager);
//        mRv = (RecyclerView)view.findViewById(R.id.rv);
//        line = (View)view.findViewById(R.id.line);
        mViewPager.setAdapter(new ViewPagerAdapter(getActivity().getFragmentManager()));
        mViewPager.setCurrentItem(mCalendarManager.INDEX_INIT);
        mViewPager.setOnPageChangeListener(this);
        mCalendarManager.initMonthInfoByMonth(mCalendarManager.INDEX_INIT);
        checkViewPagerScrollble();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
//        mRv.setLayoutManager(mLinearLayoutManager);

        return view;
    }

    boolean scrollBySelf = true;
    private int curPosition = mCalendarManager.INDEX_INIT;

    @Override
    public void onPageSelected(final int position) {
        if (scrollBySelf) {
//    		mCalendarManager.setCurrMonthPosition(position);
            mCalendarManager.upDateMonthInfoByMonth(position);
            if (position > curPosition) {
                mCalendarManager.nextMonth();
            } else {
                mCalendarManager.preMonth();
            }
            mCalendarManager.updateWeekViewByOther();
        } else {
            scrollBySelf = true;
        }
        mCalendarManager.postDelayed(new Runnable() {
            @Override
            public void run() {
                MonthViewAdapter adapter = mCalendarManager.getMonthViewAdapter(position);
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        });
        curPosition = position;
        checkViewPagerScrollble();
    }

    public int getCurPosition() {
        return curPosition;
    }

    private void checkViewPagerScrollble() {
        switch (mCalendarManager.getmMonthScrollType()) {
            case CalendarManager.SCROLL_LEFT_DISAGBLE:
                mViewPager.setLeftScrollble(false);
                break;
            case CalendarManager.SCROLL_RIGHT_DISAGBLE:
                mViewPager.setRightScrollble(false);
                break;
            case CalendarManager.SCROLL_FREE:
                mViewPager.setScrollble(true);
                break;

        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mViewPager = null;
//		mRv = null;
    }

    public void toToday() {
        final int index = mCalendarManager.getTodayIndexByMonth();
        if (curPosition == index) {
            mCalendarManager.getMonthViewAdapter(index).notifyDataSetChanged();
        } else {
            scrollBySelf = false;
            mViewPager.setCurrentItem(index);
        }

    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private FragmentManager fm;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        private String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
        }

        @Override
        public Fragment getItem(int position) {
            return mCalendarManager.getMonthPageFragment(position);
        }

        @Override
        public int getCount() {
            return mCalendarManager.INDEX_TOTLE;
        }

    }

    public void updatePage(final int index) {
        if (mViewPager.getCurrentItem() != index) {
            scrollBySelf = false;
            mCalendarManager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(index);
                }

            });
        } else {
            mCalendarManager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MonthViewAdapter adapter = mCalendarManager.getMonthViewAdapter(index);
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                }

            });
        }
    }


    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

}
