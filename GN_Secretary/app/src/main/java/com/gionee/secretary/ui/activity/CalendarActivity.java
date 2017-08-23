package com.gionee.secretary.ui.activity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;

import com.gionee.secretary.R;
import com.gionee.secretary.adapter.CardAdapter;
import com.gionee.secretary.adapter.MonthViewAdapter;
import com.gionee.secretary.adapter.WeekViewAdapter;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.utils.DensityUtils;
import com.gionee.secretary.ui.viewInterface.ICalendarView;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.view.SplashActivity;
import com.gionee.secretary.widget.CalendarRecyclerView;
import com.gionee.secretary.widget.CanotSlidingViewpager;
import com.gionee.secretary.widget.ScrollLayout;
import com.gionee.secretary.widget.ScrollLayout.ScrollLayoutCallback;
import com.gionee.secretary.widget.WeekView;
import com.gionee.secretary.widget.WeekView.WeekViewCallback;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.youju.statistics.YouJuAgent;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.presenter.MainPresenter;
import com.gionee.secretary.presenter.SearchPresenter;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.WidgetUtils;
import com.gionee.secretary.widget.GNDateTimeDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoActionBar;
import amigoui.widget.AmigoTextView;

/**
 * Created by liyu on 16/5/10.
 */
public class CalendarActivity extends PasswordBaseActivity implements View.OnClickListener, ICalendarView, ViewPager.OnPageChangeListener, WeekViewCallback, ScrollLayoutCallback {
    private LinearLayout layout_setting;
    private FrameLayout fl_1;
    private FrameLayout fl_2;
    private AmigoActionBar mActionBar;
    private CalendarManager mCalendarManager;
    private CanotSlidingViewpager weekView;
    private ViewPagerAdapter mViewPagerAdapter;
    private ScrollLayout mScrollLayout;
    private AmigoTextView mScheduleTextView;
    private AmigoTextView mNoteTextView;
    private long exitTime = 0;
    private ImageView action_search;
    private ImageView action_setting;
    private TextView action_title;
    private ImageView image_new;
    private ImageView mTipsAddSchedule;
    //private ImageView mIvTips;
    private ImageView mSearch;
    private ImageView mSettings;
    //private ImageButton mTipsPoint;
    //private ImageView mGestureTips;
    private FrameLayout mNoteView;
    private FrameLayout mCalendarView;
    private LinearLayout mCalendarEmptyView;
    //private RelativeLayout mTipsGeneralLayer;
    //private RelativeLayout mTipsGestureLayer;
    private CalendarRecyclerView mRv;
    private int view_status;
    private static final int IN_CALENAR = 0;
    private static final int IN_NOTE = 1;
    private ImageView iv_today;
    private MainPresenter mMainPresenter;
    private SearchPresenter mSearchPresent;
    private int deleteSelected = 0;
    private LinearLayoutManager lm;
    private LinearLayout mButtomBar;
    private LinearLayout mNewLayout;

    private TextView tv_date;
    private TextView tv_lunar;


    private static final String TAG = "CalendarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.e(TAG, "calendar---oncreated");
        CalendarManager.initInstance(this);
        mCalendarManager = CalendarManager.getInstance();
        setContentView(R.layout.activity_calendar);
        initActionBar();
        mMainPresenter = new MainPresenter(this, this);
        mSearchPresent = new SearchPresenter(this, -1);
        initViews();
        initWeekView();
        initRv();
        registerRefreshReceiver();

        //Gionee zhengyt 2017-3-7 add for 70716 begin
        YouJuAgent.init(this);
        YouJuAgent.setReportUncaughtExceptions(true);
        YouJuAgent.setContinueSessionMillis(100);
        YouJuAgent.setLocationEnabled(true);
        //Gionee zhengyt 2017-3-7 add for 70716 end
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(DisplayUtils.isFullScreen()){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.calendar_main_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.calendar_schedule:
                showSchedule();
                break;
            case R.id.calendar_new_add:
                showNewAdd();
                break;
            case R.id.calendar_note:
                showNote();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        unRegisterRefreshUIReceiver();
        super.onDestroy();
    }

    private void unRegisterRefreshUIReceiver() {
        if (mRefreshUIReceiver != null) {
            unregisterReceiver(mRefreshUIReceiver);
        }
    }

    private void registerRefreshReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.REFRESH_FOR_MAIN_UI);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mRefreshUIReceiver, filter);
    }

    private BroadcastReceiver mRefreshUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSchedule(mCalendarManager.getCalendar());
        }
    };

    private int prePostion = mCalendarManager.INDEX_INIT;

    @Override
    public void onPageSelected(int position) {
//		ShowTipsUtil.setGestureTipsState(this, mGestureTips, ShowTipsUtil.GESTURE_TIPS_LANDSCAPE); // 水平滑动周视图，水平手势消失
        if (!updateWeekViewByMonthScrolled) {
            if (position > prePostion) {
                mCalendarManager.nextWeek();
            } else {
                mCalendarManager.preWeek();
            }
            final Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, (position - mCalendarManager.INDEX_INIT) * 7);

            mCalendarManager.updateMonthPage(c);
            mCalendarManager.upDateMonthInfoByWeek(position);
        }
        updateWeekViewByMonthScrolled = false;
        prePostion = position;

        checkViewPagerScrollble();
        if (mViewPagerAdapter.getAdapter(position) != null) {
            mViewPagerAdapter.getAdapter(position).notifyDataSetChanged();
        }
    }

    private void checkViewPagerScrollble() {
        switch (mCalendarManager.getmWeekScrollType()) {
            case CalendarManager.SCROLL_LEFT_DISAGBLE:
                weekView.setLeftScrollble(false);
                break;
            case CalendarManager.SCROLL_RIGHT_DISAGBLE:
                weekView.setRightScrollble(false);
                break;
            case CalendarManager.SCROLL_FREE:
                weekView.setScrollble(true);
                break;

        }
    }

    public class ViewPagerAdapter extends PagerAdapter {

        SparseArray<WeekView> weekViews = new SparseArray();

        @Override
        public int getCount() {
            return mCalendarManager.INDEX_TOTLE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(weekViews.get(position));
            weekViews.remove(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            WeekView view = weekViews.get(position);
            if (view == null) {
                view = (WeekView) View.inflate(CalendarActivity.this, R.layout.fragment_week_page, null);
                view.setPositionAndCalendar(position, mCalendarManager.initCalendar);
                view.setWeekViewCallback(CalendarActivity.this);
                weekViews.put(position, view);
            }
            LogUtils.d("instantiateItem", "weekViews.size() = " + weekViews.size());
            container.addView(view);
            return view;
        }

        public WeekViewAdapter getAdapter(int position) {
            WeekView view = weekViews.get(position);
            if (view != null) {
                return view.getAdapter();
            }
            return null;
        }

        public WeekView getItem(int position) {
            return weekViews.get(position);
        }
    }

    @Override
    public void setWeekViewVisibilty(int visibility) {
        weekView.setVisibility(visibility);
    }

    @Override
    public void dispatchTouch(MotionEvent ev) {
        mScrollLayout.dispatchTouchEvent(ev);
    }

    public void initWeekView() {
        mViewPagerAdapter = new ViewPagerAdapter();
        weekView.setAdapter(mViewPagerAdapter);
        weekView.setCurrentItem(mCalendarManager.INDEX_INIT);
        weekView.setOnPageChangeListener(this);
        checkViewPagerScrollble();
    }

    public void updateWeekView(int position) {
        if (position != prePostion) {
            updateWeekViewByMonthScrolled = true;
            weekView.setCurrentItem(position, false);
        } else {
            updateWeekViewByMonthScrolled = false;
            mViewPagerAdapter.getAdapter(position).notifyDataSetChanged();
        }
    }

	private void initActionBar() {
        mActionBar = getAmigoActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);

		View view = getLayoutInflater().inflate(R.layout.actionbar_layout, null);

		action_search = (ImageView) view.findViewById(R.id.action_search);
		action_setting = (ImageView) view.findViewById(R.id.action_setting);
		action_title = (TextView) view.findViewById(R.id.action_title);
		action_title.setText(R.string.secretary);
        if(DisplayUtils.isFullScreen()){
            action_search.setImageResource(R.drawable.search_full);
            action_setting.setImageResource(R.drawable.set_full);
        }else {
            action_search.setImageResource(R.drawable.search);
            action_setting.setImageResource(R.drawable.set);
        }

		action_search.setOnClickListener(this);
		action_setting.setOnClickListener(this);

		AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.WRAP_CONTENT, AmigoActionBar.LayoutParams.WRAP_CONTENT);
		mActionBar.setCustomView(view, param);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarManager.onBackPressed();
            }
        });
		mActionBar.show();

	}

    private void initViews() {
        weekView = (CanotSlidingViewpager) findViewById(R.id.week_view);
        mScrollLayout = (ScrollLayout) findViewById(R.id.scroll_layout);
        iv_today = (ImageView) findViewById(R.id.iv_today);
        mScrollLayout.setScrollLayoutCallback(this);
        mCalendarManager.showCalendarView();
        mScheduleTextView = (AmigoTextView) findViewById(R.id.schedule_txt);
        mNoteTextView = (AmigoTextView) findViewById(R.id.note_txt);
        mNoteView = (FrameLayout) findViewById(R.id.fl_content);
        mCalendarView = (FrameLayout) findViewById(R.id.calendar_view);
        mRv = (CalendarRecyclerView) findViewById(R.id.rv);
        mButtomBar = (LinearLayout) findViewById(R.id.buttom_bar);
        mNewLayout = (LinearLayout) findViewById(R.id.new_layout);
        mCalendarEmptyView = (LinearLayout) findViewById(R.id.schedule_emptyview);
        iv_today.setOnClickListener(this);
        // tv_date.setOnClickListener(this);
        mScheduleTextView.setOnClickListener(this);
        mNoteTextView.setOnClickListener(this);

        image_new = (ImageView) findViewById(R.id.new_button);
        image_new.setOnClickListener(this);
        mSearch = (ImageView) findViewById(R.id.search);
        mSettings = (ImageView) findViewById(R.id.setting);
        if(DisplayUtils.isFullScreen()){
            mSearch.setImageResource(R.drawable.search_full);
            mSettings.setImageResource(R.drawable.set_full);
            mButtomBar.setVisibility(View.GONE);
            mNewLayout.setVisibility(View.GONE);
        }else {
            mSearch.setImageResource(R.drawable.search);
            mSettings.setImageResource(R.drawable.set);
            mButtomBar.setVisibility(View.VISIBLE);
            mNewLayout.setVisibility(View.VISIBLE);
        }
        mSearch.setOnClickListener(this);
        mSettings.setOnClickListener(this);
        switchToSchedule();

//        mTipsPoint = (ImageButton) findViewById(R.id.tips_point);
//        mGestureTips = (ImageView) findViewById(R.id.gesture_tips);

        mCalendarManager.showNoteFragment();

		/*
		 * add by liyh at 2017-1-16 for implementing Tips function --begin--
		 */
        //mIvTips = (ImageView) findViewById(R.id.tips);
        //mTipsGeneralLayer = (RelativeLayout) findViewById(R.id.tips_general_layer);
        //mTipsGestureLayer = (RelativeLayout) findViewById(R.id.tips_gesture_layer);
        //ShowTipsUtil.showTips(this, mTipsGeneralLayer, mTipsGestureLayer, mTipsPoint, Constants.TIPS_FOR_HOME_PAGE);
		/*
		 * add by zjl at 2017-1-14
		 */
        mNoteView.setVisibility(View.GONE);

        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_lunar = (TextView) findViewById(R.id.tv_lunar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_today:
                mCalendarManager.toToday();
                break;
            case R.id.action_search:
                Intent searchIntent = new Intent();
                searchIntent.setClass(this, SearchActivity.class);
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(searchIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_setting:
                Intent settingIntent = new Intent();
                settingIntent.setClass(this, SettingActivity.class);
                settingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(settingIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.search:
                Intent search = new Intent();
                search.setClass(this, SearchActivity.class);
                search.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(search);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.setting:
                Intent setting = new Intent();
                setting.setClass(this, SettingActivity.class);
                setting.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(setting);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.new_button:
                showNewAdd();
                break;
            case R.id.schedule_txt:
                showSchedule();
                break;
            case R.id.note_txt:
                showNote();
                break;
        }
    }

    private void showNewAdd(){
        if (view_status == IN_CALENAR) {
            Intent newScheduleintent = new Intent();
            newScheduleintent.setClass(this, SelfCreateScheduleActivity.class);
            newScheduleintent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(newScheduleintent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            Intent newNoteintent = new Intent();
            newNoteintent.setClass(this, AddVoiceNoteActivity.class);
            newNoteintent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(newNoteintent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    private void showSchedule(){
        showCalendarView();
        //ShowTipsUtil.showLanscapeGesture(this, mGestureTips);
    }

    private void showNote(){
        showNoteView();
        /*if (mGestureTips.getVisibility() == View.VISIBLE) {
            mGestureTips.setVisibility(View.GONE);
        }*/
    }

    public void showSetTimeDialog() {
        DateTimeListener dateTimeListener = new DateTimeListener();
        GNDateTimeDialog alarmDialog = new GNDateTimeDialog(this, AmigoAlertDialog.THEME_AMIGO_LIGHT, dateTimeListener, Calendar.getInstance(), 0, false);
        alarmDialog.hideDayPicker();
        alarmDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        boolean isInLockTask = am.isInLockTaskMode();

        LogUtils.e(TAG, "-----onKeydown---- isInLockTask = " + isInLockTask);

        if (isInLockTask && keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, getResources().getString(R.string.lock_to_app_toast), Toast.LENGTH_LONG).show();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000 || isExsitActivity(SplashActivity.class)) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 判断某一个类是否存在任务栈里面
     *
     * @return
     */
    private boolean isExsitActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        ComponentName cmpName = intent.resolveActivity(getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break; // 跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    private void switchToSchedule() {
        ColorStateList schdule_csl = getResources().getColorStateList(R.color.tv_schdule_active_color);
        ColorStateList note_csl = getResources().getColorStateList(R.color.tv_note_gray_color);
        mScheduleTextView.setTextColor(schdule_csl);
        mNoteTextView.setTextColor(note_csl);
        Drawable topScheduleDrawable = getResources().getDrawable(R.drawable.schdule_active);
        topScheduleDrawable.setBounds(0, 0, topScheduleDrawable.getMinimumWidth(), topScheduleDrawable.getMinimumHeight());
        mScheduleTextView.setCompoundDrawables(null, topScheduleDrawable, null, null);

        Drawable topNoteDrawable = getResources().getDrawable(R.drawable.note_gray);
        topNoteDrawable.setBounds(0, 0, topNoteDrawable.getMinimumWidth(), topNoteDrawable.getMinimumHeight());
        mNoteTextView.setCompoundDrawables(null, topNoteDrawable, null, null);

        view_status = IN_CALENAR;
    }

    private void switchToNote() {
        ColorStateList schdule_csl = getResources().getColorStateList(R.color.tv_schdule_gray_color);
        ColorStateList note_csl = getResources().getColorStateList(R.color.tv_note_active_color);
        mScheduleTextView.setTextColor(schdule_csl);
        mNoteTextView.setTextColor(note_csl);
        Drawable topScheduleDrawable = getResources().getDrawable(R.drawable.schdule_gray);
        topScheduleDrawable.setBounds(0, 0, topScheduleDrawable.getMinimumWidth(), topScheduleDrawable.getMinimumHeight());
        mScheduleTextView.setCompoundDrawables(null, topScheduleDrawable, null, null);

        Drawable topNoteDrawable = getResources().getDrawable(R.drawable.note_active);
        topNoteDrawable.setBounds(0, 0, topNoteDrawable.getMinimumWidth(), topNoteDrawable.getMinimumHeight());
        mNoteTextView.setCompoundDrawables(null, topNoteDrawable, null, null);

        view_status = IN_NOTE;
    }

    private class DateTimeListener implements GNDateTimeDialog.OnDateTimeSetListener {

        @Override
        public void onDateTimeSet(Calendar calendar) {
            mCalendarManager.updateMonthPage(calendar);

        }
    }

    @Override
    public void upDateMonthInfo(String monthInfo) {
        // tv_date.setText(monthInfo);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void setWeekViewClickable(boolean b) {
        mViewPagerAdapter.getItem(prePostion).setItemCliclkable(b);
    }

    @Override
    public void onItemClick(int position) {
        MonthViewAdapter monthAdapter = mCalendarManager.getMonthViewAdapter(mCalendarManager.getAssignedIndexByMonth(mCalendarManager.getCalendar()));
        if (monthAdapter != null) {
            mViewPagerAdapter.getItem(position).getAdapter().notifyDataSetChangedWithAnimation();
            monthAdapter.notifyDataSetChanged();
        }
    }

    private boolean updateWeekViewByMonthScrolled;

    @Override
    public void updateWeekViewByOther(int position) {
        updateWeekViewByMonthScrolled = true;
        updateWeekView(position);
    }

    public void showNoteView() {
        mCalendarView.setVisibility(View.GONE);
        mNoteView.setVisibility(View.VISIBLE);
        switchToNote();
    }

    public void showCalendarView() {
        mNoteView.setVisibility(View.GONE);
        mCalendarView.setVisibility(View.VISIBLE);
        switchToSchedule();
    }

    private List<BaseSchedule> mSchedules;
    private List<ExpressSchedule> mExpressSchedules;
    // private CardAdapter adapter;
    CardAdapter cardAdapter;
    private LinearLayoutManager layoutManagerFixed;
    private MyHandle mHandler = new MyHandle(this);

    private static class MyHandle extends Handler {
        private final WeakReference<CalendarActivity> mActivity;

        public MyHandle(CalendarActivity activity) {
            mActivity = new WeakReference<CalendarActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final CalendarActivity calendarActivity = mActivity.get();
            if (calendarActivity == null) {
                return;
            }
            if (calendarActivity.mRv == null)
                return;

            if (!calendarActivity.showEmptyViewIfNeed()) {
                calendarActivity.cardAdapter = new CardAdapter(calendarActivity, calendarActivity.mRv, calendarActivity.mSchedules,
                        calendarActivity.mExpressSchedules, calendarActivity.mSearchPresent);
                calendarActivity.mRv.setAdapter(calendarActivity.cardAdapter);
                calendarActivity.cardAdapter.setClickItemListener(new CardAdapter.ClickItemListener() {
                    @Override
                    public void onClick(BaseSchedule event) {
                        calendarActivity.mCalendarManager.startScheduleDetailsActivity(calendarActivity, event);
                        calendarActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                });

                calendarActivity.cardAdapter.setLongClickItemListener(new CardAdapter.LongClickItemListener() {
                    @Override
                    public void onLongCLick(final BaseSchedule event) {
                        calendarActivity.deleteCard(event);
                    }
                });
            }
        }
    }

    public void deleteCard(BaseSchedule schedule) {
        if (mMainPresenter.isRepeatEvent(schedule)) {
            deleteRepeatSchedule(schedule);
        } else {
            deleteSingleSchedule(schedule);
        }
    }

    public void refreshCards() {
        if (cardAdapter != null) {
            cardAdapter.notifyDataSetChanged();
        }
    }

    private void deleteRepeatSchedule(final BaseSchedule schedule) {
        ArrayList<String> labelArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.delete_repeating_labels)));
        String[] items = new String[]{labelArray.get(0), labelArray.get(1)};
        new AmigoAlertDialog.Builder(this).setTitle(getString(R.string.delete_recurring_event_title, schedule.getTitle()))
                // Gionee liyu 2017-02-08 modify for GNSPR #60011 begin
                .setMessage(labelArray.get(1))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMainPresenter.deleteRepeatSchedule(schedule, mSchedules, mExpressSchedules, false);
                    }
                }).show();
//		.setSingleChoiceItems(items, deleteSelected, mSingleChoiceListListener)
//				.setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						switch (deleteSelected) {
//						case 0:
//							mMainPresenter.deleteRepeatSchedule(schedule, mSchedules, mExpressSchedules, true);
//							break;
//						case 1:
//							mMainPresenter.deleteRepeatSchedule(schedule, mSchedules, mExpressSchedules, false);
//							break;
//						}
//					}
//				}).setNegativeButton(R.string.cancel, null).show();
//	}

//	private DialogInterface.OnClickListener mSingleChoiceListListener = new DialogInterface.OnClickListener() {
//		public void onClick(DialogInterface dialog, int which) {
//			deleteSelected = which;
//		}
        // Gionee liyu 2017-02-08 modify for GNSPR #60011 end
    }

    ;

    private void deleteSingleSchedule(final BaseSchedule schedule) {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.delete));
        builder.setMessage(this.getResources().getString(R.string.delete_schedule_or_not));
        builder.setNegativeButton(this.getResources().getString(R.string.cancel), null);
        builder.setPositiveButton(this.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mMainPresenter.deleteSingleSchedule(schedule, mSchedules, mExpressSchedules);
                // refreshAdapterPosition();
                // showScheduleLayout();
                WidgetUtils.updateWidget(CalendarActivity.this);
            }
        });
        builder.show();
    }

    private final static int DATE_HEIGHT = 42;
    private int currentTop;
    private int emptyTop;
    private int cacheDy;

    private void initRv() {
        lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRv.setLayoutManager(lm);
        currentTop = DensityUtils.dip2px(this, ScrollLayout.WEEK_HEIGHT_DIP + DATE_HEIGHT);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, currentTop, 0, 0);
        mRv.setLayoutParams(params);
        // EmptyView
        emptyTop = currentTop + DensityUtils.dip2px(this, 100);
        FrameLayout.LayoutParams evParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        evParams.setMargins(0, emptyTop, 0, 0);
        mCalendarEmptyView.setLayoutParams(evParams);

    }

    @Override
    public void onCalendarScrolled(int dy) {
        currentTop += dy;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, currentTop, 0, 0);
        mRv.setLayoutParams(params);

        // EmptyView
        emptyTop += (dy + cacheDy) / 3 * 2;
        cacheDy = (dy + cacheDy) % 3;
        FrameLayout.LayoutParams evParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        evParams.setMargins(0, emptyTop, 0, 0);
        mCalendarEmptyView.setLayoutParams(evParams);

        // Hide Vertical Gesture Tips
//		ShowTipsUtil.setGestureTipsState(this, mGestureTips, ShowTipsUtil.GESTURE_TIPS_VERTICAL); // 垂直滑动日历时，垂直手势消失
    }

    @Override
    protected void onStart() {
        LogUtils.i(TAG, "onStart-----CalendarActivity...");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LogUtils.i(TAG, "onStop-----CalendarActivity...");
        super.onStop();
    }

    @Override
    public void onResume() {
        LogUtils.i(TAG, "onResume -----CalendarActivity...");
        super.onResume();
        updateSchedule(mCalendarManager.getCalendar());
    }

    /*
     * set showing date blow weekView or monthView add by zjl at 2017-1-14
     */
    private void setDate(Calendar selectedCalendar) {
        if (selectedCalendar != null) {
            Date todayDate = selectedCalendar.getTime();
            if (selectedCalendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                tv_date.setText(DateUtils.getDate(todayDate));
            } else {
                tv_date.setText(DateUtils.getDateIncludeYear(todayDate));
            }
            tv_lunar.setText(mCalendarManager.getLunarMonthDay(selectedCalendar));
        }
    }

    private static class QueryScheduleThread extends Thread{
        private WeakReference<CalendarActivity> mActivity;
        Calendar cal;
        public QueryScheduleThread(CalendarActivity calendarActivity,Calendar calendar){
            mActivity = new WeakReference<CalendarActivity>(calendarActivity);
            cal = calendar;
        }

        @Override
        public void run() {
            final CalendarActivity activity = mActivity.get();
            if(activity != null){
                activity.mSchedules = activity.mCalendarManager.queryScheduleByDate(cal.getTime());
                SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                Calendar today = Calendar.getInstance();
                if (sharedPreferences.getBoolean(Constants.EXPRESS_SWITCH_PREFERENCE_KEY, true)) {
                    activity.mExpressSchedules = activity.mCalendarManager.queryExpressList(activity.mSchedules,cal);
                }
                Message msg = Message.obtain();
                activity.mHandler.sendEmptyMessage(0);
            }
            super.run();
        }
    }

    private void queryScheduleByDate(final Calendar cal) {
        new QueryScheduleThread(this,cal).start();
    }

    @Override
    public void updateSchedule(Calendar c) {
        setDate(c);
        queryScheduleByDate(c);
        isToday(c);
        updateScheduleFlags();
    }

    private void updateScheduleFlags() {
        mCalendarManager.updateScheduleFlags();
    }

    private void isToday(Calendar c) {
        Calendar cal = Calendar.getInstance();
        if (c.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
            iv_today.setVisibility(View.INVISIBLE);
        } else {
            iv_today.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void upDateWeekScheduleInfo() {
        final WeekViewAdapter adapter = mViewPagerAdapter.getAdapter(prePostion);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScheduleDeleted() {
        refreshCards();
        mCalendarManager.updateScheduleFlags();
        showEmptyViewIfNeed();
    }

    private boolean showEmptyViewIfNeed() {
        if (mSchedules == null || mSchedules.size() == 0) {
            mRv.setVisibility(View.INVISIBLE);
            mCalendarEmptyView.setVisibility(View.VISIBLE);
            return true;
        } else {
            mRv.setVisibility(View.VISIBLE);
            mCalendarEmptyView.setVisibility(View.GONE);
            return false;
        }
    }

    @Override
    public void updateCurrentWeekView() {
        //added by luorw for S10c 终端项目Bug #87720 2017-03-23 begin
        WeekView weekView = mViewPagerAdapter.getItem(prePostion);
        if (weekView != null && weekView.getAdapter() != null) {
            weekView.getAdapter().notifyDataSetChanged();
        }
        //added by luorw for S10c 终端项目Bug #87720 2017-03-23 end
    }

}
