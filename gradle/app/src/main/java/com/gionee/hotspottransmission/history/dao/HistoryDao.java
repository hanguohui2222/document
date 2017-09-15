package com.gionee.hotspottransmission.history.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.db.WlanDBMetaData;
import com.gionee.hotspottransmission.db.WlanDBOpenHelper;
import com.gionee.hotspottransmission.db.WlanSQLite;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhuboqin on 4/05/16.
 */
public class HistoryDao {

    private final WlanDBOpenHelper helper;
    private SQLiteDatabase db;

    public HistoryDao(Context context){
        helper = WlanSQLite.getSQLiteHelper(context);
    }

    public void insertHistory(HistoryInfo historyInfo){
        db = helper.getReadableDatabase();
        int isSender;
        if(historyInfo.isSender){
            isSender = 1;
        }else {
            isSender = 0;
        }
        ContentValues titleValues = new ContentValues();
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE, historyInfo.deviceName);
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE_ADDRESS, historyInfo.deviceAddress);
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.FILECOUNT, historyInfo.fileCount);
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.TOTALSIZE, historyInfo.fileSize);
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.ISSENDER, isSender);
        titleValues.put(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DATE, historyInfo.date.getTime());
        db.insert(WlanDBMetaData.T_HISTORY_TITLE_MeteData.TABLE_NAME, null, titleValues);
        //modified by luorw for GNSPR #12360 20160527 begin
        Cursor cursor = db.rawQuery("select last_insert_rowid() from " + WlanDBMetaData.T_HISTORY_TITLE_MeteData.TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            historyInfo.id = cursor.getInt(0);
        }
        //modified by luorw for GNSPR #12360 20160527 end
        List<FileInfo> fileInfoList = historyInfo.files;
        for(FileInfo fileInfo : fileInfoList){
            ContentValues values = new ContentValues();
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILENAME, fileInfo.getFileName() );
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILESTATE, fileInfo.getState() );
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILETYPE, fileInfo.getFileType() );
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILESIZE, fileInfo.getFileSize() );
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILEDATETIME, fileInfo.getModifiedDate() );
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILEURI, fileInfo.getUriString());
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.FILEPATH, fileInfo.getFilePath());
            values.put(WlanDBMetaData.T_FILE_INFO_MeteData.HISTORY_TITLE_ID, historyInfo.id);
            db.insert(WlanDBMetaData.T_FILE_INFO_MeteData.TABLE_NAME,null,values);
        }
        if(db.isOpen()){
            cursor.close();
            db.close();
        }
    }

    public synchronized List<HistoryInfo> findAllTitle(){
        db = helper.getWritableDatabase();
        List<HistoryInfo> historyInfos = new ArrayList<>();
        Cursor cursor = null;
        Cursor cursor2 = null;
        if(db.isOpen()){
            try {
                cursor = db.rawQuery("SELECT * FROM " + WlanDBMetaData.T_HISTORY_TITLE_MeteData.TABLE_NAME + " order by " + WlanDBMetaData.T_HISTORY_TITLE_MeteData._ID + " desc", null);
                historyInfos.clear();
                while (cursor.moveToNext()) {
                    HistoryInfo historyInfo = new HistoryInfo();
                    historyInfo.deviceName = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE));
                    historyInfo.deviceAddress = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE_ADDRESS));
                    historyInfo.fileCount = cursor.getInt(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.FILECOUNT));
                    historyInfo.fileSize = cursor.getLong(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.TOTALSIZE));
                    int isSender = cursor.getInt(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.ISSENDER));
                    if (isSender == 0) { //0接收者,1发送者
                        historyInfo.isSender = false;
                    } else {
                        historyInfo.isSender = true;
                    }
                    long date = cursor.getLong(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData.DATE));
                    historyInfo.date = new Date(date);
                    int title_id = cursor.getInt(cursor.getColumnIndex(WlanDBMetaData.T_HISTORY_TITLE_MeteData._ID));
                    historyInfo.id = title_id;
                    LogUtil.i("HistoryDao" + title_id);
                    cursor2 = db.query(WlanDBMetaData.T_FILE_INFO_MeteData.TABLE_NAME, null,
                            WlanDBMetaData.T_FILE_INFO_MeteData.HISTORY_TITLE_ID + "=?",
                            new String[]{"" + title_id}, null, null, null);
                    List<FileInfo> fileInfoList = new ArrayList<>();
                    if (cursor2 != null) {
                        while (cursor2.moveToNext()) {
                            FileInfo file = new FileInfo();
                            file.setFileName(cursor2.getString(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILENAME)));
                            file.setFileSize(cursor2.getLong(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILESIZE)));
                            file.setFileType(cursor2.getInt(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILETYPE)));
                            file.setUriString(cursor2.getString(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILEURI)));
                            file.setFilePath(cursor2.getString(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILEPATH)));
                            file.setState(cursor2.getInt(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILESTATE)));
                            file.setModifiedDate(cursor2.getLong(cursor2.getColumnIndex(WlanDBMetaData.T_FILE_INFO_MeteData.FILEDATETIME)));
                            fileInfoList.add(file);
                        }
                        historyInfo.files = fileInfoList;
                        historyInfos.add(historyInfo);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (cursor2 != null && !cursor2.isClosed()) {
                    cursor2.close();
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }

                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }

        return historyInfos;
    }

    public boolean deleteAllHistory(){
        db = helper.getReadableDatabase();
        int fileRes = db.delete(WlanDBMetaData.T_FILE_INFO_MeteData.TABLE_NAME, null, null);
        int titleRes = db.delete(WlanDBMetaData.T_HISTORY_TITLE_MeteData.TABLE_NAME,null,null);
        db.close();
        if(fileRes != 0 && titleRes != 0){
            return true;
        }else
            return false;
    }

    public void updateDeviceName(String address, String newName) {
        LogUtil.i("historyRecord update, address: " + address + ", newName: " + newName);
        if (address == null || newName == null) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "update " + WlanDBMetaData.T_HISTORY_TITLE_MeteData.TABLE_NAME + " set "
                + WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE + "='" + newName + "'"
                + " where " + WlanDBMetaData.T_HISTORY_TITLE_MeteData.DEVICE_ADDRESS + "='" + address + "';";
        if (db.isOpen()) {
            db.execSQL(sql);
            db.close();
        }
    }
}
