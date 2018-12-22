package com.prize.music.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author huanglingjun
 *
 */
public class SQLUtils {
	private DBUtils data;
	private static SQLUtils instance;

	private SQLUtils(Context context) {
		super();
		data = DBUtils.getInstance(context);
	}

	public static SQLUtils getInstance(Context context) {
		if (instance == null) {
			instance = new SQLUtils(context);
		}
		return instance;
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
			db = data.getWritableDatabase();
			long raw = db.insert(tableName, null, values);
			return raw != -1;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	// 删除
	public boolean delete(String tableName, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase db = null;
		try {
			db = data.getWritableDatabase();
			int count = db.delete(tableName, whereClause, whereArgs);
			return count > 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}

	}

	// 查询
	public Cursor queryWithCursor(String tableName) {
		SQLiteDatabase db = data.getWritableDatabase();
		// Cursor cursor = db.query(distinct, tableName, columns, selection,
		// selectionArgs, groupBy, having, orderBy, limit);

		// Cursor cursor=db.rawQuery(sql_qury, null);
		/*
		 * boolean exits = false; String sql =
		 * "select * from sqlite_master where name="+"'"+tableName+"'"; Cursor
		 * cursor = db.rawQuery(sql, null); if(cursor.getCount()!=0){ String
		 * sql_qury="SELECT * FROM "+tableName+";"; cursor=db.rawQuery(sql_qury,
		 * null); }
		 */
		String sql_qury = "SELECT * FROM " + tableName + ";";
		Cursor cursor = db.rawQuery(sql_qury, null);
		return cursor;
	}

	// 创建新表对应首页新建列表
	public void createTable(String tableName) {
		SQLiteDatabase db = data.getWritableDatabase();
		if (tableName != null) {
			String sql_creat = "CREATE TABLE IF NOT EXISTS " + tableName
					+ " ( " + DBContast.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + DBContast.BASE_ID
					+ " LONG,"
					+
					// DBContast.AUDIO_ID+" INTEGER,"+
					DBContast.MEIDA_TITLE + " VARCHAR(50) UNIQUE,"
					+ DBContast.AUDIO_ARTIST + " VARCHAR(20));";
			db.execSQL(sql_creat);
		}
	}

	// 移除首页新建列表
	public void removeListTable(String tableName, SQLiteDatabase db) {
		// SQLiteDatabase db = data.getWritableDatabase();
		if (tableName != null) {
			String sql_remove = "DROP TABLE IF EXISTS " + tableName + ";";
			db.execSQL(sql_remove);
		}
	}

	// 创建存放首页新建列表表名的表
	public void createListTable() {
		// String sql_remove
		// ="DROP TABLE IF EXISTS "+DBContast.TABLENAME_LIST+";";
		SQLiteDatabase db = data.getWritableDatabase();
		// db.execSQL(sql_remove);
		String sql_creatNameTable = "CREATE TABLE IF NOT EXISTS "
				+ DBContast.TABLENAME_LIST + " ( " + DBContast.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
				// DBContast.AUDIO_ID+" INTEGER,"+
				DBContast.LIST_NAME + " VARCHAR(50) UNIQUE);";
		db.execSQL(sql_creatNameTable);
	}

	// 插入首页列表表名
	public boolean insertList(String name) {
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		values.put(DBContast.LIST_NAME, name);
		try {
			db = data.getWritableDatabase();
			long raw = db.insert(DBContast.TABLENAME_LIST, null, values);
			return raw != -1;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	// 移除首页列表表名
	public void removeList(String name, SQLiteDatabase db) {
		// SQLiteDatabase db =null;
		String sql_removeList = "DELETE FROM " + DBContast.TABLENAME_LIST
				+ " WHERE " + DBContast.LIST_NAME + "='" + name + "';";
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
		SQLiteDatabase db = data.getWritableDatabase();
		// Cursor cursor = db.query(distinct, tableName, columns, selection,
		// selectionArgs, groupBy, having, orderBy, limit);
		String sql_qury = "SELECT * FROM " + tableName;
		Cursor cursor = db.rawQuery(sql_qury, null);
		List<String> names = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndex(DBContast.LIST_NAME));
			names.add(name);
		}
		return names;
	}
}
