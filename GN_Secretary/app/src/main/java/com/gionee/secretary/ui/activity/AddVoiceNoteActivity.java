package com.gionee.secretary.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoProgressDialog;
import amigoui.widget.AmigoDateTimePickerDialog;
import amigoui.widget.AmigoTextView;

import com.gionee.secretary.R;
import com.gionee.secretary.listener.NoDoubleClickListener;
import com.gionee.secretary.module.ICallStatusListener;
import com.gionee.secretary.module.TeleListener;
import com.gionee.secretary.bean.RecordBean;
import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.module.SoundPlayer;
import com.gionee.secretary.module.SoundRecorder;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.ImageLoader;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NetWorkUtil;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.StorageUtils;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.voice.PcmRecordListener;
import com.gionee.secretary.voice.PcmRecorder;
import com.gionee.secretary.voice.PropertiesHelper;
import com.gionee.secretary.widget.BillImageSpan;
import com.gionee.secretary.widget.GNDateTimeDialog;
import com.gionee.secretary.widget.ListenerInputView;
import com.gionee.secretary.widget.NoteEditText;
import com.gionee.secretary.widget.PhotoImageSpan;
import com.gionee.secretary.widget.SoundImageSpan;
import com.iflytek.recinbox.sdk.AppLoader;
import com.iflytek.recinbox.sdk.speech.impl.LybMscRecognizer;
import com.iflytek.recinbox.sdk.speech.impl.MscSampleRate;
import com.iflytek.recinbox.sdk.speech.interfaces.IMscRecognitionListener;
import com.youju.statistics.YouJuAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by hangh on 6/4/16.
 */
public class AddVoiceNoteActivity extends PasswordBaseActivity implements IMscRecognitionListener, PcmRecordListener, ListenerInputView.OnKeyBoardStateChangeListener,
        SoundRecorder.TakeSoundRecorderListener, NoteEditText.onSelectionChangedListener, ICallStatusListener {
    private static final String TAG = "AddVoiceNoteActivity";
    private static final int MAXTEXTLENGTH = 10000;
    private static final int DEFAULT_ZERO = 0;
    private AmigoProgressDialog pd;
    private EditText etDate;
    private ProgressBar progressBar;
    private NoteEditText etContent;
    private ListenerInputView mListenerInputView;
    // ImageView note_delete;
    private AmigoTextView mCancelBtn;
    private AmigoTextView mSaveBtn;
    VoiceNoteBean voiceNoteBean = null;
    long createtime = 0;
    int noteid = 0;
    // private boolean isLongVolumeAdd = false;

    private static final int SAVE_OK = 1;
    /* add by zhengjl at 2017-2-5 for GNSPR #66225 not end */
    private static final int DELETE_OK = -1;
    private static final int INSERT_PHOTO_SUCCESS = 2;
    private static final int INSERT_CAMERA_SUCCESS = 3;
    private static final int INSERT_PHOTO_FAIL = 4;
    private static final int PHOTO_IS_TOOLONG = 5;
    private static final int REFRESH_DATE = 6;
    private static final int ADD_CONTENT_LIST = 7;
    private static final int CLEAR_BLANK = 8;
    private Context mContext;

    private static final int VOICE_PARSE_SUCCESS = 0;
    private static final long MAX_STORAGE = 200 * 1024 * 1024;//200M
    ImageView mStartVoice;
    private final String PATH = Environment.getExternalStorageDirectory() + "/secretary/实时转写/";
    MscSampleRate sampleRate = MscSampleRate.SAMPLE_RATE_16K;
    LybMscRecognizer mLybMscRecognizer;
    String mRequestedType = "audio/3gpp";
    PcmRecorder mRecord;
    StringBuilder contentVoice = null;
    String parseResult = "";
    AnimationDrawable ad;
    private int mRecordLastResultLength = 0;
    private int mRecordingState = RECORDING_OFF;
    private static final int RECORDING_ON = 0;
    private static final int RECORDING_OFF = 1;
    String createTime = "";
    private RelativeLayout mIvTips;
    private RelativeLayout mTipsLayer;
    private ImageButton mTipsPoint;
    private AmigoTextView mSelectBill;
    private AmigoTextView mSelectCamera;
    private AmigoTextView mSelectReminder;
    private AmigoTextView mSelectRecord;
    private ImageView mTipsBill;
    private ImageView mTipsCamera;
    private ImageView mTipsReminder;
    private ImageView mTipsRecord;
    private ImageView mTipsVoiceInput;
    private List<View> mTipsList;
    private String[] cameras = new String[]{"照片图库", "拍照"};
    // private boolean mIsShowDateTimeDialog = false;
    private GNDateTimeDialog mDateTimeDialog;
    AmigoAlertDialog dialog;

    // 记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    InputMethodManager imm;
    SoundRecorder mRecorder;
    private LinearLayout mNoteRemindTimeLayout;
    private TextView mNoteRemindTimeTv;
    private static final String SP_INIT_KEY = "AddVoiceNoteActivity.sp.isInit";
    private static final int BILL_LENGHT = Constants.PATH_BILL_UNCHECKED_IMG.length() + Constants.URI_END_TAG_LENGTH;
    private static final String PATH_SOUND_PATH = "";
    private Bitmap bm_bill_unchecked;
    private Bitmap bm_bill_checked;
    private static final int MAX_LENGTH = 10000;
    private boolean isBillSelected;

    private static final String CAMERA_PACKAGE = "com.android.camera";
    private static final String CAMERA_CLASS = "com.android.camera.sdk.activity.CaptureActivity";

    /* modify by zhengjl at 2017-2-14 优化备忘图片加载 not begin */
    //private ImageLoader imageLoader;
    private List<RecordBean> recordBeanList;
    private static final int MAX_LENGTH_IMAGESPAN = 80;
    private WakeLock mWakeLock;
    private boolean isInCall;
    private String mImgPath;
    private File mImageDir;
    private static String currentImagePath;
    private static final String CURRENT_CLASSNAME = "com.gionee.secretary.view.AddVoiceNoteActivity";
    // StrikethroughSpan;
    // private static final String PATH_STRIKE_THROUGH = "/data"
    // added by luorw for GNSPR #72852 2017-03-17 begin
    private boolean isFromPickImage = false;
    // added by luorw for GNSPR #72852 2017-03-17 end
    private NotificationManager mNotificationManager = null;
    private static final int NOTIFICATION_VOICE_ID = 0;
    private SoundPlayer mSoundPlayer;
    private List<Object> mDataList = new ArrayList<>();
    private TelephonyManager mTelephonyMgr;
    private TeleListener mTeleListener;
    ImageLoader loader;
    private boolean mShowDialog = false;//是否显示录音对话框.
    private final MyHandler mHandler = new MyHandler(this);
    private String mReadPhoneStatePermissions = Manifest.permission.READ_PHONE_STATE;//请求的权限
    private static final int PERMISSIONS_REQUEST_READ_PHONE = 100;
    private String mReadExternalStoragePermissions = Manifest.permission.READ_EXTERNAL_STORAGE;//请求的权限
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
    private String mRecordAudioPermissions = Manifest.permission.RECORD_AUDIO;//请求的权限
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO_AND_PHONE = 102;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 103;
    private static final int PERMISSIONS_REQUEST_LYBMSC = 104;
    private boolean click_flag = false;//是否是点击语音转文字


    private static class MyHandler extends Handler {
        private final WeakReference<AddVoiceNoteActivity> mActivity;

        public MyHandler(AddVoiceNoteActivity activity) {
            mActivity = new WeakReference<AddVoiceNoteActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AddVoiceNoteActivity addVoiceNoteActivity = mActivity.get();
            if (addVoiceNoteActivity == null) {
                return;
            }
            switch (msg.what) {
                case VOICE_PARSE_SUCCESS:
                    String result = (String) msg.obj;
                    // mContentTxt.setText(result);
                    LogUtils.e(TAG, "contentVoice = " + addVoiceNoteActivity.contentVoice);
                    LogUtils.e(TAG, "result = " + result);
                    // etContent.setText(contentVoice + result);
                    String trimResult = result.substring(addVoiceNoteActivity.mRecordLastResultLength);
                    addVoiceNoteActivity.mRecordLastResultLength = result.length();
                    addVoiceNoteActivity.etContent.append(trimResult);
                    addVoiceNoteActivity.etContent.setSelection(addVoiceNoteActivity.etContent.length());
                    break;
                case SAVE_OK:
                    Toast.makeText(addVoiceNoteActivity.getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    if (addVoiceNoteActivity.pd != null) {
                        addVoiceNoteActivity.pd.dismiss();
                    }
                    addVoiceNoteActivity.sendBroadcast(new Intent(Constants.REFRESH_FOR_NOTE_DETAIL_UI));
                    // add by zhengjl at 2017-2-22 for 详情页显示错位 not end
                    addVoiceNoteActivity.finish();
                    // add by zhengjl at 2017-2-22 for 详情页显示错位 end
                    addVoiceNoteActivity.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    break;
            /* add by zhengjl at 2017-2-5 for GNSPR #66225 not end */
                case DELETE_OK:
                    Toast.makeText(addVoiceNoteActivity, "保存成功", Toast.LENGTH_SHORT).show();
                    if (addVoiceNoteActivity.pd != null) {
                        addVoiceNoteActivity.pd.dismiss();
                    }
                    Intent intent = new Intent();
                    intent.setClass(addVoiceNoteActivity, CalendarActivity.class);
                    addVoiceNoteActivity.startActivity(intent);
                    break;
            /* add by zhengjl at 2017-2-5 for GNSPR #66225 end */
                case INSERT_PHOTO_SUCCESS:
                    String path = msg.getData().getString("path");
                    LogUtils.i(TAG, "INSERT_PHOTO_SUCCESS , path = " + path);
                    addVoiceNoteActivity.insertBitmap((Bitmap) msg.obj, path);
                    break;
                case INSERT_CAMERA_SUCCESS:
                    String path2 = msg.getData().getString("path");
                    LogUtils.i(TAG, "INSERT_CAMERA_SUCCESS , path2 = " + path2);
                    // 插入图片
                    addVoiceNoteActivity.insertBitmap((Bitmap) msg.obj, Constants.FILE_SCHEME + path2);
                    break;
                case INSERT_PHOTO_FAIL:
                    Toast.makeText(addVoiceNoteActivity, "文件已损坏，插入图片失败!", Toast.LENGTH_SHORT).show();
                    break;
                case PHOTO_IS_TOOLONG:
                    LogUtils.i(TAG, "PHOTO_IS_TOOLONG");
                    Toast.makeText(addVoiceNoteActivity, "图片长度太长，无法插入！", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_DATE:
                    addVoiceNoteActivity.etDate.setText((String) msg.obj);
                    break;
                case ADD_CONTENT_LIST:
                    addVoiceNoteActivity.appendDateList();
                    //voiceNoteBean.setContent(etContent.getText().toString());
                    //mProgressBar.setVisibility(View.GONE);
                    addVoiceNoteActivity.etContent.setVisibility(View.VISIBLE);
                    addVoiceNoteActivity.progressBar.setVisibility(View.GONE);
                    addVoiceNoteActivity.setSaveBtnStatus(true);
                    addVoiceNoteActivity.etContent.requestFocus();
                    if (!addVoiceNoteActivity.mListenerInputView.getisShowingSoftInput()) {
                        addVoiceNoteActivity.imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    break;
                case CLEAR_BLANK:
                    addVoiceNoteActivity.etContent.getText().clear();
                    break;

            }
        }
    }


    private void appendDateList() {
        if (mDataList.size() == 0) {
            return;
        }
        for (int i = 0; i < mDataList.size(); i++) {
            Object date = mDataList.get(i);
            if (date instanceof SpannableString) {
                etContent.append((SpannableString) date);
            } else {
                etContent.append(date.toString());
            }
        }
//        etContent.setHighlightColor(Color.TRANSPARENT);//消除点击时的背景色
//        etContent.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public void setmShowDialog(boolean mShowDialog) {
        this.mShowDialog = mShowDialog;
    }

    public boolean ismShowDialog() {
        return mShowDialog;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        noteid = getIntent().getIntExtra(Constants.RemindConstans.NOTE_KEY, 0);
        // isLongVolumeAdd = getIntent().getBooleanExtra("isLongVolumeAddEvent",
        // false);
        contentVoice = new StringBuilder();
        mContext = this;
        // if(isLongVolumeAdd){
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        // | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_voice_note_add);
        AppLoader.setSdkLogcat(true);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initBillImageSrc();
        initViews();
        initListener();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            loadData();
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO
            }, PERMISSIONS_REQUEST_LYBMSC);
            click_flag = false;
        } else {
            initLybMscRecognizer();
        }
        initmImageDir();
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTeleListener = TeleListener.getInstance();
        mTeleListener.registerCallStatusListener(this);
        mTelephonyMgr.listen(mTeleListener, PhoneStateListener.LISTEN_CALL_STATE);
        registerNetWorkReceiver();
        mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    // Fixed #76871 by liyu start;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("fromNotificarion", false)) return;
        saveOnNewIntent();
        noteid = intent.getIntExtra(Constants.RemindConstans.NOTE_KEY, 0);
        etContent.getEditableText().clear();
        loadData();
    }
    // Fixed #76871 by liyu end;

    //added by luorw for S10 终端项目Bug #92997 2017/03/30 begin
    private void initLybMscRecognizer() {
        try {
            LybMscRecognizer.setMscId(PropertiesHelper.getInstance(this).getAppid());
            mLybMscRecognizer = LybMscRecognizer.getInstance(getApplicationContext());
            mLybMscRecognizer.initialize();
            mLybMscRecognizer.setMscRecognitionListener(this);
        } catch (Exception e) {
            LogUtils.e("luorw", "Exception = " + e.getMessage());
            finish();
            Toast.makeText(getApplicationContext(),"退出语音转文字界面，需要重新进入",Toast.LENGTH_SHORT).show();
        }
    }


    private void requestPermission() {
        boolean permission = ActivityCompat.checkSelfPermission(this, mReadPhoneStatePermissions)
                == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= 23 && !permission) {
            ActivityCompat.requestPermissions(this, new String[]{mReadPhoneStatePermissions}, PERMISSIONS_REQUEST_READ_PHONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_READ_PHONE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//授权成功
                        LogUtils.e("luorw", "onRequestPermissionsResult = 授权成功");
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {//点击拒绝授权
                        LogUtils.e("luorw", "onRequestPermissionsResult = 点击拒绝授权");
                        System.exit(0);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        loadData();
                    }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                        finish();
                        Toast.makeText(getApplicationContext(),"没有读取存储权限,故退出这个界面哦",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PERMISSIONS_REQUEST_LYBMSC:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        initLybMscRecognizer();
                        if(click_flag){
                            startVoiceEnginee();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"没有权限,无法语音转文字哦",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO_AND_PHONE:
                if(grantResults.length > 0){
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        closeBackgroundMusicIfNeed();
                        selectRecord();
                    }else {
                        Toast.makeText(this,"没有录音和读取电话权限",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        initLybMscRecognizer();
                        startVoiceEnginee();
                    } else {
                        Toast.makeText(this,"没有录音权限",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }
    //added by luorw for S10 终端项目Bug #92997 2017/03/30 end

    private void initmImageDir() {
        mImgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/secretary/noteImage/";
        mImageDir = new File(mImgPath);
        if (!mImageDir.isDirectory()) {
            mImageDir.mkdirs();
        }
    }

    private void initBillImageSrc() {
        bm_bill_unchecked = BitmapFactory.decodeResource(getResources(), R.drawable.voice_note_item_normal);
        bm_bill_checked = BitmapFactory.decodeResource(getResources(), R.drawable.voice_note_item_selected);
    }

    @Override
    public void onIdle() {
        LogUtils.e(TAG, "CALL_STATE_IDLE");
        isInCall = false;
    }

    @Override
    public void onOffHook() {
        if (mRecorder != null && mRecorder.getRecorderState()) {
            mRecorder.completeRecorder();
        }
        if (mRecordingState == RECORDING_ON) {
            stopRecording();
        }
        if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
            mSoundPlayer.pausePlayer();
        }
        isInCall = true;
    }

    @Override
    public void onRinging() {
        LogUtils.e(TAG, "CALL_STATE_RINGING");
        if (mRecordingState == RECORDING_ON) {
            stopRecording();
        }
        isInCall = true;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStart() {
        if (PasswordModel.getInstance(this).getLockState()) {
            if (isFromPickImage) {
                LogUtils.i("luorw", "addvoicenote--------onStart----解除密码锁");
                PasswordModel.getInstance(mContext).updateLockState(false);
                isFromPickImage = false;
            }
        }
        if (mRecordingState == RECORDING_ON) {
            etContent.clearFocus();
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
		/*if (mRecordingState == RECORDING_ON) {
			stopRecording();
		}*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 录音功能。当Activity不在前台时停止录音并保存。 Fix bug GNSPR #68447。
        if (mRecorder != null && mRecorder.getRecorderState()) {
            mRecorder.completeRecorder();
        }
        //abandonAudioFocus();
        mAm.abandonAudioFocus(afChangeListener);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mSoundPlayer!=null && mSoundPlayer.isPlaying()){
            LogUtils.i("SoundPlayer" , "onDestroy");
            mSoundPlayer.completePlayer();
        }
        if (loader != null) {
            loader.recycle();
        }
        if (null != mRecord && mRecord.isRecording()) {
            mRecord.stopRecording();
            mRecord.release();
            cancelNotificationForVoice();
        }
        if (mLybMscRecognizer != null) {
            mLybMscRecognizer.abortRecognize();
            mLybMscRecognizer.release();
        }
        parseResult = "";
        unRegisterNetWorkReceiver();
        mTelephonyMgr.listen(mTeleListener, PhoneStateListener.LISTEN_NONE);
        mTeleListener.unRegisterCallStatusListener(this);
        etContent.getText().clearSpans();
        Runtime.getRuntime().gc();
    }

    private void initViews() {
        //initActionBar();
        getAmigoActionBar().hide();
        etDate = (EditText) findViewById(R.id.create_time_title);
        etContent = (NoteEditText) findViewById(R.id.note_edit);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mStartVoice = (ImageView) findViewById(R.id.start_voice);
        mSelectBill = (AmigoTextView) findViewById(R.id.action_bill);
        mSelectCamera = (AmigoTextView) findViewById(R.id.action_camera);
        mSelectReminder = (AmigoTextView) findViewById(R.id.action_reminder);
        mSelectRecord = (AmigoTextView) findViewById(R.id.action_record);
        mCancelBtn = (AmigoTextView) findViewById(R.id.cancel_btn);
        mSaveBtn = (AmigoTextView) findViewById(R.id.save_btn);
        ad = (AnimationDrawable) mStartVoice.getBackground();
        mListenerInputView = (ListenerInputView) findViewById(R.id.listenerInputView);
        mListenerInputView.setOnKeyBoardStateChangeListener(this);
        etContent.addTextChangedListener(new MyTextWatcher());
        etContent.setListener(this);
        etContent.requestFocus();
        mRecordingState = RECORDING_OFF;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // mIvTips = (RelativeLayout) findViewById(R.id.tips);
        mTipsPoint = (ImageButton) findViewById(R.id.tips_point);
        mNoteRemindTimeLayout = (LinearLayout) findViewById(R.id.note_remind_time_layout);
        mNoteRemindTimeTv = (TextView) findViewById(R.id.note_remind_time_tv);

        // Tips Item
        // mTipsBill = (ImageView)findViewById(R.id.tips_bill);
        // mTipsCamera = (ImageView)findViewById(R.id.tips_camera);
        // mTipsReminder = (ImageView) findViewById(R.id.tips_reminder);
        // mTipsRecord = (ImageView) findViewById(R.id.tips_voice_record);
        // mTipsVoiceInput = (ImageView) findViewById(R.id.tips_voice_input);
        // mTipsList = new ArrayList<>();
        // mTipsList.add(mTipsBill);
        // mTipsList.add(mTipsCamera);
        // mTipsList.add(mTipsReminder);
        // mTipsList.add(mTipsRecord);
        // mTipsList.add(mTipsVoiceInput);
        mTipsLayer = (RelativeLayout) findViewById(R.id.tips_layer);
        //ShowTipsUtil.showTips(this, mTipsLayer, null, mTipsPoint, Constants.TIPS_FOR_NEW_NOTE);
    }

    private void initListener() {
        mSelectBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MAX_LENGTH - etContent.getText().length() <= MAX_LENGTH_IMAGESPAN) {
                    Toast.makeText(AddVoiceNoteActivity.this, "已不能添加待办", Toast.LENGTH_SHORT).show();
                    return;
                }
                YouJuAgent.onEvent(AddVoiceNoteActivity.this, AddVoiceNoteActivity.this.getResources().getString(R.string.click_note_bill));
                setBill();
            }
        });
        mSelectCamera.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (MAX_LENGTH - etContent.getText().length() <= MAX_LENGTH_IMAGESPAN) {
                    Toast.makeText(AddVoiceNoteActivity.this, "已不能添加图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*Gionee <gn_by_sw> <zhengyt> <2017-08-11> add for #96407 begin*/
                imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
                /*Gionee <gn_by_sw> <zhengyt> <2017-08-11> add for #96407 end*/
                YouJuAgent.onEvent(AddVoiceNoteActivity.this, AddVoiceNoteActivity.this.getResources().getString(R.string.click_note_camera));
                selectCamera();
            }
        });
        mSelectReminder.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                YouJuAgent.onEvent(AddVoiceNoteActivity.this, AddVoiceNoteActivity.this.getResources().getString(R.string.click_note_alert));
                setNoteReminder();
            }
        });
        mSelectRecord.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (MAX_LENGTH - etContent.getText().length() <= MAX_LENGTH_IMAGESPAN) {
                    Toast.makeText(AddVoiceNoteActivity.this, "已不能添加录音", Toast.LENGTH_SHORT).show();
                    return;
                }
                long romSize = StorageUtils.getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
                if (romSize < MAX_STORAGE) {
                    Toast.makeText(AddVoiceNoteActivity.this, "存储空间不足，录音失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*Gionee <gn_by_sw> <zhengyt> <2017-08-11> add for #96407 begin*/
                imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
                /*Gionee <gn_by_sw> <zhengyt> <2017-08-11> add for #96407 end*/
                YouJuAgent.onEvent(AddVoiceNoteActivity.this, AddVoiceNoteActivity.this.getResources().getString(R.string.click_note_record));
                TelecomManager tm = (TelecomManager) getSystemService(TELECOM_SERVICE);
                // 判断是否处于通话状态 Fix bug GNSPR #68447
                if(ContextCompat.checkSelfPermission(AddVoiceNoteActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(AddVoiceNoteActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddVoiceNoteActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_RECORD_AUDIO_AND_PHONE);
                } else {
                    if (tm.isInCall()) {
                        Toast.makeText(AddVoiceNoteActivity.this, "打电话时不能启动录音", Toast.LENGTH_SHORT).show();
                    } else if(!tm.isInCall()) {
                        closeBackgroundMusicIfNeed();
                        selectRecord();
                    }
                }
            }
        });
        etContent.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                onItemClicked();
            }
        });
        etDate.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
            }
        });
        mCancelBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                giveUp();
            }
        });
        mSaveBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                saveNote();
            }
        });
        mStartVoice.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (mRecordingState == RECORDING_OFF) {
                    if(ContextCompat.checkSelfPermission(AddVoiceNoteActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(AddVoiceNoteActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(AddVoiceNoteActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_LYBMSC);
                        click_flag = true;
                    } else {
                        startVoiceEnginee();
                    }
                } else if (mRecordingState == RECORDING_ON) {
                    stopRecording();
                }
            }
        });
    }

    private static class GetDataThread extends Thread{
        private final WeakReference<AddVoiceNoteActivity> mActivity;
        private VoiceNoteBean noteBean;
        public GetDataThread(AddVoiceNoteActivity addVoiceNoteActivity,VoiceNoteBean bean){
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
            noteBean = bean;
        }

        @Override
        public void run() {
            super.run();
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                activity.loader = ImageLoader.build();
                Date creatDate = new Date(noteBean.getCreateTime());
                String oldTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(creatDate);
                activity.mHandler.sendMessage(activity.mHandler.obtainMessage(REFRESH_DATE, oldTime));
                activity.createtime = noteBean.getCreateTime();
                String content = noteBean.getContent();
                if (content != null) {
                    activity.mDataList.clear();
                    activity.mHandler.sendMessage(activity.mHandler.obtainMessage(CLEAR_BLANK));
                    // etContent.setText(noteBean.getContent());
                    // etContent.setSelection(noteBean.getContent().length());

                    // 定义正则表达式，用于匹配路径
                    Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(content);
                    int startIndex = 0;
                    while (m.find()) {
                        // 取出路径前的文字
                        if (m.start() > 0) {
                            activity.mDataList.add(content.substring(startIndex, m.start()));
                        }

                        SpannableString ss = new SpannableString(m.group().toString());

                        // 取出路径
                        String path = m.group().toString();
                        // 取出路径的后缀
                        //modified by luorw for GNSPR #101691 begin
                        int len = path.length() - Constants.URI_END_TAG_LENGTH;
                        String type = path.substring(len - 3, len);
                        String realPath = path.substring(0,len);
                        //modified by luorw for GNSPR #101691 end
                        Bitmap bm = null;
                        Bitmap rbm = null;
                        // 判断附件的类型，如果是录音文件，则从资源文件中加载图片
                        if (type.equals("amr")) {
                            bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.record_bg);
                            if (bm != null) {
                                bm = activity.zoomImg(bm, DisplayUtils.getDisplayWidth(activity));
                                // 缩放图片
                                // rbm = resize(bm,200);
                                //modified by luorw for GNSPR #101691 begin
                                SoundImageSpan span = new SoundImageSpan(activity, bm, realPath);
                                //modified by luorw for GNSPR #101691 end
                                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                System.out.println(m.start() + "-------" + m.end());
                                activity.mDataList.add(ss);
                            }
                            startIndex = m.end();
                        } else {
                            if (realPath.equals(Constants.PATH_BILL_UNCHECKED_IMG)) {
                                rbm = activity.bm_bill_unchecked;
                                BillImageSpan span = new BillImageSpan(activity, rbm);
                                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                System.out.println(m.start() + "-------" + m.end());
                                activity.mDataList.add(ss);
                                startIndex = m.end();
                            } else if (realPath.equals(Constants.PATH_BILL_CHECKED_IMG)) {
                                rbm = activity.bm_bill_checked;
                                BillImageSpan span = new BillImageSpan(activity, rbm);
                                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                System.out.println(m.start() + "-------" + m.end());
                                activity.mDataList.add(ss);
                                startIndex = m.end();
                            } else {
                                //modified by luorw for GNSPR #101691 begin
                                bm = activity.loader.getBitmapFromMemCache(realPath);
                                if (bm == null) {
                                    bm = activity.decodeUriAsBitmap(Uri.parse(realPath));
                                    if (bm != null) {
                                        activity.loader.addBitmapToMemoryCache(realPath, bm);
                                    }
                                }
                                //modified by luorw for GNSPR #101691 end
                                // added by luorw for GNSPR #73315 2017-03-17 begin
                                if (bm == null) {
                                    bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.image_unavailble);
                                    PhotoImageSpan span = new PhotoImageSpan(activity, bm);
                                    ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    activity.mDataList.add(ss);
                                }
                                // added by luorw for GNSPR #73315 2017-03-17 end
                                else {
                                    // 缩放图片
                                    rbm = activity.zoomImg(bm, DisplayUtils.getDisplayWidth(activity));
                                    PhotoImageSpan span = new PhotoImageSpan(activity, rbm);
                                    ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    System.out.println(m.start() + "-------" + m.end());
                                    activity.mDataList.add(ss);
                                }
                                startIndex = m.end();
                            }
                        }

                        // 用List记录该录音的位置及所在路径，用于单击事件
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("location", m.start() + "-" + m.end());
                        map.put("path", path);
                        activity.imgList.add(map);
                    }
                    // 将最后一个图片之后的文字添加在TextView中
                    activity.mDataList.add(content.substring(startIndex, content.length()));
                }
                activity.mHandler.sendMessage(activity.mHandler.obtainMessage(ADD_CONTENT_LIST));
            }
        }
    }

    private void getDataList(final VoiceNoteBean noteBean) {
        setSaveBtnStatus(false);
        etContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new GetDataThread(this,noteBean).start();
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            LogUtils.i("decodeUriAsBitmap", "e = " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private void loadData() {
        recordBeanList = new ArrayList<>();
        voiceNoteBean = new VoiceNoteBean();
        if (noteid > 0) {
            VoiceNoteBean noteBean = VoiceNoteDao.getInstance(this).getVoiceNote(noteid);
            getDataList(noteBean);
            if (noteBean.getRemindDate() != 0) {
                mNoteRemindTimeLayout.setVisibility(View.VISIBLE);
                Date date = new Date(noteBean.getRemindDate());
                String remindTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
                mNoteRemindTimeTv.setText(remindTime);
            } else {
                mNoteRemindTimeLayout.setVisibility(View.GONE);
            }
            voiceNoteBean = noteBean;
            etContent.addTextChangedListener(new MyTextWatcher());
        } else {
            createtime = System.currentTimeMillis();
            Date date = new Date(createtime);
            createTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
            etDate.setText(createTime);
            etContent.setText("");
        }
        File file = new File(PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

	/*
	 * //等比例缩放图片 private Bitmap resize(Bitmap bitmap,int S){ int imgWidth =
	 * bitmap.getWidth(); int imgHeight = bitmap.getHeight(); double partion =
	 * imgWidth*1.0/imgHeight; double sqrtLength = Math.sqrt(partion*partion +
	 * 1); //新的缩略图大小 double newImgW = S*(partion / sqrtLength); double newImgH =
	 * S*(1 / sqrtLength); float scaleW = (float) (newImgW/imgWidth); float
	 * scaleH = (float) (newImgH/imgHeight);
	 * 
	 * Matrix mx = new Matrix(); //对原图片进行缩放 mx.postScale(scaleW, scaleH); bitmap
	 * = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
	 * return bitmap; }
	 */

    // 缩放图片
    private Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newHeight = height * newWidth / width;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    //private int getImgNewHeight()

    /*private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayOptions(AmigoActionBar.DISPLAY_SHOW_CUSTOM, AmigoActionBar.DISPLAY_SHOW_CUSTOM);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        View view = getLayoutInflater().inflate(R.layout.action_delete, null);
        mCancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        mSaveBtn = (Button) view.findViewById(R.id.note_save_btn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

		*//*
		 * modify by zhengjl zt 2017-1-16 原来是删除按钮，现在改成了舍弃，放弃当前的编辑
		 *//*
        mCancelBtn.setOnClickListener(this);
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.MATCH_PARENT, AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.FILL_HORIZONTAL);
        mActionBar.setCustomView(view, param);
        mActionBar.show();

    }*/

    @Override
    public void onBackPressed() {
        giveUp();
    }

    // added by luorw for GNSPR #70056 begin
    private boolean isModifiedRemindDate(VoiceNoteBean noteBean) {
        if (noteBean.getRemindDate() == 0 && mNoteRemindTimeTv.getVisibility() == View.INVISIBLE) {
            return false;
        }
        Date date = new Date(noteBean.getRemindDate());
        String remindTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
        if (!mNoteRemindTimeTv.getText().equals(remindTime)) {
            return true;
        } else {
            return false;
        }
    }

    private static class SaveNoteThread extends Thread{
        private final WeakReference<AddVoiceNoteActivity> mActivity;
        public SaveNoteThread(AddVoiceNoteActivity addVoiceNoteActivity){
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
        }

        @Override
        public void run() {
            super.run();
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                final CharSequence remindTime = activity.mNoteRemindTimeTv.getText();
                final String content = activity.etContent.getEditableText().toString();
                final VoiceNoteBean voiceNoteBean = new VoiceNoteBean();
                if (TextUtils.isEmpty(content)) {
                    if (activity.mNoteRemindTimeLayout.getVisibility() == View.VISIBLE) {
                        voiceNoteBean.setTitle("新建备忘");
                        voiceNoteBean.setContent(null);
                    }
                } else {
                    voiceNoteBean.setTitle(activity.getTitleFromContent(content));
                    voiceNoteBean.setContent(content);
                    voiceNoteBean.setSearchContent(activity.mSearchContent);
                }
                voiceNoteBean.setCreateTime(activity.createtime);
                if (!TextUtils.isEmpty(remindTime)) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                    Date date = null;
                    try {
                        date = format.parse(remindTime.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    voiceNoteBean.setRemindDate(date.getTime());
                } else {
                    voiceNoteBean.setRemindDate(0);
                }

                activity.recordBeanList.clear();
                Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(content);
                while (m.find()) {
                    // 取出路径
                    String uri = m.group().toString();
                    String realUri = uri.substring(0,uri.length() - Constants.URI_END_TAG_LENGTH);
                    // 取出路径的后缀
                    String type = realUri.substring(realUri.length() - 3, realUri.length());
                    if (type.equals("amr")) {
                        // 录音文件
                        MediaPlayer mediaPlayer = MediaPlayer.create(activity, Uri.parse(realUri));
                        if (mediaPlayer == null) {
                            break;
                        }
                        int time = mediaPlayer.getDuration() / 1000;
                        RecordBean recordBean = new RecordBean();
                        recordBean.setTime(time);
                        recordBean.setUri(realUri);
                        activity.recordBeanList.add(recordBean);
                    }
                }
                voiceNoteBean.setRecordBeanList(activity.recordBeanList);
                if (activity.noteid > 0) {
                    voiceNoteBean.setId(activity.noteid);
                    voiceNoteBean.setCreateTime(System.currentTimeMillis());
                    VoiceNoteDao.getInstance(activity).updateVoiceToDB(voiceNoteBean);
                    RemindUtils.noteAlarmEdit(activity, voiceNoteBean);
                } else {
                    VoiceNoteDao.getInstance(activity).saveVoiceNoteToDB(voiceNoteBean);
                    RemindUtils.noteAlarm(activity, voiceNoteBean);
                }
            }
        }
    }

    private static class DeleteVoiceNoteThread extends Thread{
        private WeakReference<AddVoiceNoteActivity> mActivity;
        public DeleteVoiceNoteThread(AddVoiceNoteActivity addVoiceNoteActivity){
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
        }

        @Override
        public void run() {
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                VoiceNoteBean bean = VoiceNoteDao.getInstance(activity).getVoiceNote(activity.noteid);
                RemindUtils.noteAlarmCancel(activity, bean);
                List<VoiceNoteBean> beanList = new ArrayList<VoiceNoteBean>();
                beanList.add(bean);
                VoiceNoteDao.getInstance(activity).deleteVoiceNotes(beanList);
            }
            super.run();
        }
    }

    /*
     *  added by liyu for GNSPR #76871 begin
     */
    private void saveOnNewIntent() {
        if (!hasModify()) {
            return;
        } else if (isWorthSaving()) {
            new SaveNoteThread(this).start();
        } else if (noteid > 0) {
            new DeleteVoiceNoteThread(this).start();
        }
    }


    // added by luorw for GNSPR #70056 end

    private void saveNote() {

        // modify by zhengjl at 2017-2-22 for 详情页显示错位
        /*if (!hasModify()) {
            returnPre();
            return;
        }*/

        // 判断新建时，点击保存时，内容是否为空
        if (isWorthSaving()) {
            if (pd == null) {
                pd = new AmigoProgressDialog(AddVoiceNoteActivity.this);
            }
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.show();
            new VoiceNoteSave(this).start();
        } else {
            // 编辑状态，内容被删除，此时直接删除这条备忘
            if (noteid > 0) {
                if (pd == null) {
                    pd = new AmigoProgressDialog(AddVoiceNoteActivity.this);
                }
                pd.setCanceledOnTouchOutside(false);
                pd.setCancelable(false);
                pd.show();
                new DeleteVoiceNote(this).start();
                return;
            } else {
                // add by zhengjl at 2017-2-21
                returnPre();
                // add by zhengjl at 2017-2-21
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }

        }
        // modify by zhengjl at 2017-2-22 for 详情

    }

    // add by zhengjl at 2017-2-22 for NoteDetailActivity 位置错乱
    private void returnPre() {

        // Gionee zhengyt 2017-3-10 modify for GNSPR#71108 Begin
        // Intent in = new Intent();
        // 编辑状态，返回详情
        // if (noteid > 0) {
        // 返回NoteDetailActivity需要从onCreat开始，否则会出现位置错乱
        // in.setClass(mContext, NoteDetailActivity.class);
        // in.putExtra("noteid", voiceNoteBean.getId());
        // finish();
        // startActivity(in);
        // add by zhengjl at 2017-2-22 增加过场动画
        // overridePendingTransition(R.anim.left_in, R.anim.right_out);
        // }
        // 新建状态，直接回到首页
        // inish();
        Toast.makeText(this,"请输入备忘内容",Toast.LENGTH_SHORT).show();
        // Gionee zhengyt 2017-3-10 modify for GNSPR#71108 end
    }

    @Override
    public void onRecorderComplete(String path) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.record_bg);
        insertSoundBitmap(bitmap, 200, Constants.FILE_SCHEME + path);
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;// 当前activity信息
        if (!mListenerInputView.getisShowingSoftInput() && CURRENT_CLASSNAME.equals(cn.getClassName())) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
        keepScreenOn(false);
        abandonAudioFocus();
    }

    /* add by zhengjl at 2017-2-5 for GNSPR #66225 not end */
    private static class DeleteVoiceNote extends Thread {
        private WeakReference<AddVoiceNoteActivity> mActivity;
        public DeleteVoiceNote(AddVoiceNoteActivity addVoiceNoteActivity) {
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
        }

        @Override
        public void run() {
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                VoiceNoteBean bean = VoiceNoteDao.getInstance(activity).getVoiceNote(activity.noteid);
                // added by luorw for GNSPR #70074 begin
                RemindUtils.noteAlarmCancel(activity, bean);
                // added by luorw for GNSPR #70074 end
                List<VoiceNoteBean> beanList = new ArrayList<VoiceNoteBean>();
                beanList.add(bean);
                VoiceNoteDao.getInstance(activity).deleteVoiceNotes(beanList);
                // LogUtils.e("zjl","delete success");
                Message message = activity.mHandler.obtainMessage(DELETE_OK);
                activity.mHandler.sendMessage(message);
            }
        }
    }

    private boolean isImageExists(Uri uri) {
        File file = new File(uri.toString().replace("file://", ""));
        return file.exists();
    }

    /* add by zhengjl at 2017-2-5 for GNSPR #66225 not end */
    private static class VoiceNoteSave extends Thread {
        final WeakReference<AddVoiceNoteActivity> mActivity;

        public VoiceNoteSave(AddVoiceNoteActivity addVoiceNoteActivity) {
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
        }

        @Override
        public void run() {
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                activity.getWorkingText();
                activity.recordBeanList.clear();
                Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(activity.etContent.getText().toString());
                //modified by luorw for GNSPR #72369 2017-03-14 begin
                boolean hasImage = false;
                boolean hasRecord = false;
                boolean imageIsAvailable = false;
                //取出内容中的除了路径的所有内容
                while (m.find()) {
                    // 取出路径
                    //added by luorw for GNSPR #101691 20170822 begin
                    String uri = m.group().toString();
                    String realUri = uri.substring(0,uri.length()-Constants.URI_END_TAG_LENGTH);
                    // 取出路径的后缀
                    String type = realUri.substring(realUri.length() - 3, realUri.length());
                    //added by luorw for GNSPR #101691 20170822 end
                    if (type.equals("amr")) {
                        // 录音文件
                        MediaPlayer mediaPlayer = MediaPlayer.create(activity, Uri.parse(realUri));
                        if (mediaPlayer == null) {
                            break;
                        }
                        int time = mediaPlayer.getDuration() / 1000;
                        RecordBean recordBean = new RecordBean();
                        recordBean.setTime(time);
                        recordBean.setUri(realUri);
                        activity.recordBeanList.add(recordBean);
                    }
                    //added by luorw for GNSPR #101691 20170822 begin
                    String matchStr = m.group();
                    String realMatchStr = matchStr.substring(0,matchStr.length()-Constants.URI_END_TAG_LENGTH);
                    String endStr = realMatchStr.substring(realMatchStr.length() - 3, realMatchStr.length());
                    //added by luorw for GNSPR #101691 20170822 end
                    //清单图标的路径
                    if (endStr.equalsIgnoreCase("png") || endStr.equalsIgnoreCase("jpg")
                            || endStr.equalsIgnoreCase("jpeg") || endStr.equalsIgnoreCase("bmp")
                            || endStr.equalsIgnoreCase("gif")) {
                        if (!realMatchStr.contains(Constants.PATH_BILL_UNCHECKED_IMG) && !realMatchStr.contains(Constants.PATH_BILL_CHECKED_IMG)) {
                            hasImage = true;
                            //Gionee zhengyt 2017-3-13 add for GNSRP #71049 BEGIN 附件被删除或者加密后，不显示附件图标
                            //modified by luorw 2017-3-14
                            Uri uristr = Uri.parse(realMatchStr);
                            //判断当前图片附件只要本地能获取到，说明图片没有丢失，并且只需要执行一次，只要有图片就必须显示图片附件图表
                            if (!imageIsAvailable && activity.isImageExists(uristr)) {
                                imageIsAvailable = true;
                            }
                        }
                    } else if (realMatchStr.contains(".amr")) {
                        hasRecord = true;
                    }
                }
                //显示附件类型
                if (hasImage && hasRecord && imageIsAvailable) {
                    activity.voiceNoteBean.setAttachmentType(Constants.ATTACHMENT_TYPE);
                } else if (hasImage && !hasRecord && imageIsAvailable) {
                    activity.voiceNoteBean.setAttachmentType(Constants.PICTURE_TYPE);
                } else if (!hasImage && hasRecord && !imageIsAvailable) {
                    activity.voiceNoteBean.setAttachmentType(Constants.RECORD_TYPE);
                } else {
                    activity.voiceNoteBean.setAttachmentType(Constants.NONE_TYPE);
                }
                activity.voiceNoteBean.setRecordBeanList(activity.recordBeanList);
                if (activity.noteid > 0) {
                    activity.voiceNoteBean.setId(activity.noteid);
                    activity.voiceNoteBean.setCreateTime(System.currentTimeMillis());
                    VoiceNoteDao.getInstance(activity).updateVoiceToDB(activity.voiceNoteBean);
                    RemindUtils.noteAlarmEdit(activity, activity.voiceNoteBean);
                } else {
                    VoiceNoteDao.getInstance(activity).saveVoiceNoteToDB(activity.voiceNoteBean);
                    RemindUtils.noteAlarm(activity, activity.voiceNoteBean);
                }
                Message message = activity.mHandler.obtainMessage(SAVE_OK);
                activity.mHandler.sendMessage(message);
            }

        }
    }

    private void getWorkingText() {

        // modify by zhengjl at 2017-1-20 for GNSPR #65716 not end
        if (TextUtils.isEmpty(etContent.getText().toString())) {
            if (mNoteRemindTimeLayout.getVisibility() == View.VISIBLE) {
                voiceNoteBean.setTitle("新建备忘");
                voiceNoteBean.setContent(null);
            }
        } else {

            voiceNoteBean.setTitle(getTitleFromContent(etContent.getText().toString()));

            LogUtils.i(TAG, "voice...content.length:" + etContent.getText().toString().length());
            voiceNoteBean.setContent(etContent.getText().toString());

            voiceNoteBean.setSearchContent(mSearchContent);

        }

        voiceNoteBean.setCreateTime(createtime);
        if (!TextUtils.isEmpty(mNoteRemindTimeTv.getText())) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            Date date = null;
            try {
                date = format.parse(mNoteRemindTimeTv.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            voiceNoteBean.setRemindDate(date.getTime());
        } else {
            voiceNoteBean.setRemindDate(0);
        }
    }

    /*
     * add by zhengyt 2017-2-5
     * 用于保存searchContent，与content不一致的是：searchContent去掉了图片和录音的路径
     */
    private String mSearchContent;

    /**
     * add by zhengjl at 2017-1-18 原本的标题显示为时间，现在改为从内容获取标题
     *
     * @param content
     * @return
     */
    private String getTitleFromContent(String content) {

        String newNote = "新建备忘";
        // 定义正则表达式，用于匹配路径
        Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        int startIndex = 0;
        StringBuffer buf = new StringBuffer();
        mSearchContent = "";

        // 取出内容中的除了路径的所有内容
        while (m.find()) {
            // 取出路径前后的文字
            int start = m.start();
            buf.append(content.substring(startIndex, start));
            startIndex = start + m.group().length();
        }
        if (m.find(0)) {
            // 添加最后一个路径之后的文字
            if (startIndex < content.length() - 1) {
                buf.append(content.substring(startIndex, content.length()));
            }
            if (buf.toString().length() > 0) {
                String temp = buf.toString();

                mSearchContent = temp;

                // modify by zhengjl at 2017-1-19 for GNSPR #65636 begin
                int index = 0;
                int end = temp.length() - 1;
                while (index < temp.length() && temp.charAt(index) == '\n') {
                    index++;
                }
                // 去除文本后的换行符
                while (end > 0 && end <= content.length() - 1 && content.charAt(end) == '\n') {
                    end--;
                }
                if (index >= end)
                    return newNote;

                // modify by zhengjl at 2017-1-19 for GNSPR #65636 end
                String[] contents = TextUtilTools.splitText(temp.substring(index, end + 1), "\n");
                if (contents == null || TextUtils.isEmpty(contents[0]))
                    return newNote;
                return contents[0].length() > 20 ? contents[0].substring(0, 21) : contents[0];
            } else {
                // 内容中只有路径
                return newNote;
            }
        } else {
            // 内容中没有路径
            int index = 0;
            mSearchContent = content;
            // 过滤文本前的换行符
            while (content.length() > index && content.charAt(index) == '\n') {
                index++;
            }
            String[] contents = TextUtilTools.splitText(content.substring(index, content.length()), "\n");
            return contents[0].length() > 20 ? contents[0].substring(0, 21) : contents[0];
        }
    }

    private boolean isWorthSaving() {
        String content = etContent.getText().toString();
        String title = etDate.getText().toString();
        boolean first = TextUtils.isEmpty(content.replace(" ", "").replace("\n", ""));
        // add by zhengjl at 2017-1-20 for GNSPR #65716
        if (mNoteRemindTimeLayout.getVisibility() == View.VISIBLE)
            return true;

        if (first) {
            return false;
        } else {
            return true;
        }
    }

    private class MyTextWatcher implements TextWatcher {
        private int start;
        private int count;
        private boolean needDeleteBill;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // modify by zhengjl at 2017-1-20 for GNSPR #65716 not end
            if (mNoteRemindTimeLayout.getVisibility() == View.VISIBLE)
                setSaveBtnStatus(true);
            if (count == 0) {
                return;
            }
            String cs = s.subSequence(start, start + count).toString();
            if (cs.equals(" ")) {
                this.start = start;
                String str = s.toString().substring(0, start);
                if (str.endsWith(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG) || str.endsWith(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG)) {
                    needDeleteBill = true;
                }
            }
            if (after == 0 && (cs.equals(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG) || cs.equals(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG))) {
                isBillSelected = false;
            }

        }

        // start开始位置，before删除字数，count添加的字数
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // modify by zhengjl at 2017-1-20 for GNSPR #65716 not end
			/* add by zhengjl at 2017-2-5 for GNSPR #66225 begin */
            if (!hasModify() && TextUtils.isEmpty(s.toString().trim()) && mNoteRemindTimeLayout.getVisibility() == View.GONE) {
                mSaveBtn.setClickable(true);
                mSaveBtn.setTextColor(getResources().getColorStateList(R.color.add_voice_note_actionbar_save_gray_button_color));
            } else {
                mSaveBtn.setClickable(true);
                mSaveBtn.setTextColor(getResources().getColorStateList(R.color.add_voice_note_actionbar_save_button_color));
                if (noteid > 0 && TextUtils.isEmpty(s.toString().trim())) {
                    mSaveBtn.setClickable(true);
                    mSaveBtn.setTextColor(getResources().getColorStateList(R.color.add_voice_note_actionbar_save_gray_button_color));
                }
            }

            if (needDeleteBill && this.start == start) {
                Editable editable = etContent.getText();
                editable.replace(start - BILL_LENGHT, start, "");
            }
            needDeleteBill = false;

            this.start = start;
            this.count = count;

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int length = editable.toString().length();
            int beforeLength = length - count;
            int leftInput = MAX_LENGTH - beforeLength;
            int delStart = start + leftInput;
            int delEnd = start + count;
            if (length > MAX_LENGTH) {
                LogUtils.i(TAG, "afterTextChanged...>lenght");
                Toast.makeText(AddVoiceNoteActivity.this, getResources().getString(R.string.texttoolong), Toast.LENGTH_SHORT).show();
                editable.delete(delStart, delEnd);
                etContent.setText(editable);
                etContent.setSelection(delStart);
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            if (isBillSelected) {
                int start = etContent.getSelectionStart();
                addImageSpan(start);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void showNotificationForVoice() {
        Intent intent = new Intent(this, AddVoiceNoteActivity.class);
        intent.putExtra("fromNotificarion", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_sys_voice)
                .setTicker("语音转文字已启动")
                .setContentTitle("语音转文字")
                .setContentText("语音转文字已启动")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //notification.setLatestEventInfo(mContext, "语音转文字", "语音转文字已启动", pendingIntent);
        mNotificationManager.notify(NOTIFICATION_VOICE_ID, notification);
    }

    private void cancelNotificationForVoice() {
        mNotificationManager.cancel(NOTIFICATION_VOICE_ID);
    }

    private void startVoiceEnginee(){
        if (!NetWorkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用,请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isInCall) {
            Toast.makeText(this, "通话过程中不能语音输入", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            closeBackgroundMusicIfNeed();
            mRecordingState = RECORDING_ON;
            mStartVoice.setBackgroundResource(R.drawable.start_voice);
            ad = (AnimationDrawable) mStartVoice.getBackground();
            ad.start();
            onStartRecord();
            showNotificationForVoice();
            if (mListenerInputView.getisShowingSoftInput()) {
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            LogUtils.d("liyu", "start_voice onError e = " + e.getMessage().toString());
            e.printStackTrace();
            stopRecording();
            finish();
            Toast.makeText(getApplicationContext(),"语音引擎初始化失败，需要重新进入",Toast.LENGTH_SHORT).show();

        }
    }


    /**
     * add by zhengjl at 2017-1-16 点击actionbar的舍弃按钮
     */
    private void giveUp() {
        //modified by luorw for GNSPR #75323 20170322 begin
        showTips();
        //modified by luorw for GNSPR #75323 20170322 end
    }

    /**
     * add by zhengjl at 2017-1-16 显示对话框，是否舍弃
     */
    private void showTips() {
        imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(AddVoiceNoteActivity.this);
        builder.setTitle(AddVoiceNoteActivity.this.getString(R.string.giveup));
        builder.setMessage(AddVoiceNoteActivity.this.getString(R.string.giveup_note));
		/*
		 * dialog的取消按钮
		 */
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.giveup, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击舍弃按钮，弹出对话框，提示是否舍弃
                // 点击舍弃，直接退出activity
                // if (noteid > 0) {
                // //编辑模式
                // VoiceNoteBean bean =
                // VoiceNoteDao.getInstance(AddVoiceNoteActivity.this).getVoiceNote(noteid);
                // List<VoiceNoteBean> beanList = new
                // ArrayList<VoiceNoteBean>();
                // beanList.add(bean);
                // VoiceNoteDao.getInstance(AddVoiceNoteActivity.this).deleteVoiceNotes(beanList);
                // }
                dialog.dismiss();

                // add by zhengjl at 2017-2-22
                AddVoiceNoteActivity.this.finish();
                // add by zhengjl at 2017-2-22
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        builder.show();
    }

    /**
     * add by zhengjl at 2017-1-16 判断进入编辑状态之后，用户是否编辑了备忘
     *
     * @return
     */
    private boolean hasModify() {
        // 编辑模式
        if (noteid > 0) {
            VoiceNoteBean noteBean = VoiceNoteDao.getInstance(this).getVoiceNote(noteid);
            boolean contentIsEqual = etContent.getText().toString().equals(noteBean.getContent());
            return !contentIsEqual || isModifiedRemindDate(noteBean);
            // add by zhengjl at 2017-01-19 for GNSPR #65635 begin
        } else if (!TextUtils.isEmpty(etContent.getText().toString().trim()) || mNoteRemindTimeLayout.getVisibility() == View.VISIBLE) {
            // 新建模式
            return true;
            // add by zhengjl at 2017-01-19 for GNSPR #65635 end
        } else
            return false;
    }

    private void onItemClicked() {
        // imm.hideSoftInputFromInputMethod(etContent.getWindowToken(), 0);
        Spanned s = etContent.getText();
        ImageSpan[] imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
        int selection = etContent.getSelectionStart();
        for (ImageSpan span : imageSpans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            if (selection >= start && selection <= end) {
                //modified by luorw for GNSPR #101691 20170822 begin
                String path = etContent.getText().toString().substring(start, end);
                String realPath = path.substring(0,path.length()-Constants.URI_END_TAG_LENGTH);
                if (realPath.length() <= 3) {
                    return;
                }
                String type = realPath.substring(realPath.length() - 3, realPath.length());
                //modified by luorw for GNSPR #101691 20170822 end
                LogUtils.e("hangh", "type = " + type);
                if (type.equals("amr")) {
					/* add by zhengjl at 2017-2-5 for GNSPR #66215 begin */
                    // etContent.clearFocus();
                    etContent.setSelection(end);
					/* add by zhengjl at 2017-2-5 for GNSPR #66215 end */
                    MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(realPath));
                    if (mediaPlayer == null) {
                        Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int time = mediaPlayer.getDuration() / 1000;
                    String timestr = TextUtilTools.formatTime(time, ":");
                    mSoundPlayer = new SoundPlayer(this, realPath);
                    mSoundPlayer.showDialog(timestr);
                    mSoundPlayer.startPlay();
                    break;
                } else {
                    viewPic(realPath);
                    etContent.setSelection(end);
                    break;
                }
            }
        }
    }

    private void onBillChecked(int start, int end) {
        BillImageSpan is = new BillImageSpan(this, bm_bill_checked);
        SpannableString ss = new SpannableString(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG);
        ss.setSpan(is, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etContent.getEditableText().replace(start, end, ss);
        char[] chars = etContent.getText().toString().toCharArray();
        for (int i = end; i < chars.length; i++) {
            if (chars[i] == '\n') {
                etContent.setSelection(i);
                break;
            } else if (i == chars.length - 1) {
                etContent.setSelection(i + 1);
                break;
            }

        }

    }

    private void onBillUnChecked(int start, int end) {
        BillImageSpan is = new BillImageSpan(this, bm_bill_unchecked);
        SpannableString ss = new SpannableString(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG);
        ss.setSpan(is, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etContent.getEditableText().replace(start, end, ss);
        char[] chars = etContent.getText().toString().toCharArray();
        for (int i = end; i < chars.length; i++) {
            if (chars[i] == '\n') {
                etContent.setSelection(i);
                break;
            } else if (i == chars.length - 1) {
                etContent.setSelection(i + 1);
                break;
            }
        }
    }

    private void viewPic(String path) {

    }

    private void setBill() {
        int index = etContent.getSelectionStart();
        Editable editable = etContent.getText();
        String subStr = editable.toString().substring(0, index);
        char[] chars = subStr.toCharArray();
		/*
		 * add by zhengjl at 2017-2-6 for GNSPR #65715 begin
		 * 当输入框里面无内容，点击待办，再点击，隐藏待办
		 */
        if (chars.length == 0) {
            ImageSpan is = new BillImageSpan(this, bm_bill_unchecked);
            //modified by luorw for GNSPR #101691 begin
            SpannableString ss = new SpannableString(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG);
            //modified by luorw for GNSPR #101691 end
            ss.setSpan(is, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            etContent.getEditableText().insert(index, " ");
            etContent.getEditableText().insert(0, ss);
            isBillSelected = true;
            return;
        }
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == '\n') {
                if (chars.length > i + 1 && !subStr.substring(i + 1).startsWith("file://")) {
                    addImageSpan(i + 1);
                } else if (chars.length == i + 1 && !subStr.substring(i).startsWith("file://")) {
                    addImageSpan(i + 1);
                    // liyu fix #73275 start
                } else if (chars.length > i + 1 && subStr.substring(i + 1).startsWith("file://")) {
                    String text = subStr.substring(i + 1);
                    Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(text);
                    if (m.find()) {
                        text = text.substring(0, m.end());
                        if (!text.equals(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG) && !text.equals(Constants.PATH_BILL_UNCHECKED_IMG+ Constants.URI_END_TAG)) {
                            editable.insert(i + 1 + m.end(), "\n");
                            addImageSpan(i + 2 + m.end());
                        } else {
                            editable.replace(i + 1, i + 2 + m.end(), "");
                            isBillSelected = false;
                        }
                    }
                }

                // liyu fix #73275 end
                break;
            } else if (i == 0) {
                if (!subStr.startsWith("file://")) {
                    addImageSpan(i);
                } else {
					/* modify by zhengjl at 2017-2-6 for GNSPR #65715 not end */
                    // ImageSpan[] spans = etContent.getText().getSpans(0,
                    // index, NoteImageSpan.class);

                    String text = subStr.substring(i);
                    Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(text);
                    if (m.find()) {
                        text = text.substring(0, m.end());
                        if (!text.equals(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG) && !text.equals(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG)) {
                            editable.insert(i + m.end(), "\n");
                            addImageSpan(i + 1 + m.end());
                            return;
                        }
                    }
                    ImageSpan[] spans = etContent.getText().getSpans(0, index, ImageSpan.class);
                    if (spans.length > 0) {
                        ImageSpan span = spans[0];
                        int start = editable.getSpanStart(span);
                        int end = editable.getSpanEnd(span);
                        String str = editable.toString().substring(start, end);
                        if (str.equals(Constants.PATH_BILL_CHECKED_IMG + Constants.URI_END_TAG) || str.equals(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG)) {
                            editable.replace(start, end + 1, "");
                            isBillSelected = false;
                        }
                    }

                }
            }
        }
    }

    private void removeImageSpan(int index, BillImageSpan span) {
        etContent.getText().removeSpan(span);
    }

    private void addImageSpan(int index) {
        isBillSelected = true;
        BillImageSpan is = new BillImageSpan(this, bm_bill_unchecked);
        SpannableString ss = new SpannableString(Constants.PATH_BILL_UNCHECKED_IMG + Constants.URI_END_TAG);
        ss.setSpan(is, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etContent.getEditableText().insert(index, " ");
        etContent.getEditableText().insert(index, ss);
    }

    private void setNoteReminder() {
        Calendar calendar = Calendar.getInstance();
        long time = 0;
        boolean isEdit;
        if (mNoteRemindTimeLayout.getVisibility() != View.VISIBLE) {
            isEdit = false;
            time = System.currentTimeMillis();
        } else {
            isEdit = true;
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            Date date = null;
            try {
                if (!TextUtils.isEmpty(mNoteRemindTimeTv.getText())) {
                    date = format.parse(mNoteRemindTimeTv.getText().toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            time = date.getTime();
        }
        calendar.setTimeInMillis(time);
        calendar.setTimeZone(TimeZone.getTimeZone(Time.getCurrentTimezone()));
        DateTimeListener dateTimeListener = new DateTimeListener();
        showDateTimePickerDialog(dateTimeListener, calendar, isEdit);
    }

    private void showDateTimePickerDialog(DateTimeListener dateTimeListener, Calendar calendar, boolean isEdit) {
        final AmigoDateTimePickerDialog d = new AmigoDateTimePickerDialog(this, dateTimeListener, calendar);
        if (isEdit) {
            d.setButton(-3, "删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mNoteRemindTimeLayout.setVisibility(View.GONE);
                    mNoteRemindTimeTv.setText("");
                    voiceNoteBean.setRemindDate(0);
                    d.dismiss();

                    // add by zhengjl at 2017-1-20 for GNSPR #65716 begin
                    if (TextUtils.isEmpty(etContent.getText().toString()))
                        setSaveBtnStatus(false);
                    // add by zhengjl at 2017-1-20 for GNSPR #65716 not end
                }
            });
        }
        d.show();
    }

    private class DateTimeListener implements AmigoDateTimePickerDialog.OnDateTimeSetListener {

        public DateTimeListener() {

        }

        @Override
        public void onDateTimeSet(Calendar calendar) {
            setNoteRemindTime(calendar);
        }
    }

    private void setNoteRemindTime(Calendar calendar) {
        //modified by luorw for GNSPR #80832 2017-04-19 begin
        long time = calendar.getTime().getTime();
        //modified by luorw for GNSPR #80832 2017-04-19 end
        if (time < System.currentTimeMillis()) {
            Toast.makeText(this, "早于当前时间，请重新设置", Toast.LENGTH_SHORT).show();
        } else {
            Date date = new Date(time);
            String remindTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
            mNoteRemindTimeTv.setText(remindTime);
            mNoteRemindTimeLayout.setVisibility(View.VISIBLE);
            voiceNoteBean.setRemindDate(time);
            voiceNoteBean.setIsActive(Constants.REMIND_NOT_ACTIVE);

            // add by zhengjl at 2017-1-20 for GNSPR #65716 not end
            setSaveBtnStatus(true);
            // add by zhengjl at 2017-1-20 for GNSPR #65716 end
        }
    }

    // 设置按钮的状态
    private void setSaveBtnStatus(boolean status) {
        mSaveBtn.setClickable(true);
        if (status) {
            mSaveBtn.setTextColor(getResources().getColorStateList(R.color.add_voice_note_actionbar_save_button_color));
        } else {
            mSaveBtn.setTextColor(getResources().getColorStateList(R.color.add_voice_note_actionbar_save_gray_button_color));
        }
    }

    private void selectRecord() {
        // 收回输入法
        if (mListenerInputView.getisShowingSoftInput()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        mRecorder = new SoundRecorder(this);
        mRecorder.setmTakeSoundRecorderListener(this);
        mRecorder.initDialog(this);
        mRecorder.startRecorder();
        keepScreenOn(true);
        // mRecorder.showDialog();
    }

    private void keepScreenOn(boolean b) {
        if (b) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
            mWakeLock.acquire();
        } else if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void selectCamera() {
        if (mListenerInputView.getisShowingSoftInput()) {
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        }
        View localView = LayoutInflater.from(this).inflate(R.layout.action_select_camera, null, false);
        TextView selectImage = (TextView) localView.findViewById(R.id.select_images);
        TextView selectCamera = (TextView) localView.findViewById(R.id.select_camera);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    isFromPickImage = true;
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    isFromPickImage = false;
                    e.printStackTrace();
                    showFreezeForApp();
                }
            }
        });
        selectCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    currentImagePath = mImgPath + System.currentTimeMillis() + ".png";
                    File mPhotoFile = new File(currentImagePath);
                    //Build.VERSION_CODES.N = 24
                    if (Build.VERSION.SDK_INT >= 24) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(AddVoiceNoteActivity.this, "com.gionee.secretary.fileprovider", mPhotoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                    }
                    startActivityForResult(intent, 2);
                    //将currentImagePath保存到sp中，防止被回收后导致图片插入失败
                    saveCurrentImagePath(currentImagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    showFreezeForApp();
                }
            }
        });
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setView(localView);
        builder.setNegativeButton("取消", null);
        dialog = builder.create();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        }, 200);
    }

    private void saveCurrentImagePath(String path) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.CAMERA_IMG_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CURRENT_IMG_PATH, path);
        editor.commit();
    }

    private String getCurrentImagePath() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.CAMERA_IMG_SP, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.CURRENT_IMG_PATH,null);
    }

    private void showFreezeForApp() {
        Toast.makeText(this, "应用已被冻结，请解冻后再重试", Toast.LENGTH_LONG).show();
    }

    private static class GetPhotoThread extends Thread {
        private WeakReference<AddVoiceNoteActivity> mActivity;
        private Intent mIntent;
        Bitmap bitmap = null;

        public GetPhotoThread(AddVoiceNoteActivity addVoiceNoteActivity, Intent intent) {
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
            mIntent = intent;
        }

        @Override
        public void run() {
            AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                Uri uri = mIntent.getData();
                Cursor actualimagecursor = null;
                // modified by luorw for GNSPR #71392 20170311 begin
                String path = "";
                if (uri.toString().contains("file://")) {
                    path = uri.toString();
                } else {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    try {
                        actualimagecursor = activity.getContentResolver().query(uri, proj, null, null, null);
					/*Gionee zhengyt 2017-3-21 add for amigoBut#88324 begin*/
                        //added by luorw for GNSPR #79098 2017-4-12 begin
                        if (actualimagecursor.getCount() != 0 && !actualimagecursor.isClosed()) {
                            //added by luorw for GNSPR #79098 2017-4-12 end
                            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            actualimagecursor.moveToFirst();
                            path = Constants.FILE_SCHEME + actualimagecursor.getString(actual_image_column_index);
                        } else {
                            Message msg = activity.mHandler.obtainMessage(INSERT_PHOTO_FAIL);
                            activity.mHandler.sendMessage(msg);
                            return;
                        }
                    } catch (Exception e) {
                        // Fixed #76311
                        LogUtils.e("liyu", TAG + "GetPhotoThread in error : e = " + e.getMessage().toString());
                        e.printStackTrace();
                    } finally {
                        if (actualimagecursor != null && !actualimagecursor.isClosed()) {
                            actualimagecursor.close();
                        }
                    }

				/*Gionee zhengyt 2017-3-21 add for amigoBut#88324 end*/
                }
                LogUtils.i(TAG, "GetPhotoThread , path = " + path);
                try {
                    // 获取原图宽度
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //added by luorw for S10 终端项目Bug #96028 20170330 begin
                    options.inTempStorage = new byte[100 * 1024];
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inPurgeable = true;
                    options.inSampleSize = 1;
                    options.inInputShareable = true;
                    //added by luorw for S10 终端项目Bug #96028 20170330 end
                    ContentResolver cr = activity.getContentResolver();
                    InputStream is = cr.openInputStream(uri);
                    bitmap = BitmapFactory.decodeStream(is, null, options);
                    is.close();
                } catch (Exception e) {
                    LogUtils.e("liyu", "e = " + e.getMessage().toString());
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    bitmap = activity.zoomImg(bitmap, DisplayUtils.getDisplayWidth(activity));
                    // LogUtils.e("hangh","bitmap height = " + bitmap.getHeight());
                    Message msg = null;
                    if (bitmap.getHeight() > 2 * DisplayUtils.getDisplayHeight(activity)) {
                        msg = activity.mHandler.obtainMessage(PHOTO_IS_TOOLONG);
                    } else {
                        msg = activity.mHandler.obtainMessage(INSERT_PHOTO_SUCCESS, bitmap);
                        Bundle bundle = new Bundle();
                        LogUtils.i("luorw", "GetPhotoThread , path = " + path);
                        bundle.putString("path", path);
                        msg.setData(bundle);
                    }
                    activity.mHandler.sendMessage(msg);
                } else {
                    LogUtils.i(TAG, "GetPhotoThread , bitmap == null , INSERT_PHOTO_FAIL");
                    Message msg = activity.mHandler.obtainMessage(INSERT_PHOTO_FAIL);
                    activity.mHandler.sendMessage(msg);
                }
                // modified by luorw for GNSPR #71392 20170311 end
            }

        }
    }

    private static class GetCameraThread extends Thread {
        private final WeakReference<AddVoiceNoteActivity> mActivity;
        Bitmap bitmap = null;

        public GetCameraThread(AddVoiceNoteActivity addVoiceNoteActivity) {
            mActivity = new WeakReference<AddVoiceNoteActivity>(addVoiceNoteActivity);
        }

        @Override
        public void run() {
            final AddVoiceNoteActivity activity = mActivity.get();
            if(activity != null){
                try {
                    if(currentImagePath == null){
                        LogUtils.i(TAG, "currentImagePath = null");
                        currentImagePath = activity.getCurrentImagePath();
                    }
                    LogUtils.i(TAG, "currentImagePath1111111111111 = " + activity.getCurrentImagePath());
                    LogUtils.i(TAG, "currentImagePath = " + currentImagePath);
                    if (currentImagePath != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(currentImagePath));
                        bitmap = BitmapFactory.decodeStream(bis, null, options);
                        if (bis != null) {
                            bis.close();
                        }
                    } else {
                        Message msg = activity.mHandler.obtainMessage(INSERT_PHOTO_FAIL);
                        activity.mHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    LogUtils.d("liyu", "e = " + e.getMessage().toString());
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    bitmap = activity.zoomImg(bitmap, DisplayUtils.getDisplayWidth(activity));
                    Message msg = activity.mHandler.obtainMessage(INSERT_CAMERA_SUCCESS, bitmap);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", currentImagePath);
                    msg.setData(bundle);
                    activity.mHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "onActivityResult , requestCode = " + requestCode + " , resultCode = " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
		/*
		 * add by zhengjl at 2017-2-8 for GNSPR #65583
		 * 设置activity的FromActivityResult值为true，防止回到本应用之后显示密码保护界面
		 */
        PasswordModel.getInstance(mContext).updateLockState(false);
        if (resultCode == RESULT_OK) {
            Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(etContent.getText().toString());
            int image_num = 0;
            while (m.find()) {
                //modified by luorw for GNSPR #101691 20170822 begin
                // 取出路径
                String path = m.group().toString();
                String realPath = path.substring(0,path.length() - Constants.URI_END_TAG_LENGTH);
                // 取出路径的后缀
                String type = realPath.substring(realPath.length() - 3, realPath.length());
                //modified by luorw for GNSPR #101691 20170822 end
                if (!type.equals("amr") && !realPath.equals(Constants.PATH_BILL_UNCHECKED_IMG) && !realPath.equals(Constants.PATH_BILL_CHECKED_IMG)) {
                    // 图片数量
                    image_num++;
                }
            }
            if (image_num >= 10) {
                Toast.makeText(mContext, "图片添加已达到上限！", Toast.LENGTH_LONG).show();
                return;
            }
            // 如果是选择照片
            if (requestCode == 1) {
                // 取得选择照片的路径
                new GetPhotoThread(this, data).start();
            }
            // 如果选择的是拍照
            else if (requestCode == 2) {
                new GetCameraThread(this).start();
            }
        }
    }

    // 将图片等比例缩放到合适的大小并添加在EditText中
    private void insertSoundBitmap(Bitmap bitmap, int S, String imgPath) {
        if (bitmap == null) {
            Toast.makeText(this, "文件已损坏，插入录音失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        bitmap = zoomImg(bitmap, DisplayUtils.getDisplayWidth(mContext));
        // 添加边框效果
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);

        try {
            final SoundImageSpan imageSpan = new SoundImageSpan(this, bitmap, imgPath);
            SpannableString spannableString = new SpannableString(imgPath + Constants.URI_END_TAG);
            spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 光标移到下一行
            // et_Notes.append("\n");
            int selectionIndex = etContent.getSelectionStart();
            if (!TextUtils.isEmpty(etContent.getText().toString().trim()) && (selectionIndex != 0 && (etContent.getText().toString().charAt(selectionIndex - 1) != '\n'))) {
                etContent.getEditableText().insert(selectionIndex, "\n");
                selectionIndex++;
            }
            Editable editable = etContent.getEditableText();

            spannableString.getSpans(0, spannableString.length(), BillImageSpan.class);

            // 将图片添加进EditText中
            editable.insert(selectionIndex, spannableString);
            // 添加图片后自动空出两行
            int position = selectionIndex + imgPath.length() + Constants.URI_END_TAG_LENGTH;
            if (position < MAX_LENGTH) {
                etContent.getEditableText().insert(position, "\n");
            }
            // 用List记录该录音的位置及所在路径，用于单击事件
            Map<String, String> map = new HashMap<String, String>();
            map.put("location", selectionIndex + "-" + (selectionIndex + spannableString.length()));
            map.put("path", imgPath + Constants.URI_END_TAG);
            imgList.add(map);
            isBillSelected = false;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "已不能添加录音", Toast.LENGTH_SHORT).show();
        }
    }

    // 将图片等比例缩放到合适的大小并添加在EditText中
    private void insertBitmap(Bitmap bitmap, String imgPath) {
        if (bitmap == null) {
            Toast.makeText(this, "文件已损坏，插入图片失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        // bitmap = zoomImg(bitmap, S);
        // 添加边框效果
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);

        final PhotoImageSpan imageSpan = new PhotoImageSpan(this, bitmap);
        //modified by luorw for GNSPR #101691 20170822 begin
        SpannableString spannableString = new SpannableString(imgPath + Constants.URI_END_TAG);
        //modified by luorw for GNSPR #101691 20170822 end
        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 光标移到下一行
        // et_Notes.append("\n");
        // add by zhengjl at 2017-2-20 for fix ui
        int selectionIndex = etContent.getSelectionStart();
        if (!TextUtils.isEmpty(etContent.getText().toString().trim()) && (selectionIndex != 0 && (etContent.getText().toString().charAt(selectionIndex - 1) != '\n'))) {
            etContent.getEditableText().insert(selectionIndex, "\n");
            selectionIndex++;
        }
        Editable editable = etContent.getEditableText();
        spannableString.getSpans(0, spannableString.length(), PhotoImageSpan.class);

        // 将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);

        // 添加图片后自动空出两行
        //modified by luorw for GNSPR #101691 20170822 begin
        etContent.getEditableText().insert(selectionIndex + imgPath.length() + Constants.URI_END_TAG_LENGTH, "\n");
        //modified by luorw for GNSPR #101691 20170822 end
        LogUtils.i(TAG, "insertBitmap , etContent = " + etContent.getText().toString());
        // 用List记录该录音的位置及所在路径，用于单击事件
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-" + (selectionIndex + spannableString.length()));
        map.put("path", imgPath + Constants.URI_END_TAG);
        imgList.add(map);
        isBillSelected = false;
    }

    private void stopRecording() {
        cancelNotificationForVoice();
        onStopRecord();
        mRecordingState = RECORDING_OFF;
        ad.stop();
        mStartVoice.setBackgroundResource(R.drawable.voice_normal);
        abandonAudioFocus();
    }

    private void onStartRecord() {
        // mContentTxt.setText("");
        mRecordLastResultLength = 0;
        parseResult = "";

        try {
            if (sampleRate == MscSampleRate.SAMPLE_RATE_16K) {
                mRecord = new PcmRecorder(this, PcmRecorder.SAMPLE_RATE_16K);
            } else {
                mRecord = new PcmRecorder(this, PcmRecorder.SAMPLE_RATE_8K);
            }
            mRecord.setRecordListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecord.startRecording();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // mStopButton.setEnabled(true);
        // mRecordButton.setEnabled(false);

        mLybMscRecognizer.initialize();
        mLybMscRecognizer.beginRecognize(sampleRate);
        contentVoice.delete(0, contentVoice.length());
        contentVoice.append(etContent.getText().toString());
        // Toast.makeText(this,"开始录音!",Toast.LENGTH_SHORT).show();
        LogUtils.e(TAG, "onStartRecord");
    }

    private void onStopRecord() {
        if (null != mRecord) {
            mRecord.stopRecording();
            mRecord.release();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // mStopButton.setEnabled(false);
        // mRecordButton.setEnabled(true);

        if (mLybMscRecognizer != null) {
            mLybMscRecognizer.stopRecognize();
        }

        // contentVoice.delete(0, contentVoice.length());
        // Toast.makeText(this,"停止录音!",Toast.LENGTH_SHORT).show();
        LogUtils.e(TAG, "onStopRecord");
    }

    private void parseJSONResult(String strResult) {
        LogUtils.d(TAG, "RecognizerManager parseJSONResult----strResult = " + strResult);
        JSONObject jsonObj;

        try {
            jsonObj = new JSONObject(strResult).getJSONObject("cn");
            JSONObject stjsonObj = new JSONObject(jsonObj.toString()).getJSONObject("st");
            JSONArray jsonObjs = new JSONObject(stjsonObj.toString()).getJSONArray("rt");
            for (int i = 0; i < jsonObjs.length(); i++) {
                parseOneItem(jsonObjs.opt(i).toString());
            }
        } catch (JSONException e) {
            LogUtils.d(TAG, "Jsons parse error ! ---- exception = " + e);
            e.printStackTrace();
        } finally {
            mHandler.sendMessage(mHandler.obtainMessage(0, parseResult));
        }
    }

    private void parseOneItem(String oneData) {
        String content;
        JSONObject jsonObj;

        try {
            JSONArray jsonObjs = new JSONObject(oneData).getJSONArray("ws");
            for (int i = 0; i < jsonObjs.length(); i++) {
                JSONArray jsonObjsArray = new JSONObject(jsonObjs.opt(i).toString()).getJSONArray("cw");
                for (int j = 0; j < jsonObjsArray.length(); j++) {
                    jsonObj = (JSONObject) jsonObjsArray.opt(j);
                    content = jsonObj.getString("w");
                    parseResult += content;
                }
            }
        } catch (JSONException e) {
            LogUtils.d(TAG, "Jsons parse error ! ---- exception = " + e);
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(String result) {
        if (null != result) {
            parseJSONResult(result);
        }
    }

    @Override
    public void onPartialResults(String result) {
        parseJSONResult(result);
    }


    @Override
    public void onRecordData(byte[] dataBuffer, int length, long timeMillisecond) {
        mLybMscRecognizer.putRecordData(dataBuffer);
    }

    @Override
    public void OnKeyBoardState(int state) {
        // TODO Auto-generated method stub
        switch (state) {
            // 开启
            case 1:
                // Toast.makeText(this, "输入法显示了.", Toast.LENGTH_SHORT).show();
                stopRecording();
                break;
            // 关闭
            case 0:
                // Toast.makeText(this, "变化为正常状态(输入法关闭).",
                // Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

    }

    @Override
    public void onBillClicked(int selection) {
        if (!mListenerInputView.getisShowingSoftInput()) {
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        }
        Spanned s = etContent.getText();
        ImageSpan[] imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
        for (ImageSpan span : imageSpans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            if (selection == start) {
                String str = etContent.getText().toString().substring(start, end);
                //added by luorw for GNSPR #101691 20170822 begin
                String realPath = str.substring(0,str.length()-Constants.URI_END_TAG_LENGTH);
                String type = realPath.substring(realPath.length() - 3, realPath.length());
                if (realPath.equals(Constants.PATH_BILL_UNCHECKED_IMG)) {
                    onBillChecked(start, end);
                    break;
                } else if (realPath.equals(Constants.PATH_BILL_CHECKED_IMG)) {
                    onBillUnChecked(start, end);
                    break;
                }
                //added by luorw for GNSPR #101691 20170822 end
            }
        }
    }

    private AudioManager mAm ;
    private boolean audioIsActive;

    private void closeBackgroundMusicIfNeed() {
        audioIsActive = mAm.isMusicActive();
        if (audioIsActive) {
            int result = mAm.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                LogUtils.d("liyu", "requestAudioFocus successfully.");
            } else {
                LogUtils.d("liyu", "requestAudioFocus failed.");
            }
        }
    }
    
    
    private boolean mAudioFocus;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                	LogUtils.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                default:
                	LogUtils.i(TAG, "AudioFocusChange focus = " + focusChange);
                    break;
            }
        }
    };
    
    
    private void requestAudioFocus() {
    	LogUtils.v(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);
        if (!mAudioFocus) {
            int result = mAm.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_MUSIC, // Use the music stream.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = true;
            } else {
            	LogUtils.e(TAG, "AudioManager request Audio Focus result = " + result);
            }
        }
    }

    private void abandonAudioFocus() {
//        if (audioIsActive && mAm != null) {
//            mAm.abandonAudioFocus(null);
//            audioIsActive = false;
//        }
    	LogUtils.v(TAG, "abandonAudioFocus mAudioFocus = " + mAudioFocus);
        if (mAudioFocus) {
            mAm.abandonAudioFocus(afChangeListener);
            mAudioFocus = false;
        }
    }
    

    //added by luorw for S10 Bug #90235 2017/03/30 begin
    private NetWorkStatusReceiver mNetWorkReceiver = new NetWorkStatusReceiver();

    private void registerNetWorkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.NETWORK_CONNECTIVITY_CHANGE);
        filter.addAction(Constants.AIRPLANE_MODE_CHANGE);
        registerReceiver(mNetWorkReceiver, filter);
    }

    private void unRegisterNetWorkReceiver() {
        if (mNetWorkReceiver != null) {
            unregisterReceiver(mNetWorkReceiver);
        }
    }

    private class NetWorkStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.NETWORK_CONNECTIVITY_CHANGE.equals(action)) {
                boolean isNetworkAvailable = NetWorkUtil.isNetworkAvailable(context);
                LogUtils.i("NetWorkStatusReceiver", "isNetworkAvailable = " + isNetworkAvailable);
                if (mRecordingState == RECORDING_ON && !isNetworkAvailable) {
                    stopRecording();
                }
            } else if (Constants.AIRPLANE_MODE_CHANGE.equals(action)) {
                boolean isOpen = intent.getBooleanExtra("state", false);
                LogUtils.i("NetWorkStatusReceiver", "isOpen = " + isOpen);
                if (mRecordingState == RECORDING_ON && isOpen) {
                    stopRecording();
                }
            }
        }
    }
    //added by luorw for S10 Bug #90235 2017/03/30 end

    @Override
    public void onRecordDisable() {
        stopRecording();
    }
}
