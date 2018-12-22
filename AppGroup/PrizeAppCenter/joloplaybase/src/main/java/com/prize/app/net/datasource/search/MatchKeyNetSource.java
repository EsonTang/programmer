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

package com.prize.app.net.datasource.search;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.PrizeMatchKeyTypeData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

public class MatchKeyNetSource
		extends
		AppAbstractNetSource<PrizeMatchKeyTypeData, Map<String, String>, GetGameListResp> {
	protected PageBean page = new PageBean();
	private String query = null;

	@Override
	protected Map<String, String> getRequest() {

		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		if (this.query != null) {
			param.put("query", this.query);

		}

		return param;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetGameListResp.class;
	}

	@Override
	protected PrizeMatchKeyTypeData parseStrResp(String resp) {
		PrizeMatchKeyTypeData bean = new Gson().fromJson(resp,
				PrizeMatchKeyTypeData.class);
		// if (bean != null) {
		// page.pageIndex = bean.getPageIndex();
		// page.pageCount = bean.getPageCount();
		// page.pageItemCount = bean.getPageItemCount();
		// page.pageSize = bean.getPageSize();
		// }
		return bean;
	}

	@Override
	public String getUrl() {

		return Constants.GIS_URL + "/search/match";
	}

	/**
	 * 判断是否有下一页
	 * 
	 * @return
	 */
	public boolean hasNextPage() {
		return page.hasNext();
	}

	/**
	 * 获得当前页
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return page.getCurrentPage();
	}

}
