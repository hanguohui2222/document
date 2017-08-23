package com.gionee.secretary.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 备忘基类
 */

public class BaseNoteSchedule extends BaseSchedule {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int id;//备忘id
    public String title;//备忘标题
    private String content; //备忘内容；
    private long createTime;  //备忘创建时间


    public BaseNoteSchedule(int noteId, String noteContent, String noteTitle) {
        super(noteId, noteContent, noteTitle);

        this.title = noteTitle;
        this.content = noteContent;

    }

    public BaseNoteSchedule(int noteId, String noteContent, String noteTitle, long crateTime) {

        this.title = noteTitle;
        this.content = noteContent;
        this.createTime = crateTime;

    }

    public BaseNoteSchedule(String mTitle, String mContent) {
        this.title = mTitle;
        this.content = mContent;
    }


    public long getNoteCreateTime() {
        return this.createTime;
    }

    public String getNoteTitle() {
        return this.title;
    }

    public String getNoteContent() {
        return this.content;
    }

    public int getNoteId() {
        return this.id;
    }

    public void setNoteTitle(String setTitle) {
        this.title = setTitle;
    }

    public void setNoteContent(String setContent) {
        this.content = setContent;
    }

    public void setNoteId(int noteId) {
        this.id = noteId;
    }
}