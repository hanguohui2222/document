package com.gionee.secretary.view;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.activity.CalendarActivity;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.OSTimer;
import com.gionee.secretary.utils.SystemUtils;
import com.gionee.secretary.utils.Timeout;
import com.youju.statistics.YouJuAgent;

//import android.os.SystemProperties;
import android.widget.LinearLayout;

import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoCheckBox;

/**
 * Created by hangh on 16-12-18.
 */

public class SplashActivity extends AmigoActivity {
    private Handler mHandler = new Handler();
    SharedPreferences sharedPreferences = null;
    private AmigoAlertDialog dialog;
    private LinearLayout mBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.EVENT_SETUP, true);
        editor.commit();
        /*if (isAppRunning()) {
            LogUtils.e("SplashActivity","isAppRunning");
            finish();
            return;
        }*/
        setContentView(R.layout.activity_launch);
        getAmigoActionBar().hide();
        mBg = (LinearLayout) findViewById(R.id.iv_bg);
        String model = Build.MODEL;
        if(!DisplayUtils.isFullScreen()){
            mBg.setBackground(getResources().getDrawable(R.drawable.start_page));
        } else {
            mBg.setBackground(getResources().getDrawable(R.drawable.start_page_full));
        }
        // added by luorw for #66866 20170209 begin
        //modified by luorw for GNSPR #71402 20170322 begin
        if (!showSystemPermissionAlert()) {
            //modified by luorw for GNSPR #71402 20170322 end
            showPermissionTip();
        } else {
            sendToMainActivity(this);
        }
        // added by luorw for #66866 20170209 end
    }

    /**
     * added by luorw for GNSPR #71402 20170322
     *
     * @return
     */
    private boolean showSystemPermissionAlert() {
        String alertEnable = SystemUtils.getProperty(Constants.SYSTEM_PERMISSION_ALERT_SUPPORT,"yes");;
        LogUtils.i("luorwtest", "alert = " + alertEnable);
        if ("yes".equals(alertEnable)) {
            return true;
        } else {
            return false;
        }
    }

    private void showPermissionTip() {
        Boolean isShow = sharedPreferences.getBoolean(Constants.SHOWTIPS, false);
        if (!isShow) {
            View view = LayoutInflater.from(this).inflate(R.layout.network_setting_confirm_layout, null);
            final AmigoCheckBox checkBox = (AmigoCheckBox) view.findViewById(R.id.checkbox);
            AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
            builder.setTitle(getString(R.string.permission_title));
            builder.setMessage(R.string.permission_content);
            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }

            });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.SHOWTIPS, checkBox.isChecked());
                    editor.commit();
                    dialog.dismiss();
                    /* Gionee zhengyt 2016-12-30 add for GNSPR #63427 Begin */
                    initYoujuAndParserEngine();
					/* Gionee zhengyt 2016-12-30 add for GNSPR #63427 End */
                    startActivity(new Intent(SplashActivity.this, CalendarActivity.class));
                    finish();
                }
            });
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            if (null != dialog && !dialog.isShowing()) {
                dialog.show();
            }
        } else {
            sendToMainActivity(this);
        }
    }

    private void sendToMainActivity(final Context context) {
        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                startActivity(new Intent(SplashActivity.this, CalendarActivity.class));
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        };

        new OSTimer(null, timeout, 1 * 1000).start();
    }

    /* Gionee zhengyt 2016-12-30 add for GNSPR #63427 Begin */
    private void initYoujuAndParserEngine() {
        YouJuAgent.init(this);
        YouJuAgent.setReportUncaughtExceptions(true);
        YouJuAgent.setContinueSessionMillis(100);
        YouJuAgent.setLocationEnabled(true);
    }

	/* Gionee zhengyt 2016-12-30 add for GNSPR #63427 End */

    private boolean isAppRunning() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		String name = am.getRunningTasks(1).get(0).baseActivity.getClassName();
        String top = am.getRunningTasks(1).get(0).topActivity.getClassName();
        int num = am.getRunningTasks(1).get(0).numActivities;
//		LogUtils.d("liyu", "am.getRunningTasks(1).get(0).numActivities = "+ num);
//		LogUtils.d("liyu", "baseActivity = "+ name);
//		LogUtils.d("liyu", "topActivity = "+ top);
        return num > 1 && "com.gionee.secretary.view.SplashActivity".equals(top);
    }

}
