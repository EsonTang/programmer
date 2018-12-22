package com.prize.app.beans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 游戏列表
 * 
 * @author prize
 * @version 1.0 2013-2-4
 *
 */
public class GameList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1471941144633032538L;
	/**
	 * 一期：100=首页轮换 101=首页磁贴 102=单机列表 103=网游列表 二期：110=首页轮换 111=首页2xn磁贴 112=排行
	 * 113=游戏分类(特殊)
	 */
	private String listCode;// list编号，轮换，磁贴 单机 网游 是固定的,请求什么id就返回什么id

	private String listName;// list名称

	private ArrayList<GameListItem> gameItems;// 游戏编号

	private String listDesc;// 列表描述 2013-05-14,先保留

	public String getListCode() {
		return listCode;
	}

	public void setListCode(String listCode) {
		this.listCode = listCode;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public ArrayList<GameListItem> getGameItems() {
		return gameItems;
	}

	public void setGameItems(ArrayList<GameListItem> gameItems) {
		this.gameItems = gameItems;
	}

	public String getListDesc() {
		return listDesc;
	}

	public void setListDesc(String listDesc) {
		this.listDesc = listDesc;
	}
}
