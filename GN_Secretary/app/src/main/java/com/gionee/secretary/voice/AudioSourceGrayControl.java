package com.gionee.secretary.voice;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaRecorder;
import android.os.Build;


/**
 * 录音的灰度控制类
 * 检查与控制部分手机不能开带降噪的录音
 * @author zhangyun
 * @date 2013-5-28
 */
@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
public class AudioSourceGrayControl {    
    private static final String TAG = "AudioSourceGrayControl";

    
    private static final String MODEL_HTC_T3 = "htc t3"; //特殊机型
    private static final String CPU_MODEL = "mt65"; //特殊CPU，MTK6517,6577,6589 xxx
    private static int mAudioSourceType = -1;
    private static boolean mIsNeedPlayWakeTone = false;

    
    /**
     * 录音的AudioSource类型
     * @return
     */
    public static int getAudioSourceType(){
        checkAudioSource();
        return mAudioSourceType;
    }
    
    /**
     * 部分机型：(在HTC T3XX)
     * 在挂断电话后的立即开始唤醒无法正常录音
     * 通过播放提示音解决
     */
    public static boolean  isNeedPlayToneOnStartRecord(){
        if (mAudioSourceType < 0){
            checkAudioSource();
        }
        return mIsNeedPlayWakeTone; 
    }
    
    /**
     * 检查录音源
     */
    @SuppressLint("DefaultLocale")
    private static void  checkAudioSource(){
        mAudioSourceType = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        //1.小于4.0版本
        if (Build.VERSION.SDK_INT < 14) {
            Logging.d(TAG,"checkAudioSource SDK_INT < 14 use MIC" );
            mAudioSourceType = MediaRecorder.AudioSource.MIC;
        } else {
            // 2.CPU判断
            String cpu = Build.HARDWARE;
            String model = Build.MODEL.toLowerCase();
            if (cpu.startsWith(CPU_MODEL)){
                mAudioSourceType = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
            }
            // 3.机型适配
//            int adapterAudioSource = SpeechFlagAdapter.getAudioSource();
//            if (adapterAudioSource == MediaRecorder.AudioSource.MIC
//                    || adapterAudioSource == MediaRecorder.AudioSource.VOICE_RECOGNITION
//                    || adapterAudioSource == MediaRecorder.AudioSource.VOICE_COMMUNICATION) {
//                mAudioSourceType = adapterAudioSource;
//            }
            //4. 特殊机型
            if (model.contains(MODEL_HTC_T3)){
                mAudioSourceType = MediaRecorder.AudioSource.MIC;
                mIsNeedPlayWakeTone = true;
            }
            Logging.d(TAG,"checkAudioSource MODEL=" + model + " CPU=" + cpu +" ret=" + mAudioSourceType);
        }
    }
}
