package com.gionee.secretary.bean;

import java.util.Date;

/**
 * 机票日程
 * Created by luorw on 11/05/16.
 */
public class FlightSchedule extends BaseSchedule {
    private String arrivalTime;//到达时间
    private String startAddress;//出发地
    private String destination;//到达地
    private String flightNum;//航班
    private String passenger;//乘客
    private String ticketNum;//票号
    private String airlineSource;//机票来源
    private String serviceNum;//客服电话
    private String alertDesc;//提示

    public FlightSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent) {
        //date为出发时间,title为“出发地到达地”
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent);
    }

    public FlightSchedule() {
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public String getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(String ticketNum) {
        this.ticketNum = ticketNum;
    }

    public String getAirlineSource() {
        return airlineSource;
    }

    public void setAirlineSource(String airlineSource) {
        this.airlineSource = airlineSource;
    }

    public String getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(String serviceNum) {
        this.serviceNum = serviceNum;
    }

    public String getAlertDesc() {
        return alertDesc;
    }

    public void setAlertDesc(String alertDesc) {
        this.alertDesc = alertDesc;
    }

    @Override
    public String toString() {
        return super.toString() + "--" + this.flightNum + "--" + this.arrivalTime;
    }
}
