package com.prize.app.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.prize.app.database.DownLoadedTable;
import com.prize.app.database.DownloadGameTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;

/**
 * 类描述：已下载列表操作管理工具
 * 
 * @author huanglingjun
 * @version 版本
 */
public class DownLoadedDAO {
	private static SQLiteDatabase database;
	/** 字段集合*/
	public static String mColunmGameItemName[] = new String[] {
			DownLoadedTable.GAME_APPID, DownLoadedTable.GAME_PACKAGE,
			DownLoadedTable.GAME_NAME, DownLoadedTable.GAME_ICON_URL,
			DownLoadedTable.GAME_VERSION_CODE, DownLoadedTable.GAME_APK_SIZE,
			DownLoadedTable.GAME_APK_URL };

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

	private static DownLoadedDAO instance;

	public static DownLoadedDAO getInstance() {
		if (null == instance) {
			instance = new DownLoadedDAO();
		}
		return instance;
	}

	/** 插入列表 */
	public boolean insertApp(AppsItemBean appBean) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownLoadedTable.GAME_APPID, appBean.id);
		contentValues.put(DownLoadedTable.GAME_PACKAGE, appBean.packageName);
		contentValues.put(DownLoadedTable.GAME_NAME, appBean.name);
		if (appBean != null && !TextUtils.isEmpty(appBean.largeIcon)) {
			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.largeIcon);

		} else {

			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.iconUrl);
		}
		contentValues.put(DownLoadedTable.GAME_VERSION_CODE,
				appBean.versionCode);
		contentValues.put(DownLoadedTable.GAME_APK_SIZE, appBean.apkSize);
		contentValues.put(DownLoadedTable.GAME_APK_URL, appBean.downloadUrl);
		PrizeDatabaseHelper.insert(DownLoadedTable.TABLE_NAME_D0WNLOADED, null,
				contentValues);

		return true;
	}

	public ArrayList<ContentValues> getAllValues(
			ArrayList<AppsItemBean> collections) {
		ArrayList<ContentValues> values = new ArrayList<ContentValues>();
		for (int i = 0; i < collections.size(); i++) {
			AppsItemBean itemBean = collections.get(i);
			values.add(getContentValues(itemBean));
		}
		return values;
	}

	public ContentValues getContentValues(AppsItemBean appBean) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownLoadedTable.GAME_APPID, appBean.id);
		contentValues.put(DownLoadedTable.GAME_PACKAGE, appBean.packageName);
		contentValues.put(DownLoadedTable.GAME_NAME, appBean.name);

		if (appBean != null && !TextUtils.isEmpty(appBean.largeIcon)) {
			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.largeIcon);

		} else {
			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.iconUrl);

		}
		contentValues.put(DownLoadedTable.GAME_VERSION_CODE,
				appBean.versionCode);
		contentValues.put(DownLoadedTable.GAME_APK_SIZE, appBean.apkSize);
		contentValues.put(DownLoadedTable.GAME_APK_URL, appBean.downloadUrl);
		return contentValues;
	}

	/** 插入所有数据到列表 */
	public boolean insertAll(ArrayList<AppsItemBean> collections) {
		ArrayList<ContentValues> values = getAllValues(collections);
		PrizeDatabaseHelper.replaceAll(DownLoadedTable.TABLE_NAME_D0WNLOADED,
				values);
		return true;
	}

	/** 插入所有数据到列表 */
	public boolean replace(AppsItemBean itemBean) {
		ContentValues contentValues = getContentValues(itemBean);
		PrizeDatabaseHelper.replace(DownLoadedTable.TABLE_NAME_D0WNLOADED,
				contentValues);
		return true;
	}

	/** 根据pkgname删除一个数据 */
	public int deleteSingle(String pkgName) {
		int state = PrizeDatabaseHelper.deleteCollection(
				DownLoadedTable.TABLE_NAME_D0WNLOADED,
				DownLoadedTable.GAME_PACKAGE + "=?", new String[] { pkgName });
		return state;
	}

	/**
	 * 方法描述：清空列表
	 */
	public void deleteAll() {
		PrizeDatabaseHelper.deleteAllData(DownLoadedTable.TABLE_NAME_D0WNLOADED);
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
				DownLoadedTable.TABLE_NAME_D0WNLOADED, mColunmGameItemName,
				null, null, null, null, null);
		
		if (cursor != null) {	
			 while (cursor.moveToNext()){
				AppsItemBean loadGame = new AppsItemBean();
				loadGame.id = cursor.getString(cursor
						.getColumnIndex(DownLoadedTable.GAME_APPID));
				loadGame.packageName = cursor.getString(cursor
						.getColumnIndex(DownLoadedTable.GAME_PACKAGE));
				loadGame.name = cursor.getString(cursor
						.getColumnIndex(DownLoadedTable.GAME_NAME));
				loadGame.iconUrl = cursor.getString(cursor
						.getColumnIndex(DownLoadedTable.GAME_ICON_URL));
				loadGame.versionCode = cursor.getInt(cursor
						.getColumnIndex(DownLoadedTable.GAME_VERSION_CODE));
				loadGame.apkSize = cursor.getLong(cursor
						.getColumnIndex(DownLoadedTable.GAME_APK_SIZE)) + "";
				loadGame.downloadUrl = cursor.getString(cursor
						.getColumnIndex(DownLoadedTable.GAME_APK_URL));
				list.add(loadGame);
				
			}
			
			cursor.close();
		}
		
		ArrayList<AppsItemBean> listTwo = new ArrayList<AppsItemBean>();
		
		for (int i = list.size()-1; i >= 0; i--) {
			listTwo.add(list.get(i));
		}
		
		return listTwo;
	}

}