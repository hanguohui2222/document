package com.gionee.secretary.bean;

import java.io.Serializable;

/**
 * Created by hangh on 3/9/17.
 */
public class RecordBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int time;//录音时长
    private String uri;
    private int noteId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }
}
