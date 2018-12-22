package com.prize.app.net.datasource.singlegame;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

/**
 **
 * 软件榜（下载榜）数据管理请求
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppDownRankListNetSource extends SimpleAppNetSource {
	public AppDownRankListNetSource(String rankType, boolean isGame) {

		super(rankType, isGame);
	}

	private static final String LISTCODE = "115";

	@Override
	public String getUrl() {
		if (isGame) {
			return Constants.GIS_URL + "/rank/gamelist";
		}
		return Constants.GIS_URL + "/rank/applist";
	}

	// @Override
	// protected Map<String, String> getRequest() {
	// Map<String, String> param = new HashMap<String, String>();
	// param.put("pageIndex", page.pageIndex + 1 + "");
	// param.put("pageSize", page.pageSize + "");
	// param.put("rankType", downloadTimes);
	//
	// return param;
	// }

}
