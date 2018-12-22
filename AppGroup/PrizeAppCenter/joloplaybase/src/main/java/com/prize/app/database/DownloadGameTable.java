package com.prize.app.database;

import android.provider.BaseColumns;

/**
 * 前台下载的表
 */
public class DownloadGameTable {

	/* download game info 表名 */
	public static final String TABLE_NAME_GAME = "table_game";

	private static final String ID = BaseColumns._ID;
	public static final String GAME_CODE = "game_code";
	public static final String GAME_PACKAGE = "pkg_name";
	public static final String GAME_CLASS = "class_name";
	public static final String GAME_NAME = "game_name";
	public static final String GAME_ICON_URL = "icon_url"; // 游戏显示小图标
	public static final String GAME_VERSION_CODE = "version_code";
	public static final String GAME_APK_SIZE = "apk_size";
	public static final String GAME_APK_URL = "apk_url";
	public static final String GAME_DOWNLOAD_STATE = "download_state";
	public static final String GAME_LOADED_SIZE = "loaded_size"; // 已经下载的大小
	public static final String GAME_TYPE = "game_type";
	public static final String GAME_LOAD_FLAG = "load_flag"; // 游戏的标记
	/**2.0版本差分包下载完成合并后需要MD5后与该值进行比较，相等才成功那**/
	public static final String GAME_TOAPKMD5 = "game_toapkmd5";//是否差分包下载
	/**应用所在页面以及位置信息  2.7add**/
	public static final String APP_PAGEINFO = "app_pageInfo";
	/**预留信息字段  2.7add**/
	public static final String APP_OTHERINFO = "app_otherInfo";

	public static final String SQL_DELETE_GAME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_GAME;
	/**2.0版增加GAME_TOAPKMD5字段 longbaoxiu 20160819**/
	public static final String SQL_CREATE_GAME_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_GAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ GAME_CODE
			+ " TEXT,"
			+ GAME_PACKAGE
			+ " TEXT UNIQUE,"
			+ GAME_CLASS
			+ " TEXT,"
			+ GAME_NAME
			+ " TEXT,"
			+ GAME_ICON_URL
			+ " TEXT,"
			+ GAME_VERSION_CODE
			+ " INTEGER,"
			+ GAME_APK_SIZE
			+ " INTEGER,"
			+ GAME_APK_URL
			+ " TEXT,"
			+ GAME_DOWNLOAD_STATE
			+ " INTEGER,"
			+ GAME_LOADED_SIZE
			+ " INTEGER,"
			+ GAME_TYPE
			+ " INTEGER,"
			+ GAME_LOAD_FLAG + " INTEGER," +GAME_TOAPKMD5+" TEXT," +APP_PAGEINFO+" TEXT," +APP_OTHERINFO+"  TEXT" +")";
//			+ GAME_LOAD_FLAG + " INTEGER," +GAME_TOAPKMD5+" TEXT" +")";
}
