package com.gionee.hotspottransmission.bean;

import java.io.Serializable;

/**
 * Created by weiqun on 2016/4/23 0025.
 */
public class RequestInfo implements Serializable{
	public int command; //命令 1.获取文件描叙 2.获取文件是否发送是否接收 3.获取文件 4.结束任务
	public String dis; //文件描述
	public String deviceName;//对方设备名称
	public String sendStatus;//是否传输
	public int index;//请求文件的下标

	public RequestInfo(){

	}

	@Override
	public String toString() {
		return "RequestInfo{" +
				"command=" + command +
				", dis='" + dis + '\'' +
				", deviceName='" + deviceName + '\'' +
				", responseSendStatus='" + sendStatus + '\'' +
				", index=" + index +
				'}';
	}
}
