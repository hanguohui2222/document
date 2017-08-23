package com.gionee.secretary.ui.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

//import com.autonavi.v2.protocol.model.Point;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.AddressBean;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.calendar.LunarUtil;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.presenter.SelfCreateSchedulePresenter;
import com.gionee.secretary.utils.ACache;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.TimeUtils;
import com.gionee.secretary.utils.WidgetUtils;
import com.gionee.secretary.ui.viewInterface.ISelfCreateScheduleView;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoProgressDialog;
import amigoui.widget.AmigoDatePicker;
import amigoui.widget.AmigoEditText;
import amigoui.widget.AmigoNumberPicker;
import amigoui.widget.AmigoSwitch;
import amigoui.widget.AmigoTextView;
import amigoui.app.AmigoDatePickerDialog;
import amigoui.widget.AmigoDateTimePickerDialog;

/**
 * Created by hangh on 5/10/16.
 */
public class SelfCreateScheduleActivity extends PasswordBaseActivity implements View.OnClickListener, ISelfCreateScheduleView {
    private static final String TAG = "SelfCreateScheduleAct";
    private static final int MAX_DESCRIPTION_EXT_LENGTH = 500;
    private static final int MAX_TITLE_EXT_LENGTH = 50;
    AmigoEditText mEtTitle;
    TextView mAddress;
    LinearLayout mStartTimeLayout;
    LinearLayout mEndTimeLayout;
    LinearLayout mRemindLayout;
    LinearLayout mRecycleLayout;
    AmigoTextView mRemind;
    private int mRemindType = 0;
    private int mRecycleType = 0;
    AmigoTextView mRecycle;
    AmigoEditText mDescription;
    AmigoSwitch mWholeDay;
    LinearLayout mAddressLayout;
    AmigoTextView mStartDate;
    AmigoTextView mEndDate;

    String citycode;
    private ACache mACache;
    private double[] mEnds = new double[2];
    private int scheduleId;
    SelfCreateSchedule selfCreateSchedule;
    private final static int SAVE_SUCCESS = 0;
    private final static int UPDATE_SUCCESS = 1;
    private final static int INPUT_TITLE = 2;
    private long start;
    private long time;
    private long end;
    private long editStart;
    private long editEnd;
    SimpleDateFormat dateFormat;
    SimpleDateFormat datetimeformat;
    SimpleDateFormat timeformat;
    String[] mRemindAdapter = new String[]{"无需提醒", "准时提醒", "智能提醒", "10分钟前", "30分钟前", "1小时前", "1天前", "自定义时间"};
    String[] mRecycleAdapter;
    //    private boolean mHasAddress = false;
    private boolean mIsRepeatEvent = false;
    private int modifySelected = 0;
    private boolean fromToday;
    private AmigoProgressDialog pd;
    String lastRemindPeriod;
    String lastStartTime;
    String lastEndTime;
    String lastRemindTime;
    boolean isChangedStartTime = false;
    boolean isChangedRemindPeriod = false;
    boolean isChangedRemindTime = false;
    boolean isChangedEndTime = false;
    private String desc;
    Date orign_date = null;
    private SelfCreateSchedulePresenter mSelfCreateSchedulePresenter;
    private static int newIdByEdit = -1;
    private ImageView mIvTips;
    private ImageButton mTipsPoint;
    private String mTripMode;
    private String mAddressRemark;
    private AmigoTextView tv_save;
    SelfCreateSchedule mSchedule;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_main);
        mACache = ACache.get(this);
        initview();
        initdata();
        initListener();
        updateUI();
        updateEditEvent(getIntent());
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Constants.LAUNCH_FROM_ADDRESS_ACTION.equals(intent.getAction())) {
            boolean isDelete = intent.getBooleanExtra(Constants.DELETE_ADDRESS, false);
            if (isDelete) {
                resetAddress();
            } else {
                updateAddress(intent);
            }
        } else {
            updateUI();
            updateEditEvent(intent);
        }
    }

    private void resetAddress() {
        mAddress.setText("");
        mTripMode = "开车";
        mAddressRemark = "";
        mAddress.setVisibility(View.VISIBLE);
//        mAddressLayout.setVisibility(View.GONE);
    }

    private void updateAddress(Intent intent) {
        mAddressLayout.setVisibility(View.VISIBLE);
        String address = intent.getExtras().getString(Constants.ADDRESS);
        mTripMode = intent.getExtras().getString(Constants.TRAVEL_MODE);
        mAddressRemark = intent.getExtras().getString(Constants.ADDRESS_REMARK);
        mAddress.setText(address);
        if (selfCreateSchedule != null) {
            selfCreateSchedule.setAddressRemark(mAddressRemark);
        }
    }

    private void updateEditEvent(Intent intent) {
        mIsRepeatEvent = intent.getBooleanExtra(Constants.IS_REPEAT_EVENT, false);
        if (mIsRepeatEvent) {
            showModifyEventDialog();
        }
    }

    private void showModifyEventDialog() {
        ArrayList<String> labelArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.modify_repeating_labels)));
        String[] items = {labelArray.get(0), labelArray.get(1)};
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.gn_edit_event_label, selfCreateSchedule.getTitle()));
        // Gionee liyu 2017-02-08 modify for GNSPR #60011 begin
        builder.setMessage(labelArray.get(1));
//        builder.setSingleChoiceItems(items, modifySelected, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                modifySelected = which;
//            }
//        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        builder.setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

//                switch (modifySelected) {
//                    case 0:
//                        fromToday = true;
//                        break;
//                    case 1:
//                        fromToday = false;
//                        break;
//                }
            }
        });
        // Gionee liyu 2017-02-08 modify for GNSPR #60011 end
        AmigoAlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SelfCreateScheduleActivity> mActivity;

        public MyHandler(SelfCreateScheduleActivity activity) {
            mActivity = new WeakReference<SelfCreateScheduleActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SelfCreateScheduleActivity selfCreateScheduleActivity = mActivity.get();
            if (selfCreateScheduleActivity == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setClass(selfCreateScheduleActivity, CalendarActivity.class);
            if (selfCreateScheduleActivity.isValidContext() && selfCreateScheduleActivity.pd != null && selfCreateScheduleActivity.pd.isShowing()) {
                selfCreateScheduleActivity.pd.dismiss();
            }
            switch (msg.what) {
                case SAVE_SUCCESS:
                    Toast.makeText(selfCreateScheduleActivity.getApplicationContext(), R.string.savesuccess, Toast.LENGTH_LONG).show();
                    if (selfCreateScheduleActivity.isForeground(selfCreateScheduleActivity, Constants.SELF_CREATE_CLASS_NAME)) {
                        selfCreateScheduleActivity.finish();
                        selfCreateScheduleActivity.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    } else {
                        //SelfCreateScheduleActivity.this.startActivity(intent);
                        selfCreateScheduleActivity.finish();
                        selfCreateScheduleActivity.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    }
                    break;
                case UPDATE_SUCCESS:
                    Toast.makeText(selfCreateScheduleActivity.getApplicationContext(), R.string.updatesuccess, Toast.LENGTH_LONG).show();
                    Intent data = new Intent();
                    data.putExtra(Constants.SCHEDULE_KEY, (SelfCreateSchedule) msg.obj);
                    data.putExtra(Constants.SCHEDULE_ID_KEY, newIdByEdit);
                    selfCreateScheduleActivity.setResult(RESULT_OK, data);
                    selfCreateScheduleActivity.finish();
                    selfCreateScheduleActivity.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    break;
                case INPUT_TITLE:
                    Toast.makeText(selfCreateScheduleActivity.getApplicationContext(), R.string.inputtitle, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    private boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidContext() {
        Activity a = this;
        if (a.isDestroyed() || a.isFinishing()) {
            return false;
        } else {
            return true;
        }
    }

    private void updateUI() {
        Intent data = getIntent();
        scheduleId = data.getIntExtra(Constants.SCHEDULE_ID_KEY, 0);
        LogUtils.e(TAG, "scheduleId = " + scheduleId);
        if (scheduleId > 0) {
            mSelfCreateSchedulePresenter.editSchedule(scheduleId);
        }
        updateWholeday(false);

    }


    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayOptions(AmigoActionBar.DISPLAY_SHOW_CUSTOM, AmigoActionBar.DISPLAY_SHOW_CUSTOM);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.action_self_create, null);
        AmigoTextView tv_cancel = (AmigoTextView) view.findViewById(R.id.cancel);
        tv_save = (AmigoTextView) view.findViewById(R.id.save);
        tv_cancel.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.FILL_HORIZONTAL);
        mActionBar.setCustomView(view, param);
        mActionBar.show();
    }

    private void initview() {
        initActionBar();
        mEtTitle = (AmigoEditText) findViewById(R.id.title);
        mAddress = (TextView) findViewById(R.id.address);
        mStartTimeLayout = (LinearLayout) findViewById(R.id.starttime_layout);
        mEndTimeLayout = (LinearLayout) findViewById(R.id.endtime_layout);
        mRemindLayout = (LinearLayout) findViewById(R.id.remind_layout);
        mRecycleLayout = (LinearLayout) findViewById(R.id.recycle_layout);
        mRemind = (AmigoTextView) findViewById(R.id.remind);
        mRecycle = (AmigoTextView) findViewById(R.id.recycle);
        mDescription = (AmigoEditText) findViewById(R.id.description);
        mWholeDay = (AmigoSwitch) findViewById(R.id.wholeday);
        mAddressLayout = (LinearLayout) findViewById(R.id.address_layout);
        mStartDate = (AmigoTextView) findViewById(R.id.start_date);
        mEndDate = (AmigoTextView) findViewById(R.id.end_date);
        mEtTitle.addTextChangedListener(new MyTextWatcher(mEtTitle, MAX_TITLE_EXT_LENGTH));
        mDescription.addTextChangedListener(new MyTextWatcher(mDescription, MAX_DESCRIPTION_EXT_LENGTH));
        mIvTips = (ImageView) findViewById(R.id.tips);


        //Tips
//        mTipsDisplayLayer = (RelativeLayout)findViewById(R.id.tips_display_layer);
//        mTipsTouchLayer = (FrameLayout)  findViewById(R.id.tips_touch_layer);

//        mTipsPoint = (ImageButton) findViewById(R.id.tips_point);
//        ShowTipsUtil.showTips(this,mTipsTouchLayer,null,mTipsPoint,Constants.TIPS_FOR_NEW_SCHEDULE);
        // Gionee sunyang 2017-01-14 modify for  GNSPR #64916 begin
        if (TextUtils.isEmpty(mEtTitle.getText()) && TextUtils.isEmpty(mDescription.getText())) {
            tv_save.setEnabled(true);
        } else {
            tv_save.setEnabled(true);
        }
        // Gionee sunyang 2017-01-14 modify for  GNSPR #64916 end
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initdata() {
        mSchedule = new SelfCreateSchedule();
        mSelfCreateSchedulePresenter = new SelfCreateSchedulePresenter(this, this);
        mRecycleAdapter = getResources().getStringArray(R.array.remind_period);
        mRemind.setText(mRemindAdapter[3]);
        mRecycle.setText(mRecycleAdapter[0]);
        datetimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeformat = new SimpleDateFormat("HH:mm");
        time = System.currentTimeMillis();
        start = TimeUtils.constructDefaultStartTime(time);
        end = TimeUtils.constructDefaultEndTime(start);
        Date startDate = new Date(start);
        mStartDate.setText(datetimeformat.format(startDate));
        Date endDate = new Date(end);
        mEndDate.setText(datetimeformat.format(endDate));
        mTripMode = "开车";
    }

    private void initListener() {
        mRemindLayout.setOnClickListener(this);
        mRecycleLayout.setOnClickListener(this);
        mStartTimeLayout.setOnClickListener(this);
        mEndTimeLayout.setOnClickListener(this);
        mAddressLayout.setOnClickListener(this);
        mWholeDay.setOnClickListener(this);
    }

    /**
     * 获取历史查询
     */
    private void getHistory() {
        boolean isHave = false;
        String name = mAddress.getText().toString();
        LogUtils.e(TAG, "address name2 = " + name);
        List<AddressBean> list = (List<AddressBean>) mACache.getAsObject(Constants.HISTORY_ADDRESS);
        if (null == list) {
            list = new ArrayList<>();
        }
        if (!name.equals("")) {
            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).getName().equals(name)) {

                    isHave = true;
                    break;
                } else {

                    isHave = false;
                }
            }
            if (!isHave) {
                LogUtils.e(TAG, "isHave = " + isHave);
                AddressBean addressBean = new AddressBean();
                addressBean.setName(name);
                addressBean.setmLatitude(mEnds[0]);
                addressBean.setmLongitude(mEnds[1]);
                addressBean.setDesc(desc);
                list.add(addressBean);
            }
            mACache.put(Constants.HISTORY_ADDRESS, (Serializable) list);
        }
    }

    /**
     * 显示提醒设置对话框
     */
    private void showRemindDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.remind));
        mRemindType = Arrays.asList(mRemindAdapter).indexOf(mRemind.getText().toString());

        if (mRemindType == -1) {
            mRemindType = mRemindAdapter.length - 1;
        }
        if (mWholeDay.isChecked()) {
            mRemindAdapter[1] = this.getResources().getString(R.string.on_time_at_ten);
        } else {
            mRemindAdapter[1] = this.getResources().getString(R.string.on_time_remind);
        }
        builder.setSingleChoiceItems(mRemindAdapter, mRemindType, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mRemindAdapter[which].equals(SelfCreateScheduleActivity.this.getResources().getString(R.string.defined_time))) {
                    showUserDefinedTimeDialog();
                } else {
                    mRemind.setText(mRemindAdapter[which]);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private AmigoNumberPicker mDayPicker;
    private AmigoNumberPicker mHourPicker;
    private AmigoNumberPicker MinutePicker;

    private void showUserDefinedTimeDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getApplicationContext().getString(R.string.defined_time_remind));
        View root = this.getLayoutInflater().inflate(R.layout.dialog_gn_user_defined_time_picker, null);
        mDayPicker = (AmigoNumberPicker) root.findViewById(R.id.day_picker);
        mDayPicker.setMaxValue(30);
        mDayPicker.setMinValue(0);
        mHourPicker = (AmigoNumberPicker) root.findViewById(R.id.hour_picker);
        mHourPicker.setMaxValue(23);
        mHourPicker.setMinValue(0);
        MinutePicker = (AmigoNumberPicker) root.findViewById(R.id.minute_picker);
        MinutePicker.setMaxValue(59);
        MinutePicker.setMinValue(0);
        mDayPicker.setValue(0);
        mHourPicker.setValue(0);
        MinutePicker.setValue(10);
        builder.setView(root);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(this.getApplicationContext().getString(R.string.time_picker_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String remindTime = mDayPicker.getValue()
                        + SelfCreateScheduleActivity.this.getResources().getString(R.string.defined_day)
                        + mHourPicker.getValue()
                        + SelfCreateScheduleActivity.this.getResources().getString(R.string.defined_hour)
                        + MinutePicker.getValue()
                        + SelfCreateScheduleActivity.this.getResources().getString(R.string.defined_minute);
                mRemind.setText(remindTime);
                long porid = mDayPicker.getValue() * 24 * 60 * 60 * 1000 + mHourPicker.getValue() * 60 * 60 * 1000 + MinutePicker.getValue() * 60 * 1000;
                mSchedule.setRemindTime(porid);
            }
        });
        builder.show();
    }

    private void showSetTimeDialog(String mTimeStr, View view) {
        Calendar calendar = Calendar.getInstance();
        if (mWholeDay.isChecked() && selfCreateSchedule != null) {
            calendar.setTimeInMillis(DateUtils.formatDate2(mTimeStr).getTime());
        } else {
            calendar.setTimeInMillis(DateUtils.formatDate3(mTimeStr).getTime());
        }
        calendar.setTimeZone(TimeZone.getTimeZone(Time.getCurrentTimezone()));
        DateTimeListener dateTimeListener = new DateTimeListener(this, view);
        DateListener dateListener = new DateListener(this, view);
        if (!mWholeDay.isChecked()) {
            showLunarDateTimePickerDialog(dateTimeListener, calendar);
        } else {
            showLunarDatePickerDiglog(dateListener, calendar);
        }
    }


    /**
     * 显示时间设置对话框
     *
     * @param mTime
     * @param view
     */
    private void showSetTimeDialog(long mTime, View view) {
        //modified by luorw for GNSPR #72839 2017-03-15 begin
        Calendar calendar = Calendar.getInstance();
        if (mWholeDay.isChecked() && selfCreateSchedule != null) {
            long time = selfCreateSchedule.getDate().getTime();
            calendar.setTimeInMillis(time);
        } else {
            calendar.setTimeInMillis(mTime);
        }
        //modified by luorw for GNSPR #72839 2017-03-15 end
        calendar.setTimeZone(TimeZone.getTimeZone(Time.getCurrentTimezone()));
        DateTimeListener dateTimeListener = new DateTimeListener(this, view);
        DateListener dateListener = new DateListener(this, view);
        if (!mWholeDay.isChecked()) {
            showLunarDateTimePickerDialog(dateTimeListener, calendar);
        } else {
            showLunarDatePickerDiglog(dateListener, calendar);
        }
    }

    /**
     * 显示农历日期设置对话框
     *
     * @param dateListener 日期变更监听器
     * @param calendar     修改前日期
     */
    private void showLunarDatePickerDiglog(DateListener dateListener, Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        AmigoDatePickerDialog lunarDialog = new AmigoDatePickerDialog(this, dateListener, year, month, day);
        if (isChinese()) {
            lunarDialog.showLunarModeSwitch();
        }
        lunarDialog.show();
    }

    /**
     * 当前系统语言是否为简体中文
     *
     * @return
     */
    private boolean isChinese() {
        String locale = getResources().getConfiguration().locale.getCountry();
        return "CN".equals(locale);
    }

    /**
     * 显示农历日期时间设置对话框
     *
     * @param dateTimeListener
     * @param calendar
     */
    private void showLunarDateTimePickerDialog(DateTimeListener dateTimeListener, Calendar calendar) {
        AmigoDateTimePickerDialog d = new AmigoDateTimePickerDialog(this, dateTimeListener, calendar);
        if (isChinese()) {
            d.showLunarModeSwitch();
        }
        d.show();
    }

    private void setDateTime(AmigoTextView dateView, long millis) {
        Time time = new Time();
        time.set(millis);
        time.normalize(true);
        String lunarDate = LunarUtil.getLunarDateWithOutYear(this, time.year,
                time.month, time.monthDay, true);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.applyPattern("HH:mm");
        dateView.setText(lunarDate);
    }

    /**
     * ISelfCreateScheduleView接口实现方法，用于处理保存成功后事件
     */
    @Override
    public void saveSuccess() {
        mHandler.sendMessage(mHandler.obtainMessage(SAVE_SUCCESS));
    }

    /**
     * ISelfCreateScheduleView接口实现方法，用于处理取消事件
     */
    @Override
    public void cancel() {
        if (scheduleId > 0) {
            LogUtils.i(TAG, "hasEdited:" + isChangedSchedule());
            if (isChangedSchedule()) {
                showCancelDialog();
            } else {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        } else {
            // Gionee sunyang modify for GNSPR #64919 at 2017-01-14 begin
//            finish();

            /*modify by zhengjl at 2017-01-19 begin
            没有内容直接返回，有内容提示是否舍弃
             */
            if (TextUtils.isEmpty(mEtTitle.getText().toString()) && TextUtils.isEmpty(mDescription.getText().toString())) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            } else
                showCancelDialog();
            /*modify by zhengjl at 2017-01-19 begin
            没有内容直接返回，有内容提示是否舍弃
             */
            // Gionee sunyang modify for GNSPR #64919 at 2017-01-14 end
        }
    }

    /**
     * 显示取消对话框
     */
    private void showCancelDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.giveup));
        builder.setMessage(this.getResources().getString(R.string.give_up_msg));
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.giveup, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        builder.show();
    }

    /**
     * 编辑模式
     *
     * @param schedule 日程Bean
     */
    @Override
    public void EditSchedule(SelfCreateSchedule schedule) {
        LogUtils.e(TAG, "selfCreateScheduleActivity.... updateUI....selfCreateSchedule...");
        selfCreateSchedule = schedule;
        if (selfCreateSchedule == null) return;
        orign_date = selfCreateSchedule.getDate();
        mEtTitle.append(selfCreateSchedule.getTitle());
        mWholeDay.setChecked(selfCreateSchedule.getIsAllDay());
        mRemind.setText(selfCreateSchedule.getRemindType());
        LogUtils.d("liyu", "mRemind = " + mRemind.getText());
        mRecycle.setText(selfCreateSchedule.getRemindPeriod());
        lastRemindPeriod = selfCreateSchedule.getRemindPeriod();
        lastRemindTime = selfCreateSchedule.getRemindType();
        if (selfCreateSchedule.getDate() != null) {
            // 编辑前判断是否为全天事件。若为全天事件，则临时开始时间设成当前时间点的整点。
            // 防止编辑全天事件时，关闭全天按钮后开始时间与结束时间显示错误。
            // (Fix bug GNSPR #65852 by liyh)
            if (mWholeDay.isChecked()) {
                editStart = TimeUtils.constructDefaultStartTime(time);
            } else {
                editStart = selfCreateSchedule.getDate().getTime();
            }
        }
        if (selfCreateSchedule.getEndtime() != null) {
            if (mWholeDay.isChecked()) {
                editEnd = TimeUtils.constructDefaultEndTime(editStart);
            } else {
                editEnd = selfCreateSchedule.getEndtime().getTime();
            }
        }
        if (TextUtils.isEmpty(selfCreateSchedule.getAddress())) {
            mAddress.setVisibility(View.VISIBLE);
            mAddressRemark = "";
            mTripMode = "开车";
        } else {
            mAddressLayout.setVisibility(View.VISIBLE);
            mAddressLayout.setOnClickListener(this);
            mAddress.setText(selfCreateSchedule.getAddress());
            //Gionee <gn_by> <zhengyt> add for Bug#90331 begin
            mTripMode = selfCreateSchedule.getTripMode();
            //Gionee <gn_by> <zhengyt> add for Bug#90331 end
            LogUtils.i(TAG, "address:" + selfCreateSchedule.getAddress() + "  ,mTravel:" + selfCreateSchedule.getTripMode());
        }
//        updateWholeday(false);
        mDescription.setText(selfCreateSchedule.getDescription());
        LogUtils.i(TAG, "selfcreateSchedule....:" + selfCreateSchedule.getDescription());
        mStartDate.setText(DateUtils.date2String4(selfCreateSchedule.getDate()));
        lastStartTime = DateUtils.date2String4(selfCreateSchedule.getDate());
        lastEndTime = DateUtils.date2String4(selfCreateSchedule.getEndtime());
        if (selfCreateSchedule.getEndtime() != null) {
            mEndDate.setText(DateUtils.date2String4(selfCreateSchedule.getEndtime()));
        }
    }

    @Override
    public void UpdateScheduleId(int id) {
        newIdByEdit = id;
    }

    private class DateTimeListener implements AmigoDateTimePickerDialog.OnDateTimeSetListener {
        private View mView;
        private Context mContext;

        public DateTimeListener(Context context, View view) {
            mContext = context;
            mView = view;
        }

        @Override
        public void onDateTimeSet(Calendar calendar) {
            setStartEndDateTime(mContext, mView, calendar);
        }
    }

    private class DateListener implements AmigoDatePickerDialog.OnDateSetListener {
        private View mView;
        private Context mContext;

        public DateListener(Context context, View view) {
            mContext = context;
            mView = view;
        }

        @Override
        public void onDateSet(AmigoDatePicker var1, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0);
            setStartEndDateTime(mContext, mView, calendar);
        }
    }

    private void setStartEndDateTime(Context context, View view, Calendar calendar) {
        long time = calendar.getTimeInMillis();
        Date date = new Date(time);
        long mStart = 0;
        long mEnd = 0;
        if (scheduleId > 0) {
            //编辑模式
            mStart = editStart;
            mEnd = editEnd;
        } else {
            //新建模式
            mStart = start;
            mEnd = end;
        }
        if (mStartTimeLayout == view) {
            //修改开始时间
            if (time >= mEnd) {
                //开始时间大于结束时间
                //Toast.makeText(mContext,R.string.gn_change_starttime_fail,Toast.LENGTH_SHORT).show();
                if (scheduleId > 0) {
                    editStart = time;
                } else {
                    start = time;
                }

                long endTime = time + 1 * 60 * 60 * 1000;

                if (scheduleId > 0) {
                    editEnd = endTime;
                } else {
                    end = endTime;
                }

                //处理全天事件
                if (!mWholeDay.isChecked()) {
                    mStartDate.setText(datetimeformat.format(date));
                    mEndDate.setText(datetimeformat.format(endTime));
                } else {
                    mStartDate.setText(dateFormat.format(date));
                    mEndDate.setText(dateFormat.format(endTime));
                }

            } else {
                if (scheduleId > 0) {
                    editStart = time;
                } else {
                    start = time;
                }

                if (!mWholeDay.isChecked()) {
                    mStartDate.setText(datetimeformat.format(date));
                } else if (mWholeDay.isChecked()) {
                    mStartDate.setText(dateFormat.format(date));
                }
//                setDateTime(mStartDateLunar, time);  //设置农历显示
            }
        } else if (mEndTimeLayout == view) {
            //修改结束时间
            if (time < mStart) {
                //结束时间小于开始时间
                Toast.makeText(context, R.string.gn_change_endtime_fail, Toast.LENGTH_SHORT).show();
                mEndDate.setText(datetimeformat.format(new Date(mEnd)));
                if (!mWholeDay.isChecked()) {
                    mEndDate.setText(datetimeformat.format(mEnd));
                } else {
                    mEndDate.setText(dateFormat.format(mEnd));
                }
            } else {
                if (scheduleId > 0) {
                    editEnd = time;
                } else {
                    end = time;
                }
                //处理全天事件
                if (!mWholeDay.isChecked()) {
                    mEndDate.setText(datetimeformat.format(date));
                } else {
                    mEndDate.setText(dateFormat.format(date));
                }
            }

        } else if (mRemind == view) {
            mRemind.setText(DateUtils.formatDate2String(date));
        }
    }

    private void showRecycleDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.period));
        mRecycleType = Arrays.asList(mRecycleAdapter).indexOf(mRecycle.getText().toString());
        if (mRecycleType == -1) {
            mRecycleType = mRecycleAdapter.length - 1;
        }
        builder.setSingleChoiceItems(mRecycleAdapter, mRecycleType, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRecycle.setText(mRecycleAdapter[which]);
                mRecycleType = which;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Hide Soft Input  隐藏软键盘
     */
    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        LogUtils.d("liyh", "SelfCreateScheduleActivity. hideSoftInput()." + "immActive=" + imm.isActive());
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mEtTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            imm.hideSoftInputFromInputMethod(mDescription.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void save() {
        SystemClock.sleep(1000);//不让闪太快，显示保存过程
        Date starttime = null;
        Date remindTime = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder startbuilder = new StringBuilder();
        StringBuilder remindBuilder = new StringBuilder();
        startbuilder.append(mStartDate.getText().toString().split(" ")[0]);
        startbuilder.append(" ");
        remindBuilder.append(mStartDate.getText().toString().split(" ")[0]);
        remindBuilder.append(" ");
        StringBuilder endbuilder = new StringBuilder();
        endbuilder.append(mEndDate.getText().toString().split(" ")[0]);
        endbuilder.append(" ");
        String[] tempStartDate = mStartDate.getText().toString().split(" ");
        String[] tempEndDate = mEndDate.getText().toString().split(" ");
        if (mWholeDay.isChecked() || (!mWholeDay.isChecked() &&
                (tempStartDate.length == 1 || tempEndDate.length == 1))) {
            startbuilder.append("00:00");
            endbuilder.append("00:00");
            remindBuilder.append("10:00");//全天事件10点提醒
        } else {
            startbuilder.append(mStartDate.getText().toString().split(" ")[1]);
            remindBuilder.append(mStartDate.getText().toString().split(" ")[1]);
            endbuilder.append(mEndDate.getText().toString().split(" ")[1]);
        }


        if (TextUtils.isEmpty(mEtTitle.getText().toString().trim())) {
            mSchedule.setTitle(this.getResources().getString(R.string.new_schedule));
        } else {
            mSchedule.setTitle(mEtTitle.getText().toString());
        }
        mSchedule.setRemindType(mRemind.getText().toString());
        mSchedule.setRemindPeriod(mRecycle.getText().toString());
        mSchedule.setAddress(mAddress.getText().toString());
        mSchedule.setAddressRemark(mAddressRemark);
        mSchedule.setDescription(mDescription.getText().toString());

        try {
            starttime = format.parse(startbuilder.toString());
            Date endTime = format.parse(endbuilder.toString());
            remindTime = format.parse(remindBuilder.toString());
            mSchedule.setDate(starttime);
            mSchedule.setEndtime(endTime);
            //add by zhengjl at 2017-1-19 for GNSPR #65250 begin
            mSchedule.setRemindDate(getRemindTimeLong(remindTime, mRemind.getText().toString()));
            //add by zhengjl at 2017-1-19 for GNSPR #65250 end
            mSchedule.setPeriod(endTime.getTime() - starttime.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "save() error:" + e);
        }
        mSchedule.setIsSmartRemind(getIsSmart(mRemind.getText().toString()));
        mSchedule.setAllDay(mWholeDay.isChecked());
        mSchedule.setTripMode(mTripMode);
        mSchedule.setType(Constants.SELF_CREATE_TYPE);
        LogUtils.d(TAG, "selfcreateActivity....enter save");
        mSelfCreateSchedulePresenter.saveSchedule(mSchedule);
        if (scheduleId > 0) {
            //编辑模式
            LogUtils.d(TAG, "selfcreateActivity....scheduleId = " + scheduleId);
            RemindUtils.alarmEdit(this, mSchedule);
        } else {
            RemindUtils.scheduleRemind(this, mSchedule);
        }
        WidgetUtils.updateWidget(this);
    }

    private long getRemindTimeLong(Date startTime, String remindType) {
        long event = startTime.getTime();
        long remindtime = -1;
        if (this.getResources().getString(R.string.not_remind).equals(remindType)) {
            remindtime = -1;
        } else if (remindType.contains(this.getResources().getString(R.string.on_time))) {
            remindtime = event;
            LogUtils.e(TAG, "remindtime = " + remindtime);
        } else if (this.getResources().getString(R.string.ten_min_ago).equals(remindType) || this.getResources().getString(R.string.smart_remind).equals(remindType)) {
            long porid = 10 * 60 * 1000;
            remindtime = event - porid;
        } else if (this.getResources().getString(R.string.half_hour_ago).equals(remindType)) {
            long porid = 30 * 60 * 1000;
            remindtime = event - porid;
        } else if (this.getResources().getString(R.string.one_hour_ago).equals(remindType)) {
            long porid = 1 * 60 * 60 * 1000;
            remindtime = event - porid;
        } else if (this.getResources().getString(R.string.one_day_ago).equals(remindType)) {
            long porid = 1 * 24 * 60 * 60 * 1000;
            remindtime = event - porid;
        } else {
            //自定义提醒
            int day = 0;
            int hour = 0;
            int minute = 0;
            if (mDayPicker == null && !"".equals(remindType)) {
                String remind = remindType;
                remind = remind.replace(getResources().getString(R.string.defined_day), ":").replace(getResources().getString(R.string.defined_hour), ":").replace(getResources().getString(R.string.defined_minute), "");
                if (remind.split(":").length != 0) {
                    day = Integer.parseInt(remind.split(":")[0]);
                    hour = Integer.parseInt(remind.split(":")[1]);
                    minute = Integer.parseInt(remind.split(":")[2]);
                }
            } else {
                day = mDayPicker.getValue();
                hour = mHourPicker.getValue();
                minute = MinutePicker.getValue();
            }
            long porid = day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000 + minute * 60 * 1000;
            remindtime = event - porid;
        }
        return remindtime;
    }

    private int getIsSmart(String remindType) {
        if (this.getResources().getString(R.string.not_remind).equals(remindType)) {
            return Constants.NOT_REMIND;
        } else if (this.getResources().getString(R.string.smart_remind).equals(remindType)) {
            return Constants.SMART_REMIND;
        } else {
            return Constants.GENERAL_REMIND;
        }
    }

    private SelfCreateSchedule getCurrentSchedule() {
        Date currentDate = null;
        Date remindDate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder startbuilder = new StringBuilder();
        StringBuilder remindBuilder = new StringBuilder();
        startbuilder.append(mStartDate.getText().toString().split(" ")[0]);
        startbuilder.append(" ");
        StringBuilder endbuilder = new StringBuilder();
        endbuilder.append(mEndDate.getText().toString().split(" ")[0]);
        endbuilder.append(" ");

        remindBuilder.append(mStartDate.getText().toString().split(" ")[0]);
        remindBuilder.append(" ");
        if (mWholeDay.isChecked()) {
            startbuilder.append("00:00");
            endbuilder.append("00:00");
            remindBuilder.append("10:00");
        } else {
            String[] startDate = mStartDate.getText().toString().split(" ");
            String[] endDate = mEndDate.getText().toString().split(" ");
            if (startDate.length > 1 && endDate.length > 1) {
                startbuilder.append(startDate[1]);
                remindBuilder.append(startDate[1]);
                endbuilder.append(endDate[1]);
            } else {
                startbuilder.append("00:00");
                endbuilder.append("00:00");
                remindBuilder.append("10:00");
            }
        }
        isChangedStartTime = !startbuilder.toString().equals(lastStartTime);
        isChangedEndTime = !endbuilder.toString().equals(lastEndTime);

        if (TextUtils.isEmpty(mEtTitle.getText().toString())) {
            selfCreateSchedule.setTitle(this.getResources().getString(R.string.new_schedule));
        } else {
            selfCreateSchedule.setTitle(mEtTitle.getText().toString());
        }
        selfCreateSchedule.setRemindType(mRemind.getText().toString());
        selfCreateSchedule.setRemindPeriod(mRecycle.getText().toString());
        selfCreateSchedule.setAddress(mAddress.getText().toString());
        selfCreateSchedule.setAddressRemark(mAddressRemark);
        selfCreateSchedule.setDescription(mDescription.getText().toString());
        selfCreateSchedule.setIsSmartRemind(getIsSmart(mRemind.getText().toString()));
        try {
            currentDate = format.parse(startbuilder.toString());
            remindDate = format.parse(remindBuilder.toString());
            Date endTime = format.parse(endbuilder.toString());
            selfCreateSchedule.setDate(currentDate);
            selfCreateSchedule.setEndtime(endTime);
            long poid = endTime.getTime() - currentDate.getTime();
            selfCreateSchedule.setPeriod(poid);
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "getCurrentSchedule() error=" + e);
        }
        selfCreateSchedule.setRemindDate(getRemindTimeLong(remindDate, mRemind.getText().toString()));
        LogUtils.d("liyu", "getCurrentSchedule=" + RemindUtils.time2String(selfCreateSchedule.getRemindDate()));
        selfCreateSchedule.setAllDay(mWholeDay.isChecked());
        selfCreateSchedule.setTripMode(mTripMode);
        selfCreateSchedule.setType(Constants.SELF_CREATE_TYPE);
        selfCreateSchedule.setId(selfCreateSchedule.getId());
        LogUtils.d("liyu", "SelfCreateSchedule schedule id = " + selfCreateSchedule.getId());
        return selfCreateSchedule;

    }


    private static class SaveScheduleThread extends Thread {
        private WeakReference<SelfCreateScheduleActivity> mActivity;
        public SaveScheduleThread(SelfCreateScheduleActivity activity){
            mActivity = new WeakReference<SelfCreateScheduleActivity>(activity);
        }
        @Override
        public void run() {
            SelfCreateScheduleActivity selfCreateScheduleActivity = mActivity.get();
            if(selfCreateScheduleActivity != null){
                selfCreateScheduleActivity.save();
                selfCreateScheduleActivity.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
            }
        }
    }

    private static class UpdateScheduleThread extends Thread {
        private final WeakReference<SelfCreateScheduleActivity> mAcitivy;
        public UpdateScheduleThread(SelfCreateScheduleActivity selfCreateScheduleActivity){
            mAcitivy = new WeakReference<SelfCreateScheduleActivity>(selfCreateScheduleActivity);
        }
        @Override
        public void run() {
            SelfCreateScheduleActivity activity = mAcitivy.get();
            if (activity != null) {
                activity.isChangedRemindPeriod = !activity.mRecycle.getText().toString().equals(activity.lastRemindPeriod);
                activity.isChangedRemindTime = !activity.mRemind.getText().toString().equals(activity.lastRemindTime);
                activity.selfCreateSchedule = activity.getCurrentSchedule();
                RemindUtils.cancelScheduleAlarm(activity, activity.selfCreateSchedule);
                LogUtils.d(TAG, "UpdateScheduleThread." + "****isRepeatEvent=" + activity.isRepeateEvent() + "****" +
                        " isChangedRemidPeriod=" + activity.isChangedRemindPeriod + ";isChangedStartTime=" + activity.isChangedStartTime +
                        ";isChangedRemindTime=" + activity.isChangedRemindTime);
                if (activity.isRepeateEvent()) {
                    //编辑重复性事件
                    if (activity.isChangedRemindPeriod || activity.isChangedStartTime || activity.isChangedRemindTime || activity.isChangedEndTime) {
                        //删除重建日程
                        activity.mSelfCreateSchedulePresenter.resetScheduleForPeriod(activity.selfCreateSchedule, activity.orign_date, activity.fromToday);
                    } else {
                        //更新日程
                        try {
                            Thread.sleep(500);
                            activity.selfCreateSchedule.setIsActive(Constants.REMIND_NOT_ACTIVE);
                            activity.mSelfCreateSchedulePresenter.updateScheduleToDB(activity.selfCreateSchedule, activity.fromToday, true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(UPDATE_SUCCESS, activity.selfCreateSchedule));
                } else {
                    //编辑一次事件
                    if (activity.isChangedRemindPeriod) {
                        //删除重建日程
                        activity.mSelfCreateSchedulePresenter.resetScheduleForPeriod(activity.selfCreateSchedule, activity.orign_date, activity.fromToday);
                    } else {
                        try {
                            Thread.sleep(500);
                            activity.selfCreateSchedule.setIsActive(Constants.REMIND_NOT_ACTIVE);
                            activity.mSelfCreateSchedulePresenter.updateScheduleToDB(activity.selfCreateSchedule, false, false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    LogUtils.d(TAG, "selfcreateActivity......UpdateScheduleThread........isEdit = true");
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(UPDATE_SUCCESS, activity.selfCreateSchedule));
                }
                RemindUtils.refreshScheduleAlarm(activity);
                activity.sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
            }
        }
    }

    private boolean isRepeateEvent() {
        boolean mIsRepeateEvent = false;
        if (!this.getResources().getString(R.string.once).equals(mRecycle.getText().toString())) {
            mIsRepeateEvent = true;//自建日程 非一次性事件
        } else {
            mIsRepeateEvent = false;//自建日程 一次性事件
        }
        return mIsRepeateEvent;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.remind_layout:
                showRemindDialog();
                break;
            case R.id.recycle_layout:
                showRecycleDialog();
                break;
            case R.id.cancel:
                cancel();
                break;
            case R.id.save:
                startSave();
                break;
            case R.id.starttime_layout:
                if (scheduleId > 0) {
                    showSetTimeDialog(mStartDate.getText().toString(), v);
                } else {
                    showSetTimeDialog(start, v);
                }
                break;
            case R.id.endtime_layout:
                if (scheduleId > 0) {
                    showSetTimeDialog(mEndDate.getText().toString(), v);
                } else {
                    showSetTimeDialog(end, v);
                }
                break;
            case R.id.address_layout:
                //当前地址为空则跳转到地址选择界面
                if (mAddress.getText().toString().trim().equals("")) {
                    Intent addAddressIntent = new Intent(this, AddAddressActivity.class);
                    addAddressIntent.putExtra(Constants.CITYCODE, citycode);
                    addAddressIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    startActivity(addAddressIntent);
                } else {
                    Intent intent2 = new Intent(this, AddressRemarkActivity.class);
                    if (scheduleId > 0) {
                        mAddressRemark = selfCreateSchedule.getAddressRemark();
                        mTripMode = selfCreateSchedule.getTripMode();
                    }
                    intent2.putExtra(Constants.ADDRESS, mAddress.getText().toString());
                    intent2.putExtra(Constants.ADDRESS_REMARK, mAddressRemark);
                    intent2.putExtra(Constants.TRAVEL_MODE, mTripMode);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    startActivity(intent2);
                }
                break;
            case R.id.wholeday:
                updateWholeday(true);
                break;
        }
    }

    private void startSave() {
        if(TextUtils.isEmpty(mEtTitle.getText()) && TextUtils.isEmpty(mDescription.getText())){
            Toast.makeText(this,"请输入日程标题或内容",Toast.LENGTH_SHORT).show();
            return;
        }
        getHistory();
        hideSoftInput();  //保存前隐藏软键盘，防止保存成功后闪屏  (Fix bug GNSPR #66334 by liyh)
        if (scheduleId > 0) {
            pd = new AmigoProgressDialog(SelfCreateScheduleActivity.this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.show();
            new UpdateScheduleThread(this).start();
        } else {
            pd = AmigoProgressDialog.show(this, "", "", false, false);
            new SaveScheduleThread(this).start();
        }
    }

    private void updateWholeday(boolean isClick) {
        if (mWholeDay.isChecked()) {
            //获取日期、时间并显示
            String startTime = mStartDate.getText().toString();
            String endTime = mEndDate.getText().toString();
            mStartDate.setText(startTime.split(" ")[0]);
            mEndDate.setText(endTime.split(" ")[0]);
            //修改备忘“准时”选项为“准时10:00”
            //added by luorw for GNSPR #78993 2017-04-12 begin
            if (isClick) {
                mRemindAdapter[1] = this.getResources().getString(R.string.on_time_at_ten);
                mRemind.setText(mRemindAdapter[1]);
            } else {
                mRemind.setText(selfCreateSchedule.getRemindType());
            }
            //added by luorw for GNSPR #78993 2017-04-12 end
        } else {
            //修改备忘选项“准时”文字
            mRemindAdapter[1] = this.getResources().getString(R.string.on_time);
            if (scheduleId > 0) {
                //编辑日程模式
                //显示日期、时间
                //added by luorw for GNSPR #72839 2017-03-15 begin
                long editTimeStart = TimeUtils.constructDefaultStartTime(System.currentTimeMillis());
                long editTimeEnd = TimeUtils.constructDefaultEndTime(editTimeStart);
                //added by luorw for GNSPR #72839 2017-03-15 end
                String startTime = mStartDate.getText().toString().split(" ")[0] + " " + timeformat.format(new Date(editTimeStart));
                String endTime = mEndDate.getText().toString().split(" ")[0] + " " + timeformat.format(new Date(editTimeEnd));
                editStart = editTimeStart;
                editEnd = editTimeEnd;
                //Gionee zhengyt 2017-3-20 modify for GNSPR#73368 Begin
                if (selfCreateSchedule.getDate() != null && !isClick) {
                    mStartDate.setText(DateUtils.date2String4(selfCreateSchedule.getDate()));
                } else {
                    mStartDate.setText(startTime);
                }
                if (selfCreateSchedule.getEndtime() != null && !isClick) {
                    mEndDate.setText(DateUtils.date2String4(selfCreateSchedule.getEndtime()));
                } else {

                    mEndDate.setText(endTime);
                }
                //Gionee zhengyt 2017-3-20 modify for GNSPR#73368 end
                //修改提醒文字
                //modified by luorw for GNSPR #68553 20170222 begin
                if (isClick) {
                    mRemind.setText(mRemindAdapter[3]);
                } else {
                    mRemind.setText(selfCreateSchedule.getRemindType());
                }
                //modified by luorw for GNSPR #68553 20170222 begin
            } else {
                //新建日程模式
                //显示日期、时间
                String startTime = mStartDate.getText().toString().split(" ")[0] + " " + timeformat.format(new Date(start));
                String endTime = mEndDate.getText().toString().split(" ")[0] + " " + timeformat.format(new Date(end));
                mStartDate.setText(startTime);
                mEndDate.setText(endTime);
                //修改提醒文字
                mRemind.setText(mRemindAdapter[3]);
            }
        }
    }


    private boolean isChangedSchedule() {
        LogUtils.i(TAG, "selfCreateScheduleActivity....isChangedSchedule...");
//        LogUtils.i(TAG, "title:" + mEtTitle.getText() + "  checked:" + mWholeDay.isChecked() + "  ,mRemind:" + mRemind.getText()
//                + "  ,mRecycle:" + mRecycle.getText() + "  ,mAddress2:" + mAddress.getText() + "  ,mTravel:" + mTripMode +
//                "  ,mDescription:" + mDescription.getText() + "  ,mStartDate:" + mStartDate.getText() + "  ,mStartTime:" + mStartTime.getText() + "  ,mEndDate:" +
//                mEndDate.getText() + "  ,endTime:" + mEndTime.getText());

        selfCreateSchedule.toString();
        boolean titleChanged = mEtTitle.getText().toString().equals(selfCreateSchedule.getTitle()); //true 没便
        boolean allDayChanged = mWholeDay.isChecked() == selfCreateSchedule.getIsAllDay();//true 没便

        boolean remindTypeChanged = mRemind.getText().toString().equals(selfCreateSchedule.getRemindType());//true 没便
        boolean remindPeriodChanged = mRecycle.getText().toString().equals(selfCreateSchedule.getRemindPeriod());//true 没便
        boolean addressChanged = mAddress.getText().toString().equals(selfCreateSchedule.getAddress());//true 没便
//        boolean tripModeChanged = mTravel.getText().toString().equals(selfCreateSchedule.getTripMode());//true 没便
        boolean tripModeChanged = mTripMode.equals(selfCreateSchedule.getTripMode());//true 没便
        boolean descriptionChanged = mDescription.getText().toString().equals(selfCreateSchedule.getDescription());//true 没便

        boolean startDataChanged = mStartDate.getText().toString().equals(DateUtils.date2String(selfCreateSchedule.getDate()));//true 没便
//        boolean startTimeChanged = mStartTime.getText().toString().equals(DateUtils.time2String(selfCreateSchedule.getDate()));//true 没便

        boolean endDataChanged = mEndDate.getText().toString().equals(DateUtils.date2String(selfCreateSchedule.getEndtime()));//true 没便
//        boolean endTimeChanged = mEndTime.getText().toString().equals(DateUtils.time2String(selfCreateSchedule.getEndtime()));//true 没便
//        LogUtils.i(TAG, "titleChanged:" + titleChanged + "  allDayChanged:" + allDayChanged + "  ,remindTypeChanged:" + remindTypeChanged
//                + "  ,remindPeriodChanged:" + remindPeriodChanged + "  ,addressChanged:" + addressChanged + "  ,tripModeChanged:" + tripModeChanged +
//                "  ,descriptionChanged:" + descriptionChanged + "  ,startDataChanged:" + startDataChanged + "  ,startTimeChanged:" + startTimeChanged + "  ,endDataChanged:" +
//                endDataChanged + "  ,endTimeChanged:" + endTimeChanged);

        if (titleChanged && allDayChanged && remindTypeChanged && remindPeriodChanged && addressChanged && tripModeChanged && descriptionChanged && startDataChanged && /*startTimeChanged &&*/ endDataChanged /*&& endTimeChanged*/) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private class MyTextWatcher implements TextWatcher {
        private int mMaxLength = 0;
        private AmigoEditText mEditText = null;
        private int start;
        private int count;

        public MyTextWatcher(AmigoEditText editText, int maxLength) {
            mEditText = editText;
            mMaxLength = maxLength;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.start = start;
            this.count = count;
            LogUtils.i(TAG, "onTextChanged....." + s.toString() + ",length:" + s.toString().length() + "  ,start:" + start + ",   before:" + before + "  ,count:" + count);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            LogUtils.i(TAG, "beforeTextChanged:" + s.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // Gionee sunyang 2017-01-14 modify for  GNSPR #64916 begin
            if (TextUtils.isEmpty(mEtTitle.getText()) && TextUtils.isEmpty(mDescription.getText())) {
                tv_save.setEnabled(true);

                //add by zjl at 2017-01-19 for setting button color begin
                tv_save.setTextColor(getResources().getColorStateList(R.color.new_self_creat_actionbar_save_gray_button_color));
            } else {
                tv_save.setEnabled(true);

                //add by zjl at 2017-01-19 for setting button color end
                tv_save.setTextColor(getResources().getColorStateList(R.color.new_self_creat_actionbar_save_button_color));
            }
            // Gionee sunyang 2017-01-14 modify for  GNSPR #64916 end

            if (TextUtils.isEmpty(editable.toString().trim())) {
                return;
            }
            LogUtils.i(TAG, "afterTextChanged....." + "  ,start:" + start + "  ,count:" + count);
            int length = editable.toString().length();
            int beforeLength = length - count;
            int leftInput = mMaxLength - beforeLength;
            int delStart = start + leftInput;
            int delEnd = start + count;
            if (length > mMaxLength) {
                LogUtils.i(TAG, "afterTextChanged...>lenght");
                Toast.makeText(SelfCreateScheduleActivity.this, getResources().getString(R.string.texttoolong), Toast.LENGTH_SHORT).show();
                editable.delete(delStart, delEnd);
                mEditText.setText(editable);
                mEditText.setSelection(delStart);
            }
        }
    }
}
