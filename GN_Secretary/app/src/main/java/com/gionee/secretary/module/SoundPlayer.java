package com.gionee.secretary.module;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import amigoui.app.AmigoAlertDialog;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by hangh on 1/13/17.
 */
public class SoundPlayer {
    private Context mContext;
    private Timer mTimer;
    private TextView mTime;
    private ImageView mStopButton;
    private AmigoAlertDialog dialog;
    private MediaPlayer mPlayer = null;
    Handler handler;
    private TimerTask mTimerTask;
    private String mAudioPath;
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener phoneStateListener;

    public SoundPlayer(Context mContext, String mAudioPath) {
        this.mContext = mContext;
        this.mAudioPath = mAudioPath;
        initDialog();
        initHandler();
        telephonyManager = (TelephonyManager) mContext.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        phoneStateListener = MyPhoneStateListener.getInstance(this);
    }

    private void initHandler() {
        handler = new Handler(this.mContext.getMainLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        String time[] = mTime.getText().toString().split(":");
                        int hour = Integer.parseInt(time[0]);
                        int minute = Integer.parseInt(time[1]);
                        int second = Integer.parseInt(time[2]);
                        if (second > 0) {
                            second--;

                        } else if (second == 0 && minute > 0) {
                            minute--;
                            second = 59;
                        }
                        if (second == 0 && minute == 0 && hour > 0) {
                            hour--;
                            minute = 59;
                            second = 59;
                        }
                        time[0] = hour + "";
                        time[1] = minute + "";
                        time[2] = second + "";
                        // 调整格式显示到屏幕上
                        if (second < 10)
                            time[2] = "0" + second;
                        if (minute < 10)
                            time[1] = "0" + minute;
                        if (hour < 10)
                            time[0] = "0" + hour;
                        // 显示在TextView中
                        mTime.setText(time[0] + ":" + time[1] + ":" + time[2]);
                        break;
                }
                // SoundRecorder.this.checkSoundRecordSuccess();
            }
        };
    }

    private static class MyPhoneStateListener extends PhoneStateListener {
        private WeakReference<SoundPlayer> mPlayerWeakReference;
        private static MyPhoneStateListener listener;

        public static MyPhoneStateListener getInstance(SoundPlayer soundPlayer) {
            if (listener == null) {
                synchronized (MyPhoneStateListener.class) {
                    if (listener == null) {
                        listener = new MyPhoneStateListener(soundPlayer);
                    }
                }
            }
            return listener;
        }

        private MyPhoneStateListener(SoundPlayer soundPlayer) {
            mPlayerWeakReference = new WeakReference<SoundPlayer>(soundPlayer);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            SoundPlayer soundPlayer = mPlayerWeakReference.get();
            if (soundPlayer != null) {
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (soundPlayer.isPlaying()) {
                        soundPlayer.pausePlayer();
                    }
                }
            }

        }
    }

    public void initDialog() {
        if (dialog == null) {
            View localView = LayoutInflater.from(mContext).inflate(R.layout.action_record_player, null, false);
            mTime = ((TextView) localView.findViewById(R.id.time));
            mStopButton = ((ImageView) localView.findViewById(R.id.stop));
            mStopButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (mPlayer.isPlaying()) {
                        pausePlayer();
                        return;
                    }
                    reStartPlayer();
                }
            });
            AmigoAlertDialog.Builder localBuilder = new AmigoAlertDialog.Builder(mContext);
            localBuilder.setView(localView);
            localBuilder.setCancelable(true);
            AmigoAlertDialog localAmigoAlertDialog = localBuilder.create();
            Window localWindow = localAmigoAlertDialog.getWindow();
            WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
            localLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            localAmigoAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    LogUtils.i("SoundPlayer","completePlayer , onDismiss");
                    completePlayer();
                }
            });
            dialog = localAmigoAlertDialog;
        }
    }

    public void showDialog(String time) {
        mTime.setText(time);
        dialog.show();
    }

    public void startPlay() {
        mPlayer = new MediaPlayer();
        mTimer = new Timer();
        mPlayer.setOnCompletionListener(new MediaCompletion());
        try {
            mPlayer.setDataSource(mAudioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LogUtils.i("SoundPlayer" , "onError");
                completePlayer();
                return true;
            }
        });
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                Message message = handler.obtainMessage();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
        if (null != telephonyManager) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void pausePlayer() {
        mStopButton.setImageResource(R.drawable.gn_record_start);
        mPlayer.pause();
        cancelTimer();
    }

    private void reStartPlayer() {
        mStopButton.setImageResource(R.drawable.gn_record_pause);
        mPlayer.start();
        startTimer();
    }

    private void startTimer() {
        this.mTimer = new Timer();
        this.mTimerTask = new TimerTask() {
            public void run() {
                handler.sendEmptyMessage(1);
            }
        };
        this.mTimer.schedule(this.mTimerTask, new Date(), 1000L);
    }

    class MediaCompletion implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtils.i("SoundPlayer" , "onCompletion");
            completePlayer();
            // mTime.setText("00:00:00");
        }
    }

    public void completePlayer() {
        LogUtils.i("SoundPlayer" , "completePlayer ---------");
        stopPlayer();
        cancelTimer();
        dismissDialog();
        if (null == telephonyManager) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }

    }

    private void stopPlayer() {
        if ((mPlayer != null) && (mPlayer.isPlaying())) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean isPlaying() {
        return (mPlayer != null) && (mPlayer.isPlaying());
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void dismissDialog() {
        if (dialog != null) {
            // added by luorw for GNSPR #75352 20170322 begin
            Activity owner = (Activity) mContext;
            Log.i("luorwtest", "getOwnerActivity = " + owner);
            if (owner != null && !owner.isDestroyed()) {
                dialog.dismiss();
                dialog = null;
            }
            // added by luorw for GNSPR #75352 20170322 end
        }
    }
}
