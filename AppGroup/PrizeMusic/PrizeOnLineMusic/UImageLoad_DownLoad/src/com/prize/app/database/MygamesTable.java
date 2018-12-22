package com.prize.app.database;

import android.provider.BaseColumns;

/**
 * 实际上已经是游戏管理的数据库
 * 
 * @author prize
 *
 */
public class MygamesTable {

	/* my games info 表名 */
	public static final String TABLE_NAME_MYGAMES = "table_mygames";

	public static final String ID = BaseColumns._ID;
	public static final String MY_GAMES_CODE = "game_code"; // 非平台的游戏，该值为空字串，可以根据该值判断，是否是平台的游戏
	public static final String MY_GAMES_PACKAGE = "pkg_name";
	public static final String MY_GAMES_CLASS = "class_name"; // 非平 台的游戏，入口可能为空
	public static final String MY_GAMES_NAME = "game_name"; // 游戏名称
	public static final String MY_GAMES_ICON_URL = "icon_url"; // 游戏显示小图标
	public static final String MY_GAMES_VERSION_CODE = "version_code";
	public static final String MY_GAMES_APK_SIZE = "apk_size";
	public static final String MY_GAMES_APK_URL = "apk_url"; // 下载地址，可能为空
	/** 游戏的标志位，比如：是否启动过,是否是推荐游戏状态，是否是山寨版等 */
	public static final String MY_GAMES_FLAG = "flag";
	public static final String MY_GAME_TYPE = "game_type"; // 游戏类型，/** 1=单机 2=网游
															// 3=页游 */
	public static final String MY_GAME_ITEMTYPE = "item_type"; // /** 元素类型 1=列表
																// 2=游戏 3=活动
																// ;4=第三方游戏
																// 5=游戏礼包 6=游戏攻略
																// **/
	public static final String MY_GAME_SIGNER_HASHCODE = "signer_hash"; // 签名hashcode

	public static final String SQL_DELETE_MY_GAMES_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_MYGAMES;
	public static final String SQL_CREATE_MY_GAMES_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_MYGAMES
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ MY_GAMES_CODE
			+ " TEXT,"
			+ MY_GAMES_PACKAGE
			+ " TEXT,"
			+ MY_GAMES_CLASS
			+ " TEXT,"
			+ MY_GAMES_NAME
			+ " TEXT,"
			+ MY_GAMES_ICON_URL
			+ " TEXT,"
			+ MY_GAMES_VERSION_CODE
			+ " INTEGER,"
			+ MY_GAMES_APK_SIZE
			+ " INTEGER,"
			+ MY_GAMES_APK_URL
			+ " TEXT,"
			+ MY_GAMES_FLAG
			+ " INTEGER,"
			+ MY_GAME_TYPE
			+ " INTEGER,"
			+ MY_GAME_ITEMTYPE
			+ " INTEGER," + MY_GAME_SIGNER_HASHCODE + " INTEGER " + ")";
}
