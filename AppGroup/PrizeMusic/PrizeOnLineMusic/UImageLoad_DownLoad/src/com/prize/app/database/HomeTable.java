package com.prize.app.database;

import android.provider.BaseColumns;

/**
 ** 数据库 首页表的字段及其他信息
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeTable {
	/** 表名 */
	public static final String TABLE_NAME_GAME = "table_home";

	public static final String ID = BaseColumns._ID;
	/** 名称 */
	public static final String HOME_NAME = "home_name";
	/** 类型 专题或者app */
	public static final String HOME_TYPE = "home_type";
	/** 对应的id */
	public static final String HOME_CODE = "home_code";
	/** 图片地址 */
	public static final String HOME_IMG = "home_img";
	/** 简介 */
	public static final String HOME_DESC = "home_desc";

	/** 排版类型 */
	public static final String HOME_CONTENT_TYPE = "home_c_type";

	/** 个数 */
	public static final String HOME_GIFTCOUNT = "HOME_GIFTCOUNT";
	/** HOME Table */
	public static final String SQL_DELETE_HOME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_GAME;
	/***
	 * 创建首页数据表Sqlite语句
	 */
	public static final String SQL_CREATE_HOME_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_GAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ HOME_CODE
			+ " INTEGER,"
			+ HOME_NAME
			+ " TEXT,"
			+ HOME_IMG
			+ " TEXT,"
			+ HOME_TYPE
			+ " TEXT,"
			+ HOME_DESC
			+ " TEXT,"
			+ HOME_CONTENT_TYPE
			+ " INTEGER,"
			+ HOME_GIFTCOUNT + " INTEGER" + ")";

}
