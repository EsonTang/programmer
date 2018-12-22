package com.prize.appcenter.ui.datamgr;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Message;

import com.prize.app.beans.GameBean;
import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.home.NewAppListNetSource;
import com.prize.app.net.datasource.singlegame.GameListNetSource;

/**
 * 新品数据管理类
 * 
 * @author prize
 * 
 */

public class NewProductManager extends AbstractDataManager {
	public static final int GAME_SUCCESS_RANKING_TILEE = 1;
	public static final int APP_DOWN_SUCCESS_RANKING_LIST = GAME_SUCCESS_RANKING_TILEE + 1;
	public static final int GAME_FAILE_RANGKING_TITLE = APP_DOWN_SUCCESS_RANKING_LIST + 1;
	public static final int GAME_FAILE_RANGKING_LIST = GAME_FAILE_RANGKING_TITLE + 1;
	public static final int GAME_SUCCESS_RANKING_LIST_PAGE = GAME_FAILE_RANGKING_LIST + 1;
	public static final int GAME_SUCCESS_RANKING_LIST_MY = GAME_SUCCESS_RANKING_LIST_PAGE + 1;

	private String listCode = null;

	// private GameRankingNetSource gameRankingNetSource;
	private NewAppListNetSource gameListNetSource;

	protected String rankType = null;
	private HashMap<String, ArrayList<GameBean>> mapData = new HashMap<String, ArrayList<GameBean>>();

	private HashMap<String, Boolean> mapRequestImg = new HashMap<String, Boolean>();
	private HashMap<String, GameListNetSource> mapNetSource = new HashMap<String, GameListNetSource>();

	public NewProductManager(DataManagerCallBack callback) {
		super(callback);
		gameListNetSource = new NewAppListNetSource(null, false);
		gameListNetSource.setListener(gameListListener);
	}

	/**
	 * 排行(app，game推荐排行)
	 * 
	 * @param listCode
	 */
	public void getRankingListData(String requestType) {
		// gameListNetSource = new GameListNetSource(listCode);
		// gameListNetSource.setListener(gameListListener);
		if (this.rankType == null) {
			gameListNetSource.doRequest(requestType);
		} else {
			// appDowmRanlListNetSource.doRequest();

		}
		// if (mapRequestImg.get(listCode) == null) {
		// mapRequestImg.put(listCode, false);
		// }
		// if (mapNetSource.get(listCode) == null) {
		// mapNetSource.put(listCode, gameListNetSource);
		// }
	}

	// /**
	// * 排行相应游戏列表
	// *
	// * @param listCode
	// */
	// public void setListCode(String listCode) {
	// gameListNetSource = new GameListNetSource(listCode);
	// gameListNetSource.setListener(gameListListener);
	// if (mapRequestImg.get(listCode) == null) {
	// mapRequestImg.put(listCode, false);
	// }
	// if (mapNetSource.get(listCode) == null) {
	// mapNetSource.put(listCode, gameListNetSource);
	// }
	// this.listCode = listCode;
	// }

	private DataManagerListener<PrizeAppsTypeData> gameListListener = new DataManagerListener<PrizeAppsTypeData>() {

		@Override
		protected Message onSuccess(int what, PrizeAppsTypeData data) {
			return Message.obtain(NewProductManager.this,
					GAME_SUCCESS_RANKING_LIST_MY, data);
		}

	};

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case APP_DOWN_SUCCESS_RANKING_LIST:
			break;

		case GAME_SUCCESS_RANKING_LIST_MY:
			break;
		}
	}

	/**
	 * 排行类游戏列表是否有下一页
	 * 
	 * @return
	 */
	public boolean isListNextPage() {
		return gameListNetSource.hasNextPage();

	}

	/**
	 * 排行类游戏列表是否为第一页
	 * 
	 * @return
	 */
	public boolean isListFirstPage(String listCode) {
		if (mapNetSource.get(listCode).getCurrentPage() <= PageBean.FIRST_PAGE) {
			return true;
		} else {
			return false;
		}
	}

	public void setNullListener() {
		if (mapNetSource.get(listCode) != null) {
			mapNetSource.get(listCode).setListener(null);
		}
		if (gameListNetSource != null) {
			gameListNetSource.setListener(null);
		}
	}

	/**
	 * 获取排行列表数据
	 * 
	 * @param listCode
	 * @return
	 */
	public ArrayList<GameBean> getDate(String listCode) {
		return listCode != null ? mapData.get(listCode) : null;
	}

	/**
	 * 判断是否请求中
	 * 
	 * @param listCode
	 * @return
	 */
	public boolean getRequestIng(String listCode) {
		if (listCode != null && mapRequestImg.get(listCode) != null) {
			return mapRequestImg.get(listCode);
		} else {
			return false;
		}
	}

	// public int getCurPageId() {
	// return mapNetSource.get(listCode).getCurrentPage();
	// }
}
