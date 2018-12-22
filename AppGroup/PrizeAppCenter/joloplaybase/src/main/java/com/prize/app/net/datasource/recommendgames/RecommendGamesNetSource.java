package com.prize.app.net.datasource.recommendgames;

import com.prize.app.net.datasource.base.GamePageNetSource;

public class RecommendGamesNetSource extends GamePageNetSource {
	// 200 标示推荐游戏列表
	public static final String LISTCODE = "200";//"1372340870011";

	public RecommendGamesNetSource() {
		super(LISTCODE);
	}
}
