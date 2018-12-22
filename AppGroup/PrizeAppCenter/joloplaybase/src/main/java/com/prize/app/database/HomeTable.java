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
	/** 包名 */
	public static final String HOME_PACKAGE = "home_package";
	/** 名称 */
	public static final String HOME_WEBURL = "home_weburl";
	/** 类型 专题或者app */
	public static final String HOME_TYPE = "home_type";
	/** 推荐栏的关键词 */
	public static final String HOME_KEY = "home_key";
	/** 对应的id */
	public static final String HOME_CODE = "home_code";
	/** 图片地址 */
	public static final String HOME_IMG = "home_img";
	/** 简介 */
	public static final String HOME_DESC = "home_desc";
	/** 增加MD5字段 11/19 longbaoxiu */

	public static final String HOME_APKMD5 = "home_apkmd5";
	/** 排版类型 */
	public static final String HOME_CONTENT_TYPE = "home_c_type";
	/** 游戏角标 */
	public static final String HOME_CORNER_ICON = "home_cornericon";
	/** 游戏评论 */
	public static final String HOME_COMMENT = "home_comment";
	/** app大小（如：10525） */
	public static final String HOME_SIZE = "home_size";
	/** app大小格式化（如：10M） */
	public static final String HOME_APKSIZEFORMAT = "home_apkSizeformat";
	/** app下载量 */
	public static final String HOME_COUNT = "home_count";
	public static final String HOME_GAMEACTIVITY = "home_activity";
	public static final String HOME_GAMECLASS = "home_class";
	/** app下载地址 */
	public static final String HOME_GAMEDOWNLOADURL = "home_url";
	public static final String HOME_GAMEVERSIONCODE = "home_var";
	/** app评分 */
	public static final String HOME_RATE = "home_rate";
	/**json*/
	public static final String JSON = "json";

	/** HOME Table */
	public static final String SQL_DELETE_HOME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_GAME;
	/** 礼包个数 */
	public static final String HOME_GIFTCOUNT = "HOME_GIFTCOUNT";
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
			+ HOME_PACKAGE
			+ " TEXT,"
			+ HOME_IMG
			+ " TEXT,"
			+ HOME_TYPE
			+ " TEXT,"
			+ HOME_DESC
			+ " TEXT,"
			+ HOME_SIZE
			+ " TEXT,"
			+ HOME_APKSIZEFORMAT
			+ " TEXT,"
			+ HOME_COUNT
			+ " TEXT,"
			+ HOME_GAMEDOWNLOADURL
			+ " TEXT,"
			+ HOME_RATE
			+ " TEXT,"
			+ HOME_CONTENT_TYPE
			+ " INTEGER,"
			+ HOME_GIFTCOUNT
			+ " INTEGER,"
			+ HOME_KEY
			+ " TEXT,"
			+ HOME_APKMD5
			+ " TEXT,"
			+ JSON
			+ " TEXT,"
			+ HOME_GAMEVERSIONCODE + " TEXT" + ")";

}
