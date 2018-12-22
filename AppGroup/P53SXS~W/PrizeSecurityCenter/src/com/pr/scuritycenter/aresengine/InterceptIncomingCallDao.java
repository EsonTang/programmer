package com.pr.scuritycenter.aresengine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptIncomingCallDao {

	private InterceptIncomingCallDBOpenHelper helper;
	private String table;

	public InterceptIncomingCallDao(Context context) {
		this.table = InterceptIncomingCallDBOpenHelper.TABLE_INTERCEPT_INCOMING_CALL;
		this.helper = new InterceptIncomingCallDBOpenHelper(context);
	}

	public boolean isFind(String number) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + table + " where number = ?", new String[]{number});
			if (cursor.moveToFirst()) {
				result = true;
			}
			cursor.close();
			db.close();
		}
		return result;
	}

	public List<InterceptIncomingCallBean> findAll() {
		List<InterceptIncomingCallBean> numbers = new ArrayList<InterceptIncomingCallBean>();
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select name,number,time from " + table, null);
			while (cursor.moveToNext()) {
				InterceptIncomingCallBean interceptIncomingCallBean = new InterceptIncomingCallBean();
				interceptIncomingCallBean.setName(cursor.getString(0));
				interceptIncomingCallBean.setNumber(cursor.getString(1));
				interceptIncomingCallBean.setTime(cursor.getString(2));
				numbers.add(interceptIncomingCallBean);
				interceptIncomingCallBean = null;
			}
			cursor.close();
			db.close();
		}
		return numbers;
	}

	public boolean add(String name, String number, String time) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("insert into " + table + " (name,number,time) values(?,?,?)", new Object[]{name, number, time});
			db.close();
		}
		return true;
	}

	public void delete(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("delete from " + table + " where number=?", new String[]{number});
			db.close();
		}
	}

	public void deleteAll() {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("delete from " + table);
			db.close();
		}
	}

}
