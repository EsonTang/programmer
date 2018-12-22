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
import com.prize.app.net.datasource.base.Giftdata;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 类描述：一键安装网络信息请求
 * 
 * @author 作者 huanglingjun
 * @version 版本
 */
public class GiftDataNetSource extends
		AppAbstractNetSource<Giftdata, Map<String, String>, GetGameListResp> {
	private String userId;
	private String giftId;

	private static final String url = Constants.GIS_URL + "/appstore/gift/draw";

	public void setData(String userId, String giftId) {
		if (userId == null || giftId == null)
			return;
		this.userId = userId;
		this.giftId = giftId;
	}

	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("giftId", giftId);
		map.put("userId", userId);
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		return GetGameListResp.class;
	}

	/**
	 * 解析
	 */
	@Override
	protected Giftdata parseStrResp(String resp) {
		Giftdata bean = new Gson().fromJson(resp, Giftdata.class);
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
