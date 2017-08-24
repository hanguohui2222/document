package com.gionee.secretary.ui.activity;

import amigoui.app.AmigoActionBar;
import amigoui.preference.AmigoListPreference;
import amigoui.preference.AmigoPreferenceScreen;
import amigoui.preference.AmigoSwitchPreference;
import amigoui.preference.AmigoPreference;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.module.settings.LoginModel;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.presenter.SettingPresenter;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.R;
import com.gionee.secretary.ui.viewInterface.ISettingView;

import amigoui.preference.AmigoPreferenceCategory;


/**
 * Created by liu on 5/12/16.
 */
public class SettingActivity extends PasswordPreferenceBaseActivity implements ISettingView, AmigoPreference.OnPreferenceChangeListener {

    private AmigoPreferenceScreen rootPref;
    private AmigoListPreference mTravelMethodListPref;
    private AmigoSwitchPreference mScheduleRemindPref;
    private AmigoPreferenceScreen mNotificationRingPtrf;
    private AmigoSwitchPreference mShowExpressStatusPref;
    private AmigoSwitchPreference mPasswordSwitchPref;
    private AmigoSwitchPreference mShowWidgetPreference;
//    private AmigoSwitchPreference  mScheduleBroadcastPref;
//    private AmigoPreferenceScreen mBroadcastTimePref;

    private SettingModel mSettingModel;
    private SettingPresenter mSettingPresenter;
    private AmigoActionBar mActionBar;

    private LoginModel mLoginModel;
    private AmigoPreference mLoginPref;
    private AmigoPreference mLoginOutPref;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 102;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.activity_setting);
        mSettingModel = SettingModel.getInstance(this);
        mLoginModel = new LoginModel(getApplicationContext());
        initActionBar();
        initPreference();
        initExpressPreference();
        initPresenter();
        setPreferenceListener();
        updateTravelModeSummery();
        //added by luorw for GNSPR #68429 20170221 begin
        enableWidgetSwitch();
        if (mSettingModel.isShowWidget()) {
            openWidgetSwitch();
        } else {
            closeWidgetSwitch();
        }
        //added by luorw for GNSPR #68429 20170221 end
        checkPasswordSwitch();
    }

    private void initExpressPreference(){
        mShowExpressStatusPref.setChecked(SettingModel.getInstance(this).isShowStatusOfExpress());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        View actionBarLayout = getLayoutInflater().inflate(
                R.layout.actionbar_setting_detail, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBar.setCustomView(actionBarLayout, param);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionBar.show();
    }

    private void initPresenter() {
        mSettingPresenter = new SettingPresenter(this, this);
    }

    private void checkPasswordSwitch() {
        mSettingPresenter.checkoutPasswordSwitch();
    }

    private void updateTravelModeSummery() {
        mSettingPresenter.updateTravelModePreferenceSummery();
    }

    @Override
    public void openPasswordSwitch() {
        mPasswordSwitchPref.setChecked(true);
    }

    @Override
    public void closePasswordSwitch() {
        mPasswordSwitchPref.setChecked(false);
    }

    @Override
    public void enableWidgetSwitch() {
        mShowWidgetPreference.setEnabled(true);
    }

    @Override
    public void openWidgetSwitch() {
        mShowWidgetPreference.setChecked(true);
    }

    @Override
    public void closeWidgetSwitch() {
        mShowWidgetPreference.setChecked(false);
    }

    @SuppressWarnings("deprecation")
    private void initPreference() {
        rootPref = (AmigoPreferenceScreen) findPreference(Constants.ROOT_PREFERENCE_KEY);
        mTravelMethodListPref = (AmigoListPreference) findPreference(Constants.TRAVEL_MODE_PREFERENCE_KEY);
        mScheduleRemindPref = (AmigoSwitchPreference) findPreference(Constants.REMIND_SWITCH_PREFERENCE_KEY);
        mNotificationRingPtrf = (AmigoPreferenceScreen) findPreference(Constants.NOTIFICATION_RING_PREFERENCE_KEY);
        mShowExpressStatusPref = (AmigoSwitchPreference) findPreference(Constants.EXPRESS_SWITCH_PREFERENCE_KEY);
        mPasswordSwitchPref = (AmigoSwitchPreference) findPreference(Constants.PASSWORD_SWITCH_PREFERENCE_KEY);
        mShowWidgetPreference = (AmigoSwitchPreference) findPreference(Constants.WIDGET_SWITCH_PREFERENCE_KEY);
        mLoginPref = (AmigoPreference) findPreference(Constants.USER_PREFERENCE_KEY);
//        mScheduleBroadcastPref = (AmigoSwitchPreference) findPreference(Constants.BROADCAST_SWITCH_PREFERENCE_KEY);
//        mBroadcastTimePref = (AmigoPreferenceScreen)findPreference(Constants.BROADCAST_TIME_PREFERENCE_KEY);
//        String broadcastTime = SettingModel.getInstance(this).getBroadcastTime();
//        if(!"".equals(broadcastTime) && broadcastTime != null){
//            mBroadcastTimePref.setSummary(broadcastTime);
//        }else{
//            mBroadcastTimePref.setSummary("默认早上8:00");
//        }
    }

    @Override
    public void updateTravelModePreferenceSummery(String summery) {
        mTravelMethodListPref.setSummary(summery);
    }

    private void setPreferenceListener() {
        mTravelMethodListPref.setOnPreferenceChangeListener(this);
        mScheduleRemindPref.setOnPreferenceChangeListener(this);
        mNotificationRingPtrf.setOnPreferenceChangeListener(this);
        mShowExpressStatusPref.setOnPreferenceChangeListener(this);
        mPasswordSwitchPref.setOnPreferenceChangeListener(this);
        mShowWidgetPreference.setOnPreferenceChangeListener(this);
//        mScheduleBroadcastPref.setOnPreferenceChangeListener(this);
//        mBroadcastTimePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(AmigoPreference preference, Object newValue) {
        return mSettingPresenter.onPreferenceChange(preference, newValue);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSettingPresenter.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void showToast(int content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    /*amigo 帐号相关的 start
    @Override*/
    public boolean onPreferenceTreeClick(AmigoPreferenceScreen preferenceScreen, AmigoPreference preference) {

        if (preference == mLoginOutPref && mLoginModel != null) {
            mLoginModel.logout();
            setFromActivityResult(true);
        }

        if (mLoginModel != null && preference == mLoginPref) {
            mLoginModel.login();
            PasswordModel.getInstance(this).setInAmigoLog(true);
            mLoginModel.setAmigoAccountLoginLinster(new LoginModel.AmigoAccountLoginLinster() {
                @Override
                public void loginOK() {
                    Toast.makeText(SettingActivity.this,"用户已登录",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void loginNo() {

                }
            });
        }

        //添加点击“通知铃声”的事件
        if (preference == mNotificationRingPtrf) {
            //added by luorw for GNSPR #102711 20170823 begin
            requestPermission();
            //added by luorw for GNSPR #102711 20170823 end
        }

//        if (preference == mBroadcastTimePref) {
//            mSettingPresenter.setBroadcastTime(this, new IsetBroadcastTimeListener() {
//                @Override
//                public void showBroadcastTime(String broadcastTime) {
//                    mBroadcastTimePref.setSummary(broadcastTime);
//                }
//            });
//        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    //added by luorw for GNSPR #102711 20170823 begin
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//授权成功
                        mSettingPresenter.setNotification();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {//点击拒绝授权
                    }
                }
                break;
            default:
                break;
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_EXTERNAL_STORAGE);
        } else {
            mSettingPresenter.setNotification();
        }
    }
    //added by luorw for GNSPR #102711 20170823 end

    public interface IsetBroadcastTimeListener {
        void showBroadcastTime(String broadcastTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i("liyy", "settingsActivity....onResume ...");
        if (mLoginModel != null) {
            mLoginModel.amigoAccountLogin();
            mLoginModel.setAmigoAccountLoginLinster(new LoginModel.AmigoAccountLoginLinster() {
                @Override
                public void loginOK() {
                    String userName = mLoginModel.mGioneeAccount.getUsername();
                    AmigoPreference loginOutCategory = findPreference(getString(R.string.loginout_key));

                    setLoginSummary(userName);
                    mLoginPref.setTitle(getString(R.string.login_user));
                    setLoginUserPhoto();
                    //设置头像
                    if (loginOutCategory == null) {
                        addLoginOutPref();
                    }
                }

                @Override
                public void loginNo() {
                    setLoginSummary(getString(R.string.login_tips));
                    mLoginPref.setTitle(getString(R.string.login));
                    setDefaultUserPhoto();
                    delLoginOutPref();
                }
            });
        }
        //初始化显示的铃声
        String ringtoneTitle = mSettingModel.getDefaultRingtoneTitle();

        //modify by zhengjl at 2017-2-23 for GNSPR #69241 begin
        if (ringtoneTitle == null || mSettingPresenter.isFileDeleted()) {
            Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this,
                    RingtoneManager.TYPE_NOTIFICATION);
            ringtoneTitle = mSettingPresenter.getRingtoneTitleFromUri(ringtoneUri);
        }
        updateNotifyRingPreferenceSummery(ringtoneTitle);
        //modify by zhengjl at 2017-2-23 for GNSPR #69241 end
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void setDefaultUserPhoto() {
        mLoginPref.setIcon(R.drawable.user_icon);
    }

    private void setLoginUserPhoto() {
        Bitmap photo = mLoginModel.getUserPhoto();
        if (photo == null) {
            return;
        }
        int width = photo.getWidth();
        int height = photo.getHeight();
        int newWidth = 380;  //Gionee zhengyt 2017-3-13 modify for GNSPR#71424 
        int newHeight = 380;
        Matrix mtx = new Matrix();
        mtx.postScale(((float) newWidth) / width, ((float) newHeight) / height);
        Bitmap resizePhoto = Bitmap.createBitmap(photo, 0, 0, width, height, mtx, true);
        mLoginPref.setIcon(new BitmapDrawable(resizePhoto));
    }

    private void setLoginSummary(String userName) {
        if (userName != null && !TextUtils.isEmpty(userName)) {
            LogUtils.i("liyy", "settingsActivity....onResume ...LoginName:" + userName);
            mLoginPref.setSummary(userName);
        }
    }

    private void delLoginOutPref() {
        AmigoPreference loginOutCategory = findPreference(getString(R.string.loginout_key));
        if (loginOutCategory != null) {
            rootPref.removePreference(loginOutCategory);
        }

    }

    private void addLoginOutPref() {
        AmigoPreferenceCategory loginOutCategory = new AmigoPreferenceCategory(this);
        loginOutCategory.setTitle(R.string.loginout);
        loginOutCategory.setKey(getString(R.string.loginout_key));
        rootPref.addPreference(loginOutCategory);

        mLoginOutPref = new AmigoPreference(this);
        mLoginOutPref.setTitle(R.string.loginout);
//        mLoginOutPref.setOnPreferenceClickListener(this);

        loginOutCategory.addPreference(mLoginOutPref);
    }


//        @Override
//    public boolean onPreferenceClick(AmigoPreference amigoPreference) {
//        if (amigoPreference == mLoginOutPref && mLoginModel != null && mLoginModel.isAmigoAccountLogin()) {
//
//            mLoginModel.loginOut(new VerifyListener() {
//                @Override
//                public void onSucess(Object o) {
//                }
//
//                @Override
//                public void onCancel(Object o) {
//                }
//            });
//            return true;
//        }
//
//        return false;
//    }
    /*amigo 帐号相关的 end*/


    /*
    更新铃声提示
     */
    @Override
    public void updateNotifyRingPreferenceSummery(String ringName) {
        mNotificationRingPtrf.setSummary(ringName);
    }


}
