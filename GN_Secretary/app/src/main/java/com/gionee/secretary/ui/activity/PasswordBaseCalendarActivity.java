package com.gionee.secretary.ui.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

import java.util.List;

/**
 * Created by zhuboqin on 21/07/16.
 */
public class PasswordBaseCalendarActivity extends Activity {
    private String TAG = "PasswordBaseCalendarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //modify by zhengjl at 2017-2-23 for  GNSPR #68151 begin
    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.i(TAG, "zhengyt -----PasswordBaseActivity...");
        boolean islock = PasswordModel.getInstance(this).isShowLockScreen();
        LogUtils.i(TAG, "PasswordBaseActivity...onResume,,,,,,,isFromActivityResult = " + isFromActivityResult + "  ,  isLock:" + islock);
        // Gionee sunyang 2017-01-18 modify for GNSPR #65583 begin
        if (PasswordModel.getInstance(this).isShowLockScreen() && !isFromActivityResult) {
            Intent intent = new Intent(this, VerifyPassWordActivity.class);
            intent.putExtra("isFromStart", 1);
            startActivity(intent);
        }
        isFromActivityResult = false;
        // Gionee sunyang 2017-01-18 modify for GNSPR #65583 end
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
    //modify by zhengjl at 2017-2-23 for  GNSPR #68151 end

    @Override
    protected void onStop() {
        super.onStop();
        String topApp = getLauncherTopApp(this);
        LogUtils.i(TAG, "PasswordBaseActivity..." + this.getClass() + "....onStop....getLauncherTopApp:" + topApp + "    ,isLock:" + PasswordModel.getInstance(this).getLockSwitch());
        if (!getPackageName().equals(topApp) && PasswordModel.getInstance(this).getLockSwitch()) {//打开的是其他用用如状态栏下拉进的
            LogUtils.i(TAG, "PasswordBaseActivity...." + this.getClass() + "    setPWD....LockState....true");
            PasswordModel.getInstance(this).updateLockState(true);
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


    // 判断是否需要显示密码界面
    private boolean isFromActivityResult = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isFromActivityResult = true;
    }

}
