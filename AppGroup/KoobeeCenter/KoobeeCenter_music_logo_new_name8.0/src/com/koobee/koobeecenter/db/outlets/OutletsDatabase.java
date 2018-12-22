package com.koobee.koobeecenter.db.outlets;

import android.os.Environment;

import com.koobee.koobeecenter.db.AbstractDatabase;

import java.io.File;
import java.util.List;

/**
 * Created by yiyi on 2015/5/20.
 */
public class OutletsDatabase extends AbstractDatabase {

	private static OutletsDatabase mDatabase;
	private String dbName = "user2.db";

	private OutletsDatabase() {
	}

	public static OutletsDatabase getInstance() {
		if (mDatabase == null) {
			mDatabase = new OutletsDatabase();

			String folderPath = mDatabase.getDatabaseFolder();
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		return mDatabase;
	}

	public List<CustomerTable.Info> queryCitys() {
		if (!initDatabase()) {
			return null;
		}
		return CustomerTable.queryCitys(getSQLiteDatabase());
	}

	public List<CustomerTable.Info> queryByArea(String area) {
		if (!initDatabase()) {
			return null;
		}
		return CustomerTable.queryByArea(getSQLiteDatabase(), area);
	}

	@Override
	public String getDatabaseFileName() {
		return dbName;
	}

	@Override
	protected int getMinSupportVersion() {
		return Integer.MIN_VALUE;
	}

	@Override
	public String getDatabaseFolder() {
		return Environment.getExternalStorageDirectory().getPath() + "/koobee/";
	}
}
