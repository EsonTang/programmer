package com.prize.app.net.datasource.base;

import com.prize.app.net.AbstractNetData;

/**
 **
 * 搜索匹配
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class PrizeMatchKeyTypeData extends AbstractNetData {
	public AppsItemBean app;
	public String[] items;

	private int pageCount;
	private int pageIndex;
	private short pageSize;
	private int pageItemCount;

	// /**
	// * 将服务器的GameListItem类转换为GameListItemBean
	// *
	// * @param items
	// * @return
	// */
	// public ArrayList<GameListItemBean> getGameListItemBean(
	// ArrayList<GameListItem> items) {
	// ArrayList<GameListItemBean> lists = new ArrayList<GameListItemBean>();
	// if (items != null) {
	// for (GameListItem item : items) {
	// GameListItemBean itemBean = new GameListItemBean();
	// itemBean.itemCode = item.getItemCode();
	// itemBean.itemDesc = item.getItemDesc();
	// itemBean.itemImg = item.getItemImg();
	// itemBean.itemNickName = item.getItemNickName();
	// itemBean.itemCornerIcon = item.getItemCornerIcon();
	// itemBean.itemComment = item.getItemComment();
	// itemBean.listcode = listCode;
	// itemBean.itemComment = item.getItemComment();
	// itemBean.itemCornerIcon = item.getItemCornerIcon();
	// byte type = 0;
	// if (null != item.getItemType()) {
	// itemBean.itemType = NumberUtils.getByteValue(item
	// .getItemType());
	// type = itemBean.itemType;
	// }
	// if (type == GameListItemBean.ITEM_TYPE_GAME
	// || type == GameListItemBean.ITEM_TYPE_OTHER_SOURCE) {
	// itemBean.game = getGameBean(item.getGame());
	// itemBean.game.itemComment = item.getItemComment();
	// itemBean.game.itemCornerIcon = item.getItemCornerIcon();
	// }
	//
	// if (6 == type) {
	// // 6=游戏攻略
	// itemBean.tactics = new TacticsBean(item.getTactics());
	// }
	//
	// if (GameListItemBean.ITME_TYPE_WAPLINK == type) {
	// // 7 = 超链接
	// itemBean.wapLink = new WapLinkBean(item.getWapLink());
	// }
	//
	// if (listCode.equals(GameTypeNetSource.LISTCODE)) {
	// itemBean.subGameListItems = getGameListItemBean(item
	// .getSubGameListItems());
	// }
	// lists.add(itemBean);
	// }
	// }
	//
	// return lists;
	// }

	public int getPageCount() {
		return pageCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public short getPageSize() {
		return pageSize;
	}

	public int getPageItemCount() {
		return pageItemCount;
	}

}
