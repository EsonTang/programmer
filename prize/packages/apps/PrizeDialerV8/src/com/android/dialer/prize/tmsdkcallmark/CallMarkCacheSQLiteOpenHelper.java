package com.android.dialer.prize.tmsdkcallmark;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wangzhong on 2017/5/5.
 */

public class CallMarkCacheSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "prize_call_mark_cache.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "phone_number_type";
    public static final String TABLE_COLUMN_ID = "_id";
    public static final String TABLE_COLUMN_PHONE_NUMBER = "number";
    public static final String TABLE_COLUMN_TIME = "time";
    public static final String TABLE_COLUMN_TYPE = "type";

    public CallMarkCacheSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (" + TABLE_COLUMN_ID + " integer primary key autoincrement, "
                + TABLE_COLUMN_PHONE_NUMBER + " text, "
                + TABLE_COLUMN_TIME + " text, "
                + TABLE_COLUMN_TYPE + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
