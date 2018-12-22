package com.prize.music.history;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import com.prize.music.database.DBUtils;
import com.prize.music.helpers.utils.LogUtils;

public class HistoryDao {
	private static final HistoryDao mInstance = new HistoryDao();
	private volatile static DBUtils dbUtils;

	private HistoryDao() {

	}

	public static HistoryDao getInstance(Context context) {
		if (dbUtils == null) {
			dbUtils = DBUtils.getInstance(context);
		}
		return mInstance;
	}

	public boolean insert(ContentValues values) {
		SQLiteDatabase db = null;
		try {
			db = dbUtils.getWritableDatabase();
			long raw = db.insert(HistoryColumns.TABLE_NAME, null, values);
			return raw != -1;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	public boolean delete(int _id) {
		SQLiteDatabase db = null;
		String whereClause = HistoryColumns.ID + "=" + _id;
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(HistoryColumns.TABLE_NAME, whereClause, null);
			return count > 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}

	}

	public boolean deleteByAudioId(long _id) {
		SQLiteDatabase db = null;
		String whereClause = HistoryColumns.AUDIO_ID + "=" + _id;
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(HistoryColumns.TABLE_NAME, whereClause, null);
			return count > 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}

	}

	public Cursor queryWithCursor() {
		SQLiteDatabase db = dbUtils.getWritableDatabase();
		String sql_qury = "SELECT * FROM " + HistoryColumns.TABLE_NAME
				+ " group by " + HistoryColumns.AUDIO_ID
				+ " order by timestamp desc limit 300;";
		Cursor cursor = db.rawQuery(sql_qury, null);
		return cursor;
	}

	/**
	 *
	 */
	public void delRepeat() {
		SQLiteDatabase db = null;
		try {
			String sql = "delete from table_history where table_history._id not in (select MAX(table_history._id) from table_history group by audio_id);";
			db = dbUtils.getWritableDatabase();
			db.execSQL(sql);
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * ��ȡ���������ļ���id
	 * 
	 * @param context
	 * @param audioIds
	 *            �����ļ���AudioID
	 * @return
	 */
	public Cursor getAudioIDsInHistory(Context context, long[] audioIds) {
		if (audioIds == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		int length = audioIds.length;
		if (length <= 0) {
			return null;
		}
		// ��audioIds��Ϊ(2,3,4,5)����ʽ������ݿ��ѯ������
		StringBuffer audioIdsstring = new StringBuffer("(");
		for (int i = 0; i < length; i++) {
			if (audioIds[i] <= 0) {
				continue;
			}
			audioIdsstring.append(audioIds[i] + ",");
		}
		audioIdsstring.setCharAt(audioIdsstring.length() - 1, ')');

		SQLiteDatabase db = dbUtils.getWritableDatabase();
		String sql_qury = "SELECT * FROM " + HistoryColumns.TABLE_NAME
				+ " where " + HistoryColumns.AUDIO_ID + " in " + audioIdsstring
				+ " group by " + HistoryColumns.AUDIO_ID
				+ " order by timestamp desc limit 300;";
		Cursor cursor = db.rawQuery(sql_qury, null);

		return cursor;
	}
}
