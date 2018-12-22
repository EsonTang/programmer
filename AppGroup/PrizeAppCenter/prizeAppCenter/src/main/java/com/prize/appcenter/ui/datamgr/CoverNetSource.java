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

import java.util.Map;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.CoverData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 类描述：启动页网络信息请求
 * 
 * @author 作者 huanglingjun
 * @version 版本
 */
public class CoverNetSource extends
		AppAbstractNetSource<CoverData, Map<String, String>, GetGameListResp> {

	private static final String url = Constants.GIS_URL
			+ "/recommand/startupads";

	/**
	 * 请求参数
	 */
	@Override
	protected Map<String, String> getRequest() {
		return null;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		return GetGameListResp.class;
	}

	/**
	 * 解析
	 */
	@Override
	protected CoverData parseStrResp(String resp) {
		CoverData bean = new Gson().fromJson(resp, CoverData.class);
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
