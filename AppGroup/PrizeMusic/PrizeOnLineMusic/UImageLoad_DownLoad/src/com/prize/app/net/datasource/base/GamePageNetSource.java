package com.prize.app.net.datasource.base;

import android.text.TextUtils;

import com.prize.app.beans.GameList;
import com.prize.app.beans.Page;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListReq;
import com.prize.app.net.req.GetGameListResp;

/**
 * 获取游戏列表，该列表只包含游戏的ITEM，如果需要获取混合型的列表， 即列表中可能 是 礼包， 公告，子列表，游戏的，请使用
 * {@link SimpleGamePageNetSource}
 * 
 */
public class GamePageNetSource extends
		AbstractNetSource<GameData, GetGameListReq, GetGameListResp> {

	private String listCode;
	/** 页信息 */
	private PageBean page = new PageBean();

	public GamePageNetSource(String listCode) {
		if (TextUtils.isEmpty(listCode)) {
			throw new RuntimeException("list Code can not null");
		}
		this.listCode = listCode;
	}

	@Override
	protected GameData parseResp(GetGameListResp resp) {
		GameData data = new GameData();
		GameList gameList = resp.getGameList();
		Page netPage = resp.getPage();
		if (netPage != null) {
			page.setPage(netPage);
		}
		data.setGameBean(gameList);
		return data;
	}

	@Override
	protected GetGameListReq getRequest() {
		GetGameListReq req = new GetGameListReq();
		// Page netPage = PageBean.convertToNetPage(page);
		// netPage.setPageIndex(page.pageIndex + 1);
		// req.setPage(netPage);
		// req.setIdentification(UUID.randomUUID());
		// req.setListCode(listCode);
		return req;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetGameListResp.class;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/getgamelist";
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
	 * @return
	 */
	public int getCurrentPage() {
		return page.getCurrentPage();
	}

	@Override
	protected GameData parseStrResp(String resp) {

		// TODO Auto-generated method stub
		return null;
	}
}
