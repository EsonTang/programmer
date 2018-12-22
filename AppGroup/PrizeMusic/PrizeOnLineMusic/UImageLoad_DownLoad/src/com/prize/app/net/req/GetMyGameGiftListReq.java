package com.prize.app.net.req;

import com.prize.app.beans.Page;

/**
 * 获取我的游戏礼包列表 url: /getmygamegiftlist
 * 
 * @author prize
 * @version 1.0 2013-12-26
 */
public class GetMyGameGiftListReq extends BaseReq {
	private Page page;

	private Byte gameGiftReqType;// 1=可用礼包（未过期） 2=过期礼包

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Byte getGameGiftReqType() {
		return gameGiftReqType;
	}

	public void setGameGiftReqType(Byte gameGiftReqType) {
		this.gameGiftReqType = gameGiftReqType;
	}
}
