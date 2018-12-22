package com.prize.appcenter.ui.datamgr;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Message;

import com.prize.app.beans.GameBean;
import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.base.PrizeTopicTypeData;
import com.prize.app.net.datasource.base.TopicDetailNetSource;
import com.prize.app.net.datasource.base.TopicNetSource;
import com.prize.app.net.datasource.singlegame.GameListNetSource;

/**
 * 专题数据管理类
 * 
 * @author longbaoxiu
 * 
 */

public class TopicManager extends AbstractDataManager {
	public static final int GET_TOPIC_DETAIL_SUCCESS = 1;
	public static final int GET_TOPIC_DETAIL_FAILE = GET_TOPIC_DETAIL_SUCCESS + 1;
	public static final int TOPIC_FAILE_LIST = GET_TOPIC_DETAIL_FAILE + 1;
	public static final int TOPIC_SUCCESS__LIST = TOPIC_FAILE_LIST + 1;

	private String listCode = null;

	private TopicNetSource gameListNetSource;
	private TopicDetailNetSource mTopicDetailNetSource;

	private HashMap<String, ArrayList<GameBean>> mapData = new HashMap<String, ArrayList<GameBean>>();

	private HashMap<String, Boolean> mapRequestImg = new HashMap<String, Boolean>();
	private HashMap<String, GameListNetSource> mapNetSource = new HashMap<String, GameListNetSource>();

	/**
	 * 构造函数
	 * 
	 * @param appTypeId
	 *            专题Id 类型：1 - 应用；2 - 游戏
	 * @param topicId
	 */
	public TopicManager(DataManagerCallBack callback, String appTypeId,
			String topicId) {
		super(callback);
		// this.callBack = callback;
		if (topicId != null) {
			mTopicDetailNetSource = new TopicDetailNetSource(appTypeId, topicId);
			mTopicDetailNetSource.setListener(appdownrankListListener);
		} else {
			gameListNetSource = new TopicNetSource(appTypeId, topicId);
			gameListNetSource.setListener(gameListListener);
		}
	}

	/**
	 * 专题列表
	 * 
	 * @param requestType
	 */
	public void getTopicListData(String requestType) {
		gameListNetSource.doRequest(requestType);
	}

	/**
	 * 专题列表
	 * 
	 * @param requestType
	 */
	public void getTopicDetailData(String requestType) {
		mTopicDetailNetSource.doRequest(requestType);
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

	// private DataManagerListener<GameTypeData> gameTypeListener = new
	// DataManagerListener<GameTypeData>() {
	//
	// @Override
	// protected Message onSuccess(int what, GameTypeData data) {
	// titles = data.items;
	// String title = JSON.toJSONString(data);
	// RankDataSouce.setTitle(title);
	// return Message.obtain(TopicManager.this, GET_TOPIC_DETAIL_SUCCESS,
	// data.items);
	// }
	//
	// @Override
	// protected Message onFailed(int what) {
	// return Message.obtain(TopicManager.this, GAME_FAILE_RANGKING_TITLE);
	// }
	//
	// };

	private DataManagerListener<PrizeAppsTypeData> appdownrankListListener = new DataManagerListener<PrizeAppsTypeData>() {

		@Override
		protected Message onSuccess(int what, PrizeAppsTypeData data) {
			return Message.obtain(TopicManager.this, GET_TOPIC_DETAIL_SUCCESS,
					data);
		}

		@Override
		protected Message onFailed(int what) {
			mapRequestImg.put(listCode, false);
			return Message.obtain(TopicManager.this, GET_TOPIC_DETAIL_FAILE);
		}

	};
	private DataManagerListener<PrizeTopicTypeData> gameListListener = new DataManagerListener<PrizeTopicTypeData>() {

		@Override
		protected Message onSuccess(int what, PrizeTopicTypeData data) {
			return Message.obtain(TopicManager.this, TOPIC_SUCCESS__LIST, data);
		}

		// @Override
		// protected Message onFailed(int what) {
		// // mapRequestImg.put(listCode, false);
		// return Message.obtain(TopicManager.this, TOPIC_FAILE_LIST);
		// }

	};

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
		// switch (what) {
		// case TOPIC_FAILE_LIST:
		// // ArrayList<GameBean> cacheList = cache.getGameRanking(listCode);
		// // if (cacheList != null) {
		// // mapData.put(listCode, cacheList);
		// // }
		// break;
		//
		// case TOPIC_SUCCESS__LIST:
		// PrizeTopicTypeData data = (PrizeTopicTypeData) obj;
		// // if (data != null && data.listCode != null) {
		// // String code = data.listCode;
		// // mapRequestImg.put(code, false);
		// // cache.putGameRankingPage(code, mapNetSource.get(code).getPage());
		// // if (isListFirstPage(code)) {
		// // cache.putGameRanking(code, data.items);
		// // String listjson = null;
		// // try {
		// // listjson = JSON.toJSONString(data);
		// // } catch (Exception e) {
		// // }
		// // RankDataSouce.setListJson(code, listjson);
		// // } else {
		// // cache.addMoreGameRanking(code, data.items);
		// // }
		// // ArrayList<GameBean> cacheLists = cache.getGameRanking(code);
		// // if (cacheLists != null) {
		// // mapData.put(code, cacheLists);
		// // }
		// // }
		// break;
		// case GET_TOPIC_DETAIL_SUCCESS:
		// break;
		// case GET_TOPIC_DETAIL_FAILE:
		// break;
		// }
	}

	// /**
	// * 获取排行头数据
	// */
	// public void doRankingTitleRequest() {
	// if (titles != null && titles.size() == 0) {
	// String titlejson = RankDataSouce.getTitleJson();
	// GameTypeData data = null;
	// try {
	// data = JSON.parseObject(titlejson, GameTypeData.class);
	// } catch (Exception e) {
	// }
	// if (data != null && data.items != null)
	// titles = data.items;
	// if (titles.size() > 0) {
	// callBack.onBack(GAME_SUCCESS_RANKING_TILEE, 0, 0, null);
	// }
	// }
	// // gameRankingNetSource.doRequest();
	// }

	// /**
	// * 获取排行相应的列表
	// *
	// * @param isMore是否是滑动加载更多
	// * @return
	// */
	// public boolean doListGameRequest(boolean isScrollAddMore) {
	// boolean hasNext = true;
	// PageBean page = cache.getGameRankingPage(listCode);
	// // 如果是首次请求，那么先使用DB保存的历史数据
	// // if (page == null && isScrollAddMore == false) {
	// // String listdata = RankDataSouce.getListJson(listCode);
	// // GameData data = null;
	// // try {
	// // data = JSON.parseObject(listdata, GameData.class);
	// // } catch (Exception e) {
	// //
	// // }
	// // if (data != null && data.items != null) {
	// // mapData.put(listCode, data.items);
	// // }
	// // callBack.onBack(GAME_SUCCESS_RANKING_LIST_PAGE, 0, 0, null);
	// // }
	// if (page != null) {
	// hasNext = page.hasNext();
	// }
	// if ((hasNext && isScrollAddMore) || cache.isGameRankNull(listCode)) {
	// if (page != null) {// 缓存进来重写设置page，解决需多次加载才能显示数据问题
	// mapNetSource.get(listCode).setPage(
	// cache.getGameRankingPage(listCode));
	// }
	// if (mapRequestImg.get(listCode) != null
	// && mapRequestImg.get(listCode) == false) {
	// mapRequestImg.put(listCode, true);
	// mapNetSource.get(listCode).doRequest();
	// }
	// return true;
	// } else {
	// Message msg = new Message();
	// msg.what = APP_DOWN_SUCCESS_RANKING_LIST;
	// sendMessageDelayed(msg, 100);
	// return false;
	// }
	// }

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
