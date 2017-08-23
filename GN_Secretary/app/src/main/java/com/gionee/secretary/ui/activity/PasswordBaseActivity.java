package com.gionee.secretary.ui.activity;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.gionee.secretary.R;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

import java.util.List;

import amigoui.app.AmigoActivity;


/**
 * Created by zhuboqin on 21/07/16.
 */
public class PasswordBaseActivity extends AmigoActivity {
    private String TAG = "PasswordBaseActivity";

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
        LogUtils.i(TAG, "PasswordBaseActivity...onStart,,,,,,," + this.getClass() + "  ,  isLock:" + islock + " , isFromActivityResult = " + isFromActivityResult);
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
        //modified by luorw for 1602：#66378，#66883  2017-04-19 begin
        if ("".equals(topApp)) {
            LogUtils.i(TAG, "PasswordBaseActivity.....onStop....getLauncherTopApp为空---return");
            return;
        }
        //modified by luorw for 1602：#66378，#66883  2017-04-19 end
        //modified by luorw for SW17G02A01 #878362017-04-19 begin
        /*if (FeatureOption.GN_APK_DETAIL_SHARE_FILTER_SECURITY) {
            if ("com.gionee.security".equals(topApp) || "android".equals(topApp)) {
                LogUtils.i(TAG, "PasswordBaseActivity.....onStop....过滤com.gionee.security和android---return");
                return;
            }
        }*/
        //modified by luorw for SW17G02A01 #87836 2017-04-19 end
        LogUtils.i(TAG, "PasswordBaseActivity..." + this.getClass() + "....onStop....getLauncherTopApp:" + topApp + "    ,isLock:" + PasswordModel.getInstance(this).getLockSwitch());
        if (!getPackageName().equals(topApp) && PasswordModel.getInstance(this).getLockSwitch()) {//打开的是其他用用如状态栏下拉进的
            LogUtils.i(TAG, "PasswordBaseActivity...." + this.getClass() + "    setPWD....LockState....true");
            PasswordModel.getInstance(this).updateLockState(true);
        }
    }

    //modified by luorw for SW17G02A01 #87836 && 1602ROM修改之后获取不到最上层的包名而无法上锁 2017-04-19 begin
    public String getLauncherTopApp(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                LogUtils.i(TAG, "getLauncherTopApp..." + appTasks.get(0).topActivity.getPackageName());
                return appTasks.get(0).topActivity.getPackageName();
            }
        } else {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!android.text.TextUtils.isEmpty(result)) {
                LogUtils.i(TAG, "getLauncherTopApp..." + result);
                return result;
            }
        }
        return "";
    }

    //modified by luorw for SW17G02A01 #87836 2017-04-19 end
    // 判断是否需要显示密码界面
    private boolean isFromActivityResult = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(TAG, "onActivityResult...true");
        isFromActivityResult = true;
    }

}
