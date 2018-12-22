package com.prize.appcenter.ui.datamgr;

import android.os.Message;

import com.prize.app.beans.GameBean;
import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.singlegame.AppDownRankListNetSource;
import com.prize.app.net.datasource.singlegame.GameListNetSource;
import com.prize.app.net.datasource.singlegame.RankListNetSource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 排行数据管理类
 *
 * @author prize
 */

public class RankingDataManager extends AbstractDataManager {
    public static final int GAME_SUCCESS_RANKING_TILEE = 1;
    public static final int APP_DOWN_SUCCESS_RANKING_LIST = GAME_SUCCESS_RANKING_TILEE + 1;
    public static final int GAME_FAILE_RANGKING_TITLE = APP_DOWN_SUCCESS_RANKING_LIST + 1;
    public static final int GAME_FAILE_RANGKING_LIST = GAME_FAILE_RANGKING_TITLE + 1;
    public static final int GAME_SUCCESS_RANKING_LIST_PAGE = GAME_FAILE_RANGKING_LIST + 1;
    public static final int GAME_SUCCESS_RANKING_LIST_MY = GAME_SUCCESS_RANKING_LIST_PAGE + 1;

    private String listCode = null;

    // private GameRankingNetSource gameRankingNetSource;
    private RankListNetSource gameListNetSource;
    private AppDownRankListNetSource appDowmRanlListNetSource;

    protected String rankType = null;
    private HashMap<String, ArrayList<GameBean>> mapData = new HashMap<String, ArrayList<GameBean>>();

    private HashMap<String, Boolean> mapRequestImg = new HashMap<String, Boolean>();
    private HashMap<String, GameListNetSource> mapNetSource = new HashMap<String, GameListNetSource>();

    public RankingDataManager(DataManagerCallBack callback) {
        super(callback);
        gameListNetSource = new RankListNetSource(false);
        gameListNetSource.setListener(gameListListener);
    }

    /**
     * @param callback DataManagerCallBack
     * @param rankType 榜单类型 1：应用 2：游戏 4:新品榜 8：流行榜
     * @param isGame   是否游戏
     */
    public RankingDataManager(DataManagerCallBack callback, String rankType,
                              boolean isGame) {
        super(callback);
        this.rankType = rankType;
        appDowmRanlListNetSource = new AppDownRankListNetSource(rankType,
                isGame);
        appDowmRanlListNetSource.setListener(appdownrankListListener);

    }

    /**
     * 排行(app，game推荐排行)
     *
     * @param requestType
     */
    public void getRankingListData(String requestType) {
        if (this.rankType == null) {
            gameListNetSource.doRequest(requestType);
        } else {
            appDowmRanlListNetSource.doRequest(requestType);

        }
    }

    public void reSetPagerIndex(int index) {
        if (appDowmRanlListNetSource == null) {
            gameListNetSource.reSetPagerIndex(index);
        } else {
            appDowmRanlListNetSource.reSetPagerIndex(index);
        }
    }

    public boolean isFirstPage() {
        if (appDowmRanlListNetSource == null)
            return gameListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE;

        return appDowmRanlListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE;
    }

    private DataManagerListener<PrizeAppsTypeData> appdownrankListListener = new DataManagerListener<PrizeAppsTypeData>() {

        @Override
        protected Message onSuccess(int what, PrizeAppsTypeData data) {
            return Message.obtain(RankingDataManager.this,
                    APP_DOWN_SUCCESS_RANKING_LIST, data);
        }

        @Override
        protected Message onFailed(int what) {
            mapRequestImg.put(listCode, false);
            return Message.obtain(RankingDataManager.this,
                    GAME_FAILE_RANGKING_LIST);
        }

    };
    private DataManagerListener<PrizeAppsTypeData> gameListListener = new DataManagerListener<PrizeAppsTypeData>() {

        @Override
        protected Message onSuccess(int what, PrizeAppsTypeData data) {
            return Message.obtain(RankingDataManager.this,
                    GAME_SUCCESS_RANKING_LIST_MY, data);
        }

        // @Override
        // protected Message onFailed(int what) {
        // mapRequestImg.put(listCode, false);
        // return Message.obtain(RankingDataManager.this,
        // GAME_FAILE_RANGKING_LIST);
        // }

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
        if (this.rankType == null) {
            return gameListNetSource.hasNextPage();
        }
        return appDowmRanlListNetSource.hasNextPage();
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

}
