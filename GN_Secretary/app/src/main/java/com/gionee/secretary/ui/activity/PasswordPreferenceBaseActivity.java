package com.gionee.secretary.ui.activity;

import amigoui.preference.AmigoPreferenceActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

import java.util.List;

public class PasswordPreferenceBaseActivity extends AmigoPreferenceActivity {

    private String TAG = "PasswordPreferenceBaseActivity";
    // 判断是否需要显示密码界面
    private boolean isFromActivityResult = false;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    //modify by zhengjl at 2017-2-23 for  GNSPR #68151 begin
    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.i(TAG, "zhengyt -----PasswordBaseActivity...");
        boolean islock = PasswordModel.getInstance(this).isShowLockScreen();
        LogUtils.i(TAG, "PasswordBaseActivity...onResume,,,,,,," + this.getClass() + "  ,  isLock:" + islock + " , isFromActivityResult = " + isFromActivityResult);
        if (PasswordModel.getInstance(this).isShowLockScreen() && !isFromActivityResult) {
            Intent intent = new Intent(this, VerifyPassWordActivity.class);
            intent.putExtra("isFromStart", 1);
            startActivity(intent);
        }
        isFromActivityResult = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    //modify by zhengjl at 2017-2-23 for  GNSPR #68151 end

    @Override
    protected void onStop() {
        super.onStop();
        if (!mContext.getPackageName().equals(getLauncherTopApp(mContext)) && PasswordModel.getInstance(mContext).getLockSwitch()) {//打开的是其他用用如状态栏下拉进的
            if (PasswordModel.getInstance(mContext).getIsFromAmigAccount()) {
                PasswordModel.getInstance(mContext).updateLockState(false);
                PasswordModel.getInstance(mContext).setInAmigoLog(false);
            } else {
                LogUtils.e(TAG, "onStop---------updateLockState-----------true");
                PasswordModel.getInstance(mContext).updateLockState(true);
            }
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //modified by luorw for 60611 20170207 begin
        if (data == null || !data.getBooleanExtra("isClosePw", false)) {
            isFromActivityResult = true;
            //modify by zhengjl at 2017-2-10 for GNSPR #66880 begin
            PasswordModel.getInstance(this).updateLockState(false);
            //modify by zhengjl at 2017-2-10 for GNSPR #66880 end
        }
        //modified by luorw for 60611 20170207 end
    }

    public void setFromActivityResult(boolean flag) {
        isFromActivityResult = flag;
    }
}
