package com.gionee.secretary;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
//import com.autonavi.v2.protocol.RequestManager;
//import com.gionee.featureoption.FeatureOption;
import com.gionee.secretary.db.SecretaryDBOpenHelper;
import com.gionee.secretary.db.SecretarySQLite;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.receiver.PasswordLockReceiver;
import com.squareup.leakcanary.LeakCanary;
import com.ted.android.core.SmsParserEngine;
import com.ted.sdk.yellow.CtaDataBus;

import amigoui.changecolors.ChameleonColorManager;

/**
 * Created by rongdd on 16-5-11.
 */
public class SecretaryApplication extends Application {
    private static final String TAG = "SecretaryApplication";
    private static Context appContext = null;
    public static boolean isLocked = false;
    public static String mPackageName;
    private BroadcastReceiver mReceiver = new PasswordLockReceiver();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        appContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        /*if (FeatureOption.GN_APK_CHAMELEON_COLOR_ENABLE) {
            ChameleonColorManager.getInstance().register(this);
        }*/
//        RequestManager.init(getApplicationContext());//ETS
        SmsParserEngine.getInstance(this).initialise(this, null);
        CtaDataBus.setNetworkAccessible(true);
        LogUtils.e(TAG, "oncreated");
        registerScreenActionReceiver();
        mPackageName = getApplicationContext().getPackageName();
        //added by luorw for M2017 Bug #35139 20161129
        setLockStatus();
    }

    /**
     * added by luorw for M2017 Bug #35139 20161129
     */
    private void setLockStatus() {
        boolean lockSwitch = PasswordModel.getInstance(this).getLockSwitch();
        if (lockSwitch) {
            LogUtils.i(TAG, "setPWD....LockState....true");
            PasswordModel.getInstance(this).updateLockState(true);
        }
    }

    @Override
    public void onTerminate() {
        unregisterScreenActionReceiver();
        SecretaryDBOpenHelper helper = SecretarySQLite.getDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            LogUtils.d("hangh", "close db");
            db.close();
        }
        super.onTerminate();
    }

    public void registerScreenActionReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, filter);
    }

    public void unregisterScreenActionReceiver() {
        unregisterReceiver(mReceiver);
    }

}
