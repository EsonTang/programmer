package com.prize.app.net.datasource.home;

import java.util.ArrayList;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Navbars;
import com.prize.app.beans.Navblocks;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.database.dao.HomeDAO;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.onlinemusibean.AlbumsBean;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.RadioSceneBean;

/**
 ** 操作首页数据库（保存主页最新的内容，等操作）
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeDataSource {
	private static HomeDAO homeDAO = null;

	/**
	 * 初始化首页数据库
	 * 
	 * @return void
	 * @see
	 */
	public static void initDataBase() {
		getHomeDAO();
	}

	/**
	 * 获取DownloadDAO, 防止操作数据失败
	 * 
	 * @return
	 */
	private static HomeDAO getHomeDAO() {
		synchronized (HomeDataSource.class) {
			if (homeDAO == null) {
				homeDAO = new HomeDAO();
			}
		}
		return homeDAO;
	}

	// public static void destroyHomeDataBase() {
	// if (null != homeDAO) {
	// homeDAO = null;
	// }
	// }

	/**
	 * 替换主页内容
	 * 
	 * @param records
	 * <br/>
	 *            {@link HomeRecord#CONTENT_TYPE_AD}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_new_albums_LIST}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_DAILY_SONGS}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_RECOMMEND_COLLECT}
	 * @return
	 */
	public static boolean replaceHomeRecords(ArrayList<HomeRecord> records,
			int recordType) {
		HomeDAO dao = getHomeDAO();
		dao.beginTransaction();
		try {
			if (recordType == HomeRecord.CONTENT_TYPE_AD) {
				dao.deleteAllAD();
			} else if (recordType == HomeRecord.CONTENT_TYPE_NEW_ALBUMS_LIST) {
				dao.deleteNewAlbums();
			} else if (recordType == HomeRecord.CONTENT_TYPE_SCENE_SONGS) {
				dao.deleteSceneSongs();
			} else if (recordType == HomeRecord.CONTENT_TYPE_DAILY_SONGS) {
				dao.deleteDailySongs();
			} else if (recordType == HomeRecord.CONTENT_TYPE_HOT_SONGS_LIST) {
				dao.deleteHotSongs();
			} else if (recordType == HomeRecord.CONTENT_TYPE_RECOMMEND_COLLECT) {
				dao.deleteRecommendCollect();
			} else if (recordType == HomeRecord.CONTENT_TYPE_HOTSONG_RANK) {
				dao.deleteHotSongRank();

			}
			for (HomeRecord record : records) {
				dao.insert(record);
			}
			dao.setTransactionSuccessful();
			return true;
		} finally {
			dao.endTransaction();
		}
	}

	/**
	 * 取广告
	 * 
	 * @return
	 */
	public static ArrayList<HomeAdBean> getADs() {
		return getHomeDAO().getADRecords();
	}

	/**
	 * 根据类型取今日推荐歌单内容
	 * 
	 * @return
	 */
	public static ArrayList<SongDetailInfo> getRecordsDailySongs() {
		return getHomeDAO().getRecordsDailySongs();
	}

	/**
	 * 取新碟上架内容
	 * 
	 * @return
	 */
	public static ArrayList<AlbumsBean> getRecordNewAlbums() {
		return getHomeDAO().getRecordNewAlbums();
	}

	/**
	 * 取热门歌曲内容
	 * 
	 * @return
	 */
	public static ArrayList<SongDetailInfo> getRecordsHotSong() {
		return getHomeDAO().getRecordsHotSong();
	}

	/**
	 * 取热门collect内容
	 * 
	 * @return
	 */
	public static ArrayList<CollectBean> getRecordsCollect() {
		return getHomeDAO().getRecordsCollect();
	}

	/**
	 * 取场景音乐内容
	 * 
	 * @return
	 */
	public static ArrayList<RadioSceneBean> getRecordsSceneSongs() {
		return getHomeDAO().getRecordsSceneSongs();
	}

	/**
	 * 根据类型取音乐排行榜内容
	 * 
	 * @return
	 */
	public static ArrayList<SongDetailInfo> getRecordsRankSongs() {
		return getHomeDAO().getRecordsRankSongs();
	}
}
