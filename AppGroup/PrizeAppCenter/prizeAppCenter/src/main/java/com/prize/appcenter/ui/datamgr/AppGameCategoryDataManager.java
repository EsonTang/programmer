package com.prize.appcenter.ui.datamgr;

import android.os.Message;
/**
 * 
 *  app - game 分类数据管理
 * 
 */

import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppGameCategoryData;
import com.prize.app.net.datasource.base.CategoryNetSource;

public class AppGameCategoryDataManager extends AbstractDataManager {

	private final String TAG = "AppCategoryDataManager";

	public static final int WHAT_SUCESS_LIST = 1;
	public static final int WHAT_SUCESS_LIST_EMPTY = 0;
	private String rootType = null;
	private CategoryNetSource mCategoryNetSource = null;
	private DataManagerListener<AppGameCategoryData> recommandListListener = new DataManagerListener<AppGameCategoryData>() {
		@Override
		protected Message onSuccess(int what, AppGameCategoryData data) {
			return super.onSuccess(WHAT_SUCESS_LIST, data);
		}

	};

	/**
	 * @param callback
	 * @param rootType
	 *            应用类型： 0-不区分； 1-软件； 2-游戏
	 */
	public AppGameCategoryDataManager(DataManagerCallBack callback, String rootType) {
		super(callback);
		this.rootType = rootType;
		mCategoryNetSource = new CategoryNetSource(rootType);
		mCategoryNetSource.setListener(recommandListListener);
	}


	/**
	 * 获取类别列表
	 * 
	 * @return void
	 * @see
	 */
	public void getCategoryList(String requestTAG) {
		mCategoryNetSource.doRequest(requestTAG);
	}

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case NetSourceListener.WHAT_NETERR:
			// if (isFirstPage()) {
			// cache.setData(null);
			// }
			break;
		}
	}

	public void setNullListener() {
		if (mCategoryNetSource != null) {
			mCategoryNetSource.setListener(null);
		}
	}
}
