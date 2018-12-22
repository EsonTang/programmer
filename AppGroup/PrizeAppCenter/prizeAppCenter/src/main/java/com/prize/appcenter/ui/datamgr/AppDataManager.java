package com.prize.appcenter.ui.datamgr;

import android.os.Message;

import com.prize.app.beans.PageBean;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AdNetSource;
import com.prize.app.net.datasource.base.AppData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.home.AppListNetSource;

/**
 * 
 **
 * 应用于游戏界面的数据请求管理类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppDataManager extends AbstractDataManager {

	private final String TAG = "HomeDataManager";

	public static final int WHAT_SUCESS_RECOMMAND = 1;
	public static final int WHAT_FAILED_RECOMMAND = WHAT_SUCESS_RECOMMAND + 1;
	public static final int WHAT_SUCESS_NOTICE = WHAT_FAILED_RECOMMAND + 1;
	public static final int WHAT_FAILED_NOTICE = WHAT_SUCESS_NOTICE + 1;
	public static final int WHAT_SUCESS_LIST = WHAT_FAILED_NOTICE + 1;
	public static final int WHAT_FAILED_LIST = WHAT_SUCESS_LIST + 1;
	// 广告
	private AdNetSource getRecommandNetSource = null;
	// private GetRecommandNetSource getRecommandNetSource = null;
	private DataManagerListener<AppData> recommandListener = new DataManagerListener<AppData>() {

		@Override
		protected Message onSuccess(int what, AppData data) {
			if (data != null && data.ads.size() > 0) {
				// ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				// HomeRecord record = null;
				// for (GameListItemBean item : data.items) {
				// record = obtainHomeRecord(HomeRecord.CONTENT_TYPE_AD, item);
				// if (!BitmapMgr.loadBitmapToCache(item.itemImg, false)) {
				// return super.onFailed(WHAT_FAILED_RECOMMAND);
				// }
				// records.add(record);
				// }
				// HomeDataSource.replaceHomeRecords(records,
				// HomeRecord.CONTENT_TYPE_AD);
				return super.onSuccess(WHAT_SUCESS_RECOMMAND, data);
			}
			return super.onFailed(WHAT_FAILED_RECOMMAND);
		}

		// @Override
		// protected Message onFailed(int what) {
		// return super.onFailed(WHAT_FAILED_RECOMMAND);
		// }
	};

	private AppListNetSource gameListNetSource = null;
	private DataManagerListener<PrizeAppsTypeData> recommandListListener = new DataManagerListener<PrizeAppsTypeData>() {
		@Override
		protected Message onSuccess(int what, PrizeAppsTypeData data) {
			if (data != null && data.apps.size() > 0) {
//				ArrayList<AppsItemBean> records = new ArrayList<AppsItemBean>();
//				HomeRecord record = null;
//				for (AppsItemBean item : data.apps) {
//					record = obtainHomeRecord(HomeRecord.CONTENT_TYPE_LIST,
//							item);
//					// BitmapMgr.loadBitmapToCache(item.iconUrl, false);
//					// records.add(record);
//				}
				// HomeDataSource.replaceHomeRecords(records,
				// HomeRecord.CONTENT_TYPE_LIST);
				return super.onSuccess(WHAT_SUCESS_LIST, data);
			}
			return super.onFailed(WHAT_FAILED_LIST);
		}

		// @Override
		// protected Message onFailed(int what) {
		// // return super.onFailed(WHAT_FAILED_LIST);
		// }
	};

	/**
	 * @param callback
	 * @param appType
	 *            应用类型： 0-不区分； 3-软件； 4-游戏
	 */
	public AppDataManager(DataManagerCallBack callback, String appType) {
		super(callback);
		gameListNetSource = new AppListNetSource(appType, false);
		gameListNetSource.setListener(recommandListListener);
	}

	/**
	 * new HomeRecord
	 * @param contentType
	 * @param item
	 * @return
	 */
	private HomeRecord obtainHomeRecord(int contentType, AppsItemBean item) {
		HomeRecord record = new HomeRecord();
		record.id = item.id;
		// record.content_type = contentType;
		// record.desc = item.itemDesc;
		record.iconUrl = item.iconUrl;
		// record.type = item.itemType;
		record.name = item.name;
		// record.packageName = getPkgName(item.game);
		// record.webUrl = getWebUrl(item.tactics);
		// record.itemComment = item.itemComment;
		// record.itemCornerIcon = item.itemCornerIcon;
		// if (item.game != null) {
		// record.gameActivity = item.game.gameActivity;
		// record.gameClass = item.game.gameClass;
		// record.downloadUrl = item.game.gameDownloadUrl;
		// record.varCode = item.game.gameVersionCode;
		// record.downloadCount = item.game.gameDownloadCountNick;
		// record.size = item.game.gameSizeShow;
		// }
		return record;
	}

	/**
	 * 获取推荐列表
	 * 
	 * @return void
	 */
	public void getRecommandList(String requestTag) {
		gameListNetSource.doRequest(requestTag);
	}
	
	public void reSetPagerIndex(int index) {
		gameListNetSource.reSetPagerIndex(index);
	}

	/**
	 * 获取推荐列表
	 * 
	 * @return void
	 * @see
	 */
	// public void getRecommandList(PageBean page) {
	// gameListNetSource.setPage(page);
	// gameListNetSource.doRequest();
	// }

	public boolean hasNextPage() {
		return gameListNetSource.hasNextPage();
	}

	public boolean isFirstPage() {
		return gameListNetSource.getCurrentPage() <= PageBean.FIRST_PAGE;
	}

	/**
	 * 
	 * 获取头部（推荐位）
	 * 
	 * @return void
	 */
	public void getRecommands(String requestTag, String appType) {
		if (getRecommandNetSource == null) {
			getRecommandNetSource = new AdNetSource(appType);
			getRecommandNetSource.setListener(recommandListener);
		}	
		getRecommandNetSource.doRequest(requestTag);
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
