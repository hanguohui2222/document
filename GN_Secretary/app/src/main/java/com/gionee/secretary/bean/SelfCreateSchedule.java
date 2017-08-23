package com.gionee.secretary.bean;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义日程
 * Created by luorw on 11/05/16.
 */
public class SelfCreateSchedule extends BaseSchedule {
    private static final String TAG = "SelfCreateSchedule";
    private static final long serialVersionUID = 1L;
    private String address;//地点
    private String description;//描述
    private String tripMode;//出行方式
    private Date endtime;//设置日程结束时间
    private Date subPeriodDate;//与base表中的date相同，用于更新或删除周期重复的日程
    private int subPeriodId;//周期重复的日程ID
    private long period;
    private String addressRemark;//地址备注

    public long getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(long remindTime) {
        this.remindTime = remindTime;
    }

    private long remindTime;//自定义时间间隔

    private String title;  /*Gionee zhengyt 2016-12-20 add for search not Begin*/

    public SelfCreateSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, int periodID) {
        //date为日程开始时间,title为自定义title
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, periodID);
    }

    public SelfCreateSchedule() {

    }

    public String getAddressRemark() {
        return addressRemark;
    }

    public void setAddressRemark(String addressRemark) {
        this.addressRemark = addressRemark;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setSubPeriodId(int subPeriodId) {
        this.subPeriodId = subPeriodId;
    }

    public int getSubPeriodId() {
        return subPeriodId;
    }

    public void setSubPeriodDate(Date subPeriodDate) {
        this.subPeriodDate = subPeriodDate;
    }

    public Date getSubPeriodDate() {
        return subPeriodDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTripMode() {
        return tripMode;
    }

    public void setTripMode(String tripMode) {
        this.tripMode = tripMode;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    @Override
    public String toString() {
        LogUtils.i(TAG, "selfCreateSchedule.....toString....title:" + this.getTitle() + "  ,IsAllDay():" + this.getIsAllDay()
                + "  ,RemindType():" + this.getRemindType() + "   ,Recycle:" + this.getRemindPeriod() + "  ,mAddress2:" + this.getAddress()
                + "   ,mTravel:" + this.getTripMode() + "  mDescription:" + this.getDescription() + " ,startData:" + DateUtils.date2String(this.getDate())
                + "  ,startTime:" + DateUtils.time2String(this.getDate()) + "   ,endData:" + DateUtils.date2String(this.getEndtime()) + "  ,EndTime:" + DateUtils.time2String(this.getEndtime()));

        return super.toString();
    }
}
