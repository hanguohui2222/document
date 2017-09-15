package com.gionee.hotspottransmission.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gionee.hotspottransmission.db.WlanDBMetaData.T_DEVICE_INFO_MeteData;
import com.gionee.hotspottransmission.db.WlanDBMetaData.T_FILE_INFO_MeteData;
import com.gionee.hotspottransmission.db.WlanDBMetaData.T_HISTORY_TITLE_MeteData;
import com.gionee.hotspottransmission.utils.LogUtil;

/**
 * Created by zhuboqin on 30/04/16.
 */
public class WlanDBOpenHelper extends SQLiteOpenHelper {

    public WlanDBOpenHelper(Context context) {
        super(context, WlanDBMetaData.DATABASE_NAME, null, WlanDBMetaData.DATABASE_VERSION);
    }

    public WlanDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_DEVICE_INFO_MeteData.TABLE_NAME + "("
                + T_DEVICE_INFO_MeteData._ID + " integer primary key autoincrement,"
                + T_DEVICE_INFO_MeteData.DEVICENAME + " varchar(40), "
                + T_DEVICE_INFO_MeteData.CONNECTTIME + " date,"
                + T_DEVICE_INFO_MeteData.ADDRESS + " varchar(40),"
                + T_DEVICE_INFO_MeteData.STATUS + " varchar(40),"
                + T_DEVICE_INFO_MeteData.IP + " varchar(100)"
                + ")");

        db.execSQL("CREATE TABLE " + T_HISTORY_TITLE_MeteData.TABLE_NAME + "("
                + T_DEVICE_INFO_MeteData._ID + " integer primary key autoincrement,"
                + T_HISTORY_TITLE_MeteData.DEVICE + " varchar(40) ,"
                + T_HISTORY_TITLE_MeteData.DEVICE_ADDRESS + " varchar(40) ,"
                + T_HISTORY_TITLE_MeteData.FILECOUNT + " integer,"
                + T_HISTORY_TITLE_MeteData.TOTALSIZE + " long,"
                + T_HISTORY_TITLE_MeteData.ISSENDER + " integer,"
                + T_HISTORY_TITLE_MeteData.DATE + " date"
                + ")");

        String sql = "CREATE TABLE " + T_FILE_INFO_MeteData.TABLE_NAME + "("
                + T_FILE_INFO_MeteData._ID + " integer primary key autoincrement,"
                + T_FILE_INFO_MeteData.FILENAME + " varchar(40), "
                + T_FILE_INFO_MeteData.FILESTATE + " integer,"
                + T_FILE_INFO_MeteData.FILETYPE + " integer,"
                + T_FILE_INFO_MeteData.FILESIZE + " long,"
                + T_FILE_INFO_MeteData.FILEDATETIME + " long,"
                + T_FILE_INFO_MeteData.FILEURI + " varchar(100),"
                + T_FILE_INFO_MeteData.FILEPATH + " varchar(100),"
                + T_FILE_INFO_MeteData.HISTORY_TITLE_ID + " integer,"
                + "CONSTRAINT fk_historyTitleId FOREIGN KEY("+T_FILE_INFO_MeteData.HISTORY_TITLE_ID+") references "
                + T_HISTORY_TITLE_MeteData.TABLE_NAME+"("+T_HISTORY_TITLE_MeteData._ID+")"
                + ")";
        LogUtil.e(sql);
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
