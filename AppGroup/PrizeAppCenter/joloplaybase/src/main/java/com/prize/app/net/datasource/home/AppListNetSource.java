package com.prize.app.net.datasource.home;

import com.prize.app.net.datasource.base.SimpleAppNetSource;

import java.util.HashMap;
import java.util.Map;

public class AppListNetSource extends SimpleAppNetSource {

	// 101 代表公告列表
	private static final String LISTCODE = "115";

	/**
	 * 是否请求游戏
	 * 
	 * @param isGame
	 */
	public AppListNetSource(String appType, boolean isGame) {
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

}
