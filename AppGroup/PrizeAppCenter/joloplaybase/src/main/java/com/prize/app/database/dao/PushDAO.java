package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.PushTable;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * push 表格操作类（下载进程操作）
 * 
 * @author huanglingjun
 *
 */
public class PushDAO {

	private static PushDAO instance;

	public static PushDAO getInstance() {
		if (null == instance) {
			instance = new PushDAO();
		}
		return instance;
	}

	public boolean insert(AppsItemBean bean) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PushTable.APP_PACKAGERNAME, bean.packageName);
		contentValues.put(PushTable.APP_VERSION, bean.versionCode);
		PrizeDatabaseHelper.insert(PushTable.TABLE_NAME_PUSH, null,
				contentValues);
		return true;
	}

//	public boolean insertAll(ArrayList<AppsItemBean> list) {
//		for (int i = 0; i < list.size(); i++) {
//			AppsItemBean bean = list.get(i);
//			ContentValues contentValues = new ContentValues();
//			contentValues.put(PushTable.APP_PACKAGERNAME, bean.packageName);
//			contentValues.put(PushTable.APP_VERSION, bean.versionCode);
//			PrizeDatabaseHelper.insert(PushTable.TABLE_NAME_PUSH, null,
//					contentValues);
//		}
//		return true;
//	}

	public void replace(AppsItemBean bean) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PushTable.APP_PACKAGERNAME, bean.packageName);
		contentValues.put(PushTable.APP_VERSION, bean.versionCode);

		PrizeDatabaseHelper.replace(PushTable.TABLE_NAME_PUSH, contentValues);
	}

	public void replaceAll(List<AppsItemBean> list) {
		 ArrayList<ContentValues> values = new ArrayList<ContentValues>();
		for (int i = 0; i < list.size(); i++) {
			AppsItemBean bean = list.get(i);
			ContentValues contentValues = new ContentValues();
			contentValues.put(PushTable.APP_PACKAGERNAME, bean.packageName);
			contentValues.put(PushTable.APP_VERSION, bean.versionCode);			
			values.add(contentValues);
		}
		PrizeDatabaseHelper.replaceAll(PushTable.TABLE_NAME_PUSH,
				values);
	}

	/** 删除一条数据 */
	public void deletePushData(String pkgName) {
		PrizeDatabaseHelper.delete(PushTable.TABLE_NAME_PUSH,
				PushTable.APP_PACKAGERNAME + "=?", new String[] { pkgName });
	}

	/**
	 * 方法描述：删除所有数据
	 */
	public void deleteAllData(String[] pkgNames) {
		PrizeDatabaseHelper.delete(PushTable.TABLE_NAME_PUSH,
				PushTable.APP_PACKAGERNAME + "=?", pkgNames);
	}

	/**
	 * @return ArrayList<AppsItemBean>
	 * @see
	 */
	public ArrayList<AppsItemBean> getAppList() {
		ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
		/*
		 * String sql =
		 * "select * from table_game where download_state=0 or download_state=4;"
		 * ; Cursor cursor = PrizeDatabaseHelper.rawQuery(sql, null);
		 */
		Cursor cursor = null;
		try {
			cursor = PrizeDatabaseHelper.query(PushTable.TABLE_NAME_PUSH,
					PushTable.PUSH_COLUMNS, null, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					AppsItemBean bean = new AppsItemBean();
					bean.versionCode = cursor.getInt(cursor
							.getColumnIndex(PushTable.APP_VERSION));
					bean.packageName = cursor.getString(cursor
							.getColumnIndex(PushTable.APP_PACKAGERNAME));
					list.add(bean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return list;
	}

}