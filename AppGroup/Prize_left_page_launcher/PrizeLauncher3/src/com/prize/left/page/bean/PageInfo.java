package com.prize.left.page.bean;

public class PageInfo {
	/**
	 * "pageInfo": { "pageIndex": 1, "pageSize": 3, "pageCount": 1, "total": 3 }
	 */	
	public int pageCount;
	
	public int total;

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	
	
}
