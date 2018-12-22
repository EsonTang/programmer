package com.android.lpserver.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;

/**
 * Created by prize on 2016/11/9.
 */
public class HBHelper extends SQLiteOpenHelper implements Serializable{
    public HBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table red_bag1 (_id integer primary key autoincrement ,sender text , get_money real , date text)");

        db.execSQL("create table red_bag2 (_id integer primary key autoincrement ,success_amount integer , ave_time real , sum real,total_time real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if(newVersion > oldVersion){
            db.execSQL();
            onCreate(db);
        }*/
    }
}
