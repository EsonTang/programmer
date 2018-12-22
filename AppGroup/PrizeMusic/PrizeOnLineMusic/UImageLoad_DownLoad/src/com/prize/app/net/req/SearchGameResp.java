package com.prize.app.net.req;

import java.util.ArrayList;

import com.prize.app.beans.Game;
import com.prize.app.beans.Page;

/**
 * 搜索响应
 * 
 * @author prize
 * @version 1.0 2013-2-25
 *
 */
public class SearchGameResp extends BaseResp {
	private Page page;

	private ArrayList<Game> games;// 游戏编号

	private String gameSearchFromInfo;// 搜哪里搜索到的

	private Byte gameSearchFromTag;// 1=来自于本身搜索，2=来自于第三方搜索

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public ArrayList<Game> getGames() {
		return games;
	}

	public void setGames(ArrayList<Game> games) {
		this.games = games;
	}

	public String getGameSearchFromInfo() {
		return gameSearchFromInfo;
	}

	public void setGameSearchFromInfo(String gameSearchFromInfo) {
		this.gameSearchFromInfo = gameSearchFromInfo;
	}

	public Byte getGameSearchFromTag() {
		return gameSearchFromTag;
	}

	public void setGameSearchFromTag(Byte gameSearchFromTag) {
		this.gameSearchFromTag = gameSearchFromTag;
	}

}
