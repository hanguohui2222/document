package com.gionee.secretary.bean;

import java.util.Date;

/**
 * 信用卡/贷款日程
 * Created by luorw on 11/05/16.
 */
public class BankSchedule extends BaseSchedule {
    private String bankName;//银行名称
    private String repaymentMonth;//还款日
    private String billMonth;//账单月份
    private String repaymentAmount;//还款金额
    private String cardNum;//卡帐号
    private String alertDesc;//提醒描述

    public BankSchedule(Date date, int type, boolean isAllDay, String title, String remindType, String remindPeriod, long remindDate, int isSmartRemind, int isRemindActive, String smsSender, String smsContent, String source) {
        //date为到期还款日,title为“信用卡账单/贷款账单”
        super(date, type, isAllDay, title, remindType, remindPeriod, remindDate, isSmartRemind, isRemindActive, smsSender, smsContent, source);
    }

    public BankSchedule() {
    }

    public String getBillMonth() {
        return billMonth;
    }

    public void setBillMonth(String billMonth) {
        this.billMonth = billMonth;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRepaymentMonth() {
        return repaymentMonth;
    }

    public void setRepaymentMonth(String repaymentMonth) {
        this.repaymentMonth = repaymentMonth;
    }

    public String getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(String repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getAlertDesc() {
        return alertDesc;
    }

    public void setAlertDesc(String alertDesc) {
        this.alertDesc = alertDesc;
    }

    @Override
    public String toString() {
        return super.toString() + "--" + this.bankName + "--" + this.repaymentAmount;
    }
}
