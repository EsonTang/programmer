package com.prize.music.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prize.app.BaseApplication;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.util.JLog;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.history.HistoryColumns;

/**
 * 
 * @author lixing
 *
 */
public class SQLUtils {
	private static MusicDatabaseHelper mDataBaseHelper;
	private static SQLUtils instance;
	private final static String TAG = "SQLUtils";

	private SQLUtils(Context context) {
		super();
		mDataBaseHelper = MusicDatabaseHelper.getInstance(context);
	}

	public static synchronized SQLUtils getInstance(Context context) {
		if (instance == null) {
			instance = new SQLUtils(context);
		}
		return instance;
	}

	/**
	 * @author lixing
	 * @see 插入数据
	 * @param tab_name
	 * @param values
	 * @return boolean
	 */
	public boolean insert(String tab_name, ContentValues values) {
		long rowId = -1;
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
	
		try {
			rowId = db.insert(tab_name, null, values);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {

		
		}
		if (rowId != -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 开始事务
	 */
	public void beginTransaction() {
		mDataBaseHelper.beginTransaction();
	}

	/**
	 * 结束事务
	 */
	public void endTransaction() {
		mDataBaseHelper.endTransaction();
	}

	/**
	 * 结束事务
	 */
	public void setTransactionSuccessful() {
		mDataBaseHelper.setTransactionSuccessful();
	}

	// 插入
	public boolean insert(String tableName, Map<String, Object> map) {
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		values.put("title", map.get("title").toString());
		values.put("artist", map.get("artist").toString());
		values.put("base_id", Long.parseLong(map.get("base_id").toString()));
		// values.put("audio_id",
		// Integer.parseInt(map.get("audio_id").toString()));
		try {
			db = mDataBaseHelper.openDatabase();
			// db = mDataBaseHelper.getWritableDatabase();
			long raw = db.insert(tableName, null, values);
			return raw != -1;
		} finally {
		
		}
	}

	/**
	 * @see 根据条件删除数据
	 * @param tableName
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public boolean delete(String tableName, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase db = null;
		try {
			db = mDataBaseHelper.openDatabase();
			// db = mDataBaseHelper.getWritableDatabase();
			int count = db.delete(tableName, whereClause, whereArgs);
			return count > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	/**
	 * 
	 * @see 查询，返回整个表的cursor
	 * @param tableName
	 * @return Cursor,返回整个表的cursor
	 */
	public Cursor queryCursor(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		String sql_qury = "SELECT * FROM " + tableName + ";";
		Cursor cursor = db.rawQuery(sql_qury, null);
		return cursor;
	}

	/**
	 * 
	 * 查询某个歌单的信息（除了播放记录,并且会过滤删除系统数据库的歌曲）
	 * 
	 * @param tableName
	 * @return List<MusicInfo>
	 */
	public List<MusicInfo> query(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		String sql_qury = "SELECT * FROM " + tableName + ";";
		Cursor cursor_love = db.rawQuery(sql_qury, null);
		List<MusicInfo> lista = new ArrayList<MusicInfo>();
		List<MusicInfo> deleteList = new ArrayList<MusicInfo>();
		int index_title = cursor_love
				.getColumnIndex(DatabaseConstant.MEIDA_TITLE);
		int index_artist = cursor_love
				.getColumnIndex(DatabaseConstant.AUDIO_ARTIST);
		int index_base_id = cursor_love
				.getColumnIndex(DatabaseConstant.SONG_BASE_ID);
		int index_album_id = cursor_love
				.getColumnIndex(DatabaseConstant.MEIDA_ALBUM_NAME);
		int index_logo_id = cursor_love
				.getColumnIndex(DatabaseConstant.MEIDA_ALBUM_LOGO);
		int index_user_id = cursor_love
				.getColumnIndex(DatabaseConstant.SONG_USER_ID);
		int index_source_type = cursor_love
				.getColumnIndex(DatabaseConstant.SONG_SOURCE_TYPE);
		try {
			while (cursor_love.moveToNext()) {
				MusicInfo music_info = new MusicInfo();
				music_info.songName = cursor_love.getString(index_title);
				music_info.singer = cursor_love.getString(index_artist);
				music_info.songId = cursor_love.getLong(index_base_id);
				music_info.albumLogo = cursor_love.getString(index_logo_id);
				music_info.albumName = cursor_love.getString(index_album_id);
				music_info.userId = cursor_love.getString(index_user_id);
				music_info.source_type = cursor_love
						.getString(index_source_type);
				if (music_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
					if (MusicUtils.isSongInSysData(music_info.songId)) {
						lista.add(music_info);
					} else {
						deleteList.add(music_info);
					}
				} else {
					lista.add(music_info);
				}
			}
			MusicUtils.removeMultiFromMyCollect(BaseApplication.curContext,
					deleteList, null, tableName);

		} catch (Exception e) {
		} finally {
			if (cursor_love != null) {
				cursor_love.close();
			}
		}

		return lista;
	}

	/**
	 * 
	 * 查询播放记录
	 * 
	 * @param tableName
	 * @return List<MusicInfo>
	 */
	public List<MusicInfo> queryHistory(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		String sql_qury = "SELECT * FROM " + tableName + ";";
		Cursor cursor = db.rawQuery(sql_qury, null);
		List<MusicInfo> lista = new ArrayList<MusicInfo>();
		// modify by pengy for 18155 2016.07.05
		List<MusicInfo> deleteList = new ArrayList<MusicInfo>();
		int index_title = cursor.getColumnIndex(HistoryColumns.AUDIO_TITLE);
		int index_artist = cursor.getColumnIndex(HistoryColumns.AUDIO_ARTIST);
		int index_base_id = cursor
				.getColumnIndex(HistoryColumns.HISTORY_BASE_ID);
		int index_album_id = cursor.getColumnIndex(HistoryColumns.AUDIO_ALUBM);
		int index_imageurl = cursor.getColumnIndex(HistoryColumns.IMAGEURL);
		int index_source_type = cursor
				.getColumnIndex(HistoryColumns.HISTORY_SOURCE_TYPE);
		try {
			while (cursor.moveToNext()) {
				MusicInfo music_info = new MusicInfo();
				music_info.songName = cursor.getString(index_title);
				music_info.albumName = cursor.getString(index_album_id);
				music_info.albumLogo = cursor.getString(index_imageurl);
				music_info.singer = cursor.getString(index_artist);
				music_info.songId = cursor.getLong(index_base_id);
				music_info.userId = /* cursor.getString(index_user_id) */MusicUtils
						.getUserId();
				music_info.source_type = cursor.getString(index_source_type);
				// modify by pengy  for bug 18155 start 2016.07.05
//				lista.add(music_info);
				if (music_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
					if (MusicUtils.isSongInSysData(music_info.songId)) {
						lista.add(music_info);
					} else {
						deleteList.add(music_info);
					}
				} else {
					lista.add(music_info);
				}
			}
			MusicUtils.removeMultiFromMyCollect(BaseApplication.curContext,
					deleteList, null, tableName);
			// modify by pengy  for bug 18155 end 2016.07.05
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		Collections.reverse(lista);
		return lista;
	}

	public List<ListInfo> queryMenu() {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_LIST
				+ ";";
		Cursor mPlayListCursor = db.rawQuery(sql_qury, null);
		List<ListInfo> arraylist = new ArrayList<ListInfo>();
		if (mPlayListCursor != null && !mPlayListCursor.isClosed()
				&& mPlayListCursor.getCount() >= 0) {
			int index_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_NAME);
			int index_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_ID);
			int index_table_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_TABLE_NAME);
			int index_logo = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_MENUIAMGEURL);
			int index_source_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_TYPE);
			int index_user_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_USER_ID);
			int index_source_online_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE);
			while (mPlayListCursor.moveToNext()) {
				ListInfo info = new ListInfo();
				info.menuName = mPlayListCursor.getString(index_name);
				info.menuId = mPlayListCursor.getLong(index_id);
				info.list_table_name = mPlayListCursor
						.getString(index_table_name);
				info.menuImageUrl = mPlayListCursor
						.getString(index_logo);
				info.source_type = mPlayListCursor.getString(index_source_type);
				info.list_user_id = mPlayListCursor.getString(index_user_id);
				info.menuType = mPlayListCursor
						.getString(index_source_online_type);
				JLog.i(TAG, "info.menuType="+info.menuType+"---info.source_type="+info.source_type+"--info.menuIamgeUrl ="+info.menuImageUrl );
				arraylist.add(info);
			}
			if (mPlayListCursor != null) {
				mPlayListCursor.close();
			}
		
		}
		return arraylist;
	}

	// 创建新表对应首页新建列表
	public void createTable(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		if (tableName != null) {
			String sql_creat = "CREATE TABLE IF NOT EXISTS " + tableName
					+ " ( " + DatabaseConstant.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ DatabaseConstant.SONG_BASE_ID + " LONG,"
					+
					// DBContast.AUDIO_ID+" INTEGER,"+
					DatabaseConstant.MEIDA_TITLE + " VARCHAR(50) UNIQUE,"
					+ DatabaseConstant.AUDIO_ARTIST + " VARCHAR(20));";
			db.execSQL(sql_creat);
		}
		
	}

	// 移除首页新建列表
	public void removeListTable(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		if (tableName != null) {
			String sql_remove = "DROP TABLE IF EXISTS " + tableName + ";";
			db.execSQL(sql_remove);
		}
	
	}

	// 创建存放首页新建列表表名的表
	public void createListTable() {
		// String sql_remove
		// ="DROP TABLE IF EXISTS "+DBContast.TABLENAME_LIST+";";
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		// db.execSQL(sql_remove);
		String sql_creatNameTable = "CREATE TABLE IF NOT EXISTS "
				+ DatabaseConstant.TABLENAME_LIST + " ( " + DatabaseConstant.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
				// DBContast.AUDIO_ID+" INTEGER,"+
				DatabaseConstant.LIST_NAME + " VARCHAR(50) UNIQUE);";
		db.execSQL(sql_creatNameTable);
	

	}

	// 插入首页列表表名
	public boolean insertList(String name) {
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		values.put(DatabaseConstant.LIST_NAME, name);
		try {
			db = mDataBaseHelper.openDatabase();
			// db = mDataBaseHelper.getWritableDatabase();
			long raw = db.insert(DatabaseConstant.TABLENAME_LIST, null, values);
			return raw != -1;
		} finally {
			
		}
	}

	// 移除首页列表表名
	public void removeList(String name, SQLiteDatabase db) {
		// SQLiteDatabase db =null;
		String sql_removeList = "DELETE FROM "
				+ DatabaseConstant.TABLENAME_LIST + " WHERE "
				+ DatabaseConstant.LIST_NAME + "='" + name + "';";
		try {
			// db = data.getWritableDatabase();
			// db.delete(DBContast.TABLENAME_LIST, DBContast.LIST_NAME, new
			// String[]{name});
			db.execSQL(sql_removeList);
		} finally {
			if (db != null && db.isOpen()) {
				// db.close();
				// db = null;
			}
		}
	}

	// 查询首页列表表名
	public List<String> queryListName(String tableName) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		// Cursor cursor = db.query(distinct, tableName, columns, selection,
		// selectionArgs, groupBy, having, orderBy, limit);
		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_LIST;
		Cursor cursor = db.rawQuery(sql_qury, null);
		List<String> names = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndex(DatabaseConstant.LIST_NAME));
			names.add(name);
		}
		if (cursor != null) {
			cursor.close();
		}
	
		return names;
	}

	/**
	 * @see 查询 歌单名 是否存在
	 * @param list_name
	 * @return
	 */
	public boolean isPlayListNameExit(String list_name) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		// Cursor cursor = db.query(distinct, tableName, columns, selection,
		// selectionArgs, groupBy, having, orderBy, limit);
		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_LIST;
		Cursor cursor = db.rawQuery(sql_qury, null);
		boolean result = false;
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndex(DatabaseConstant.LIST_NAME));
			if (list_name.equals(name)) {
				result = true;
				break;
			}
		}
		if (cursor != null) {
			cursor.close();
		}
	
		return result;
	}

	/**
	 * @see 查询 表单单名 是否存在
	 * @param list_name
	 * @return
	 */
	public boolean isPlayListTableExit(String list_table_name) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		// Cursor cursor = db.query(distinct, tableName, columns, selection,
		// selectionArgs, groupBy, having, orderBy, limit);
		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_LIST;
		Cursor cursor = db.rawQuery(sql_qury, null);
		boolean result = false;
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndex(DatabaseConstant.LIST_TABLE_NAME));
			if (list_table_name.equals(name)) {
				result = true;
				break;
			}
		}
		if (cursor != null) {
			cursor.close();
		}
	
		return result;
	}

	/**
	 * @see 更改歌单名
	 * @param old_list_name
	 * @param new_list_name
	 * @return
	 */
	public boolean reNamePlayListName(String old_list_name, String new_list_name) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		// Cursor cursor = db.rawQuery("select * from " +
		// DatabaseConstant.TABLENAME_LIST + " where Name=?",
		// new String[]{old_list_name});

		db.execSQL("update " + DatabaseConstant.TABLENAME_LIST
				+ " set list_name=? where list_name=?", new String[] {
				new_list_name, old_list_name });
		// closeDatabase();
		return true;
	}

	/**
	 * 设置歌单的logo
	 * 
	 * @param menuIamgeUrl
	 *            url或者Bitmap保存到本地的绝对路径
	 * @param list_name
	 *            歌单名
	 * @return boolean
	 */
	public boolean updatePlayListInfo(String menuIamgeUrl, String list_name) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		db.execSQL("update " + DatabaseConstant.TABLENAME_LIST
				+ " set list_menuiamgeurl=? where list_table_name=?",
				new String[] { menuIamgeUrl, list_name });
		JLog.i(TAG, "updatePlayListInfo-menuIamgeUrl=" + menuIamgeUrl
				+ "--list_name=" + list_name);
		return true;
	}

	/**
	 * 
	 * 返回歌单封面地址
	 * @param list_name  歌单名
	 * @return String  歌单logo地址
	 */
	public String getPlayListLogoInfo(String list_name) {
		String menuIamgeUrl = null;
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		Cursor cursor = db.query(DatabaseConstant.TABLENAME_LIST,
				new String[] { DatabaseConstant.LIST_MENUIAMGEURL },
				DatabaseConstant.LIST_TABLE_NAME+"=?", new String[] { list_name },
				null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					menuIamgeUrl = cursor.getString(0);
				}
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		JLog.i(TAG, "getPlayListLogoInfo-menuIamgeUrl=" + menuIamgeUrl
				+ "--list_name=" + list_name);
		return menuIamgeUrl;
	}

	/**
	 * 
	 * 查询数据库是否已经添加
	 * 
	 * @param table_name
	 * @param songId
	 * @return boolean
	 */
	public boolean isAdded(String table_name, long songId) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		Cursor cursor = db.query(table_name, null,
				DatabaseConstant.SONG_BASE_ID + "=?",
				new String[] { String.valueOf(songId) }, null, null, null);
		if (cursor == null) {
			return false;
		}
		int count =cursor.getCount();
		cursor.close();
		return count>=1;
	}

	/**
	 * @see 建歌单的表单
	 * @author lixing
	 * @param table_name
	 */
	public void createSongsTable(String table_name) {
		SQLiteDatabase db = mDataBaseHelper.openDatabase();
		// SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		String songs_love_creat = "CREATE TABLE IF NOT EXISTS " + table_name
				+ " ( " + DatabaseConstant.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DatabaseConstant.SONG_USER_ID + " LONG,"
				+ DatabaseConstant.SONG_BASE_ID + " LONG NOT NULL,"
				+ DatabaseConstant.MEIDA_ALBUM_NAME + " VARCHAR(50),"
				+ DatabaseConstant.MEIDA_ALBUM_LOGO + " VARCHAR(100),"
				+ DatabaseConstant.SONG_SOURCE_TYPE + " VARCHAR(20),"
				+ DatabaseConstant.MEIDA_TITLE + " VARCHAR(50) UNIQUE,"
				+ DatabaseConstant.AUDIO_ARTIST + " VARCHAR(20));";
		db.execSQL(songs_love_creat);
		// closeDatabase();
		LogUtils.i(TAG, "songs_love table created");
	}

	// public void closeDatabase() {
	// if (mDataBaseHelper != null) {
	// mDataBaseHelper.closeDatabase();
	// }
	// }

	/**
	 * 查询表里是否存在添加的歌曲
	 * @param tableName
	 * @param songId
	 * @return
	 */
//	public boolean queryIsSongExsit(String tableName, long songId) {
//		SQLiteDatabase db = mDataBaseHelper.openDatabase();
//		Cursor cursor = db.query(tableName,
//				new String[] { DatabaseConstant.SONG_BASE_ID },
//				DatabaseConstant.SONG_BASE_ID + "=?",
//				new String[] { String.valueOf(songId) }, null, null, null);
//		if (cursor == null) {
//			return false;
//		}
//		int count =cursor.getCount();
//		cursor.close();
//		return count>=1;
//	}

}
