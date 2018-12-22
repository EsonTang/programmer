package com.prize.app.beans;

import java.io.Serializable;

/**
 * 分页信息,针对比较固定的分页，例如游戏列表，攻略列表，礼包列表
 * 
 * @author prize
 * @version 1.0 2013-2-4
 * 
 */
public class Page implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2872469230221985575L;

	private Integer pageIndex;// 页面id,最小从1开始

	private short pageSize;// 一个页面内的元素个数

	private Integer pageCount;// 总页面数

	private Integer pageItemCount;// 总记录数

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public short getPageSize() {
		return pageSize;
	}

	public void setPageSize(short pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public Integer getPageItemCount() {
		return pageItemCount;
	}

	public void setPageItemCount(Integer pageItemCount) {
		this.pageItemCount = pageItemCount;
	}
}
