package com.prize.app.net.datasource.home;

import com.prize.app.net.datasource.base.SimpleGamePageNetSource;
 
public class GameListNetSource extends SimpleGamePageNetSource {

	// 101 代表公告列表
	private static final String LISTCODE = "115";

	public GameListNetSource() {
		super(LISTCODE);
	}
}
