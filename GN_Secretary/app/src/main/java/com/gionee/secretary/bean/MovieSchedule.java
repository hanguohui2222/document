package com.gionee.secretary.bean;

import java.util.Date;

/**
 * 电影日程
 * Created by luorw on 11/05/16.
 */
public class MovieSchedule extends BaseSchedule {
    private String movieName;//电影名称
    private String cinemaName;//电影院名称
    private String seatDesc;//座位描述
    private String ticketCertificate;//取票凭证
    private String playTime;//观影时间

    public MovieSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent, String source) {
        //date为播放日期,title为电影名称
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent, source);
    }

    public MovieSchedule() {
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getSeatDesc() {
        return seatDesc;
    }

    public void setSeatDesc(String seatDesc) {
        this.seatDesc = seatDesc;
    }

    public String getTicketCertificate() {
        return ticketCertificate;
    }

    public void setTicketCertificate(String ticketCertificate) {
        this.ticketCertificate = ticketCertificate;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    @Override
    public String toString() {
        return super.toString() + "--" + this.movieName + "--" + this.ticketCertificate;
    }
}
