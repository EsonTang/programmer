package com.prize.app.net.datasource.singlegame;

import com.prize.app.net.datasource.base.SimpleGamePageNetSource;

/**
 * 游戏分类
 * 
 * @author prize
 * 
 */
public class GameTypeNetSource extends SimpleGamePageNetSource {
	// 113=游戏分类(特殊)
	public static final String LISTCODE = "113";

	public GameTypeNetSource() {
		super(LISTCODE);
	}

}
