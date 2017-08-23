package com.gionee.secretary.bean;

import java.util.Date;

/**
 * 酒店日程
 * Created by luorw on 11/05/16.
 */
public class HotelSchedule extends BaseSchedule {
    private String hotelName;//酒店名称
    private String checkOutDate;//退房日期
    private String roomStyle;//房型
    private String checkInPeople;//入住人
    private String roomCounts;//房间数
    private String serviceNum;//客服电话
    private String hotelAddress;//酒店地址

    public HotelSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent) {
        //date为入住日期,title为酒店名称
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent);
    }

    public HotelSchedule() {
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getRoomStyle() {
        return roomStyle;
    }

    public void setRoomStyle(String roomStyle) {
        this.roomStyle = roomStyle;
    }

    public String getCheckInPeople() {
        return checkInPeople;
    }

    public void setCheckInPeople(String checkInPeople) {
        this.checkInPeople = checkInPeople;
    }


    public String getRoomCounts() {
        return roomCounts;
    }

    public void setRoomCounts(String roomCounts) {
        this.roomCounts = roomCounts;
    }

    public String getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(String serviceNum) {
        this.serviceNum = serviceNum;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    @Override
    public String toString() {
        return super.toString() + "--" + this.hotelAddress + "--" + this.checkOutDate;
    }
}
