package com.prize.uploadappinfo.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.uploadappinfo.bean.AppRecordInfo;
import com.prize.uploadappinfo.database.AppStateTable;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;

/**
 * 
 * @author Administrator
 *
 */
public class AppStateDAO {
	public static final int COLUMN_BASE_ID = 0;
	public static final int COLUMN_APP_NAME = COLUMN_BASE_ID + 1;
	public static final int COLUMN_APP_PACKAG = COLUMN_APP_NAME + 1;
	public static final int COLUMN_OP_TIME = COLUMN_APP_PACKAG + 1;
	public static final int COLUMN_ADDRESS = COLUMN_OP_TIME + 1;
	public static final int COLUMN_TYPE = COLUMN_ADDRESS + 1;

	private static AppStateDAO instance;

	// 数据库的每项对应的列
	public static AppStateDAO getInstance() {
		if (instance == null) {
			synchronized (AppStateDAO.class) {
				if (instance == null) {
					instance = new AppStateDAO();
				}
			}
		}
		return instance;
	}

	/**
	 * 获取数据库里的安装卸载信息
	 * 
	 * @return
	 */
	public ArrayList<AppRecordInfo> getApps() {
		ArrayList<AppRecordInfo> infos = new ArrayList<AppRecordInfo>();
		Cursor cursor = PrizeDatabaseHelper.query(AppStateTable.TABLE_NAME,
				null, null, null, null, null, null);
		if (cursor != null) {
			try {

				while (cursor.moveToNext()) {
					AppRecordInfo loadGame = new AppRecordInfo();
					loadGame.appName = cursor.getString(COLUMN_APP_NAME);
					loadGame.packageName = cursor.getString(COLUMN_APP_PACKAG);
					loadGame.opTime = cursor.getLong(COLUMN_OP_TIME);
					loadGame.address = cursor.getString(COLUMN_ADDRESS);
					loadGame.type = cursor.getString(COLUMN_TYPE);
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
