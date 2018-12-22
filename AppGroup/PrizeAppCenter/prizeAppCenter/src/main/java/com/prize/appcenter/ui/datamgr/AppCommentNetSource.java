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

package com.prize.appcenter.ui.datamgr;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.AppCommentData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

public class AppCommentNetSource
		extends
		AppAbstractNetSource<AppCommentData, Map<String, String>, GetGameListResp> {
	private String appId;
	private int pageIndex;
	private int pageSize;

	public AppCommentNetSource() {
		super();
	}

	public void setData(String appId, int pageIndex, int pageSize) {
		if (pageIndex <= 0 || pageSize <= 0) {
			return;
		}
		this.appId = appId;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	@Override
	protected Map<String, String> getRequest() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("appId", appId + "");
		map.put("pageIndex", pageIndex + "");
		map.put("pageSize", pageSize + "");
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		// TODO Auto-generated method stub
		return GetGameListResp.class;
	}

	@Override
	protected AppCommentData parseStrResp(String resp) {
		AppCommentData appCommentData = new Gson().fromJson(resp,
				AppCommentData.class);
		// TODO Auto-generated method stub
		return appCommentData;
	}

	@Override
	public String getUrl() {

		// TODO Auto-generated method stub
		return Constants.GIS_URL + "/comment/list";
	}

}
