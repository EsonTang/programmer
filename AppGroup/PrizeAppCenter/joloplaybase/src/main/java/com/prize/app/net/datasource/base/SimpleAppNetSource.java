package com.prize.app.net.datasource.base;

import com.google.gson.Gson;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;
import com.prize.app.util.JLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取列表:该列表的ITEM可能是游戏,礼包,公告,子列表等,是混合型列表 如果要获取存游戏的列表,请使用{@link GamePageNetSource}
 * 
 */
public class SimpleAppNetSource
		extends
		AppAbstractNetSource<PrizeAppsTypeData, Map<String, String>, GetGameListResp> {

	// private String listCode;
	protected PageBean page = new PageBean();
	// private byte requestListTag = 0;
	private final String TAG = "SimpleAppNetSource";
	/** 分类的请求 */
	public static final int LIST_TAG_TYPE = 1;
	protected String rankType = null;
	protected String appType = null;
	protected String categoryId = null;
	protected String developer = null;
	protected boolean isGame = false;
	protected String tag ;

	public SimpleAppNetSource() {
	}

	@Override
	public void doRequest(String requestTAG){
		tag = requestTAG;
		super.doRequest(requestTAG);
	}
	
	/**
	 * 
	 * @param rankType
	 * 
	 */
	/**
	 * 
	 * @param rankType
	 *            排序类型： downloadTimes - 下载榜 ； hotValue - 飙升榜； latest - 新品榜
	 * @param isGame
	 */
	public SimpleAppNetSource(String rankType, boolean isGame) {
		this.rankType = rankType;
		this.isGame = isGame;
	}

	@Override
	protected Map<String, String> getRequest() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageIndex", page.pageIndex + 1 + "");
		param.put("pageSize", page.pageSize + "");
		if (this.rankType != null) {
			param.put("rankType", this.rankType);

		}

		return param;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetGameListResp.class;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/recommand/list";// 首页推荐
	}

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
	 * @return int
	 */
	public int getCurrentPage() {
		return page.getCurrentPage();
	}

	@Override
	protected PrizeAppsTypeData parseStrResp(String resp) {
		JLog.i(TAG, "page.pageIndex="+page.pageIndex);
//		if(page.pageIndex<=0){//第一次默认是0；
//			UpdateCach.getInstance().setJsonData(tag, resp);
//		}
		PrizeAppsTypeData bean = new Gson().fromJson(resp,
				PrizeAppsTypeData.class);
		if (bean != null) {
			page.pageIndex = bean.getPageIndex();
			page.pageCount = bean.getPageCount();
			page.pageItemCount = bean.getPageItemCount();
			page.pageSize = bean.getPageSize();
		}
		return bean;
	}
	
	public void reSetPagerIndex(int index){
		page.pageIndex = index;
	}
}
