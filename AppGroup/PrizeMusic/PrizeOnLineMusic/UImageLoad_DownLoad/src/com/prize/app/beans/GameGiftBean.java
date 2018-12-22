package com.prize.app.beans;

import java.io.Serializable;

import com.prize.app.util.NumberUtils;

/**
 * 游戏活动礼包 礼包信息
 */
public class GameGiftBean implements Serializable {
	private static final long serialVersionUID = 1L;

	public String gameCode;// 游戏编号

	public String gameGiftCode;// 游戏礼包代码

	public String gameGiftName;// 游戏礼包名称

	public String gameGiftDesc;// 游戏礼包描述,被gameGiftWapUrl代替

	public String gameGiftIconUrl;// 游戏礼包图标下载地址

	public byte gameGiftType;// 游戏礼包类型，1 代表激活码

	public int gameGiftGoodsTotalNum;// 礼包物品总数

	public int gameGiftGoodsRemainNum;// 剩余物品总数

	public long gameGiftEndTime;// 截至时间

	public String gameGiftGoodsCode;// 礼包物品，如果状态是 gameGiftStatus=那么这里会有值

	public byte gameGiftStatus = 1;// 游戏礼包状态 1=可领取，2=已领取，3=已过期，4=已领完
									// (终端显示，抢光了=已过期+已领完)

	public String gameGiftWapUrl; // 游戏礼包wap地址

	public GameGiftBean(GameGift gift) {
		if(null == gift){
			return;
		}
		gameCode = gift.getGameCode();
		gameGiftCode = gift.getGameGiftCode();
		gameGiftName = gift.getGameGiftName();
		gameGiftDesc = gift.getGameGiftDesc();
		gameGiftIconUrl = gift.getGameGiftIconUrl();
		gameGiftType = NumberUtils.getByteValue(gift.getGameGiftType());
		gameGiftGoodsTotalNum = NumberUtils.getIntegerValue(gift
				.getGameGiftGoodsTotalNum());
		gameGiftGoodsRemainNum = NumberUtils.getIntegerValue(gift
				.getGameGiftGoodsRemainNum());
		gameGiftEndTime = NumberUtils.getLongValue(gift.getGameGiftEndTime());
		gameGiftGoodsCode = gift.getGameGiftGoodsCode();
		gameGiftStatus = NumberUtils.getByteValue(gift.getGameGiftStatus());
		gameGiftWapUrl = gift.getGameGiftWapUrl();
	}
}
