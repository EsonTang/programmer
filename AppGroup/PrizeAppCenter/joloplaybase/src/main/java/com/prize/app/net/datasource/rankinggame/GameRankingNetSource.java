package com.prize.app.net.datasource.rankinggame;

import com.prize.app.net.datasource.base.SimpleGamePageNetSource;

public class GameRankingNetSource extends SimpleGamePageNetSource {

	// 112 游戏排行
	private static final String LISTCODE = "112";

	public GameRankingNetSource() {
		super(LISTCODE);
	}

}
