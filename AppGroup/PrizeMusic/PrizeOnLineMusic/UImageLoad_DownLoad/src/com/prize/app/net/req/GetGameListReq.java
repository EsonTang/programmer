package com.prize.app.net.req;

/**
 * 获取游戏列表
 * 
 * @author prize
 * @version 1.0 2013-2-4
 *
 */
public class GetGameListReq extends BaseReq {

	/**
	 * 请求的列表code 主干3.0老协议：100代表首页轮换,101代表首页磁贴,102代表单机游戏,103代表网络游戏
	 * 主干Htc3.0新协议：110=首页轮换 111=首页2xn磁贴 112=排行 113=游戏分类(特殊)
	 * 200=推荐游戏列表(终端v3.21开始) 2013-08-19 新增 114=首页3个固定资源 115=首页普通列表 (终端v3.22版本开始)
	 * 2013-12-26 新增 201=首页推荐游戏列表
	 * 
	 */
	private String listCode;

	// private Page page;

	private Integer pageIndex;// 页面id,最小从1开始

	private Short pageSize;// 一个页面内的元素个数

	private Byte listTag;// 2013-12-26添加 ，默认为null 1=代表来自分类请求

	public String getListCode() {
		return listCode;
	}

	public void setListCode(String listCode) {
		this.listCode = listCode;
	}

	// public Page getPage() {
	// return page;
	// }
	//
	// public void setPage(Page page) {
	// this.page = page;
	// }

	public Byte getListTag() {
		return listTag;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Short getPageSize() {
		return pageSize;
	}

	public void setPageSize(Short pageSize) {
		this.pageSize = pageSize;
	}

	public void setListTag(Byte listTag) {
		this.listTag = listTag;
	}
}
