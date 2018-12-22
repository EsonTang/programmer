package com.prize.music.database;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.history.HistoryColumns;

/**
 * @see
 * @author lixing
 *
 */
public class MusicDatabaseHelper extends SQLiteOpenHelper {
	private final static String TAG = "MusicDatabaseHelper";
	private static final String DATABASE_NAME = "MusicDB.db";

	private static final int VERSION = 1;

	private static MusicDatabaseHelper instance;
	private static SQLiteDatabase database;
	
	
	private AtomicInteger mOpenCounter = new AtomicInteger();
	

	public MusicDatabaseHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO Auto-generated constructor stub
	}
	
	
	  public synchronized SQLiteDatabase openDatabase() {
	       if(mOpenCounter.incrementAndGet() == 1) {
	           // Opening new database
	    	   database = instance.getWritableDatabase();
	       }
	       return database;
	   }
	  
	  public synchronized void closeDatabase() {
		  JLog.i(TAG, "mOpenCounter.decrementAndGet()="+mOpenCounter.decrementAndGet());
	       if(mOpenCounter.decrementAndGet() == 0) {
	           // Closing database
	    	   database.close();
	 
	       }
	   }
	

	private MusicDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	/**
	 * @param context
	 * @return
	 */
	public static MusicDatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new MusicDatabaseHelper(context);
		}
		return instance;
	}

	/**
	 * @see 建收藏歌曲的表单
	 * @author lixing
	 * @param db
	 */
	private void createLoveSongsTab(SQLiteDatabase db, String table_name){
		String songs_love_creat = "CREATE TABLE IF NOT EXISTS "
				+ table_name + " ( " 
				+ DatabaseConstant.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ DatabaseConstant.SONG_USER_ID + " VARCHAR(20),"
				+ DatabaseConstant.SONG_BASE_ID + " LONG NOT NULL,"
				+ DatabaseConstant.SONG_SOURCE_TYPE + " VARCHAR(20),"
				+ DatabaseConstant.MEIDA_ALBUM_NAME + " VARCHAR(50),"
				+ DatabaseConstant.MEIDA_ALBUM_LOGO + " VARCHAR(100),"
				+ DatabaseConstant.MEIDA_TITLE + " VARCHAR(50),"
				+ DatabaseConstant.AUDIO_ARTIST + " VARCHAR(20));";
		db.execSQL(songs_love_creat);
		LogUtils.i(TAG, "songs_love table created");
	}

	/**
	 * @see 创建歌曲播放历史表单
	 * @author lixing
	 * @param db
	 */
	private void createHistorySongsTab(SQLiteDatabase db){
		String history_create = "CREATE TABLE IF NOT EXISTS "
				+ DatabaseConstant.TABLENAME_HISTORY + " ( " + HistoryColumns.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HistoryColumns.HISTORY_BASE_ID + " INTEGER NOT NULL,"
				+ HistoryColumns.HISTORY_USER_ID + " VARCHAR(20),"
				+ HistoryColumns.AUDIO_ALUBM + " VARCHAR(20), "
				+ HistoryColumns.HISTORY_SOURCE_TYPE + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_ARTIST + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_DURATION + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_TITLE + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_YEAR + " LONG, " + HistoryColumns.URL
				+ " VARCHAR(50), " + HistoryColumns.IMAGEURL + " VARCHAR(50), "
				+ HistoryColumns.TIMESTAMP + " LONG); ";
		db.execSQL(history_create);
		LogUtils.i(TAG, "HistorySongs table created");
	}

	/**
	 * @see 建保存歌单信息的表单
	 * @author lixing
	 * @param db
	 */
	private void createListTab(SQLiteDatabase db) {
		String songs_love_creat = "CREATE TABLE IF NOT EXISTS "
				+ DatabaseConstant.TABLENAME_LIST + " ( " + DatabaseConstant.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DatabaseConstant.LIST_NAME + " VARCHAR(50) UNIQUE,"
				+ DatabaseConstant.LIST_TABLE_NAME + " VARCHAR(50),"
				+ DatabaseConstant.LIST_MENUIAMGEURL + " VARCHAR(80),"
				+ DatabaseConstant.LIST_ID + " LONG,"
				+ DatabaseConstant.LIST_SOURCE_TYPE + " VARCHAR(20),"
				+ DatabaseConstant.LIST_SOURCE_ONLINE_TYPE + " VARCHAR(20),"
				+ DatabaseConstant.LIST_USER_ID + " VARCHAR(20));";
		db.execSQL(songs_love_creat);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createLoveSongsTab(db, DatabaseConstant.TABLENAME_LOVE);
		createHistorySongsTab(db);
		createListTab(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public static SQLiteDatabase getDatabase() {
		if (null == database) {
			initPrizeSQLiteDatabase();
		}
		return database;
	}

	public  void beginTransaction() {
		try {
			getDatabase().beginTransaction();
		} catch (Exception e) {
		}
	}

	public  void endTransaction() {
		try {
			getDatabase().endTransaction();
		} catch (Exception e) {
		}

	}
	public  void setTransactionSuccessful() {
		try {
			getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
		}

	}
	public static synchronized void initPrizeSQLiteDatabase() {
		if (null == instance) {
			try {
				instance = new MusicDatabaseHelper(BaseApplication.curContext);
				database = instance.getWritableDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
