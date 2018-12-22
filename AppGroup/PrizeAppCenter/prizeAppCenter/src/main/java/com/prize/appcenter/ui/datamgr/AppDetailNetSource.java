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

import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：
 * 
 * @author 作者 huanglingjun
 * @version 版本
 */
public class AppDetailNetSource
		extends
		AppAbstractNetSource<AppDetailData, Map<String, String>, GetGameListResp> {

	private String appId;
	private String userId;
	private static final String url = Constants.GIS_URL + "/appinfo/details";

	/**
	 * 请求参数
	 */
	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("appId", appId + "");
		if(!TextUtils.isEmpty(userId)){
			map.put("userId", userId);
		}
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		return GetGameListResp.class;
	}

	public void setData(String appId, String userId) {
		if (appId == null || userId == null)
			return;
		this.appId = appId;
		this.userId = userId;
	}

	/**
	 * 解析
	 */
	@Override
	protected AppDetailData parseStrResp(String resp) {
		AppDetailData bean = new Gson().fromJson(resp, AppDetailData.class);
		return bean;
	}

	/**
	 * 获取url
	 */
	@Override
	public String getUrl() {
		return url;
	}

}
