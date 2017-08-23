package com.gionee.secretary.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.Timestamp;


/**
 * Created by hangh on 5/23/17.
 */

public class SecretaryProvider extends ContentProvider {

    private Context mContext;
    private SQLiteDatabase mDb;
    private static final String TAG = "SecretaryProvider";
    public static final String AUTHORITY = "secretary";
    //public static final Uri SELF_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/self");
    //public static final Uri TRAIN_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/train");
    public static final int SELF_URI_CODE = 0;
    public static final int TRAIN_URI_CODE = 1;
    public static final int BANK_URI_CODE = 2;
    public static final int EXPRESS_URI_CODE = 3;
    public static final int FLIGHT_URI_CODE = 4;
    public static final int HOTEL_URI_CODE = 5;
    public static final int MOVIE_URI_CODE = 6;
    public static final int SELF_URI_CODE_ID = 100;
    public static final int TRAIN_URI_CODE_ID = 101;
    public static final int BANK_URI_CODE_ID = 102;
    public static final int EXPRESS_URI_CODE_ID = 103;
    public static final int FLIGHT_URI_CODE_ID = 104;
    public static final int HOTEL_URI_CODE_ID = 105;
    public static final int MOVIE_URI_CODE_ID = 106;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "self", SELF_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "train", TRAIN_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "bank", BANK_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "express", EXPRESS_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "flight", FLIGHT_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "hotel", HOTEL_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "movie", MOVIE_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "self/#", SELF_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "train/#", TRAIN_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "bank/#", BANK_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "express/#", EXPRESS_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "flight/#", FLIGHT_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "hotel/#", HOTEL_URI_CODE_ID);
        sUriMatcher.addURI(AUTHORITY, "movie/#", MOVIE_URI_CODE_ID);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        initProviderData();
        return true;
    }

    private void initProviderData() {
        mDb = SecretarySQLite.getDBHelper(mContext).getWritableDatabase();
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (match) {
            case SELF_URI_CODE:
                qb.setTables("t_self_create_schedule_info LEFT JOIN t_base_schedule_info ON t_self_create_schedule_info._id = t_base_schedule_info._id");
                break;
            case TRAIN_URI_CODE:
                qb.setTables("t_train_schedule_info LEFT JOIN t_base_schedule_info ON t_train_schedule_info._id = t_base_schedule_info._id");
                break;
            case BANK_URI_CODE:
                qb.setTables("t_bank_schedule_info LEFT JOIN t_base_schedule_info  ON t_bank_schedule_info._id = t_base_schedule_info._id");
                break;
            case EXPRESS_URI_CODE:
                qb.setTables("t_express_schedule_info LEFT JOIN t_base_schedule_info ON t_express_schedule_info._id = t_base_schedule_info._id");
                break;
            case FLIGHT_URI_CODE:
                qb.setTables("t_flight_schedule_info LEFT JOIN t_base_schedule_info ON t_flight_schedule_info._id = t_base_schedule_info._id");
                break;
            case HOTEL_URI_CODE:
                qb.setTables("t_hotel_schedule_info LEFT JOIN t_base_schedule_info ON t_hotel_schedule_info._id = t_base_schedule_info._id");
                break;
            case MOVIE_URI_CODE:
                qb.setTables("t_movie_schedule_info LEFT JOIN t_base_schedule_info ON t_movie_schedule_info._id = t_base_schedule_info._id");
                break;
            default:
                break;
        }
        Cursor cursor = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    private ContentValues getBaseContentValues(ContentValues values) {
        ContentValues baseValue = new ContentValues();
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TYPE, values.getAsInteger(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TYPE));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TITLE, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TITLE));
        long date = values.getAsLong(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.DATE);
        Timestamp timestamp = new Timestamp(date);
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.DATE, timestamp.toString());
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_ALL_DAY, values.getAsInteger(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_ALL_DAY));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_TYPE, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_TYPE));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_PERIOD, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_PERIOD));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_DATE, values.getAsLong(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.REMIND_DATE));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND, values.getAsInteger(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_SMART_REMIND));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE, values.getAsInteger(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.IS_REMIND_ACTIVE));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.SENDER, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.SENDER));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Content, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Content));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Source, values.getAsString(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.Source));
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.PERIOD_ID, values.getAsInteger(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.PERIOD_ID));
        long broadcastDate = values.getAsLong(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE);
        Timestamp timestampDate = new Timestamp(broadcastDate);
        baseValue.put(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.BROADCAST_SORT_DATE, timestampDate.toString());
        return baseValue;
    }

    private ContentValues getSelfContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS, values.getAsString(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS));
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION, values.getAsString(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.DESCRIPTION));
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE, values.getAsString(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TRIP_MODE));
        long endtime = values.getAsLong(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.END_TIME);
        Timestamp endtimeStamp = new Timestamp(endtime);
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.END_TIME, endtimeStamp.toString());
        long subproiddate = values.getAsLong(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE);
        Timestamp subProidStamp = new Timestamp(subproiddate);
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_DATE, subProidStamp.toString());
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID, values.getAsInteger(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.SUB_PERIOD_ID));
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.PERIOD, values.getAsInteger(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.PERIOD));
        subValue.put(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK, values.getAsString(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.ADDRESS_REMARK));
        return subValue;
    }

    private ContentValues getTrainContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData._ID, rowid);
        long starttime = values.getAsLong(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.START_TIME);
        Timestamp starttimeStamp = new Timestamp(starttime);
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.START_TIME, starttimeStamp.toString());
        long arrivaltime = values.getAsLong(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ARRIVAL_TIME);
        Timestamp arrivalStamp = new Timestamp(arrivaltime);
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ARRIVAL_TIME, arrivalStamp.toString());
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DEPARTURE, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DEPARTURE));
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DESTINATION, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.DESTINATION));
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TRAINNUMBER, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TRAINNUMBER));
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.SEATNUMBER, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.SEATNUMBER));
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERNUMBER, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERNUMBER));
        subValue.put(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERPERSON, values.getAsString(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.ORDERPERSON));
        return subValue;
    }

    private ContentValues getBankContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BANK_NAME, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BANK_NAME));
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.CARD_NUM, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.CARD_NUM));
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_AMOUNT, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_AMOUNT));
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_MONTH, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.REPAYMENT_MONTH));
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BILL_MONTH, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.BILL_MONTH));
        subValue.put(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.ALERT_DESC, values.getAsString(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.ALERT_DESC));
        return subValue;
    }

    private ContentValues getExpressContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE, values.getAsLong(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TRACE_DATE));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY, values.getAsString(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_COMPANY));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM, values.getAsString(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_NUM));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE, values.getAsString(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_STATE));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS, values.getAsString(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_PROGRESS));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE, values.getAsString(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.EXPRESS_CODE));
        subValue.put(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE, values.getAsInteger(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.STATE));
        return subValue;
    }

    private ContentValues getFlightContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.AIRLINE_SOURCE, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.AIRLINE_SOURCE));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ARRIVAL_TIME, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ARRIVAL_TIME));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.FLIGHT_NUM, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.FLIGHT_NUM));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.DESTINATION, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.DESTINATION));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.PASSENGER, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.PASSENGER));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.SERVICE_NUM, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.SERVICE_NUM));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.START_ADDRESS, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.START_ADDRESS));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TICKET_NUM, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TICKET_NUM));
        subValue.put(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ALERT_DESC, values.getAsString(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.ALERT_DESC));
        return subValue;
    }

    private ContentValues getHotelContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKIN_PEOPLE, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKIN_PEOPLE));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKOUT_DATE, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.CHECKOUT_DATE));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_ADDRESS, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_ADDRESS));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_NAME, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.HOTEL_NAME));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_COUNTS, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_COUNTS));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.SERVICE_NUM, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.SERVICE_NUM));
        subValue.put(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_STYLE, values.getAsString(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.ROOM_STYLE));
        return subValue;
    }

    private ContentValues getMovieContentValues(ContentValues values, long rowid) {
        ContentValues subValue = new ContentValues();
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData._ID, rowid);
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.CINEMA_NAME, values.getAsString(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.CINEMA_NAME));
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.MOVIE_NAME, values.getAsString(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.MOVIE_NAME));
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.SEAT_DESC, values.getAsString(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.SEAT_DESC));
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TICKET_CERTIFICATE, values.getAsString(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TICKET_CERTIFICATE));
        subValue.put(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.PLAY_TIME, values.getAsString(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.PLAY_TIME));
        return subValue;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri newUri = null;
        ContentValues baseValue = getBaseContentValues(values);
        long rowid = mDb.insert(SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME, null, baseValue);
        int match = sUriMatcher.match(uri);
        ContentValues subValue = null;
        switch (match) {
            case SELF_URI_CODE:
                subValue = getSelfContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case TRAIN_URI_CODE:
                subValue = getTrainContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case BANK_URI_CODE:
                subValue = getBankContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case EXPRESS_URI_CODE:
                subValue = getExpressContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case FLIGHT_URI_CODE:
                subValue = getFlightContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case HOTEL_URI_CODE:
                subValue = getHotelContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            case MOVIE_URI_CODE:
                subValue = getMovieContentValues(values, rowid);
                mDb.insert(SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME, null, subValue);
                break;
            default:
                break;
        }
        newUri = ContentUris.withAppendedId(uri, rowid);
        if (newUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        String where = "";
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        String baseTable = SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME;
        int count = 0;
        try {
            mDb.beginTransaction();
            switch (match) {
                case SELF_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case TRAIN_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case BANK_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case EXPRESS_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case FLIGHT_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case HOTEL_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case MOVIE_URI_CODE_ID:
                    where = "_id = " + uri.getPathSegments().get(1);
                    mDb.delete(baseTable, where, null);
                    count = mDb.delete(table, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }
        String volumeName = uri.getPathSegments().get(0);
        Uri notifyUri = Uri.parse("content://" + AUTHORITY + "/" + volumeName);
        getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int count = 0;
        String table = getTableName(uri);
        ContentValues baseValue = getBaseContentValues(values);
        String where = "_id = " + uri.getPathSegments().get(1);
        long rowId = Long.parseLong(uri.getPathSegments().get(1));
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        String baseTable = SecretaryDBMetaData.T_BASE_SCHEDULE_MeteData.TABLE_NAME;
        ContentValues subValue = null;
        try {
            mDb.beginTransaction();
            mDb.update(baseTable, baseValue, where, null);
            switch (match) {
                case SELF_URI_CODE_ID:
                    subValue = getSelfContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case TRAIN_URI_CODE_ID:
                    subValue = getTrainContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case BANK_URI_CODE_ID:
                    subValue = getBankContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case EXPRESS_URI_CODE_ID:
                    subValue = getExpressContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case FLIGHT_URI_CODE_ID:
                    subValue = getFlightContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case HOTEL_URI_CODE_ID:
                    subValue = getHotelContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                case MOVIE_URI_CODE_ID:
                    subValue = getMovieContentValues(values, rowId);
                    count = mDb.update(table, subValue, where, null);
                    mDb.setTransactionSuccessful();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }
        if (count > 0 && !mDb.inTransaction()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case SELF_URI_CODE:
            case SELF_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_SELF_CREATE_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case TRAIN_URI_CODE:
            case TRAIN_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_TRAIN_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case BANK_URI_CODE:
            case BANK_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_BANK_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case EXPRESS_URI_CODE:
            case EXPRESS_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_EXPRESS_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case FLIGHT_URI_CODE:
            case FLIGHT_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_FLIGHT_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case HOTEL_URI_CODE:
            case HOTEL_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_HOTEL_SCHEDULE_MeteData.TABLE_NAME;
                break;
            case MOVIE_URI_CODE:
            case MOVIE_URI_CODE_ID:
                tableName = SecretaryDBMetaData.T_MOVIE_SCHEDULE_MeteData.TABLE_NAME;
                break;
            default:
                break;
        }
        return tableName;
    }
}
