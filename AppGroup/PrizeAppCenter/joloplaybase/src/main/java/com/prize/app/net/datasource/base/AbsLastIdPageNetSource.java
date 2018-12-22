package com.prize.app.net.datasource.base;

import com.prize.app.net.AbstractNetData;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseReq;
import com.prize.app.net.req.BaseResp;

public abstract class AbsLastIdPageNetSource<T extends AbstractNetData, V extends BaseReq, Q extends BaseResp>
		extends AbstractNetSource<T, V, Q> {
	// 1 标示是最后一页
	public static final byte LAST_PAGE_FLAG = 1;

	private short isLast;

	private int lastItemId;

	public boolean nextPage() {
		return isLast == LAST_PAGE_FLAG ? false : true;
	}

	public void resetLastItemId() {
		lastItemId = 0;
	}

	/**
	 * 设置是否为最后一页
	 * 
	 * @param isLast
	 */
	public void setIsLast(Short isLast) {
		this.isLast = (isLast == null ? Short.MIN_VALUE : isLast);
	}

	/**
	 * 设置Last id
	 * 
	 * @param lastItemId
	 */
	public void setLastItemId(Integer lastItemId) {
		this.lastItemId = (lastItemId == null ? Integer.MIN_VALUE : lastItemId);
	}

	public int getLastItemId() {
		return lastItemId;
	}

	/**
	 * 必须调用{@link #setIsLast}和{@link #setLastItemId}
	 */
	@Override
	protected abstract T parseResp(Q resp);

}
