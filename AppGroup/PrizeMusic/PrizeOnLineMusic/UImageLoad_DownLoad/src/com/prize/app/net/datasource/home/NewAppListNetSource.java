package com.prize.app.net.datasource.home;

import java.util.HashMap;
import java.util.Map;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

public class NewAppListNetSource extends SimpleAppNetSource {

	// 101 代表公告列表
	private static final String LISTCODE = "115";

	/**
	 * @param rankType
	 *            排序类型： downloadTimes - 下载榜 ； hotValue - 飙升榜； latest - 新品榜
	 * @param isGame
	 */
	public NewAppListNetSource(String appType, boolean isGame) {
		super(appType, isGame);
		super.appType = appType;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		if (super.appType != null) {
			param.put("appType", appType);

		}

		return param;
	}

	@Override
	public String getUrl() {

		return Constants.GIS_URL + "/recommand/latest";
	}

}
