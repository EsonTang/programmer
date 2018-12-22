package com.prize.music.database;

import com.prize.music.history.HistoryColumns;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author huanglingjun
 *
 */
public class DBUtils extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "MusicDB.db";

	private static final int VERSION = 1;

	private static DBUtils instance;

	public DBUtils(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO Auto-generated constructor stub
	}

	private DBUtils(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	/**
	 * @param context
	 * @return
	 */
	public static DBUtils getInstance(Context context) {
		if (instance == null) {
			instance = new DBUtils(context);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql_creat = "CREATE TABLE IF NOT EXISTS "
				+ DBContast.TABLENAME_LOVE + " ( " + DBContast.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + DBContast.BASE_ID
				+ " LONG,"
				+
				// DBContast.AUDIO_ID+" INTEGER,"+
				DBContast.MEIDA_TITLE + " VARCHAR(50) UNIQUE,"
				+ DBContast.AUDIO_ARTIST + " VARCHAR(20));";
		String history_create = "CREATE TABLE IF NOT EXISTS "
				+ HistoryColumns.TABLE_NAME + " ( " + HistoryColumns.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HistoryColumns.AUDIO_ID + " INTEGER NOT NULL,"
				+ HistoryColumns.AUDIO_ALUBM + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_ARTIST + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_DURATION + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_TITLE + " VARCHAR(20), "
				+ HistoryColumns.AUDIO_YEAR + " LONG, " + HistoryColumns.URL
				+ " VARCHAR(50), " + HistoryColumns.IMAGEURL + " VARCHAR(50), "
				+ HistoryColumns.TIMESTAMP + " LONG); ";
		db.execSQL(sql_creat);
		db.execSQL(history_create);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
