package com.prize.app.net.datasource.search;

import com.prize.app.beans.Page;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.datasource.base.GameData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.SearchGameReq;
import com.prize.app.net.req.SearchGameResp;

/**
 * 
 **
 * 
 * @author longbaoxiu
 * @version V1.0 2015
 */
public class SearchGameNetSource extends
		AbstractNetSource<GameData, SearchGameReq, SearchGameResp> {

	private PageBean page = new PageBean();
	private String keyWord = null;
	private byte gameSearchFromTag;// 1=来自于本身搜索，2=来自于第三方搜索,需要告知,如果是2且pageIndex=2
									// 服务端就直接搜索第三方
	private String gameSearchFromInfo;// 搜哪里搜索到的

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	@Override
	protected GameData parseResp(SearchGameResp resp) {
		Page netPage = resp.getPage();
		if (null != page) {
			page.setPage(netPage);
		}
		return data;
	}

	@Override
	protected SearchGameReq getRequest() {
		SearchGameReq req = new SearchGameReq();
		req.setsKeyword(keyWord);
		Page netPage = PageBean.convertToNetPage(page);
		netPage.setPageIndex(page.pageIndex + 1);
		if (page.pageIndex > 0) {
			// 搜索下一页数据
		} else {
			// 默认是 1
			gameSearchFromTag = 1;
		}
		req.setGameSearchFromTag(gameSearchFromTag);
		req.setPage(netPage);
		return req;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/searchgame";
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return SearchGameResp.class;
	}

	public boolean hasNextPage() {
		return page.hasNext();
	}

	public int getCount() {
		return page.pageItemCount;
	}

	/**
	 * 重置搜索信息，PAGE，from where, from info
	 */
	public void resetSearchInfo() {
		page.pageCount = 1;
		page.pageIndex = 0;
		page.pageItemCount = 0;
		page.pageSize = 0;
		gameSearchFromInfo = null;
		gameSearchFromTag = 1;
	}

	public String getGameSearchFromInfo() {
		return gameSearchFromInfo;
	}

	/**
	 * 1=来自于本身搜索，2=来自于第三方搜索,需要告知,如果是2且pageIndex=2 服务端就直接搜索第三方
	 * 
	 * @return
	 */
	public byte getGameSearchFromTag() {
		return gameSearchFromTag;
	}

	public int getCurentPage() {
		return page.getCurrentPage();
	}

	@Override
	protected GameData parseStrResp(String resp) {

		// TODO Auto-generated method stub
		return null;
	}
}
