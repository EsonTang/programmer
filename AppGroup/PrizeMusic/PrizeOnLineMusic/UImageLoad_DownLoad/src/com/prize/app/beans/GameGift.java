package com.prize.app.beans;

import java.io.Serializable;

/**
 * 游戏活动礼包 礼包信息
 * 
 * @author prize
 * @version 1.0 2013-4-2
 * 
 */
public class GameGift implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -734762357324878191L;

	private String gameCode;// 游戏编号

	private String gameGiftCode;// 游戏礼包代码

	private String gameGiftName;// 游戏礼包名称

	private String gameGiftDesc;// 游戏礼包描述,被gameGiftWapUrl代替

	private String gameGiftIconUrl;// 游戏礼包图标下载地址

	private Byte gameGiftType;// 游戏礼包类型，1 代表激活码

	private Integer gameGiftGoodsTotalNum;// 礼包物品总数

	private Integer gameGiftGoodsRemainNum;// 剩余物品总数
	private Long gameGiftEndTime;// 截至时间

	private String gameGiftGoodsCode;// 礼包物品，如果状态是 gameGiftStatus=2那么这里会有值

	private Byte gameGiftStatus = 1;// 游戏礼包状态 1=可领取，2=已领取，3=已过期，4=已领完
									// (终端显示，抢光了=已过期+已领完)

	private String gameGiftWapUrl;// 游戏礼包wap地址

	public String getGameGiftCode() {
		return gameGiftCode;
	}

	public void setGameGiftCode(String gameGiftCode) {
		this.gameGiftCode = gameGiftCode;
	}

	public String getGameGiftName() {
		return gameGiftName;
	}

	public void setGameGiftName(String gameGiftName) {
		this.gameGiftName = gameGiftName;
	}

	public String getGameGiftDesc() {
		return gameGiftDesc;
	}

	public void setGameGiftDesc(String gameGiftDesc) {
		this.gameGiftDesc = gameGiftDesc;
	}

	public String getGameGiftIconUrl() {
		return gameGiftIconUrl;
	}

	public void setGameGiftIconUrl(String gameGiftIconUrl) {
		this.gameGiftIconUrl = gameGiftIconUrl;
	}

	public Byte getGameGiftType() {
		return gameGiftType;
	}

	public void setGameGiftType(Byte gameGiftType) {
		this.gameGiftType = gameGiftType;
	}

	public Integer getGameGiftGoodsTotalNum() {
		return gameGiftGoodsTotalNum;
	}

	public void setGameGiftGoodsTotalNum(Integer gameGiftGoodsTotalNum) {
		this.gameGiftGoodsTotalNum = gameGiftGoodsTotalNum;
	}

	public Integer getGameGiftGoodsRemainNum() {
		return gameGiftGoodsRemainNum;
	}

	public void setGameGiftGoodsRemainNum(Integer gameGiftGoodsRemainNum) {
		this.gameGiftGoodsRemainNum = gameGiftGoodsRemainNum;
	}

	public Long getGameGiftEndTime() {
		return gameGiftEndTime;
	}

	public void setGameGiftEndTime(Long gameGiftEndTime) {
		this.gameGiftEndTime = gameGiftEndTime;
	}

	public Byte getGameGiftStatus() {
		return gameGiftStatus;
	}

	public void setGameGiftStatus(Byte gameGiftStatus) {
		this.gameGiftStatus = gameGiftStatus;
	}

	public String getGameGiftGoodsCode() {
		return gameGiftGoodsCode;
	}

	public void setGameGiftGoodsCode(String gameGiftGoodsCode) {
		this.gameGiftGoodsCode = gameGiftGoodsCode;
	}

	public String getGameCode() {
		return gameCode;
	}

	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}

	public String getGameGiftWapUrl() {
		return gameGiftWapUrl;
	}

	public void setGameGiftWapUrl(String gameGiftWapUrl) {
		this.gameGiftWapUrl = gameGiftWapUrl;
	}

}
