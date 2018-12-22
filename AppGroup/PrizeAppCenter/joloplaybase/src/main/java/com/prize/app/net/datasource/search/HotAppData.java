package com.prize.app.net.datasource.search;

import com.prize.app.beans.HotKeyBean;
import com.prize.app.net.AbstractNetData;

import java.util.ArrayList;
import java.util.List;

public class HotAppData extends AbstractNetData {
	/**
	 * 列表
	 */

	public int pageCount;
	public int pageIndex;
	public int pageSize;
	public int pageItemCount;
	public List<HotKeyBean> hotwords = new ArrayList<HotKeyBean>();
}
