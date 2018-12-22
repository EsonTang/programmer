package com.prize.app.net.req;

import com.prize.app.beans.GameList;
import com.prize.app.beans.Page;

/**
 * 获取列表响应
 * 
 * @author prize
 * @version 1.0 2013-2-4
 *
 */
public class GetGameListResp extends BaseResp {
	private Page page;// 分页

	private GameList gameList;// 游戏列表

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public GameList getGameList() {
		return gameList;
	}

	public void setGameList(GameList gameList) {
		this.gameList = gameList;
	}

}
