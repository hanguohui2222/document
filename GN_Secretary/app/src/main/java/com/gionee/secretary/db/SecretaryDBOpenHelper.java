package com.gionee.secretary.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gionee.secretary.db.SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by luorw on 5/11/16.
 */
public class SecretaryDBOpenHelper extends SQLiteOpenHelper {

    public SecretaryDBOpenHelper(Context context) {
        super(context, SecretaryDBMetaData.DB_NAME, null, SecretaryDBMetaData.DB_VERSION);
    }

    public SecretaryDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private void createDatabase_v_1(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData._ID + " integer primary key autoincrement,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TYPE + " integer,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TITLE + " varchar(100),"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.DATE + " datetime,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_ALL_DAY + " integer default 0,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_TYPE + " varchar(40) default '准时',"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_PERIOD + " varchar(40) default '一次',"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_DATE + " integer,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND + " integer default 0,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE + " integer default 0,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.SENDER + " TEXT,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Content + " TEXT,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Source + " TEXT,"
                    + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.PERIOD_ID + " integer"
                    + " );");
            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS + " varchar(40),"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION + " varchar(100),"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE + " varchar(40) default '开车',"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.END_TIME + " datetime,"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE + " datetime,"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID + " integer,"
                    + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.PERIOD + " integer"
                    + " );");
            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.START_TIME + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ARRIVAL_TIME + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DEPARTURE + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DESTINATION + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TRAINNUMBER + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.SEATNUMBER + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERNUMBER + " varchar(40),"
                    + SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERPERSON + " varchar(40)"
                    + " );");

            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BANK_NAME + " varchar(40),"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.CARD_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_AMOUNT + " varchar(40),"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_MONTH + " varchar(40),"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BILL_MONTH + " varchar(40),"
                    + SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.ALERT_DESC + " varchar(40)"
                    + " );");

            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE + " datetime,"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY + " varchar(40),"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE + " varchar(40),"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS + " varchar(40),"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE + " varchar(40),"
                    + SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE + " integer default 0"
                    + " );");

            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.AIRLINE_SOURCE + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ARRIVAL_TIME + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.FLIGHT_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.DESTINATION + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.PASSENGER + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.SERVICE_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.START_ADDRESS + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TICKET_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ALERT_DESC + " varchar(40)"
                    + " );");

            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKIN_PEOPLE + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKOUT_DATE + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_ADDRESS + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_NAME + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_COUNTS + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.SERVICE_NUM + " varchar(40),"
                    + SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_STYLE + " varchar(40)"
                    + " );");

            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData._ID + " integer,"
                    + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.CINEMA_NAME + " varchar(40),"
                    + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.MOVIE_NAME + " varchar(40),"
                    + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.SEAT_DESC + " varchar(40),"
                    + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TICKET_CERTIFICATE + " varchar(40)"
                    + " );");

            db.execSQL("CREATE TABLE " + T_SEARCH_HISTORY_MeteData.TABLE_NAME + "( "
                    + T_SEARCH_HISTORY_MeteData._ID + " integer primary key autoincrement,"
                    + T_SEARCH_HISTORY_MeteData.CONTENT + " varchar(100)"
                    + " );");
            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_VOICE_NOTE_MeteData._ID + " integer primary key autoincrement,"
                    + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TITLE + " varchar(40),"
                    + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CONTENT + " text,"
                    + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.CREATE_TIME + " integer"
                    + " );");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createDatabase_v_2(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK + " varchar(100)");
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME + " datetime");
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE + " integer default 0");
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT + " text ");
            if(!isExistAttachmentSegment(db)){
                db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE + "  integer default 0 ");
            }
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.PLAY_TIME + " varchar(30) default 'null'");
            db.execSQL("CREATE TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME + "( "
                    + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData._ID + " integer primary key autoincrement,"
                    + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_TIME + " integer,"
                    + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.RECORD_URI + " text,"
                    + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.NOTE_ID + " integer"
                    + " );");
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE + " datetime");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (SecretaryDBMetaData.DB_VERSION == 1) {
            createDatabase_v_1(db);
        } else if (SecretaryDBMetaData.DB_VERSION == 2) {
            createDatabase_v_1(db);
            createDatabase_v_2(db);
        } else if (SecretaryDBMetaData.DB_VERSION == 3) {
            createDatabase_v_1(db);
            createDatabase_v_2(db);
        }
    }

    /**
     * 处理db各个版本之间从低版本到高版本的升级
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 3) {
            createDatabase_v_2(db);
        } else if (oldVersion == 2 && newVersion == 3) {
            if (!isExistAttachmentSegment(db)) {
                db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " ADD " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE + "  integer default 0 ");
            }

        }
    }

    private boolean isExistAttachmentSegment(SQLiteDatabase db){
        Cursor cursor = null;
        boolean isExist = false;
        try {
            cursor = db.rawQuery("PRAGMA table_info(t_voice_note)", null);
            isExist = false;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    Log.e("hangh", "name = " + name);
                    if (SecretaryDBMetaData.T_VOICE_NOTE_MeteData.ATTACHMENT_TYPE.equals(name)) {
                        isExist = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return isExist;
    }

    /**
     * 数据库降级
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.i("luorw", "onDowngrade , oldVersion = " + oldVersion + " , newVersion = " + newVersion);
//        int tempVersion = oldVersion - 1;
//        for (int i = tempVersion; i <= newVersion; i--) {
//            downgradeVersion(db,tempVersion);
//        }
    }

    private void downgradeVersion(SQLiteDatabase db, int downVersion) {
        LogUtils.i("luorw", "downgradeVersion , downVersion = " + downVersion);
        if (downVersion == 1) {
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK);
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.REMIND_TIME);
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.IS_REMIND_ACTIVE);
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_VOICE_NOTE_MeteData.SEARCH_CONTENT);
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.PLAY_TIME);
            db.execSQL("DROP TABLE IF EXISTS " + SecretaryDBMetaData.T_VOICE_NOTE_RECORD_MeteData.TABLE_NAME);
            db.execSQL("ALTER TABLE " + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME + " DROP COLUMN " + SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE);
        }
    }

}
