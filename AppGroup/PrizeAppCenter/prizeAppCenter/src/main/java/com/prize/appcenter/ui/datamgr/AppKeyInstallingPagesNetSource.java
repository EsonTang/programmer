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
import com.prize.app.net.datasource.base.AppsKeyInstallingPageListData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：一键安装网络信息请求
 * 
 * @author 作者 huanglingjun
 * @version 版本
 */
public class AppKeyInstallingPagesNetSource
		extends
		AppAbstractNetSource<AppsKeyInstallingPageListData, Map<String, String>, GetGameListResp> {

	private static final String url = Constants.GIS_URL + "/onekey/bystep";

	private String appType;
	private String type;



	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 请求参数
	 */
	@Override
	protected Map<String, String> getRequest() {
		if(TextUtils.isEmpty(type))
			return  null;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("type", type);
		return map;
	}
	
	public void setAppType(String apptype) {
		this.appType = apptype;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		return GetGameListResp.class;
	}

	/**
	 * 解析
	 */
	@Override
	protected AppsKeyInstallingPageListData parseStrResp(String resp) {
		return new Gson().fromJson(resp,
				AppsKeyInstallingPageListData.class);
	}

	/**
	 * 获取url
	 */
	@Override
	public String getUrl() {
		return url;
	}



}
