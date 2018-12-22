package com.prize.app.constants;

import java.io.File;

import android.os.Environment;

public class Constants {

	// 现网地址
	public static final String GIS_URL = "http://music.szprize.cn/music";
	//public static final String GIS_URL = "http://192.168.1.148:8080/music";

	public static final String SYSTEM_UPGRADE_URL = GIS_URL + "/upgrade/check"; // 系统版自升级url

	// 下载模块的常量
	public static final String QES_ACCEPT_CONTENT_TYPE = "application/octet-stream,application/vnd.android.package-archive";
	public static final String QES_UNACCEPT_CONTENT_TYPE = "text/html,text/plain";
	public static final String ANDROID_APP_SUFFIX = ".apk";
	public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
	public static final String PRIZEAPPCENTER = "PrizeMusic";
	public static final int PAGE_SIZE = 20;
	public static String DOWNLOAD_FOLDER_NAME = "download";
	public static final String DOWNLOAD_FILE_NAME = PRIZEAPPCENTER
			+ ANDROID_APP_SUFFIX;
	public static final String DOWNLOAD_TEMP_FILE_NAME = PRIZEAPPCENTER
			+ PRIZE_TEM_FILE_SUFFIX;
	public static final String APKFILEPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(DOWNLOAD_FILE_NAME).toString();
	public static final String APKFILETEMPPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(DOWNLOAD_TEMP_FILE_NAME).toString();

	public static final String KEY_COLLECT = "collect";
	public static final String KEY_ALBUM = "album";
	public static final String KEY_SONGS = "song";
	public static final String KEY_RANK = "rank"; //排行榜
	public static final String KEY_RADIO = "radio";
	public static final String LOGO = "logo";
	public static final String TITLE = "title";
	public static final String KEY = "key";
	public static final String TYPE = "type";
	public static final String INTENTTRANSBEAN = "intenttransbean";
	public static final String APP_MD5 = "appMD5";
	public static final String KEY_NAME_DOWNLOAD_ID = "key_name_download_id";
	public static final String KEY_RADIO_GUESS_YOU_LIKE = "key_radio_guess_you_like";
	public static final String KEY_RADIO_SCENE = "key_radio_scene";
	public static final String REFLUSH_BROADCAST = "reflush_broadcast";
	public static final String WHERE = "where";
	public static final String REFLUSH_SONGS_BROADCAST = "reflush_songs_broadcast";

	// 本地音乐的常量
	public final static String ACTION_FR_2_FR_SURE = "action_fr_to_fr_sure";
	public final static String ACTION_CANCEL_FR_TO_FR = "action_cancel_fr_to_fr";
	public final static String ACTION_CANCE = "action_cancel";
	public final static String ACTION_SORT = "action_sort";
	public final static String ACTION_BELL = "action_bell";
	public final static String ACTION_DELETE = "action_delete";
	public final static String ACTION_ADD = "action_add";

	// Last.fm API
	public final static String LASTFM_API_KEY = "0bec3f7ec1f914d7c960c12a916c8fb3";

	// SharedPreferences
	public final static String APOLLO = "Apollo",
			APOLLO_PREFERENCES = "apollopreferences", ARTIST_KEY = "artist",
			ALBUM_KEY = "album", ALBUM_ID_KEY = "albumid",
			NUMALBUMS = "num_albums", GENRE_KEY = "genres",
			ARTIST_ID = "artistid", NUMWEEKS = "numweeks",
			BUILD_DEPENDS = "build_depends", PLAYLIST_NAME_FAVORITES = "我喜欢的",
			PLAYLIST_NAME = "playlist", WIDGET_STYLE = "widget_type",
			VISUALIZATION_TYPE = "visualization_type",
			DELETE_CACHE = "delete_cache", BUILD_VERSION = "build_version",
			UP_STARTS_ALBUM_ACTIVITY = "upStartsAlbumActivity",
			TABS_ENABLED = "tabs_enabled";

	// Image Loading Constants
	public final static String TYPE_ARTIST = "artist", TYPE_ALBUM = "album",
			TYPE_GENRE = "genre", TYPE_SONG = "song",
			TYPE_PLAYLIST = "playlist", ALBUM_SUFFIX = "albartimg",
			ARTIST_SUFFIX = "artstimg", PLAYLIST_SUFFIX = "plylstimg",
			GENRE_SUFFIX = "gnreimg", SRC_FIRST_AVAILABLE = "first_avail",
			SRC_LASTFM = "last_fm", SRC_FILE = "from_file",
			SRC_GALLERY = "from_gallery", SIZE_NORMAL = "normal",
			SIZE_THUMB = "thumb", SIZE_MEDIU = "mediu";

	// Bundle & Intent type
	public final static String MIME_TYPE = "mimetype",
			INTENT_ACTION = "action", DATA_SCHEME = "file";

	// Storage Volume
	public final static String EXTERNAL = "external";

	// Playlists
	public final static long PLAYLIST_UNKNOWN = -1, PLAYLIST_ALL_SONGS = -2,
			PLAYLIST_QUEUE = -3, PLAYLIST_NEW = -4, PLAYLIST_FAVORITES = -5,
			PLAYLIST_RECENTLY_ADDED = -6;

	// Genres
	public final static String[] GENRES_DB = { "Blues", "Classic Rock",
			"Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz",
			"Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap",
			"Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
			"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
			"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
			"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
			"Noise", "AlternRock", "Bass", "Soul", "Punk", "Space",
			"Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic",
			"Gothic", "Darkwave", "Techno-Industrial", "Electronic",
			"Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy",
			"Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
			"Native American", "Cabaret", "New Wave", "Psychedelic", "Rave",
			"Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk",
			"Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
			"Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing",
			"Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass",
			"Avantgarde", "Gothic Rock", "Progressive Rock",
			"Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band",
			"Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
			"Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
			"Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam",
			"Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad",
			"Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo",
			"A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass",
			"Club-House", "Hardcore", "Terror", "Indie", "Britpop",
			"Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
			"Heavy Metal", "Black Metal", "Crossover",
			"Contemporary Christian", "Christian Rock ", "Merengue", "Salsa",
			"Thrash Metal", "Anime", "JPop", "Synthpop" };

	// Theme item type
	public final static int THEME_ITEM_BACKGROUND = 0,
			THEME_ITEM_FOREGROUND = 1;

	public final static String INTENT_ADD_TO_PLAYLIST = "com.andrew.apolloMod.ADD_TO_PLAYLIST",
			INTENT_PLAYLIST_LIST = "playlistlist",
			INTENT_CREATE_PLAYLIST = "com.andrew.apolloMod.CREATE_PLAYLIST",
			INTENT_RENAME_PLAYLIST = "com.andrew.apolloMod.RENAME_PLAYLIST",
			INTENT_KEY_RENAME = "rename",
			INTENT_KEY_DEFAULT_NAME = "default_name";

	private Constants() {
		throw new UnsupportedOperationException();
	}

	// public final static String LYRIC_SAVE_PATH = StorageManagerEx
	// .getInternalStoragePath() + File.separator + "prizemusic" + "/lrc";

	public final static String LYRIC_SAVE_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "prizemusic"
			+ "/lrc";
	public final static String SONG_SAVE_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "prizemusic"
			+ "/song";

	public static final String TABLE_PERSON_PATH = "content://com.prize.music.provider/table_person";
	public static final String TABLE_ACCOUNT_PATH = "content://com.prize.music.provider/table_account";
	public static final String SWITCH_KEY = "switch_key";
	public static final String SWITCH_VALUE_ON = "ON";
	
	
	public final static String SORT_COLLECT_LOGO_SAVE_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "prizemusic"
			+ "/sortCollectLogo";
	
}
