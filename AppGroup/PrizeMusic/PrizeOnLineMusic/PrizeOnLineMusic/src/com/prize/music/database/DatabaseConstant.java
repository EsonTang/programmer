package com.prize.music.database;

import com.prize.app.constants.Constants;

/**
 * 
 * @author huanglingjun
 *
 */
public class DatabaseConstant {

	public static final String TABLENAME_LIST = "list_table"; // 保存所有歌单信息的表名
																// ，所有歌单信息都保存在这个表里
	public static final String TABLENAME_LOVE = "songs_love_table"; // "我喜欢的"
																	// 的表名
	public static final String TABLENAME_HISTORY = "table_history"; // 播放历史表单名

	// =========================单首歌曲保存的信息=============================//
	public static final String MEIDA_TITLE = "song_title";
	public static final String MEIDA_ALBUM_NAME = "album_name";
	public static final String MEIDA_ALBUM_LOGO = "album_logo";
	public static final String AUDIO_ARTIST = "song_artist";
	public static final String SONG_BASE_ID = "song_base_id"; // 可以存放本地歌曲的AUDIO_ID
																// 和 网络歌曲的
																// song_id
	public static final String SONG_USER_ID = "song_user_id";
	public static final String SONG_SOURCE_TYPE = "song_source_type"; // 歌曲类型，是本地歌曲，还是
																		// 网络歌曲
	// ============================单首歌曲保存的信息===============================================//

	public static final String ID = "_id";
	public static final String AUDIO_ID = "audio_id";
	// 无用户登陆时候，默认的user_id
	public static final long DEFAULT_USER_ID = -1000;
	// ============================================================//
	public static final String ONLIEN_TYPE = "online_type"; // 表示是在线的歌曲/歌单
	public static final String LOCAL_TYPE = "local_type"; // 表示是本地的歌曲/歌单
	// ============================================================//

	// 首页添加列表表名
	// ====================================歌单的信息==================================================//
	/*** 该歌单的名字****/
	public static final String LIST_NAME = "list_name"; //
	/***  该歌单对应的表名.每个歌单一个表****/
	public static final String LIST_TABLE_NAME = "list_table_name"; //
	public static final String LIST_ID = "list_id"; // 该歌单对应的id
	/** 歌单类型，是本地歌单，还是在线歌单 ***/
	public static final String LIST_SOURCE_TYPE = "source_type"; //
	public static final String LIST_USER_ID = "list_user_id"; // 该歌单对应的用户的id
	/** 在线歌单类型，是Constants.KEY_COLLECT还是Constants.KEY_ALBUM ***/
	public static final String LIST_SOURCE_ONLINE_TYPE = "source_online_type"; // 
	public static final String LIST_MENUIAMGEURL = "list_menuiamgeurl";// logo
	// ====================================歌单的信息==================================================//

}
