package com.prize.appcenter.ui.datamgr;

import java.util.ArrayList;

import android.os.Message;

import com.prize.app.beans.PageBean;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.home.CategoryListNetSource;

/**
 * 
 **
 * 请求分类管理
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class ListForSubClassDataManager extends AbstractDataManager {

	public static final int WHAT_SUCESS_LIST = 0;
	public static final int WHAT_FAILED_LIST = WHAT_SUCESS_LIST + 1;
	private CategoryListNetSource gameListNetSource = null;
	private DataManagerListener<PrizeAppsTypeData> recommandListListener = new DataManagerListener<PrizeAppsTypeData>() {
		@Override
		protected Message onSuccess(int what, PrizeAppsTypeData data) {
			if (data.apps.size() > 0) {
				ArrayList<AppsItemBean> records = new ArrayList<AppsItemBean>();
				HomeRecord record = null;
				// for (AppsItemBean item : data.apps) {
				// // if (item.game != null
				// // && MyGamesDataSource.getInstance().mygamesContain(
				// // item.game.gamePkgName)) {
				// // continue;
				// // }
				// // record = obtainHomeRecord(HomeRecord.CONTENT_TYPE_LIST,
				// // item);
				// // BitmapMgr.loadBitmapToCache(item.iconUrl, false);
				// // records.add(record);
				// }
				// HomeDataSource.replaceHomeRecords(records,
				// HomeRecord.CONTENT_TYPE_LIST);
				return super.onSuccess(WHAT_SUCESS_LIST, data);
			}
			return super.onFailed(WHAT_FAILED_LIST);
		}

	};

	/**
	 * @param callback
	 * @param appType
	 *            应用类型： 0-不区分； 1-软件； 2-游戏
	 * @param developer
	 *            开发者
	 */
	public ListForSubClassDataManager(DataManagerCallBack callback,
			String categoryId, String developer) {
		super(callback);
		gameListNetSource = new CategoryListNetSource(categoryId, developer);
		gameListNetSource.setListener(recommandListListener);
	}

	/**
	 * 获取分类所属数据列表
	 * 
	 * @return void
	 * @see
	 */
	public void getRecommandList(String requestType) {
		gameListNetSource.doRequest(requestType);
	}

	public boolean hasNextPage() {
		return gameListNetSource.hasNextPage();
	}

	public boolean isFirstPage() {
		return gameListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE ? true
				: false;
	}

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case NetSourceListener.WHAT_NETERR:
			break;
		}
	}

	public void setNullListener() {
		if (gameListNetSource != null) {
			gameListNetSource.setListener(null);
		}
	}
}
