package com.prize.app.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.prize.app.database.DownLoadDataTable;
import com.prize.app.database.DownloadGameTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.beans.DownLoadDataBean;
import com.prize.app.net.datasource.base.AppsItemBean;

/**
 * 类描述：下载列表操作管理工具
 * 
 * @author huanglingjun
 * @version 版本
 */
public class DownLoadDataDAO {
	private static SQLiteDatabase database;
	/** 游戏数据库表名 */
	public static String mColunmGameItemName[] = new String[] {
			DownLoadDataTable.DOWNLOADTYPE, DownLoadDataTable.PACKAGENAME};
	
	private static DownLoadDataDAO instance;

	public static DownLoadDataDAO getInstance() {
		if (null == instance) {
			instance = new DownLoadDataDAO();
		}
		return instance;
	}

	/** 插入列表 */
	public boolean insertApp(String downloadType, String packageName,String timeDelta) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownLoadDataTable.DOWNLOADTYPE, downloadType);
		contentValues.put(DownLoadDataTable.PACKAGENAME, packageName);
		contentValues.put(DownLoadDataTable.TIMEDALTA, timeDelta);
		PrizeDatabaseHelper.insert(DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA, null,
				contentValues);

		return true;
	}

	public ContentValues getContentValues(String downloadType, String packageName,String timeDelta) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownLoadDataTable.DOWNLOADTYPE, downloadType);
		contentValues.put(DownLoadDataTable.PACKAGENAME, packageName);
		contentValues.put(DownLoadDataTable.TIMEDALTA, timeDelta);
		return contentValues;
	}

	/** 插入所有数据到列表 */
	public boolean replace(String downloadType, String packageName,String timeDelta) {
		ContentValues contentValues = getContentValues(downloadType,packageName,timeDelta);
		PrizeDatabaseHelper.replace(DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA,
				contentValues);
		return true;
	}

	/** 根据pkgname删除一个条数据 */
	public int deleteSingle(String pkgName) {
		int state = PrizeDatabaseHelper.deleteCollection(
				DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA,
				DownLoadDataTable.PACKAGENAME + "=?", new String[] { pkgName });
		return state;
	}

	/**
	 * 方法描述：清空列表
	 */
	public void deleteAll() {
		PrizeDatabaseHelper.deleteAllData(DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA);
	}

	/**
	 * 方法描述：获取下载成功的apk
	 * 
	 * @return ArrayList<AppsItemBean>
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public ArrayList<DownLoadDataBean> getHasDownloadedAppList() {
		ArrayList<DownLoadDataBean> list = new ArrayList<DownLoadDataBean>();
		Cursor cursor = null;
		try {
			cursor = PrizeDatabaseHelper.query(
					DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA, DownLoadDataTable.ACCOUNT_COLUMNS,
					null, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					DownLoadDataBean downloadApp = new DownLoadDataBean();
					downloadApp.downloadType = cursor.getString(cursor
							.getColumnIndex(DownLoadDataTable.DOWNLOADTYPE));
					downloadApp.packageName = cursor.getString(cursor
							.getColumnIndex(DownLoadDataTable.PACKAGENAME));
					downloadApp.timeDelta = Long.parseLong(cursor.getString(cursor
							.getColumnIndex(DownLoadDataTable.TIMEDALTA)));
					list.add(downloadApp);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();			
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return list;
	}

}