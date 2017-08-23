package com.gionee.secretary.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by hanguohui on 8/24/16.
 */
public class CreateProcessService extends IntentService {
    public CreateProcessService() {
        super("CreateProcessService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String sender = intent.getExtras().getString(Constants.SMS_SENDER);
        final String smsContent = intent.getExtras().getString(Constants.SMS_CONTENT);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent smsIntent = new Intent();
        Bundle smsbundle = new Bundle();
        smsIntent.setClass(this, SmsParseService.class);
        smsbundle.putString(Constants.SMS_SENDER, sender);
        smsbundle.putString(Constants.SMS_CONTENT, smsContent);
        smsIntent.putExtras(smsbundle);
        startService(smsIntent);
    }
}
