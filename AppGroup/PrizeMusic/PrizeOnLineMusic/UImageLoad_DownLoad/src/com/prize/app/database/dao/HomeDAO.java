package com.prize.app.database.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Navbars;
import com.prize.app.beans.Navblocks;
import com.prize.app.database.HomeTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.onlinemusibean.AlbumsBean;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.RadioSceneBean;
import com.prize.onlinemusibean.RecomendRankBean;

/**
 **
 * 操作首页相关数据库的DAO
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeDAO {

	private static final String COLLUMS[] = new String[] { HomeTable.HOME_CODE,
			HomeTable.HOME_CONTENT_TYPE, HomeTable.HOME_DESC,
			HomeTable.HOME_IMG, HomeTable.HOME_TYPE, HomeTable.HOME_NAME,
			HomeTable.HOME_GIFTCOUNT, };

	/**
	 * 取所有广告内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<HomeAdBean> getADRecords() {
		ArrayList<HomeAdBean> ads = new ArrayList<HomeAdBean>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { "" + HomeRecord.CONTENT_TYPE_AD };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			HomeAdBean record = null;
			while (cursor.moveToNext()) {
				record = new HomeAdBean();
				record.pic_url_yasha = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG));
				record.url = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_TYPE));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {
		}

		return ads;
	}

	/**
	 * 根据类型取今日推荐歌单内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<SongDetailInfo> getRecordsDailySongs() {
		ArrayList<SongDetailInfo> ads = new ArrayList<SongDetailInfo>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_DAILY_SONGS };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			SongDetailInfo record = null;
			while (cursor.moveToNext()) {
				record = new SongDetailInfo();
				record.setSongId(cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE)));
				record.setSongName(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME)));
				record.setAlbumLogo(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG)));
				record.setSingers(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_DESC)));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {
		}

		return ads;
	}

	/**
	 * 根据类型取新碟上架内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<AlbumsBean> getRecordNewAlbums() {
		ArrayList<AlbumsBean> ads = new ArrayList<AlbumsBean>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_NEW_ALBUMS_LIST };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			AlbumsBean record = null;
			while (cursor.moveToNext()) {
				record = new AlbumsBean();
				record.album_id = cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE));
				record.album_logo = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG));
				record.album_name = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME));
				record.artist_name = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_DESC));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {
		}

		return ads;
	}

	/**
	 * 根据类型取热门歌曲内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<SongDetailInfo> getRecordsHotSong() {
		ArrayList<SongDetailInfo> ads = new ArrayList<SongDetailInfo>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_HOT_SONGS_LIST };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			SongDetailInfo record = null;
			while (cursor.moveToNext()) {
				record = new SongDetailInfo();
				record.setSongId(cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE)));
				record.setSongName(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME)));
				record.setAlbumLogo(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG)));
				record.setSingers(cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_DESC)));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {

		}

		return ads;
	}

	/**
	 * 根据类型取热门collect内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<CollectBean> getRecordsCollect() {
		ArrayList<CollectBean> ads = new ArrayList<CollectBean>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_RECOMMEND_COLLECT };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			CollectBean record = null;
			while (cursor.moveToNext()) {
				record = new CollectBean();
				record.list_id = cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE));
				record.play_count = cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_GIFTCOUNT));
				record.collect_name = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME));
				record.collect_logo = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {

		}

		return ads;
	}

	/**
	 * 根据类型取场景音乐内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<RadioSceneBean> getRecordsSceneSongs() {
		ArrayList<RadioSceneBean> ads = new ArrayList<RadioSceneBean>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_SCENE_SONGS };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			RadioSceneBean record = null;
			while (cursor.moveToNext()) {
				record = new RadioSceneBean();
				record.radio_id = cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE));
				record.title = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME));
				record.logo = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {

		}

		return ads;
	}

	/**
	 * 根据类型取音乐排行榜内容
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<SongDetailInfo> getRecordsRankSongs() {
		ArrayList<SongDetailInfo> ads = new ArrayList<SongDetailInfo>();
		String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
		String[] selectionArgs = new String[] { ""
				+ HomeRecord.CONTENT_TYPE_HOTSONG_RANK };
		Cursor cursor = PrizeDatabaseHelper.query(HomeTable.TABLE_NAME_GAME,
				COLLUMS, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return ads;
		}
		try {
			SongDetailInfo record = null;
			while (cursor.moveToNext()) {
				record = new SongDetailInfo();
				record.song_id = cursor.getInt(cursor
						.getColumnIndex(HomeTable.HOME_CODE));
				record.song_name = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_NAME));
				record.album_logo = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_IMG));
				record.singers = cursor.getString(cursor
						.getColumnIndex(HomeTable.HOME_DESC));
				ads.add(record);
			}
			cursor.close();
		} catch (Exception e) {

		}

		return ads;
	}

	/**
	 * 添加
	 * 
	 * @param record
	 * @return
	 */
	public void insert(HomeRecord record) {

		ContentValues contentValues = new ContentValues();
		contentValues.put(HomeTable.HOME_CODE, record.id);
		if (!TextUtils.isEmpty(record.name)) {
			contentValues.put(HomeTable.HOME_NAME, record.name);

		}
		if (!TextUtils.isEmpty(record.adType)) {
			contentValues.put(HomeTable.HOME_TYPE, record.adType);

		}
		contentValues.put(HomeTable.HOME_IMG, record.iconUrl);
		contentValues.put(HomeTable.HOME_GIFTCOUNT, record.playCount);

		if (!TextUtils.isEmpty(record.subTitle)) {
			contentValues.put(HomeTable.HOME_DESC, record.subTitle);

		}

		contentValues.put(HomeTable.HOME_CONTENT_TYPE, record.contentType);

		PrizeDatabaseHelper.insert(HomeTable.TABLE_NAME_GAME, null,
				contentValues);

	}

	/**
	 * 删除所有广告
	 */
	public void deleteAllAD() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_AD + "" });
	}

	/**
	 * 删除今日推荐歌单
	 */
	public void deleteDailySongs() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_DAILY_SONGS + "" });
	}

	/**
	 * 删除新碟上架
	 */
	public void deleteNewAlbums() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_NEW_ALBUMS_LIST + "" });
	}

	/**
	 * 删除热门歌曲
	 */
	public void deleteHotSongs() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_HOT_SONGS_LIST + "" });
	}

	/**
	 * 删除今日推荐歌单
	 */
	public void deleteRecommendCollect() {
		PrizeDatabaseHelper
				.delete(HomeTable.TABLE_NAME_GAME,
						HomeTable.HOME_CONTENT_TYPE + "=?",
						new String[] { HomeRecord.CONTENT_TYPE_RECOMMEND_COLLECT
								+ "" });
	}

	/**
	 * 删除场景音乐
	 */
	public void deleteSceneSongs() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_SCENE_SONGS + "" });
	}

	/**
	 * 删除榜单
	 */
	public void deleteHotSongRank() {
		PrizeDatabaseHelper.delete(HomeTable.TABLE_NAME_GAME,
				HomeTable.HOME_CONTENT_TYPE + "=?",
				new String[] { HomeRecord.CONTENT_TYPE_HOTSONG_RANK + "" });
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
