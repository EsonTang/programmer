
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
import com.prize.app.net.datasource.base.MoreListData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 类描述：删除安装记录网络信息请求
 * @author 作者 huanglingjun
 * @version 版本
 */
public class AppMoreListNetSource
		extends
		AppAbstractNetSource<MoreListData, Map<String, String>, GetGameListResp> {

	private String cardId;
	private int pageIndex;
	private int pageSize;
	
	/**
	 * 请求参数
	 */
	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("cardId", cardId);
		map.put("pageIndex", pageIndex+"");	
		map.put("pageSize", pageSize+"");
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		
		return GetGameListResp.class;
	}

	public void setData(String cardId,int pageIndex,int pageSize) {
		if (cardId == null || pageIndex == 0 || pageSize == 0) return;
		this.cardId = cardId;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
	
	/**
	 * 解析
	 */
	@Override
	protected MoreListData parseStrResp(String resp) {
		MoreListData bean = new Gson().fromJson(resp,
				MoreListData.class);
		return bean;
	}
	
	/**
	 * 获取url
	 */
	@Override
	public String getUrl() {
		return Constants.GIS_URL+"/recommand/cardapps";
	}

}

