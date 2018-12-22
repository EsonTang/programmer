package com.prize.app.database.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.prize.app.database.CoverTable;
import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.CoverItemBean;
import com.prize.app.threads.SQLSingleThreadExcutor;

/**
 **
 * 操作封面数据库
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class CoverDAO {
	public static CoverItemBean query() {

		Cursor cursor = PrizeDatabaseHelper.query(CoverTable.TABLE_NAME_GAME,
				null, null, null, null, null, null);

		CoverItemBean record = null;
		try {
			while (cursor.moveToNext()) {
				record = new CoverItemBean();
				record.id = cursor.getInt(cursor
						.getColumnIndex(CoverTable.COVERID));

				record.associateId = cursor.getString(cursor
						.getColumnIndex(CoverTable.ASSOCIATEID));
				record.adType = cursor.getString(cursor
						.getColumnIndex(CoverTable.ADTYPE));
				record.title = cursor.getString(cursor
						.getColumnIndex(CoverTable.TITLE));
				record.imageUrl = cursor.getString(cursor
						.getColumnIndex(CoverTable.IMAGEURL));
				record.url = cursor.getString(cursor
						.getColumnIndex(CoverTable.URL));
				record.description = cursor.getString(cursor
						.getColumnIndex(CoverTable.DESCRIPTION));
				record.createTime = cursor.getString(cursor
						.getColumnIndex(CoverTable.CREATETIME));
				record.status = cursor.getInt(cursor
						.getColumnIndex(CoverTable.STATUS));
				record.position = cursor.getInt(cursor
						.getColumnIndex(CoverTable.POSITION));
				record.seconds = cursor.getInt(cursor
						.getColumnIndex(CoverTable.SECONDS));
				record.startTime = cursor.getString(cursor
						.getColumnIndex(CoverTable.STARTTIME));
				record.endTime = cursor.getString(cursor
						.getColumnIndex(CoverTable.ENDTIME));
			}
			cursor.close();
		} catch (Exception e) {
		}
		return record;
	}
	
    /**
     * 查询所有
     * @return
     */
	public static ArrayList<CoverItemBean> queryAll() {

		ArrayList<CoverItemBean> list = new ArrayList<CoverItemBean>();
		Cursor cursor = PrizeDatabaseHelper.query(CoverTable.TABLE_NAME_GAME,
				null, null, null, null, null, null);
		CoverItemBean record = null;
		try {
			if (cursor != null) {
				while (cursor.moveToNext()) {
					record = new CoverItemBean();
					record.id = cursor.getInt(cursor
							.getColumnIndex(CoverTable.COVERID));
					record.associateId = cursor.getString(cursor
							.getColumnIndex(CoverTable.ASSOCIATEID));
					record.adType = cursor.getString(cursor
							.getColumnIndex(CoverTable.ADTYPE));
					record.title = cursor.getString(cursor
							.getColumnIndex(CoverTable.TITLE));
					record.imageUrl = cursor.getString(cursor
							.getColumnIndex(CoverTable.IMAGEURL));
					record.url = cursor.getString(cursor
							.getColumnIndex(CoverTable.URL));
					record.description = cursor.getString(cursor
							.getColumnIndex(CoverTable.DESCRIPTION));
					record.createTime = cursor.getString(cursor
							.getColumnIndex(CoverTable.CREATETIME));
					record.status = cursor.getInt(cursor
							.getColumnIndex(CoverTable.STATUS));
					record.position = cursor.getInt(cursor
							.getColumnIndex(CoverTable.POSITION));
					record.seconds = cursor.getInt(cursor
							.getColumnIndex(CoverTable.SECONDS));
					record.startTime = cursor.getString(cursor
							.getColumnIndex(CoverTable.STARTTIME));
					record.endTime = cursor.getString(cursor
							.getColumnIndex(CoverTable.ENDTIME));
					list.add(record);
				}
			}
			cursor.close();
		} catch (Exception e) {
		}
		return list;
	}
	
	public static boolean isupdate(ContentValues cv) {
		Cursor cursor = null;
		String orgImgUrl = cv.getAsString(CoverTable.IMAGEURL);
		String columns[] = new String[] { CoverTable.COVERID,
				CoverTable.IMAGEURL };
		String where = CoverTable.IMAGEURL + "=?";
		String[] whereClause = new String[] { orgImgUrl };
		try {
			cursor = PrizeDatabaseHelper.query(CoverTable.TABLE_NAME_GAME,
					columns, where, whereClause, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String imgUrl = cursor.getString(1);
					if (imgUrl.equals(orgImgUrl)) {
						cursor.close();
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cursor != null) {
			cursor.close();
		}
		return true;

	}

	public static void updateCoverDb(final List<ContentValues> values) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try {
					for(ContentValues v:values) {
						updateCoverTable(v, PrizeDatabaseHelper.getDatabase());
					}
				} catch (Exception e) {
				}
			}
		});
	}
	
	/***
	 * 插入或更新已安装应用表<br>
	 * 只针对 CoverTable表操作
	 * 
	 * @param cv
	 * @return 0: fail, else: success
	 */
	private static long updateCoverTable(ContentValues cv, SQLiteDatabase db) {
		try {
			
			String url = cv.getAsString(CoverTable.IMAGEURL);
			String selection = CoverTable.IMAGEURL+"=?";
			String selectionArgs[] = new String[]{url}; 
			Cursor cursor = db.query(CoverTable.TABLE_NAME_GAME, null, selection, selectionArgs, null, null, null);
			if(cursor ==null || cursor.getCount()<1){
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
				return db.insert(CoverTable.TABLE_NAME_GAME, null, cv);
			}
			else if(cursor.getCount()>0){
				return db.update(CoverTable.TABLE_NAME_GAME, cv, selection, selectionArgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 开始事务
	 */
	public void beginTransaction() {
		PrizeDatabaseHelper.beginTransaction();
	}

	/**
	 * 结束事务
	 */
	public void endTransaction() {
		PrizeDatabaseHelper.endTransaction();
	}

	/**
	 * 事务成功
	 */
	public void setTransactionSuccessful() {
		PrizeDatabaseHelper.setTransactionSuccessful();
	}
}
