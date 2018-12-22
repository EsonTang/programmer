package com.pr.scuritycenter.aresengine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptIncomingCallDBOpenHelper extends SQLiteOpenHelper {
	
	private final static String DB_INTERCEPT_INCOMING_CALL = "intercept_incomingcall.db";
	private final static int VERSION_INTERCEPT_INCOMING_CALL = 1;
	
	public final static String TABLE_INTERCEPT_INCOMING_CALL = "intercept_incomingcall";

	public InterceptIncomingCallDBOpenHelper(Context context) {
		super(context, DB_INTERCEPT_INCOMING_CALL, null, VERSION_INTERCEPT_INCOMING_CALL);
	}

	/**
	 * Incoming call intercept form : _id, name, number, time;
	 */
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL("create table " + TABLE_INTERCEPT_INCOMING_CALL + " (_id integer primary key autoincrement, name varchar(20), number varchar(20), time varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}

}
