package com.prize.app.beans;


public class PageBean {
	public static int FIRST_PAGE = 1;
	// 页面id,最小从1开始，但客户端一开始没有页，故初始值是0
	public int pageIndex = 0;

	// 一个页面内的元素个数
	public short pageSize = 20;

	// 总页面数
	public int pageCount = 1;

	// 总记录数
	public int pageItemCount;

	/**
	 * 是否有下一页
	 * 
	 * @return boolean
	 */
	public boolean hasNext() {
		return pageIndex + 1<=pageCount;
	}

	/**
	 * 获取当前页
	 * 
	 * @return pageIndex
	 */
	public int getCurrentPage() {
		return pageIndex;
	}

	public static Page convertToNetPage(PageBean pageBean) {
		Page page = new Page();
		if (null == pageBean) {
			return page;
		}

		page.setPageCount(pageBean.pageCount);
		page.setPageIndex(pageBean.pageIndex);
		page.setPageItemCount(pageBean.pageItemCount);
		page.setPageSize(pageBean.pageSize);
		return page;
	}

	public void setPage(Page netPage) {
		if (null == netPage) {
			return;
		}
		pageCount = netPage.getPageCount();
		pageIndex = netPage.getPageIndex();
		Integer itemCount = netPage.getPageItemCount();
		pageItemCount = (itemCount == null) ? -1 : itemCount.intValue();
		pageSize = netPage.getPageSize();
	}
}
