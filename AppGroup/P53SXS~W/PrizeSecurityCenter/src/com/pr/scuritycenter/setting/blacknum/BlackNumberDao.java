package com.pr.scuritycenter.setting.blacknum;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author wangzhong
 *
 */
public class BlackNumberDao {
	
    private BlackNumberDBOpenHelper helper;

    public BlackNumberDao(Context context) {
        helper = new BlackNumberDBOpenHelper(context);
    }

    public boolean find(String number) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from blacknumber where number = ?", new String[]{number});
            if (cursor.moveToFirst()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public int findNumberMode(String number) {
        int result = -1;
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select mode from blacknumber where number = ?", new String[]{number});
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public boolean add(String number, int mode) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("insert into blacknumber (number,mode) values(?,?)", new Object[]{number, mode});
            db.close();
        }
        return find(number);
    }

    public void delete(String number) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("delete from blacknumber where number=?", new String[]{number});
            db.close();
        }
    }

    public void update(String oldnumber, String newnumber, int mode) {
        if (newnumber.isEmpty()) {
            newnumber = oldnumber;
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("update blacknumber set number=?,mode=? where number=?", new Object[]{newnumber, mode, oldnumber});
            db.close();
        }
    }

    public List<BlackNumberBean> findAll() {
        List<BlackNumberBean> numbers = new ArrayList<BlackNumberBean>();
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select number,mode from blacknumber", null);
            while (cursor.moveToNext()) {
            	BlackNumberBean blackNumber = new BlackNumberBean();
                blackNumber.setNumber(cursor.getString(0));
                blackNumber.setMode(cursor.getInt(1));
                numbers.add(blackNumber);
                blackNumber = null;
            }
            cursor.close();
            db.close();
        }
        return numbers;
    }
    
}
