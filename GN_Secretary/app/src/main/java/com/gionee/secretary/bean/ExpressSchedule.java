package com.gionee.secretary.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 快递日程
 * Created by luorw on 11/05/16.
 */
public class ExpressSchedule extends BaseSchedule {
    private String expressNum;//快递单号
    private String expressCompany;//快递公司
    private String expressState;//派送状态
    private String expressProgress;//派送进度描述
    private String expressCode;//快递简写;
    private Date trace_date;
    private int State;//快递鸟物流状态: 2-在途中，3-签收,4-问题件
    private List<Trace> Traces;
    private String Reason;

    public ExpressSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent) {
        //date为拦截到快递短信的时间，只要未签收date就顺延下去，title为快递公司名称
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent);
    }

    public ExpressSchedule() {
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public Date getTrace_date() {
        return trace_date;
    }

    public void setTrace_date(Date trace_date) {
        this.trace_date = trace_date;
    }

    public List<Trace> getTraces() {
        return Traces;
    }

    public void setTraces(List<Trace> traces) {
        this.Traces.addAll(traces);
    }

    public String getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(String expressCode) {
        this.expressCode = expressCode;
    }

    public void setState(int state) {
        this.State = state;
    }

    public int getState() {
        return State;
    }

    public String getExpressNum() {
        return expressNum;
    }

    public void setExpressNum(String expressNum) {
        this.expressNum = expressNum;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getExpressState() {
        return expressState;
    }

    public void setExpressState(String expressState) {
        this.expressState = expressState;
    }

    public String getExpressProgress() {
        return expressProgress;
    }

    public void setExpressProgress(String expressProgress) {
        this.expressProgress = expressProgress;
    }

    @Override
    public String toString() {
        return super.toString() + "--" + this.expressNum + "--" + this.expressCompany;
    }

    public static class Trace implements Serializable {
        private static final long serialVersionUID = 1L;
        private String AcceptTime;
        private String AcceptStation;

        public String getAcceptStation() {
            return AcceptStation;
        }

        public void setAcceptStation(String acceptStation) {
            AcceptStation = acceptStation;
        }

        public String getAcceptTime() {
            return AcceptTime;
        }

        public void setAcceptTime(String acceptTime) {
            AcceptTime = acceptTime;
        }
    }

}
