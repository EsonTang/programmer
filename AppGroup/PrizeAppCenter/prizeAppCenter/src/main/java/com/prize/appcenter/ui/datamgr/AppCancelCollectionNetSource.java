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
import com.prize.app.net.datasource.base.AppsCollectionListData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 类描述：取消收藏列表
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppCancelCollectionNetSource
		extends
		AppAbstractNetSource<AppsCollectionListData, Map<String, String>, GetGameListResp> {
	private String userId;
	private String appIds;

	public AppCancelCollectionNetSource() {
		super();
	}

	public void setData(String userId, String appIds) {
		if (userId == null || appIds == null)
			return;
		this.userId = userId;
		this.appIds = appIds;
	}

	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("appIds", appIds);
		map.put("userId", userId);
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		// TODO Auto-generated method stub
		return GetGameListResp.class;
	}

	@Override
	protected AppsCollectionListData parseStrResp(String resp) {
		AppsCollectionListData appCollectionListData = new Gson().fromJson(
				resp, AppsCollectionListData.class);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {

		// TODO Auto-generated method stub
		 return Constants.GIS_URL+"/appstore/collection/cancel";
//		return "http://192.168.1.34:8080/appstore/collection/cancel";
	}

}
