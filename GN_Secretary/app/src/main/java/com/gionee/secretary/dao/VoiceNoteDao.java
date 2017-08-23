package com.gionee.secretary.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gionee.secretary.bean.BaseNoteSchedule;
import com.gionee.secretary.bean.RecordBean;
import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.db.SecretaryDBMetaData;
import com.gionee.secretary.db.SecretaryDBOpenHelper;
import com.gionee.secretary.db.SecretarySQLite;
import com.gionee.secretary.utils.LogUtils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hangh on 6/4/16.
 */
public class VoiceNoteDao {
    private Context mContext;
    private static VoiceNoteDao voiceNoteDao = null;
    private SecretaryDBOpenHelper mDBHelper;

    private VoiceNoteDao(Context context) {
        mContext = context;
        mDBHelper = SecretarySQLite.getDBHelper(context);
    }

    public static VoiceNoteDao getInstance(Context context) {
        if (voiceNoteDao == null) {
            synchronized (VoiceNoteDao.class) {
                if (voiceNoteDao == null) {
                    voiceNoteDao = new VoiceNoteDao(context.getApplicationContext());
                }
            }
        }
        return voiceNoteDao;
    }

    public synchronized void saveVoiceNoteToDB(VoiceNoteBean voiceNoteBean) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TITLE, voiceNoteBean.getTitle());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CONTENT, voiceNoteBean.getContent());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME, voiceNoteBean.getCreateTime());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME, voiceNoteBean.getRemindDate());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE, voiceNoteBean.getIsRemindActive());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT, voiceNoteBean.getSearchContent());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE, voiceNoteBean.getAttachmentType());
        if (db.isOpen()) {
            db.insert(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME, null, contentValues);
            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                voiceNoteBean.setId(cursor.getInt(0));
            }
            int noteid = voiceNoteBean.getId();
            updateRecord(noteid, voiceNoteBean);

        }
    }


    private synchronized void updateRecord(int noteid, VoiceNoteBean voiceNoteBean) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            String where = SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.NOTE_ID + " = ? ";
            String[] cauce = {"" + noteid};
            db.delete(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME, where, cauce);
            for (RecordBean bean : voiceNoteBean.getRecordBeanList()) {
                values.put(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_TIME, bean.getTime());
                values.put(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_URI, bean.getUri());
                values.put(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.NOTE_ID, noteid);
                db.insert(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    /**
     * 批量删除voice便签
     */

    public synchronized boolean deleteVoiceNotes(List<VoiceNoteBean> voiceNoteBeanList) {

        boolean flag = false;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "delete from " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " where "
                + SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID + "= ?";
        db.beginTransaction();
        try {
            for (VoiceNoteBean bean : voiceNoteBeanList) {
                if (db.isOpen()) {
                    db.execSQL(sql, new Object[]{bean.getId()});
                }
            }
            db.setTransactionSuccessful();
            flag = true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return flag;
    }


    public synchronized boolean deleteOneNote(VoiceNoteBean voiceNoteBeanList) {
        boolean flag = false;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "delete from " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " where "
                + SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID + "= ?";
        String recordsql = "delete from " + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME + " where "
                + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.NOTE_ID + "= ?";
        db.beginTransaction();
        try {
            if (db.isOpen()) {
                db.execSQL(sql, new Object[]{voiceNoteBeanList.getId()});
                db.execSQL(recordsql, new Object[]{voiceNoteBeanList.getId()});
            }
            db.setTransactionSuccessful();
            flag = true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return flag;
    }


    public synchronized void updateVoiceToDB(VoiceNoteBean voiceNoteBean) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String where = SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID + " = " + voiceNoteBean.getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TITLE, voiceNoteBean.getTitle());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CONTENT, voiceNoteBean.getContent());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME, voiceNoteBean.getCreateTime());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME, voiceNoteBean.getRemindDate());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE, voiceNoteBean.getIsRemindActive());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT, voiceNoteBean.getSearchContent());
        contentValues.put(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE, voiceNoteBean.getAttachmentType());
        if (db.isOpen()) {
            db.update(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME, contentValues, where, null);
            int noteid = voiceNoteBean.getId();
            updateRecord(noteid, voiceNoteBean);
        }
    }

    public synchronized VoiceNoteBean getVoiceNote(int id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        VoiceNoteBean noteBean = new VoiceNoteBean();
        String where = " where " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID + " = ? ";
        String sql = "select * from " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + where;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            if (cursor.moveToNext()) {
                noteBean.setId(id);
                noteBean.setTitle(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TITLE)));
                noteBean.setContent(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CONTENT)));
                noteBean.setCreateTime(cursor.getLong(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME)));
                noteBean.setRemindDate(cursor.getLong(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME)));
                noteBean.setIsActive(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE)));
                noteBean.setAttachmentType(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE)));
                noteBean.setSearchContent(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return noteBean;
    }

    public synchronized RecordBean getRecordBean(String uriString) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        RecordBean recordBean = new RecordBean();
        String where = " where " + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_URI + " = ? ";
        String sql = "select * from " + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME + where;
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(uriString)});
            if (cursor.moveToNext()) {
                recordBean.setId(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData._ID)));
                recordBean.setTime(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_TIME)));
                recordBean.setUri(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_URI)));
                recordBean.setNoteId(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.NOTE_ID)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return recordBean;

    }

    public synchronized List<VoiceNoteBean> getVoiceNoteListByDate() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<VoiceNoteBean> noteBeans = new ArrayList<>();
        String orderBy = SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME + " DESC";
        if (db.isOpen()) {
            Cursor cursor = db.query(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME, null, null, null, null, null, orderBy);
            noteBeans.clear();
            LogUtils.i("liyy", "voice....getlists:" + cursor.getCount());
            while (cursor.moveToNext()) {
                VoiceNoteBean voiceNoteBean = new VoiceNoteBean();
                voiceNoteBean.setId(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID)));
                voiceNoteBean.setTitle(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TITLE)));
                voiceNoteBean.setContent(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CONTENT)));
                voiceNoteBean.setCreateTime(cursor.getLong(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME)));
                voiceNoteBean.setRemindDate(cursor.getLong(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME)));
                voiceNoteBean.setIsActive(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE)));
                voiceNoteBean.setAttachmentType(cursor.getInt(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE)));
                voiceNoteBean.setSearchContent(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT)));
                noteBeans.add(voiceNoteBean);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return noteBeans;
    }
    
    /*Gionee zhengyt 2016-12-20 add for search not Begin*/
    /*
     * 查询备忘，搜索关键字，包括title和备忘内容搜索
     * */

    public synchronized List<BaseNoteSchedule> queryNoteByKeyWord(String keyWord, int page) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int limit = page * Constants.PAGE_CONTENT_COUNT;
        String table = "t_voice_note";

        String selection = "title like ? or search_content like ?";
        String[] selectionArgs = new String[]{"%" + keyWord + "%", "%" + keyWord + "%",};

        List<BaseNoteSchedule> infos = new ArrayList<>();
        String orderby = " create_time asc";

        Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, orderby, null);
        LogUtils.i("liyy", "cousor....querynotebyword.....count:" + cursor.getCount());
        infos.clear();

        while (cursor.moveToNext()) {
            BaseNoteSchedule info = (BaseNoteSchedule) setBaseNoteScheduleInfoValue(cursor);
            infos.add(info);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return infos;
    }

    private BaseNoteSchedule setBaseNoteScheduleInfoValue(Cursor cursor) {

        int noteId = cursor.getInt(cursor.getColumnIndex("_id"));
        String noteTitle = cursor.getString(cursor.getColumnIndex("title"));
        String noteContent = cursor.getString(cursor.getColumnIndex("content"));
        long noteCreateTime = cursor.getLong(cursor.getColumnIndex("create_time"));
        /*add by zhengjl at 2017-1-22 begin
        解决搜索页面小闹钟不显示的问题
         */
        long remindTime = cursor.getLong(cursor.getColumnIndex("remind_time"));

        BaseNoteSchedule info = new BaseNoteSchedule(noteId, noteContent, noteTitle, noteCreateTime);

//    	info.setNoteTitle(cursor.getString(cursor.getColumnIndex("title")));
//    	info.setNoteTitle(cursor.getString(cursor.getColumnIndex("content")));


        info.setNoteId(cursor.getInt(cursor.getColumnIndex("_id")));
        info.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        info.setNoteContext(cursor.getString(cursor.getColumnIndex("content")));

        info.setRemindDate(remindTime);
        /*add by zhengjl at 2017-1-22 end*/

        return info;
    }

    /*Gionee zhengyt 2016-12-20 add for search not End*/
}
