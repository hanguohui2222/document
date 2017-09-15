package com.gionee.hotspottransmission.history.bean;

import android.text.TextUtils;
import com.gionee.hotspottransmission.constants.Constants;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhuboqin on 3/05/16.
 */
public class DeviceInfo {

    public String deviceName;//设备昵称
    public Date connectTime;//连接的时间
    public String deviceAddress;//指的是设备的IMEI号，用于判断是否是重复连接的同一设备
    public String status;//offline，online，work
    public String ip;//设备ip地址

    public DeviceInfo() {
    }

    public DeviceInfo(String deviceName, Date connectTime, String deviceAddress) {
        this.connectTime = connectTime;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceInfo)) {
            return false;
        }
        String thisDate = date2String(this.connectTime);
        String oDate = date2String(((DeviceInfo) o).connectTime);
        if (TextUtils.isEmpty(thisDate) || TextUtils.isEmpty(this.deviceAddress)) {
            return false;
        }
        if (thisDate.equals(oDate)
                && this.deviceAddress.equals(((DeviceInfo) o).deviceAddress)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String str = sdf.format(date);
        return str;
    }
}
