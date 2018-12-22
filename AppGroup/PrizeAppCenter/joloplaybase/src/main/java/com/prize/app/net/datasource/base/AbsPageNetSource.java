package com.prize.app.net.datasource.base;

import com.prize.app.beans.Page;
import com.prize.app.net.AbstractNetData;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseReq;
import com.prize.app.net.req.BaseResp;

public abstract class AbsPageNetSource<T extends AbstractNetData, V extends BaseReq, Q extends BaseResp>
		extends AbstractNetSource<T, V, Q> {

	protected Page page = null;

	/** 起始页为 1 */
	private static final int FRIST_PAGE_NO = 1;

	public AbsPageNetSource() {
		page = new Page();
		page.setPageIndex(FRIST_PAGE_NO);
	}

	/**
	 * 判定是否为第一页
	 * 
	 * @return
	 */
	public boolean isFirstPage() {
		return page.getPageIndex() == FRIST_PAGE_NO;
	}

	/**
	 * 判定是否有下一页
	 * 
	 * @return
	 */
	public boolean nextPage() {
		int pageIndex = page.getPageIndex();
		if (pageIndex < page.getPageCount()) {
			page.setPageIndex(++pageIndex);
			return true;
		} else {
			return false;
		}
	}
}
