package com.gionee.secretary.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.receiver.RemindReceiver;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by  liuml on 16-5-29.
 */
public class RemindService extends Service {
    static final boolean DEBUG = true;
    private static final String TAG = "lml";
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "enter RemindService.onCreate");
        HandlerThread thread = new HandlerThread("RemindService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
//        RemindUtils.scheduleRecentAlarm(RemindService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "enter RemindService.onStartCommand");
        if (intent != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent.getExtras();
            mServiceHandler.sendMessage(msg);
        }
        return START_REDELIVER_INTENT;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            processMessage(msg);
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by RemindReceiver is released.
            RemindReceiver.finishStartingService(RemindService.this, msg.arg1);
        }
    }

    void processMessage(Message msg) {
        Bundle bundle = (Bundle) msg.obj;

        String action = bundle.getString(Constants.RemindConstans.ACTION_KEY);
        String remindType = bundle.getString(Constants.RemindConstans.MSG_KEY);
        int scheduleId = bundle.getInt(Constants.RemindConstans.SCHEDULEID_KEY);
        if (DEBUG) {
            LogUtils.d(TAG, " Action = " + action);
        }
        if (Constants.NORMAL_REMIND_ACTION.equals(action)) {
            RemindReceiver.scheduleNormalRemind(this, scheduleId, remindType);
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                || action.equals(Constants.REFRESH_TASK_ACTION)) {
            Intent intent = new Intent();
            intent.putExtra(Constants.RemindConstans.ACTION_KEY, action);
            intent.setClass(this, InitAlarmsService.class);
            startService(intent);
        } else if (Constants.NOTE_REMIND_ACTION.equals(action)) {
            int noteId = bundle.getInt(Constants.RemindConstans.NOTE_KEY);
            RemindReceiver.noteRemind(this, noteId);
        }
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
