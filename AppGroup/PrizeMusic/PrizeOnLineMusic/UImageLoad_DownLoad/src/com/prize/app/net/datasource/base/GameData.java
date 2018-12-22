package com.prize.app.net.datasource.base;

import java.util.ArrayList;

import com.prize.app.beans.Game;
import com.prize.app.beans.GameBean;
import com.prize.app.beans.GameList;
import com.prize.app.beans.GameListItem;
import com.prize.app.net.AbstractNetData;
import com.prize.app.util.NumberUtils;

/**
 * 游戏数据
 * 
 * @author prize
 * 
 */
public class GameData extends AbstractNetData {
	public ArrayList<GameBean> items = new ArrayList<GameBean>();
	public String listCode;

	public String listname;

	public void setGameBean(GameList gameList) {
		if (null == gameList) {
			return;
		}
		ArrayList<GameListItem> gameListItems = gameList.getGameItems();
		listCode = gameList.getListCode();
		listname = gameList.getListName();
		GameBean item;
		if (gameListItems != null) {
			for (GameListItem gameListItem : gameListItems) {
				Game game = gameListItem.getGame();
				if (null == game) {
					continue;
				}
				item = new GameBean(game, listCode);
				item.itemCornerIcon = gameListItem.getItemCornerIcon();
				item.itemComment = gameListItem.getItemComment();
				item.itemType = NumberUtils.getByteValue(gameListItem
						.getItemType());
				items.add(item);
			}
		}
	}
}
