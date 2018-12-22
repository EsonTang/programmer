package com.prize.app.net.datasource.search;

import java.util.HashMap;
import java.util.Map;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

public class SearchListNetSource extends SimpleAppNetSource {
	private String query = null;

	public SearchListNetSource(String query) {
		this.query = query;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/search/post";
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		if (this.query != null) {
			param.put("query", this.query);

		}

		return param;
	}
}
