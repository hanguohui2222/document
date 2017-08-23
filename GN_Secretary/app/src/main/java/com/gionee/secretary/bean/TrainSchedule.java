package com.gionee.secretary.bean;


import java.util.Date;

/**
 * 火车票日程
 * Created by hangh on 5/14/16.
 */
public class TrainSchedule extends BaseSchedule {
    private String starttime;//出发时间
    private String arrivaltime;//到达时间
    private String departure;//出发地
    private String destination;//目的地
    private String trainnumber;//车次
    private String seatnumber;//座位号
    private String ordernumber;//订单号
    private String orderperson;//订票人

    public TrainSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent) {
        //date为出发时间,title为"出发地目的地"
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent);
    }

    public TrainSchedule() {

    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getArrivaltime() {
        return arrivaltime;
    }

    public void setArrivaltime(String arrivaltime) {
        this.arrivaltime = arrivaltime;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTrainnumber() {
        return trainnumber;
    }

    public void setTrainnumber(String trainnumber) {
        this.trainnumber = trainnumber;
    }

    public String getSeatnumber() {
        return seatnumber;
    }

    public void setSeatnumber(String seatnumber) {
        this.seatnumber = seatnumber;
    }

    public String getOrdernumber() {
        return ordernumber;
    }

    public void setOrdernumber(String ordernumber) {
        this.ordernumber = ordernumber;
    }

    public String getOrderperson() {
        return orderperson;
    }

    public void setOrderperson(String orderperson) {
        this.orderperson = orderperson;
    }
}
