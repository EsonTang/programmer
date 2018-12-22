package com.prize.uploadappinfo.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.uploadappinfo.bean.AppInfo;
import com.prize.uploadappinfo.bean.AppRecordInfo;
import com.prize.uploadappinfo.database.AppStateTable;
import com.prize.uploadappinfo.database.InstalledAppTable;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;

/**
 * 
 * @author Administrator
 *
 */
public class AppInstalledDAO {
	public static final int COLUMN_BASE_ID = 0;
	public static final int COLUMN_APP_NAME = COLUMN_BASE_ID + 1;
	public static final int COLUMN_APP_PACKAG = COLUMN_APP_NAME + 1;
	public static final int COLUMN_VERSION_CODE = COLUMN_APP_PACKAG + 1;
	public static final int COLUMN_VERSIONNAME = COLUMN_VERSION_CODE + 1;

	private static AppInstalledDAO instance;

	// 数据库的每项对应的列
	public static AppInstalledDAO getInstance() {
		if (instance == null) {
			synchronized (AppInstalledDAO.class) {
				if (instance == null) {
					instance = new AppInstalledDAO();
				}
			}
		}
		return instance;
	}

	/**
	 * 获取手机安装应用
	 * 
	 * @return
	 */
	public ArrayList<AppInfo> getAllAppsInPhone() {
		ArrayList<AppInfo> infos = new ArrayList<AppInfo>();
		Cursor cursor = PrizeDatabaseHelper.query(InstalledAppTable.TABLE_NAME,
				null, null, null, null, null, null);
		if (cursor != null) {
			try {

				while (cursor.moveToNext()) {
					AppInfo loadGame = new AppInfo();
					loadGame.appName = cursor.getString(COLUMN_APP_NAME);
					loadGame.packageName = cursor.getString(COLUMN_APP_PACKAG);
					loadGame.versionCode = cursor.getInt(COLUMN_VERSION_CODE);
					loadGame.versionName = cursor.getString(COLUMN_VERSIONNAME);
					infos.add(loadGame);
				}
			} catch (Exception e) {

			} finally {
				if (cursor != null)
					cursor.close();

			}
		}
		return infos;
	}

	/**
	 * 插入安装或者卸载的app信息
	 * 
	 * @return
	 */
	public void insertAppInfo(AppRecordInfo info) {
		if (info == null)
			return;
		ContentValues contentValues = new ContentValues();
		contentValues.put(AppStateTable.APP_NAME, info.appName);
		contentValues.put(AppStateTable.APP_PACKAGE, info.packageName);
		contentValues.put(AppStateTable.OP_TIME, info.opTime);
		contentValues.put(AppStateTable.ADDRESS, info.address);
		contentValues.put(AppStateTable.TYPE, info.type);
		PrizeDatabaseHelper.insert(AppStateTable.TABLE_NAME, null,
				contentValues);
	}
}
