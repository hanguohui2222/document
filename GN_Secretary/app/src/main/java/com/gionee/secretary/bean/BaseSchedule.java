package com.gionee.secretary.bean;

import android.util.Log;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日程基类
 * Created by zhuboqin on 11/05/16.
 */
public class BaseSchedule implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;//日程id
    public Date date;//日程发生时间
    public int type;//银行类,火车票,飞机票,自建,电影,快递
    public boolean isAllDay;//全天 0否,1是
    public String title;//日程标题
    private String remindType; //提醒类型；
    private String remindPeriod;//提醒周期
    public long remindDate;//提醒时间
    public int isSmartRemind;//-1不提醒;0常规提醒；1智能提醒
    public int isRemindActive;        // -1 已提醒 ；0未激活；1激活
    public String smsSender;//发件人
    public String smsContent;//信息内容
    public String source;
    private int periodID;//重复周期日程的id

    private String noteContent;
    private int noteId;
    private String noteTitle;

    private Date broadcastSortDate;//播报日程用于排序的日期

    public Date getBroadcastSortDate() {
        return broadcastSortDate;
    }

    public void setBroadcastSortDate(Date broadcastSortDate) {
        this.broadcastSortDate = broadcastSortDate;
    }

    public BaseSchedule() {

    }

    public BaseSchedule(int noteId, String noteContent, String noteTitle) {
        this.noteId = noteId;
        this.noteContent = noteContent;
        this.noteTitle = noteTitle;
    }

    public BaseSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, int periodID) {
        this.date = date;
        this.type = type;
        this.isAllDay = isAllDay;
        this.title = title;
        this.remindType = remindType;
        this.remindPeriod = remindPeriod;
        this.remindDate = remindDate;
        this.isSmartRemind = isSmartRemind;
        this.isRemindActive = isRemindActive;
        this.periodID = periodID;
    }

    public BaseSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, int periodID, String content) {
        this.date = date;
        this.type = type;
        this.isAllDay = isAllDay;
        this.title = title;
        this.remindType = remindType;
        this.remindPeriod = remindPeriod;
        this.remindDate = remindDate;
        this.isSmartRemind = isSmartRemind;
        this.isRemindActive = isRemindActive;
        this.periodID = periodID;

        this.noteContent = content;
    }

    public BaseSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent) {
        this.date = date;
        this.type = type;
        this.isAllDay = isAllDay;
        this.title = title;
        this.remindType = remindType;
        this.remindPeriod = remindPeriod;
        this.remindDate = remindDate;
        this.isSmartRemind = isSmartRemind;
        this.isRemindActive = isRemindActive;
        this.smsSender = smsSender;
        this.smsContent = smsContent;
    }

    public BaseSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent, String source) {
        this.date = date;
        this.type = type;
        this.isAllDay = isAllDay;
        this.title = title;
        this.remindType = remindType;
        this.remindPeriod = remindPeriod;
        this.remindDate = remindDate;
        this.isSmartRemind = isSmartRemind;
        this.isRemindActive = isRemindActive;
        this.smsSender = smsSender;
        this.smsContent = smsContent;
        this.source = source;
    }

    public void setNoteContext(String text) {
        this.noteContent = text;
    }

    public String getNoteContext() {
        return noteContent;
    }

    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

    public int getPeriodID() {
        return periodID;
    }


    public String getSmsSender() {
        return smsSender;
    }

    public void setSmsSender(String smsSender) {
        this.smsSender = smsSender;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    public int getIsSmartRemind() {
        return isSmartRemind;
    }

    public void setIsSmartRemind(int isSmartRemind) {
        this.isSmartRemind = isSmartRemind;
    }

    public int getIsRemindActive() {
        return isRemindActive;
    }

    public void setIsActive(int isRemindActive) {
        this.isRemindActive = isRemindActive;
    }

    public long getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(long remindDate) {
        this.remindDate = remindDate;
    }

    public String getRemindType() {
        return remindType;
    }

    public void setRemindType(String remindType) {
        this.remindType = remindType;
    }

    public String getRemindPeriod() {
        return remindPeriod;
    }

    public void setRemindPeriod(String remindPeriod) {
        this.remindPeriod = remindPeriod;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setNoteId(int mId) {
        this.id = mId;
    }

    public int getNoteId() {
        return this.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public boolean getIsAllDay() {
        return isAllDay;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        LogUtils.d("lml", "BaseSchedule.....toString....id:" + this.getId() + ", date:" + this.getDate() + ", type:" + this.getType() +
                " ,isAllDay:" + this.getIsAllDay() + ", title:" + this.getTitle() + "  ,RemindType():" + this.getRemindType()
                + " ,remindPeriod:" + this.getRemindPeriod() + " , remindDate:" + this.getRemindDate() + ":" + RemindUtils.time2String(this.getRemindDate())
                + ", isSmartRemind:" + this.getIsSmartRemind() + ", isRemindActive:" + this.getIsRemindActive() + " ,source:"
                + this.getSource() + ", periodID:" + this.getPeriodID());

        return super.toString();
    }

}
