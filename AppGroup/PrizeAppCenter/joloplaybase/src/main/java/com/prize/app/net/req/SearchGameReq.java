package com.prize.app.net.req;

import com.prize.app.beans.Page;

/**
 * 根据关键词搜索游戏
 * 
 * @author prize
 * @version 1.0 2013-2-25
 *
 */
public class SearchGameReq extends BaseReq {

	private Page page;

	private String sKeyword;

	private Byte gameSearchFromTag;// 1=来自于本身搜索，2=来自于第三方搜索,需要告知,如果是2且pageIndex=2
									// 服务端就直接搜索第三方

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getsKeyword() {
		return sKeyword;
	}

	public void setsKeyword(String sKeyword) {
		this.sKeyword = sKeyword;
	}

	public Byte getGameSearchFromTag() {
		return gameSearchFromTag;
	}

	public void setGameSearchFromTag(Byte gameSearchFromTag) {
		this.gameSearchFromTag = gameSearchFromTag;
	}

}
