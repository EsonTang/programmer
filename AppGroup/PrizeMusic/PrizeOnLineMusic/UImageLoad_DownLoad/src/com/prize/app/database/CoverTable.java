package com.prize.app.database;

import android.provider.BaseColumns;

/**
 ** 数据库 首页表的字段及其他信息
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CoverTable {
	/** 表名 */
	public static final String TABLE_NAME_GAME = "table_cover";

	public static final String ID = BaseColumns._ID;

	public static final String COVERID = "cover_id";

	public static final String ASSOCIATEID = "associateId";

	public static final String ADTYPE = "adType";

	public static final String TITLE = "title";

	public static final String IMAGEURL = "imageUrl";

	public static final String URL = "url";

	public static final String DESCRIPTION = "description";

	public static final String CREATETIME = "createTime";

	public static final String STATUS = "status";

	public static final String POSITION = "position";
	
	public static String SECONDS ="seconds";
	
	public static String STARTTIME ="startTime";
	
	public static String ENDTIME ="endTime";

	/*
	 * "id": 18, "associateId": 0, "adType": "web", "title": "启动广告", "imageUrl":
	 * "http://cdnimages.oss-cn-hangzhou.aliyuncs.com/appstore/ad/启动广告_1447069709471_关于.png"
	 * , "url": "http://wefire.qq.com/", "description": "阿斯蒂芬", "createTime":
	 * "2015-11-09", "status": 1, "position": 3
	 */

	/** HOME Table */
	public static final String SQL_DELETE_COVER_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_GAME;
	/***
	 * 创建首页数据表Sqlite语句
	 */
	public static final String SQL_CREATE_COVER_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_GAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ COVERID
			+ " INTEGER,"
			+ ASSOCIATEID
			+ " TEXT,"
			+ ADTYPE
			+ " TEXT,"
			+ TITLE
			+ " TEXT,"
			+ URL
			+ " TEXT,"
			+ IMAGEURL
			+ " TEXT,"
			+ DESCRIPTION
			+ " TEXT,"
			+ CREATETIME
			+ " TEXT,"
			+ STATUS
			+ " INTEGER,"
			+ SECONDS
			+ " INTEGER,"
			+ STARTTIME
			+ " TEXT,"
			+ ENDTIME
			+ " TEXT,"
			+ POSITION
			+ " INTEGER" + ")";

}
