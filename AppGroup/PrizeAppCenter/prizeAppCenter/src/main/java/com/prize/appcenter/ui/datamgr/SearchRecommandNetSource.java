package com.prize.appcenter.ui.datamgr;

import java.util.HashMap;
import java.util.Map;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

/**
 * 
 * 24小时热搜请求管理
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class SearchRecommandNetSource extends SimpleAppNetSource {
	private final String url = Constants.GIS_URL + "/search/hotApps";

	public SearchRecommandNetSource() {
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");

		return null;
	}
}
