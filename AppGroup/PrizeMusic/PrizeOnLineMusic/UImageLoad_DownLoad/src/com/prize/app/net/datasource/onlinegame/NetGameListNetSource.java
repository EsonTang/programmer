package com.prize.app.net.datasource.onlinegame;

import java.util.HashMap;
import java.util.Map;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

/**
 **
 * 网游请求source
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class NetGameListNetSource extends SimpleAppNetSource {

	private String netType = null;

	public NetGameListNetSource(String netType) {
		this.netType = netType;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/category/netlist";
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		param.put("netType", netType);

		return param;
	}

}
