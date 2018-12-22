package com.prize.appcenter.ui.datamgr;

import android.os.Message;

import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppGameCategoryData;
import com.prize.app.net.datasource.base.CategoryNetSource;

public class AppCategoryDataManager extends AbstractDataManager {

	public static final int WHAT_SUCESS_LIST = 1;
	public static final int WHAT_SUCESS_LIST_EMPTY = 0;
	private CategoryNetSource mCategoryNetSource = null;
	private DataManagerListener<AppGameCategoryData> recommandListListener = new DataManagerListener<AppGameCategoryData>() {
		@Override
		protected Message onSuccess(int what, AppGameCategoryData data) {
			return super.onSuccess(WHAT_SUCESS_LIST, data);
		}

	};

	/**
	 * @param callback
	 * @param appType
	 *            应用类型： 0-不区分； 1-软件； 2-游戏
	 */
	public AppCategoryDataManager(DataManagerCallBack callback, String rootType) {
		super(callback);
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
			break;
		}
	}

	public void setNullListener() {
		if (mCategoryNetSource != null) {
			mCategoryNetSource.setListener(null);
		}
	}
}
