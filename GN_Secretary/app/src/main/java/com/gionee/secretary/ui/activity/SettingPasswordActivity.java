package com.gionee.secretary.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.viewInterface.ISettingPasswordView;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.widget.PasswordInputView;
import com.gionee.secretary.presenter.SettingPasswordPresenter;
import com.gionee.secretary.utils.LogUtils;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

import com.gionee.secretary.R;

import java.lang.ref.WeakReference;


/**
 * Created by zhuboqin on 6/07/16.
 */
public class SettingPasswordActivity extends AmigoActivity implements ISettingPasswordView {

    private AmigoActionBar mActionBar;
    private PasswordInputView passwordInputView;
    private PasswordInputView confirm_passwordInputView;
    private PasswordInputView reset_passwordInputView;
    private TextView tv_pass;

    private String first_pass;

    private static final int MSG_SHOW_INPUT_METHOD = 1;
    private static final int MSG_SHOW_NEXT = 2;
    private static final int MSG_AGAIN = 3;
    private static final long DELAY_MILLIS = 300l;
    private final String TAG = "SettingPasswordActivity";

    private SettingPasswordPresenter mSettingPasswordPresenter;
    private MyHandler mCommonHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_password);
        if(savedInstanceState != null){
            first_pass = savedInstanceState.getString("first_pass");
        }
        initActionBar();
        initView();
        setListener();
        initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        passwordInputView.requestFocus();
        Message msg = mCommonHandler.obtainMessage(MSG_SHOW_INPUT_METHOD);
        mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);

        Intent intent = getIntent();
        int isGoTo = intent.getIntExtra("goToReste", 0);

        LogUtils.e(TAG, "zhengyt-----SettingPasswordActivty  -----isGoTo = "+isGoTo);
        if (isGoTo == 1) {
            reset_passwordInputView.setVisibility(View.VISIBLE);
            confirm_passwordInputView.setVisibility(View.GONE);
            passwordInputView.setVisibility(View.GONE);
            tv_pass.setText(R.string.reset_pw);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("first_pass",first_pass);
        super.onSaveInstanceState(outState);
    }

    private void initView() {
        passwordInputView = (PasswordInputView) findViewById(R.id.passwordInputView);
        confirm_passwordInputView = (PasswordInputView) findViewById(R.id.confirm_passwordInputView);
        reset_passwordInputView = (PasswordInputView) findViewById(R.id.reset_passwordInputView);
        tv_pass = (TextView) findViewById(R.id.tv_pass);
        passwordInputView.setVisibility(View.VISIBLE);
        confirm_passwordInputView.setVisibility(View.GONE);
        reset_passwordInputView.setVisibility(View.GONE);
    }

    private void initPresenter() {
        mSettingPasswordPresenter = new SettingPasswordPresenter(this, this);
    }

    private void setListener() {
        passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == Constants.PASSWORD_LENGTH) {
                    first_pass = passwordInputView.getText().toString();
                    Message msg = mCommonHandler.obtainMessage(MSG_SHOW_NEXT);
                    mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
                }
            }
        });

        confirm_passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mSettingPasswordPresenter.SettingPassword(s, confirm_passwordInputView.getText().toString(), first_pass);
            }
        });

        reset_passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LogUtils.e(TAG, "zhengyt-----afterTextChanged  -----" + s);
                if(reset_passwordInputView.getVisibility() == View.VISIBLE){
                first_pass = reset_passwordInputView.getText().toString();
                mSettingPasswordPresenter.SettingPassword(s, reset_passwordInputView.getText().toString(), reset_passwordInputView.getText().toString(), true, 0);
                }
            }
        });
    }

    private void initActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View actionBarLayout = getLayoutInflater().inflate(
                R.layout.actionbar_set_password, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBar.setCustomView(actionBarLayout, param);
        mActionBar.show();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SettingPasswordActivity> mActivity;

        public MyHandler(SettingPasswordActivity activity) {
            mActivity = new WeakReference<SettingPasswordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SettingPasswordActivity settingPasswordActivity = mActivity.get();
            if (settingPasswordActivity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_INPUT_METHOD:
                    settingPasswordActivity.showInputMethod();
                    break;
                case MSG_SHOW_NEXT:
                    LogUtils.i(settingPasswordActivity.TAG, "handleMessage....first_pass:" + settingPasswordActivity.first_pass);
                    settingPasswordActivity.tv_pass.setText(R.string.confirm_pw);
                    settingPasswordActivity.confirm_passwordInputView.requestFocus();
                    settingPasswordActivity.confirm_passwordInputView.setVisibility(View.VISIBLE);
                    settingPasswordActivity.passwordInputView.setVisibility(View.GONE);
                    break;
                case MSG_AGAIN:
                    settingPasswordActivity.tv_pass.setText(R.string.error_pw);
                    settingPasswordActivity.confirm_passwordInputView.setText("");
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void inputAgain() {
        LogUtils.e(TAG, "inputAgain  -----");
        Message msg = mCommonHandler.obtainMessage(MSG_AGAIN);
        mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordInputView.getApplicationWindowToken(), 0);
    }

}
