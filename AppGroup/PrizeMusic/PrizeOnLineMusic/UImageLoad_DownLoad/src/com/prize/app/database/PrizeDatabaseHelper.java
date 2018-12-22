/**
 *  
 */
package com.prize.app.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.app.threads.SQLSingleThreadExcutor;

/**
 **
 * 数据库操作类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class PrizeDatabaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "DownloadManager.db";// 数据库名称
	private static final int DB_VERSION = 2;// 数据库版本
	private static SQLiteDatabase database;
	private static PrizeDatabaseHelper dbHelper;

	public interface DatabaseInsertResult {
		void insertResult(long result);
	};

	public interface DatabaseDeleteResult {
		void deleteResult(int result);
	};

	/***
	 * SQL语句
	 */
	public static final String SQL_DELETE_TABLE = "drop table if exists ";
	public static final String SQL_CREATE_TABLE = "create table if not exists ";

	/**
	 * SQLiteOpenHelper
	 * 
	 */
	public PrizeDatabaseHelper() {
		super(BaseApplication.curContext, DB_NAME, null, DB_VERSION);
	}

	public static synchronized void initPrizeSQLiteDatabase() {
		if (null == dbHelper) {
			try {
				dbHelper = new PrizeDatabaseHelper();
				database = dbHelper.getWritableDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void releasePrizeDataBase() {
		if (null != database) {
			database.close();
			database = null;
		}

		if (null != dbHelper) {
			dbHelper.close();
			dbHelper = null;
		}
	}

	public static SQLiteDatabase getDatabase() {
		if (null == database) {
			initPrizeSQLiteDatabase();
		}
		return database;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DownloadGameTable.SQL_CREATE_GAME_TABLE);
		db.execSQL(HomeTable.SQL_CREATE_HOME_TABLE);
		db.execSQL(SearchHistory.SQL_CREATE_GAME_TABLE);
		db.execSQL(PersonTable.SQL_CREATE_PERSON_TABLE);
		db.execSQL(AccountTable.SQL_CREATE_ACCOUNT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		clearDB(db);
	}

	// public void onDowngrade(SQLiteDatabase db, int oldVersion, int
	// newVersion) {
	// Locale locale = Locale.getDefault();
	// if (Locale.CHINA.getCountry().equals(locale.getCountry())) {
	// Toast.makeText(BaseApplication.curContext,
	// "你进行了降级操作,为确保您的正常使用,请升级新版本.", Toast.LENGTH_LONG).show();
	// } else {
	// Toast.makeText(
	// BaseApplication.curContext,
	// "You degraded operation, to ensure that you properly, please upgrade to the new version.",
	// Toast.LENGTH_LONG).show();
	// }
	//
	// clearDB(db);
	// }

	/**
	 * 清空数据表 并重新创建
	 * 
	 * @param db
	 * @return void
	 * @see
	 */
	private static void clearDB(SQLiteDatabase db) {
		// 数据库降级，清空数据库，确保能启动

		db.execSQL(DownloadGameTable.SQL_DELETE_GAME_TABLE);
		db.execSQL(DownloadGameTable.SQL_CREATE_GAME_TABLE);

		db.execSQL(HomeTable.SQL_DELETE_HOME_TABLE);
		db.execSQL(HomeTable.SQL_CREATE_HOME_TABLE);

		db.execSQL(SearchHistory.SQL_DELETE_TABLE);
		db.execSQL(SearchHistory.SQL_CREATE_GAME_TABLE);

		db.execSQL(PersonTable.SQL_DELETE_PERSON_TABLE);
		db.execSQL(PersonTable.SQL_CREATE_PERSON_TABLE);

		db.execSQL(AccountTable.SQL_DELETE_ACCOUNT_TABLE);
		db.execSQL(AccountTable.SQL_CREATE_ACCOUNT_TABLE);

		/*
		 * // 更新列表 db.execSQL(UpdateAppTable.SQL_DELETE_GAME_TABLE);
		 * db.execSQL(UpdateAppTable.SQL_CREATE_GAME_TABLE);
		 * 
		 * // 收藏列表 db.execSQL(UserCollectionTable.SQL_DELETE_GAME_TABLE);
		 * db.execSQL(UserCollectionTable.SQL_CREATE_GAME_TABLE);
		 */
	}

	/**
	 * 执行SQL语句
	 * 
	 * @param sql
	 */
	public static void executeSQL(final String sql, final Object[] bindArgs) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					if (null != bindArgs) {
						getDatabase().execSQL(sql, bindArgs);
					} else {
						getDatabase().execSQL(sql);
					}
				} catch (Exception e) {
				}
			}
		});
	}

	public static void update(final String table, final ContentValues values,
			final String whereClause, final String[] whereArgs) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					getDatabase().update(table, values, whereClause, whereArgs);
				} catch (Exception e) {
				}

			}
		});
	}

	public static void insert(final String table, final String nullColumnHack,
			final ContentValues values, final DatabaseInsertResult resultCB) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					long result = getDatabase().insert(table, nullColumnHack,
							values);
					resultCB.insertResult(result);
				} catch (Exception e) {
				}
			}
		});
	}

	public static void insert(final String table, final String nullColumnHack,
			final ContentValues values) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					long id = getDatabase().insert(table, nullColumnHack,
							values);
				} catch (Exception e) {
					return;
				}
			}
		});
	}

	public static void delete(final String table, final String whereClause,
			final String[] whereArgs) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					getDatabase().delete(table, whereClause, whereArgs);
				} catch (Exception e) {
					return;
				}

			}
		});
	}

	public static boolean deleteGame(final String table,
			final String whereClause, final String[] whereArgs) {
		int a = getDatabase().delete(table, whereClause, whereArgs);
		return a > 0;
	}

	/**
	 * 方法描述：删除列表里面的数据返回状态
	 */
	public static int deleteCollection(final String table,
			final String whereClause, final String[] whereArgs) {
		int state = getDatabase().delete(table, whereClause, whereArgs);
		return state;
	}

	/**
	 * 方法描述：替换列表数据
	 * 
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static void replaceAll(final String table,
			final ArrayList<ContentValues> values) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				beginTransaction();
				try {
					for (ContentValues contentValues : values) {
						getDatabase().replace(table, null, contentValues);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				setTransactionSuccessful();
				endTransaction();
			}
		});
	}

	/**
	 * 方法描述：替换列表数据
	 * 
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static void replace(final String table,
			final ContentValues contentValues) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try {
					getDatabase().replace(table, null, contentValues);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 方法描述：清空列表中的所有数据
	 */
	public static void deleteAll(final String table) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					String sql_delete_all = "TRUNCATE TABLE " + table;
					getDatabase().execSQL(sql_delete_all);
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * 方法描述：清空列表中的所有数据
	 */
	public static void deleteAllData(final String table) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					String sql_delete_all = "DELETE FROM " + table + ";";
					getDatabase().execSQL(sql_delete_all);
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * 查询操作可以多线程
	 */
	public static Cursor query(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		try {
			return getDatabase().query(table, columns, selection,
					selectionArgs, groupBy, having, orderBy);
		} catch (Exception e) {
			try {
				// 如果数据查询操作异常，将清除所有数据，重新创建数据库
				clearDB(database);
			} catch (Exception e2) {
			}
			return null;
		}
	}

	public static void beginTransaction() {
		try {
			getDatabase().beginTransaction();
		} catch (Exception e) {
		}
	}

	public static void endTransaction() {
		try {
			getDatabase().endTransaction();
		} catch (Exception e) {
		}

	}

	public static void setTransactionSuccessful() {
		try {
			getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
		}

	}

	public static Cursor rawQuery(String sql, String[] selectionArgs) {
		return getDatabase().rawQuery(sql, selectionArgs);
	}

//	/***
//	 * 插入或更新已安装应用表<br>
//	 * 只针对 InstalledAppTable表操作
//	 * 
//	 * @param cv
//	 * @return 0: fail, else: success
//	 */
//	public static long updateInstalledTable(ContentValues cv) {
//		SQLiteDatabase db = getDatabase();
//		return updateInstalledTable(cv, db);
//	}
//
//	/***
//	 * 插入或更新已安装应用表<br>
//	 * 只针对 InstalledAppTable表操作
//	 * 
//	 * @param cv
//	 * @return 0: fail, else: success
//	 */
//	private static long updateInstalledTable(ContentValues cv, SQLiteDatabase db) {
//
//		String pkg = cv.getAsString(InstalledAppTable.PKG_NAME);
//		if (TextUtils.isEmpty(pkg))
//			return 0;
//
//		String[] args = new String[] { pkg };
//		Cursor c = db.query(InstalledAppTable.TABLE_NAME, null,
//				InstalledAppTable.PKG_NAME + "=?", args, null, null, null);
//
//		if (null == c || c.getCount() < 1) {
//			if (c != null)
//				c.close();
//			return db.insert(InstalledAppTable.TABLE_NAME, null, cv);
//		} else if (c.getCount() > 0) {
//			c.close();
//			return db.update(InstalledAppTable.TABLE_NAME, cv,
//					InstalledAppTable.PKG_NAME + "=?", args);
//		}
//		return 0;
//	}
//
//	/***
//	 * 批量插入已安装应用的数据
//	 * 
//	 * @param datas
//	 *            数据
//	 * @return 0 : fail, -1: no data, 1:success
//	 */
//	public static int batchInsert(List<ContentValues> datas) {
//		if (null == datas || datas.size() < 1)
//			return -1;
//		SQLiteDatabase db = getDatabase();
//		try {
//			db.beginTransaction();
//			int sz = datas.size();
//			for (int i = 0; i < sz; i++) {
//				ContentValues cv = datas.get(i);
//				updateInstalledTable(cv, db);
//			}
//			db.setTransactionSuccessful();
//			db.endTransaction();
//			return 1;
//		} catch (Exception e) {
//			e.printStackTrace();
//			db.endTransaction();
//			return 0;
//		}
//	}
}
