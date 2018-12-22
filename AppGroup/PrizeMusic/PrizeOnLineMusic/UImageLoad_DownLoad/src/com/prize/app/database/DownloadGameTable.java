package com.prize.app.database;

import android.provider.BaseColumns;

/**
 ** 下载任务
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DownloadGameTable {

	/* download game info 表名 */
	public static final String TABLE_NAME_GAME = "table_game";

	public static final String ID = BaseColumns._ID;
	public static final String SONGS_QUALITY = "songs_quality";
	/**** 歌曲id ****/
	public static final String SONG_ID = "song_id";
	public static final String SONG_SINGER = "song_singer";
	public static final String GAME_NAME = "game_name";
	public static final String SONG_ALBUM_NAME = "song_album_name";
	public static final String SONG_ALBUM_ID = "song_album_id";
	public static final String GAME_ICON_URL = "icon_url"; // 游戏显示小图标
	public static final String GAME_VERSION_CODE = "version_code";
	public static final String GAME_APK_SIZE = "apk_size";
	public static final String SONG_DOWN_URL = "song_down_url";
	public static final String GAME_DOWNLOAD_STATE = "download_state";
	public static final String GAME_LOADED_SIZE = "loaded_size"; // 已经下载的大小
	public static final String GAME_TYPE = "game_type";
	public static final String GAME_LOAD_FLAG = "load_flag"; // 游戏的标记
	/**** 时间戳 ****/
	public static final String GAME_TIME_STAP = "time_stap "; // 时间戳

	public static final String SQL_DELETE_GAME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_GAME;

	public static final String SQL_CREATE_GAME_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_GAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ SONGS_QUALITY
			+ " TEXT,"
			+ SONG_ID
			+ " TEXT UNIQUE,"
			+ SONG_SINGER
			+ " TEXT,"
			+ GAME_NAME
			+ " TEXT,"
			+ GAME_ICON_URL
			+ " TEXT,"
			+ GAME_VERSION_CODE
			+ " INTEGER,"
			+ GAME_APK_SIZE
			+ " INTEGER,"
			+ SONG_DOWN_URL
			+ " TEXT,"
			+ GAME_DOWNLOAD_STATE
			+ " INTEGER,"
			+ GAME_LOADED_SIZE
			+ " INTEGER,"
			+ GAME_TYPE
			+ " INTEGER,"
			+ GAME_LOAD_FLAG
			+ " INTEGER,"
			+ GAME_TIME_STAP
			+ " INTEGER,"
			+ SONG_ALBUM_NAME
			+ " TEXT,"
			+ SONG_ALBUM_ID
			+ " INTEGER"
			+")";
//	+ " INTEGER," + GAME_TIME_STAP + " INTEGER" + ")";
}
