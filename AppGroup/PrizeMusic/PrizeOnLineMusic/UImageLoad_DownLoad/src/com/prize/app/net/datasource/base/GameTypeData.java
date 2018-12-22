package com.prize.app.net.datasource.base;

import com.prize.app.beans.Game;
import com.prize.app.beans.GameBean;
import com.prize.app.net.AbstractNetData;

/**
 * 游戏类型数据
 * 
 * @author prize
 * 
 */
public class GameTypeData extends AbstractNetData {

	private String listCode;

	public GameTypeData() {
		listCode = null;
	}

	public GameTypeData(String listCode) {
		this.listCode = listCode;
	}

	/**
	 * 将服务器的Game类转换为GameBean
	 * 
	 * @param game
	 * @return
	 */
	public GameBean getGameBean(Game game) {
		if (game != null) {
			return new GameBean(game, listCode);
		} else {
			return null;
		}
	}

}
