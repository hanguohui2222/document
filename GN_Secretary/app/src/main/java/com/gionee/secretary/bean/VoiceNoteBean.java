package com.gionee.secretary.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hangh on 6/4/16.
 */
public class VoiceNoteBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private long createTime;
    private String content;
    private String title;
    public long remindDate;//备忘提醒时间
    public int isRemindActive;

    private int attachmentType;//附件类型 0:无；1:附件;2:图片;3:录音

    public List<RecordBean> recordBeanList;

    public String search_content;

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

    public long getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public String getSearchContent() {
        return search_content;
    }

    public void setSearchContent(String content) {
        this.search_content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<RecordBean> getRecordBeanList() {
        return recordBeanList;
    }

    public void setRecordBeanList(List<RecordBean> recordBeanList) {
        this.recordBeanList = recordBeanList;
    }

    public int getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(int attachmentType) {
        this.attachmentType = attachmentType;
    }
}
