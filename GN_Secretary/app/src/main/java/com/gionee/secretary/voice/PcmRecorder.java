package com.gionee.secretary.voice;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * 录音控制类
 * @author zhangyun
 * @date 2013-12-30 增加start异常catch
 */
public class PcmRecorder {
    private static final String TAG = "SPEECH_PcmRecorder";

    
    public static final int SAMPLE_RATE_16K = 16 * 1000;
    public static final int SAMPLE_RATE_8K = 8 * 1000; 

    private static final short DEFAULT_BIT_SAMPLES = 16;
    private static final int RECORD_BUFFER_TIMES_FOR_FRAME = 10;
    public static final int DEFAULT_TIMER_INTERVAL = 40;
    private static final short DEFAULT_CHANNELS = 1;

    private byte[] mBuffer = null;
    private AudioRecord mRecorder = null;
    private PcmRecordListener mRecordListener = null;
    private Object mReadLock = new Object();
    private long mStartTime = 0;
    private boolean mIsRecording = false;
    private Thread mReadThread;
    private WeakReference<Activity> mActivity;

    /**
     * 录音器构建类，如果创建失败直接ThrowException
     * @param context
     * @throws Exception
     */
    public PcmRecorder(Context context) throws Exception {
        this(DEFAULT_CHANNELS, DEFAULT_BIT_SAMPLES, SAMPLE_RATE_16K, DEFAULT_TIMER_INTERVAL);
    }
    
    public PcmRecorder(Activity activity,int sampleRate) throws Exception {
        this(DEFAULT_CHANNELS, DEFAULT_BIT_SAMPLES, sampleRate, DEFAULT_TIMER_INTERVAL);
        mActivity = new WeakReference<Activity>(activity);
    }

    public PcmRecorder(short channels, short bitSamples, int sampleRate,
            int timeInterval) throws Exception {
        if (timeInterval % DEFAULT_TIMER_INTERVAL != 0) {
            Logging.e(TAG, "parameter error, timeInterval must be multiple of " + DEFAULT_TIMER_INTERVAL);
            throw new Exception();
        }
        int framePeriod = sampleRate * timeInterval / 1000;						 
        int recordBufferSize = framePeriod * RECORD_BUFFER_TIMES_FOR_FRAME
                * bitSamples * channels / 8;
        int channelConfig = (channels == 1 ? AudioFormat.CHANNEL_CONFIGURATION_MONO
                : AudioFormat.CHANNEL_CONFIGURATION_STEREO);
        int audioFormat = (bitSamples == 16 ? AudioFormat.ENCODING_PCM_16BIT
                : AudioFormat.ENCODING_PCM_8BIT);

        int audioSource = AudioSourceGrayControl.getAudioSourceType();

        int min = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
                audioFormat);
        if (recordBufferSize < min) {
            recordBufferSize = min;
            Logging.w(TAG,"Increasing buffer size to " + Integer.toString(recordBufferSize));
        }
        //8K录音强制不降噪
        if (sampleRate == SAMPLE_RATE_8K){
            audioSource = MediaRecorder.AudioSource.MIC;            
        }
        mRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, recordBufferSize);

        if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
            mRecorder.release();
            mRecorder = null;
            Logging.e(TAG, "create AudioRecord error");
            throw new Exception("create AudioRecord error");
        }
 
        mBuffer = new byte[framePeriod * channels * bitSamples / 8];
        Logging.d(TAG, "create AudioRecord ok buffer size=" + mBuffer.length
                + " audioSource=" + audioSource + " sampleRate=" + sampleRate);
    }
    private boolean tipFlag = true;
    /**
     * 读取录音数据
     * @return
     */
    private int readRecordData() {
        int count = 0;
        try {
            if (mRecorder != null) {
                if (mRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
//                    Logging.d(TAG, "readRecordData END RECORDSTATE_STOPPED");
                    Log.e("weiqun12345","readRecordData END RECORDSTATE_STOPPED");
//                    Toast.makeText(mContext.getApplicationContext(), "222", Toast.LENGTH_LONG).show();
                    if(tipFlag) {
                    	 Log.e("weiqun12345","other input already started");
                        final Activity activity = mActivity.get();
                        if(activity != null){
                            activity.runOnUiThread(new Runnable(){

                                @Override
                                public void run() {
                                    Toast.makeText(activity.getApplicationContext(), "音频冲突，语音转文字无法进行", Toast.LENGTH_LONG).show();
                                    if (mRecordListener != null) {
                                        mRecordListener.onRecordDisable();
                                    }
                                }

                            });
                        }
                    	 tipFlag = false;
                    }
                    return 0;
                }
                count = mRecorder.read(mBuffer, 0, mBuffer.length);
                if (count > 0 && mRecordListener != null) {
                    mRecordListener.onRecordData(mBuffer, count,
                            SystemClock.elapsedRealtime() - mStartTime);
                } else {
                    Logging.d(TAG, "count = " + count);
                }
            } else {
                Logging.d(TAG, "readRecordData null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
    
    /**
     * 每次录音开始创建一个读数据子线程
     */
    private void startReadThread() {
        mReadThread = new Thread("PcmRecorderNew") {
            @Override
            public void run() {
                LoggingTime.d(TAG,"startReadThread OK=" + this.getId());
                while (mIsRecording) {
                    synchronized (mReadLock) {
                        readRecordData();
                    }
                    SystemClock.sleep(10);
                }
                LoggingTime.d(TAG,"startReadThread finish=" + this.getId());
            }

        };
        mReadThread.setPriority(Thread.MAX_PRIORITY);
        mReadThread.start();
    }

    public void setRecordListener(PcmRecordListener listener) {
        mRecordListener = listener;
    }

    public void removeRecordListener() {
        mRecordListener = null;
    }

    public void startRecording() {
        LoggingTime.d(TAG, "startRecording begin");
        if (null == mRecorder || mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED){
           Logging.e(TAG, "startRecording STATE_UNINITIALIZED");
           return;
        }
        if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            Logging.e(TAG, "startRecording RECORDSTATE_RECORDING");
            return;
        }
        
        try {
            mRecorder.startRecording();
            mIsRecording = true;
            mStartTime = SystemClock.elapsedRealtime();
            startReadThread();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
        LoggingTime.d(TAG, "startRecording end");
    }

    public void stopRecording() {
        if (mRecorder != null) {
            Logging.d(TAG, "stopRecording into");
            // FIXME 等读取完成后再Stop 2012-8-13 ，解决部分手机Stop后读取数据阻塞问题
            mIsRecording = false;
            if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                synchronized (mReadLock) {
                    mRecorder.stop();
                }
            }  
            Logging.d(TAG, "stopRecording end"); 
        }
        //mActivity = null;
    }

    /**
     *  停止读数据
     * 读数据线程会根据此标志结束 
     * */
    public void stopReadingData(){
        mIsRecording = false;
    }
    
    /**
     * 释放录音设备
     */
    public void release() {
        if (null != mRecorder && mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            stopRecording();
        }
        // FIXME 在部分机器上release后 read方法会阻塞,增加
        Logging.d(TAG, "release begin");
        synchronized (mReadLock) {
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }
            Logging.d(TAG, "release ok");
        }
        Logging.d(TAG, "release end");
    }

    /**
     * 获取录音器采样率
     * @return
     */
    public int getSampleRate() {
        if (mRecorder != null) {
            return mRecorder.getSampleRate();
        } else {
            return SAMPLE_RATE_16K;
        }
    }

    /**
     * 是否在录音中状态
     * @return
     */
    public boolean isRecording() {
        if (mRecorder != null) {
            return mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
        } else {
            return false;
        }
    }
}
