package com.gionee.secretary.bean;

import java.util.Date;

/**
 * 提供给xiaoyan的接口
 * Created by hangh on 6/15/16.
 */
public class SubBaseSchedule {
    public int id;//日程id
    public String date;//日程发生时间
    public String endtime;//卡片结束时间
    public String title;//日程标题

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

}
