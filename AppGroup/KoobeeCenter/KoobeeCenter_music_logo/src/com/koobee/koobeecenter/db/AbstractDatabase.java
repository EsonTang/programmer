package com.koobee.koobeecenter.db;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by yiyi.
 */
public abstract class AbstractDatabase {
	private int mDatabaseVersion;
	private SQLiteDatabase mSQLiteDatabase;
	private boolean mIsInited;

	public abstract String getDatabaseFileName();

	protected abstract int getMinSupportVersion();

	public int getDatabaseVersion() {
		if (!initDatabase()) {
			return 0;
		}
		return mDatabaseVersion;
	}

	public SQLiteDatabase getSQLiteDatabase() {
		return mSQLiteDatabase;
	}

	protected boolean initDatabase() {
		if (mIsInited) {
			return true;
		}

		String databasePath = getDatabaseFolder() + getDatabaseFileName();
		File file = new File(databasePath);
		if (!file.exists()) {
			return false;
		}
		try {
			mSQLiteDatabase = SQLiteDatabase.openDatabase(databasePath, null,
					SQLiteDatabase.OPEN_READONLY);
			mDatabaseVersion = mSQLiteDatabase.getVersion();
		} catch (Exception e) {
			StackTraceElement stack = new Throwable().getStackTrace()[0];
			String localInfo = stack.getFileName() + ":"
					+ stack.getLineNumber();
			String message = e.toString();
			return false;
		}

		mIsInited = true;
		return true;
	}

	public void unInitDatabase() {
		if (mSQLiteDatabase != null) {
			mSQLiteDatabase.close();
			mSQLiteDatabase = null;
		}
		mIsInited = false;
	}

	public String getDatabaseFolder() {
		return "";
	}

	public boolean isDatabaseExist() {
		String databasePath = getDatabaseFolder() + getDatabaseFileName();
		File database = new File(databasePath);
		return database.exists();
	}

	public boolean isNeedUpdate() {
		if (!isDatabaseExist()) {
			return false;
		}
		int localVersion = getDatabaseVersion();
		if (localVersion < getMinSupportVersion()) {
			return true;
		}
		return false;
	}

	public String getCommonCss(int fontSizeType) {
		return "";
	}
}
