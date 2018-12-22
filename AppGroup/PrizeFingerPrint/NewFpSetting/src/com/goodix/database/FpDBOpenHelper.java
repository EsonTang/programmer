package com.goodix.database;

import com.goodix.model.AppLockEvent;
import com.goodix.model.AppLockInfo;
import com.goodix.model.FpInfo;
import com.goodix.util.ConstantUtil;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

public class FpDBOpenHelper extends SQLiteOpenHelper{

	public static final String DB_NAME = "fp_data.db";
	public static final int DATABASE_VERSION = 2;

	private static FpDBOpenHelper instance;

	public static FpDBOpenHelper getInstance(Context context){
		if(null == instance){
			instance = new FpDBOpenHelper(context);
		}
		return instance;
	}

	public FpDBOpenHelper(Context context ){
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		createFpInfoTable(db);
		createAppLockTable(db);
		createAppLockInfoTable(db);
	}
	
	public void createTable(SQLiteDatabase db,String tableName){
		if(tableName.equals(ConstantUtil.APP_LOCK_TB_NAME)){
			createAppLockTable(db);
		}else if(tableName.equals(ConstantUtil.APP_LOCK_INFO_TB_NAME)){
			createAppLockInfoTable(db);
		}else if(tableName.equals(ConstantUtil.FP_INFO_TB_NAME)){
			createFpInfoTable(db);
		}
	}
	
	private void createFpInfoTable(SQLiteDatabase db){
		String fpSql = "CREATE TABLE IF NOT EXISTS " + ConstantUtil.FP_INFO_TB_NAME + " (" + FpInfo.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + FpInfo.NAME
				+ " TEXT," + FpInfo.DESCRIPTION + " TEXT," + FpInfo.URI + " TEXT)";
		db.execSQL(fpSql);
	}
	
	private void createAppLockTable(SQLiteDatabase db){
		String appSql = "CREATE TABLE IF NOT EXISTS " + ConstantUtil.APP_LOCK_TB_NAME + " (" + AppLockEvent.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + AppLockEvent.PKG_NAME
				+ " TEXT," + AppLockEvent.CLASS_NAME+ " TEXT," + AppLockEvent.NEED_LOCK + " INTEGER DEFAULT(0) ,"  
				+ AppLockEvent.ALREADY_UNLOCKER + " INTEGER DEFAULT(0))";
		db.execSQL(appSql);
	}
	
	private void createAppLockInfoTable(SQLiteDatabase db){
		String fpSql = "CREATE TABLE IF NOT EXISTS " + ConstantUtil.APP_LOCK_INFO_TB_NAME + " (" + AppLockInfo.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + AppLockInfo.INFO + " TEXT)";
		db.execSQL(fpSql);
	}
	
	public void deleteTableByName(SQLiteDatabase db,String TABLE_NAME){
		String sql=" DROP TABLE IF EXISTS "+TABLE_NAME;
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldvision, int newvision){
		db.execSQL("DROP TABLE IF EXISTS " + ConstantUtil.FP_INFO_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ConstantUtil.APP_LOCK_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ConstantUtil.APP_LOCK_INFO_TB_NAME);
		onCreate(db);
	}
}
