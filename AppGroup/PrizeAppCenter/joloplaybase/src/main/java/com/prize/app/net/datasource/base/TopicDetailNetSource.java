package com.prize.app.net.datasource.base;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

/**
 * 获取列表:该列表的ITEM可能是游戏,礼包,公告,子列表等,是混合型列表 如果要获取存游戏的列表,请使用{@link GamePageNetSource}
 * 
 */
public class TopicDetailNetSource
		extends
		AppAbstractNetSource<PrizeAppsTypeData, Map<String, String>, GetGameListResp> {

	// private String listCode;
	protected PageBean page = new PageBean();
	// private byte requestListTag = 0;
	private final String TAG = "SimpleAppNetSource";
	/** 分类的请求 */
	public static final int LIST_TAG_TYPE = 1;
	protected String appTypeId = null;
	protected String topicId = null;

	public TopicDetailNetSource() {
	}

	/**
	 * 构造函数
	 * 
	 * @param appTypeId
	 *            专题Id 类型：1 - 应用；2 - 游戏
	 * @param topicId
	 */
	public TopicDetailNetSource(String appTypeId, String topicId) {
		this.appTypeId = appTypeId;
		this.topicId = topicId;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", "50");
		// param.put("pageSize", page.pageSize + "");
		if (this.topicId != null) {
			param.put("topicId", this.topicId);

		}
		if (this.appTypeId != null) {
			param.put("appTypeId", this.appTypeId);

		}

		return param;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetGameListResp.class;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/topic/details";// 专题详情
	}

	// @Override
	// public String getUrl() {
	// return Constants.GIS_URL + "/getgamelist";
	// }

	public void setPage(PageBean page) {
		this.page = page;
	}

	public PageBean getPage() {
		return page;
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

	@Override
	protected PrizeAppsTypeData parseStrResp(String resp) {
		PrizeAppsTypeData bean = new Gson().fromJson(resp,
				PrizeAppsTypeData.class);
		// fastjson解析时候，字段不能为private （public 可以，其他没有试过）
		// PrizeAppsTypeData bean = JSON
		// .parseObject(resp, PrizeAppsTypeData.class);

		if (bean != null) {
			page.pageIndex = bean.getPageIndex();
			page.pageCount = bean.getPageCount();
			page.pageItemCount = bean.getPageItemCount();
			page.pageSize = bean.getPageSize();
		}
		return bean;
	}
}
