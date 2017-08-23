package com.gionee.secretary.ui.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import amigoui.app.AmigoActivity;
import amigoui.app.AmigoActionBar;

import com.gionee.secretary.R;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoTextView;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.viewInterface.IPasswordVerifyView;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.widget.PasswordInputView;
import com.gionee.secretary.presenter.PasswordVerifyPresenter;
import com.gionee.secretary.presenter.SettingPasswordPresenter;
import com.gionee.secretary.module.settings.LoginModel;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;
import com.youju.statistics.YouJuAgent;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by zhuboqin on 6/07/16.
 */
public class VerifyPassWordActivity extends AmigoActivity implements IPasswordVerifyView, TextWatcher, View.OnClickListener {

    private final String TAG = "VerifyPassWordActivity";
    private TextView tv_pass;
    private PasswordInputView passwordInputView;
    private PasswordInputView resetInputView;
    private PasswordInputView confirmInputView;
    private AmigoActionBar mActionBar;
    private static final int MSG_SHOW_INPUT_METHOD = 1;
    private static final long DELAY_MILLIS = 300l;
    private InputMethodManager imm;
    private PasswordVerifyPresenter mPasswordVerifyPresenter;
    private static final int MSG_CLEAR = 9;
    private static final int MSG_AGAIN = 3;
    private static final int ERROR_MSG = -1;
    //    private twChangerListener psChangeListener = new twChangerListener();
//    private twNullChangeListener psNullChangeListener = new twNullChangeListener();
    private static final int RESET_FAIL_COUNT = 4;
    private static final int RESET_PASSWORD = 5;
    private static final int CONFIRM_PASSWORD = 6;
    private TextView mGetBack;
    private static int pass_fail_count = 1;
    private static final int MAX_FAIL_COUNT = 5;
    private static final int COOLING_TIME = 60000;
    private Thread mThread;
    private LoginModel mLoginModel;
    private SettingPasswordPresenter mSettingPasswordPresenter;
    private int isFromStart = 0;
    private PasswordModel mPasswordModel;
    private boolean isResetPassWord;

    /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
    private String first_pass;
    MyHandler mCommonHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<VerifyPassWordActivity> mActivity;

        public MyHandler(VerifyPassWordActivity activity) {
            mActivity = new WeakReference<VerifyPassWordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final VerifyPassWordActivity verifyPassWordActivity = mActivity.get();
            if (verifyPassWordActivity == null) {
                return;
            }
            verifyPassWordActivity.isResetPassWord = false;
            switch (msg.what) {
                case MSG_CLEAR:
                    verifyPassWordActivity.passwordInputView.setText("");
                    break;
                case MSG_AGAIN:
                    LogUtils.e(verifyPassWordActivity.TAG, "handleMessage  -----handleMessage---- MSG_AGAIN");
                    if (pass_fail_count >= MAX_FAIL_COUNT) {
                        verifyPassWordActivity.tv_pass.setText(R.string.cool_time);
                        verifyPassWordActivity.mGetBack.setVisibility(View.VISIBLE);
                        verifyPassWordActivity.passwordInputView.setEnabled(false);
                        LogUtils.e(verifyPassWordActivity.TAG, "---------isEnable=-------" + verifyPassWordActivity.passwordInputView.isEnabled());
                        //added by luorw for  GNSPR #67676 20170220 begin
                        verifyPassWordActivity.startFreezePassword();
                        //added by luorw for  GNSPR #67676 20170220 end
                        new Thread() {

                            @Override
                            public void run() {
                                SystemClock.sleep(150);
                                verifyPassWordActivity.mCommonHandler.sendEmptyMessage(MSG_CLEAR);
                            }

                        }.start();
                        break;
                    } else {
                        verifyPassWordActivity.tv_pass.setText(verifyPassWordActivity.getApplicationContext().getString(R.string.pw_error_count, (5 - pass_fail_count)));
                        verifyPassWordActivity.passwordInputView.setText("");
                        pass_fail_count++;
                        break;
                    }
                case MSG_SHOW_INPUT_METHOD:
                    verifyPassWordActivity.showInputMethod();
                    break;
                case RESET_FAIL_COUNT:
                    LogUtils.e(verifyPassWordActivity.TAG, "zhengyt--handler---RESET_FAIL_COUNT  -----");
                    pass_fail_count = 1;
                    verifyPassWordActivity.tv_pass.setText(R.string.write_pw);
                    verifyPassWordActivity.passwordInputView.setEnabled(true);
                    verifyPassWordActivity.passwordInputView.setText("");
                    //added by luorw for GNSPR #71286 20170308 begin
                    verifyPassWordActivity.mGetBack.setVisibility(View.INVISIBLE);
                    //added by luorw for GNSPR #71286 20170308 end
                    //added by luorw for GNSPR #75710 20170324 begin
                    verifyPassWordActivity.passwordInputView.setVisibility(View.VISIBLE);
                    verifyPassWordActivity.resetInputView.setVisibility(View.GONE);
                    verifyPassWordActivity.confirmInputView.setVisibility(View.GONE);
                    //added by luorw for GNSPR #75710 20170324 end
                    verifyPassWordActivity.showInputMethod();
                    verifyPassWordActivity.imm.showSoftInput(verifyPassWordActivity.passwordInputView, InputMethodManager.SHOW_IMPLICIT);
                    break;
                case RESET_PASSWORD:
                    LogUtils.e(verifyPassWordActivity.TAG, "zhengyt--handler---RESET_PASSWORD  -----");
//                	Intent intent = new Intent(getApplicationContext(),SettingPasswordActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//                    intent.putExtra("goToReste",1);
//                    startActivity(intent);
                    /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
                    verifyPassWordActivity.showInputMethod();
                    verifyPassWordActivity.confirmInputView.setVisibility(View.GONE);
                    verifyPassWordActivity.tv_pass.setText(R.string.reset_pw);
                    verifyPassWordActivity.mGetBack.setVisibility(View.GONE);
                    verifyPassWordActivity.resetInputView.setVisibility(View.VISIBLE);
                    verifyPassWordActivity.passwordInputView.setVisibility(View.GONE);
                    verifyPassWordActivity.resetInputView.requestFocus();
                    pass_fail_count = 1;
                    verifyPassWordActivity.isResetPassWord = true;
                    verifyPassWordActivity.imm.showSoftInput(verifyPassWordActivity.resetInputView, InputMethodManager.SHOW_FORCED);
                    break;
                /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
                case CONFIRM_PASSWORD:
                    verifyPassWordActivity.showInputMethod();
                    verifyPassWordActivity.tv_pass.setText(R.string.confirm_pw);
                    verifyPassWordActivity.mGetBack.setVisibility(View.GONE);
                    verifyPassWordActivity.resetInputView.setVisibility(View.GONE);
                    verifyPassWordActivity.confirmInputView.setVisibility(View.VISIBLE);
                    verifyPassWordActivity.passwordInputView.setVisibility(View.GONE);
                    verifyPassWordActivity.confirmInputView.requestFocus();
                    break;
                case ERROR_MSG:
                    verifyPassWordActivity.showInputMethod();
                    verifyPassWordActivity.tv_pass.setText(R.string.error_pw);
                    verifyPassWordActivity.confirmInputView.setText("");
                    break;
                /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
                default:
                    break;
            }
        }
    }

    //added by luorw for  GNSPR #67676 20170220 begin
    private void startFreezePassword() {
        mPasswordModel = PasswordModel.getInstance(getApplicationContext());
        mPasswordModel.updateFreezePwdState(true);
        Intent intent = new Intent(Constants.FREEZING_PASSWORD_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, Constants.FREEZING_PASSWORD_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pi);
    }

    //added by luorw for  GNSPR #67676 20170220 end
    @Override
    protected void onNewIntent(Intent intent) {
        LogUtils.e(TAG, "VerifyPassWordActivity onnewIntnet");
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        LogUtils.e(TAG, "VerifyPassWordActivity activiy oncreate");
        initPresenter();
        initKeyBoard();
        //added by luorw for  GNSPR #67676 20170220 begin
        registerPasswordFreezeReceiver();
        //added by luorw for  GNSPR #67676 20170220 end
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //added by luorw for  GNSPR #67676 20170220 begin
        unregisterPasswordFreezeReceiver();
        //added by luorw for  GNSPR #67676 20170220 end
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPasswordVerifyPresenter.changeActionBar();
        //modified by luorw for  GNSPR #67676 20170220 begin
        if (!isFreezePwd()) {
            passwordInputView.clearFocus();
            passwordInputView.setVisibility(View.VISIBLE);
            Message msg = mCommonHandler.obtainMessage(MSG_SHOW_INPUT_METHOD);
            mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
            resetInputView.setVisibility(View.INVISIBLE);
            confirmInputView.setVisibility(View.INVISIBLE);
            // added by liyu Fixed #77068 start
            mCommonHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // added by liyu Fixed #75302 start
                    imm.showSoftInput(passwordInputView, InputMethodManager.SHOW_IMPLICIT);
                    // added by liyu Fixed #75302 end
                }
            }, 380);
            // added by liyu Fixed #77068 end

        } else {
            passwordInputView.clearFocus();
            if (isResetPassWord) {
                imm.showSoftInput(resetInputView, InputMethodManager.SHOW_IMPLICIT);
            } else {
                hideInputMethod();
            }
        }
        //modified by luorw for  GNSPR #67676 20170220 end
        Intent intent = getIntent();
        isFromStart = intent.getIntExtra("isFromStart", 0);
        isResetPassWord = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideInputMethod();
    }

    @Override
    public void onBackPressed() {
    	passwordInputView.setText("");
        mPasswordVerifyPresenter.backToUpView();
    }

    private void initPresenter() {
        mSettingPasswordPresenter = new SettingPasswordPresenter(this, this);
        mPasswordVerifyPresenter = new PasswordVerifyPresenter(this, this);
    }

    @Override
    public void initActionBar(boolean isClosePw) {
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordVerifyPresenter.backToUpView();
            }
        });
        View actionBarLayout = getLayoutInflater().inflate(
                R.layout.actionbar_set_password, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        AmigoTextView tv_password = (AmigoTextView) actionBarLayout.findViewById(R.id.tv_password);
        if (isClosePw) {//关闭密码保护
            tv_password.setText(this.getResources().getString(R.string.close_protect));
        } else {
            tv_password.setText(this.getResources().getString(R.string.pw_protect));
        }
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                mPasswordVerifyPresenter.backToUpView();
            }
        });
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBar.setCustomView(actionBarLayout, param);
        mActionBar.show();
    }

    private void initView() {
        tv_pass = (TextView) findViewById(R.id.tv_pass);
        passwordInputView = (PasswordInputView) findViewById(R.id.passwordInputView);
        passwordInputView.addTextChangedListener(this);
        mPasswordModel = PasswordModel.getInstance(getApplicationContext());
        resetInputView = (PasswordInputView) findViewById(R.id.reset_passwordInputView);
        resetInputView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                LogUtils.e(TAG, "zhengyt -----aftetTextChange -----=" + s);
//				mSettingPasswordPresenter.SettingPassword(s,resetInputView.getText().toString(),resetInputView.getText().toString(),true,isFromStart);
//				if(s.length() == Constants.PASSWORD_LENGTH && isFromStart == 1){
//					mPasswordModel.updateLockState(false);
//					mPasswordModel.updatePassword(s.toString());
//					finish();
//				}
                if (s.length() == Constants.PASSWORD_LENGTH) {
                    first_pass = s.toString();
                    Message msg = mCommonHandler.obtainMessage(CONFIRM_PASSWORD);
                    mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
                }
            }
        });

        confirmInputView = (PasswordInputView) findViewById(R.id.confirm_passwordInputView);
        confirmInputView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                LogUtils.e(TAG, "zhengyt -----aftetTextChange -----=" + s);
                mSettingPasswordPresenter.SettingPassword(s, s.toString(), first_pass, true, isFromStart);
            }
        });

        mGetBack = (TextView) findViewById(R.id.get_back);
        mGetBack.setVisibility(View.GONE);
        mGetBack.setOnClickListener(this);
        mLoginModel = new LoginModel(getApplicationContext());
        //added by luorw for  GNSPR #67676 20170220 begin
        setFreezePwdLayout();
        //added by luorw for  GNSPR #67676 20170220 end
    }

    private boolean isFreezePwd() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFreezePwd = sharedPreferences.getBoolean(Constants.FREEZING_PASSWORD_STATE, false);
        return isFreezePwd;
    }

    //added by luorw for  GNSPR #67676 20170220 begin
    private void setFreezePwdLayout() {
        if (isFreezePwd()) {
            tv_pass.setText(R.string.cool_time);
            mGetBack.setVisibility(View.VISIBLE);
            passwordInputView.setText("");
            passwordInputView.setEnabled(false);
        } else {
            tv_pass.setText(R.string.write_pw);
            passwordInputView.setEnabled(true);
            passwordInputView.setText("");
        }
    }

    //added by luorw for  GNSPR #67676 20170220 end
    private void initKeyBoard() {
        if (imm == null) {
            imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        }
    }

    @Override
    public void passwordAgain() {
        LogUtils.e(TAG, "passwordAgain  -----");
        Message msg = mCommonHandler.obtainMessage(MSG_AGAIN);
        mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void showInputMethod() {
        //fixed 70370
        passwordInputView.requestFocus();
        //imm.showSoftInput(passwordInputView, 0);
    }

    @Override
    public void hideInputMethod() {
        //modified by luorw for GNSPR #79076 2017-04-13 begin
        if (imm != null) {
            //modified by luorw for GNSPR #79076 2017-04-13 end
            imm.hideSoftInputFromWindow(passwordInputView.getApplicationWindowToken(), 0);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mPasswordVerifyPresenter.verifyPassword(s, passwordInputView.getText().toString());
    }

    @Override
    public void resetFailCount() {
        // TODO Auto-generated method stub
        pass_fail_count = 1;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.get_back: {
                YouJuAgent.onEvent(this, this.getResources().getString(R.string.click_find_password));
                if (mLoginModel != null) {
                    LogUtils.e(TAG, "zhengyt-----onGetBack  -----");
                    mLoginModel.amigoAccountLogin();
                    mLoginModel.setAmigoAccountLoginLinster(new LoginModel.AmigoAccountLoginLinster() {
                        @Override
                        public void loginOK() {
                            mLoginModel.setPassWord(true, mCommonHandler);
                            mLoginModel.checkLogInfo();
                        }

                        @Override
                        public void loginNo() {
                            // Gionee liyu 2017-02-07 modify for GNSPR #65646 begin
                            VerifyPassWordActivity.this.startActivity(new Intent(VerifyPassWordActivity.this, ForgetPasswordActivity.class));
                            // Gionee liyu 2017-02-07 modify for GNSPR #65646 end
                        }
                    });
                }
                break;
            }
        }
    }


    //added by luorw for 60611 20170207 begin
    @Override
    protected void onStop() {
        super.onStop();
        String topApp = getLauncherTopApp(this);
        LogUtils.i(TAG, "PasswordBaseActivity..." + this.getClass() + "....onStop....getLauncherTopApp:" + topApp + "    ,isLock:" + PasswordModel.getInstance(this).getLockSwitch());
        //modified by luorw 2017-04-13 for GNSPR #79076当详情分享框弹出来时，识别到的topApp是android，应该过滤掉，否则会上锁并跳转到输入密码界面
        if (!"android".equals(topApp) && !getPackageName().equals(topApp) && PasswordModel.getInstance(this).getLockSwitch()) {//打开的是其他用用如状态栏下拉进的
            LogUtils.i(TAG, "PasswordBaseActivity...." + this.getClass() + "    setPWD....LockState....true");
            PasswordModel.getInstance(this).updateLockState(true);
        }
        Intent intent = getIntent();
        boolean isClosePw = intent.getBooleanExtra(Constants.EXTRA_CLOSE_PASSWORD_SWITCH, false);
        if (isClosePw) {
            Intent in = new Intent();
            in.putExtra("isClosePw", true);
            setResult(1, in);
            finish();
        }
    }

    //added by luorw for 60611 20170207 end
    public String getLauncherTopApp(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
        if (null != appTasks && !appTasks.isEmpty()) {
            LogUtils.i(TAG, "getLauncherTopApp..." + appTasks.get(0).topActivity.getPackageName());
            return appTasks.get(0).topActivity.getPackageName();
        }
        LogUtils.i(TAG, "getLauncherTopApp..." + "null");
        return "";
    }

    private void showCanNotResetPop() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(getApplicationContext());
        builder.setTitle(R.string.settings);
        builder.setMessage(R.string.can_not_reset_password);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /*ad by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
    @Override
    public void inputAgain() {
        LogUtils.e(TAG, "inputAgain  -----");
        Message msg = mCommonHandler.obtainMessage(ERROR_MSG);
        mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
    }

    //added by luorw for  GNSPR #67676 20170220 begin
    public class PasswordFreezeReceiver extends BroadcastReceiver {
        private String TAG = "PasswordFreezeReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.e(TAG, action);
            if (action.equals(Constants.UNDO_FREEZING_PASSWORD_ACTION)) {
                Message msg = mCommonHandler.obtainMessage(RESET_FAIL_COUNT);
                mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
            }
        }
    }

    private BroadcastReceiver mReceiver = new PasswordFreezeReceiver();

    public void registerPasswordFreezeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UNDO_FREEZING_PASSWORD_ACTION);
        registerReceiver(mReceiver, filter);
    }

    public void unregisterPasswordFreezeReceiver() {
        unregisterReceiver(mReceiver);
    }
    //added by luorw for  GNSPR #67676 20170220 end
}
