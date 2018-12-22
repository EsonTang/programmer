package com.prize.app.net.datasource.home;

import com.prize.app.database.beans.HomeRecord;
import com.prize.app.database.dao.HomeDAO;

import java.util.List;

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
	 */
	public static void initDataBase() {
		getHomeDAO();
	}

	/**
	 * 获取DownloadDAO, 防止操作数据失败
	 * 
	 * @return  HomeDAO
	 */
	private static HomeDAO getHomeDAO() {
		synchronized (HomeDataSource.class) {
			if (homeDAO == null) {
				homeDAO = new HomeDAO();
			}
		}
		return homeDAO;
	}

//	public static void destroyHomeDataBase() {
//		if (null != homeDAO) {
//			homeDAO = null;
//		}
//	}

	/**
	 * 替换主页内容
	 * 
	 * @param records
	 * <br/>
	 *            {@link HomeRecord#CONTENT_TYPE_AD}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_LIST}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_NOTICE}<br/>
	 *            {@link HomeRecord#CONTENT_TYPE_NAVBLOCKS}
	 * @return boolean
	 */
	public static boolean replaceHomeRecords(List<HomeRecord> records,
			int recordType) {
		HomeDAO dao = getHomeDAO();
		dao.beginTransaction();
		try {
			if (recordType == HomeRecord.CONTENT_TYPE_LIST)
				dao.deleteAllGame();
			for (HomeRecord record : records) {
				dao.insert(record);
			}
			dao.setTransactionSuccessful();
			return true;
		} finally {
			dao.endTransaction();
		}
	}

//	/**
//	 * 取游戏列表
//	 * 
//	 * @return
//	 */
//	public static ArrayList<AppsItemBean> getGameList() {
//		ArrayList<AppsItemBean> gameList = getHomeDAO().getGameList();
//		return gameList;
//	}
	
	/**
	 * 取首页缓存数据
	 * 
	 * @return  String
	 */
	public static String getListJson() {	
		return getHomeDAO().getJsonList();
	}
}
