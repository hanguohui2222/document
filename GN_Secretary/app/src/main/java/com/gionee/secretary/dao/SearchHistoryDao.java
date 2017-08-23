package com.gionee.secretary.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gionee.secretary.bean.SearchHistoryInfo;
import com.gionee.secretary.db.SecretaryDBMetaData;
import com.gionee.secretary.db.SecretaryDBOpenHelper;
import com.gionee.secretary.db.SecretarySQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luorw on 5/11/16.
 */
public class SearchHistoryDao {
    private SecretaryDBOpenHelper mDBHelper;

    public SearchHistoryDao(Context context) {
        mDBHelper = SecretarySQLite.getDBHelper(context);
    }

    /**
     * 保存搜索历史
     *
     * @param content
     */
    public synchronized void saveSearchHistoryToDB(String content) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        /*String sql = "insert into " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME + " values(null, "
                +  "'" + content + "');";*/
        ContentValues values = new ContentValues();
        values.put(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT, content);
        if (db.isOpen()) {
            db.insert(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME, null, values);
            //db.close();
        }
    }

    /**
     * 清除所有搜索历史
     *
     * @return
     */
    public synchronized boolean clearSearchHistory() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int result = db.delete(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME, null, null);
        //db.close();
        if (result != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * del by liyy 2015-05-13
     * 查询所有搜索记录
     * @return
     */
//    public List<SearchHistoryInfo> querySearchHistory(){
//        SQLiteDatabase db = mDBHelper.getWritableDatabase();
//        List<SearchHistoryInfo> historyInfos = new ArrayList<>();
//        if(db.isOpen()){
//            Cursor cursor = db.rawQuery("SELECT * FROM " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME, null);
//            historyInfos.clear();
//            while(cursor.moveToNext()){
//                SearchHistoryInfo historyInfo = new SearchHistoryInfo();
//                historyInfo.setContent(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT)));
//                historyInfos.add(historyInfo);
//            }
//            db.close();
//        }
//        return historyInfos;
//    }


    /**
     * add by liyy 2015-05-13
     * 查询指定条件记录
     *
     * @return
     */
    public synchronized List<String> querySearchHistory(String option) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<String> historyInfos = new ArrayList<>();
        String where = SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT + " = ?";
        if (db.isOpen()) {
            //Cursor cursor = db.rawQuery("SELECT * FROM " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME+" where "+SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT+"='"+option + "'", null );
            Cursor cursor = db.query(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME, null, where, new String[]{option}, null, null, null);
            historyInfos.clear();
            while (cursor.moveToNext()) {
                historyInfos.add(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            //db.close();
        }
        return historyInfos;
    }

    /**
     * add by liyy 2015-05-13
     * 查询包含指定条件记录
     *
     * @return
     */
    public synchronized List<String> querySearchTextByKeyWord(String option) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<String> historyInfos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME + " where " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT + " like '%" + option + "%'", null);
            historyInfos.clear();
            while (cursor.moveToNext()) {
                historyInfos.add(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            //db.close();
        }
        return historyInfos;
    }

    /**
     * add by liyy 2015-05-13
     * 查询所有搜索记录
     *
     * @return
     */
    public synchronized List<String> querySearchHistory() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<String> historyInfos = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.TABLE_NAME, null);
            historyInfos.clear();
            while (cursor.moveToNext()) {
                historyInfos.add(cursor.getString(cursor.getColumnIndex(SecretaryDBMetaData.T_SEARCH_HISTORY_MeteData.CONTENT)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            //db.close();
        }
        return historyInfos;
    }
}
