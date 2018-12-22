package com.prize.appcenter.ui.datamgr;

import android.os.Message;

import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.search.SearchListNetSource;

/**
 * 请求搜索结果管理
 *
 * @author 龙宝修
 */

public class SearchResultListManager extends AbstractDataManager {
    public static final int SUCCESS = 1;
//    public static final int FAILE = SUCCESS + 1;

    private SearchListNetSource gameListNetSource;

//    private HashMap<String, ArrayList<GameBean>> mapData = new HashMap<String, ArrayList<GameBean>>();

//    private HashMap<String, Boolean> mapRequestImg = new HashMap<String, Boolean>();
//    private HashMap<String, GameListNetSource> mapNetSource = new HashMap<String, GameListNetSource>();

    public SearchResultListManager(DataManagerCallBack callback, String query) {
        super(callback);
        gameListNetSource = new SearchListNetSource(query);
        gameListNetSource.setListener(gameListListener);
    }

    public void setQuery(String query) {
        if (gameListNetSource != null) {
            gameListNetSource.setQuery(query);
        }
    }

    private DataManagerListener<PrizeAppsTypeData> gameListListener = new DataManagerListener<PrizeAppsTypeData>() {

        @Override
        protected Message onSuccess(int what, PrizeAppsTypeData data) {
            return Message.obtain(SearchResultListManager.this, SUCCESS, data);
        }

    };

    @Override
    protected void handleMessage(int what, int arg1, int arg2, Object obj) {
    }

    /**
     * 是否有下一页
     *
     * @return boolean
     */
    public boolean isListNextPage() {
        return gameListNetSource.hasNextPage();
    }

    public boolean isFirstPage() {
        return gameListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE;
    }

    public void getNewData(String requestType) {
        if (gameListNetSource != null) {
            gameListNetSource.setPage(new PageBean());
            gameListNetSource.doRequest(requestType);
        }
    }

    public void getNextListPage(String requestType) {

        if (gameListNetSource != null) {
            gameListNetSource.doRequest(requestType);
        }
    }

//    /**
//     * 排行类游戏列表是否为第一页
//     *
//     * @return
//     */
//    public boolean isListFirstPage(String listCode) {
//        if (mapNetSource.get(listCode).getCurrentPage() <= PageBean.FIRST_PAGE) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    public void setNullListener() {
        if (gameListNetSource != null) {
            gameListNetSource.setListener(null);
        }
    }

//    /**
//     * 获取排行列表数据
//     *
//     * @param listCode
//     * @return
//     */
//    public ArrayList<GameBean> getDate(String listCode) {
//        return listCode != null ? mapData.get(listCode) : null;
//    }

//	/**
//	 * 判断是否请求中
//	 *
//	 * @param listCode
//	 * @return
//	 */
//	public boolean getRequestIng(String listCode) {
//		if (listCode != null && mapRequestImg.get(listCode) != null) {
//			return mapRequestImg.get(listCode);
//		} else {
//			return false;
//		}
//	}

    public int getCurPageIndex() {
        return gameListNetSource.getCurrentPage();
    }
}
