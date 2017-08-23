package com.gionee.secretary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.service.CreateProcessService;
import com.gionee.secretary.service.SmsParseService;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by hangh on 5/13/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean setup = preferences.getBoolean(Constants.EVENT_SETUP, false);
        LogUtils.e("hangh", "EVENT_SETUP setup = " + setup);
        // 判断是系统短信；
        if (setup && (intent != null) && Constants.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            // 获取短信的内容和发件人
            StringBuilder body = new StringBuilder();// 短信内容
            StringBuilder number = new StringBuilder();// 短信发件人
            String sender = null;
            boolean ret = false;
            Object[] pdus = (Object[]) bundle.get(Constants.SMS_PDUS);
            SmsMessage[] message = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            for (SmsMessage currentMessage : message) {
                body.append(currentMessage.getDisplayMessageBody());
                number.append(currentMessage.getDisplayOriginatingAddress());
                sender = currentMessage.getDisplayOriginatingAddress();
                String number2 = currentMessage.getOriginatingAddress();
                Cursor cursor = null;
                try {
                    if (!TextUtils.isEmpty(number2)) {
                        Uri uri = Uri.parse("content://mms-sms/conversations/privateContact/" + number2);
                        cursor = context.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            ret = true;
                        }
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
            if (sender.contains("+86")) {
                sender = sender.substring(3);
            }
            LogUtils.e("hangh", "ret = " + ret);
            if (!ret) {//不是私密短信
                Intent smsIntent = new Intent();
                Bundle smsbundle = new Bundle();
                smsIntent.setClass(context, CreateProcessService.class);
                smsbundle.putString(Constants.SMS_SENDER, sender);
                smsbundle.putString(Constants.SMS_CONTENT, body.toString());
                smsIntent.putExtras(smsbundle);
                context.startService(smsIntent);
            }
            LogUtils.e(TAG, "SmsReceiver" + "sender = " + sender + " , body = " + body.toString());
        }
    }
}
