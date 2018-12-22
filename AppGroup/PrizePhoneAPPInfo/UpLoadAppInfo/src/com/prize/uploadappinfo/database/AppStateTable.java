package com.prize.uploadappinfo.database;

import android.provider.BaseColumns;

/**
 ** 手机应用安装卸载表
 * 
 * @author prize
 * @version V1.0
 */
public class AppStateTable {
	/** 表名 */
	public static final String TABLE_NAME = "app_state";

	public static final String ID = BaseColumns._ID;
	/** 名称 */
	public static final String APP_NAME = "appName";
	/** 包名 */
	public static final String APP_PACKAGE = "packageName";
	/** 安装或则卸载时间 */
	public static final String OP_TIME = "opTime";
	/** 卸载时或者安装时所在的位置 */
	public static final String ADDRESS = "address";
	/** 卸载或安装类型 */
	public static final String TYPE = "type";

	/** HOME Table */
	public static final String SQL_DELETE_HOME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME;
	/***
	 * 创建首页数据表Sqlite语句
	 */
	public static final String SQL_CREATE_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ APP_NAME
			+ " TEXT,"
			+ APP_PACKAGE
			+ " TEXT,"
			+ OP_TIME
			+ " TEXT,"
			+ ADDRESS
			+ " TEXT,"
			+ TYPE + " TEXT" + ")";

}
