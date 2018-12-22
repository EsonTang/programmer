package com.prize.app.database.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.prize.app.database.DownloadGameTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.download.DownloadState;
import com.prize.app.download.DownloadTask;
import com.prize.app.download.DownloadTaskMgr;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.util.JLog;
import com.prize.onlinemusibean.SongDetailInfo;

public class GameDAO {
	private static final String TAG = "GameDAO";
	/** 游戏数据库表名 */
	public static String mColunmGameItemName[] = new String[] {
			DownloadGameTable.SONGS_QUALITY, DownloadGameTable.SONG_ID,
			DownloadGameTable.SONG_SINGER, DownloadGameTable.GAME_NAME,
			DownloadGameTable.GAME_ICON_URL,
			DownloadGameTable.GAME_VERSION_CODE,
			DownloadGameTable.GAME_APK_SIZE, DownloadGameTable.SONG_DOWN_URL,
			DownloadGameTable.GAME_DOWNLOAD_STATE,
			DownloadGameTable.GAME_LOADED_SIZE, DownloadGameTable.GAME_TYPE,
			DownloadGameTable.GAME_LOAD_FLAG, DownloadGameTable.GAME_TIME_STAP,
			DownloadGameTable.SONG_ALBUM_NAME, DownloadGameTable.SONG_ALBUM_ID };

	// 数据库的每项对应的列
	public static final int COLUMN_SONGS_QUALITY = 0;
	public static final int COLUMN_SONGS_ID = COLUMN_SONGS_QUALITY + 1;
	public static final int COLUMN_SONGS_SINGER = COLUMN_SONGS_ID + 1;
	public static final int COLUMN_GAME_NAME = COLUMN_SONGS_SINGER + 1;
	public static final int COLUMN_GAME_ICONURL = COLUMN_GAME_NAME + 1;
	public static final int COLUMN_GAME_VERSIONCODE = COLUMN_GAME_ICONURL + 1;
	public static final int COLUMN_GAME_APKSIZE = COLUMN_GAME_VERSIONCODE + 1;
	public static final int COLUMN_SONG_DOWN_URL = COLUMN_GAME_APKSIZE + 1;
	public static final int COLUMN_GAME_DOWNSTATE = COLUMN_SONG_DOWN_URL + 1;
	public static final int COLUMN_GAME_DOWNLOAD_POSITION = COLUMN_GAME_DOWNSTATE + 1;
	public static final int COLUMN_GAME_GAME_TYPE = COLUMN_GAME_DOWNLOAD_POSITION + 1;
	public static final int COLUMN_GAME_LOAD_FLAG = COLUMN_GAME_GAME_TYPE + 1;
	public static final int COLUMN_GAME_TIME_STAP = COLUMN_GAME_LOAD_FLAG + 1;
	public static final int COLUMN_SONG_ALBUM_NAME = COLUMN_GAME_TIME_STAP + 1;
	public static final int COLUMN_SONG_ALBUM_ID = COLUMN_SONG_ALBUM_NAME + 1;

	private static GameDAO instance;

	public static GameDAO getInstance() {
		if (instance == null) {
			synchronized (GameDAO.class) {
				if (instance == null) {
					instance = new GameDAO();
				}
			}
		}
		return instance;
	}

	/** 创建游戏下载任务 */
	public boolean insertGame(DownloadTask task) {
		if (null == task) {
			return false;
		}
		JLog.i(TAG, "新建的下载任务歌曲song_name=" + task.loadGame.song_name
				+ "--singers:" + task.loadGame.singers + "--artist_name:"
				+ task.loadGame.artist_name + "--album_name:"
						+ task.loadGame.album_name + "--album_id:"
				+ task.loadGame.album_id + "--album_logo:"
				+ task.loadGame.album_logo);
		SongDetailInfo gameBean = task.loadGame;
		StringBuffer sb = new StringBuffer();
		if (gameBean.permission != null && gameBean.permission.quality != null
				&& gameBean.permission.quality.length > 0) {
			String[] ary = gameBean.permission.quality;
			int length = ary.length;
			for (int i = 0; i < length; i++) {
				sb.append(ary[i]);
			}

		}

		String songs_quality = sb.toString();
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadGameTable.SONGS_QUALITY, songs_quality);
		contentValues.put(DownloadGameTable.SONG_ID, gameBean.song_id);
		contentValues.put(DownloadGameTable.SONG_SINGER, gameBean.singers);
		contentValues.put(DownloadGameTable.GAME_NAME, gameBean.song_name);
		contentValues.put(DownloadGameTable.SONG_ALBUM_NAME,gameBean.album_name);
		contentValues.put(DownloadGameTable.SONG_ALBUM_ID,gameBean.album_id);
		contentValues.put(DownloadGameTable.GAME_TIME_STAP, 0);
		if (gameBean != null && !TextUtils.isEmpty(gameBean.album_logo)) {
			contentValues.put(DownloadGameTable.GAME_ICON_URL,
					gameBean.album_logo);

		}
		if (!TextUtils.isEmpty(gameBean.listen_file)) {
			contentValues.put(DownloadGameTable.SONG_DOWN_URL,
					gameBean.listen_file);

		}
		contentValues.put(DownloadGameTable.GAME_DOWNLOAD_STATE,
				task.gameDownloadState);
		contentValues.put(DownloadGameTable.GAME_LOADED_SIZE,
				task.gameDownloadPostion);
		contentValues.put(DownloadGameTable.GAME_LOAD_FLAG, task.loadFlag);
		PrizeDatabaseHelper.insert(DownloadGameTable.TABLE_NAME_GAME, null,
				contentValues);

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
		SongDetailInfo gameBean = task.loadGame;
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadGameTable.SONG_ID, gameBean.song_id);
		contentValues.put(DownloadGameTable.SONG_SINGER, gameBean.singers);
		contentValues.put(DownloadGameTable.GAME_NAME, gameBean.song_name);

		if (gameBean != null && !TextUtils.isEmpty(gameBean.getAlbumLogo())) {
			contentValues.put(DownloadGameTable.GAME_ICON_URL,
					gameBean.getAlbumLogo());

		}
		// else {
		//
		// contentValues.put(DownloadGameTable.GAME_ICON_URL,
		// gameBean.getArtistLogo());
		// }
		// contentValues.put(DownloadGameTable.GAME_VERSION_CODE,
		// gameBean.versionCode);
		// contentValues.put(DownloadGameTable.GAME_APK_SIZE, gameBean.apkSize);
		contentValues
				.put(DownloadGameTable.SONG_DOWN_URL, gameBean.listen_file);
		contentValues.put(DownloadGameTable.GAME_LOAD_FLAG, task.loadFlag);

		String where = DownloadGameTable.SONG_ID + "=?";
		String[] args = new String[] { String.valueOf(gameBean.song_id) };

		PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
				contentValues, where, args);
	}

	/** 根据pkgname删除一个游戏数据 */
	public void deleteData(String pkgName) {
		PrizeDatabaseHelper.delete(DownloadGameTable.TABLE_NAME_GAME,
				DownloadGameTable.SONG_ID + "=?", new String[] { pkgName });
	}

	/**
	 * 方法描述：删除所有已下载app
	 */
	public void deleteAllData(String[] pkgNames) {
		PrizeDatabaseHelper.delete(DownloadGameTable.TABLE_NAME_GAME,
				DownloadGameTable.SONG_ID + "=?", pkgNames);
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
	public void updateState(int song_id, int state) {
		if ((DownloadState.STATE_DOWNLOAD_SUCESS == state)
				|| (DownloadState.STATE_DOWNLOAD_ERROR == state)
				|| (DownloadState.STATE_DOWNLOAD_PAUSE == state)) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DownloadGameTable.GAME_DOWNLOAD_STATE, state);
			PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
					contentValues, DownloadGameTable.SONG_ID + "=?",
					new String[] { String.valueOf(song_id) });
		}
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
	public void updateState(int song_id, int state, long time) {
		if ((DownloadState.STATE_DOWNLOAD_SUCESS == state)
				|| (DownloadState.STATE_DOWNLOAD_ERROR == state)
				|| (DownloadState.STATE_DOWNLOAD_PAUSE == state)) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DownloadGameTable.GAME_DOWNLOAD_STATE, state);
			// contentValues.put(DownloadGameTable.GAME_TIME_STAP, time);
			PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
					contentValues, DownloadGameTable.SONG_ID + "=?",
					new String[] { String.valueOf(song_id) });
		}
	}
	
	/**
	 * 
	 * @return SongDetailInfo
	 * @see
	 */
	public SongDetailInfo getSongById(int song_id) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		Cursor cursor = null;
		while (cursor == null||cursor.getCount() == 0) {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			cursor = PrizeDatabaseHelper.query(
					DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
					DownloadGameTable.SONG_ID + "=? and " + DownloadGameTable.GAME_DOWNLOAD_STATE + "=?",
					new String[] {String.valueOf(song_id), String
							.valueOf(DownloadState.STATE_DOWNLOAD_SUCESS) }, null,
					null, DownloadGameTable.GAME_TIME_STAP + " DESC");
		}
		cursor.moveToNext();
		SongDetailInfo loadGame = new SongDetailInfo();
		loadGame.song_id = cursor.getInt(COLUMN_SONGS_ID);
		loadGame.song_name = cursor.getString(COLUMN_GAME_NAME);
		loadGame.singers = cursor.getString(COLUMN_SONGS_SINGER);
		loadGame.songs_quality = cursor.getString(COLUMN_SONGS_QUALITY);
		loadGame.album_logo = cursor.getString(COLUMN_GAME_ICONURL);
		loadGame.album_name = cursor.getString(COLUMN_SONG_ALBUM_NAME);
		loadGame.listen_file = cursor.getString(COLUMN_SONG_DOWN_URL);
		loadGame.album_id = cursor.getInt(COLUMN_SONG_ALBUM_ID);
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		Log.i(TAG, loadGame.toString());
		return loadGame;
	}
	
	public void backupLocalId(int song_id, long uri_id) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DownloadGameTable.GAME_VERSION_CODE, uri_id);
			PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
					contentValues, DownloadGameTable.SONG_ID + "=?",
					new String[] { String.valueOf(song_id) });
	}
	
	public long getUriIdByOnlineSongId(long song_id) {
		Cursor cursor = null;
		long mUriId = -1;
		cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
				DownloadGameTable.SONG_ID + "=? and " + DownloadGameTable.GAME_DOWNLOAD_STATE + "=?",
				new String[] {String.valueOf(song_id), String
						.valueOf(DownloadState.STATE_DOWNLOAD_SUCESS) }, null,
				null, DownloadGameTable.GAME_TIME_STAP + " DESC");
		if (cursor!=null&&cursor.moveToFirst()&&cursor.getCount()>0) {
			try {
				mUriId = cursor.getLong(cursor.getColumnIndex(DownloadGameTable.GAME_VERSION_CODE));
			} catch (Exception e) {
				mUriId = -1;
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		Log.i(TAG, "[getUriIdByOnlineSongId] mUriId : " + mUriId);
		return mUriId;
	}

	/**
	 * 获取下载完成的任务最新的排列在前
	 * 
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public ArrayList<SongDetailInfo> getDownLoadedAppList() {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
				DownloadGameTable.GAME_DOWNLOAD_STATE + "=?",
				new String[] { String
						.valueOf(DownloadState.STATE_DOWNLOAD_SUCESS) }, null,
				null, DownloadGameTable.GAME_TIME_STAP + " DESC");
		if (cursor != null) {
			while (cursor.moveToNext()) {
				SongDetailInfo loadGame = new SongDetailInfo();
				loadGame.song_id = cursor.getInt(COLUMN_SONGS_ID);
				loadGame.song_name = cursor.getString(COLUMN_GAME_NAME);
				loadGame.singers = cursor.getString(COLUMN_SONGS_SINGER);
				loadGame.songs_quality = cursor.getString(COLUMN_SONGS_QUALITY);
				loadGame.album_logo = cursor.getString(COLUMN_GAME_ICONURL);
				loadGame.album_name = cursor.getString(COLUMN_SONG_ALBUM_NAME);
				loadGame.listen_file = cursor.getString(COLUMN_SONG_DOWN_URL);
				loadGame.album_id = cursor.getInt(COLUMN_SONG_ALBUM_ID);
			
				list.add(loadGame);
			}
			cursor.close();
		}
		return filterDeleteSong(list);
	}

	ArrayList<SongDetailInfo> filterDeleteSong(ArrayList<SongDetailInfo> items) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (items == null || items.size() <= 0)
			return list;
		int len = items.size();
		for (int index = 0; index < len; index++) {
			SongDetailInfo info = items.get(index);
			if (DownloadHelper.isFileExists(info)) {
				list.add(info);
			} else {
				AppManagerCenter.cancelDownload(info);
			}
		}
		return list;
	}

	/**
	 * 获取下载任务 下载中及待下载的的app
	 * 
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public ArrayList<SongDetailInfo> getDownAppList() {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
				DownloadGameTable.GAME_DOWNLOAD_STATE + "!=?",
				new String[] { String
						.valueOf(DownloadState.STATE_DOWNLOAD_SUCESS) }, null,
				null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				SongDetailInfo loadGame = new SongDetailInfo();
				loadGame.song_id = cursor.getInt(COLUMN_SONGS_ID);
				loadGame.song_name = cursor.getString(COLUMN_GAME_NAME);
				loadGame.singers = cursor.getString(COLUMN_SONGS_SINGER);
				loadGame.totalSize = cursor.getInt(COLUMN_GAME_APKSIZE);
				loadGame.listen_file = cursor.getString(COLUMN_SONG_DOWN_URL);
				list.add(loadGame);
			}
			cursor.close();
		}
		return list;
	}

	// /**
	// * 获取下载任务 下载中及待下载的的app
	// *
	// * @return ArrayList<SongDetailInfo>
	// * @see
	// */
	// public ArrayList<SongDetailInfo> getDownloadAppList() {
	// ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
	// /*
	// * String sql =
	// * "select * from table_game where download_state=0 or download_state=4;"
	// * ; Cursor cursor = PrizeDatabaseHelper.rawQuery(sql, null);
	// */
	// Cursor cursor = PrizeDatabaseHelper.query(
	// DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, null,
	// null, null, null, null);
	// if (cursor != null) {
	// while (cursor.moveToNext()) {
	// if (cursor.getLong(COLUMN_GAME_APKSIZE) != cursor
	// .getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
	// SongDetailInfo loadGame = new SongDetailInfo();
	// // loadGame.id = cursor.getString(COLUMN_GAME_ID);
	// // loadGame.gameActivity =
	// // cursor.getString(COLUMN_GAME_CLASS);
	// loadGame.packageName = cursor.getString(COLUMN_SONGS_ID);
	// loadGame.name = cursor.getString(COLUMN_GAME_NAME);
	// loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
	// loadGame.versionCode = cursor
	// .getInt(COLUMN_GAME_VERSIONCODE);
	// loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
	// loadGame.downloadUrl = cursor
	// .getString(COLUMN_SONG_DOWN_URL);
	// // loadGame.rating = cursor
	// // .getString(COLUMN_GAME_GAME_TYPE);
	// // int status = cursor.getInt(COLUMN_GAME_DOWNSTATE);
	// list.add(loadGame);
	// }
	// }
	// cursor.close();
	// }
	// return list;
	// }

	/**
	 * 方法描述：获取下载成功的apk
	 * 
	 * @return ArrayList<SongDetailInfo>
	 * @see 类名/完整类名/完整类名#方法名
	 */
	// public ArrayList<SongDetailInfo> getHasDownloadedAppList() {
	// ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
	// Cursor cursor = PrizeDatabaseHelper.query(
	// DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, null,
	// null, null, null, null);
	// if (cursor != null) {
	// while (cursor.moveToNext()) {
	// if (cursor.getLong(COLUMN_GAME_APKSIZE) == cursor
	// .getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
	// SongDetailInfo loadGame = new SongDetailInfo();
	// loadGame.id = Integer.parseInt(cursor
	// .getString(COLUMN_GAME_ID));
	// // loadGame.gameActivity =
	// // cursor.getString(COLUMN_GAME_CLASS);
	// loadGame.packageName = cursor
	// .getString(COLUMN_GAME_PACKAGE);
	// loadGame.name = cursor.getString(COLUMN_GAME_NAME);
	// loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
	// loadGame.versionCode = cursor
	// .getInt(COLUMN_GAME_VERSIONCODE);
	// loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
	// loadGame.downloadUrl = cursor
	// .getString(COLUMN_GAME_DOWNURL);
	// int status = cursor.getInt(COLUMN_GAME_DOWNSTATE);
	// list.add(loadGame);
	// }
	// }
	// }
	// return list;
	// }

	/**
	 * 更新下载进度
	 * 
	 * @param pkgName
	 * @param pos
	 * @return
	 */
	public void updateDownloadSize(String pkgName, long totalSize, long pos) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadGameTable.GAME_APK_SIZE, totalSize);
		contentValues.put(DownloadGameTable.GAME_LOADED_SIZE, pos);
		PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
				contentValues, DownloadGameTable.SONG_ID + "=?",
				new String[] { pkgName });
	}

	public long getDownloadSize(String pkgName) {
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
				DownloadGameTable.SONG_ID + "=?", new String[] { pkgName },
				null, null, null);
		int totalSize = 0;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				totalSize = cursor.getInt(COLUMN_GAME_APKSIZE);
			}
			cursor.close();
		}
		return totalSize;
	}

	public void updateBatchGameFlag(Iterator<Entry<String, DownloadTask>> ite,
			boolean isUser) {
		Entry<String, DownloadTask> entity;
		DownloadTask task;
		PrizeDatabaseHelper.beginTransaction();
		try {
			while (ite.hasNext()) {
				entity = ite.next();
				task = entity.getValue();
				if (null == task) {
					return;
				}
				task.pauseTask(isUser);
				ContentValues contentValues = new ContentValues();
				contentValues.put(DownloadGameTable.GAME_LOAD_FLAG,
						task.loadFlag);
				String pkgName = task.loadGame.song_id + "";
				PrizeDatabaseHelper.getDatabase().update(
						DownloadGameTable.TABLE_NAME_GAME, contentValues,
						DownloadGameTable.SONG_ID + "=?",
						new String[] { pkgName });

			}
			PrizeDatabaseHelper.setTransactionSuccessful();

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			PrizeDatabaseHelper.endTransaction();

		}

	}

	public void updateGameFlag(DownloadTask loadGameTask) {
		if (null == loadGameTask) {
			return;
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadGameTable.GAME_LOAD_FLAG,
				loadGameTask.loadFlag);
		String pkgName = loadGameTask.loadGame.song_id + "";

		PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
				contentValues, DownloadGameTable.SONG_ID + "=?",
				new String[] { pkgName });
	}

	// public void updateGameDownUrl(DownloadTask loadGameTask) {
	// if (null == loadGameTask) {
	// return;
	// }
	// ContentValues contentValues = new ContentValues();
	// contentValues.put(DownloadGameTable.SONG_DOWN_URL,
	// loadGameTask.loadGame.listen_file);
	// String pkgName = loadGameTask.loadGame.song_id + "";
	//
	// PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
	// contentValues, DownloadGameTable.SONG_ID + "=?",
	// new String[] { pkgName });
	// }

	/**
	 * 
	 * 获取所有的下载任务
	 * 
	 * @return HashMap<String,DownloadTask>
	 * @author prize
	 */
	public HashMap<String, DownloadTask> getAllDownloadExeTask() {
		HashMap<String, DownloadTask> dataMap = new HashMap<String, DownloadTask>();
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName,
				DownloadGameTable.GAME_DOWNLOAD_STATE + "!=?",
				new String[] { String
						.valueOf(DownloadState.STATE_DOWNLOAD_SUCESS) }, null,
				null, null);
		// Cursor cursor = PrizeDatabaseHelper.query(
		// DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, null,
		// null, null, null, null);
		DownloadTask task;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				task = sqlToDownloadTask(cursor);
				dataMap.put(String.valueOf(task.loadGame.song_id), task);
			}
			cursor.close();
		}

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
		SongDetailInfo loadGame = data.loadGame;
		loadGame.song_id = cursor.getInt(COLUMN_SONGS_ID);
		loadGame.song_name = cursor.getString(COLUMN_GAME_NAME);
		loadGame.album_name = cursor.getString(COLUMN_SONG_ALBUM_NAME);
		loadGame.album_id = cursor.getInt(COLUMN_SONG_ALBUM_ID);
		loadGame.singers = cursor.getString(COLUMN_SONGS_SINGER);
		loadGame.album_logo = cursor.getString(COLUMN_GAME_ICONURL);
		loadGame.totalSize = cursor.getInt(COLUMN_GAME_APKSIZE);
		loadGame.listen_file = cursor.getString(COLUMN_SONG_DOWN_URL);
		data.gameDownloadState = cursor.getInt(COLUMN_GAME_DOWNSTATE);
		if ((DownloadState.STATE_DOWNLOAD_WAIT == data.gameDownloadState)
				|| DownloadState.STATE_DOWNLOAD_START_LOADING == data.gameDownloadState) {
			// 如果是wait or
			// loading的状态，修改成暂停。因为APP已经被退出了，另外，兼容之前的版本，之前记录loading的状态，需要转换回来
			data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
		}
		int loadsize = (int) cursor.getLong(COLUMN_GAME_DOWNLOAD_POSITION);
		data.setDownloadSize(loadGame.totalSize, loadsize);
		data.loadFlag = cursor.getInt(COLUMN_GAME_LOAD_FLAG);
		return data;
	}

	// /**
	// * 转换为下载任务
	// *
	// * @param cursor
	// * @return
	// */
	// private DownloadTask sqlToDownloadTask(Cursor cursor) {
	// DownloadTask data = new DownloadTask();
	// SongDetailInfo loadGame = data.loadGame;
	// loadGame.id = Integer.parseInt(cursor.getString(COLUMN_GAME_ID));
	// // loadGame.gameActivity = cursor.getString(COLUMN_GAME_CLASS);
	// loadGame.packageName = cursor.getString(COLUMN_GAME_PACKAGE);
	// loadGame.name = cursor.getString(COLUMN_GAME_NAME);
	// loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
	// loadGame.versionCode = cursor.getInt(COLUMN_GAME_VERSIONCODE);
	//
	// long sz = cursor.getLong(COLUMN_GAME_APKSIZE);
	//
	// loadGame.apkSize = String.valueOf(sz);
	// loadGame.downloadUrl = cursor.getString(COLUMN_GAME_DOWNURL);
	// data.gameDownloadState = cursor.getInt(COLUMN_GAME_DOWNSTATE);
	// if ((DownloadState.STATE_DOWNLOAD_WAIT == data.gameDownloadState)
	// || DownloadState.STATE_DOWNLOAD_START_LOADING == data.gameDownloadState)
	// {
	// // 如果是wait or
	// // loading的状态，修改成暂停。因为APP已经被退出了，另外，兼容之前的版本，之前记录loading的状态，需要转换回来
	// data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
	// }
	// // added by fanjunchen for state is error.
	// else if (DownloadState.STATE_DOWNLOAD_SUCESS == data.gameDownloadState) {
	// String path = FileUtils.getDownloadAppFilePath(String
	// .valueOf(loadGame.id));
	// File apk = new File(path);
	// if (apk.exists()) {
	// if (sz != apk.length())
	// data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
	// } else {
	// apk = null;
	// path = FileUtils.getDownloadTmpFilePath(String
	// .valueOf(loadGame.id));
	// apk = new File(path); // 下载的临时文件
	// if (apk.exists()) {
	// if (sz != apk.length())
	// data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
	// }
	// // else
	// // data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
	// apk = null;
	// }
	// }
	// // end added fanjunchen
	//
	// int loadsize = (int) cursor.getLong(COLUMN_GAME_DOWNLOAD_POSITION);
	// data.setDownloadSize(Integer.parseInt(loadGame.apkSize), loadsize);
	// // loadGame.gameType = (byte) cursor.getInt(COLUMN_GAME_GAME_TYPE);
	//
	// data.loadFlag = cursor.getInt(COLUMN_GAME_LOAD_FLAG);
	// return data;
	// }

	// /*** * 更新应用的URL * @param url * @param pkg */
	// public final void updateDownUrl(final String url, final String pkg) {
	// ContentValues contentValues = new ContentValues();
	// contentValues.put(DownloadGameTable.SONG_DOWN_URL, url);
	// PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
	// contentValues, DownloadGameTable.SONG_ID + "=?",
	// new String[] { pkg });
	// }

	/***
	 * 
	 * @param pkg
	 * @return String
	 * @see
	 */
	public final String getAppDownUrl(String pkg) {
		String result = null;
		Cursor cursor = PrizeDatabaseHelper.query(
				DownloadGameTable.TABLE_NAME_GAME,
				new String[] { DownloadGameTable.SONG_DOWN_URL },
				DownloadGameTable.SONG_ID + "=?", new String[] { pkg }, null,
				null, null);
		if (cursor != null) {
			if (cursor.moveToNext()) {
				result = cursor.getString(0);
			}
			cursor.close();
		}
		return result;
	}

	/**
	 * 批量删除数据库信息
	 * 
	 * @param infos
	 * @return void
	 */
	public void deleteDataBatch(ArrayList<SongDetailInfo> infos) {
		PrizeDatabaseHelper.beginTransaction();
		for (SongDetailInfo info : infos) {
			PrizeDatabaseHelper.deleteGame(DownloadGameTable.TABLE_NAME_GAME,
					DownloadGameTable.SONG_ID + "=?",
					new String[] { String.valueOf(info.song_id) });
		}
		PrizeDatabaseHelper.setTransactionSuccessful();
		PrizeDatabaseHelper.endTransaction();

	}
}