package com.prize.app.beans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 游戏列表元素
 * 
 * @author prize
 * @version 1.0 2013-2-4
 *
 */
public class GameListItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8207951096828260426L;

	private String itemNickName;// 标题党，数据库没有的话，取对应元素的名称

	private String itemImg;// 图片，如果是game,如果没有设置，那么取游戏大图标

	private Byte itemType;// 元素类型 1=列表 2=游戏 3=活动 ;4=第三方游戏 5=游戏礼包 6=游戏攻略 7=wap类型

	private String itemCode;// 元素code

	private String itemDesc;// 简介

	private String itemCornerIcon;// 资源角标 2013-8-19 增加v3.22终端新增

	private String itemComment;// 资源评论 2013-8-19 增加v3.22终端新增

	private Game game;// 如果 itemType=2或者4 这里要设置

	private ArrayList<GameListItem> subGameListItems;// 当父类表listcode=113即分类列表才会有值,2013-05-10新增

	public String getItemNickName() {
		return itemNickName;
	}

	public void setItemNickName(String itemNickName) {
		this.itemNickName = itemNickName;
	}

	public String getItemImg() {
		return itemImg;
	}

	public void setItemImg(String itemImg) {
		this.itemImg = itemImg;
	}

	public Byte getItemType() {
		return itemType;
	}

	public void setItemType(Byte itemType) {
		this.itemType = itemType;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemDesc() {
		return itemDesc;
	}

	public void setItemDesc(String itemDesc) {
		this.itemDesc = itemDesc;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public ArrayList<GameListItem> getSubGameListItems() {
		return subGameListItems;
	}

	public void setSubGameListItems(ArrayList<GameListItem> subGameListItems) {
		this.subGameListItems = subGameListItems;
	}

	public String getItemCornerIcon() {
		return itemCornerIcon;
	}

	public void setItemCornerIcon(String itemCornerIcon) {
		this.itemCornerIcon = itemCornerIcon;
	}

	public String getItemComment() {
		return itemComment;
	}

	public void setItemComment(String itemComment) {
		this.itemComment = itemComment;
	}

}
