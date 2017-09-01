package com.gionee.secretary.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.presenter.CardDetailPresenter;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.ShareUtil;
import com.gionee.secretary.utils.WidgetUtils;
import com.gionee.secretary.ui.fragment.AirTicketDetailsFragment;
import com.gionee.secretary.ui.fragment.CreditCardDetailsFragment;
import com.gionee.secretary.ui.fragment.ExpressDetailsFragment;
import com.gionee.secretary.ui.fragment.HotelRoomDetailsFragment;
import com.gionee.secretary.ui.fragment.MovieTicketDetailsFragment;
import com.gionee.secretary.ui.fragment.SelfCreateScheduleDetailFragment;
import com.gionee.secretary.ui.fragment.TrainTicketDetailsFragment;
import com.gionee.secretary.ui.viewInterface.ICardDetailView;
import com.youju.statistics.YouJuAgent;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoProgressDialog;

public class CardDetailsActivity extends PasswordBaseActivity implements ICardDetailView {

    private static final String TAG = CardDetailsActivity.class.getSimpleName();

    private FragmentManager mFragmentManager;
    BaseSchedule mSchedule;
    SelfCreateSchedule selfCreateSchedule = null;
    AmigoProgressDialog progressDialog;
    Menu menu;
    private int deleteSelected = 1;
    private int mEventId;
    private CardDetailPresenter mCardDetailPresenter;
    private boolean isNotifation;
    SharedPreferences sharedPreferences;
    private MyHandler mHandler = new MyHandler(this);
    private static final int SHARE_SUCCESS = 1;
    private static final int SHARE_FAIL = 2;

    private static class MyHandler extends Handler {
        private final WeakReference<CardDetailsActivity> mActivity;

        public MyHandler(CardDetailsActivity activity) {
            mActivity = new WeakReference<CardDetailsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CardDetailsActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            if (activity.progressDialog != null && activity.progressDialog.isShowing()) {
                if (activity != null && !activity.isDestroyed()) {
                    activity.progressDialog.dismiss();
                }
            }
            switch (msg.what){
                case SHARE_SUCCESS:
                    ComponentName componentName = activity.getComponentName();
                    if (componentName != null && componentName.getClassName().equals(ShareUtil.getLauncherTopApp(activity))) {
                        File file = (File)msg.obj;
                        ShareUtil.shareIntent(file,activity);
                    }
                    break;
                case SHARE_FAIL:
                    Toast.makeText(activity, "分享的内容太长，分享失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getSchedule();
        if(mSchedule == null){
            Toast.makeText(this.getApplicationContext(),"该日程不存在",Toast.LENGTH_SHORT).show();
            finish();
        }
        isFromNotification();
        initView();
//        registerLongScreenShotReceiver();
    }

    @Override
    protected void onDestroy() {
        mCardDetailPresenter.detachView();
        super.onDestroy();
    }

    private void isFromNotification() {
        Intent intent = getIntent();
        if(intent == null)
            return;
        isNotifation = intent.getBooleanExtra(Constants.NOTIFACATION_FLAG, false);
        if (isNotifation) {
            YouJuAgent.onEvent(this, this.getResources().getString(R.string.click_notification));
        }
    }

    public BaseSchedule getSchedule() {
        if (mCardDetailPresenter == null) {
            mCardDetailPresenter = new CardDetailPresenter(this, this);
        }
        Intent intent = getIntent();
        if(intent == null){
            return null;
        }
        mSchedule = (BaseSchedule) intent.getSerializableExtra(Constants.SCHEDULE_KEY);
        if (mSchedule == null) {
            mEventId = sharedPreferences.getInt(Constants.LAST_EVENT_ID_KEY, 0);
            mSchedule = mCardDetailPresenter.getScheduleById(mEventId);
        }
        //是否是从语音进入
        boolean isVoice = intent.getBooleanExtra("voice_flag",false);
        if(isVoice){
            mEventId = intent.getIntExtra("eventid",0);
            mSchedule = mCardDetailPresenter.getScheduleById(mEventId);
        }
        //是否是从智能助手进入
        boolean isTYAssistant = intent.getBooleanExtra("assistant",false);
        if(isTYAssistant){
            mEventId = intent.getIntExtra("scheduleId",0);
            mSchedule = mCardDetailPresenter.getScheduleById(mEventId);
        }
        return mSchedule;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mSchedule != null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(Constants.LAST_EVENT_ID_KEY, mSchedule.getId());
            editor.commit();
        }
        super.onPause();
    }

    private void initView() {
        initActionBar();
//        initStatusBar();
        Fragment fragment = setFragment();
        mFragmentManager = getFragmentManager();
        if(fragment == null)
            return;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.frame_content, fragment);
        transaction.commit();
    }

    private void showProgressDialog() {
        progressDialog = new AmigoProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在生成图片...");
        progressDialog.show();
    }

    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        //mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.card_detail_activity_actionbar_color)));
        View actionBarLayout = getLayoutInflater().inflate(
                R.layout.actionbar_card_detail, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        ImageView btn_share = (ImageView) actionBarLayout.findViewById(R.id.btn_share);
        if(DisplayUtils.isFullScreen()){
            btn_share.setImageResource(R.drawable.share_btn_full);
        }else {
            btn_share.setImageResource(R.drawable.share_btn);
        }
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gionee sunyang 2017-01-16 modify for GNSPR #64960 begin
                if (isNotifation) {
                    onBackPressed();
                } else {
                    //火车卡片，时刻表已打开时，按后退返回火车卡片详情界面  (Fix GNSPR #65732 by liyh)
                    if (mSchedule != null && mSchedule.getType() == Constants.TRAIN_TYPE) {
                        TrainTicketDetailsFragment trainFragment = (TrainTicketDetailsFragment) getCurrentFragment();
                        if (trainFragment.isTrainTimeTableLoad()) {
                            trainFragment.hideTrainTimeTable();
                            return;
                        }
                    }
                    finish();
                }
            }
        });
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YouJuAgent.onEvent(CardDetailsActivity.this, CardDetailsActivity.this.getResources().getString(R.string.click_share));
                ShareUtil shareUtil = new ShareUtil();
                //added by luorw for 调用长截屏进行分享 2017-04-15
//                if(mSchedule.getType() == Constants.TRAIN_TYPE) {
//                    TrainTicketDetailsFragment trainFragment = (TrainTicketDetailsFragment)getCurrentFragment();
//                    if(trainFragment.isTrainTimeTableLoad()) {
//                        Intent intent = new Intent(Constants.LONG_SCREEN_SHOT_START);
//                        intent.putExtra("package_name","com.gionee.secretary");
//                        sendBroadcast(intent);
//                    }else{
//                        shareUtil.shareSchedule(CardDetailsActivity.this);
//                    }
//                }else
                if(mSchedule == null)
                    return;
                if (mSchedule.getType() == Constants.EXPRESS_TYPE) {
                    ExpressDetailsFragment expressFragment = (ExpressDetailsFragment) getCurrentFragment();
                    showProgressDialog();
                    TakeBitmapThread thread = new TakeBitmapThread(expressFragment.getmScrollView(), CardDetailsActivity.this);
                    thread.start();
                } else if (mSchedule.getType() == Constants.SELF_CREATE_TYPE) {
                    SelfCreateScheduleDetailFragment fragment = (SelfCreateScheduleDetailFragment) getCurrentFragment();
                    showProgressDialog();
                    TakeBitmapThread thread = new TakeBitmapThread(fragment.getmScrollView(), CardDetailsActivity.this);
                    thread.start();
                } else {
                    showProgressDialog();
                    TakeScreenThread thread = new TakeScreenThread(CardDetailsActivity.this);
                    thread.start();
                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gionee sunyang 2017-01-16 modify for GNSPR #64960 begin
                if (isNotifation) {
                    onBackPressed();
                } else {
                    //火车卡片，时刻表已打开时，按后退返回火车卡片详情界面  (Fix GNSPR #65732 by liyh)
                    if (mSchedule != null && mSchedule.getType() == Constants.TRAIN_TYPE) {
                        TrainTicketDetailsFragment trainFragment = (TrainTicketDetailsFragment) getCurrentFragment();
                        if (trainFragment.isTrainTimeTableLoad()) {
                            trainFragment.hideTrainTimeTable();
                            return;
                        }
                    }
                    finish();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
                // Gionee sunyang 2017-01-16 modify for GNSPR #64960 end
            }
        });
        mActionBar.setCustomView(actionBarLayout);
        mActionBar.show();
    }

//    private void initStatusBar() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.statusbar_color));
//            window.setNavigationBarColor(Color.TRANSPARENT);
//        }
//    }

    //added by luorw for GNSPR #60206 20161207 begin
    @Override
    public void onBackPressed() {
        if (isNotifation) {
            super.onBackPressed();
        } else {
            if (mSchedule != null && mSchedule.getType() == Constants.TRAIN_TYPE) {
                //火车卡片，时刻表已打开时，按后退键返回火车卡片详情界面  (Fix GNSPR #65732 by liyh)
                TrainTicketDetailsFragment trainFragment = (TrainTicketDetailsFragment) getCurrentFragment();
                if (trainFragment.isTrainTimeTableLoad()) {
                    trainFragment.hideTrainTimeTable();
                    return;
                }
            }
            super.onBackPressed();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    /**
     * Get Current Fragment in FrameLayout Container
     * 取得当前container中的Fragment对象
     *
     * @return Instance of current fragment 当前Fragment对象
     */
    private Fragment getCurrentFragment() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.frame_content);
        return currentFragment;
    }

    private static class TakeScreenThread extends Thread{
        private WeakReference<CardDetailsActivity> reference;
        public TakeScreenThread(CardDetailsActivity mActivity) {
            this.reference = new WeakReference<CardDetailsActivity>(mActivity);
        }

        @Override
        public void run() {
            final CardDetailsActivity activity = reference.get();
            if(activity != null){
                SystemClock.sleep(500);//不让闪太快，显示进度过程
                Bitmap bitmap = ShareUtil.takeScreenShot(activity);
                if (bitmap != null) {
                    String path = "/sdcard/Android/data/" + activity.getApplicationInfo().packageName + "/";
                    String fileName = System.currentTimeMillis() + ".png";
                    File mFile = new File(path, fileName);
                    File parentFile = mFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    ShareUtil.savePic(bitmap, mFile, path);
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_SUCCESS,mFile));
                } else {
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_FAIL));
                }
            }
        }
    }

    private static class TakeBitmapThread extends Thread{
        private WeakReference<CardDetailsActivity> reference;
        ScrollView mView;
        Bitmap bitmap = null;
        public TakeBitmapThread(ScrollView view,CardDetailsActivity mActivity) {
            this.reference = new WeakReference<CardDetailsActivity>(mActivity);
            mView = view;
            bitmap = ShareUtil.getBitmapByView(mView);
        }

        @Override
        public void run() {
            final CardDetailsActivity activity = reference.get();
            if(activity != null){
                SystemClock.sleep(500);//不让闪太快，显示进度过程
                if (bitmap != null) {
                    String path = "/sdcard/Android/data/" + activity.getApplicationInfo().packageName + "/";
                    String fileName = System.currentTimeMillis() + ".png";
                    File mFile = new File(path, fileName);
                    File parentFile = mFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    Bitmap compressBp = ShareUtil.compressImage(bitmap);
                    bitmap.recycle();
                    bitmap = null;
                    ShareUtil.savePic(compressBp, mFile, path);
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_SUCCESS,mFile));
                }else {
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_FAIL));
                }
            }
        }
    }

    // Gionee sunyang 2017-01-16 modify for GNSPR #64960 end
    //added by luorw for GNSPR #60206 20161207 end
    private Fragment setFragment() {
        Fragment frgmt = null;
        if(mSchedule != null){
            int cardType = mSchedule.getType();
            switch (cardType) {
                case Constants.SELF_CREATE_TYPE:
                    frgmt = SelfCreateScheduleDetailFragment.newInstance();
                    break;
                case Constants.BANK_TYPE:
                    frgmt = CreditCardDetailsFragment.newInstance();
                    break;
                case Constants.TRAIN_TYPE:
                    frgmt = TrainTicketDetailsFragment.newInstance();
                    break;
                case Constants.FLIGHT_TYPE:
                    frgmt = AirTicketDetailsFragment.newInstance();
                    break;
                case Constants.MOVIE_TYPE:
                    frgmt = MovieTicketDetailsFragment.newInstance();
                    break;
                case Constants.HOTEL_TYPE:
                    frgmt = HotelRoomDetailsFragment.newInstance();
                    break;
                case Constants.EXPRESS_TYPE:
                    frgmt = ExpressDetailsFragment.newInstance((ExpressSchedule) mSchedule);
                    break;
                default:
                    break;
            }
        }
        return frgmt;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        MenuItem menuDelete = menu.findItem(R.id.detail_delete);
        MenuItem menuEdit = menu.findItem(R.id.detail_edit);
        if (mSchedule != null && mSchedule.getType() == Constants.SELF_CREATE_TYPE) {
            menuDelete.setVisible(true);
            menuEdit.setVisible(true);
        } else {
            menuDelete.setVisible(true);
            menuEdit.setVisible(false);
        }

        if(DisplayUtils.isFullScreen()){
            //全面屏
            menuDelete.setIcon(R.drawable.gn_private_delete_enable_full);
            menuEdit.setIcon(R.drawable.edit_enable_full);
        }else {
            menuDelete.setIcon(R.drawable.gn_private_delete_enable);
            menuEdit.setIcon(R.drawable.edit_enable);
        }

        if (mSchedule != null && mSchedule.getType() == Constants.TRAIN_TYPE) {
            TrainTicketDetailsFragment fragment = (TrainTicketDetailsFragment) getCurrentFragment();
            //火车卡片打开时刻表时,Menu删除按钮需要隐藏  (Fix bug GNSPR #66024 by liyh)
            Boolean menuVisible = !fragment.isTrainTimeTableLoad();
            menuDelete.setVisible(menuVisible);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mSchedule == null)
            return false;
        if (ScheduleInfoDao.getInstance(this).getScheduleInfoById(mSchedule.getId()) == null) {
            Toast.makeText(this, getString(R.string.card_not_exist), Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.detail_delete:
                deleteCard();
                YouJuAgent.onEvent(this, this.getResources().getString(R.string.click_delete_schedule));
                break;
            case R.id.detail_edit:
                editCard();
                YouJuAgent.onEvent(this, this.getResources().getString(R.string.click_edit_schedule));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isRepeateEvent() {
        boolean mIsRepeateEvent = false;
        if (mSchedule != null && mSchedule.getType() == Constants.SELF_CREATE_TYPE) {
            selfCreateSchedule = (SelfCreateSchedule) mSchedule;
            if (!"一次".equals(selfCreateSchedule.getRemindPeriod())) {
                mIsRepeateEvent = true;//自建日程 非一次性事件
            } else {
                mIsRepeateEvent = false;//自建日程 一次性事件
            }
        } else {
            mIsRepeateEvent = false;//短信解析日程
        }
        return mIsRepeateEvent;
    }


    private DialogInterface.OnClickListener mDeleteRepeatingDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            progressDialog = new AmigoProgressDialog(CardDetailsActivity.this);
            progressDialog.show();
            switch (deleteSelected) {
                case 0:
                    new DeleteRepeadScheduleThread(CardDetailsActivity.this,selfCreateSchedule, true).start();
                    break;
                case 1:
                    new DeleteRepeadScheduleThread(CardDetailsActivity.this,selfCreateSchedule, false).start();
                    break;
            }

        }
    };

    private DialogInterface.OnClickListener mSingleChoiceListListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            deleteSelected = which;
        }
    };

    public void deleteCard() {
        ArrayList<String> labelArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.delete_repeating_labels)));
        if (isRepeateEvent()) {
            String[] items = new String[]{labelArray.get(0), labelArray.get(1)};
            new AmigoAlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_recurring_event_title, selfCreateSchedule.getTitle()))
                    // Gionee liyu 2017-02-08 modify for GNSPR #60011 begin
//                    .setSingleChoiceItems(items, deleteSelected, mSingleChoiceListListener)
                    .setPositiveButton(R.string.sec_ok, mDeleteRepeatingDialogListener)
                    .setMessage(labelArray.get(1))
                    // Gionee liyu 2017-02-08 modify for GNSPR #60011 end
                    .setNegativeButton(R.string.cancel, null).show();

        } else {

            //删除普通日程
            //modify by zhengjl for delete self creat shdule begin
            new AmigoAlertDialog.Builder(this)
                    .setTitle(this.getResources().getString(R.string.delete))
                    .setMessage(this.getResources().getString(R.string.delete_schedule_or_not))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread deleteScheduleThread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    mCardDetailPresenter.deleteScheduleById(mSchedule);
                                }
                            });
                            deleteScheduleThread.start();
                        }
                    }).show();
            //modify by zhengjl for delete self creat shdule end

        }
    }

    @Override
    public void deleteSuccess(boolean isRepeatSchedule) {
        if (isRepeatSchedule) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            CardDetailsActivity.this.setResult(110, new Intent());
        } else {
            Toast.makeText(this, this.getResources().getString(R.string.delete_success), Toast.LENGTH_LONG).show();
        }
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        WidgetUtils.updateWidget(this);
    }

    @Override
    public void showCardDetail(Intent data) {
        mSchedule = (BaseSchedule) data.getSerializableExtra(Constants.SCHEDULE_KEY);
        int id = data.getIntExtra(Constants.SCHEDULE_ID_KEY, -1);
        if(mSchedule == null)
            return;
        if (id != -1) {
            mSchedule.setId(id);
        }
        Fragment fragment = SelfCreateScheduleDetailFragment.newInstanceForUpdate(mSchedule);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.frame_content, fragment);
        transaction.commit();
        WidgetUtils.updateWidget(this);
    }

    @Override
    public void editCard() {
        if(mSchedule == null)
            return;
        Intent intent = new Intent();
        intent.setClass(this, SelfCreateScheduleActivity.class);
        intent.putExtra(Constants.SCHEDULE_ID_KEY, mSchedule.getId());
        intent.putExtra(Constants.IS_REPEAT_EVENT, isRepeateEvent());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(intent, 1);
    }

    private static class DeleteRepeadScheduleThread extends Thread {
        BaseSchedule baseSchedule;
        boolean fromToday;
        private final WeakReference<CardDetailsActivity> mActivity;
        public DeleteRepeadScheduleThread(CardDetailsActivity cardDetailsActivity,BaseSchedule mBaseSchedule, boolean mFromToday) {
            mActivity = new WeakReference<CardDetailsActivity>(cardDetailsActivity);
            baseSchedule = mBaseSchedule;
            fromToday = mFromToday;
        }

        @Override
        public void run() {
            CardDetailsActivity activity = mActivity.get();
            if(activity != null){
                RemindUtils.deletePeriodScheduleAlarm(activity, baseSchedule, fromToday);
                activity.mCardDetailPresenter.deleteScheduleRepeatAll(baseSchedule, fromToday);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            showCardDetail(data);
        }
    }
}
