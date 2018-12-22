
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
import com.prize.app.net.datasource.base.AppsInstalledListData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 类描述：安装记录网络信息请求
 * 
 * @author 作者 huanglingjun
 * @version 版本
 */
public class AppUploadInstalledNetSource
		extends
		AppAbstractNetSource<AppsInstalledListData, Map<String, String>, GetGameListResp> {

	private String userId;
	private String appId;
	private static final String url = Constants.GIS_URL + "/install/post";

	/**
	 * 请求参数
	 */
	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userId", userId);	
		map.put("appId", appId+"");	
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		
		return GetGameListResp.class;
	}

	public void setData(String userId,String appId) {
		this.userId = userId;
		this.appId = appId;
	}
	
	/**
	 * 解析
	 */
	@Override
	protected AppsInstalledListData parseStrResp(String resp) {
		AppsInstalledListData bean = new Gson().fromJson(resp,
				AppsInstalledListData.class);
		return null;
	}
	
	/**
	 * 获取url
	 */
	@Override
	public String getUrl() {
		return url;
	}

}

