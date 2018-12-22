/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.app.net.datasource.base;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

public class AdNetSource extends
		AppAbstractNetSource<AppData, Map<String, String>, GetGameListResp> {
	private PageBean page = new PageBean();
	private byte requestListTag = 0;
	private String listCode;
	private String appType = null;

	/**
	 * 是否请求游戏
	 * 
	 * @param isGame
	 */
	public AdNetSource(String appType) {
		this.appType = appType;
	}

	@Override
	protected Map<String, String> getRequest() {
		// GetGameListReq req = new GetGameListReq();
		Map<String, String> param = new HashMap<String, String>();
		if (this.appType != null) {
			param.put("appType", this.appType);
		}
		return param;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		// TODO Auto-generated method stub
		return GetGameListResp.class;
	}

	@Override
	protected AppData parseStrResp(String resp) {
		AppData data = new Gson().fromJson(resp, AppData.class);
		return data;
	}

	@Override
	public String getUrl() {

		return Constants.GIS_URL + "/recommand/summary";
	}

}
