package com.gionee.secretary.db;

import android.content.Context;

/**
 * Created by luorw on 5/11/16.
 */
public class SecretarySQLite {
    private static SecretaryDBOpenHelper mDBHelper;

    public static SecretaryDBOpenHelper getDBHelper(Context context) {
        if (mDBHelper == null) {
            synchronized (SecretarySQLite.class) {
                if (mDBHelper == null) {
                    mDBHelper = new SecretaryDBOpenHelper(context.getApplicationContext());
                }
            }
        }
        return mDBHelper;
    }
}
