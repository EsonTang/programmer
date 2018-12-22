package com.prize.app.net.req;

import java.util.ArrayList;

import com.prize.app.beans.GameGift;
import com.prize.app.beans.Page;

/**
 * 获取我的礼包的响应
 * 
 * @author prize
 * @version 1.0 2013-12-26
 */
public class GetMyGameGiftListResp extends BaseResp {

	private Page page;// 分页
	private ArrayList<GameGift> gameGiftList;// 礼包列表

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public ArrayList<GameGift> getGameGiftList() {
		return gameGiftList;
	}

	public void setGameGiftList(ArrayList<GameGift> gameGiftList) {
		this.gameGiftList = gameGiftList;
	}
}
