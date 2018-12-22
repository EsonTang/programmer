package com.prize.appcenter.ui.datamgr;

import android.os.Message;

import com.prize.app.beans.PageBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.net.datasource.home.HomeAppListNetSource;

/**
 * *
 * 首页数据请求管理类
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeDataManager extends AbstractDataManager {
    public static final int WHAT_SUCESS_RECOMMAND = 1;
    public static final int WHAT_FAILED_RECOMMAND = WHAT_SUCESS_RECOMMAND + 1;
    private static final int WHAT_SUCESS_NOTICE = WHAT_FAILED_RECOMMAND + 1;
    public static final int WHAT_FAILED_NOTICE = WHAT_SUCESS_NOTICE + 1;
    public static final int WHAT_SUCESS_LIST = WHAT_FAILED_NOTICE + 1;
    public static final int WHAT_FAILED_LIST = WHAT_SUCESS_LIST + 1;

    private HomeAppListNetSource gameListNetSource = null;

    private DataManagerListener<PrizeAppsCardData> recommandCardListener = new DataManagerListener<PrizeAppsCardData>() {
        @Override
        protected Message onSuccess(int what, PrizeAppsCardData data) {
            if (data != null && data.apps != null && data.apps.size() > 0) {
                return super.onSuccess(WHAT_SUCESS_LIST, data);
            }
            return super.onFailed(WHAT_FAILED_LIST);
        }

    };

    /**
     * @param callback DataManagerCallBack
     * @param appType  应用类型： 0-不区分； 1-软件； 2-游戏
     */
    public HomeDataManager(DataManagerCallBack callback, String appType) {
        super(callback);
        gameListNetSource = new HomeAppListNetSource(appType, false);
        gameListNetSource.setListener(recommandCardListener);
    }


    /**
     * 获取推荐列表
     */
    public void getRecommandList(String requestTAG) {
        gameListNetSource.doRequest(requestTAG);
    }

    public boolean hasNextPage() {
        return gameListNetSource.hasNextPage();
    }

    public void reSetPagerIndex(int index) {
        gameListNetSource.reSetPagerIndex(index);
    }

    public boolean isFirstPage() {
        return gameListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE;
    }


    @Override
    protected void handleMessage(int what, int arg1, int arg2, Object obj) {
    }

    public void setNullListener() {
        if (gameListNetSource != null) {
            gameListNetSource.setListener(null);
        }
    }
}
