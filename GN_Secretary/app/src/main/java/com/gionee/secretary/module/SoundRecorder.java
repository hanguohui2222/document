package com.gionee.secretary.module;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import amigoui.app.AmigoAlertDialog;

import com.gionee.secretary.R;
import com.gionee.secretary.ui.activity.AddVoiceNoteActivity;

/**
 * Created by hangh on 12/31/16.
 */
public class SoundRecorder {
    private Context mContext;
    //语音保存路径
    private String mFilePath = null;
    private Timer mTimer;
    private boolean mRecorderState = false;    //录音状态。true表示正在录音，false表示不在录音。
    private MediaRecorder mRecorder = null;
    Handler handler;
    private TextView mTime;
    private ImageView mStopButton;
    private AmigoAlertDialog dialog;
    private AddVoiceNoteActivity activity;


    private TakeSoundRecorderListener mTakeSoundRecorderListener;

    public SoundRecorder(Context mContext) {
        activity = (AddVoiceNoteActivity) mContext;
        this.mContext = mContext;
        initHandler();
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

                        if (second < 59) {
                            second++;

                        } else if (second == 59 && minute < 59) {
                            minute++;
                            second = 0;

                        }
                        if (second == 59 && minute == 59 && hour < 98) {
                            hour++;
                            minute = 0;
                            second = 0;
                        }

                        time[0] = hour + "";
                        time[1] = minute + "";
                        time[2] = second + "";
                        //调整格式显示到屏幕上
                        if (second < 10)
                            time[2] = "0" + second;
                        if (minute < 10)
                            time[1] = "0" + minute;
                        if (hour < 10)
                            time[0] = "0" + hour;

                        //显示在TextView中
                        mTime.setText(time[0] + ":" + time[1] + ":" + time[2]);
                        break;
                }
                //SoundRecorder.this.checkSoundRecordSuccess();
            }
        };
    }

    public void completeRecorder() {
        stopRecorder();
        dismissDialog();
        activity.setmShowDialog(false);
        if (mTakeSoundRecorderListener != null) {
            mTakeSoundRecorderListener.onRecorderComplete(mFilePath);
        }
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void setmTakeSoundRecorderListener(TakeSoundRecorderListener mTakeSoundRecorderListener) {
        this.mTakeSoundRecorderListener = mTakeSoundRecorderListener;
    }

    public void initDialog(Activity mActivity) {
        if (dialog == null) {
            View localView = LayoutInflater.from(mActivity).inflate(R.layout.action_record_sound, null, false);
            mTime = ((TextView) localView.findViewById(R.id.time));
            mStopButton = ((ImageView) localView.findViewById(R.id.stop));
            mStopButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    completeRecorder();
                }
            });
            AmigoAlertDialog.Builder localBuilder = new AmigoAlertDialog.Builder(mActivity);
            localBuilder.setView(localView);
            localBuilder.setCancelable(false);
            AmigoAlertDialog localAmigoAlertDialog = localBuilder.create();
            Window localWindow = localAmigoAlertDialog.getWindow();
            WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
            localLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog = localAmigoAlertDialog;
        }
    }

    public void showDialog() {
        mTime.setText("00:00:00");
        dialog.show();
    }

    public void startRecorder() {
        //每一次调用录音，可以录音多次，至多满意为至，最后只将最后一次的录音文件保存，其他的删除
        if (mFilePath != null) {
            File oldFile = new File(mFilePath);
            oldFile.delete();
        }
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        str = str + "record.amr";
        String paintPath = mContext.getCacheDir().getAbsolutePath();
        File dir = new File(paintPath);
        File file = new File(paintPath, str);
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            if (file.exists()) {
                file.delete();
            }
        }

        mFilePath = paintPath + "/" + str;
        //计时器
        mTimer = new Timer();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                completeRecorder();
            }
        });
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            dismissDialog();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            e.printStackTrace();
        }

        try {
            if(!mRecorderState){
                mRecorder.start();
                mRecorderState = true;  //设置录音状态为true，表示正在录音。
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "音频冲突，无法录音!", Toast.LENGTH_SHORT).show();
            return;
        }
        mTime.setText("00:00:00");
        if (!activity.ismShowDialog()) {
            dialog.show();
        }
        activity.setmShowDialog(true);
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);

            }
        }, 1000, 1000);


    }

    /**
     * 取得当前录音状态
     *
     * @return true表示正在录音，false表示不在录音
     */
    public boolean getRecorderState() {
        return mRecorderState;
    }

    private void stopRecorder() {
        try {
            mRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTimer.cancel();
        mTimer = null;
        mRecorder.release();
        mRecorder = null;
        mRecorderState = false;
    }

    public interface TakeSoundRecorderListener {
        public void onRecorderComplete(String path);
    }
}
