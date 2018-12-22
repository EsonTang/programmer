package com.koobee.koobeecenter.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by yiyi.
 */
public class TableBase {
	protected static Cursor queryInfo(SQLiteDatabase db, String column,
			Object value, List<String> columns, String table) {
		if (TextUtils.isEmpty(column)) {
			return null;
		}
		String where = column + "=\"" + value + "\"";
		Object info = new Object();
		Cursor cursor = null;
		try {
			cursor = db.query(table, columns.toArray(new String[] {}), where,
					null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
		} catch (Exception e) {
			StackTraceElement stack = new Throwable().getStackTrace()[0];
			String localInfo = stack.getFileName() + ":"
					+ stack.getLineNumber();
			String message = e.toString();
		}
		return cursor;
	}

	protected static boolean isExistAsColumn(SQLiteDatabase database,
			String column, Object value, String table) {
		if (TextUtils.isEmpty(column)) {
			return false;
		}
		String where = column + "=\"" + value + "\"";
		Cursor cursor = null;
		try {
			cursor = database.query(table, null, where, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				return true;
			}

		} catch (Exception e) {
			StackTraceElement stack = new Throwable().getStackTrace()[0];
			String localInfo = stack.getFileName() + ":"
					+ stack.getLineNumber();
			String message = e.toString();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	protected static void addColumn(List<String> columns, long flag,
			long keyFlag, String valueColumn) {
		if ((flag & keyFlag) != 0) {
			columns.add(valueColumn);
		}
	}

	protected static String getValueString(List<String> columns, Cursor cursor,
			String column) {
		if (columns.contains(column)) {
			int index = cursor.getColumnIndex(column);
			return cursor.getString(index);
		}
		return "";
	}

	protected static Integer getValueInt(List<String> columns, Cursor cursor,
			String column) {
		if (columns.contains(column)) {
			int index = cursor.getColumnIndex(column);
			return cursor.getInt(index);
		}
		return 0;
	}

	protected static Double getValueDouble(List<String> columns, Cursor cursor,
			String column) {
		if (columns.contains(column)) {
			int index = cursor.getColumnIndex(column);
			return cursor.getDouble(index);
		}
		return 0.0;
	}

}
