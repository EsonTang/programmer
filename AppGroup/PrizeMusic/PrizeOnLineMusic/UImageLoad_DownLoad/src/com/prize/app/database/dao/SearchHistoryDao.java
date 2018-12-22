/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：操作搜索记录数据库
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.app.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.SearchHistory;
import com.prize.app.database.beans.HomeRecord;

/**
 **
 * 操作搜索记录数据库
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchHistoryDao {
	private static final String COLLUMS[] = new String[] {
			SearchHistory.HISTORY_TITLE, SearchHistory.TIMESTAMP };

	/**
	 * 获取搜索记录
	 * 
	 * @return ArrayList<String>
	 * @see
	 */
	public static ArrayList<String> getSearchHistoryList() {
		ArrayList<String> gameList = new ArrayList<String>();
		Cursor cursor = PrizeDatabaseHelper.query(SearchHistory.TABLE_NAME,
				COLLUMS, null, null, null, null, SearchHistory.TIMESTAMP
						+ " desc");
		if (cursor == null) {
			return gameList;
		}

		try {
			while (cursor.moveToNext()) {
				String title = cursor.getString(cursor
						.getColumnIndex(SearchHistory.HISTORY_TITLE));
				gameList.add(title);
			}
			cursor.close();
		} catch (Exception e) {
			cursor.close();
		}
		return gameList;
	}

	/**
	 * 获取搜索记录和输入的字符串匹配
	 * 
	 * @return ArrayList<String>
	 * @see
	 */
	public static ArrayList<String> getSearchHistoryMatchList(String enterKey) {
		ArrayList<String> gameList = new ArrayList<String>();
		Cursor cursor = PrizeDatabaseHelper.query(SearchHistory.TABLE_NAME,
				COLLUMS, null, null, null, null, SearchHistory.TIMESTAMP
						+ " desc");
		if (cursor == null) {
			return gameList;
		}

		try {
			while (cursor.moveToNext()) {
				String title = cursor.getString(cursor
						.getColumnIndex(SearchHistory.HISTORY_TITLE));
				if (title.contains(enterKey)) {
					gameList.add(title);
				}
			}
			cursor.close();
		} catch (Exception e) {
			cursor.close();
		}
		return gameList;
	}

	/**
	 * 添加搜索记录
	 * 
	 * @param record
	 *            搜索关键字
	 * @return
	 */
	public static void insert(String title, long currentTime) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(SearchHistory.HISTORY_TITLE, title);
		contentValues.put(SearchHistory.TIMESTAMP, currentTime);
		delRepeat(title);
		PrizeDatabaseHelper.insert(SearchHistory.TABLE_NAME, null,
				contentValues);

	}

	/**
	 * 先查询，后删除
	 * 
	 * @param title
	 * @return void
	 */
	public static void delRepeat(String title) {
		StringBuilder order = new StringBuilder();
		order.append(SearchHistory.HISTORY_TITLE + "=" + "'" + title + "'");
		// String sql =
		// "delete from table_history where table_history._id not in (select MAX(table_history._id) from table_history group by audio_id);";
		Cursor cursor = PrizeDatabaseHelper.query(SearchHistory.TABLE_NAME,
				null, order.toString(), null, null, null, null);
		if (cursor == null || cursor.getCount() <= 0) {
			return;
		}
		PrizeDatabaseHelper.delete(SearchHistory.TABLE_NAME,
				SearchHistory.HISTORY_TITLE + "=?", new String[] { title });
	}

	/**
	 * 清空表数据
	 * 
	 * @param title
	 * @return void
	 */
	public static void cleardata() {
		StringBuilder order = new StringBuilder("delete from ");
		order.append(SearchHistory.TABLE_NAME);
		PrizeDatabaseHelper.executeSQL(order.toString(), null);
	}
}
