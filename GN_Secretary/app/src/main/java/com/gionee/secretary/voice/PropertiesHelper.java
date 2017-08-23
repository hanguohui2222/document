package com.gionee.secretary.voice;

import java.util.Properties;

import android.content.Context;

/**
 * @Title: PropertiesHelper.java
 * @Package com.iflytek.util.file
 * @Description: TODO(用一句话描述该文件做什么)
 * @author caoya
 * @Email:yacao@iflytek.com
 * @date 2016年5月24日 上午10:25:47
 * @version V1.0
 */
public class PropertiesHelper {

	private static PropertiesHelper mInstance;

	private static Context mContext;
	
	private String appid;
	
	private String sessionId;
	
	private String aesKey;

	public static synchronized PropertiesHelper getInstance(Context context) {
		if (null == mInstance) {
			mContext = context.getApplicationContext();
			mInstance = new PropertiesHelper();
		}
		return mInstance;
	}

	private PropertiesHelper() {
		parseProperties();
	}

	private void parseProperties() {
		Properties prop = new Properties();
		try {
			int id = mContext.getResources()
					.getIdentifier("user", "raw", mContext.getPackageName());
			prop.load(mContext.getResources().openRawResource(id));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String appid = prop.getProperty("appid");// msc appid
		setAppid(appid);
		String sessionId = prop.getProperty("sessionId");// 大客户sessionId
		setSessionId(sessionId);
		String aesKey = prop.getProperty("aesKey");// 大客户aesKey
		setAesKey(aesKey);
	}
	
	/**
	 * 获取在线转写 msc appid
	 * @return
	 */
	public String getAppid() {
		return appid;
	}

	private void setAppid(String appid) {
		this.appid = appid;
	}

	/**
	 * 大客户sessionId
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	private void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 大客户aesKey
	 * @return
	 */
	public String getAesKey() {
		return aesKey;
	}

	private void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}
	
}
