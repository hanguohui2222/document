package com.gionee.secretary.voice;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.gionee.secretary.R;
import com.gionee.secretary.ui.activity.PasswordBaseActivity;
import com.iflytek.recinbox.sdk.AppLoader;
import com.iflytek.recinbox.sdk.speech.impl.LybMscRecognizer;
import com.iflytek.recinbox.sdk.speech.impl.MscSampleRate;
import com.iflytek.recinbox.sdk.speech.interfaces.IMscRecognitionListener;

public class MscRecorderActivity  extends PasswordBaseActivity implements IMscRecognitionListener,OnClickListener,PcmRecordListener{
	private final String TAG= "MscRecorderActivity";
	
	
    private final String PATH = Environment.getExternalStorageDirectory()+"/SDKDEMO/实时转写/";
    MscSampleRate sampleRate = MscSampleRate.SAMPLE_RATE_16K;
    LybMscRecognizer mLybMscRecognizer;
    String mRequestedType = "audio/3gpp";

	String parseResult = "";
    ImageButton mRecordButton;
    ImageButton mStopButton;
    PcmRecorder mRecord;
    TextView mContentTxt;


	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 0:
				String result = (String) msg.obj;
				mContentTxt.setText(result);
			}
		}
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("yanjc", "---MscRecorderActivity---onCreate-------------");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msc_recorder);
		AppLoader.setSdkLogcat(true);
        mRecordButton = (ImageButton) findViewById(R.id.recordButton);
        mStopButton = (ImageButton) findViewById(R.id.stopButton);
        mStopButton.setEnabled(false);
        mRecordButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mContentTxt = (TextView) findViewById(R.id.text);
        
        File file = new File(PATH);
        if(!file.exists()){
        	file.mkdir();
        }
        
        RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				int radioButtonId = arg0.getCheckedRadioButtonId();
				switch(radioButtonId){
				case R.id.sample_16K:
					sampleRate = MscSampleRate.SAMPLE_RATE_16K;
					break;
				case R.id.sample_8K:
					sampleRate = MscSampleRate.SAMPLE_RATE_8K;
					break;
				}
			}
		});
        //替换为集成方的appId
//        LybMscRecognizer.setMscId("572fefce");
        LybMscRecognizer.setMscId(PropertiesHelper.getInstance(this).getAppid());
        mLybMscRecognizer = LybMscRecognizer.getInstance(getApplicationContext());
        mLybMscRecognizer.initialize();
        mLybMscRecognizer.setMscRecognitionListener(this);
	}
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View button) {
        if (!button.isEnabled())
            return;

        switch (button.getId()) {
            case R.id.recordButton:
            	onStartRecord();
            	break;
            case R.id.stopButton:
            	onStopRecord();
                break;
        }
	}
	
	private void onStartRecord(){
		mContentTxt.setText("");
		parseResult = "";
		
        try {
        	if(sampleRate == MscSampleRate.SAMPLE_RATE_16K){
	            mRecord = new PcmRecorder(this, PcmRecorder.SAMPLE_RATE_16K);
            }else{
	            mRecord = new PcmRecorder(this, PcmRecorder.SAMPLE_RATE_8K);
            }
            mRecord.setRecordListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecord.startRecording();
        mStopButton.setEnabled(true);
        mRecordButton.setEnabled(false);
        
        mLybMscRecognizer.initialize();
        mLybMscRecognizer.beginRecognize(sampleRate);
	}
	
	private void onStopRecord(){
        if (null != mRecord) {
            mRecord.stopRecording();
            mRecord.release();
        }
        mStopButton.setEnabled(false);
        mRecordButton.setEnabled(true);
        
        mLybMscRecognizer.stopRecognize();
	}
	
	@Override
	public void onRecordData(byte[] dataBuffer, int length, long timeMillisecond) {
		// TODO Auto-generated method stub
		mLybMscRecognizer.putRecordData(dataBuffer);
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		Logging.d(TAG, ""+error);
	}

	@Override
	public void onResults(String result) {
		// TODO Auto-generated method stub
		if(null != result){
			parseJSONResult(result);
		}
	}

	@Override
	public void onPartialResults(String result) {
		// TODO Auto-generated method stub
		parseJSONResult(result);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        if (null != mRecord && mRecord.isRecording()) {
            mRecord.stopRecording();
            mRecord.release();
        }
        mLybMscRecognizer.abortRecognize();
		mLybMscRecognizer.release();
		
		parseResult = "";
	}

    private void parseJSONResult(String strResult){
    	Log.d(TAG, "RecognizerManager parseJSONResult----strResult = " + strResult);
    	JSONObject jsonObj;
    	
		try { 
			jsonObj = new JSONObject(strResult).getJSONObject("cn");
			JSONObject stjsonObj  = new JSONObject(jsonObj.toString()).getJSONObject("st");
			JSONArray jsonObjs = new JSONObject(stjsonObj.toString()).getJSONArray("rt"); 
			for(int i = 0; i < jsonObjs.length() ; i++){ 
				parseOneItem(jsonObjs.opt(i).toString());
			} 
		} catch (JSONException e) { 
			Log.d(TAG, "Jsons parse error ! ---- exception = " + e); 
			e.printStackTrace(); 
		} finally{
			mHandler.sendMessage(mHandler.obtainMessage(0, parseResult));
		}
    }
    
    private void parseOneItem(String oneData){
    	String content;
    	JSONObject jsonObj;
    	
		try { 
			JSONArray jsonObjs = new JSONObject(oneData).getJSONArray("ws"); 
			for(int i = 0; i < jsonObjs.length() ; i++){ 
				JSONArray jsonObjsArray = new JSONObject(jsonObjs.opt(i).toString()).getJSONArray("cw");
				for(int j = 0; j < jsonObjsArray.length() ; j++){
					jsonObj = (JSONObject)jsonObjsArray.opt(j);
					content = jsonObj.getString("w");  
					parseResult += content;
				}
			} 
		} catch (JSONException e) { 
			Log.d(TAG, "Jsons parse error ! ---- exception = " + e); 
			e.printStackTrace(); 
		} finally{
		}
    }
	@Override
	public void onRecordDisable() {
		// TODO Auto-generated method stub
		
	}
}
