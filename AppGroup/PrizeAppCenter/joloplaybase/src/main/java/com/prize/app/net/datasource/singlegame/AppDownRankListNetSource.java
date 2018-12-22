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


	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/rank/ranklist";
	}


}
