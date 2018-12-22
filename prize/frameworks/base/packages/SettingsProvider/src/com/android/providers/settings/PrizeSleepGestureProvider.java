package com.android.providers.settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.util.Log;

public class PrizeSleepGestureProvider extends ContentProvider {
	private SQLiteOpenHelper mOpenHelper;
	private final static UriMatcher sMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private final static String AUTHORITY = "com.prize.sleepgesture";
	private final static String SLEEP_GESTURE = "sleepgesture";
	
	public final static Uri SLEEP_GESTURE_URI =  Uri.parse("content://com.prize.sleepgesture/sleepgesture"); 
	
	private static final int MATCH_SLEEP_GESTURE = 0;
	private static final int MATCH_SLEEP_GESTURE_ID = 1;

	static {
		sMatcher.addURI(AUTHORITY, SLEEP_GESTURE, MATCH_SLEEP_GESTURE);
		sMatcher.addURI(AUTHORITY, SLEEP_GESTURE + "/#", MATCH_SLEEP_GESTURE_ID);
	}
	
	
	public static class SleepGestureColumns{
		public final static String ID = "id";
		public final static String ITEM = "item";
		public final static String NAME = "name";
		public final static String PACKAGE_NAME = "packagename";
		public final static String CLASS_NAME = "classname";
		public final static String SUM = "sum";
		public final static String ONOFF = "onoff";
	}
	

	private static class DatabaseHelper extends SQLiteOpenHelper {
		Context mContext;
		private final static String SLEEP_GESTURE_NAME = "sleepgesture.db";

		public DatabaseHelper(Context context) {
			super(context, SLEEP_GESTURE_NAME, null, 1);
			mContext = context;
		}

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.e("liup", "SQLiteDatabase onCreate");
			db.execSQL("create table " + SLEEP_GESTURE + " (id INTEGER PRIMARY KEY," +
					"item TEXT UNIQUE ON CONFLICT REPLACE," +
					"name TEXT UNIQUE ON CONFLICT REPLACE," +
					"packagename TEXT," +
					"classname TEXT," +
					"sum TEXT," +
					"onoff INTEGER)");
			loadDefaultSleepGesture(db);
			Log.e("liup", "SQLiteDatabase onCreate finish");
		}

		private void loadDefaultSleepGesture(SQLiteDatabase db) {
			SQLiteStatement stmt = null;
			try {
				stmt = db.compileStatement("INSERT OR IGNORE INTO " 
						+ SLEEP_GESTURE + "(item,name,packagename,classname,sum,onoff)"
						+ " VALUES(?,?,?,?,?,?);");
				String[] default_lsit = mContext.getResources().getStringArray(
						R.array.default_sleep_gesture_array);
				for (String string : default_lsit) {
					loadSleepGesture(stmt,string);
				}

			} finally {
				if (stmt != null) stmt.close();
			}
		}
		
		private void loadSleepGesture(SQLiteStatement stmt,
				String defiaultString) {
			String[] temp = defiaultString.split(",");

			for (int i = 0; i < temp.length; i++) {
				if (i == temp.length - 1) {
					stmt.bindLong(i+1, Integer.valueOf(temp[i]));
				} else {
					stmt.bindString(i+1, temp[i]);
				}
			}
			stmt.execute();
		}
		
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long id;
		String where;
		//Log.e("liup", "sMatcher.match(uri) = " + sMatcher.match(uri));
		switch (sMatcher.match(uri)) {
		case MATCH_SLEEP_GESTURE:
			return db.query(SLEEP_GESTURE, projection, selection,
					selectionArgs, null, null, sortOrder);
		case MATCH_SLEEP_GESTURE_ID:
			id = ContentUris.parseId(uri);
			where = "id" + "=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;
			}
			return db.query(SLEEP_GESTURE, projection, where, selectionArgs,
					null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown Uri : " + uri);
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long id;
		String where;
		switch (sMatcher.match(uri)) {
		case MATCH_SLEEP_GESTURE:
			return db.update(SLEEP_GESTURE, values, selection, selectionArgs);
		case MATCH_SLEEP_GESTURE_ID:
			id = ContentUris.parseId(uri);
			where = "id" + "=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;
			}
			return db.update(SLEEP_GESTURE, values, where, selectionArgs);
		default:
			throw new IllegalArgumentException("Unknown Uri : " + uri);
		}
		
	}
}
