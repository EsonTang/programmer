package com.prize.music.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicDatabaseHelper;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.utils.MusicUtils;
/**
 * 
 **
 * 播放记录
 * @author longbaoxiu
 * @version V1.0
 */
public class HistoryDao {
	private static final HistoryDao mInstance = new HistoryDao();
	private volatile static MusicDatabaseHelper dbUtils;

	private HistoryDao() {

	}

	public static HistoryDao getInstance(Context context) {
		if (dbUtils == null) {
			dbUtils = MusicDatabaseHelper.getInstance(context);
		}
		return mInstance;
	}

	public boolean insert(ContentValues values) {
		SQLiteDatabase db = null;
		try {
			//deleteByIdAndSourceType((long)(values.get(HistoryColumns.HISTORY_BASE_ID)), (String)(values.get(HistoryColumns.HISTORY_SOURCE_TYPE)));
			deleteBySongNameAndSinger((String)values.get(HistoryColumns.AUDIO_TITLE), (String)(values.get(HistoryColumns.AUDIO_ARTIST)));
			deleteMoreQuantity();
			db = dbUtils.getWritableDatabase();
			long raw = db.insert(DatabaseConstant.TABLENAME_HISTORY, null, values);
			return raw != -1;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param songName Singer
	 * @return
	 */
	public boolean deleteBySongNameAndSinger(String songName, String Singer) {
		SQLiteDatabase db = null;
//		String whereClause = HistoryColumns.HISTORY_BASE_ID + "=" + id + " and " + HistoryColumns.HISTORY_SOURCE_TYPE + "=" + source_type;
        final String whereClause = HistoryColumns.AUDIO_TITLE + "=? and " + HistoryColumns.AUDIO_ARTIST + "=? ";
        final String[] whereArgs = new String[]{songName + "", Singer};
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(DatabaseConstant.TABLENAME_HISTORY, whereClause, whereArgs);			
			return count > 0;
		} catch(Exception exception){
			return false;//prize-IllegalStateException-pengcancan-20160817
		}finally {
//			if (db != null && db.isOpen()) {
//				db.close();
//				db = null;
//			}
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param source_type
	 * @return
	 */
	public boolean deleteByIdAndSourceType(long id, String source_type) {
		SQLiteDatabase db = null;
//		String whereClause = HistoryColumns.HISTORY_BASE_ID + "=" + id + " and " + HistoryColumns.HISTORY_SOURCE_TYPE + "=" + source_type;
        final String whereClause = HistoryColumns.HISTORY_BASE_ID + "=? and " + HistoryColumns.HISTORY_SOURCE_TYPE + "=? ";
        final String[] whereArgs = new String[]{id + "", source_type};
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(DatabaseConstant.TABLENAME_HISTORY, whereClause, whereArgs);			
			return count > 0;
		} finally {
			// if (db != null && db.isOpen()) {
			// db.close();
			// db = null;
			// }
		}
	}
	
	/**
	 * @see 数据库只保存120首歌,保留最近播放的歌
	 * @param _id
	 * @return
	 */
	public static final int MAXQUANTITY = 119;

	public boolean deleteMoreQuantity() {
		Cursor cursor = queryAllCursor(DatabaseConstant.TABLENAME_HISTORY);
		if (cursor != null) {
			try {
				cursor.moveToFirst();
			} catch (Exception e) {
				if(cursor !=null && !cursor.isClosed()){
					cursor.close();
					cursor = null;
				}
			}
			if (cursor.getCount() <= MAXQUANTITY) {
				cursor.close();
				return true;
			} else {
				try {
					int songId_index = cursor
							.getColumnIndex(HistoryColumns.HISTORY_BASE_ID);
					int source_type_index = cursor
							.getColumnIndex(HistoryColumns.HISTORY_SOURCE_TYPE);
					long songId = cursor.getLong(songId_index);
					String source_type = cursor.getString(source_type_index);
					deleteByIdAndSourceType(songId, source_type);
					
				} catch (Exception e) {
				}finally{
					if(cursor !=null && !cursor.isClosed()){
						cursor.close();
						cursor = null;
					}
				}
			}
		}
		 
		return false;
	}
	

	public boolean delete(int _id) {
		SQLiteDatabase db = null;
		String whereClause = HistoryColumns.ID + "=" + _id;
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(DatabaseConstant.TABLENAME_HISTORY, whereClause, null);
			return count > 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}

	}

	/**
	 * @see 删除本地歌曲，根据AudioId
	 * @param _id
	 * @return
	 */
	public boolean deleteByAudioId(long _id) {
		SQLiteDatabase db = null;
//		String whereClause = HistoryColumns.HISTORY_BASE_ID + "=" + _id +" and " + HistoryColumns.HISTORY_SOURCE_TYPE + "=" + DatabaseConstant.LOCAL_TYPE;
		String whereClause = HistoryColumns.HISTORY_BASE_ID + "=?" + " and " + HistoryColumns.HISTORY_SOURCE_TYPE + "=?";
		String[] selections = new String[] {_id +"", DatabaseConstant.LOCAL_TYPE};
		try {
			db = dbUtils.getWritableDatabase();
			int count = db.delete(DatabaseConstant.TABLENAME_HISTORY, whereClause, selections);
			return count > 0;
		} finally {
//			if (db != null && db.isOpen()) {
//				db.close();
//				db = null;
//			}
		}

	}

//	public Cursor queryWithCursor() {
//		SQLiteDatabase db = dbUtils.getWritableDatabase();
//		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_HISTORY
//				+ " group by " + HistoryColumns.AUDIO_ID
//				+ " order by timestamp desc limit 300;";
//		Cursor cursor = db.rawQuery(sql_qury, null);
//		return cursor;
//	}

	public Cursor queryAllCursor(String table_name) {
		SQLiteDatabase db = dbUtils.getWritableDatabase();
//		String sql_qury = "SELECT * FROM " + table_name + ";" ;
		String sql_qury = "SELECT * FROM " + table_name
				+ " group by " + HistoryColumns.HISTORY_BASE_ID
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
		String sql_qury = "SELECT * FROM " + DatabaseConstant.TABLENAME_HISTORY
				+ " where " + HistoryColumns.AUDIO_ID + " in " + audioIdsstring
				+ " group by " + HistoryColumns.AUDIO_ID
				+ " order by timestamp desc limit 300;";
		Cursor cursor = db.rawQuery(sql_qury, null);

		return cursor;
	}
	
	
	/**
	 * @see 获取最近播放的列表所有歌曲
	 * @param table_name
	 * @return List<MusicInfo>
	 */
	public List<MusicInfo> getAllMusicInfoFromTable(String table_name){
		List<MusicInfo> list = new ArrayList<MusicInfo>();
		Cursor cursor = queryAllCursor(DatabaseConstant.TABLENAME_HISTORY);
		if (cursor != null && cursor.getCount() > 0) {
			if (table_name.equals(DatabaseConstant.TABLENAME_HISTORY)) {
				int index_title = cursor
						.getColumnIndex(HistoryColumns.AUDIO_TITLE);
				int index_artist = cursor
						.getColumnIndex(HistoryColumns.AUDIO_ARTIST);
				int index_base_id = cursor
						.getColumnIndex(HistoryColumns.HISTORY_BASE_ID);
//				int index_user_id = cursor
//						.getColumnIndex(HistoryColumns.HISTORY_USER_ID);
				int index_source_type = cursor
						.getColumnIndex(HistoryColumns.HISTORY_SOURCE_TYPE);
				
				try {
					while (cursor.moveToNext()) {
						MusicInfo music_info = new MusicInfo();
						music_info.songName = cursor.getString(index_title);
						music_info.singer = cursor.getString(index_artist);
						music_info.songId = cursor.getLong(index_base_id);
						music_info.userId = /* cursor.getString(index_user_id) */MusicUtils
								.getUserId();
						music_info.source_type = cursor
								.getString(index_source_type);
						list.add(music_info);
					}
					cursor.close();
					
				} catch (Exception e) {
					// TODO: handle exception
				}finally{
					if(cursor !=null){
						cursor.close();
					}
				}
			}
			if(cursor !=null){
				cursor.close();
			}
		}
		return list;
	}
	
	
}
