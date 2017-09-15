package com.gionee.hotspottransmission.db;

import android.content.Context;

/**
 * Created by zhuboqin on 30/04/16.
 */
public class WlanSQLite {

    private static WlanDBOpenHelper helper ;

    public static WlanDBOpenHelper getSQLiteHelper(Context context){
        if(helper == null){
            synchronized (WlanSQLite.class){
                if(helper == null){
                    helper = new WlanDBOpenHelper(context);
                }
            }
        }
        return helper;
    }

}
