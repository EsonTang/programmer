package com.prize.videoc.db;

import android.content.Context;

import com.lidroid.xutils.DbUtils;

public class DbManager {
	public static DbManager mInstance = new DbManager();
	private volatile DbUtils db;
	private static final String DB_NAME = "pvideo.db";

	private DbManager() {
	}

	public static DbManager getInstance() {
		return mInstance;
	}

	public void createDb(Context ctx) {
		synchronized (DbManager.class) {
			if (db == null)
				db = createFinalDb(ctx);
		}
	}

	public DbUtils getDb() {
		return db;
	}

	private static DbUtils createFinalDb(Context context) {
		DbUtils db = DbUtils.create(context, DB_NAME);

		return db;
	}

}
