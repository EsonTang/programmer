package com.prize.appcenter.ui.datamgr;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Message;

import com.prize.app.beans.GameBean;
import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.home.RequiedSoftListNetSource;
import com.prize.app.net.datasource.singlegame.GameListNetSource;

/**
 * 游戏礼包数据管理类
 * 
 * @author longbaoxiu
 * 
 */

public class GiftPkgManager extends AbstractDataManager {
	public static final int SUCCESS_LIST_PAGE = 1;
	public static final int FAILE_LIST_PAGE = SUCCESS_LIST_PAGE + 1;

	private String listCode = null;

	private RequiedSoftListNetSource gameListNetSource;

	protected String rankType = null;
	private HashMap<String, ArrayList<GameBean>> mapData = new HashMap<String, ArrayList<GameBean>>();

	private HashMap<String, Boolean> mapRequestImg = new HashMap<String, Boolean>();
	private HashMap<String, GameListNetSource> mapNetSource = new HashMap<String, GameListNetSource>();

	private boolean isGame = false;

	public GiftPkgManager(DataManagerCallBack callback, String appId) {
		super(callback);
		// this.callBack = callback;
		gameListNetSource = new RequiedSoftListNetSource(appId, false);
		gameListNetSource.setListener(gameListListener);
	}

	/**
	 * 排行(app，game推荐排行)
	 * 
	 * @param listCode
	 */
	public void getRankingListData(String requestType) {
		if (this.rankType == null) {
			gameListNetSource.doRequest(requestType);
		} else {

		}
	}

	private DataManagerListener<PrizeAppsTypeData> gameListListener = new DataManagerListener<PrizeAppsTypeData>() {

		@Override
		protected Message onSuccess(int what, PrizeAppsTypeData data) {
			return Message.obtain(GiftPkgManager.this, SUCCESS_LIST_PAGE, data);
		}

	};

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

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {

	}
}
