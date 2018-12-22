package com.prize.app.net.datasource.home;

import java.util.HashMap;
import java.util.Map;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

public class CategoryListNetSource extends SimpleAppNetSource {

	/**
	 * 
	 * @param isGame
	 */
	public CategoryListNetSource(String categoryI, String developer) {
		super.categoryId = categoryI;
		super.developer = developer;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		if (super.categoryId != null) {
			param.put("categoryId", categoryId);

		}
		if (super.developer != null) {
			param.put("developer", developer);

		}

		return param;
	}

	@Override
	public String getUrl() {
		if (super.categoryId != null)
			return Constants.GIS_URL + "/category/apps";// 根据分类获取应用数据

		if (super.developer != null)
			return Constants.GIS_URL + "/appinfo/listbydev";// 根据开发者获取应用数据
		return "";
	}
}
