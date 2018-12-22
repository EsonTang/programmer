package com.prize.app.net.datasource.gamegift;

import java.util.ArrayList;

import com.prize.app.beans.AppGiftCodes;
import com.prize.app.beans.GameGift;
import com.prize.app.beans.GameGiftBean;
import com.prize.app.net.AbstractNetData;

public class MyGiftsData extends AbstractNetData {
	private ArrayList<GameGiftBean> gameGiftList;// 礼包列表
	public ArrayList<AppGiftCodes> appGiftCodes;
	public int pageCount;

	public MyGiftsData() {
		gameGiftList = new ArrayList<GameGiftBean>();
	}

	public void addGifts(ArrayList<GameGift> gifts) {
		if (null == gifts) {
			return;
		}
		GameGiftBean myGift;
		for (GameGift gift : gifts) {
			myGift = new GameGiftBean(gift);
			gameGiftList.add(myGift);
		}
	}

	public ArrayList<GameGiftBean> getGameGiftList() {
		return gameGiftList;
	}
}
