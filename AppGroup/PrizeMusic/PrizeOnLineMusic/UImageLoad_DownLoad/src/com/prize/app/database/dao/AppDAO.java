package com.prize.app.database.dao;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.app.database.DownloadAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.DownloadState;
import com.prize.app.download.DownloadTask;
import com.prize.app.net.datasource.base.AppsItemBean;

public class AppDAO {
	/** 游戏数据库表名 */
	public static String mColunmGameItemName[] = new String[] {
			DownloadAppTable.GAME_CODE, DownloadAppTable.GAME_PACKAGE,
			DownloadAppTable.GAME_CLASS, DownloadAppTable.GAME_NAME,
			DownloadAppTable.GAME_ICON_URL, DownloadAppTable.GAME_VERSION_CODE,
			DownloadAppTable.GAME_APK_SIZE, DownloadAppTable.GAME_APK_URL,
			DownloadAppTable.GAME_DOWNLOAD_STATE,
			DownloadAppTable.GAME_LOADED_SIZE, DownloadAppTable.GAME_TYPE,
			DownloadAppTable.GAME_LOAD_FLAG };

	// 数据库的每项对应的列
	public static final int COLUMN_GAME_ID = 0;
	public static final int COLUMN_GAME_PACKAGE = COLUMN_GAME_ID + 1;
	public static final int COLUMN_GAME_CLASS = COLUMN_GAME_PACKAGE + 1;
	public static final int COLUMN_GAME_NAME = COLUMN_GAME_CLASS + 1;
	public static final int COLUMN_GAME_ICONURL = COLUMN_GAME_NAME + 1;
	public static final int COLUMN_GAME_VERSIONCODE = COLUMN_GAME_ICONURL + 1;
	public static final int COLUMN_GAME_APKSIZE = COLUMN_GAME_VERSIONCODE + 1;
	public static final int COLUMN_GAME_DOWNURL = COLUMN_GAME_APKSIZE + 1;
	public static final int COLUMN_GAME_DOWNSTATE = COLUMN_GAME_DOWNURL + 1;
	public static final int COLUMN_GAME_DOWNLOAD_POSITION = COLUMN_GAME_DOWNSTATE + 1;
	public static final int COLUMN_GAME_GAME_TYPE = COLUMN_GAME_DOWNLOAD_POSITION + 1;
	public static final int COLUMN_GAME_LOAD_FLAG = COLUMN_GAME_GAME_TYPE + 1;

	private static AppDAO instance;

	public static AppDAO getInstance() {
		if (null == instance) {
			instance = new AppDAO();
		}
		return instance;
	}

	/** 创建游戏下载任务 */
	public boolean insertGame(DownloadTask task) {
		// if (null == task) {
		// return false;
		// }
		// AppsItemBean gameBean = task.loadGame;
		//
		// ContentValues contentValues = new ContentValues();
		// contentValues.put(DownloadAppTable.GAME_CODE, gameBean.id);
		// contentValues.put(DownloadAppTable.GAME_PACKAGE,
		// gameBean.packageName);
		// contentValues.put(DownloadAppTable.GAME_NAME, gameBean.name);
		// contentValues.put(DownloadAppTable.GAME_ICON_URL, gameBean.iconUrl);
		// contentValues.put(DownloadAppTable.GAME_VERSION_CODE,
		// gameBean.versionCode);
		// contentValues.put(DownloadAppTable.GAME_APK_SIZE, gameBean.apkSize);
		// contentValues.put(DownloadAppTable.GAME_APK_URL,
		// gameBean.downloadUrl);
		// contentValues.put(DownloadAppTable.GAME_DOWNLOAD_STATE,
		// task.gameDownloadState);
		// contentValues.put(DownloadAppTable.GAME_LOADED_SIZE,
		// task.gameDownloadPostion);
		// // contentValues.put(DownloadAppTable.GAME_CLASS,
		// // task.isUpdate_install);
		// // contentValues.put(DownloadAppTable.GAME_TYPE, gameBean.gameType);
		// contentValues.put(DownloadAppTable.GAME_LOAD_FLAG, task.loadFlag);
		// PrizeDatabaseHelper.insert(DownloadAppTable.TABLE_NAME_APP, null,
		// contentValues);

		return true;
	}

	/**
	 * 只更新修改游戏信息
	 * 
	 * @param gameBean
	 * @param loadSize
	 * @return
	 */
	public void updateGame(DownloadTask task) {
		// AppsItemBean gameBean = task.loadGame;
		// ContentValues contentValues = new ContentValues();
		// contentValues.put(DownloadAppTable.GAME_CODE, gameBean.id);
		// contentValues.put(DownloadAppTable.GAME_PACKAGE,
		// gameBean.packageName);
		// // contentValues.put(DownloadAppTable.GAME_CLASS,
		// // gameBean.gameActivity);
		// contentValues.put(DownloadAppTable.GAME_NAME, gameBean.name);
		// contentValues.put(DownloadAppTable.GAME_ICON_URL, gameBean.iconUrl);
		// contentValues.put(DownloadAppTable.GAME_VERSION_CODE,
		// gameBean.versionCode);
		// contentValues.put(DownloadAppTable.GAME_APK_SIZE, gameBean.apkSize);
		// contentValues.put(DownloadAppTable.GAME_APK_URL,
		// gameBean.downloadUrl);
		// // contentValues.put(DownloadAppTable.GAME_TYPE, gameBean.gameType);
		// contentValues.put(DownloadAppTable.GAME_LOAD_FLAG, task.loadFlag);
		//
		// String where = DownloadAppTable.GAME_PACKAGE + "=?";
		// String[] args = new String[] { gameBean.packageName };
		//
		// PrizeDatabaseHelper.update(DownloadAppTable.TABLE_NAME_APP,
		// contentValues, where, args);
	}

	/** 根据pkgname删除一个游戏数据 */
	public void deleteData(String pkgName) {
		PrizeDatabaseHelper.delete(DownloadAppTable.TABLE_NAME_APP,
				DownloadAppTable.GAME_PACKAGE + "=?", new String[] { pkgName });
	}

	/**
	 * 方法描述：删除所有已下载app
	 */
	public void deleteAllData(String[] pkgNames) {
		PrizeDatabaseHelper.delete(DownloadAppTable.TABLE_NAME_APP,
				DownloadAppTable.GAME_PACKAGE + "=?", pkgNames);
	}

	/**
	 * 修改下载状态，只需记录
	 * STATE_DOWNLOAD_SUCESS，STATE_DOWNLOAD_ERROR，STATE_DOWNLOAD_PAUSE
	 * ，恢复时，用于区别下载状态
	 * 
	 * @param gameCode
	 * @param state
	 * @return
	 */
	public void updateState(String pkgName, int state) {
		if ((DownloadState.STATE_DOWNLOAD_SUCESS == state)
				|| (DownloadState.STATE_DOWNLOAD_ERROR == state)
				|| (DownloadState.STATE_DOWNLOAD_PAUSE == state)) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DownloadAppTable.GAME_DOWNLOAD_STATE, state);
			PrizeDatabaseHelper.update(DownloadAppTable.TABLE_NAME_APP,
					contentValues, DownloadAppTable.GAME_PACKAGE + "=?",
					new String[] { pkgName });
		}
	}

	/**
	 * 获取下载任务 下载中及待下载的的app
	 * 
	 * @return ArrayList<AppsItemBean>
	 * @see
	 */
	public ArrayList<AppsItemBean> getDownAppList() {
		ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
		/*
		 * String sql =
		 * "select * from table_game where download_state=0 or download_state=4;"
		 * ; Cursor cursor = PrizeDatabaseHelper.rawQuery(sql, null);
		 */
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadAppTable.TABLE_NAME_APP, mColunmGameItemName, null,
				null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (cursor.getLong(COLUMN_GAME_APKSIZE) != cursor
						.getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
					AppsItemBean loadGame = new AppsItemBean();
					loadGame.id = cursor.getString(COLUMN_GAME_ID);
					// loadGame.gameActivity =
					// cursor.getString(COLUMN_GAME_CLASS);
					loadGame.packageName = cursor
							.getString(COLUMN_GAME_PACKAGE);
					loadGame.name = cursor.getString(COLUMN_GAME_NAME);
					loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
					loadGame.versionCode = cursor
							.getInt(COLUMN_GAME_VERSIONCODE);
					loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
					loadGame.downloadUrl = cursor
							.getString(COLUMN_GAME_DOWNURL);
					int status = cursor.getInt(COLUMN_GAME_DOWNSTATE);
					list.add(loadGame);
				}
			}
		}
		return list;
	}

	/**
	 * 获取下载任务 下载中及待下载的的app
	 * 
	 * @return ArrayList<AppsItemBean>
	 * @see
	 */
	public ArrayList<AppsItemBean> getDownloadAppList() {
		ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
		/*
		 * String sql =
		 * "select * from table_game where download_state=0 or download_state=4;"
		 * ; Cursor cursor = PrizeDatabaseHelper.rawQuery(sql, null);
		 */
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadAppTable.TABLE_NAME_APP, mColunmGameItemName, null,
				null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (cursor.getLong(COLUMN_GAME_APKSIZE) != cursor
						.getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
					AppsItemBean loadGame = new AppsItemBean();
					loadGame.id = cursor.getString(COLUMN_GAME_ID);
					// loadGame.gameActivity =
					// cursor.getString(COLUMN_GAME_CLASS);
					loadGame.packageName = cursor
							.getString(COLUMN_GAME_PACKAGE);
					loadGame.name = cursor.getString(COLUMN_GAME_NAME);
					loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
					loadGame.versionCode = cursor
							.getInt(COLUMN_GAME_VERSIONCODE);
					loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
					loadGame.downloadUrl = cursor
							.getString(COLUMN_GAME_DOWNURL);
					int status = cursor.getInt(COLUMN_GAME_DOWNSTATE);
					list.add(loadGame);
				}
			}
		}
		return list;
	}

	/**
	 * 方法描述：获取下载成功的apk
	 * 
	 * @return ArrayList<AppsItemBean>
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public ArrayList<AppsItemBean> getHasDownloadedAppList() {
		ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadAppTable.TABLE_NAME_APP, mColunmGameItemName, null,
				null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (cursor.getLong(COLUMN_GAME_APKSIZE) == cursor
						.getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
					AppsItemBean loadGame = new AppsItemBean();
					loadGame.id = cursor.getString(COLUMN_GAME_ID);
					// loadGame.gameActivity =
					// cursor.getString(COLUMN_GAME_CLASS);
					loadGame.packageName = cursor
							.getString(COLUMN_GAME_PACKAGE);
					loadGame.name = cursor.getString(COLUMN_GAME_NAME);
					loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
					loadGame.versionCode = cursor
							.getInt(COLUMN_GAME_VERSIONCODE);
					loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
					loadGame.downloadUrl = cursor
							.getString(COLUMN_GAME_DOWNURL);
					int status = cursor.getInt(COLUMN_GAME_DOWNSTATE);
					list.add(loadGame);
				}
			}
		}
		return list;
	}

	/**
	 * 更新下载进度
	 * 
	 * @param pkgName
	 * @param pos
	 * @return
	 */
	public void updateDownloadSize(String pkgName, long totalSize, long pos) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadAppTable.GAME_APK_SIZE, totalSize);
		contentValues.put(DownloadAppTable.GAME_LOADED_SIZE, pos);
		PrizeDatabaseHelper.update(DownloadAppTable.TABLE_NAME_APP,
				contentValues, DownloadAppTable.GAME_PACKAGE + "=?",
				new String[] { pkgName });
	}

	public void updateGameFlag(DownloadTask loadGameTask) {
		// if (null == loadGameTask) {
		// return;
		// }
		//
		// ContentValues contentValues = new ContentValues();
		// contentValues.put(DownloadAppTable.GAME_LOAD_FLAG,
		// loadGameTask.loadFlag);
		// String pkgName = loadGameTask.loadGame.packageName;
		//
		// PrizeDatabaseHelper.update(DownloadAppTable.TABLE_NAME_APP,
		// contentValues, DownloadAppTable.GAME_PACKAGE + "=?",
		// new String[] { pkgName });
	}

	/**
	 * 获取所有的下载任务
	 * 
	 * @return
	 */
	public HashMap<String, DownloadTask> getAllDownloadExeTask() {
		HashMap<String, DownloadTask> dataMap = new HashMap<String, DownloadTask>();
		// Cursor cursor = PrizeDatabaseHelper.query(
		// DownloadAppTable.TABLE_NAME_APP, mColunmGameItemName, null,
		// null, null, null, null);
		// DownloadTask task;
		// if (cursor != null) {
		// while (cursor.moveToNext()) {
		// task = sqlToDownloadTask(cursor);
		// dataMap.put(task.loadGame.packageName, task);
		// }
		// cursor.close();
		// }

		return dataMap;
	}

	/**
	 * 转换为下载任务
	 * 
	 * @param cursor
	 * @return
	 */
	private DownloadTask sqlToDownloadTask(Cursor cursor) {
		DownloadTask data = new DownloadTask();
		// AppsItemBean loadGame = data.loadGame;
		// loadGame.id = cursor.getString(COLUMN_GAME_ID);
		// // loadGame.gameActivity = cursor.getString(COLUMN_GAME_CLASS);
		// loadGame.packageName = cursor.getString(COLUMN_GAME_PACKAGE);
		// loadGame.name = cursor.getString(COLUMN_GAME_NAME);
		// loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
		// loadGame.versionCode = cursor.getInt(COLUMN_GAME_VERSIONCODE);
		// loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
		// loadGame.downloadUrl = cursor.getString(COLUMN_GAME_DOWNURL);
		// data.gameDownloadState = cursor.getInt(COLUMN_GAME_DOWNSTATE);
		// if ((DownloadState.STATE_DOWNLOAD_WAIT == data.gameDownloadState)
		// || DownloadState.STATE_DOWNLOAD_START_LOADING ==
		// data.gameDownloadState) {
		// // 如果是wait or
		// // loading的状态，修改成暂停。因为APP已经被退出了，另外，兼容之前的版本，之前记录loading的状态，需要转换回来
		// data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
		// }
		// int loadsize = (int) cursor.getLong(COLUMN_GAME_DOWNLOAD_POSITION);
		// data.setDownloadSize(Integer.parseInt(loadGame.apkSize), loadsize);
		// // loadGame.gameType = (byte) cursor.getInt(COLUMN_GAME_GAME_TYPE);
		//
		// data.loadFlag = cursor.getInt(COLUMN_GAME_LOAD_FLAG);
		return data;
	}
}